#!/bin/bash
cd /home/ec2-user/app

DOCKER_PS_OUTPUT=$(docker ps | grep playground)
RUNNING_CONTAINER_NAME=$(echo "$DOCKER_PS_OUTPUT" | awk '{print $NF}')

HEALTH_CHECK_URL=/actuator/health

if [ ${RUNNING_CONTAINER_NAME} == "playground-blue" ]; then
    echo "Server Port on Running: playground-blue (:8080)"
    RUNNING_SERVER_PORT=8080
elif [ ${RUNNING_CONTAINER_NAME} == "playground-green" ]; then
    echo "Server Port on Running: playground-green (:8081)"
    RUNNING_SERVER_PORT=8081
else
  echo "Server Port on Running: None"
fi

# Green Up
if [ $RUNNING_SERVER_PORT == 8080 ]; then
  echo "Switching 8080 -> 8081 ..."
  CURRENT_SERVER_PORT=8081

  echo "docker-compose pull."
  sudo docker-compose pull playground-green
  docker images -a
  docker-compose up -d playground-green

  echo "Start health check after 15 seconds"
  sleep 15

  for retry_count in {1..15}; do
    echo "Green Health Check ..."

    RESPONSE=$(curl -s http://localhost:${CURRENT_SERVER_PORT}${HEALTH_CHECK_URL})
    UP_COUNT=$(echo $RESPONSE | grep 'UP' | wc -l)
    echo "Health Check Response: ${RESPONSE}"

    if [ $UP_COUNT -ge 1 ]; then
      echo "Success Health check!"
      break;
    else
      echo "Health check의 응답을 알 수 없거나 status가 UP이 아닙니다."
    fi

    if [ $retry_count -eq 15 ]; then
      echo "[$NOW_TIME] Health check 실패.."
      echo "[$NOW_TIME] Nginx에 연결하지 않고 배포를 종료합니다."
      docker-compose stop playground-green
      exit 1
    fi
  done;
  sleep 3


  echo "[$NOW_TIME] Nginx Reload (Port 스위칭 적용)"
  echo "set \$service_url http://127.0.0.1:${CURRENT_SERVER_PORT};" |sudo tee /etc/nginx/conf.d/service-url.inc
  sudo nginx -s reload
  echo "[$NOW_TIME] 스위칭 후 실행 중인 Port: $(sudo cat /etc/nginx/conf.d/service-url.inc)"
  echo "[$NOW_TIME] Blue Container Stop"
  docker-compose stop playground-blue


# Blue up
else

  echo "Switching 8081 -> 8080 ..."
    CURRENT_SERVER_PORT=8080

    echo "docker-compose pull."
    sudo docker-compose pull playground-blue
    docker images -a
    docker-compose up -d playground-blue

    echo "Start health check after 15 seconds"
    sleep 15

    for retry_count in {1..15}; do
      echo "Blue Health Check ..."

      RESPONSE=$(curl -s http://localhost:${CURRENT_SERVER_PORT}${HEALTH_CHECK_URL})
      UP_COUNT=$(echo $RESPONSE | grep 'UP' | wc -l)
      echo "Health Check Response: ${RESPONSE}"

      if [ $UP_COUNT -ge 1 ]; then
        echo "Success Health check!"
        break;
      else
        echo "Health check의 응답을 알 수 없거나 status가 UP이 아닙니다."
      fi

      if [ $retry_count -eq 15 ]; then
        echo "[$NOW_TIME] Health check 실패.."
        echo "[$NOW_TIME] Nginx에 연결하지 않고 배포를 종료합니다."
        docker-compose stop playground-blue
        exit 1
      fi
    done;
    sleep 3


    echo "[$NOW_TIME] Nginx Reload (Port 스위칭 적용)"
    echo "set \$service_url http://127.0.0.1:${CURRENT_SERVER_PORT};" |sudo tee /etc/nginx/conf.d/service-url.inc
    sudo nginx -s reload
    echo "[$NOW_TIME] 스위칭 후 실행 중인 Port: $(sudo cat /etc/nginx/conf.d/service-url.inc)"
    echo "[$NOW_TIME] Green Container Stop"
    docker-compose stop playground-green
fi



# Nginx를 통해서 서버에 접근 가능한지 확인
RESPONSE=$(curl -s http://localhost:${CURRENT_SERVER_PORT}${WEB_HEALTH_CHECK_URL})
UP_COUNT=$(echo $RESPONSE | grep 'UP' | wc -l)
echo "[$NOW_TIME] Health check 응답: ${RESPONSE}"

if [ $UP_COUNT -ge 1 ]
then
    echo "[$NOW_TIME] 서버 변경 성공"
else
    echo "[$NOW_TIME] 서버 변경 실패"
    echo "[$NOW_TIME] 서버 응답 결과: ${RESPONSE}"
    exit 1
fi
