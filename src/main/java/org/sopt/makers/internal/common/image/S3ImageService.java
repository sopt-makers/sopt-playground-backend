package org.sopt.makers.internal.common.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sopt.makers.internal.auth.AuthConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final S3Client s3Client;
    private final AuthConfig authConfig;

    @Value("${cloud.aws.bucket.image}")
    private String imageBucketName;

    /**
     * MultipartFile을 S3에 업로드합니다.
     * @param file 업로드할 파일
     * @param type 이미지 타입 (popup, project, profile 등)
     * @return S3 이미지 URL
     */
    public String uploadImage(MultipartFile file, String type) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID() + "-" + originalFilename;

        // S3 키 생성 (환경별 경로)
        String key = "/" + authConfig.getActiveProfile() + "/image/" + type + "/" + filename;

        // Content-Type 설정
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }

        // S3에 업로드
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(imageBucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // path-style URL 반환
            String imageUrl = "https://s3.ap-northeast-2.amazonaws.com/" + imageBucketName + "/" + key;
            return imageUrl;
        } catch (Exception e) {
            log.error("Failed to upload image to S3: {}", filename, e);
            throw new IOException("S3 업로드 실패", e);
        }
    }

    /**
     * S3에서 이미지를 삭제합니다.
     * @param imageUrl S3 이미지 URL
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("이미지 URL이 비어 있습니다.");
            return;
        }

        try {
            // URL에서 키 추출
            String key = extractKeyFromUrl(imageUrl);
            if (key == null) {
                log.warn("Failed to extract key from URL: {}", imageUrl);
                return;
            }

            // S3에서 객체 삭제
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(imageBucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted image from S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete image from S3: {}", imageUrl, e);
            // 삭제 실패 시에도 계속 진행 (로그만 남김)
        }
    }

    /**
     * S3 URL에서 키를 추출합니다.
     */
    private String extractKeyFromUrl(String imageUrl) {
        try {
            // S3 path-style URL 파싱
            String bucketPrefix = "sopt-makers-internal/";
            int bucketIndex = imageUrl.indexOf(bucketPrefix);
            if (bucketIndex != -1) {
                return imageUrl.substring(bucketIndex + bucketPrefix.length());
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to extract key from URL: {}", imageUrl, e);
            return null;
        }
    }
}
