#!/bin/bash
cd /home/ec2-user/deploy

# sudo sed -i 's/-Dspring.profiles.active=dev/-Dspring.profiles.active=prod/g' ${BUILD_PATH}/aws/start.sh
nohup java -jar -Dspring.profiles.active=dev /home/ec2-user/deploy/INTERNAL.jar >nohup.out 2>&1 </dev/null &