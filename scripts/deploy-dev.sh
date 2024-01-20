#!/usr/bin/env bash

REPOSITORY=/home/ubuntu/app/build/libs

echo "> 현재 구동 중인 애플리케이션 pid 확인"

CURRENT_PID=$(lsof -ti tcp:8080)

echo "현재 구동 중인 애플리케이션 pid: $CURRENT_PID"

if [ -z "${CURRENT_PID}" ]
then
  echo "현재 구동 중인 애플리케이션이 없으므로 종료 X"
else
  echo "> kill -9 $CURRENT_PID"
  sudo kill -9 "$CURRENT_PID"
  sleep 5
fi

echo "> 새 애플리케이션 배포"

JAR_NAME=$(ls -tr $REPOSITORY/*SNAPSHOT.jar | tail -n 1)

echo "> JAR NAME: $JAR_NAME"

echo "> $JAR_NAME 에 실행권한 추가"

chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"

nohup java -jar -Dspring.config.location=/home/ubuntu/app/src/main/resources/application.yml,/home/ubuntu/app/src/main/resources/email-config.yaml,/home/ubuntu/app/src/main/resources/password-config.yaml,/home/ubuntu/app/src/main/resources/logback.xml -Duser.timezone=Asia/Seoul $JAR_NAME >> $REPOSITORY/nohup.out 2>&1 &

echo "> jar 실행 완료"