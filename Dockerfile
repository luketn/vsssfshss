FROM alpine

RUN apk upgrade --no-cache && \
    apk add --no-cache openjdk17-jre-headless

WORKDIR /opt/vsssfshss

ADD target/vsssfshss.jar .

EXPOSE 8080/tcp

CMD java -jar vsssfshss.jar