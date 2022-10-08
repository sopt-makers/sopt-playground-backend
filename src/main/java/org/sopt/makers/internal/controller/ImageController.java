package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presigned-url")
public class ImageController {

    @Value("${cloud.aws.bucket.project}")
    private String projectImageBucketName;

    private final S3Presigner presigner;

    @Operation(summary = "유저 id로 조회 API")
    @GetMapping("")
    public ResponseEntity<Map<String, String>> getImagePath (@RequestParam String filename) {
        val keyName = "%s-%s".formatted(UUID.randomUUID().toString(), filename);
        val objectRequest = PutObjectRequest.builder()
                .bucket(projectImageBucketName)
                .acl("public-read")
                .key(keyName)
                .build();

        val presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        val presignedRequest = presigner.presignPutObject(presignRequest);
        val signedUrl = presignedRequest.url().toString();
        val response = Map.of("signedUrl", signedUrl, "filename", keyName);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
