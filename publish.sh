#!/usr/bin/env sh

cd esthesis-core-backend && ./publish.sh && cd .. && \
cd esthesis-core-ui && ./publish.sh && cd .. && \
cd esthesis-core-device && ./publish.sh && cd ..
