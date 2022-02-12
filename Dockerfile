FROM alpine

RUN apk upgrade --no-cache && \
    apk add --no-cache openjdk17-jre-headless

WORKDIR /opt/vsssfshss

ADD target/vsssfshss-0.0.1-SNAPSHOT.jar .

EXPOSE 8080/tcp

CMD java -jar vsssfshss-0.0.1-SNAPSHOT.jar