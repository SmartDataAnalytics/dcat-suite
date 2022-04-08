## DCAT Client Documentation

Work in progress
Main work at present is to find a nice specification for the view rewriting, so that even non-dcat catalogs or such with errorneous modeling
may be made to work with the dcat client system.


* Serving data
The easiest way to serve data is using
`dcat serve <id>`

This will download the data for the given id, and invoke the default 'serve' recipe, which is using In-Memory Fuseki.
The advantage is, that this creates no temporary files, however it is limited by RAM and the performance of Jena.


`dcat serve --recipe docker-virtuoso <id>`
This starts a new docker container wih a virtuoso and deploys the data there. If a browser is available, it is opened.





* Retrieve data

`dcat get $id`: First, if not already done, install the artifact with the given $id into the local repository. Afterwards, print the content or link to the content on the console.

* Register a catalog resolver

* Load a catalog into another one
DCAT files can be placed into the directory-based catalog.





The DCAT Client combines several advanced concepts and technologies to enable seamless interactions with dcat catalogs.

* Unified interaction with catalogs purely with SPARQL. In case a catalog resides in an RDF file, it is internally loaded into the Jena ARQ triple store, and then internally accessed with SPARQL - so there is no distinction between local and remote access.
    * Advanced query optimizer / transformer to work around issues of frequently used legacy versions of virtuoso (noting it because it was alot of work to make it working)
* Client-code uses the Facete faceted search API to generate the queries
* Query answering over views system: Data quality issues are not necessarily a show stopper. The query rewriting system allows for working around them.
* File-based HTTP Caching System: Datasets are quite similar to HTTP resources: A dataset is referred to by an identifier, however it may have representations in different formats. Choosing the right one is subject to content negotiation.
* An algebra-based approach to content type conversion and content encoding allows for tracking and caching of intermediate results.
* Exploitation of known relations between abstract and concrete syntaxes: If a request for a concrete syntax C1 of the abstract syntax A is made, however only a concrete representation C2 is available, it will be seamlessly converted. In short: one can e.g. request HDT from any other RDF format.
* Automatic binding of operations to the fastest available implementation, which makes use of system tools, such as lbzip2, if they are available with fallback to Apache commons compress.
* Seamless HDT support via content-type application/x-hdt with file extension .hdt
* Generic json mapping of resources for use in shellscripts in conjunction with jq



## Configuration of catalog connections
A DCAT catalog is itself just an RDF dataset. Hence, configuration works in the same way as referring to any other RDF dataset.
The RDF processing interchange format enables the specification of the construction of RDF datasets.


Note: RPIF is actually imprecise - it is a TSSL - triple store specification language - the result of tssl is a set up connection to a sparql endpoint which provides access to the data according to specification.
 triple store with the appropriate data or KBSL (knowledge base specification language)...
So the aspects we are dealing with are:
* data transformations in terms of sparql queries, e.g. run a construct query on a source dataset
* data access / query rewriting (or more generally: sparql statement rewriting)
* triple store service (which triple store to use, virtuoso, stardog, etc)


So what exactly does construct queries and insert queries do?
construct yields a stream of triples / quads, usually loaded into an in-memory store (jena-model)
insert modifies the content of a datapod (the dataset), and thus invalidates any current distribution relation to its datasets, and possibly

The main issue is, that we need to abstract descriptions of access to any resource - that can be passed to a resource resolver (as in the spring framework).

ResourceResolver.resolve(rdfResource, expectedJavaClass)

queryOverView
  subOp
  views ( [a StringResource ; value 'somestring' ] [a UrlResource : value 'filename']]) )







The usual procedue is:
* pick an url from the download section of a website
* download the data
* transform it to a suitable format for the triple store
* choose a target graph (or even target topology; graph groups)
* start the db service if not running, create if not exists
* (bulk) load the data
* run opertions
* shut down the service





TODO Rewriting a dataset over views should probably be part of rpif, but how to specify it? A lazy way would be to add a
flag on the operator, but it is cleaner if each operation (with its distinct semantics) maps to its own java class - so no flags!
In relational algebra, there is also no flag that turns a join into e.g. a left join - but its a separate operation.

Right, the view transformation is an operation on an rdf datapod that wraps its getConnection method with the view rewriting,
So the specification looks like:

```
  [] a rpif:SparqlRewrite
    subOp
    views (..)
```

Now the question is: How to reference the views? They may be rdf documents nicely describing the views - but in practice a .sparql file is sufficient.










~/.dcat/settings.d/my-repo.ttl
```
@prefix eg: <http://www.example.org/> .
@prefix rpif: <http://w3id.org/rpif/vocab#> .

[]
  eg:resolvers (
    [
      a rpif:DataRefUrl ;
      rpif:dataRefUrl <file:///home/raven/Projects/limbo/git/metadata-catalog/catalog.all.ttl>
    ]
    [
      a rpif:DataRefSparqlEndpoint ;
      rpif:serviceUrl <https://databus.dbpedia.org/repo/sparql> ;
      
    ]
  )

```


