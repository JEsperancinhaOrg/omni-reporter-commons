# omni-reporter-commons

[![Twitter URL](https://img.shields.io/twitter/url?logoColor=blue&style=social&url=https%3A%2F%2Fimg.shields.io%2Ftwitter%2Furl%3Fstyle%3Dsocial)](https://twitter.com/intent/tweet?text=%20Checkout%20this%20%40github%20repo%20by%20%40joaofse%20%F0%9F%91%A8%F0%9F%8F%BD%E2%80%8D%F0%9F%92%BB%3A%20https%3A//github.com/JEsperancinhaOrg/omni-reporter-commons)
[![Generic badge](https://img.shields.io/static/v1.svg?label=GitHub&message=omni-reporter-commons&color=informational)](https://github.com/JEsperancinhaOrg/omni-reporter-commons)

[![GitHub release](https://img.shields.io/github/release/JEsperancinhaOrg/omni-reporter-commons.svg)](#)
[![Maven Central](https://img.shields.io/maven-central/v/org.jesperancinha.plugins/omni-coveragereporter-commons)](https://mvnrepository.com/artifact/org.jesperancinha.plugins/omni-coveragereporter-commons)
[![Sonatype Nexus](https://img.shields.io/nexus/r/https/oss.sonatype.org/org.jesperancinha.plugins/omni-coveragereporter-commons.svg)](https://search.maven.org/artifact/org.jesperancinha.plugins/omni-coveragereporter-commons)

[![javadoc](https://javadoc.io/badge2/org.jesperancinha.plugins/omni-reporter-commons/javadoc.svg)](https://javadoc.io/doc/org.jesperancinha.plugins/omni-reporter-commons)

[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

[![Snyk Score](https://snyk-widget.herokuapp.com/badge/mvn/org.jesperancinha.plugins/omni-coveragereporter-commons/badge.svg)](https://github.com/JEsperancinhaOrg/omni-reporter-commons)
[![Known Vulnerabilities](https://snyk.io/test/github/JEsperancinhaOrg/omni-reporter-commons/badge.svg)](https://snyk.io/test/github/JEsperancinhaOrg/omni-reporter-maven-plugin)

[![omni-reporter-commons](https://github.com/JEsperancinhaOrg/omni-reporter-commons/actions/workflows/omni-reporter-commons.yml/badge.svg)](https://github.com/JEsperancinhaOrg/omni-reporter-commons/actions/workflows/omni-reporter-commons.yml)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/b69f88ded9114d0797dd68c0ca51f0c5)](https://www.codacy.com/gh/JEsperancinhaOrg/omni-reporter-commons/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=JEsperancinhaOrg/omni-reporter-commons&amp;utm_campaign=Badge_Grade)
[![codebeat badge](https://codebeat.co/badges/6b8bd101-109d-4bf1-8ba1-7e9b28da73a8)](https://codebeat.co/projects/github-com-jesperancinhaorg-omni-reporter-commons-main)

[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/b69f88ded9114d0797dd68c0ca51f0c5)](https://www.codacy.com/gh/JEsperancinhaOrg/omni-reporter-commons/dashboard?utm_source=github.com&utm_medium=referral&utm_content=JEsperancinhaOrg/omni-reporter-commons&utm_campaign=Badge_Coverage)
[![codecov](https://codecov.io/gh/JEsperancinhaOrg/omni-reporter-commons/branch/main/graph/badge.svg?token=v0fqcS93fY)](https://codecov.io/gh/JEsperancinhaOrg/omni-reporter-commons)
[![Coverage Status](https://coveralls.io/repos/github/JEsperancinhaOrg/omni-reporter-commons/badge.svg?branch=main)](https://coveralls.io/github/JEsperancinhaOrg/omni-reporter-commons?branch=main)

[![GitHub language count](https://img.shields.io/github/languages/count/JEsperancinhaOrg/omni-reporter-commons.svg)](#)
[![GitHub top language](https://img.shields.io/github/languages/top/JEsperancinhaOrg/omni-reporter-commons.svg)](#)
[![GitHub top language](https://img.shields.io/github/languages/code-size/JEsperancinhaOrg/omni-reporter-commons.svg)](#)

Common Library Supporting the Maven and Gradle plugins of the same name

## How to use

Using this library depends on how you decide to integrate it. At the moment these are the current plugins I have integrating this library:

[![Generic badge](https://img.shields.io/static/v1.svg?label=GitHub&message=omni-coveragereporter-maven-plugin&color=informational)](https://github.com/JEsperancinhaOrg/omni-reporter-maven-plugin)

[![Generic badge](https://img.shields.io/static/v1.svg?label=GitHub&message=omni-coveragereporter-gradle-plugin&color=informational)](https://github.com/JEsperancinhaOrg/omni-reporter-gradle-plugin)

Please have a look at the documentation in these repos to see the possibilities to integrate this library in you project with practical examples.

## Functionality description

1.  Path Corrections for Codecov Jacoco reports
2.  Codecov support for endpoint V4 version (*1)
3.  API token support for codacy
4.  Disable flags for Coveralls and Codacy to force them out even when environment variables are available
5.  `disableCoveralls`
6.  `disableCodacy`
7.  Exception handling for Codacy formatting issue
8.  `failOnXmlParsingError`, false by default
9.  Codacy update so solve Xerces module error. Manual implementation required
10. Codacy support (*1)
11. `failOnReportNotFound`
12. `failOnUnknown` Bug fix
13. Possibility to add external root sources - useful in cases where projects are using scala, java, kotlin and/or clojure at the same time. The plugin only recognizes one source directory. Parameter name is `extraSourceFolders`
14. `failOnReportSendingError`
15. `useCoverallsCount` to let Coveralls decide Job and run numbers.
16. Ignore test build directory by default. Make `ignoreTestBuildDirectory`, `true` by default.
17. Find files in all sources directories including generated sources
18. Rejection words implemented. Fixes issue with GitHub pipelines build names for Coveralls Report
19. Token log Redacting (even in debug) for Coveralls Report
20. We can ignore unknown class error generated by Jacoco. This happens with some Kotlin code. The option is `failOnUnknown`
21. [Saga](https://timurstrekalov.github.io/saga/) and [Cobertura](https://www.mojohaus.org/cobertura-maven-plugin/) support is not given because of the lack of updates in these plugins for more than 5 years.
22. Plugin will search for all jacoco.xml files located in the build directory.
23. If there are two reports with the same file reported, the result will be a sum.
24. Coveralls support (*1)
25. Line Coverage
26. Parallelization support with `parallelization` parameter
27. HttpRequestParallelization support with `httpRequestParallelization` parameter

To be up to date with the changes please check the [ReleaseNotes](./ReleaseNotes.md) document.

## Coverage report Graphs

<div align="center">
<img width="30%" src="https://codecov.io/gh/JEsperancinhaOrg/omni-reporter-commons/branch/main/graphs/sunburst.svg"/>
<img width="30%" src="https://codecov.io/gh/JEsperancinhaOrg/omni-reporter-commons/branch/main/graphs/tree.svg"/>
</div>
<div align="center">
<img width="60%" src="https://codecov.io/gh/JEsperancinhaOrg/omni-reporter-commons/branch/main/graphs/icicle.svg"/>
</div>


## References

-   [Snyk Security Information](https://snyk.io/blog/instant-security-information-with-the-snyk-security-badge/)
-   [Jacoco Open Source Code](https://github.com/jacoco/jacoco)
-   [What does each line of the lcov output file mean?](https://giters.com/linux-test-project/lcov/issues/113)
-   [Codacy Coverage Reporter](https://github.com/codacy/codacy-coverage-reporter)
-   [Jackson Module](https://medium.com/@foxjstephen/how-to-actually-parse-xml-in-java-kotlin-221a9309e6e8)
-   [XCode Environment Variable Reference](https://developer.apple.com/documentation/xcode/environment-variable-reference)
-   [Cross-CI reference](https://github.com/streamich/cross-ci)
-   [Coveralls API reference](https://docs.coveralls.io/api-reference)
-   [Git Hub Environment Variables](https://docs.github.com/en/actions/learn-github-actions/environment-variables)
-   [Git Lab Environment Variables](https://docs.gitlab.com/ee/ci/variables/predefined_variables.html)
-   [Check Run Reporter](https://github.com/marketplace/check-run-reporter)
-   [Codacy Maven Plugin](https://github.com/halkeye/codacy-maven-plugin)
-   [Coveralls Maven Plugin](https://github.com/trautonen/coveralls-maven-plugin)
-   [Example Java Maven for CodeCov](https://github.com/codecov/example-java-maven)
-   [CodeCov Maven Plugin](https://github.com/alexengrig/codecov-maven-plugin)

## About me

[![GitHub followers](https://img.shields.io/github/followers/jesperancinha.svg?label=Jesperancinha&style=for-the-badge&logo=github&color=grey "GitHub")](https://github.com/jesperancinha)
