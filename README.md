# ckan-deploy
Deploy datasets to CKAN using DCAT descriptions

## Quick Usage
```bash
ckan-deploy --apikey=yourApiKey --host=yourCkanUrl mySelfDescribingDataset.nq
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



## TODOs

* Make it possible to use extraction and deploy as separate phases. This step would take as input an SDD, and yield an RDF file with the dcat descriptions, together with relative urls to the written out dataset files. Currently, the file extraction takes place with temporary files that are deleted after deployment.


