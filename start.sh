#!/bin/sh

docker build -t acme/keycloak .
docker-compose up