FROM java:8-jdk
RUN apt-get update
RUN apt-get install -y zsh wget curl git software-properties-common vim iputils-ping

WORKDIR /hermes
COPY bin/webrtc bin/webrtc
COPY script script
COPY build/libs/hermes-1.0-SNAPSHOT.jar build/libs/hermes-1.0-SNAPSHOT.jar

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

CMD script/start.sh
