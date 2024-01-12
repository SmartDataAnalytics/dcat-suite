#!/bin/bash

set -eu

. dcat-mvn-id.sh

echo "here"

export baseUrl="http://maven.aksw.org/repository/"

file="$1"

mvnId=`dcat-mvn-id "$file"`

# TODO Only keep the latest metadata version in the target store
# This means that we need to extract the number after the last '-'

mvnUrn="urn:mvn:$mvnId"

tmpFile=`mktemp /tmp/dcat-mvn-sync.XXXXXX.trig`

echo "Filtering data"
# Filter the content to the graph that matches the mvnUrn
rpt integrate "$file" \
  "DELETE { ?s ?p ?o } INSERT { GRAPH <$mvnUrn> { ?s ?p ?o } } WHERE { ?s ?p ?o }" \
  "CONSTRUCT { GRAPH ?g { ?s ?p ?o } } WHERE { GRAPH ?g { ?s ?p ?o } FILTER(STRSTARTS(STR(?g), '$mvnUrn')) }" > "$tmpFile"

# TODO The link to the pom file is a stub - eg:urn
rpt integrate --io "$tmpFile" --out-format trig/blocks "DELETE { GRAPH ?g { ?s ?p ?o } } INSERT { GRAPH ?g { ?s ?p ?x } } WHERE { GRAPH ?g { ?s ?p ?o } FILTER (?p IN( <http://www.w3.org/ns/dcat#downloadURL>, <http://www.example.org/urn> )&& STRSTARTS(STR(?o), 'urn:mvn:')) BIND(IRI(CONCAT('$baseUrl', mvn:toPath(STR(?o)))) AS ?x) }" gspo.rq

echo "New data:"
cat "$tmpFile"

echo "Replacing data"
# Upload the graph to the endpoint (requires write access)
# rpt integrate -e remote --loc http://localhost:8642/sparql "DROP GRAPH <$mvnUrn>" "$tmpFile"
rpt integrate -e remote --loc http://localhost:8642/sparql "DELETE { GRAPH ?g { ?s ?p ?o } } WHERE { GRAPH ?g { ?s ?p ?o } FILTER(STRSTARTS(STR(?g), '$mvnUrn')) }" "$tmpFile"

rm "$tmpFile"

