#!/usr/bin/env bash

mvn clean package
#TODO: parametrise jar name
docker build . --build-arg JAR_FILE=target/remind-me-0.0.1-SNAPSHOT.jar -t remind-me