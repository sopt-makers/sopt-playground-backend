package org.sopt.makers.internal.project.domain;

import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "creator_id")
    private Long writerId;

    @Column(name = "generation")
    private Integer generation;

    @Column(name = "category")
    private String category;

    @Column(name = "start_at")
    private LocalDate startAt;

    @Column(name = "end_at")
    private LocalDate endAt;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "service_type", columnDefinition = "text[]")
    private String[] serviceType;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "is_founding")
    private Boolean isFounding;

    private String summary;

    private String detail;

    @Column(name = "logo_image")
    private String logoImage;

    @Column(name = "thumbnail_image")
    private String thumbnailImage;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "images", columnDefinition = "text[]")
    private String[] images;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateAll (
            String name,
            Integer generation,
            String category,
            LocalDate startAt,
            LocalDate endAt,
            String[] serviceType,
            Boolean isAvailable,
            Boolean isFounding,
            String summary,
            String detail,
            String logoImage,
            String thumbnailImage,
            String[] images
    ) {
        this.name = name == null ? this.name : name;
        this.generation = generation == null ? this.generation : generation;
        this.category = category == null ? this.category : category;
        this.startAt = startAt == null ? this.startAt : startAt;
        this.endAt = endAt;
        this.serviceType = serviceType == null ? this.serviceType : serviceType;
        this.isAvailable = isAvailable == null ? this.isAvailable : isAvailable;
        this.isFounding = isFounding == null ? this.isFounding : isFounding;
        this.summary = summary == null ? this.summary : summary;
        this.detail = detail == null ? this.detail : detail;
        this.logoImage = logoImage == null ? this.logoImage : logoImage;
        this.thumbnailImage = thumbnailImage == null ? this.thumbnailImage : thumbnailImage;
        this.images = images == null ? this.images : images;
        this.updatedAt = LocalDateTime.now();
    }
}