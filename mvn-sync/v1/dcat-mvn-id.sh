#!/bin/bash

# Return a maven identifier for a (dcat) file in a maven repository

# dcatFile=`realpath -e "$1"`

parent-find() {
  local file="$1"
  local dir="$2"

  test -e "$dir/$file" && echo "$dir" && return 0
  [ '/' = "$dir" ] && return 1

  parent-find "$file" "$(dirname "$dir")"
}


dcat-mvn-id() {
  dcatFile="$1"

  metaName='maven-metadata.xml'
  metaFolder=`parent-find "$metaName" "$dcatFile"`

  if [ -z "$metaFolder" ]; then
    echo "No $metaName file found in any parent of $dcatFile"
    exit 1
  fi

  metaFile="$metaFolder/$metaName"

  # Parse groupId, artifactId and version from the maven-metadata.xml file
  groupId=`xmlstarlet sel -t -v "/metadata/groupId" "$metaFile"`
  rawArtifactId=`xmlstarlet sel -t -v "/metadata/artifactId" "$metaFile"`
  artifactId=`echo "$rawArtifactId" | sed -E "s|^(.*)-dcat-metadata$|\1|g"`

  givenVersion=`xmlstarlet sel -t -v "/metadata/version" "$metaFile"`

  if [ -z "$givenVersion" ]; then
    relPath=`realpath --relative-to="$metaFolder" "$dcatFile"`
    derivedVersion=`dirname "$relPath"`
    if [ "$derivedVersion" == "." ]; then
      echo "No version could be derived for given file $dcatFile"
      exit 1
    fi
    version="$derivedVersion"
  else
    version="$givenVersion"
  fi

  echo "$groupId:$artifactId:$version"
}

