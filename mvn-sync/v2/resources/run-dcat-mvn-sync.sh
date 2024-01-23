#!/bin/bash

echoerr() { echo "$@" 1>&2; }

SCRIPT_FILE="$(realpath "${BASH_SOURCE:-$0}")"
SCRIPT_DIR="$(dirname "$SCRIPT_FILE")"

echoerr "SCRIPT_DIR=$SCRIPT_DIR"

WATCH_DIR="$1"
WORK_DIR="$2"

if [ -z "$WATCH_DIR" ]; then
  echo "No path to watch specified"
  exit 1
fi

if [ -z "$WORK_DIR" ]; then
  echo "No path to work dir specified (this is where meta projects are generated)"
  exit 1
fi

. "$SCRIPT_DIR"/dcat-mvn-id.sh

process-file() {
  REPO="$1"
  FILE="$2"
  EVENT="$3"

  echo "Detected event $EVENT on file: $FILE"

  # Match dataset artifacts - for those files we instantiate metadata projects
  export IN_TYPE="$(echo "$FILE" | sed -nE 's|^.*\.((nt\|ttl\|nq\|trig\|rdf(\.xml)?)(\.(gz\|bz2))?)$|\1|p')"

  if  [[ "$FILE" =~ ^.*-dcat\..*\.*$ && ! -z "$IN_TYPE" ]]; then
    echo "Processing as dcat metadata: $FILE"
    "$SCRIPT_DIR"/sync-mvn.sh "$FILE"
    echo "Completed as dcat metadata: $FILE"
  elif [ ! -z "$IN_TYPE" -a "$EVENT" != "DELETE" ]; then
    # Note: Deletion of data so far does not trigger removal of metadata
    # Future versions of this script could support options for that

    echo "Processing as data artifact: $FILE"
    declare -A map
    dcat-mvn-id-core "map" "$FILE"

    export IN_GROUPID="${map['groupId']}"
    export IN_ARTIFACTID=${map['artifactId']}
    export IN_VERSION="${map['version']}"

    SNAPSHOT=""
    # base version has "-SNAPSHOT" stripped
    BASE_VERSION="${IN_VERSION%%-SNAPSHOT}"
    [[ "$BASE_VERSION" != "$IN_VERSION" ]] && SNAPSHOT="-SNAPSHOT"
    
    export OUT_GROUPID="dcat.${IN_GROUPID}"
    export OUT_ARTIFACTID="$IN_ARTIFACTID"
    # TODO Auto-Increment suffix number based on repository content
    export OUT_VERSION="${BASE_VERSION}-1${SNAPSHOT}"

    OUT_FOLDER="$REPO/${OUT_GROUPID//.//}/$OUT_ARTIFACTID"
    OUT_FILE="$OUT_FOLDER/pom.xml"

    echo "Outfolder: $OUT_FOLDER"

    mkdir -p "$OUT_FOLDER"
    cat "$SCRIPT_DIR/metadata.template.pom.xml" | envsubst '$IN_GROUPID $IN_ARTIFACTID $IN_VERSION $IN_TYPE $OUT_GROUPID $OUT_ARTIFACTID $OUT_VERSION' > "$OUT_FILE"

    # (cd "$OUT_FOLDER" && mvn install)
    (cd "$OUT_FOLDER" && mvn -Prelease deploy -Dmaven.install.skip)

    echo "Completed processing as data artifact: $FILE"
    #mvn org.apache.maven.plugins:maven-install-plugin:3.1.1:install-file  -Dfile=path-to-your-artifact-jar \
    #                                                                              -DgroupId=your.groupId \
    #                                                                              -DartifactId=your-artifactId \
    #                                                                              -Dversion=version \
    #                                                                              -Dpackaging=jar \
    #                                                                              -DlocalRepositoryPath=path-to-specific-local-repo
    # mvn install:install-file \
    # -Dpackaging=pom \
    # -Dfile=tmp/dependency-management-1.0.0-SNAPSHOT.pom \
    # -DpomFile=tmp/dependency-management-1.0.0-SNAPSHOT.pom

    # Match metadata files with a '-dcat.' in their name:
    # These files are loaded into a triple store
  fi
}

# process-file "/tmp/repo" "/home/raven/.m2/repository/org/coypu/data/disasters/disasters/0.20240108.1501/disasters-0.20240108.1501.nt.bz2"
# "/home/raven/.m2/repository/org/coypu/data/climatetrace/disasters/0.20240108.1501-SNAPSHOT/disasters-0.20240108.1501-SNAPSHOT-dcat.nt.bz2"

inotifywait "$WATCH_DIR" --recursive --monitor --format '%w%f\t%e' --event CLOSE_WRITE --event DELETE | \
  while read RECORD; do
    FILE="$(echo "$RECORD" | cut -f 1)"
    EVENT="$(echo "$RECORD" | cut -f 2)"
    process-file "$WORK_DIR" "$FILE" "$EVENT"
  done

