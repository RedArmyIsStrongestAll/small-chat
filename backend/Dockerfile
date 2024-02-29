FROM eclipse-temurin:17.0.10_7-jdk AS build
WORKDIR /build_dir
COPY ./.mvn ./.mvn
COPY ./pom.xml ./mvnw ./
RUN chmod 777 -R /build_dir && sed -i 's/\r$//' mvnw
RUN ./mvnw dependency:go-offline
COPY ./src /build_dir/src
RUN ./mvnw package -P without-application

FROM eclipse-temurin:17.0.10_7-jre AS run
ARG APP_VERSION=0.0.2
ARG NAME_PROPERTIES_FILE=application.dev.properties
COPY --from=build /build_dir/target/small-chat-$APP_VERSION.jar /app/small-chat.jar
COPY ./applications/$NAME_PROPERTIES_FILE /app/application.properties
WORKDIR /app
RUN chmod 777 ./small-chat.jar
CMD ["java", "-jar", "small-chat.jar"]

EXPOSE 8080