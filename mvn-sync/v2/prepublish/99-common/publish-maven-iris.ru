DELETE { GRAPH ?g { ?s ?p ?o } }
INSERT { GRAPH ?g { ?s ?p ?x } }
WHERE {
  GRAPH ?g { ?s ?p ?o }
  FILTER (?p IN( <http://www.w3.org/ns/dcat#downloadURL>, <http://www.example.org/urn> )&& STRSTARTS(STR(?o), 'urn:mvn:')) BIND(IRI(CONCAT('$baseUrl', mvn:toPath(STR(?o)))) AS ?x)
}

