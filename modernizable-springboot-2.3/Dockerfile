#
# Build
#
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /opt/shipping

COPY pom.xml /opt/shipping/
RUN mvn dependency:resolve -B --no-transfer-progress
COPY src /opt/shipping/src/
RUN mvn package -B --no-transfer-progress -DskipTests

#
# Run
#
FROM eclipse-temurin:21-jre

EXPOSE 8080

WORKDIR /opt/shipping

RUN groupadd -r shipping && useradd -r -g shipping shipping
USER shipping

ENV CART_ENDPOINT=cart:8080
ENV DB_HOST=mysql

COPY --from=build /opt/shipping/target/shipping-1.0.jar shipping.jar

CMD [ "java", "-Xmn256m", "-Xmx768m", "-jar", "shipping.jar" ]

