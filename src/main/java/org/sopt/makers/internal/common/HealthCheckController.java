package org.sopt.makers.internal.common;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
