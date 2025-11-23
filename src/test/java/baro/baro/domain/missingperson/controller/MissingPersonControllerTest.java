package baro.baro.domain.missingperson.controller;

import baro.baro.domain.missingperson.dto.req.RegisterMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.ReportSightingRequest;
import baro.baro.domain.missingperson.dto.req.SearchMissingPersonRequest;
import baro.baro.domain.missingperson.dto.req.UpdateMissingPersonRequest;
import baro.baro.domain.missingperson.dto.res.MissingPersonDetailResponse;
import baro.baro.domain.missingperson.dto.res.MissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.RegisterMissingPersonResponse;
import baro.baro.domain.missingperson.dto.res.ReportSightingResponse;
import baro.baro.domain.missingperson.service.MissingPersonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MissingPersonControllerTest {

    @InjectMocks
    private MissingPersonController missingPersonController;

    @Mock
    private MissingPersonService missingPersonService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(missingPersonController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void registerMissingPersonReturnsResponse() throws Exception {
        RegisterMissingPersonRequest request = RegisterMissingPersonRequest.create(
                "홍길동", "2010-01-01", "url", "2024-01-01T00:00:00",
                150, 40, "체형", "특징", "상의", "하의", "기타", 37.5, 127.0, "MALE");
        RegisterMissingPersonResponse response = RegisterMissingPersonResponse.create(1L);
        when(missingPersonService.registerMissingPerson(any())).thenReturn(response);

        mockMvc.perform(post("/missing-persons/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingPersonId").value(1L));

        verify(missingPersonService).registerMissingPerson(any());
    }

    @Test
    void updateMissingPersonDelegatesToService() throws Exception {
        UpdateMissingPersonRequest request = UpdateMissingPersonRequest.create(
                "홍길동", "2010-01-01", "url", "2024-01-01T00:00:00",
                150, 40, "체형", "특징", "상의", "하의", "기타", 37.5, 127.0);
        RegisterMissingPersonResponse response = RegisterMissingPersonResponse.create(2L);
        when(missingPersonService.updateMissingPerson(2L, request)).thenReturn(response);

        mockMvc.perform(put("/missing-persons/register/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.missingPersonId").value(2L));
    }

    @Test
    void searchMissingPersonsReturnsPage() throws Exception {
        MissingPersonResponse dto = MissingPersonResponse.create(1L, "홍길동", "서울",
                "2024-01-01T00:00:00", 150, 40, "체형", "url");
        Page<MissingPersonResponse> page = new PageImpl<>(List.of(dto), org.springframework.data.domain.PageRequest.of(0, 5), 1);
        when(missingPersonService.searchMissingPersons(any(SearchMissingPersonRequest.class))).thenReturn(page);

        mockMvc.perform(get("/missing-persons/search")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("홍길동"));
    }

    @Test
    void getMissingPersonDetailReturnsDto() throws Exception {
        MissingPersonDetailResponse response = MissingPersonDetailResponse.create(
                10L, "홍길동", "2010-01-01", "부산", "2024-01-01T00:00:00",
                150, 40, "체형", "특징", "상의", "하의", "기타", 37.5, 127.0, "url", null, null);
        when(missingPersonService.getMissingPersonDetail(10L)).thenReturn(response);

        mockMvc.perform(get("/missing-persons/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    void reportSightingReturnsResponse() throws Exception {
        ReportSightingRequest request = ReportSightingRequest.builder()
                .missingPersonId(5L)
                .latitude(37.4)
                .longitude(126.9)
                .build();
        ReportSightingResponse response = ReportSightingResponse.success();
        when(missingPersonService.reportSighting(any())).thenReturn(response);

        mockMvc.perform(post("/missing-persons/sightings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(response.getMessage()));
    }
}
