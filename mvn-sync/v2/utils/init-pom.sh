#!/bin/bash

# This script instantiates the template pom.xml in order to create
# a fresh maven repository with only the relevant dependencies
# such that they can be copied into the docker image

export IN_GROUPID="org.example.test"
export IN_ARTIFACTID="test"
export IN_VERSION="0.0.1-SNAPSHOT"

export OUT_GROUPID="dcat.${IN_GROUPID}"
export OUT_ARTIFACTID="$IN_ARTIFACTID"
export OUT_VERSION="${IN_VERSION}"

OUT_FOLDER="./target"
OUT_FILE="$OUT_FOLDER/pom.xml"

mkdir -p "$OUT_FOLDER"

#cat metadata.template.pom.xml | envsubst '$IN_GROUPID $IN_ARTIFACTID $IN_VERSION $OUT_GROUPID $OUT_ARTIFACTID $OUT_VERSION' > "$OUT_FILE"
cp seed.pom.xml "$OUT_FILE"
cd "$OUT_FOLDER"

#mvn -DskipPrepareInput -Dmdep.useRepositoryLayout=true dependency:copy-dependencies
#mvn -DskipPrepareInput -D"maven.repo.local=repository" compile
mvn -D"maven.repo.local=repository" compile
#mv target/dependency repository
#rmdir target

