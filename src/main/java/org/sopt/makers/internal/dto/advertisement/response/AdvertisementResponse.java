package org.sopt.makers.internal.dto.advertisement.response;

public record AdvertisementResponse(

        Long id,

        String bannerUrl,

        String imageUrl,

        String companyName
) {
}
