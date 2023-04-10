FROM maven:3-amazoncorretto-17

COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package

EXPOSE 8080

RUN cp /tmp/target/*.jar /opt/om-1.0.0.jar

CMD java -jar /opt/om-1.0.0.jar