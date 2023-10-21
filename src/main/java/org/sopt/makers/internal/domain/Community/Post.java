package org.sopt.makers.internal.domain.Community;

import lombok.*;
import org.hibernate.annotations.Type;
//import org.sopt.makers.internal.domain.CategoryComment;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
//import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "writer_id")
    private String writerId;

    @Column
    private String title;

    @Column
    private String content;

    @Column
    private Integer hits;

    @Type(type = "string-array")
    @Column(name = "images", columnDefinition = "text[]")
    private String[] images;

    @Column(name = "is_question")
    private Boolean isQuestion;

    @Column(name = "is_blind_writer")
    private Boolean isBlindWriter;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

//    @Builder.Default
//    @OneToMany(
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    @JoinColumn(name = "comment_id")
//    private List<CategoryComment> comments = new ArrayList<>();
}