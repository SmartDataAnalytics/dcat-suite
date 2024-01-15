<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>org.aksw.dcat</groupId>
	<artifactId>init</artifactId>
	<version>0.0.1-SNAPSHOT</version>

	<parent><groupId>org.aksw.data.config</groupId><artifactId>aksw-data-deployment</artifactId><version>0.0.8</version><relativePath></relativePath></parent>

	<packaging>pom</packaging>

	<properties>
		<sparql-maven-plugin.version>0.0.1-SNAPSHOT</sparql-maven-plugin.version>
	</properties>

<!--
	<dependencies>
		<dependency>
			<groupId>org.aksw.maven.plugins</groupId>
			<artifactId>sparql-maven-plugin</artifactId>
			<version>${sparql-maven-plugin.version}</version>
		</dependency>
	</dependencies>
-->

	<build>
		<plugins>
				<plugin>
				<groupId>org.aksw.maven.plugins</groupId>
				<artifactId>sparql-maven-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<executions>
					<execution>
						<id>generate-metadata</id>
						<phase>process-resources</phase>
						<goals>
							<goal>run</goal>
						</goals>
						<configuration>
							<args>
								<arg>CONSTRUCT WHERE { ?s ?p ?o }</arg>
							</args>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
			


	</build>


</project>
