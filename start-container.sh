if [ -z "${1}" ]; then
  echo "Missing container repository and tag"
  exit 1;
else
  echo "Container repository and tag: $1"
fi
CONTAINER_REPOSITORY=${1}

if [[ -z "${DISCORD_TOKEN}" ]] || [[ -z "${LOG_CHANNEL_ID}" ]] ; then
  echo 'Missing required environment variable: DISCORD_TOKEN, LOG_CHANNEL_ID'
  exit 1
fi

CONTAINER_NAME="capivara_bot"
OLD_CONTAINER_ID=$(podman ps --all --quiet --filter "name=$CONTAINER_NAME")

if [[ -z "${OLD_CONTAINER_ID}" ]]; then
  echo "Container not found"
else
    echo "Found container $OLD_CONTAINER_ID"
    echo "Stopping..."
    WAIT=10 # seconds
    podman stop $OLD_CONTAINER_ID -t $WAIT || { echo 'Failed to stop container'; exit 1; }
    echo "Removing..."
    podman rm --force $OLD_CONTAINER_ID || { echo 'Failed to remove container'; exit 1; }
fi

echo "Starting container..."
podman run -d \
    -e DISCORD_TOKEN=$DISCORD_TOKEN \
    -e LOG_CHANNEL_ID=$LOG_CHANNEL_ID \
    -e CURUPIRA_RESET=${CURUPIRA_RESET:-false} \
    -e DATABASE_DRIVER=${DATABASE_DRIVER:-org.h2.Driver} \
    -e DATABASE_DIALECT=${DATABASE_DIALECT:-org.hibernate.dialect.H2Dialect} \
    -e DATABASE_URL=${DATABASE_URL:-jdbc:h2:file:./banco_h2;DB_CLOSE_DELAY=-1} \
    -e DATABASE_USERNAME=${DATABASE_USERNAME:-sa} \
    -e DATABASE_PASSWORD=${DATABASE_PASSWORD:-sa} \
    -e JAVA_ARGS=${JAVA_ARGS:--Xmx200M} \
    -p 8080:8080 \
    --cpus 0.5 \
    --memory 400M \
    --name $CONTAINER_NAME \
    --restart always \
    --network metrics \
    --network capivara \
    $CONTAINER_REPOSITORY || { echo 'Failed to start container failed'; exit 1; }


SLEEP=10
CHECKS=0
MAX_CHECKS=6
echo "Starting 'healthcheck'"
while true; do
    NEW_CONTAINER_ID=$(podman ps --quiet --filter "name=$CONTAINER_NAME")
    if [[ -z "${NEW_CONTAINER_ID}" ]]; then
        echo "Container died? exiting"
        exit 1
    fi

    if [[ CHECKS -eq MAX_CHECKS ]]; then
        break
    fi
    ((CHECKS += 1))
    sleep $SLEEP
done;

echo "Container should be alive"

echo "Cleaning images..."
podman image prune -a -f || { echo 'Failed to start clean images'; exit 1; }
