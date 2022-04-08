## Transformation of DCAT Models

## Transform now supports default value expressions and string substitutions
TODO Add those to the documentation
```
dcat transform create -g org.aksw.sportal -a void -v 1.0.0 rdf-processing-toolkit-parent/use-case-sportal-analysis/src/main/resources/compact/* -b '?D=<$INPUT>' -b '?B = <$INPUT/$CLASSIFIER>'  > void.conjure.ttl
```


The dcat tansform command allows for applying transformation on the content in RDF files

Synopsis
```bash
dcat transform [-m] [--transform xform.sparql]* input-dcat.ttl
```
* `-m` | `--materialize` Flag to indicate whether to execute/materialize the specified transformation. Withouth this flag, a specification is built that can be run at a later stage.
* --transform xform.sparql zero or more instances of transformation in terms of .sparql files - i.e. files that contain a sequence of SPARQL queries
* `input-dcat.ttl` The input DCAT model whose distributions are subject to transformation


TODO: Describe the approach in case all distributions are equivalent in content, however multiple output formats (e.g. nt, ttl and hdt) are desired. In this case, it is sufficient to apply a transformation only once.


Given a DCAT snippet as below, transformations of the data contained in distributions based on conjure can be performed as follows.
At the time of writing, conjure only supports SPARQL-based transformations, but adding other transformation types is on its roadmap.

```turtle
[ a                 cat:Dataset ;
  dataid:artifact   "rostock_baumaerkte" ;
  dataid:group      "org.limbo.poi-rostock" ;
  dcterms:issued    "2020-02-03T17:51:54.007+01:00"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
  dcterms:license   limbo:NullLicense ;
  void:triples      196 ;
  eg:localId        "rostock_baumaerkte" ;
  owl:versionInfo   "2020-01-20" ;
  cat:distribution  [ a                cat:Distribution ;
                      void:triples     196 ;
                      eg:localId       "rostock_baumaerkte" ;
                      eg:relPath       "rostock_baumaerkte-2020-01-20.ttl" ;
                      cat:downloadURL  <file:///home/raven/Projects/limbo/git/poi-rostock/rostock_baumaerkte-2020-01-20.ttl>
                    ]
] .

```

Transformation of the initial DCAT model in order for its distributions to contain the specification to apply a transformation based on `replacens.sparql` together with a given binding of its placeholders `SOURCE_NS` and `TARGET_NS`:


```bash
dcat transform -D 'SOURCE_NS=https://portal.limbo-project.org' -D 'TARGET_NS=https://data.limbo-project.org' --transform replacens.sparql /home/raven/Projects/limbo/git/poi-rostock/target/effective.dcat.ttl > intermediate.dcat.ttl
```


The resulting dcat model now no longer has a downloadURL but instead an rpif:op predicate with the workflow specification that upon execution performs he transformation:

```turtle
[ a                 cat:Dataset ;
  dataid:artifact   "rostock_baumaerkte" ;
  dataid:group      "org.limbo.poi-rostock" ;
  dcterms:issued    "2020-02-03T17:51:54.007+01:00"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
  dcterms:license   limbo:NullLicense ;
  void:triples      196 ;
  eg:localId        "rostock_baumaerkte" ;
  owl:versionInfo   "2020-01-20" ;
  cat:distribution  [ a             cat:Distribution ;
                      void:triples  196 ;
                      <http://w3id.org/rpif/vocab#op>
                              [ a       <http://w3id.org/rpif/vocab#OpStmtList> ;
                                <http://w3id.org/rpif/vocab#queryString>
                                        ( "BASE    <file:///home/raven/Projects/Eclipse/dcat-suite-parent/dcat-suite-cli/>\n\nDELETE {\n  ?s ?p1 ?o .\n}\nINSERT {\n  ?s ?p2 ?o .\n}\nWHERE\n  { ?s  ?p1  ?o\n    FILTER strstarts(str(?p1), \"https://portal.limbo-project.org\")\n    BIND(iri(replace(str(?p1), \"https://portal.limbo-project.org\", \"https://data.limbo-project.org\")) AS ?p2)\n  }\n" "BASE    <file:///home/raven/Projects/Eclipse/dcat-suite-parent/dcat-suite-cli/>\n\nDELETE {\n  ?s1 ?p ?o .\n}\nINSERT {\n  ?s2 ?p ?o .\n}\nWHERE\n  { ?s1  ?p  ?o\n    FILTER strstarts(str(?s1), \"https://portal.limbo-project.org\")\n    BIND(iri(replace(str(?s1), \"https://portal.limbo-project.org\", \"https://data.limbo-project.org\")) AS ?s2)\n  }\n" "BASE    <file:///home/raven/Projects/Eclipse/dcat-suite-parent/dcat-suite-cli/>\n\nDELETE {\n  ?s ?p ?o1 .\n}\nINSERT {\n  ?s ?p ?o2 .\n}\nWHERE\n  { ?s  ?p  ?o1\n    FILTER ( strstarts(str(?o1), \"https://portal.limbo-project.org\") && isIRI(?o1) )\n    BIND(iri(replace(str(?o1), \"https://portal.limbo-project.org\", \"https://data.limbo-project.org\")) AS ?o2)\n  }\n" ) ;
                                <http://w3id.org/rpif/vocab#subOp>
                                        [ a       <http://w3id.org/rpif/vocab#OpDataRefResource> ;
                                          <http://w3id.org/rpif/vocab#dataRef>
                                                  [ a       <http://w3id.org/rpif/vocab#DataRefUrl> ;
                                                    <http://w3id.org/rpif/vocab#dataRefUrl>
                                                            <file:///home/raven/Projects/limbo/git/poi-rostock/rostock_baumaerkte-2020-01-20.ttl>
                                                  ]
                                        ]
                              ] ;
                      eg:localId    "rostock_baumaerkte" ;
                      eg:relPath    "rostock_baumaerkte-2020-01-20.ttl"
                    ]
] .

```

Materialization can be done with the -m flag; this executes the workflow and writes the result into a new file.

```bash
dcat transform --materialize intermediate.dcat.ttl > final.dcat.ttl
```

```turtle
[ a                 cat:Dataset ;
  dataid:artifact   "rostock_baumaerkte" ;
  dataid:group      "org.limbo.poi-rostock" ;
  dcterms:issued    "2020-02-03T17:51:54.007+01:00"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
  dcterms:license   limbo:NullLicense ;
  void:triples      196 ;
  eg:localId        "rostock_baumaerkte" ;
  owl:versionInfo   "2020-01-20" ;
  cat:distribution  [ a                cat:Distribution ;
                      void:triples     196 ;
                      eg:localId       "rostock_baumaerkte" ;
                      eg:relPath       "rostock_baumaerkte-2020-01-20.ttl" ;
                      cat:downloadURL  <file:///home/.../target/file-2203727464623458073.dat>
                    ]
] .

```


Note: As `--materialize` (shortcut `-m`) is a flag to the transformation, both steps can be combined:

```bash
dcat transform -D 'SOURCE_NS=https://portal.limbo-project.org' -D 'TARGET_NS=https://data.limbo-project.org' -m --transform replacens.sparql /home/raven/Projects/limbo/git/poi-rostock/target/effective.dcat.ttl > final.dcat.ttl
```


