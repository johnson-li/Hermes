#!/usr/bin/env bash

ssh workstation-vm "docker run -d -e 'functionality=service' -e 'services=RandomGenerator' -e 'job_id=12345' --network host johnson163/hermes-cuda"
ssh pi2 "docker run -d -e 'functionality=service' -e 'services=PrintService' -e 'job_id=12345' --network host johnson163/hermes-arm"
ssh workstation "docker run -d -e 'functionality=service' -e 'services=EchoService' -e 'job_id=12345' --network host johnson163/hermes-cuda"
