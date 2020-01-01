FROM openjdk:8-jdk-alpine
MAINTAINER yong.fei <best.fei@good.com>
ENV TZ=Asia/Shanghai
RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
VOLUME /tmp
ADD mocksite-1.0.0.jar mocksite.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/mocksite.jar"]