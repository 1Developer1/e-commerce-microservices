FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY shared-kernel/pom.xml shared-kernel/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY shared-kernel/src shared-kernel/src
COPY product-service/src product-service/src
RUN apk add --no-cache maven && \
    mvn clean package -pl shared-kernel,product-service -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /build/product-service/target/product-service-1.0-SNAPSHOT.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
