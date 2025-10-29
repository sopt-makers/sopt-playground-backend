package org.sopt.makers.internal.popup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import org.sopt.makers.internal.common.AuditingTimeEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Popup extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String pcImageUrl;

    @Column(nullable = false)
    private String mobileImageUrl;

    @Column
    private String linkUrl;

    @Column
    private Boolean openInNewTab;

    @Column
    private Boolean showOnlyToRecentGeneration;

    public void update(
            LocalDate startDate,
            LocalDate endDate,
            String pcImageUrl,
            String mobileImageUrl,
            String linkUrl,
            Boolean openInNewTab,
            Boolean showOnlyToRecentGeneration
    ) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.pcImageUrl = pcImageUrl;
        this.mobileImageUrl = mobileImageUrl;
        this.linkUrl = linkUrl;
        this.openInNewTab = openInNewTab;
        this.showOnlyToRecentGeneration = showOnlyToRecentGeneration;
    }
}
