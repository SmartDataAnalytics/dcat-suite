
## Configuration
In general, DCAT sources can be configured in `$HOME/.dcat/settings.ttl`.
The example below shows configuration of one DCAT resolver based on a file/URL and one based on a SPARQL endpoint.

**Note: Namespaces and some class names are work in progress.**

```
@prefix eg: <http://www.example.org/> .
@prefix rpif: <http://w3id.org/rpif/vocab#> .

[]
  eg:resolvers (
    [
      a eg:DcatResolver ;
      eg:dataRef [
        a rpif:DataRefUrl ;
        rpif:dataRefUrl <file:///home/user/limbo-project.org/metadata-catalog/catalog.all.ttl>
      ]
    ]
    [
      a eg:DcatResolver ;
      eg:dataRef [
        a rpif:DataRefSparqlEndpoint ;
         rpif:serviceUrl <https://databus.dbpedia.org/repo/sparql>
      ] ;
      eg:views (
        [ a rpif:ResourceSpecUrl ; rpif:resourceUrl "dataid-to-dcat-inferences.sparql" ]
      )
    ]
  ) ;
  .
```

*Resolvers* are specifications of RDF data sources that provide access to the catalog data.
The configuration makes use of the [Conjure system](https://github.com/SmartDataAnalytics/jena-sparql-api/tree/develop/jena-sparql-api-conjure), which enables the specification of how to "conjure"/obtain RDF datasets from other RDF datasets.
In essence, Conjure provide facilities to express references to (RDF) data as well as operations on it in RDF.
Because a DCAT catalog *IS* an RDF dataset, we can directly this system and its vocabulary.
Because RDF data hardly conforms to standard data models in practice, the dcat client ships with support of specifying a virtual RDF DCAT catalog graph in terms of SPARQL construct queries, as demonstrated in the provisioning of views for the DBpedia Databus SPARQL endpoint.

The RDF processing interchange format (RPIF) is an attempt at devising both a vocabular and a reference implementation to capture references to RDF data in various ways as well as virtual and materialized transformations, most prominatly based on SPARQL.
`rpif:ResourceSpecUrl` just denotes a transclusion of the content of the referenced file or URL (which may be on the Java classpath) as a string literal.


For programmatic access to the config, we provide a Java/RDF binding, as shown in the following snipped.
The configuration can be readily reused in third party code by including the dcat-suite-core maven dependency.

```java
CatalogResolver cr = CatalogResolverUtils.createCatalogResolverDefault();
```

```java
Model configModel = RDFDataMgr.loadModel("path/to/settings.ttl);

// Perform transclusion
ResourceSpecUtils.resolve(configModel);

List<DcatResolverConfig> configs = configModel
    .listResourcesWithProperty(ResourceFactory.createProperty("http://www.example.org/resolvers"))
    .mapWith(r -> r.as(DcatResolverConfig.class))
    .toList();
```
## View Definitions

Views are defined simply as a sequence of SPARQL queries. The code/mechanism for processing is the same as used in [sparql-integrate](https://github.com/SmartDataAnalytics/Sparqlintegrate).

* [DATAID-to-DCAT](../dcat-suite-binding-ckan/src/main/resources/dataid-to-dcat-inferences.sparql)


## Searching Catalogs using the CLI client
The basic search command are:

* `dcat search <pattern>` (human readable output)
* `dcat search --jq <pattern>` (json)

The output format of the first command is:
```
cat:Dataset, void:Linkset: org.limbo:train_2-TO-wikidata:1.0.1
  latest
  url: https://metadata.limbo-project.org/dataset-org.limbo-train_2-TO-wikidata-1.0.1

cat:Dataset: org.limbo:train_2:0.10
  latest
  url: https://metadata.limbo-project.org/dataset-org.limbo-train_2-0.10
```

The second version provides all information as a JSON array, such as for subsequent processing by scripts:

```json
[
  {
    "id": "https://metadata.limbo-project.org/dataset-org.limbo-train_2-0.10",
    "id_type": "uri",
    "identifier": [
      "org.limbo-train_2-0.10"
    ],
    "issued": [
      "2019-11-14T08:45:41.780+00:00"
    ]
    /* ... */
   }
]
```

The search command actually maps to a SPARQL query template and can in principle be exchanged with custom semantics.
* [match-by-regex.sparql](../dcat-suite-binding-ckan/src/main/resources/match-by-regex.sparql) - Template for matching items based on the search pattern. The resource IRIs and - if available - dct:identifiers will be used as the primary means of identification.
* [match-exact.sparql](../dcat-suite-binding-ckan/src/main/resources/match-exact.sparql) - Match items by the exact given string - namely resource IRI and dct:identifier.

Note, that if items actually have identifiers but use a vocabulary term different from dct:identifier, a view can be provided to align the models from the vantage point of the dcat client.


**TODO Make configurable**


## Future Work
* Leverage GIT: If DCAT catalogs are distributed via a version control system, such as GIT, we could use of the GIT facilities to sync local copies.




