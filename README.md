# DCAT - Easy RDF Data Management

Retrieving, publishing and loading data in a single DCAT-centric tool.
One could say, DCAT is to datasets what pom.xml is to (Java) software projects.

## In a nutshell

*Question:* _How many commands does it take to load the 50+ files of [this ckan record](http://ckan.qrowd.aksw.org/dataset/org-linkedgeodata-osm-bremen-2018-04-04) into a virtuoso triple store (with default port and credentials)?_

*Answer:* _2_

```bash
dcat import ckan --host=http://ckan.qrowd.aksw.org --dataset=org-linkedgeodata-osm-bremen-2018-04-04 > /tmp/dcat.nt
dcat deploy virtuoso --allowed=/writeable/dir/readable/by/virtuoso /tmp/dcat.nt
```


*Question:* _And how do I create a [graph group](http://docs.openlinksw.com/virtuoso/rdfgraphsecuritygroups/) so I can view all these files as a single graph?_

*Answer:* _It already happened_


*Question:* _So I have this DCAT file with dcat:downloadURL pointing to local files. How can I publish it to CKAN?_

*Answer:* _Like this:_
```bash
dcat deploy ckan --host=http://ckan.example.org --apikey=my-ckan-api-key dcat.nt
```


## Feature overview

| API                        | DCAT retrieval | Deploy RDF | Deploy non RDF |
|----------------------------|----------------|------------|----------------|
| CKAN                       |      X         |       X    |     x          |
| Virtuoso RDF Bulk Loader   |      .         |       X    |    n/a         |
| Generic SPARQL             |      .         |       .    |                |
| URL to DCAT resource       |      X         |      n/a   |    n/a         |


. = future work

### DCAT Example
Here is a short example of a DCAT dataset description in order to give you an impression of what we are talking about.

```turtle
@prefix eg: <http://example.org/> .
@prefix dcat: <http://www.w3.org/ns/dcat#> .
@prefix dct: <http://purl.org/dc/terms/> .

eg:myDataset
    a dcat:Dataset ;
    dct:identifier "my-dataset" ;
    dct:title "My Dataset" ;
    dct:description "Really useful dataset" ;
    dcat:distribution eg:myFirstDistribution-of-myDataset ;
    .

eg:myFirstDistribution-of-myDataset
    a dcat:Distribution ;
    dct:title "My Distribution" ;
    dct:description "Download of my distribution" ;
    dcat:accessURL <a/relative/path/or/a/url/of/a/web/resource/or/a/named/graph> ;
    .

```

### CLI Quick Usage

* Show help
```
dcat --help
```

* Show all DCAT related information from an RDF URI or filename

```bash
dcat show my-dcat.nt
```

* Deploy datasets based on a DCAT description to CKAN

```bash
dcat deploy ckan --apikey=yourApiKey --host=yourCkanUrl my-dcat.nt
```

This will create a copy of the input DCAT file under `target/ckan/deploy-dcat.nt` file with the `dcat:accessURL` replaced by the CKAN resources. If you host this file anywhere on the Web, it will give you working download links - neat!

* Deploy a self-describing dataset (see below) to CKAN

```bash
dcat deploy ckan --apikey=yourApiKey --host=yourCkanUrl mySelfDescribingDataset.nq
```

* Expand the graphs of a self-describing dataset to individual files based on its contained DCAT description
```bash
dcat expand mySelfDescribingDataset.nq

# Now you can also deploy the expanded form:
cd target/dcat/mySelfDescribingDataset
dcat deploy ckan dcat.nt --host=yourCkanUrl --apikey=yourSecretKey
```


## Building
```bash
mvn clean install
```

## Installing the Debian package (requires root)

*After the build* run

```bash
./reinstall-debs.sh
```


## What is a self-describing dataset (SDD)?
A SDD is simply a quad-based dataset that contains DCAT dataset and distribution information in its _default graph_.

## Extracting datasets from an SDD
The `dcat:accessURL` attribute of distributions is thereby intepreted as follows:
* If at least one of the given accessURLs matches the IRI of a graph within the SDD, `ckan-deploy` will deploy a an RDF file to CKAN that is the union of all graphs denoted by accessURLs. An error will raised if any other accessURL points to a non-existent graph.
* If there is at most one accessURL, a CKAN resource will be created, with the URL attribute set if present.
* An error is raised otherwise

## How to create self-describing datasets?
You can use your favourite RDF tool.

Shamless self-advertisement: [Sparql Integrate](https://github.com/SmartDataAnalytics/SparqlIntegrate) is a tool that enables expressing data integration workflows as a sequence of SPARQL queries that make use of function extensions for XML, CSV and JSON processing. Hence, it makes it fairly easy to  create quad based datasets. You only need to design your workflow such that it outputs appropriate DCAT descriptions.


## Example
This example assumes that the debian packages of `ckan-deploy` and [`sparql-integrate`](https://github.com/SmartDataAnalytics/SparqlIntegrate) are installed.


```
cd /tmp

git clone https://github.com/QROWD/QROWD-RDF-Data-Integration.git qrowd-rdf-data-integration
cd qrowd-rdf-data-integration/datasets/1046-1051

sparql-integrate workloads.sparql process.sparql emit.sparql > dataset.nq
dcat deploy ckan --host=yourCkanInstance --apikey=yourApiKey dataset.nq
```

The dataset entry on our CKAN: http://ckan.qrowd.aksw.org/dataset/trento-railway-time-tables

For explanations about the transformations using the `*.sparql` files, please refer to [this page](https://github.com/QROWD/QROWD-RDF-Data-Integration/tree/master/datasets/1046-1051).


## CLI Roadmap

These commands are not yet implemented, but appear to be useful. These descriptions are not final.

* Generate a meta dcat file that treats another dcat file as a dataset. The meta file can be used to deploy the described file.
```
dcat meta my-datasests.dcat.nt > meta.dcat.nt
```

* Upload rdf file via SPARQL Update
```
dcat deploy sparql --user=dba --pass=dba --host=http://example.org/sparql dcat.nt
```



## TODOs

* Add support for user agent field on upload
* Possibly add support for profiles that bundle commonly needed information, such as apikey and user agent


