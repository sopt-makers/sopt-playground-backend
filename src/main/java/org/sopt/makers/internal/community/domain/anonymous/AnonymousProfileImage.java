package org.sopt.makers.internal.community.domain.anonymous;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnonymousProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @Builder
    private AnonymousProfileImage(Long id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }
}
