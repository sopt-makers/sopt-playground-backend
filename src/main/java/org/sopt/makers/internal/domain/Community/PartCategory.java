package org.sopt.makers.internal.domain.Community;

import lombok.*;
import org.sopt.makers.internal.domain.Part;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table
public class PartCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "sub_category")
    @Enumerated(EnumType.STRING)
    private Part subCategory;
}