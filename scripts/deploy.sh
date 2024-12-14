cd /home/ec2-user/app

HEALTH_CHECK_URL=/actuator/health

sudo docker-compose pull ${ECR_REPO}
docker images -a
docker-compose up -d playground