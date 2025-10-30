package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.Sighting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SightingRepository extends JpaRepository<Sighting, Long> {
}

