package org.sopt.makers.internal.project.domain;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class)
})
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

    @Type(type = "string-array")
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

    @Type(type = "string-array")
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
        this.endAt = endAt == null ? this.endAt : endAt;
        this.serviceType = serviceType == null ? this.serviceType : serviceType;
        this.isAvailable = isAvailable == null ? this.isAvailable : isAvailable;
        this.summary = summary == null ? this.summary : summary;
        this.detail = detail == null ? this.detail : detail;
        this.logoImage = logoImage == null ? this.logoImage : logoImage;
        this.thumbnailImage = thumbnailImage == null ? this.thumbnailImage : thumbnailImage;
        this.images = images == null ? this.images : images;
        this.updatedAt = LocalDateTime.now();
    }
}