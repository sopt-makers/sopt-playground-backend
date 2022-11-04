package org.sopt.makers.internal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "Authorization")
@Tag(name = "Image 관련 API", description = "Image upload와 관련있는 API들")
public class ImageController {

    @Value("${cloud.aws.bucket.project}")
    private String projectImageBucketName;

    @Value("${cloud.aws.bucket.profile}")
    private String profileImageBucketName;

    private final S3Presigner presigner;

    @Operation(summary = "이미지 업로드를 위한 presigned url 관련 API")
    @GetMapping("")
    public ResponseEntity<Map<String, String>> getImagePath (
            @RequestParam String filename,
            @RequestParam(required = false) String type
    ) {
        val keyName = "%s-%s".formatted(UUID.randomUUID().toString(), filename);
        val bucketName = type == null || type.equals("project") ? projectImageBucketName : profileImageBucketName;
        val objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .acl("public-read")
                .contentType("image/*")
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
