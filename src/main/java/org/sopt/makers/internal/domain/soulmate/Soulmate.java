package org.sopt.makers.internal.domain.soulmate;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "soulmate")
public class Soulmate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mate_id")
    private Long mateId;

    @Column(name = "other_mate_id")
    private Long opponentId;

    @Convert(converter = SoulmateStateConverter.class)
    private SoulmateState state;

    private Integer missionSequence;

    private LocalDateTime stateModifiedAt;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public void changeState (SoulmateState state, LocalDateTime now) {
        this.state = state;
        this.stateModifiedAt = now;
        if (SoulmateState.MatchingReady.equals(state)) {
            this.startAt = now;
        }
        if (SoulmateState.OpenProfile.equals(state) || SoulmateState.Disconnected.equals(state)) {
            this.endAt = now;
        }
    }

    public void matched (Long opponentId) {
        this.opponentId = opponentId;
        this.missionSequence = 1;
    }

    public void bothMissionCompleted (SoulmateState state, LocalDateTime now) {
        this.state = state;
        this.stateModifiedAt = now;
        this.missionSequence += 1;
    }
}
