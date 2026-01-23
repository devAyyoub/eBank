FROM quay.io/keycloak/keycloak:latest

COPY keycloak-config.json /opt/keycloak/data/import/realm-export.json

RUN /opt/keycloak/bin/kc.sh build
