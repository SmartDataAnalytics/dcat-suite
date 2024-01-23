#!/bin/bash

# Return a maven identifier for a (dcat) file in a maven repository
# dcatFile=`realpath -e "$1"`

# ISSUES with deletion
# - "realpath" and "find" do fail if the paths do no longer exists
# - the maven-metadata.xml file may have been deleted. Maybe add a fallback that extracts the GAV from the file path?

parent-find() {
  local pattern="$1"
  local search_dir="$2"

  while [ "$search_dir" != "/" ]; do
    file="$(find "$search_dir" -maxdepth 1 -name "$pattern" -print -quit)"
    if [[ -n $file ]]; then
        echo "$file"
        return 0
    fi
    search_dir="$(dirname "$search_dir")"
  done
  return 1
}

dcat-mvn-id-core() {
  declare -n amap="$1"
  dcatFile="$2"

  metaFilePattern='maven-metadata*.xml'
  metaFile="$(parent-find "$metaFilePattern" "$(dirname "$dcatFile")")"
  if [ -z "$metaFile" ]; then
    echo "No $metaFilePattern file found in any parent of $dcatFile"
    return 1
  fi

  metaFolder=`dirname "$metaFile"`

  # metaFile="$metaFolder/$metaName"

  # Parse groupId, artifactId and version from the maven-metadata.xml file
  amap['groupId']="$(xmlstarlet sel -t -v "/metadata/groupId" "$metaFile")"
  rawArtifactId="$(xmlstarlet sel -t -v "/metadata/artifactId" "$metaFile")"
  amap['artifactId']="$(echo "$rawArtifactId" | sed -E "s|^(.*)-dcat-metadata$|\1|g")"

  givenVersion="$(xmlstarlet sel -t -v "/metadata/version" "$metaFile")"

  if [ -z "$givenVersion" ]; then
    relPath=`realpath --relative-to="$metaFolder" "$dcatFile"`
    derivedVersion=`dirname "$relPath"`
    if [ "$derivedVersion" == "." ]; then
      echo "No version could be derived for given file $dcatFile"
      return 1
    fi
    version="$derivedVersion"
  else
    version="$givenVersion"
  fi

  # echo "$groupId:$artifactId:$version"
  amap['version']="$version"
}


dcat-mvn-id() {
  dcatFile="$1"
  declare -A map

  dcat-mvn-id-core "map" "$dcatFile"
  echo "${map['groupId']}:${map['artifactId']}:${map['version']}"
}

# dcat-mvn-id "/home/raven/.m2/repository/dcat/org/coypu/data/climatetrace/disasters/0.20240108.1501-SNAPSHOT/disasters-0.20240108.1501-SNAPSHOT-dcat.ttl.bz2"


