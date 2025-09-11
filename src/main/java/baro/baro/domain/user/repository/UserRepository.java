package baro.baro.domain.user.repository;

import baro.baro.domain.user.service.UserService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
