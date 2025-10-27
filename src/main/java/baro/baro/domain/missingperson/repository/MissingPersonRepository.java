package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.MissingPerson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissingPersonRepository extends JpaRepository<MissingPerson, Long> {
    
    @Query(value = "SELECT DISTINCT mp.* FROM youfi.missing_persons mp " +
                   "INNER JOIN youfi.missing_cases mc ON mp.id = mc.missing_person_id " +
                   "WHERE mc.case_status = 'OPEN'",
           countQuery = "SELECT COUNT(DISTINCT mp.id) FROM youfi.missing_persons mp " +
                       "INNER JOIN youfi.missing_cases mc ON mp.id = mc.missing_person_id " +
                       "WHERE mc.case_status = 'OPEN'",
           nativeQuery = true)
    Page<MissingPerson> findAllOpenCases(Pageable pageable);

    @Query(value = """
SELECT DISTINCT
  mp.*,
  ST_Distance(
    mp.location::geography,
    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
  ) AS distance
FROM youfi.missing_persons mp
JOIN youfi.missing_cases mc ON mp.id = mc.missing_person_id
WHERE mc.case_status = 'OPEN'
  AND ST_DWithin(
    mp.location::geography,
    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
    :radius
  )
ORDER BY distance
""", nativeQuery = true)
    List<MissingPerson> findNearbyMissingPersons(@Param("latitude") Double latitude, 
                                                @Param("longitude") Double longitude, 
                                                @Param("radius") Integer radius);
}
