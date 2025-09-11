package baro.baro.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import baro.baro.domain.user.entity.User;
import baro.baro.domain.missingperson.entity.MissingPerson;

import java.time.ZonedDateTime;

@Entity
@Table(name = "relationships", schema = "youfi")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Relationship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MissingPerson member;
    
    @Column(length = 50)
    private String relation;
    
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}