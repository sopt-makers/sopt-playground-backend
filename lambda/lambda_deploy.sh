#!/bin/bash

set -e  # 에러 발생 시 스크립트 중단

echo "aws profile 을 입력해주세요 (기본 configuration 활용 시, default 입력) :"
read profile
if [ -z "$profile" ]; then
    profile="default"
fi
profile=$(echo $profile | tr '[:upper:]' '[:lower:]')

echo "배포 방식을 선택해주세요 [ dev | prod ] :"
read mode
if [ -z "$mode" ]; then
    mode="dev"
fi
mode=$(echo $mode | tr '[:upper:]' '[:lower:]')

echo "=== 컨테이너 이미지 기반 람다 배포 시작 ==="

# 환경별 리전 및 리포지토리 설정
REGION="ap-northeast-2"
ACCOUNT_ID="379013966998"
if [ "$mode" = "prod" ]; then
  REPOSITORY_NAME="playground-prod"
else
  REPOSITORY_NAME="playground-dev"
fi

REPO_URI="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$REPOSITORY_NAME"

echo "사용할 설정:"
echo "  - Profile: $profile"
echo "  - Mode: $mode"
echo "  - Region: $REGION"
echo "  - Repository: $REPOSITORY_NAME"
echo "  - ECR URI: $REPO_URI"

# 타임스탬프 기반 고유 태그 생성
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
IMAGE_TAG="build-$TIMESTAMP"

echo "=== 1단계: ECR 로그인 ==="
# 공개 ECR 로그인 (Lambda adapter 이미지 접근용)
aws ecr-public get-login-password --region us-east-1 --profile "$profile" | docker login --username AWS --password-stdin public.ecr.aws
if [ $? -ne 0 ]; then
    echo "❌ 공개 ECR 로그인 실패"
    exit 1
fi

# 개인 ECR 로그인 (이미지 푸시용)
aws ecr get-login-password --region "$REGION" --profile "$profile" | docker login --username AWS --password-stdin "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"
if [ $? -ne 0 ]; then
    echo "❌ 개인 ECR 로그인 실패"
    exit 1
fi
echo "✅ ECR 로그인 완료"

echo "=== 2단계: Docker를 사용한 GraalVM 네이티브 빌드 ==="
echo "Docker를 사용하여 GraalVM 네이티브 컴파일을 수행합니다..."
docker build --platform=linux/arm64 -t "$REPOSITORY_NAME:$IMAGE_TAG" .
if [ $? -ne 0 ]; then
    echo "❌ Docker 네이티브 빌드 실패"
    exit 1
fi
echo "✅ Docker 네이티브 빌드 완료"

echo "=== 3단계: ECR 리포지토리 확인/생성 ==="
echo "리포지토리 확인 중: $REPOSITORY_NAME"
aws ecr describe-repositories --region "$REGION" --profile "$profile" --repository-names "$REPOSITORY_NAME" > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "ECR 리포지토리가 존재하지 않습니다. 생성 중..."
    aws ecr create-repository --region "$REGION" --profile "$profile" --repository-name "$REPOSITORY_NAME" --image-scanning-configuration scanOnPush=true
    if [ $? -ne 0 ]; then
        echo "❌ ECR 리포지토리 생성 실패"
        exit 1
    fi
    echo "✅ ECR 리포지토리 생성 완료"
else
    echo "✅ ECR 리포지토리 존재 확인"
fi

echo "=== 4단계: ECR에 이미지 푸시 ==="
# 고유 태그로 이미지 푸시
docker tag "$REPOSITORY_NAME:$IMAGE_TAG" "$REPO_URI:$IMAGE_TAG"
docker push "$REPO_URI:$IMAGE_TAG"
if [ $? -ne 0 ]; then
    echo "❌ ECR 이미지 푸시 실패"
    exit 1
fi

# latest 태그도 업데이트
docker tag "$REPOSITORY_NAME:$IMAGE_TAG" "$REPO_URI:latest"
docker push "$REPO_URI:latest"
if [ $? -ne 0 ]; then
    echo "❌ ECR latest 태그 업데이트 실패"
    exit 1
fi

echo "✅ ECR 이미지 푸시 완료"

echo "=== 5단계: 로컬 이미지 정리 ==="
echo "로컬 Docker 이미지 정리 중..."
docker rmi "$REPOSITORY_NAME:$IMAGE_TAG" 2>/dev/null || true
docker rmi "$REPO_URI:$IMAGE_TAG" 2>/dev/null || true
docker rmi "$REPO_URI:latest" 2>/dev/null || true

echo "✅ 로컬 이미지 정리 완료"

echo "=== 6단계: Lambda 배포 ==="
cd lambda

# 고유 태그로 배포
IMAGE_URI="$REPO_URI:$IMAGE_TAG"
echo "배포 대상 이미지: $IMAGE_URI"

sam deploy \
  --config-env "$mode" \
  --profile "$profile" \
  --parameter-overrides ImageUri="$IMAGE_URI"


# echo "Private ECR 이미지 정리: latest 태그 제거"
# aws ecr batch-delete-image \
#   --region "$REGION" \
#   --profile "$profile" \
#   --repository-name "$PRIVATE_REPO_NAME" \
#   --image-ids imageTag=latest | cat || true

# # 남아있는 untagged digest 완전 제거
# UNTAGGED_DIGESTS=$(aws ecr list-images \
#   --region "$REGION" \
#   --profile "$profile" \
#   --repository-name "$PRIVATE_REPO_NAME" \
#   --filter tagStatus=UNTAGGED \
#   --query 'imageIds[].imageDigest' --output text || true)

# if [ -n "$UNTAGGED_DIGESTS" ] && [ "$UNTAGGED_DIGESTS" != "None" ]; then
#   for digest in $UNTAGGED_DIGESTS; do
#     aws ecr batch-delete-image \
#       --region "$REGION" \
#       --profile "$profile" \
#       --repository-name "$PRIVATE_REPO_NAME" \
#       --image-ids imageDigest=$digest | cat || true
#   done
# fi

echo "✅ 람다 배포 성공"

# 배포 결과 URL 출력
echo "=== 배포 완료 정보 ==="
API_URL=$(aws cloudformation describe-stacks \
    --stack-name "playground-$mode" \
    --query 'Stacks[0].Outputs[?OutputKey==`ApiEndpoint`].OutputValue' \
    --output text \
    --profile $profile)

if [ -n "$API_URL" ] && [ "$API_URL" != "None" ]; then
    echo "🚀 API 접속 URL: $API_URL"
else
    echo "❌ API URL을 찾을 수 없습니다."
    exit 1
fi

# echo "=== 7단계: Lambda Health Check ==="
# echo "Lambda가 정상적으로 동작하는지 확인합니다..."
# echo "15초마다 체크하여 총 5분간 모니터링합니다."

# # Health Check 설정
# HEALTH_CHECK_URL="$API_URL/actuator/health"
# CHECK_INTERVAL=15  # 15초
# MAX_CHECKS=20      # 5분 = 300초 / 15초 = 20번
# SUCCESS_COUNT=0
# FAIL_COUNT=0
# AUTH_HEADER="Authorization: Bearer e"
# echo "Health Check URL: $HEALTH_CHECK_URL"
# echo "체크 간격: ${CHECK_INTERVAL}초"
# echo "최대 체크 횟수: ${MAX_CHECKS}회"
# echo ""


# for i in $(seq 1 $MAX_CHECKS); do
#     echo "[$i/$MAX_CHECKS] Health Check 시도 중... ($(date '+%H:%M:%S'))"
    
#     # HTTP 상태 코드와 응답 시간 측정 (Bearer 토큰 포함)
#     RESPONSE=$(curl -s -H "$AUTH_HEADER" -w "\n%{http_code}\n%{time_total}" "$HEALTH_CHECK_URL" 2>/dev/null || echo -e "\n000\n0")
    
#     HTTP_CODE=$(echo "$RESPONSE" | tail -n 2 | head -n 1)
#     RESPONSE_TIME=$(echo "$RESPONSE" | tail -n 1)
#     BODY=$(echo "$RESPONSE" | sed '$d' | sed '$d')
    
#     if [ "$HTTP_CODE" = "200" ]; then
#         SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
#         echo "✅ Health Check 성공 (${RESPONSE_TIME}s) - 응답: $BODY"
        
#         # 한 번이라도 성공하면 조기 종료
#         echo "🎉 Lambda가 정상적으로 동작합니다! (1회 성공)"
#         break
#     else
#         FAIL_COUNT=$((FAIL_COUNT + 1))
#         echo "❌ Health Check 실패 (HTTP: $HTTP_CODE, 시간: ${RESPONSE_TIME}s)"
#     fi
    
#     # 마지막 체크가 아니면 대기
#     if [ $i -lt $MAX_CHECKS ]; then
#         echo "⏳ ${CHECK_INTERVAL}초 대기 중..."
#         sleep $CHECK_INTERVAL
#     fi
# done

# echo ""
# echo "=== Health Check 결과 요약 ==="
# echo "✅ 성공: $SUCCESS_COUNT회"
# echo "❌ 실패: $FAIL_COUNT회"
# echo "📊 성공률: $(( SUCCESS_COUNT * 100 / (SUCCESS_COUNT + FAIL_COUNT) ))%"

# if [ $SUCCESS_COUNT -gt 0 ]; then
#     echo "🎉 Lambda 배포 및 Health Check 완료!"
#     echo "🚀 API URL: $API_URL"
# else
#     echo "💥 5분 동안 Lambda Health Check 실패"
#     echo "CloudWatch Logs를 확인하여 문제를 해결해주세요."
#     exit 1
# fi

# exit
