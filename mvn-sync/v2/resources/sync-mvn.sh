#!/bin/bash

echoerr() { echo "$@" 1>&2; }

echoerr "PWD: $(pwd)"

SCRIPT_FILE="$(realpath "${BASH_SOURCE:-$0}")"
SCRIPT_DIR="$(dirname "$SCRIPT_FILE")"

set -eu

. "$SCRIPT_DIR"/maven-utils.sh

#export baseUrl="http://maven.aksw.org/repository/"

prefix="$1"
relfile="$2"
absfile="${1}${2}"

mvnId="$(path-to-maven-gav "$relfile")"

# TODO Only keep the latest metadata version in the target store
# This means that we need to extract the number after the last '-'

mvnUrn="urn:mvn:$mvnId"

# If the dcat file exists, then apply post processing and load it into the store
# Conversely, if the file does not exist then assume it was deleted and drop the corresponding graph
if [ -f "$absfile" ]; then
  tmpFile=`mktemp /tmp/dcat-mvn-sync.XXXXXX.trig`

  echo "Filtering data"
  # Filter the content to the graph that matches the mvnUrn
  # "DELETE { ?s ?p ?o } INSERT { GRAPH <$mvnUrn> { ?s ?p ?o } } WHERE { ?s ?p ?o }"
  rpt integrate -X "$absfile" \
    "MOVE DEFAULT TO <$mvnUrn>" \
    "CONSTRUCT { GRAPH ?g { ?s ?p ?o } } WHERE { GRAPH ?g { ?s ?p ?o } FILTER(STRSTARTS(STR(?g), '$mvnUrn')) }" > "$tmpFile"

  # TODO The link to the pom file is a stub - eg:urn
  #rpt integrate -X --io "$tmpFile" --out-format trig/blocks "DELETE { GRAPH ?g { ?s ?p ?o } } INSERT { GRAPH ?g { ?s ?p ?x } } WHERE { GRAPH ?g { ?s ?p ?o } FILTER (?p IN( <http://www.w3.org/ns/dcat#downloadURL>, <http://www.example.org/urn> )&& STRSTARTS(STR(?o), 'urn:mvn:')) BIND(IRI(CONCAT('$baseUrl', mvn:toPath(STR(?o)))) AS ?x) }" gspo.rq

  rpt integrate -X --io "$tmpFile" --out-format trig/blocks $(find ./prepublish/ -name '*.ru' -print0 | sort -zu | xargs -0 printf '%q ') gspo.rq

  echo "New data:"
  cat "$tmpFile"

  echo "Replacing data"
  # Upload the graph to the endpoint (requires write access)
  # rpt integrate -e remote --loc http://localhost:8642/sparql "DROP GRAPH <$mvnUrn>" "$tmpFile"
  # rpt integrate -X -e remote --loc http://localhost:8642/sparql --db-loader insert "DELETE { GRAPH ?g { ?s ?p ?o } } WHERE { GRAPH ?g { ?s ?p ?o } FILTER(STRSTARTS(STR(?g), '$mvnUrn')) }" "$tmpFile"
  rpt integrate -X -e remote --loc http://localhost:8642/sparql --db-loader insert "DROP GRAPH <$mvnUrn>" "$tmpFile"

  rm "$tmpFile"
else
  rpt integrate -X -e remote --loc http://localhost:8642/sparql "DROP GRAPH <$mvnUrn>"  
fi

