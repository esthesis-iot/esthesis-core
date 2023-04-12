#!/usr/bin/env sh

mvn clean install && \
cd dataflows && ./publish.sh && cd .. && \
cd services && ./publish.sh && cd ..
