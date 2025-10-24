# Lambda 배포 가이드

## 개요

이 문서는 SOPT Playground Backend를 AWS Lambda로 배포하는 프로세스를 설명합니다.
Spring Boot 애플리케이션을 GraalVM Native Image로 컴파일하여 AWS Lambda Container로 배포합니다.

---

## Lambda 폴더 구조

```
lambda/
├── template-dev.yaml           # AWS SAM 템플릿 (CloudFormation)
├── dev.Dockerfile              # GraalVM 네이티브 이미지 빌드용 Dockerfile
├── local_dev_lambda_deploy.sh  # 로컬 배포 자동화 스크립트
└── samconfig.toml              # SAM CLI 설정 파일
```

### 파일 설명

#### 1. `template-dev.yaml`
- **용도**: AWS SAM (Serverless Application Model) 템플릿
- **설명**: Lambda 함수, HTTP API Gateway, 스케줄러 등 인프라를 정의하는 CloudFormation 템플릿
- **주요 구성**:
  - Lambda 함수 설정 (메모리: 4096MB, 타임아웃: 300초)
  - ARM64 아키텍처 사용
  - HTTP API Gateway 설정 (CORS 포함)
  - Warmer 이벤트 (5분마다 실행되어 콜드 스타트 방지)
  - Auto Publish Alias: live

#### 2. `dev.Dockerfile`
- **용도**: GraalVM 네이티브 컴파일용 멀티스테이지 Docker 빌드 파일
- **설명**: Spring Boot 애플리케이션을 네이티브 바이너리로 컴파일하여 최적화된 Lambda 이미지 생성
- **주요 특징**:
  - **Stage 1 (Builder)**: GraalVM 17.0.9으로 네이티브 컴파일
    - Gradle dependency 캐싱 최적화
    - 메모리: 3GB 할당
  - **Stage 2 (Runtime)**: Amazon Linux 2023 기반 최종 이미지
    - AWS Lambda Web Adapter (0.9.1) 포함
    - Spring Profile: `lambda-dev`
    - Health check 엔드포인트: `/actuator/health`

#### 3. `local_dev_lambda_deploy.sh`
- **용도**: 로컬 환경에서 Lambda를 배포하는 자동화 쉘 스크립트
- **설명**: ECR 로그인부터 Docker 빌드, 이미지 푸시, SAM 배포까지 전 과정 자동화
- **주요 기능**:
  - AWS Profile 선택 (기본값: default)
  - 타임스탬프 기반 이미지 태그 생성 (`build-YYYYMMDD-HHMMSS`)
  - ECR Public/Private 로그인
  - Docker 네이티브 빌드 (ARM64)
  - ECR 리포지토리 자동 생성
  - 빌드 후 로컬 이미지 자동 정리

#### 4. `samconfig.toml`
- **용도**: AWS SAM CLI 설정 파일
- **설명**: `sam deploy` 명령어 실행 시 사용되는 환경별 설정
- **환경 구성**:
  - **dev**: 개발 환경 (스택명: `playground-dev`)
  - **prod**: 운영 환경 (스택명: `playground-prod`)
- **공통 설정**:
  - Region: `ap-northeast-2` (서울)
  - Capabilities: `CAPABILITY_IAM`
  - 자동 이미지 레지스트리/S3 버킷 생성

---

## 로컬에서 Lambda 배포

### 전제 조건

1. **필수 도구**:
   - AWS CLI (v2 이상)
   - Docker Desktop
   - AWS SAM CLI
   - 적절한 AWS 자격 증명 (IAM 권한)

2. **AWS 권한**:
   - ECR (이미지 푸시)
   - Lambda (함수 배포)
   - CloudFormation (스택 생성/업데이트)
   - IAM (역할 생성)

### 배포 프로세스

스크립트 실행:
```bash
./lambda/local_dev_lambda_deploy.sh
```

#### 단계별 설명

**1단계: AWS Profile 입력**
```
aws profile 을 입력해주세요 (기본 configuration 활용 시, default 입력) :
```
- 사용할 AWS 자격 증명 프로파일 선택
- 기본값: `default`

**2단계: ECR 로그인**
```bash
# Public ECR (Lambda Adapter 이미지 접근)
aws ecr-public get-login-password --region us-east-1 | docker login ...

# Private ECR (이미지 푸시)
aws ecr get-login-password --region ap-northeast-2 | docker login ...
```
- Public ECR: AWS Lambda Web Adapter 이미지 다운로드
- Private ECR: 빌드한 이미지 업로드

**3단계: Docker 네이티브 빌드**
```bash
docker build -f lambda/dev.Dockerfile --platform=linux/arm64 -t playground-dev:build-TIMESTAMP .
```
- GraalVM으로 Spring Boot를 네이티브 바이너리로 컴파일
- ARM64 아키텍처 (Lambda 최적화)
- 빌드 시간: 약 10-15분 소요
- 최종 이미지 크기: 약 100-150MB

**4단계: ECR 리포지토리 확인/생성**
```bash
aws ecr describe-repositories --repository-names playground-dev
# 없으면 자동 생성
aws ecr create-repository --repository-name playground-dev
```

**5단계: ECR 이미지 푸시**
```bash
# 타임스탬프 태그
docker tag playground-dev:build-TIMESTAMP ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/playground-dev:build-TIMESTAMP
docker push ...

# latest 태그
docker tag playground-dev:build-TIMESTAMP ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/playground-dev:latest
docker push ...
```
- 고유한 타임스탬프 태그로 버전 추적
- `latest` 태그로 최신 이미지 관리

**6단계: 로컬 이미지 정리**
```bash
docker rmi playground-dev:build-TIMESTAMP
docker rmi ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/playground-dev:*
```
- 디스크 공간 확보

**7단계: SAM 배포**
```bash
cd lambda
sam deploy \
  --config-env dev \
  --profile PROFILE \
  --parameter-overrides ImageUri=ECR_IMAGE_URI
```
- CloudFormation 스택 생성/업데이트
- Lambda 함수 생성 및 이미지 연결
- API Gateway 엔드포인트 생성
- 배포 완료 후 API URL 출력

---

## GitHub Actions로 Lambda 배포

### 트리거

워크플로우 파일: `.github/workflows/deploy-lambda-dev.yml`

**실행 조건**:
1. 수동 실행 (`workflow_dispatch`)

### 배포 프로세스

#### 1. 환경 설정 (Steps 1-3)

**코드 체크아웃**:
```yaml
- name: Checkout code
  uses: actions/checkout@v3
```

**JDK 17 설치**:
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v3
  with:
    distribution: 'corretto'
    java-version: '17'
```
- Amazon Corretto 17 사용 (AWS 권장 배포판)

**Gradle 캐시 설정**:
```yaml
- name: Setup Gradle cache
  uses: actions/cache@v3
```
- 빌드 속도 향상 (dependency 다운로드 재사용)

#### 2. AWS 인증 및 설정 파일 가져오기 (Steps 4-6)

**AWS 자격 증명 설정**:
```yaml
- name: Configure AWS credentials
  uses: aws-actions/configure-aws-credentials@v2
  with:
    aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_TEMP }}
    aws-secret-access-key: ${{ secrets.AWS_SECRET_KEY_TEMP }}
```

**S3에서 민감한 설정 파일 다운로드**:
```yaml
# Spring Profile 설정
aws s3 cp s3://sopt-makers-internal/dev/deploy/application-lambda-dev.yml \
  src/main/resources/

# Apple Sign-in 키 파일
aws s3 cp s3://sopt-makers-internal/dev/deploy/APPLE_KEY \
  src/main/resources/static/
```
- 데이터베이스 연결 정보, API 키 등 민감 정보 포함

#### 3. Docker 멀티플랫폼 빌드 설정 (Steps 7-8)

**QEMU 설정** (ARM64 에뮬레이션):
```yaml
- name: Set up QEMU for multi-platform builds
  uses: docker/setup-qemu-action@v2
  with:
    platforms: linux/arm64
```
- x86_64 러너에서 ARM64 이미지 빌드 가능

**Docker Buildx 설정**:
```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v2
```
- 멀티플랫폼 빌드 지원
- 빌드 캐시 최적화

#### 4. ECR 로그인 (Steps 9-10)

**Public ECR**:
```bash
aws ecr-public get-login-password --region us-east-1 | docker login ...
```

**Private ECR**:
```bash
aws ecr get-login-password --region ap-northeast-2 | docker login ...
```

#### 5. Docker 이미지 빌드 (Steps 11-12)

**타임스탬프 태그 생성**:
```bash
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
IMAGE_TAG=build-$TIMESTAMP
```
- 예: `build-20241024-143022`

**GraalVM 네이티브 컴파일**:
```yaml
- name: Build Docker image with GraalVM native compilation
  run: |
    docker buildx build \
      --platform=linux/arm64 \
      --cache-from type=gha \
      --cache-to type=gha,mode=max \
      -f lambda/dev.Dockerfile \
      -t playground-dev:build-TIMESTAMP \
      --load \
      .
```
- GitHub Actions 캐시 활용 (`type=gha`)
- 빌드 시간 단축 (캐시 hit 시 5-7분)

#### 6. ECR 이미지 푸시 (Step 13)

```bash
REPO_URI=ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/playground-dev

# 타임스탬프 태그 푸시
docker tag playground-dev:build-TIMESTAMP $REPO_URI:build-TIMESTAMP
docker push $REPO_URI:build-TIMESTAMP

# latest 태그 푸시
docker tag playground-dev:build-TIMESTAMP $REPO_URI:latest
docker push $REPO_URI:latest
```

#### 7. SAM 배포 (Steps 14-16)

**Python 및 SAM CLI 설치**:
```yaml
- name: Set up Python for SAM CLI
  uses: actions/setup-python@v4
  with:
    python-version: '3.11'

- name: Install AWS SAM CLI
  run: pip install aws-sam-cli
```

**Lambda 배포**:
```bash
cd lambda
sam deploy \
  --config-env dev \
  --no-confirm-changeset \
  --no-fail-on-empty-changeset \
  --parameter-overrides ImageUri=$IMAGE_URI
```
- `samconfig.toml`의 `dev` 환경 설정 사용
- 변경사항 없으면 실패하지 않음
- 이미지 URI를 파라미터로 전달

---

## 배포 비교: 로컬 vs GitHub Actions

| 항목 | 로컬 배포                     | GitHub Actions                 |
|------|---------------------------|--------------------------------|
| **실행 환경** | 개발자 로컬 머신                 | GitHub 호스티드 러너 (Ubuntu)        |
| **AWS 인증** | AWS Profile (수동 입력)       | GitHub Secrets                 |
| **빌드 시간** | 약 20분 (8GB Docker Memory) | 150분... (Git Action Memory 한도) |
| **캐시** | Docker 레이어 캐시             | GitHub Actions 캐시              |
| **설정 파일** | 로컬에 미리 배치 필요              | S3에서 자동 다운로드                   |
| **트리거** | 수동 스크립트 실행                | Git Push 또는 수동 트리거             |
| **장점** | 빠른 테스트, 디버깅 용이            | 자동화, 원격 실행                     |
| **단점** | 환경 의존적, 로컬 메모리 소모         | 초기 캐시 미스 시, 매우 느림              |

---

## 주요 기술 스택

### 1. GraalVM Native Image
- **목적**: JVM 없이 네이티브 바이너리로 실행
- **장점**:
  - 빠른 시작 시간 (콜드 스타트 최소화)
  - 낮은 메모리 사용량
  - 작은 이미지 크기
- **단점**:
  - 긴 컴파일 시간
  - 리플렉션 설정 필요

### 2. AWS Lambda Web Adapter
- **용도**: 기존 웹 애플리케이션을 Lambda에서 실행
- **기능**:
  - HTTP 요청을 Lambda 이벤트로 변환
  - Health check 지원
  - 8080 포트로 Spring Boot 실행

### 3. AWS SAM (Serverless Application Model)
- **용도**: 서버리스 배포 프레임워크
- **장점**:
  - CloudFormation 확장
  - 간단한 구문
  - 로컬 테스트 지원 (`sam local`)

### 4. ARM64 아키텍처
- **선택 이유**:
  - 20% 가격 절감 (x86_64 대비)
  - 우수한 성능/가격 비율
  - Graviton2 프로세서 활용

---

## 참고

- [AWS Lambda 공식 문서](https://docs.aws.amazon.com/lambda/)
- [AWS SAM 개발자 가이드](https://docs.aws.amazon.com/serverless-application-model/)
- [GraalVM Native Image](https://www.graalvm.org/native-image/)
- [AWS Lambda Web Adapter](https://github.com/awslabs/aws-lambda-web-adapter)

