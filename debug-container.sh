DISCORD_TOKEN="${DISCORD_TOKEN:-}"
CURUPIRA_RESET=false
LOG_CHANNEL_ID="${LOG_CHANNEL_ID:-}"

if [[ -z "${DISCORD_TOKEN}" ]] || [[ -z "${LOG_CHANNEL_ID}" ]] ; then
  echo 'Missing any of the required environment variable: DISCORD_TOKEN, LOG_CHANNEL_ID'
  exit 1
fi

echo "Cleaning old images..."
podman image rm localhost/capivara:latest

CONTAINER_NAME="capivara_bot"
OLD_CONTAINER_ID=$(podman ps --all --quiet --filter "name=$CONTAINER_NAME")

if [[ -z "${OLD_CONTAINER_ID}" ]]; then
  echo "Container not found"
else
    echo "Found container $OLD_CONTAINER_ID"
    echo "Stopping container..."
    WAIT=10 # seconds
    podman stop $OLD_CONTAINER_ID -t $WAIT || { echo 'Failed to stop container'; exit 1; }
    echo "Removing container..."
    podman rm --force $OLD_CONTAINER_ID || { echo 'Failed to remove container'; exit 1; }
fi

echo "Building image..."
podman build --build-arg APP_VERSION="debug" -t capivara:latest -f Dockerfile .

echo "Starting container..."
podman run \
    -e DISCORD_TOKEN=$DISCORD_TOKEN \
    -e LOG_CHANNEL_ID=$LOG_CHANNEL_ID \
    -e CURUPIRA_RESET="false" \
    -e DATABASE_DRIVER="org.h2.Driver" \
    -e DATABASE_DIALECT="org.hibernate.dialect.H2Dialect" \
    -e DATABASE_URL="jdbc:h2:file:./banco_h2;DB_CLOSE_DELAY=-1" \
    -e DATABASE_USERNAME="sa" \
    -e DATABASE_PASSWORD="sa" \
    -e JAVA_ARGS="-Xmx300M" \
    --cpus 1 \
    --memory 400M \
    --name capivara_bot \
    --restart always \
    --network slirp4netns:allow_host_loopback=true \
    capivara:latest