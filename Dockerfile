FROM java:8-jdk

RUN apt-get update
RUN apt-get install -y bsdmainutils pulseaudio consolekit libav-tools libopencv-dev

WORKDIR /hermes
COPY bin/webrtc bin/webrtc
COPY bin/darknet bin/darknet
COPY script script
COPY lib lib
COPY build/libs/hermes-1.0-SNAPSHOT.jar build/libs/hermes-1.0-SNAPSHOT.jar

CMD script/start.sh
