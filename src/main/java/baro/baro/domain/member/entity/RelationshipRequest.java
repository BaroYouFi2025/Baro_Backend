package baro.baro.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import baro.baro.domain.common.enums.RelationshipRequestStatus;
import baro.baro.domain.user.entity.User;

import java.time.ZonedDateTime;

@Entity
@Table(name = "relationship_requests", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationshipRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_user_id", nullable = false)
    private User inviterUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_user_id")
    private User inviteeUser;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RelationshipRequestStatus status = RelationshipRequestStatus.PENDING;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
    
    @Column(name = "responded_at")
    private ZonedDateTime respondedAt;
}