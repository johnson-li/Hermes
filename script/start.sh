#!/usr/bin/env bash

/etc/init.d/dbus start
pulseaudio -D

cd "$(dirname "$0")"
cd ..

arch=`uname -m`

if [ "${arch}" = "x86_64" ]; then
    java -cp ./build/libs/hermes-1.0-SNAPSHOT.jar core.Starter
else
    java -Xbootclasspath/p:./lib/alpn-boot-8.1.9.v20160720.jar -cp ./build/libs/hermes-1.0-SNAPSHOT.jar core.Starter
fi;
