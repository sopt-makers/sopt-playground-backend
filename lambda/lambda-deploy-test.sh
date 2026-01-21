#!/bin/bash

# 간단한 Lambda JAR 배포 스크립트

set -e  # 에러 발생시 중단

# 설정
ENV=${1:-dev}
S3_BUCKET="sopt-makers-internal"
STACK_NAME="playground-${ENV}"
AWS_REGION="ap-northeast-2"

# 색상 정의
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo "🚀 Lambda JAR 배포 시작 (환경: $ENV)"

# 1. JAR 빌드
echo "📦 JAR 빌드 중..."
./gradlew clean lambdaJar -x test

# 2. S3 업로드
JAR_FILE=$(ls build/distributions/*-lambda.zip | head -1)
# 타임스탬프 추가 (YYYYMMDD-HHMMSS 형식)
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
S3_KEY="lambda/playground-${ENV}-${TIMESTAMP}-lambda.zip"

echo "☁️ S3 업로드 중..."
echo "  파일: $JAR_FILE"
echo "  S3 경로: s3://${S3_BUCKET}/${S3_KEY}"
aws s3 cp "$JAR_FILE" "s3://${S3_BUCKET}/${S3_KEY}"

# 4. SAM으로 배포
echo "🔄 SAM 배포 중..."
cd lambda

# SAM 배포 실행
sam deploy \
  --config-env ${ENV} \
  --stack-name ${STACK_NAME} \
  --no-fail-on-empty-changeset \
  --parameter-overrides \
    S3Bucket=${S3_BUCKET} \
    S3Key=${S3_KEY}

cd ..

echo -e "${GREEN}✅ 배포 완료!${NC}"

# API 엔드포인트 출력
API_ENDPOINT=$(aws cloudformation describe-stacks \
  --stack-name ${STACK_NAME} \
  --query "Stacks[0].Outputs[?OutputKey=='ApiEndpoint'].OutputValue" \
  --output text \
  --region ${AWS_REGION})

echo -e "${GREEN}🌐 API: ${API_ENDPOINT}${NC}"
