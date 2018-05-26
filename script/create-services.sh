#!/usr/bin/env bash

ssh pi1 "docker run -dp=5556:5556 -e 'functionality=service' -e 'services=RandomGenerator' -e 'job_id=12345' --network hermes johnson163/hermes-arm"
ssh pi2 "docker run -dp=5556:5556 -e 'functionality=service' -e 'services=PrintService' -e 'job_id=12345' --network hermes johnson163/hermes-arm"
ssh workstation "docker run -dp=5556:5556 -e 'functionality=service' -e 'services=EchoService' -e 'job_id=12345' --network hermes johnson163/hermes"
