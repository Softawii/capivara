FROM docker.io/eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app
ADD . /tmp

ARG APP_VERSION="invalid"

RUN cd /tmp && \
    chmod +x gradlew && \
    ./gradlew deploy -P version=$APP_VERSION --no-daemon && \
    mv build/libs/CapivaraBot.jar /app && \
    rm -rf /tmp/*

FROM docker.io/eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app .

ENV JAVA_ARGS="-Xmx300M"
ENV LOG_DIRECTORY="/app/logs"
ENV SPRING_CONFIG_LOCATION="/app/main.properties"
ENV DISCORD_TOKEN="invalid"
ENV CURUPIRA_RESET="true"
ENV LOG_CHANNEL_ID="invalid"
ENV DATABASE_DRIVER="org.postgresql.Driver"
ENV DATABASE_DIALECT="org.hibernate.dialect.PostgreSQL95Dialect"
ENV DATABASE_URL="jdbc:postgresql://localhost:5432/capivara"
ENV DATABASE_USERNAME="database-username"
ENV DATABASE_PASSWORD="database-password"

ENTRYPOINT java ${JAVA_ARGS} -jar CapivaraBot.jar \
    --token=${DISCORD_TOKEN} \
    --spring.config.location=file:${SPRING_CONFIG_LOCATION} \
    --curupira.reset=${CURUPIRA_RESET} \
    --log.directory=${LOG_DIRECTORY} \
    --log.channel.id=${LOG_CHANNEL_ID} \
    --spring.datasource.driverClassName=${DATABASE_DRIVER} \
    --spring.jpa.database-platform=${DATABASE_DIALECT} \
    --spring.datasource.url=${DATABASE_URL} \
    --spring.datasource.username=${DATABASE_USERNAME} \
    --spring.datasource.password=${DATABASE_PASSWORD}