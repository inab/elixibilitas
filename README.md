### Elixibilitas: Quality Metrics tool for Elixir BioTools Registry.

###### Enterprise Java 8 (JEE8) Platform
Elixibilitas project is strictly adherent to the [JEE8](https://www.jcp.org/en/jsr/detail?id=366) specification.
The tool is developed and deployed on [WildFly 14.1](http://wildfly.org/) server, 
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

###### OWL2 Ontology
There is an ontological view to the OpenEBench tools.

The tools ontology is located at:

> [https://openebench.bsc.es/monitor/tools.owl](https://openebench.bsc.es/monitor/tools.owl)

The tools data may be obtained in OWL2 JSON-LD format for any concrete tool record:<br/>
> [https://openebench.bsc.es/monitor/tool/biotools:pmut:2017/web/mmb.irbbarcelona.org](https://openebench.bsc.es/monitor/tool/biotools:pmut:2017/web/mmb.irbbarcelona.org)

or for the entire tools collection:<br/>
> [https://openebench.bsc.es/monitor/tool/](https://openebench.bsc.es/monitor/tool/)

The decision to return JSON or JSON-LD is taken on the HTTP "Accept" header.<br/><br/>
These URLs may be imported into [Protegé](https://protege.stanford.edu/) tool.<br/>
**NB:** Entire ontology is very big and exceeds the default Protegé memory settings.


###### REST API
The REST API is based on [JAX-RS](jcp.org/en/jsr/detail?id=370) API.

> Open API 3.0 (aka Swagger): [https://openebench.bsc.es/monitor/openapi.json](https://openebench.bsc.es/monitor/openapi.json)<br/>
> Tool JSON Schema: [https://openebench.bsc.es/monitor/tool/tool.json](https://openebench.bsc.es/monitor/tool/tool.json)<br/>
> Metrics JSON Schema: [https://openebench.bsc.es/monitor/metrics/metrics.json](https://openebench.bsc.es/monitor/metrics/metrics.json)
<br/><br/>

The API provides an access to biological tools descriptions:
```
https://openebench.bsc.es/monitor/tool/
```
> Returns all OpenEBench tools.<br/>
> The pagination is implemented via the HTTP Range Header (i.g. "Range: tools=10-30").<br/>
> The response always contains the HTTP Content-Range Header ("Content-Range: tools 10-30/20000").
```
https://openebench.bsc.es/monitor/tool/{id}
https://openebench.bsc.es/monitor/tool/{id}/{type}
https://openebench.bsc.es/monitor/tool/{id}/{type}/{host}
https://openebench.bsc.es/monitor/tool/{id}/{type}/{host}/{path}
```
where:
- {id} is the prefixed tool id (i.e. "biotools:pmut")
- {type} is a type of the tool ("web", "app", "cmd", "db", "rest", "soap")
- {host} is the tool provider which is usually provider's host
- {path} is a JSON pointer to locate sub-property to return

---

> Note that {id}/{type}/{host} uniquely identify the tool, while omitting the {type} or {host} returns an array of descriptions.<br/><br/>
> example 1: [https://openebench.bsc.es/monitor/tool/biotools:pmut:2017/web/mmb.irbbarcelona.org](https://openebench.bsc.es/monitor/tool/biotools:pmut:2017/web/mmb.irbbarcelona.org) .<br/>
> example 2: [https://openebench.bsc.es/monitor/tool/biotools:pmut:2017/web/mmb.irbbarcelona.org/credits](https://openebench.bsc.es/monitor/tool/biotools:pmut:2017/web/mmb.irbbarcelona.org/credits) .<br/>

curl patch tool data example: 
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://openebench.bsc.es/monitor/tool/{id}/description -d '"new description."'
```

---
It is also possible to get the list of all tools identifiers providing "Accept: text/uri-list" HTTP header:
```
curl -v -H 'Accept: text/uri-list' https://openebench.bsc.es/monitor/tool
```

Quality Metrics accessed via:
```
https://openebench.bsc.es/monitor/metrics/
https://openebench.bsc.es/monitor/metrics/{id}/{type}/{host}/{path}
```
> example1: [https://openebench.bsc.es/monitor/metrics/biotools:pmut:2017/web/mmb.irbbarcelona.org](https://openebench.bsc.es/monitor/metrics/biotools:pmut:2017/web/mmb.irbbarcelona.org) .<br/>
> example2: [https://openebench.bsc.es/monitor/metrics/biotools:pmut:2017/web/mmb.irbbarcelona.org/project/website](https://openebench.bsc.es/monitor/metrics/biotools:pmut:2017/web/mmb.irbbarcelona.org/project/website) .<br/>
> curl patch metrics data example: 
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://openebench.bsc.es/monitor/metrics/{id}/support/email -d 'true'
```
or, what is the same:
```
curl -v -X PATCH -u user:password -H 'Content-Type: application/json' /
https://openebench.bsc.es/monitor/metrics/{id} -d '{"support.email": true}'
```
the former patches the json using JSON Patch, while the latter uses mongodb 'upsert' notation.

---
It is possible to query tools:
```
https://openebench.bsc.es/monitor/rest/search?id={id}&{projection}&{text}&{name}&{description}
```
where:
- {id} is the compound tool id (i.e. "pmut", "biotools:pmut", ":pmut:2017")
- {projection} tools properties to return
- {text} text to search
> The method is thought for the client's GUI that may use a pagination mechanism.<br/>
> The pagination is implemented via the HTTP Range Header (i.g. "Range: tools=10-30").<br/>
> The response always contains the HTTP Content-Range Header ("Content-Range: tools 10-30/10000").<br/>
> When pagination is used, the server seponds with 206 Partial Content.<br/>
> The results are grouped by the id and sorted by names.<br/>
> example 1: [https://openebench.bsc.es/monitor/rest/search](https://openebench.bsc.es/monitor/rest/search) .<br/>
> example 2: [https://openebench.bsc.es/monitor/rest/search?id=pmut](https://openebench.bsc.es/monitor/rest/search?id=pmut) .<br/>
> example 3: [https://openebench.bsc.es/monitor/rest/search?text=alignment](https://openebench.bsc.es/monitor/rest/search?text=alignment) .<br/>

---

The API also provides EDAM descriptions for the tool:
```
https://openebench.bsc.es/monitor/rest/edam/tool/
https://openebench.bsc.es/monitor/rest/edam/tool/{id}/{type}/{host}
```
> example: [https://openebench.bsc.es/monitor/rest/edam/tool/biotools:pmut:2017/web/mmb.irbbarcelona.org](https://openebench.bsc.es/monitor/rest/edam/tool/biotools:pmut:2017/web/mmb.irbbarcelona.org) .

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
> example: [https://openebench.bsc.es/monitor/metrics/log/biotools:pmut:2017/cmd/mmb.irbbarcelona.org/project/website/operational](https://openebench.bsc.es/monitor/metrics/log/biotools:pmut:2017/cmd/mmb.irbbarcelona.org/project/website/operational) .
