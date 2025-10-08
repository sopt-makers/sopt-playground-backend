# GraalVM 네이티브 컴파일 Lambda 배포 가이드

## 개요
이 프로젝트는 Docker를 사용한 GraalVM 네이티브 컴파일을 통해 AWS Lambda의 Cold Start 시간을 대폭 단축합니다.

## 주요 변경사항

### 1. build.gradle
- GraalVM 네이티브 컴파일 설정 추가
- 네이티브 바이너리 이름: `internal`
- 메인 클래스: `org.sopt.makers.internal.InternalApplication`

### 2. Dockerfile (멀티스테이지 빌드)
- 1단계: GraalVM으로 네이티브 컴파일
- 2단계: 최적화된 최종 Lambda 이미지 생성
- JAR 파일 대신 네이티브 바이너리 사용

### 4. Lambda 설정
- 메모리 사용량 감소 (1024MB → 512MB)
- 워밍업 간격 조정 (3분 → 5분)

### 5. 배포 스크립트
- Docker를 사용한 GraalVM 네이티브 컴파일
- 로컬 GraalVM 설치 불필요

## 개발 및 배포 방법

### 1. 로컬 개발 (JAR 파일 사용)
```bash
# 로컬에서 JAR 파일로 실행
./run-local.sh

# 또는 수동으로
./gradlew clean build -x test
java -jar build/libs/internal-0.0.1-SNAPSHOT.jar
```

### 2. Lambda 배포 (GraalVM 네이티브 컴파일)
```bash
cd lambda
./lambda_deploy.sh
```

### 3. 수동 배포
```bash
# 1. Docker로 네이티브 컴파일 및 이미지 빌드
docker build -t playground-dev:latest .

# 2. ECR 푸시 및 Lambda 배포
# (lambda_deploy.sh 스크립트 참조)
```

## 예상 성능 개선
- **Cold Start 시간**: 3-5초 → 0.5-1초
- **메모리 사용량**: 50% 감소
- **이미지 크기**: 30-50% 감소

## 주의사항
1. **Docker 메모리**: 네이티브 컴파일은 많은 메모리가 필요합니다 (최소 8GB 권장)
2. **빌드 시간**: 네이티브 컴파일은 시간이 오래 걸릴 수 있습니다 (10-20분)
3. **리플렉션**: 리플렉션을 사용하는 라이브러리는 추가 설정이 필요할 수 있습니다
4. **동적 기능**: 네이티브 컴파일 시 일부 동적 기능이 제한될 수 있습니다

## 시스템 요구사항
- Docker Desktop (최소 8GB 메모리 할당)
- AWS CLI 설정 완료
- ECR 접근 권한

## 문제 해결
네이티브 컴파일 실패 시:
1. **Docker 메모리 부족**: Docker Desktop에서 메모리 할당량 증가
2. **리플렉션 오류**: `reflect-config.json`에 필요한 클래스 추가
3. **리소스 오류**: `resource-config.json`에 필요한 리소스 추가
4. **빌드 로그 확인**: Docker 빌드 로그에서 누락된 클래스/리소스 확인

## 성능 모니터링
배포 후 CloudWatch에서 다음 메트릭을 확인하세요:
- Duration: 실행 시간
- Memory Utilization: 메모리 사용량
- Cold Start: 첫 실행 시간
