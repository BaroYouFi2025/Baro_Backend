package baro.baro.domain.device.repository;

import baro.baro.domain.device.entity.Device;
import baro.baro.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByUuid(UUID uuid);
    List<Device> findByUser(User user);
    Optional<Device> findByIdAndUser(Long id, User user);
}
