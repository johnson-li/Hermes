#!/usr/bin/env bash

cd "$(dirname "$0")"
cd ..
java -cp ./build/libs/hermes-1.0-SNAPSHOT.jar core.Starter
