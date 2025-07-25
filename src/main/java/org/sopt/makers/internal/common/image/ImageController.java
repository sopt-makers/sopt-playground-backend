package org.sopt.makers.internal.common.image;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.common.image.ImageRequest;
import org.sopt.makers.internal.common.image.ImageResponse;
import org.sopt.makers.internal.exception.WrongImageInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/presigned-url")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Image 관련 API", description = "Image upload와 관련있는 API들")
public class ImageController {

    @Value("${spring.profiles.active}")
    private String activeProfile;
    @Value("${cloud.aws.bucket.image}")
    private String imageBucketName;

    private final S3Presigner presigner;
    private final String projectImageBucketPath = "/image/project/";
    private final String profileImageBucketPath = "/image/profile/";

    @Operation(summary = "이미지 업로드를 위한 presigned url 관련 API")
    @GetMapping("")
    public ResponseEntity<Map<String, String>> getImagePath (
            @RequestParam String filename,
            @RequestParam(required = false) String type
    ) {
        val bucketPathName = type == null || type.equals("project") ? projectImageBucketPath : profileImageBucketPath;
        val keyName = "/" + activeProfile + bucketPathName + UUID.randomUUID() + "-" + filename;
        val bucketName = imageBucketName;
        val splittedFileName = filename.split("\\.");
        var extension = splittedFileName[splittedFileName.length-1].toLowerCase();
        if (extension.equals("jpg")) extension = "jpeg";
        val contentType = "image/" + extension;
        val objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentType(contentType)
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

    @Operation(summary = "multiple 이미지 업로드를 위한 presigned url 관련 API")
    @PostMapping("")
    public ResponseEntity<List<ImageResponse>> getImagesUrls (
            @RequestBody List<ImageRequest> imageRequests
    ) {
        if (imageRequests.size() > 10) throw new WrongImageInputException("이미지 개수를 초과했습니다.", "OutOfNumberImages");

        val responses = imageRequests.stream().map(request -> {
            val type = request.type();
            val filename = request.filename();
            val bucketPathName = type == null || type.equals("project") ? projectImageBucketPath : profileImageBucketPath;
            val keyName = "/" + activeProfile + bucketPathName + "%s-%s".formatted(UUID.randomUUID().toString(), filename);
            val bucketName = imageBucketName;
            val splittedFileName = filename.split("\\.");
            var extension = splittedFileName[splittedFileName.length-1].toLowerCase();
            if (extension.equals("jpg")) extension = "jpeg";
            val contentType = "image/" + extension;
            val objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .contentType(contentType)
                    .build();

            val presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            val presignedRequest = presigner.presignPutObject(presignRequest);
            val signedUrl = presignedRequest.url().toString();
            return new ImageResponse(signedUrl, keyName);
        }).toList();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}
