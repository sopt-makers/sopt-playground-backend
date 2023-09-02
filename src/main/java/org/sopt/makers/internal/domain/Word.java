package org.sopt.makers.internal.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "word")
public class Word implements Comparable<Word>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long memberId;

    @Column
    private String word;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Override
    public int compareTo(Word o) {
        return this.id.compareTo(o.id);
    }
}