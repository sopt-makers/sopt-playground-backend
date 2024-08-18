package org.sopt.makers.internal.service.advertisement;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.domain.advertisement.AdvertisementBanner;
import org.sopt.makers.internal.dto.advertisement.response.AdvertisementResponse;
import org.sopt.makers.internal.repository.advertisement.AdvertisementBannerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementBannerRepository advertisementBannerRepository;

    public List<AdvertisementResponse> getActiveCompanyBanners() {
        List<AdvertisementBanner> banners = advertisementBannerRepository.findAllByActiveCompany();
        return banners.stream()
                .map(banner -> new AdvertisementResponse(banner.getId(), banner.getBannerUrl(), banner.getImageUrl(), banner.getCompany().getCompanyName()))
                .collect(Collectors.toList());
    }
}
