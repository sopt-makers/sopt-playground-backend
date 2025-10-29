package org.sopt.makers.internal.popup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Popup {

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Popup of(
            LocalDate startDate,
            LocalDate endDate,
            String pcImageUrl,
            String mobileImageUrl,
            String linkUrl,
            Boolean openInNewTab,
            Boolean showOnlyToRecentGeneration
    ) {
        Popup popup = new Popup();
        popup.startDate = startDate;
        popup.endDate = endDate;
        popup.pcImageUrl = pcImageUrl;
        popup.mobileImageUrl = mobileImageUrl;
        popup.linkUrl = linkUrl;
        popup.openInNewTab = openInNewTab;
        popup.showOnlyToRecentGeneration = showOnlyToRecentGeneration;
        return popup;
    }

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