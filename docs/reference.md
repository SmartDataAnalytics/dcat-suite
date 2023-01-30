# dcat add

`dcat add` is used to create different types of dcat entities based on the provided arguments and dcat repository properties.

The command can generate DCAT dataset and distribution descriptions for a given set of files.


`dcat add files`


Cardinalities:
* A file can have multiple content identifiers.
* A content identifier may be referenced by multiple datasets.


A `datribution` is a portmanteau from dataset and distribution. It refers to a specific pair of dataset and distribution.


## Selectors
Selectors are means to specify a set of maven coordinates.
* A file's base name can serve as an artifact id.
* A file's last modified date can serve as the version
* -g -a -v -t -c allow for specifying the maven GAVTC components.
* -p (for pattern) can be used to specify a GAVTC pattern. For example, `org.example.mygroup:::nt.bz2` would match all artifacts in all versions in the
specified group of type nt.bz2. An empty component thus acts as a placeholder.
* --dataset / --content limits matches to either type.

## Mapping files to content identifiers without linking them to datasets

The `--content` option modifies the behavior that only the content-related aspects apply. Conversely, dataset aspects are left out.
The following maps files to content identifiers.

```bash
dcat add --content file
```

## Added datasets without linking to content files

The `--dataset` option restricts the add operation to only the dataset aspect.

```bash
dcat add --dataset file
```

The file argument is only used to infer an artifact id and version for the dataset.



## Adding content to datasets

```
dcat link [dataset selector] [distribution selector]
```



## Show local repository status

dcat status


```
entity       | type    | deployed version | local version  | local content modified | conflict
file1.nt.bz  | file    |            1.0.0 |        1.0.0   | yes                    | yes
urn:mvn:foo  | dataset |            1.1.0 | 1.2.0-SNAPSHOT | yes                    | no
urn:mn:foo   | dataset |            2.0.0 | 2.1.0-SNAPSHOT | no                     | no

```

Conflicts arise if a local non-snapshot version differs in content from the deployed version.
The typical resolution is to change the version of the local artifact.


## Versioning of datasets and content

```bash
dcat version --set 1.0.0-SNAPSHOT [selector]
```



## One-shot vs Mapped content
In one-shot mode, local files merely exist as containers for content that is staged for upload and which can be deleted afterwards.
In mapped mode, file locations should be retained such that the files can be re-created in predefined locations from the deployed versions - similar to e.g. git lfs.

Technically, the modes affect the dcat:downloadURL attribute of content URNs.


## Moving files
If a mapping between content and file location should be retained and it turns out that a file is in the wrong directory then
a `dcat mv` operation updates _both_ repository metadata and file location.

```bash
dcat mv source-file target-file
```




