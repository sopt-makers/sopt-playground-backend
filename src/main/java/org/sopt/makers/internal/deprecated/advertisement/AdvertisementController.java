package org.sopt.makers.internal.deprecated.advertisement;

import lombok.RequiredArgsConstructor;
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
