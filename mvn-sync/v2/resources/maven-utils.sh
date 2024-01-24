#!/bin/bash

##
# Given a path to a maven artifact file, derive the maven coordinates, i.e.
# groupId, artifactId, version, classifier and type.
#
# Additional fields are qualifier, baseVersion and snapshot.
#
# /absolute/path/to/repo  ./org/aksw/jenax/jenax-models-dcat-api/4.8.0-1-SNAPSHOT/jenax-models-dcat-api-4.8.0-1-20230317.133558-57-sources.jar.sha1
#
parse-maven-path() {
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
  groupPathTmp="$(dirname "$artifactIdPath")"
  groupId=""
  while [ "$groupPathTmp" != '.' -a "$groupPathTmp" != '.' -a "$groupPathTmp" != '/' ]; do
    segment="$(basename "$groupPathTmp")"    
    groupPathTmp="$(dirname "$groupPathTmp")"
    
    if [ "$segment" = '.' -o "$segment" = '/' ]; then
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

path-to-maven-gav() {
  dcatFile="$1"
  declare -A map

  parse-maven-path "map" "$dcatFile"
  echo "${map['groupId']}:${map['artifactId']}:${map['version']}"
  # echo "${map['groupId']}:${map['artifactId']}:${map['version']} | ${map['classifier']} | ${map['type']} | ${map['snapshotQualifier']} | ${map['versionSuffix']}"
}

