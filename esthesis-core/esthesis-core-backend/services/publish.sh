#!/usr/bin/env sh

cd srv-about && ./publish.sh && cd .. && \
cd srv-agent && ./publish.sh && cd .. && \
cd srv-application && ./publish.sh && cd .. && \
cd srv-audit && ./publish.sh && cd .. && \
cd srv-campaign && ./publish.sh && cd .. && \
cd srv-command && ./publish.sh && cd .. && \
cd srv-crypto && ./publish.sh && cd .. && \
cd srv-dataflow && ./publish.sh && cd .. && \
cd srv-device && ./publish.sh && cd .. && \
cd srv-dt && ./publish.sh && cd .. && \
cd srv-infrastructure && ./publish.sh && cd .. && \
cd srv-kubernetes && ./publish.sh && cd .. && \
cd srv-provisioning && ./publish.sh && cd .. && \
cd srv-public-access && ./publish.sh && cd .. && \
cd srv-security && ./publish.sh && cd .. && \
cd srv-settings && ./publish.sh && cd .. && \
cd srv-tag && ./publish.sh && cd ..
