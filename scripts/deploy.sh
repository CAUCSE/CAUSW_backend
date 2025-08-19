#!/usr/bin/env bash

git init

BRANCH=$(git rev-parse --abbrev-ref HEAD)
echo "> branch $BRANCH"

function deploy_module() {
  local MODULE_NAME=$1
  local REPOSITORY=$2
  local PORT=$3

  echo "> [$MODULE_NAME] 현재 구동 중인 애플리케이션 pid 확인 (포트 $PORT)"
  CURRENT_PID=$(lsof -ti tcp:$PORT)
  echo "[$MODULE_NAME] 현재 구동 중인 애플리케이션 pid: $CURRENT_PID"

  if [ -z "$CURRENT_PID" ]; then
    echo "[$MODULE_NAME] 현재 구동 중인 애플리케이션이 없으므로 종료하지 않음"
  else
    echo "[$MODULE_NAME] kill -9 $CURRENT_PID"
    sudo kill -9 "$CURRENT_PID"
    sleep 5
  fi

  echo "[$MODULE_NAME] 새 애플리케이션 배포"
  JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)
  echo "[$MODULE_NAME] JAR NAME: $JAR_NAME"

  chmod +x $JAR_NAME

  echo "[$MODULE_NAME] 애플리케이션 실행"
  nohup java -jar -Duser.timezone=Asia/Seoul $JAR_NAME >> $REPOSITORY/nohup_${MODULE_NAME}.out
 2>&1 &
  echo "[$MODULE_NAME] 실행 완료"
}

if [ "$BRANCH" = "main" ]; then
  echo "Deploying on main branch"

  deploy_module "app-main" "/home/ubuntu/app/app-main" 8080
  deploy_module "app-chat" "/home/ubuntu/app/app-chat" 8081

elif [ "$BRANCH" = "develop" ]; then
  echo "Deploying on develop branch"

  deploy_module "app-main" "/home/ubuntu/app/app-main" 8080
  deploy_module "app-chat" "/home/ubuntu/app/app-chat" 8081

else
  echo "Deploy script does not support branch: $BRANCH"
fi