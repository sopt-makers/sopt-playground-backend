package org.sopt.makers.internal.domain.Community;

import lombok.*;
import org.sopt.makers.internal.domain.Promotion;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table
public class PromotionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long postId;

    @Column
    @Enumerated(EnumType.STRING)
    private Promotion subCategory;
}