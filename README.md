### Elixibilitas: Quality Metrics tool for Elixir BioTools Registry.

###### Enterprise Java 8 (JEE8) Platform
Elixibilitas project is strictly adherent to the JEE8 specification.
The tools is developed and deployed on [WildFly 10.1](http://wildfly.org/) server, 
but should run on other servers (i.e. [Apache Tomcat](http://tomcat.apache.org/)).

###### MongoDB
Quality Metrics are stored in [MongoDB](www.mongodb.com)

###### Apache Maven build system
To simplify build process Elixibilitas uses [Apache Maven](https://maven.apache.org/) build system.

Modules are logically separated by their functionality:
- openebench-tools-model - Java [JSON-B](https://www.jcp.org/en/jsr/detail?id=367) model classes for the tools.
- elixibilitas-metrics-model - Java [JSON-B](https://www.jcp.org/en/jsr/detail?id=367) model classes for quality metrics.
- elixibilitas-tools-dao - MongoDB data access classes for "tools" collection management.
- elixibilitas-metrics-dao - MongoDB data access classes for "metrics" collection management.
- openebench-tools-rest - RESTful API to access tools descriptions. 
- elixibilitas-metrics-rest - RESTful API to access quality metrics. 
- openebench-edam-rest - RESTful API to access [EDAM](http://edamontology.org) ontology descriptions.



###### REST API
The REST API is based on [JAX-RS](jcp.org/en/jsr/detail?id=370) API.

The API provides an access to biological tools descriptions:
```
https://elixir.bsc.es/tool[?{projection}]
https://elixir.bsc.es/tool/{id}
https://elixir.bsc.es/tool/{id}/{type}
https://elixir.bsc.es/tool/{id}/{type}/{host}
https://elixir.bsc.es/tool/{id}/{type}/{host}/{path}
```
where:
- {id} is the prefixed tool id (i.e. "bio.tools:pmut")
- {type} is a type of the tool ("web", "app", "cmd", "db", "rest", "soap")
- {host} is the tool provider which is usually provider's host
- {path} is a JSON pointer to locate sub-property to return

> Note that {id}/{type}/{host} uniquely identify the tool, while omitting the {type} or {host} returns an array of descriptions.
> example 1: https://elixir.bsc.es/tool?projection=publications](https://elixir.bsc.es/tool?projection=publications).

> example 2: [https://elixir.bsc.es/tool/bio.tools:pmut/web/mmb.irbbarcelona.org](https://elixir.bsc.es/tool/bio.tools:pmut/web/mmb.irbbarcelona.org).

> example 3: [https://elixir.bsc.es/tool/bio.tools:pmut/web/mmb.irbbarcelona.org/credits](https://elixir.bsc.es/tool/bio.tools:pmut/web/mmb.irbbarcelona.org/credits).

Quality Metrics accessed via:
```
https://elixir.bsc.es/metrics/{id}/{type}/{host}/{path}
```
> example1: [https://elixir.bsc.es/metrics/bio.tools:pmut/web/mmb.irbbarcelona.org](https://elixir.bsc.es/metrics/bio.tools:pmut/web/mmb.irbbarcelona.org).

> example2: [https://elixir.bsc.es/metrics/bio.tools:pmut/web/mmb.irbbarcelona.org/project/website](https://elixir.bsc.es/metrics/bio.tools:pmut/web/mmb.irbbarcelona.org/project/website).


The API also provides EDAM descriptions for the tool:
```
https://elixir.bsc.es/edam/tool/
https://elixir.bsc.es/edam/tool/{id}/{type}/{host}
```
> example: [https://elixir.bsc.es/edam/tool/bio.tools:pmut/web/mmb.irbbarcelona.org](https://elixir.bsc.es/edam/tool/bio.tools:pmut/web/mmb.irbbarcelona.org).

or descriptions of the EDAM term itself:
```
https://elixir.bsc.es/edam/description?term={edam id}
```
> example: [https://elixir.bsc.es/edam/description?term=http://edamontology.org/format_3607](https://elixir.bsc.es/edam/description?term=http://edamontology.org/format_3607).

There is also full text search over EDAM ontology.
```
https://elixir.bsc.es/edam/search?text={text to search}
```
> example: [https://elixir.bsc.es/edam/search?text=alignment](https://elixir.bsc.es/edam/search?text=alignment).
