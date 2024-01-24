PREFIX dcat: <http://www.w3.org/ns/dcat#>
PREFIX dcterms: <http://purl.org/dc/terms/>

INSERT {
  GRAPH ?g { ?s dcterms:issued ?timestamp }
}
WHERE {
  GRAPH ?g { ?s a dcat:Dataset }
  FILTER(NOT EXISTS { ?s dcterms:issued []})
  BIND(NOW() AS ?timestamp)
}

