package baro.baro.domain.member.repository;

import baro.baro.domain.member.entity.GpsTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GpsTrackRepository extends JpaRepository<GpsTrack, Long> {

}
