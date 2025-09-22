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

echo "=== 1단계: Gradle 빌드 ==="
./gradlew clean build -x test
if [ $? -ne 0 ]; then
    echo "❌ Gradle 빌드 실패"
    exit 1
fi
echo "✅ Gradle 빌드 완료"

echo "=== 2단계: ECR 리포지토리 확인/생성 ==="
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

echo "=== 3단계: ECR 로그인 ==="
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

# 타임스탬프 기반 고유 태그 생성
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
IMAGE_TAG="build-$TIMESTAMP"

echo "=== 4단계: Docker 이미지 빌드 ==="
docker build -t "$REPOSITORY_NAME:$IMAGE_TAG" .
if [ $? -ne 0 ]; then
    echo "❌ Docker 이미지 빌드 실패"
    exit 1
fi
echo "✅ Docker 이미지 빌드 완료"

echo "=== 5단계: ECR에 이미지 푸시 ==="
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

echo "=== 5-1단계: 로컬 이미지 정리 ==="
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
fi

exit
