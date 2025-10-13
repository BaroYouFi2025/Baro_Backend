package baro.baro.domain.member.repository;

import baro.baro.domain.member.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
}
