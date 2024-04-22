package org.sopt.makers.internal.community.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class AnonymousProfileImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;
}
