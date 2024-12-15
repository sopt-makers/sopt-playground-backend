#!/bin/bash

HEALTH_CHECK_URL=/actuator/health

# Health check to verify if the server is up
health_check() {
  local PORT=$1

  echo "▶️ Start health check after 15 seconds"
  sleep 15

  for retry_count in $(seq 1 $RETRIES); do
    echo "Health Check on Port ${PORT} ..."
    sleep 3

    RESPONSE=$(curl -s http://localhost:${PORT}${HEALTH_CHECK_URL})
    UP_COUNT=$(echo $RESPONSE | grep 'UP' | wc -l)
    echo "Health Check Response: ${RESPONSE}"

    if [ $UP_COUNT -ge 1 ]; then
      echo "✅ Success Health check!"
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
