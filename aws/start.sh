REPOSITORY=/home/ec2-user/app
cd $REPOSITORY

JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep '.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

echo "> $JAR_PATH 배포" #3
nohup java -jar -Dspring.profiles.active=dev $REPOSITORY/build/libs/internal-0.0.1-SNAPSHOT.jar >nohup.out 2>&1 </dev/null &