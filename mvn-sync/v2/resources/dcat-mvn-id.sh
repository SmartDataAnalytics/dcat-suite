#!/bin/bash

# Return a maven identifier for a (dcat) file in a maven repository
# dcatFile=`realpath -e "$1"`

# ISSUES with deletion
# - "realpath" and "find" do fail if the paths do no longer exists
# - the maven-metadata.xml file may have been deleted.

# SOLUTION: Use "dcat-mvn-id-from-path" which operates on the path string itself.

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

##
# Given a path to a maven artifact file, derive the maven coordinates, i.e.
# groupId, artifactId, version, classifier and type.
#
# Additional fields are qualifier, baseVersion and snapshot.
#
# /absolute/path/to/repo  ./org/aksw/jenax/jenax-models-dcat-api/4.8.0-1-SNAPSHOT/jenax-models-dcat-api-4.8.0-1-20230317.133558-57-sources.jar.sha1
#
dcat-mvn-id-from-path() {
  declare -n amap="$1"
  dcatFile="$2"

  snapshotVersionPattern='-\\d+\.\\d+-\\d+'

  filename="$(basename "$dcatFile")"

  # Parent of the given path is assumed to be the version
  versionPath="$(dirname "$dcatFile")"
  version="$(basename "$versionPath")"
  
  # Parent of the versionPath is assumed to be the artifactId
  artifactIdPath="$(dirname "$versionPath")"
  artifactId="$(basename "$artifactIdPath")"
  
  # Remaining parents are assumed to be the groupId
  groupPathTmp="$artifactIdPath"
  groupId=""
  while [ "$groupPathTmp" != '.' -a "$groupPathTmp" != '.' ]; do
    segment="$(basename "$groupPathTmp")"    
    groupPathTmp="$(dirname "$groupPathTmp")"
    
    if [ "$segment" = '.' ]; then
      continue
    fi

    [[ "$groupId" = "" ]] && groupId="$segment" || groupId="${segment}.${groupId}"
  done
  
  snapshot=""
  # base version has "-SNAPSHOT" stripped
  baseVersion="${version%%-SNAPSHOT}"
  [[ "$baseVersion" != "$version" ]] && snapshot="-SNAPSHOT"
  
  # Remove the snapshot qualifier which has the form "-20230317.133558-57"
  # - Remove the artifactId prefix from the filename
  # avsct = artifact version qualifier classifier type
  vqct="${filename#"$artifactId"}"
  qct="${vqct#"-$baseVersion"}"

  qStr="$(sed -nE "s|^(-[0-9]+\.[0-9]+-[0-9]+).*|\1|p" <<< "$qct")"
  q="${qStr#-}"
  ct="${qct#"$q"}"
  
  # Extract the classifier if present. It starts with a dash and is delimeted by dot.
  cStr="$(sed -nE "s|^(-[^.]*).*|\1|p" <<< "$ct")"
  c="${cStr#-}"

  tStr="${ct#"-$c"}"
  t="${tStr#"."}"
  
  # Parse groupId, artifactId and version from the maven-metadata.xml file
  amap['groupId']="$groupId"
  amap['artifactId']="$artifactId"
  amap['version']="$version"

  amap['classifierStr']="$cStr"
  amap['classifier']="$c"

  amap['typeStr']="$tStr"
  amap['type']="$t"

  amap['snapshotQualifierStr']="$qStr"
  amap['snapshotQualifier']="$q"
  
  amap['versionSuffix']="$snapshot"
}

dcat-mvn-id() {
  dcatFile="$1"
  declare -A map

  dcat-mvn-id-core "map" "$dcatFile"
  echo "${map['groupId']}:${map['artifactId']}:${map['version']}"
  # echo "${map['groupId']}:${map['artifactId']}:${map['version']} | ${map['classifier']} | ${map['type']} | ${map['snapshotQualifier']} | ${map['versionSuffix']}"
}

# dcat-mvn-id "/home/raven/.m2/repository/dcat/org/coypu/data/climatetrace/disasters/0.20240108.1501-SNAPSHOT/disasters-0.20240108.1501-SNAPSHOT-dcat.ttl.bz2"


