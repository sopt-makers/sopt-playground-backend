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
    private Long mateOneId;

    @Column(name = "other_mate_id")
    private Long mateTwoId;

    private SoulmateState state;

    private LocalDateTime stateModifiedAt;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}
