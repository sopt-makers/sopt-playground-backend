package org.sopt.makers.internal.community.domain.category;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column
    private String name;

    @Column
    private String content;

    @Column
    private Boolean hasAll;

    @Column
    private Boolean hasBlind;

    @Column
    private Boolean hasQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @Column
    private Integer displayOrder;

    @Builder
    private Category(Long id, String name, String content, Boolean hasAll, Boolean hasBlind,
                     Boolean hasQuestion, Category parent, List<Category> children, Integer displayOrder) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.hasAll = hasAll;
        this.hasBlind = hasBlind;
        this.hasQuestion = hasQuestion;
        this.parent = parent;
        this.children = children;
        this.displayOrder = displayOrder;
    }
}