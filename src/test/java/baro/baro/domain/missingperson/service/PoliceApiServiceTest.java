package baro.baro.domain.missingperson.service;

import baro.baro.domain.missingperson.dto.external.PoliceApiMissingPerson;
import baro.baro.domain.missingperson.dto.external.PoliceApiResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonPoliceResponse;
import baro.baro.domain.missingperson.entity.MissingPersonPolice;
import baro.baro.domain.missingperson.event.PhotoProcessingEvent;
import baro.baro.domain.missingperson.exception.MissingPersonErrorCode;
import baro.baro.domain.missingperson.exception.MissingPersonException;
import baro.baro.domain.missingperson.repository.MissingPersonPoliceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PoliceApiServiceTest {

    @Mock
    private WebClient policeApiWebClient;
    @Mock
    private MissingPersonPoliceRepository policeRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private PoliceApiService policeApiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(policeApiService, "esntlId", "esntl");
        ReflectionTestUtils.setField(policeApiService, "authKey", "auth");
        ReflectionTestUtils.setField(policeApiService, "rowSize", 1);

        lenient().when(policeApiWebClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(org.mockito.ArgumentMatchers.<Function<UriBuilder, URI>>any()))
                .thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void syncMissingPersonsFromPoliceApi_persistsEntitiesAndPublishesEvents() {
        PoliceApiResponse firstPage = responseWith(2, List.of(missingPerson(1L, "Kim", "photo-a")));
        PoliceApiResponse secondPage = responseWith(2, List.of(missingPerson(2L, "Lee", "photo-b")));

        when(responseSpec.bodyToMono(PoliceApiResponse.class))
                .thenReturn(Mono.just(firstPage), Mono.just(secondPage));
        when(policeRepository.findAllIds()).thenReturn(Set.of(2L));
        when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(), any())).thenReturn(new int[0][0]);

        policeApiService.syncMissingPersonsFromPoliceApi();

        ArgumentCaptor<List<MissingPersonPolice>> entitiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(anyString(), entitiesCaptor.capture(), eq(100), any());
        List<MissingPersonPolice> persistedEntities = entitiesCaptor.getValue();
        assertThat(persistedEntities).hasSize(2);
        assertThat(persistedEntities.stream().map(MissingPersonPolice::getId)).containsExactly(1L, 2L);

        ArgumentCaptor<PhotoProcessingEvent> eventCaptor = ArgumentCaptor.forClass(PhotoProcessingEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
        List<PhotoProcessingEvent> events = eventCaptor.getAllValues();

        assertThat(events.get(0).getMissingPersonId()).isEqualTo(1L);
        assertThat(events.get(0).getPhotoBase64Data()).isEqualTo("photo-a");
        assertThat(events.get(0).isPreserveExistingOnFailure()).isFalse();
        assertThat(events.get(1).getMissingPersonId()).isEqualTo(2L);
        assertThat(events.get(1).isPreserveExistingOnFailure()).isTrue();

        verify(policeRepository).findAllIds();
    }

    @Test
    void syncMissingPersonsFromPoliceApi_whenFirstPageMissingList_doesNotPersistAnything() {
        PoliceApiResponse emptyResponse = new PoliceApiResponse();
        emptyResponse.setTotalCount(0);
        emptyResponse.setList(null);

        when(responseSpec.bodyToMono(PoliceApiResponse.class)).thenReturn(Mono.just(emptyResponse));

        policeApiService.syncMissingPersonsFromPoliceApi();

        verifyNoInteractions(policeRepository, jdbcTemplate, eventPublisher);
    }

    @Test
    void syncMissingPersonsFromPoliceApi_whenNetworkError_throwsMissingPersonException() {
        WebClientRequestException networkException = new WebClientRequestException(
                new IOException("fail"), HttpMethod.POST, URI.create("https://police.local"), new HttpHeaders());
        when(responseSpec.bodyToMono(PoliceApiResponse.class)).thenReturn(Mono.error(networkException));

        MissingPersonException exception = assertThrows(MissingPersonException.class,
                () -> policeApiService.syncMissingPersonsFromPoliceApi());

        assertThat(exception.getMissingPersonErrorCode())
                .isEqualTo(MissingPersonErrorCode.POLICE_API_NETWORK_ERROR);
    }

    @Test
    void syncMissingPersonsFromPoliceApi_whenResponseError_throwsMissingPersonException() {
        WebClientResponseException responseException = WebClientResponseException.create(
                500, "Internal Server Error", HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
        when(responseSpec.bodyToMono(PoliceApiResponse.class)).thenReturn(Mono.error(responseException));

        MissingPersonException exception = assertThrows(MissingPersonException.class,
                () -> policeApiService.syncMissingPersonsFromPoliceApi());

        assertThat(exception.getMissingPersonErrorCode())
                .isEqualTo(MissingPersonErrorCode.POLICE_API_RESPONSE_ERROR);
    }

    @Test
    void syncMissingPersonsFromPoliceApi_whenDataIntegrityViolationOccurs_throwsMissingPersonException() {
        PoliceApiResponse singlePage = responseWith(1, List.of(missingPerson(3L, "Choi", "photo-c")));

        when(responseSpec.bodyToMono(PoliceApiResponse.class)).thenReturn(Mono.just(singlePage));
        when(policeRepository.findAllIds()).thenReturn(Set.of());
        when(jdbcTemplate.batchUpdate(anyString(), anyList(), anyInt(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        MissingPersonException exception = assertThrows(MissingPersonException.class,
                () -> policeApiService.syncMissingPersonsFromPoliceApi());

        assertThat(exception.getMissingPersonErrorCode())
                .isEqualTo(MissingPersonErrorCode.POLICE_API_DATA_SAVE_FAILED);
    }

    @Test
    void syncMissingPersonsFromPoliceApi_whenUnexpectedErrorOccurs_throwsGenericMissingPersonException() {
        PoliceApiResponse singlePage = responseWith(1, List.of(missingPerson(5L, "Han", "photo-d")));

        when(responseSpec.bodyToMono(PoliceApiResponse.class)).thenReturn(Mono.just(singlePage));
        when(policeRepository.findAllIds()).thenThrow(new IllegalStateException("boom"));

        MissingPersonException exception = assertThrows(MissingPersonException.class,
                () -> policeApiService.syncMissingPersonsFromPoliceApi());

        assertThat(exception.getMissingPersonErrorCode())
                .isEqualTo(MissingPersonErrorCode.POLICE_API_CALL_FAILED);
    }

    @Test
    void getAllMissingPersons_returnsMappedPage() {
        Pageable pageable = PageRequest.of(0, 5);
        MissingPersonPolice entity = MissingPersonPolice.createFromPoliceApi(
                missingPerson(10L, "Park", "encoded"));
        Page<MissingPersonPolice> page = new PageImpl<>(List.of(entity), pageable, 1);

        when(policeRepository.findAll(pageable)).thenReturn(page);

        Page<MissingPersonPoliceResponse> result = policeApiService.getAllMissingPersons(pageable);

        assertThat(result.getContent()).hasSize(1);
        MissingPersonPoliceResponse dto = result.getContent().get(0);
        assertThat(dto.getMissingPersonPoliceId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Park");
        assertThat(dto.getDress()).isEqualTo("blue jacket");
    }

    @Test
    void getMissingPersonById_whenNotFound_throwsException() {
        when(policeRepository.findById(404L)).thenReturn(Optional.empty());

        MissingPersonException exception = assertThrows(MissingPersonException.class,
                () -> policeApiService.getMissingPersonById(404L));

        assertThat(exception.getMissingPersonErrorCode())
                .isEqualTo(MissingPersonErrorCode.MISSING_PERSON_NOT_FOUND);
    }

    private PoliceApiResponse responseWith(int totalCount, List<PoliceApiMissingPerson> list) {
        PoliceApiResponse response = new PoliceApiResponse();
        response.setTotalCount(totalCount);
        response.setList(list);
        response.setResult("OK");
        response.setMsg("SUCCESS");
        return response;
    }

    private PoliceApiMissingPerson missingPerson(long id, String name, String photoBase64) {
        PoliceApiMissingPerson person = new PoliceApiMissingPerson();
        person.setMsspsnIdntfccd(id);
        person.setOccrde("20240101");
        person.setAlldressingDscd("blue jacket");
        person.setAgeNow("10");
        person.setAge(7);
        person.setWritngTrgetDscd("STATUS");
        person.setSexdstnDscd("M");
        person.setEtcSpfeatr("scar");
        person.setOccrAdres("Seoul");
        person.setNm(name);
        person.setTknphotolength(120);
        person.setTknphotoFile(photoBase64);
        return person;
    }
}
