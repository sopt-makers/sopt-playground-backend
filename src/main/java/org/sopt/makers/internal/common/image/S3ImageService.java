package org.sopt.makers.internal.common.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.bucket.image}")
    private String imageBucketName;

    /**
     * S3에서 이미지를 삭제합니다.
     * @param imageUrl S3 이미지 URL (예: https://s3.ap-northeast-2.amazonaws.com/sopt-makers-internal//lambda-dev/image/popup/xxx.jpg)
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
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
     * 예: https://s3.ap-northeast-2.amazonaws.com/sopt-makers-internal//lambda-dev/image/popup/xxx.jpg
     * -> /lambda-dev/image/popup/xxx.jpg
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
