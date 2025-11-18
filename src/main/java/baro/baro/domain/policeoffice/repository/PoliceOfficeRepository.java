package baro.baro.domain.policeoffice.repository;

import baro.baro.domain.policeoffice.entity.PoliceOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoliceOfficeRepository extends JpaRepository<PoliceOffice, Long> {

    @Query(value = """
            SELECT po.*
            FROM youfi.police_offices po
            WHERE po.location IS NOT NULL
              AND ST_DWithin(
                    po.location::public.geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::public.geography,
                    :radiusMeters
                  )
            ORDER BY ST_Distance(
                    po.location::public.geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::public.geography
                  )
            LIMIT :limit
            """, nativeQuery = true)
    List<PoliceOffice> findNearbyOffices(@Param("latitude") Double latitude,
                                         @Param("longitude") Double longitude,
                                         @Param("radiusMeters") Integer radiusMeters,
                                         @Param("limit") Integer limit);
}
