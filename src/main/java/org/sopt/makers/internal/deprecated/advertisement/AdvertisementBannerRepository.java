package org.sopt.makers.internal.deprecated.advertisement;

import org.sopt.makers.internal.deprecated.advertisement.domain.AdvertisementBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdvertisementBannerRepository extends JpaRepository<AdvertisementBanner, Long> {

    @Query("SELECT ab FROM AdvertisementBanner ab WHERE ab.company.isActive = true")
    List<AdvertisementBanner> findAllByActiveCompany();
}
