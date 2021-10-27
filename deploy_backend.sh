# Build project
echo "##### Build CAUSW backend application #####"
echo "Active profile : " ${SPRING_PROFILES_ACTIVE}
./gradlew build --exclude-task test -DSPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# Build docker file
echo "\n\n##### Build docker file #####"
docker build -t gcr.io/causw-dev/backend-app .

# Push docker file
echo "\n\n##### Push docker file #####"
docker push gcr.io/causw-dev/backend-app

# Restart cluster
echo "\n\n##### Restart cluster with new docker file #####"
kubectl rollout restart deployment backend-app
