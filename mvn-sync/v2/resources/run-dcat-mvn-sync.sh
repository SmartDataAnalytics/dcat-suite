#!/bin/bash

##
# Script to watch a set of maven repositories
# abs_watch_dir/reponame/artifactFile
#

echoerr() { echo "$@" 1>&2; }

SCRIPT_FILE="$(realpath "${BASH_SOURCE:-$0}")"
SCRIPT_DIR="$(dirname "$SCRIPT_FILE")"

echoerr "SCRIPT_DIR=$SCRIPT_DIR"

WATCH_DIR_RAW="$1"
# Normalize watch dir into an absolute path. Also removes trailing slash.
WATCH_DIR="$(cd "$WATCH_DIR_RAW"; pwd)"

WORK_DIR="$2"

if [ -z "$WATCH_DIR" ]; then
  echo "No path to watch specified"
  exit 1
fi

if [ -z "$WORK_DIR" ]; then
  echo "No path to work dir specified (this is where meta projects are generated)"
  exit 1
fi

. "$SCRIPT_DIR"/maven-utils.sh

##
# parse-repo-path outMap WATCH_DIR ABSFILE
#
# Parse the absolute path "ABSFILE" against "WATCH_DIR" into the components
# prefix/file where prefix=watch_dir/reponame
#
parse-repo-path() {
  declare -n amap="$1"

  WATCH_DIR="$2"
  ABSFILE="$3"

  # Cut away the watch dir
  REPOFILE="${ABSFILE#"$WATCH_DIR/"}"

  # Extract the repo folder
  REPONAME="$(cut -d "/" -f1 <<< "$REPOFILE")"
    
  # Cut away the repo folder which leaves us with the relative path to the file
  FILE="${REPOFILE#"$REPONAME/"}"
  
  amap['reponame']="$REPONAME"
  amap['prefix']="$WATCH_DIR/$REPONAME/"
  amap['file']="$FILE"
}

##
# process-file WORK_DIR PREFIX RELFILE EVENT
# 
# PREFIX must end with '/' - it as assumed that ${PREFIX}${RELFILE} forms an absolute path.
#
process-file() {
  WORK_DIR="$1"
  PREFIX="$2"
  RELFILE="$3"
  EVENT="$4"

  echo "Detected event $EVENT on file: $RELFILE (under $PREFIX)"

  # Match dataset artifacts - for those files we instantiate metadata projects
  export IN_TYPE="$(echo "$RELFILE" | sed -nE 's|^.*\.((nt\|ttl\|nq\|trig\|rdf(\.xml)?)(\.(gz\|bz2))?)$|\1|p')"

  if  [[ "$RELFILE" =~ ^.*-dcat\..*\.*$ && ! -z "$IN_TYPE" ]]; then
    echo "Processing as dcat metadata: $RELFILE"
    "$SCRIPT_DIR"/sync-mvn.sh "$PREFIX" "$RELFILE"
    echo "Completed as dcat metadata: $RELFILE"
  elif [ ! -z "$IN_TYPE" -a "$EVENT" != "DELETE" ]; then
    # Note: Deletion of data so far does not trigger removal of metadata
    # Future versions of this script could support options for that

    echo "Processing as data artifact: $RELFILE (under $PREFIX)"
    declare -A map
    parse-maven-path "map" "$RELFILE"

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

    OUT_FOLDER="$WORK_DIR/${OUT_GROUPID//.//}/$OUT_ARTIFACTID"
    OUT_FILE="$OUT_FOLDER/pom.xml"

    echo "Outfolder: $OUT_FOLDER"

    mkdir -p "$OUT_FOLDER"
    cat "$SCRIPT_DIR/metadata.template.pom.xml" | envsubst '$IN_GROUPID $IN_ARTIFACTID $IN_VERSION $IN_TYPE $OUT_GROUPID $OUT_ARTIFACTID $OUT_VERSION' > "$OUT_FILE"

    # (cd "$OUT_FOLDER" && mvn install)
    (cd "$OUT_FOLDER" && mvn -Prelease deploy -Dmaven.install.skip)

    echo "Completed processing as data artifact: $RELFILE (under $PREFIX)"
  fi
}

# process-file "/tmp/repo" "/home/raven/.m2/repository/org/coypu/data/disasters/disasters/0.20240108.1501/disasters-0.20240108.1501.nt.bz2"
# "/home/raven/.m2/repository/org/coypu/data/climatetrace/disasters/0.20240108.1501-SNAPSHOT/disasters-0.20240108.1501-SNAPSHOT-dcat.nt.bz2"

inotifywait "$WATCH_DIR" --recursive --monitor --format '%e %w%f' --event CLOSE_WRITE --event DELETE | \
  while read RECORD; do
    EVENT="$(cut -d' ' -f1 <<< "$RECORD")"
    # -f2- outputs all columns starting from the 2nd one
    ABSFILE="$(cut -d' ' -f2- <<< "$RECORD")"

    declare -A map
    parse-repo-path "map" "$WATCH_DIR" "$ABSFILE"
    
    PREFIX="${map['prefix']}"
    RELFILE="${map['file']}"

    echo "Change $EVENT in '$RELFILE' (under '$PREFIX')"
    process-file "$WORK_DIR" "$PREFIX" "$RELFILE" "$EVENT"
  done



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

