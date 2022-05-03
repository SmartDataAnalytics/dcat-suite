---
title: Dataset Dependencies
nav_order: 10
---

## Dataset Dependencies

Datasets in maven can be published either as conventional jar archives or as specially typed artifacts such as csv.bz2 or ttl.gz.

If a dataset is packaged as a conventional jar file, then one can also use a conventional dependency declaration in order to place
the contained file(s) on the classpath.

Otherwise, the `maven-dependency-plugin:copy` goal can be used to place a set of typed artifact into their right place.
Typically, one wants to place datasets from typed artifacts in the same location as if they had been placed under `/src/main/resources`.
This is accomplished by configuring `maven-dependency-plugin` to `copy` dependencies to the output directory `${project.build.outputDirectory}`.
Setting `stripVersion=true` produces a file whose name is independent from the dependency version and thus makes it easy to reference it from the source code.
A complete example is shown below:


```xml
<?xml version="1.0" encoding="UTF-8"?>
<project
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"
  xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.github.myaccount</groupId>
  <artifactId>ml-project</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>jar</packaging>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.1.1</version>
        <executions>
          <execution>
            <phase>install</phase>
            <goals>
              <goal>copy</goal>
            </goals>
            <!-- For a reference of all configuration options for the 'copy' goal 
              refer to: https://maven.apache.org/plugins/maven-dependency-plugin/copy-mojo.html -->
            <configuration>
              <!--
              Setting 'stripVersion=true' results in the file
                target/classes/resilience.bz2 
              whereas 'stripVersion=false' results in the file
                target/classes/resilience-2022-05-02.1-SNAPSHOT.bz2
              The former file name is easier to reference from code
              -->
              <stripVersion>true</stripVersion>
              <artifactItems>
                <artifactItem>
                  <groupId>org.example.ml.models</groupId>
                  <artifactId>resilience</artifactId>
                  <version>2022-05-02.1-SNAPSHOT</version>
                  <type>bz2</type>
                  <!-- The setting of the output directory resolves to 'target/classes' 
                    which is the same place where files under src/main/resources go -->
                  <outputDirectory>${project.build.outputDirectory}</outputDirectory>
                </artifactItem>
              </artifactItems>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>

</project>
```

