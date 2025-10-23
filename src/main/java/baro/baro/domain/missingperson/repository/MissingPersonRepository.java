package baro.baro.domain.missingperson.repository;

import baro.baro.domain.missingperson.entity.MissingPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissingPersonRepository extends JpaRepository<MissingPerson, Long> {
    
    @Query(value = "SELECT * FROM youfi.missing_persons mp " +
                   "WHERE ST_DWithin(mp.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radius) " +
                   "ORDER BY ST_Distance(mp.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326))",
           nativeQuery = true)
    List<MissingPerson> findNearbyMissingPersons(@Param("latitude") Double latitude, 
                                                @Param("longitude") Double longitude, 
                                                @Param("radius") Integer radius);
}
