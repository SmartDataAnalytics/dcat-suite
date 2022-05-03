#!/usr/bin/env bash

LIB_DIR="/usr/share/lib/rdf-processing-toolkit-cli/"
MAIN_CLASS="org.aksw.rdf_processing_toolkit.cli.main.MainCliRdfProcessingToolkit"

# EXTRA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED"
/lib/jvm/java-1.11.0-openjdk-amd64/bin/java $EXTRA_OPTS $JAVA_OPTS -cp "$LIB_DIR:$LIB_DIR/lib/*" "$MAIN_CLASS" integrate -e tdb2 --loc dcat-mvn-db --db-keep --server

