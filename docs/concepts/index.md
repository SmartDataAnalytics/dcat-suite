# Concepts

## What is a Dataset

There is no single agreed upon definition, but my preferred one is:

* A dataset is an instance of a datamodel.

Generally, datamodels are not fully specified. For example, while CSV is a synatx for representing contents of spreed sheets, several external paramaters have been provided to parse CSV into a relational data model. Examples for external parameters are the number of lines to skip, whether the first row is a header row, and what are quote characters, cell separators and line delimiters. 
And even if all the parameters are provided, it is still not clear when two tables represent the same information - for example, does row order matter?

This is where RDF shines: The RDF datamodel defines equivalence of RDF graphs in terms of graph isomorphism. Because RDF is about making implicit information explicit, it increases the potential for automatization in data processing pipelines.

## How do Datasets differ from Source Code

The main differences are that datasets may be (a) in non-line based formats and/or (b) very large which makes VCS systems unsuitable for tracking changess.

Besides the differences there are many commonalities:
A source code project is a specification for how to automatically generate (and deploy) a set of target artifacts - and such target artifacts may be datasets.


## Static vs Streaming Data


