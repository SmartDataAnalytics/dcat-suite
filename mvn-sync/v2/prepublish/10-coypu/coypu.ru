# Load auxiliary data into the default graph

LOAD <datasets.ttl>

PREFIX eg: <http://www.example.org/>
PREFIX coypu: <https://metadata.coypu.org/dataset/>
PREFIX t:     <https://schema.coypu.org/metadata-template#>

DELETE { GRAPH ?g { ?s ?p ?o1 } }
INSERT { GRAPH ?g { ?s ?p ?o2 } }
# SELECT ?groupId ?artifactId ?templateId
WHERE {
  GRAPH ?g {
    ?s
      a dcat:Dataset ;
      eg:groupId ?groupId ;
      eg:artifactId ?artifactId ;
      .
  }
  FILTER(STRSTARTS(STR(?groupId), "org.coypu.data"))

  BIND(IRI(CONCAT(STR(coypu:), ?artifactId)) AS ?rawTemplateId)
  LATERAL {
      { BIND(?rawTemplateId AS ?templateId) }
    UNION
      { ?templateId t:alias ?rawTemplateId }
  }
  FILTER EXISTS { ?templateId ?x ?y }
  # BIND(afn:printf("s=%s - templateId=%s", ?s, ?templateId) AS ?dummy)
  LATERAL {
    { SELECT DISTINCT ?templateId ?p { ?templateId ?p [] } }
    FILTER(?p != t:alias)
    LATERAL {
        { GRAPH ?g { ?s ?p ?o1 } }
      UNION
        { ?templateId ?p ?o2 }
    }
  }
}

# Clear default graph again
CLEAR DEFAULT

