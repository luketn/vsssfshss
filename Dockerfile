FROM alpine

RUN apk upgrade --no-cache && \
    apk add --no-cache openjdk17-jre-headless

WORKDIR /opt/vsssfshss

ADD target/quarkus-app .

EXPOSE 8080/tcp

CMD java -jar quarkus-run.jar