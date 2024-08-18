package org.sopt.makers.internal.controller.advertisement;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.dto.advertisement.response.AdvertisementResponse;
import org.sopt.makers.internal.service.advertisement.AdvertisementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/advertisement")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping("")
    public ResponseEntity<List<AdvertisementResponse>> getAdvertisement() {
        return ResponseEntity.status(HttpStatus.OK).body(advertisementService.getActiveCompanyBanners());
    }
}
