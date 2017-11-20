### Elixibilitas: Quality Metrics tool for Elixir BioTools Registry.

###### Enterprise Java 8 (JEE8) Platform
Elixibilitas project is strictly adherent to the [JEE8](https://www.jcp.org/en/jsr/detail?id=366) specification.
The tools is developed and deployed on [WildFly 10.1](http://wildfly.org/) server, 
but should run on other servers (i.e. [Apache Tomcat](http://tomcat.apache.org/)).

###### MongoDB
Quality Metrics are stored in [MongoDB](www.mongodb.com)

###### Apache Maven build system
To simplify build process Elixibilitas uses [Apache Maven](https://maven.apache.org/) build system.

Modules are logically separated by their functionality:
- openebench-tools-model - Java [JSON-B](https://www.jcp.org/en/jsr/detail?id=367) model classes for the tools.
- elixibilitas-metrics-model - Java [JSON-B](https://www.jcp.org/en/jsr/detail?id=367) model classes for quality metrics.
- elixibilitas-dao - MongoDB data access classes for "tools" and "metrics" collection management.
- elixibilitas-rest - RESTful API.

###### REST API
The REST API is based on [JAX-RS](jcp.org/en/jsr/detail?id=370) API.

The API provides an access to biological tools descriptions:
```
https://elixir.bsc.es/monitor/tool/{id}
https://elixir.bsc.es/monitor/tool/{id}/{type}
https://elixir.bsc.es/monitor/tool/{id}/{type}/{host}
https://elixir.bsc.es/monitor/tool/{id}/{type}/{host}/{path}
```
where:
- {id} is the prefixed tool id (i.e. "bio.tools:pmut")
- {type} is a type of the tool ("web", "app", "cmd", "db", "rest", "soap")
- {host} is the tool provider which is usually provider's host
- {path} is a JSON pointer to locate sub-property to return

---

> Swagger: [https://elixir.bsc.es/monitor/metrics/openapi.json](https://elixir.bsc.es/monitor/metrics/openapi.json)<br/>
> Metrics JSON Schema: [https://elixir.bsc.es/monitor/metrics/metrics.json](https://elixir.bsc.es/monitor/metrics/metrics.json)<br/><br/>
> Note that {id}/{type}/{host} uniquely identify the tool, while omitting the {type} or {host} returns an array of descriptions.<br/><br/>
> example 1: [https://elixir.bsc.es/monitor/tool/bio.tools:pmut/web/mmb.irbbarcelona.org](https://elixir.bsc.es/monitor/tool/bio.tools:pmut/web/mmb.irbbarcelona.org) .<br/>
> example 2: [https://elixir.bsc.es/monitor/tool/bio.tools:pmut/web/mmb.irbbarcelona.org/credits](https://elixir.bsc.es/monitor/tool/bio.tools:pmut/web/mmb.irbbarcelona.org/credits) .<br/>
> curl patch tool data example: 
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://elixir.bsc.es/tool/{id}/description -d '"new description."'
```

Quality Metrics accessed via:
```
https://elixir.bsc.es/metrics/
https://elixir.bsc.es/metrics/{id}/{type}/{host}/{path}
```
> example1: [https://elixir.bsc.es/monitor/metrics/bio.tools:pmut/web/mmb.irbbarcelona.org](https://elixir.bsc.es/monitor/metrics/bio.tools:pmut/web/mmb.irbbarcelona.org) .<br/>
> example2: [https://elixir.bsc.es/monitor/metrics/bio.tools:pmut/web/mmb.irbbarcelona.org/project/website](https://elixir.bsc.es/monitor/metrics/bio.tools:pmut/web/mmb.irbbarcelona.org/project/website) .<br/>
> curl patch metrics data example: 
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://elixir.bsc.es/monitor/metrics/{id}/support/email -d 'true'
```
or, what is the same:
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://elixir.bsc.es/monitor/metrics/{id} -d '{"support": {"email": true}}'
```

The API also provides EDAM descriptions for the tool:
```
https://elixir.bsc.es/monitor/edam/tool/
https://elixir.bsc.es/monitor/edam/tool/{id}/{type}/{host}
```
> example: [https://elixir.bsc.es/monitor/edam/tool/bio.tools:pmut/web/mmb.irbbarcelona.org](https://elixir.bsc.es/monitor/edam/tool/bio.tools:pmut/web/mmb.irbbarcelona.org) .

or descriptions of the EDAM term itself:
```
https://elixir.bsc.es/monitor/edam/description?term={edam id}
```
> example: [https://elixir.bsc.es/monitor/edam/description?term=http://edamontology.org/format_3607](https://elixir.bsc.es/monitor/edam/description?term=http://edamontology.org/format_3607) .

There is also full text search over EDAM ontology.
```
https://elixir.bsc.es/monitor/edam/search?text={text to search}
```
> example: [https://elixir.bsc.es/monitor/edam/search?text=alignment](https://elixir.bsc.es/monitor/edam/search?text=alignment) .

There are simple stat info that can be obtained from the server:

>[https://elixir.bsc.es/monitor/rest/statistics](https://elixir.bsc.es/monitor/rest/statistics) : basic statistics.<br/>
>[https://elixir.bsc.es/monitor/rest/statistics/total](https://elixir.bsc.es/monitor/rest/statistics/total) : total number of tools.<br/>
>[https://elixir.bsc.es/monitor/rest/statistics/operational](https://elixir.bsc.es/monitor/rest/statistics/operational) : number of tools those homepage is accessible.

```
https://elixir.bsc.es/monitor/rest/statistics/{tool_type} : number of tools of particular type ("web", "cmd", etc.)
```
> example: [https://elixir.bsc.es/monitor/rest/statistics/cmd](https://elixir.bsc.es/monitor/rest/statistics/cmd) .

All changes are stored in a log collection and could be accessed:

```
https://elixir.bsc.es/monitor/tools/log/{id}/{type}/{host}/{path}
https://elixir.bsc.es/monitor/metrics/log/{id}/{type}/{host}/{path}
```
> example: [https://elixir.bsc.es/monitor/metrics/log/bio.tools:pmut/cmd/mmb.irbbarcelona.org/project/website/operational](https://elixir.bsc.es/monitor/metrics/log/bio.tools:pmut/cmd/mmb.irbbarcelona.org/project/website/operational) .
