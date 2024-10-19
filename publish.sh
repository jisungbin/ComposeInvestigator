#!/bin/bash

set -e

PROJECTS=(
  ":runtime"
  ":compiler"
  ":compiler-gradle-plugin"
)

COMMAND_SUFFIX="publishMavenPublicationToMavenCentralRepository"

if [ "$1" == "local" ]; then
  COMMAND_SUFFIX="publishToMavenLocal"
fi

for PROJECT in "${PROJECTS[@]}"; do
  echo "Executing: ./gradlew ${PROJECT}:${COMMAND_SUFFIX}"
  ./gradlew "${PROJECT}:${COMMAND_SUFFIX}" --console=plain
done
