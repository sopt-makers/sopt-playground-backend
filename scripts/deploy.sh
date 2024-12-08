REPOSITORY=/home/ec2-user/app
cd $REPOSITORY

HEALTH_CHECK_URL=/actuator/health

sudo docker pull ${{ secrets.ECR_REPO }}:arm64
docker images -a
docker-compose up -d playground