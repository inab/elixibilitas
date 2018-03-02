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

> Open API 3.0 (aka Swagger): [https://openebench.bsc.es/monitor/openapi.json](https://openebench.bsc.es/monitor/openapi.json)<br/>
> Tool JSON Schema: [https://openebench.bsc.es/monitor/tool/tool.json](https://openebench.bsc.es/monitor/tool/tool.json)<br/>
> Metrics JSON Schema: [https://openebench.bsc.es/monitor/metrics/metrics.json](https://openebench.bsc.es/monitor/metrics/metrics.json)
<br/><br/>

The API provides an access to biological tools descriptions:
```
https://openebench.bsc.es/monitor/tool/{id}
https://openebench.bsc.es/monitor/tool/{id}/{type}
https://openebench.bsc.es/monitor/tool/{id}/{type}/{host}
https://openebench.bsc.es/monitor/tool/{id}/{type}/{host}/{path}
```
where:
- {id} is the prefixed tool id (i.e. "bio.tools:pmut")
- {type} is a type of the tool ("web", "app", "cmd", "db", "rest", "soap")
- {host} is the tool provider which is usually provider's host
- {path} is a JSON pointer to locate sub-property to return

---

> Note that {id}/{type}/{host} uniquely identify the tool, while omitting the {type} or {host} returns an array of descriptions.<br/><br/>
> example 1: [https://openebench.bsc.es/monitor/tool/bio.tools:pmut:2017/web/mmb.irbbarcelona.org](https://openebench.bsc.es/monitor/tool/bio.tools:pmut:2017/web/mmb.irbbarcelona.org) .<br/>
> example 2: [https://openebench.bsc.es/monitor/tool/bio.tools:pmut:2017/web/mmb.irbbarcelona.org/credits](https://openebench.bsc.es/monitor/tool/bio.tools:pmut:2017/web/mmb.irbbarcelona.org/credits) .<br/>
> curl patch tool data example: 
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://openebench.bsc.es/monitor/tool/{id}/description -d '"new description."'
```

Quality Metrics accessed via:
```
https://openebench.bsc.es/monitor/metrics/
https://openebench.bsc.es/monitor/metrics/{id}/{type}/{host}/{path}
```
> example1: [https://openebench.bsc.es/monitor/metrics/bio.tools:pmut:2017/web/mmb.irbbarcelona.org](https://openebench.bsc.es/monitor/metrics/bio.tools:pmut:2017/web/mmb.irbbarcelona.org) .<br/>
> example2: [https://openebench.bsc.es/monitor/metrics/bio.tools:pmut:2017/web/mmb.irbbarcelona.org/project/website](https://openebench.bsc.es/monitor/metrics/bio.tools:pmut:2017/web/mmb.irbbarcelona.org/project/website) .<br/>
> curl patch metrics data example: 
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://openebench.bsc.es/monitor/metrics/{id}/support/email -d 'true'
```
or, what is the same:
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://openebench.bsc.es/monitor/metrics/{id} -d '{"support": {"email": true}}'
```
---
It is possible to query tools:
```
https://openebench.bsc.es/monitor/rest/search?id={id}&{skip}&{limit}&{projection}&{text}&{name}&{description}
```
where:
- {id} is the compound tool id (i.e. "pmut", "bio.tools:pmut", ":pmut:2017")
- {skip} skip 'n' tools
- {limit} return 'n' tools
- {projection} tools properties to return
- {text} text to search
> example 1: [https://openebench.bsc.es/monitor/rest/search?id=pmut](https://openebench.bsc.es/monitor/rest/search?id=pmut) .<br/>

---

The API also provides EDAM descriptions for the tool:
```
https://openebench.bsc.es/monitor/rest/edam/tool/
https://openebench.bsc.es/monitor/rest/edam/tool/{id}/{type}/{host}
```
> example: [https://openebench.bsc.es/monitor/rest/edam/tool/bio.tools:pmut:2017/web/mmb.irbbarcelona.org](https://openebench.bsc.es/monitor/rest/edam/tool/bio.tools:pmut:2017/web/mmb.irbbarcelona.org) .

or descriptions of the EDAM term itself:
```
https://openebench.bsc.es/monitor/rest/edam/description?term={edam id}
```
> example: [https://openebench.bsc.es/monitor/rest/edam/description?term=http://edamontology.org/format_3607](https://openebench.bsc.es/monitor/rest/edam/description?term=http://edamontology.org/format_3607) .

There is also full text search over EDAM ontology.
```
https://elixir.bsc.es/monitor/rest/edam/search?text={text to search}
```
> example: [https://openebench.bsc.es/monitor/rest/edam/search?text=alignment](https://openebench.bsc.es/monitor/rest/edam/search?text=alignment) .

There are simple stat info that can be obtained from the server:

>[https://openebench.bsc.es/monitor/rest/statistics](https://openebench.bsc.es/monitor/rest/statistics) : basic statistics.<br/>
>[https://openebench.bsc.es/monitor/rest/statistics/total](https://openebench.bsc.es/monitor/rest/statistics/total) : total number of tools.<br/>
>[https://openebench.bsc.es/monitor/rest/statistics/operational](https://openebench.bsc.es/monitor/rest/statistics/operational) : number of tools those homepage is accessible.

```
https://openebench.bsc.es/monitor/rest/statistics/{tool_type} : number of tools of particular type ("web", "cmd", etc.)
```
> example: [https://openebench.bsc.es/monitor/rest/statistics/cmd](https://openebench.bsc.es/monitor/rest/statistics/cmd) .

All changes are stored in a log collection and could be accessed:

```
https://openebench.bsc.es/monitor/tools/log/{id}/{type}/{host}/{path}
https://openebench.bsc.es/monitor/metrics/log/{id}/{type}/{host}/{path}
```
> example: [https://openebench.bsc.es/monitor/metrics/log/bio.tools:pmut:2017/cmd/mmb.irbbarcelona.org/project/website/operational](https://openebench.bsc.es/monitor/metrics/log/bio.tools:pmut:2017/cmd/mmb.irbbarcelona.org/project/website/operational) .
