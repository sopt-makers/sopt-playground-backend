package org.sopt.makers.internal.deprecated.soulmate.domain;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "soulmate_mission_history")
public class SoulmateMissionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "soulmate_id")
    private Long soulmateId;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "mission_sequence")
    private Integer missionSequence;

    private String message;

    private LocalDateTime sentAt;
}
