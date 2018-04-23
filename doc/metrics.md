### OpenEBench Tools Metrics.

#### Identity & Findability metrics

|   | metrics                        | json path                           | type    | description                                                        |
|---|--------------------------------|-------------------------------------|---------|-------------------------------------------------------------------|
| 1 | canonical:website              | project.website                     | object  | Project/software has a web page.                                  |
| 2 | canonical:domain               | project.identity.domain             | bool    | Project/software has its own domain name.                         |
| 3 | canonical:trademark            | project.identity.trademark          | bool    | Project/software name is trade-marked.                            |
| 4 | version:robots_compatible      | project.website.robots              | bool    | Could Search Engine Robots track their website?                   |
| 5 | version:registries             |                                     |         | Software registries that include the software.                    |
| 6 | version:scientific_benchmark   |                                     |         | Software is a part of scientific benchmark activities.            |
| 7 | canonical:recognizability      |                                     |         | Project/software has a distinct name within its application area. |

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
| 15 | version:source/comments_ratio |                                     |       | Ratio code/comments, code lines/document lines. It reflects how much the code is documented.                       |

#### Usability: Understability metrics

|    | metrics                       | json path                           | type  | description                                                                                                                                                           |
|----|-------------------------------|-------------------------------------|-------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 16 | canonical:description         | project.summary.description         | bool  | Whether high-level description of the tool is available. It should contain concept, rationale and application. By application we understand what the software is for. |
| 17 | canonical:architecture        | project.summary.architecture        | bool  | Whether architectural overview, with diagrams, is available.                                                                                                          |
| 18 | canonical:usecase             | project.summary.case_studies        | bool  | Whether case studies of use are available. Example of use.                                                                                                            |
| 19 | canonical:concept             | project.summary.concept             | bool  | Whether high-level description of how the tool/software works.                                                                                                        |
| 20 | canonical:rationale           | project.summary.rationale           | bool  | Whether design rationale is available.                                                                                                                                |
| 21 | canonical:application         | project.summary.usecases            | uri   | Whether descriptions of intended use cases are available.                                                                |

#### Usability: Buildability & Installability

|    | metrics                            | json path                           | type  | description                                                                                         |
|----|------------------------------------|-------------------------------------|-------|-----------------------------------------------------------------------------------------------------|
| 22 | version:build_instructions         | project.build.instructions          | bool  | Whether instructions for building the software are provided.                                        |
| 23 | version:installation_instructions  | project.deployment.instructions     | bool  | Whether instructions for installing the software are provided.                                      |
| 24 | version:build_dependencies         | project.build.dependencies          | bool  | Whether the list of all third-party dependencies for proper project build is provided.              |
| 25 | version:execution_dependencies     | project.deployment.dependencies     | bool  | Whether the list of all third-party runtime dependencies is provided.                               |
| 26 | version:execution_test             |                                     | bool  | Whether the software has specific tests to ensure the correct installation.                         |
| 27 | version:virtual_environments       | distribution.vre                    | bool  | Whether the resource could be built into a virtual environment such as BioConda.                    |
| 28 | version:type                       | project.build.automated             | bool  | Type of the automated build system used (make, maven, ant, etc).                                    |
| 29 | version:package_based_installation | distribution.packages               | bool  | Whether the software can be installed from pre-configured packages e.g. RPMs, DEBs, PiP, CPAN, PECL |
| 30 | version:language                   | distribution.sourcecode.interpreted | bool  | Code source languange. Whether it is compiled (C, C++, C#) or interpreted (Python, Perl, Ruby).     |
| 31 | version:operative_system           | project.build.unix                  | bool  | Operative system used to build the software (Unix or Not).                                          |
| 32 | compiler_warnings                  |                                     |       | Whether the compiler gives warnings. Compilation sucess.                                            |
| 33 | version:automated                  | project.build.automated             | bool  | Whether an automated build system used.                                                             |

#### Copyright

|    | metrics             | json path                         | type  | description                                                           |
|----|---------------------|-----------------------------------|-------|-----------------------------------------------------------------------|
| 34 | copyright_statement | project.website.copyright         | bool  | Web site states copyright.                                            |
| 35 | credits             | project.website.acknowledgement   | bool  | Web site states who developed/develops the software, funders etc.     |
| 36 | consistency         |                                   | bool  | All sites state exactly the same copyright, licencing and authorship. |
| 37 | copyright_headers   | distribution.sourcecode.copyright | bool  | Each source code file has a copyright statement.                      |

#### Licensing

|    | metrics             | json path                               | type   | description                                                        |
|----|---------------------|-----------------------------------------|--------|--------------------------------------------------------------------|
| 38 | project_license     | project.license                         | object | Project has a license.                                             |
| 39 | license_statement   | project.website.license                 | bool   | Web site states license.                                           |
| 40 | sourcecode_license  | distribution.sourcecode.license         | bool   | Code distribution includes a license file.                         |
| 41 | license_headers     | distribution.sourcecode.license_headers | bool   | Each source code file has a license header.                        |
| 42 | open_source         | project.license.open_source             | bool   | Project has an open source license.                                |
| 43 | osi                 | project.license.osi                     | bool   | Software has an Open Software Initiative (OSI)-recognised license. |

#### Accesibility

|    | metrics                                  | json path                                    | type   | description                                                               |
|----|------------------------------------------|----------------------------------------------|--------|---------------------------------------------------------------------------|
| 44 | binary_distribution                      | distribution.binaries                        | object | Whether binary distributions are available.                               |
| 45 | binary_distribution_freeness             | distribution.binaries.freeness               | bool   | Binary distributions are freely available.                                |
| 46 | binaries_download_registration           | distribution.binaries.registration           | bool   | Binary distributions are available without the need for any registration. |
| 47 | sourcecode                               | distribution.sourcecode                      | object | Source distributions are available.                                       |
| 48 | sourcecode_freeness                      | distribution.sourcecode.free                 | bool   | Source distributions are freely available.                                |
| 49 | sourcecode_download_registration         | distribution.sourcecode.public               | bool   | Sourcecode is available without the need for any registration.            |
| 50 | sourcecode_repository                    | distribution.sourcecode.repository           | object | Access to source code repository is available.                            |
| 51 | sourcecode_anonymous_access              | distribution.sourcecode.repository.anonymous | bool   | Anonymous read-only access to source code repository.                     |
| 52 | sourcecode_repository_browse             | distribution.sourcecode.repository.online    | bool   | Ability to browse source code repository online.                          |
| 53 | sourcecode_repository_accessible         |                                              | bool   | Make sure the repository (github, bit bucket) contains code.              |
| 54 | sourcecode_repository_version_controlled |                                              | bool   | Check whether code contains any information about versions/releases/tags. |

#### Changeabilty

|    | metrics              | json path                                    | type     | description                                               |
|----|----------- ----------|----------------------------------------------|----------|-----------------------------------------------------------|
| 55 | contributions_policy |                                              |          | Whether the project has defined a contributions policy.   |
| 56 | issue_tracker        | support.issue_tracker.public_tracker         | bool     | Whether the tool has a publicly accessible issue tracker. |
| 57 | resolve_time         | support.issue_tracker.resolve_time           | duration | How long are issues open.                                 |
| 58 | governance           | project.governance                           | bool     | Whether the project has defined a governance model.       |



