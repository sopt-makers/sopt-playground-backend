package org.sopt.makers.internal.community.domain.category;

import lombok.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryGroup;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private CommunityCategoryCode code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_group")
    private CommunityCategoryGroup categoryGroup;

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

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Category> children = new ArrayList<>();

    @Column
    private Integer displayOrder;

    @Builder
    private Category(
        Long id,
        CommunityCategoryCode code,
        CommunityCategoryGroup categoryGroup,
        String name,
        String content,
        Boolean hasAll,
        Boolean hasBlind,
        Boolean hasQuestion,
        Boolean isActive,
        Category parent,
        List<Category> children,
        Integer displayOrder
    ) {
        this.id = id;
        this.code = code;
        this.categoryGroup = categoryGroup;
        this.name = name;
        this.content = content;
        this.hasAll = hasAll;
        this.hasBlind = hasBlind;
        this.hasQuestion = hasQuestion;
        this.isActive = isActive == null || isActive;
        this.parent = parent;
        this.children = children == null ? new ArrayList<>() : children;
        this.displayOrder = displayOrder;
    }
}