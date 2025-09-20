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
cd lambda

# 환경별 리전 및 리포지토리 설정
REGION="ap-northeast-2"
ACCOUNT_ID="379013966998"
if [ "$mode" = "prod" ]; then
  REPOSITORY_NAME="playground-prod"
else
  REPOSITORY_NAME="playground-dev"
fi

REPO_URI="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$REPOSITORY_NAME"

echo "ECR latest 태그의 digest 조회 중... ($REPOSITORY_NAME)"
IMAGE_DIGEST=$(aws ecr describe-images \
  --region "$REGION" \
  --profile "$profile" \
  --repository-name "$REPOSITORY_NAME" \
  --image-ids imageTag=latest \
  --query 'imageDetails[0].imageDigest' \
  --output text || true)

if [ -z "$IMAGE_DIGEST" ] || [ "$IMAGE_DIGEST" = "None" ]; then
  echo "❌ latest 태그의 이미지 digest를 찾을 수 없습니다. 이미지가 존재하는지 확인해주세요."
  exit 1
fi

IMAGE_URI_WITH_DIGEST="$REPO_URI@$IMAGE_DIGEST"
echo "배포 대상 이미지: $IMAGE_URI_WITH_DIGEST"

sam deploy \
  --config-env "$mode" \
  --profile "$profile" \
  --parameter-overrides ImageUri="$IMAGE_URI_WITH_DIGEST"


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
    --query 'Stacks[0].Outputs[?OutputKey==`FunctionUrl`].OutputValue' \
    --output text \
    --profile $profile)

if [ -n "$API_URL" ] && [ "$API_URL" != "None" ]; then
    echo "🚀 API 접속 URL: $API_URL"
else
    echo "❌ API URL을 찾을 수 없습니다."
fi

exit
