#!/usr/bin/env bash

BRANCH=$(git rev-parse --abbrev-ref HEAD)

echo "> branch $BRANCH"

if [ "$BRANCH" = "main" ]; then

    # main 브랜치에 대한 동작
    echo "Deploying on main branch"

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

    nohup java -jar -Dspring.config.location=/home/ubuntu/app/src/main/resources/application.yml,/home/ubuntu/app/src/main/resources/application-prod.yml,/home/ubuntu/app/src/main/resources/email-config.yaml,/home/ubuntu/app/src/main/resources/password-config.yaml -Duser.timezone=Asia/Seoul $JAR_NAME >> $REPOSITORY/nohup.out 2>&1 &

    echo "> jar 실행 완료"


elif [ "$BRANCH" = "develop" ]; then

    # develop 브랜치에 대한 동작
    echo "Deploying on develop branch"

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

    nohup java -jar -Dspring.config.location=/home/ubuntu/app/src/main/resources/application.yml,/home/ubuntu/app/src/main/resources/application-dev.yml,/home/ubuntu/app/src/main/resources/email-config.yaml,/home/ubuntu/app/src/main/resources/password-config.yaml -Duser.timezone=Asia/Seoul $JAR_NAME >> $REPOSITORY/nohup.out 2>&1 &

    echo "> jar 실행 완료"

fi