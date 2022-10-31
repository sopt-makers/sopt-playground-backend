package org.sopt.makers.internal.controller;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.dto.auth.AccessTokenResponse;
import org.sopt.makers.internal.dto.auth.AuthByFacebookRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class HealthCheckController {

    @GetMapping("")
    public ResponseEntity<String> healthCheck () {
        return ResponseEntity.status(HttpStatus.OK).body("Hello Internal!");
    }
}
