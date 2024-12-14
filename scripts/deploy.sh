#!/bin/bash
cd /home/ec2-user/app


ALL_PORTS=("8080","8081")
HEALTH_CHECK_URL=/actuator/health

# Check the running container name
AVAILABLE_PORT=()
DOCKER_PS_OUTPUT=$(docker ps | grep playground)
RUNNING_CONTAINER_NAME=$(echo "$DOCKER_PS_OUTPUT" | awk '{print $NF}')
RUNNING_SERVER_PORT=""

check_running_container() {
  if [[ ${RUNNING_CONTAINER_NAME} =~ "playground-blue" ]]; then
      echo "실행 중인 포트: playground-blue (:8080)"
      RUNNING_SERVER_PORT=8080
  elif [[ ${RUNNING_CONTAINER_NAME} =~ "playground-green" ]]; then
      echo "실행 중인 포트: playground-green (:8081)"
      RUNNING_SERVER_PORT=8081
  else
    echo "실행 중인 포트: None"
  fi
}

# Get the available ports by excluding the currently running port
get_available_ports() {
  for item in "${ALL_PORTS[@]}"; do
    if [ "$item" != "${RUNNING_SERVER_PORT}" ]; then
      AVAILABLE_PORT+=("$item")
    fi
  done
}

deploy_container() {
    local CONTAINER_NAME=$1
    local PORT=$2

    echo "Switching to ${CONTAINER_NAME} at Port ${PORT} ..."
    echo "docker-compose pull & up ..."

    docker-compose pull ${CONTAINER_NAME}
    docker-compose up -d ${CONTAINER_NAME}
}

reload_nginx() {
    local PORT=$1

    echo "Nginx Reload (Port switching applied) ..."

    echo "set \$service_url http://127.0.0.1:${PORT};" | sudo tee /etc/nginx/conf.d/service-url.inc
    sudo nginx -s reload
    echo "Current running Port after switching: $(sudo cat /etc/nginx/conf.d/service-url.inc)"
}

stop_container() {
    local CONTAINER_NAME=$1

    echo "[$NOW_TIME] Stopping ${CONTAINER_NAME} Container"
    docker-compose stop ${CONTAINER_NAME}
}

## 파일 분리 예정
health_check() {
  local PORT=$1
  local RETRIES=15

  echo "Start health check after 15 seconds"
  sleep 15

  for retry_count in $(seq 1 $RETRIES); do
    echo "Health Check on Port ${PORT} ..."
    sleep 3

    RESPONSE=$(curl -s http://localhost:${PORT}${HEALTH_CHECK_URL})
    UP_COUNT=$(echo $RESPONSE | grep 'UP' | wc -l)
    echo "Health Check Response: ${RESPONSE}"

    if [ $UP_COUNT -ge 1 ]; then
      echo "Success Health check!"
      break;
    else
      echo "Health check response is empty or not status 'UP'"
    fi

    if [ $retry_count -eq $RETRIES ]; then
      echo "Health check failed after $RETRIES attempts."
      return 1
    fi
  done
}

### --

check_running_container
get_available_ports

# If no available ports, exit the script
if [ ${#AVAILABLE_PORT[@]} -eq 0 ]; then
    echo "실행 가능한 포트가 없습니다."
    exit 1
fi


# Green Up
if [ "${RUNNING_SERVER_PORT}" == "8080" ]; then
  CURRENT_SERVER_PORT=8081

  # function call (container name, current port)
  deploy_container "playground-green" ${CURRENT_SERVER_PORT}

  if ! health_check "playground-green"; then
    echo "Health Check failed ..."
    stop_container "playground-green"
    exit 1
  fi
  reload_nginx ${CURRENT_SERVER_PORT}
  stop_container "playground-blue"

# Blue up
else
  CURRENT_SERVER_PORT=8080

  deploy_container "playground-blue" ${CURRENT_SERVER_PORT}

  if ! health_check "playground-blue"; then
    echo "Health Check failed ..."
    stop_container "playground-blue"
    exit 1
  fi
  reload_nginx ${CURRENT_SERVER_PORT}
  stop_container "playground-green"
fi



# Final health check through Nginx to confirm the server change
echo "Final health check applied nginx port switching ..."
if ! health_check ${CURRENT_SERVER_PORT}; then
  echo "Server change failed ..."
  exit 1
fi