FROM java:8-jdk

WORKDIR /hermes
COPY bin/webrtc bin/webrtc
COPY script script
COPY build/libs/hermes-1.0-SNAPSHOT.jar build/libs/hermes-1.0-SNAPSHOT.jar

CMD script/start.sh
