package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.CaseStatusType;
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
    
    @Query(value = "SELECT DISTINCT mp FROM MissingPerson mp " +
                   "JOIN MissingCase mc ON mc.missingPerson = mp " +
                   "WHERE mc.caseStatus = :caseStatus",
           countQuery = "SELECT COUNT(DISTINCT mp) FROM MissingPerson mp " +
                        "JOIN MissingCase mc ON mc.missingPerson = mp " +
                        "WHERE mc.caseStatus = :caseStatus")
    Page<MissingPerson> findAllOpenCases(@Param("caseStatus") CaseStatusType caseStatus, Pageable pageable);

    @Query(value = """
SELECT DISTINCT
  mp.*,
  ST_Distance(
    mp.location::public.geography,
    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::public.geography
  ) AS distance
FROM youfi.missing_persons mp
JOIN youfi.missing_cases mc ON mp.id = mc.missing_person_id
WHERE mc.case_status = 'OPEN'
  AND ST_DWithin(
    mp.location::public.geography,
    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::public.geography,
    :radius
  )
ORDER BY distance
""", nativeQuery = true)
    List<MissingPerson> findNearbyMissingPersons(@Param("latitude") Double latitude, 
                                                @Param("longitude") Double longitude, 
                                                @Param("radius") Integer radius);

    @Query("SELECT DISTINCT mp FROM MissingPerson mp " +
            "JOIN MissingCase mc ON mc.missingPerson = mp " +
            "WHERE mc.reportedBy.id = :userId AND mc.caseStatus = :caseStatus " +
            "ORDER BY mp.createdAt DESC")
    List<MissingPerson> findAllByReporterId(@Param("userId") Long userId,
                                            @Param("caseStatus") CaseStatusType caseStatus);
}
