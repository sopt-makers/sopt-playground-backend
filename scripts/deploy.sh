#!/bin/bash
cd /home/ec2-user/app

# Source the external scripts to load their functions
source ./health_check.sh
source ./deploy_container.sh
source ./nginx_reload.sh
source ./stop_container.sh

ALL_PORTS=("8080","8081")
HEALTH_CHECK_URL=/actuator/health

# Check the running container name
AVAILABLE_PORT=()
DOCKER_PS_OUTPUT=$(docker ps | grep playground)
RUNNING_CONTAINER_NAME=$(echo "$DOCKER_PS_OUTPUT" | awk '{print $NF}')
RUNNING_SERVER_PORT=""

check_running_container() {
  if [[ ${RUNNING_CONTAINER_NAME} =~ "playground-blue" ]]; then
      echo "Running Port: playground-blue (:8080)"
      RUNNING_SERVER_PORT=8080
  elif [[ ${RUNNING_CONTAINER_NAME} =~ "playground-green" ]]; then
      echo "Running Port: playground-green (:8081)"
      RUNNING_SERVER_PORT=8081
  else
    echo "Running Port: None"
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
    echo "❌Health Check failed ..."
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
    echo "❌Health Check failed ..."
    stop_container "playground-blue"
    exit 1
  fi
  reload_nginx ${CURRENT_SERVER_PORT}
  stop_container "playground-green"
fi



# Final health check through Nginx to confirm the server change
echo "Final health check applied nginx port switching ..."
if ! health_check ${CURRENT_SERVER_PORT}; then
  echo "❌Server change failed ..."
  exit 1
fi

echo "✅Server change successful 👍"