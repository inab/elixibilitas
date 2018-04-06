### OpenEBench Tools Metrics.

#### Identity & Findability metrics

|   | metrics                        | json path                           | type | description                                                        |
|---|--------------------------------|-------------------------------------|-------|-------------------------------------------------------------------|
| 1 | canonical:website              | project.website                     | null  | Project/software has a web page.                                  |
| 2 | canonical:domain               | project.identity.domain             | bool  | Project/software has its own domain name.                         |
| 3 | canonical:trademark            | project.identity.trademark          | bool  | Project/software name is trade-marked.                            |
| 4 | version:robots_compatible      | project.website.robots              | bool  | Could Search Engine Robots track their website?                   |
| 5 | version:registries             |                                     |       | Software registries that include the software.                    |
| 6 | version:scientific_benchmark   |                                     |       | Software is a part of scientific benchmark activities.            |
| 7 | canonical:recognizability      |                                     |       | Project/software has a distinct name within its application area. |

#### Usability: Documentation metrics

|    | metrics                       | json path                           | type  | description                                                                                                        |
|----|-------------------------------|-------------------------------------|-------|--------------------------------------------------------------------------------------------------------------------|
| 8  | version:help                  | project.documentation.howto         | uri   | Whether there is a general help about how to use the tool.                                                         |
| 9  | version:tutorial              | project.documentation.tutorial      | uri   | Whether there is a tutorial associated.                                                                            |
| 10 | version:readme                | distribution.sourcecode.readme      |       | Whether there is a readme file distributed along the code.                                                         |
| 11 | version:publications          | project.publications                | int   | Whether the resource has an associated publication.                                                                |
| 12 | version:cite                  | project.documentation.citation      | bool  | Whether the resource includes an statement on how to cite it  and potentially associated algorithms, methods, etc. |
| 13 | version:api                   | project.documentation.api           | uri   | Complete API documentation (e.g. JavaDoc, Doxygen).                                                                |
| 14 | version:repositories          | project.documentation.api_versioned | uri[] | Whether the API documentation is held under version control system.                                                |
| 15 | version:source/comments_ratio |                                     |       |                                                                                                                    |
