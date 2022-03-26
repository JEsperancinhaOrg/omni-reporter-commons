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
[![BCH compliance](https://bettercodehub.com/edge/badge/JEsperancinhaOrg/omni-reporter-commons?branch=main)](https://bettercodehub.com/results/JEsperancinhaOrg/omni-reporter-commons)

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

## About me 👨🏽‍💻🚀🏳️‍🌈

[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/JEOrgLogo-20.png "João Esperancinha Homepage")](http://joaofilipesabinoesperancinha.nl)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/medium-20.png "Medium")](https://medium.com/@jofisaes)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/bmc-20.png "Buy me a Coffe")](https://www.buymeacoffee.com/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/credly-20.png "Credly")](https://www.credly.com/users/joao-esperancinha)
[![Generic badge](https://img.shields.io/static/v1.svg?label=Homepage&message=joaofilipesabinoesperancinha.nl&color=6495ED "João Esperancinha Homepage")](https://joaofilipesabinoesperancinha.nl/)
[![GitHub followers](https://img.shields.io/github/followers/jesperancinha.svg?label=jesperancinha&style=social "GitHub")](https://github.com/jesperancinha)
[![Twitter Follow](https://img.shields.io/twitter/follow/joaofse?label=João%20Esperancinha&style=social "Twitter")](https://twitter.com/joaofse)
[![Generic badge](https://img.shields.io/static/v1.svg?label=GitHub&message=JEsperancinhaOrg&color=yellow "jesperancinha.org dependencies")](https://github.com/JEsperancinhaOrg)   
[![Generic badge](https://img.shields.io/static/v1.svg?label=Articles&message=Across%20The%20Web&color=purple)](https://github.com/jesperancinha/project-signer/blob/master/project-signer-templates/Articles.md)
[![Generic badge](https://img.shields.io/static/v1.svg?label=Webapp&message=Image%20Train%20Filters&color=6495ED)](http://itf.joaofilipesabinoesperancinha.nl/)
[![Generic badge](https://img.shields.io/static/v1.svg?label=All%20Badges&message=Badges&color=red "All badges")](https://joaofilipesabinoesperancinha.nl/badges)
[![Generic badge](https://img.shields.io/static/v1.svg?label=Status&message=Project%20Status&color=red "Project statuses")](https://github.com/jesperancinha/project-signer/blob/master/project-signer-quality/Build.md)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/coursera-20.png "Coursera")](https://www.coursera.org/user/da3ff90299fa9297e283ee8e65364ffb)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/google-apps-20.png "Google Apps")](https://play.google.com/store/apps/developer?id=Joao+Filipe+Sabino+Esperancinha)   
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/sonatype-20.png "Sonatype Search Repos")](https://search.maven.org/search?q=org.jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/docker-20.png "Docker Images")](https://hub.docker.com/u/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/stack-overflow-20.png)](https://stackoverflow.com/users/3702839/joao-esperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/reddit-20.png "Reddit")](https://www.reddit.com/user/jesperancinha/)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/devto-20.png "Dev To")](https://dev.to/jofisaes)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/hackernoon-20.jpeg "Hackernoon")](https://hackernoon.com/@jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/codeproject-20.png "Code Project")](https://www.codeproject.com/Members/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/github-20.png "GitHub")](https://github.com/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/bitbucket-20.png "BitBucket")](https://bitbucket.org/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/gitlab-20.png "GitLab")](https://gitlab.com/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/bintray-20.png "BinTray")](https://bintray.com/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/free-code-camp-20.jpg "FreeCodeCamp")](https://www.freecodecamp.org/jofisaes)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/hackerrank-20.png "HackerRank")](https://www.hackerrank.com/jofisaes)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/codeforces-20.png "Code Forces")](https://codeforces.com/profile/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/codebyte-20.png "Codebyte")](https://coderbyte.com/profile/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/codewars-20.png "CodeWars")](https://www.codewars.com/users/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/codepen-20.png "Code Pen")](https://codepen.io/jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/hacker-news-20.png "Hacker News")](https://news.ycombinator.com/user?id=jesperancinha)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/infoq-20.png "InfoQ")](https://www.infoq.com/profile/Joao-Esperancinha.2/)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/linkedin-20.png "LinkedIn")](https://www.linkedin.com/in/joaoesperancinha/)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/xing-20.png "Xing")](https://www.xing.com/profile/Joao_Esperancinha/cv)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/tumblr-20.png "Tumblr")](https://jofisaes.tumblr.com/)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/pinterest-20.png "Pinterest")](https://nl.pinterest.com/jesperancinha/)
[![alt text](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/icons-20/quora-20.png "Quora")](https://nl.quora.com/profile/Jo%C3%A3o-Esperancinha)

## Achievements

[![VMware Spring Professional 2021](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/badges/vmware-spring-professional-2021.png "VMware Spring Professional 2021")](https://www.credly.com/badges/762fa7a4-9cf4-417d-bd29-7e072d74cdb7)
[![Oracle Certified Professional, JEE 7 Developer](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/badges/oracle-certified-professional-java-ee-7-application-developer-100.png "Oracle Certified Professional, JEE7 Developer")](https://www.credly.com/badges/27a14e06-f591-4105-91ca-8c3215ef39a2)
[![Oracle Certified Professional, Java SE 11 Programmer](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/badges/oracle-certified-professional-java-se-11-developer-100.png "Oracle Certified Professional, Java SE 11 Programmer")](https://www.credly.com/badges/87609d8e-27c5-45c9-9e42-60a5e9283280)
[![IBM Cybersecurity Analyst Professional](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/badges/ibm-cybersecurity-analyst-professional-certificate-100.png "IBM Cybersecurity Analyst Professional")](https://www.credly.com/badges/ad1f4abe-3dfa-4a8c-b3c7-bae4669ad8ce)
[![Certified Advanced JavaScript Developer](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/badges/cancanit-badge-1462-100.png "Certified Advanced JavaScript Developer")](https://cancanit.com/certified/1462/)
[![Certified Neo4j Professional](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/badges/professional_neo4j_developer-100.png "Certified Neo4j Professional")](https://graphacademy.neo4j.com/certificates/c279afd7c3988bd727f8b3acb44b87f7504f940aac952495ff827dbfcac024fb.pdf)
[![Deep Learning](https://raw.githubusercontent.com/jesperancinha/project-signer/master/project-signer-templates/badges/deep-learning-100.png "Deep Learning")](https://www.credly.com/badges/8d27e38c-869d-4815-8df3-13762c642d64)
