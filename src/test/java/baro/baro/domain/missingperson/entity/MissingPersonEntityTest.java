package baro.baro.domain.missingperson.entity;

import baro.baro.domain.missingperson.entity.GenderType;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MissingPersonEntityTest {

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void fromCreatesEntityWithProvidedFields() {
        Point location = geometryFactory.createPoint(new Coordinate(127.0, 37.5));
        MissingPerson person = MissingPerson.from(
                "홍길동",
                "2010-01-01",
                "MALE",
                "2024-01-01T10:00:00",
                "마른 체형",
                "왼팔에 흉터",
                "파란 셔츠",
                "청바지",
                "빨간 운동화",
                140,
                35,
                "https://jjm.jojaemin.com/images/2025/11/23/b6e3968f-4ea2-48de-8fa2-eca5c60adc87.jpeg",
                location,
                "서울시"
        );

        assertThat(person.getName()).isEqualTo("홍길동");
        assertThat(person.getGender()).isEqualTo(GenderType.MALE);
        assertThat(person.getBody()).contains("마른");
        assertThat(person.getLocation()).isEqualTo(location);
        assertThat(person.getAddress()).isEqualTo("서울시");
    }

    @Test
    void updateFromReplacesOnlyNonNullFields() {
        Point initialLocation = geometryFactory.createPoint(new Coordinate(127.0, 37.5));
        MissingPerson person = MissingPerson.builder()
                .name("홍길동")
                .birthDate(LocalDate.of(2010, 1, 1))
                .body("마른")
                .location(initialLocation)
                .address("서울")
                .missingDate(LocalDateTime.parse("2024-01-01T10:00:00"))
                .build();

        Point newLocation = geometryFactory.createPoint(new Coordinate(126.9, 37.4));
        person.updateFrom(
                "김철수",
                "2011-02-02",
                "건장",
                null,
                null,
                null,
                null,
                150,
                40,
                newLocation,
                "부산",
                "2024-02-01T12:00:00"
        );

        assertThat(person.getName()).isEqualTo("김철수");
        assertThat(person.getBody()).isEqualTo("건장");
        assertThat(person.getBodyEtc()).isNull();
        assertThat(person.getHeight()).isEqualTo(150);
        assertThat(person.getLocation()).isEqualTo(newLocation);
        assertThat(person.getAddress()).isEqualTo("부산");
    }
}
