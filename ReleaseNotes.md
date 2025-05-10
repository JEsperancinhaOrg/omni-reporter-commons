# Omni Reporter Commons Release Notes

#### Release 0.4.6 - Upcoming

1. Support for Group Coverage with Branch Coverage (mostly Coveralls)
2. Source encoding gets automatically chosen unless we configure flag `failOnNoEncoding` to `true`
3. Overseer option: `overseer`. If this option is on, the plugin will look for external reports and sources automatically following the [Overseer.md](./Overseer.md) document rules
4. GoLang Coverage Support (`.out` files)
5. Fix double parent phenomenon for Codecov specific projects where the path fix algorithm doubles the parent folder. Example is [![Generic badge](https://img.shields.io/static/v1.svg?label=GitHub&message=Vertext%20Test%20Drives&color=informational)](https://github.com/jesperancinha/jeorg-vertex-osgi-testdrives).
- Difficult to fix - The jacoco report being sent does have the path in a correct way, so this seems to be a divergent behaviour that happens in some Codecov corner cases. Nonetheless, a solution will be investigated. Perhaps an extra option
6. Define where the name and location of the original jar when jacoco reports are being used as described in the [Troubleshooting](https://github.com/JEsperancinhaOrg/omni-reporter-maven-plugin/blob/main/Troubleshooting.md) document.
7. Add dynamic search for common source folder standards (from `src/main/java` to all known languages. i.e. add `src/main/scala` to `src/main/java`) 

#### Release 0.4.5 - 2025/05/10

Ignores unknown properties

#### Release 0.4.4 - 2024/08/06

1. Updates for unknown Fields in Meta


#### Release 0.4.3 - 2023/05/10

1. Library updates

#### Release 0.4.2 - 2023/02/02

1. Parallelization support for multiple sub-report frameworks like Codacy

#### Release 0.4.1 - 2023/02/02

1. Parallelization support for multiple sub-report frameworks like Codacy (DO NOT USE THIS VERSION)

#### Release 0.4.0 - 2023/01/31

1. Parallelization support

#### Release 0.3.2 - 2023/01/11

1. Support for JDK19

#### Release 0.3.1 - 2022/03/01

1. Share extra `source` and `report` algorithm
2. Ignore `node_modules` folder

#### Release 0.3.0 - 2022/02/14

1. Refactoring - Current version still has a lot of code that can be reused.
2. Option `OMNI_LOG` with environment variable. Logs everything into `target/omni.log` file (Not configurable yet)
3. Read JSON configuration method

#### Release 0.2.0 - 2022/02/07

1. Interoperability
- Important Static methods made available to `Clojure` and `Java`
- Reduction of the need to create instances on the fly.
3. LCov Expected Values Parsing Improvements - The absence of some values would generate an unwanted exception
4. Clojure's language support

> This release contains breaking changes because it now supports interoperability between Kotlin and other JVM languages

#### Release 0.1.5 - 2022/02/02

###### Features

1. Remove possibility to send unknown reports in Omni to Codecov. Unfortunately Codecov crashes with unknown reports. The only way to check the correct format of the report is essentially to provide the implementation to do that. In this way report sending to Codecov becomes restricted to the known formats to Omni.
2. Remove banned file name list - Since the algorithm no longer relies on the filename, it doesn't make sense anymore to keep doing that. Therefore, the banned list is removed.

###### Bugs

1. Jacoco XML generation sometimes results in empty string.

#### Release 0.1.4 - 2022/02/1

1. Url Fix for BitBucket

#### Release 0.1.3 - 2022/02/1

###### Features

1. ~~Reports in the same folder are merged and average, (We assume thart if there are different reports in the same folder, it only means that there are different brands in there.)~~ Unfortunately, by doing so, the results may become inconsistent for reports that do not report coverage for certain
   files. Please make sure not to have different report brands (i.e. Clover and LCov together) for the same files. In that case, line coverage will be reported duplicate.
2. Exclude report option `reportRejectList`. Given the above, it may be difficult to manage that situation. We can then use this option to exclude a report that for some reason we don't want to consider for the overall coverage calculation). See example in [![Generic badge](https://img.shields.io/static/v1.svg?label=GitLab&message=Bridge%20Management%20Logistics&color=informational)](https://gitlab.com/jesperancinha/bridge-logistics).
3. Change file detection to include content. The check should be based on text labels and parsing should be avoided.
4. ~~Remove files from the excluded list where applicable (code wise). Files in de excluded list are also used in a filename based algorithm. This needs to be changed.~~ - I leave it there because `coverage-final.json` is not a part of any other report default naming strategy. This way I keep this protection.
5. Add support for Circle CI and BitBucket builds

#### Release 0.1.2 - 2022/01/31

###### Features

1. Create flag `fetchBranchNameFromEnv` to fetch the branch name from environment variables

#### Release 0.1.1 - 2022/01/31

###### Bugs

1. Fix Reporting sending of test report files by default with `ignoreTestBuildDirectory`
2. Fix Codacy not including TSX in TypeScript reports

#### Release 0.1.0 - 2022/01/30

###### Features

> !! Breaking Changes !!

1. Support for [CoveragePy](https://coverage.readthedocs.io/)
2. Support for [LCov](https://wiki.documentfoundation.org/Development/Lcov)
3. Support for [Jacoco.exec](https://www.jacoco.org/jacoco/) files
4. Support for [OpenClover](https://openclover.org/index)

###### Bugs

1. Root Path fix for internal elements

---

Support Upgrade Leap

--- 

#### Release 0.0.1 - 2022/01/22

It was found that under certain circumstances (i.e. use of Gradle INFO level), the Http Google Clients lets client logs output go to the console. This represents a security risk because access keys to the coverage API's would be exposed in that way. This release covers that and avoids this way of
credentials being exposed in the logs.

#### Release 0.0.0 - 2022/01/21

Below the stripped line, you'll find the release roadmap for the [omni-reporter-maven-plugin](https://github.com/JEsperancinhaOrg/omni-reporter-maven-plugin).

This library has its origins in this plugin. All the code has been moved to this library in order to be able to share code between the maven, gradle and future plugins.

NOTE: Version 0.0.0 of this library is to be released as the inception version. Full test coverage and improvements will follow in upcoming versions.

---

Omni Reporter Plugin Releases

---

## Release notes from Omni Reporter Maven Plugin

#### Release 0.0.11 - 2022/01/13

1. Ignore if package not found for Codecov with `failOnUnknown`

#### Release 0.0.10 - 2022/01/09

1. Path Corrections for Codecov Jacoco reports

#### Release 0.0.9 - 2022/01/09

1. Codecov support for endpoint V4 version
2. API token support for codacy

#### Release 0.0.8 - 2022/01/08

1. Disable flags for Coveralls and Codacy to force them out even when environment variables are available
    1. `disableCoveralls`
    2. `disableCodacy`
2. Exception handling for Codacy formatting issue
    3. `failOnXmlParsingError`, false by default
3. Codacy update so solve Xerces module error. Manual implementation required

#### Release 0.0.7 - 2022/01/06

1. Codacy support - JAVA 11 Only
2. `failOnReportNotFound`
3. `failOnUnknown` Bug fix
4. Possibility to add external root sources - useful in cases where projects are using scala, java, kotlin and/or clojure at the same time. The plugin only recognizes one source directory. Parameter name is `extraSourceFolders`
5. `failOnReportSendingError`

#### Release 0.0.6 - 2022/01/05

1. Rollback `CI_COMMIT_REF_NAME`. Unfortunately this messes up the way the JOB ID is calculated.

> It has been observed that GitLab checks out the repo on the desired commit, but looses reference to the branch at this point. This means that the only reference to the commit is the hash. If we change the branch name, it will use the hash-number regardless of what we configure for the Job ID.
> Example
>
> Branch = main, Hash = AAAAAAAAAAAAA and service_number = null => JobId = #AAAAAAAAAAAAA, Branch Name = main
>
> Branch = AAAAAAAAAAAAA, Hash AAAAAAAAAAAAA and service_number = null -> JobId = #AAAAAAAAAAAAA, Branch Name = AAAAAAAAAAAAA
>
> Branch = AAAAAAAAAAAAA, Hash AAAAAAAAAAAAA and service_number = 99 -> JobId = 99, Branch Name = AAAAAAAAAAAAA

#### Release 0.0.5 - 2022/01/05

1. Fix branch naming of GitLab using `CI_COMMIT_REF_NAME`

#### Release 0.0.4 - 2022/01/03

1. Fix JAXB upgrade -> Use [Jackson Module](https://medium.com/@foxjstephen/how-to-actually-parse-xml-in-java-kotlin-221a9309e6e8) parser

#### Release 0.0.3 - 2022/01/03

1. Correct JOB_ID and RUN_ID for GIT_RUN pipeline
2. `useCoverallsCount` to let Coveralls decide Job and run numbers.

#### Release 0.0.2 - 2022/01/03

1. Source encoding gets automatically chosen unless we configure flag `failOnNoEncoding` to `true`
2. Ignore test build directory by default. Make `ignoreTestBuildDirectory`, `true` by default.
3. Find files in all sources directories including generated sources

#### Release 0.0.1 - 2022/01/02

1. Rejection words implemented. Fixes issue with GitHub pipelines build names for Coveralls Report
2. Token log shadowing (even in debug) for Coveralls Report

#### Release 0.0.0 - 2022/01/01

1. We can ignore unknown class error generated by Jacoco. This happens with some Kotlin code. The option is `failOnUnknown`
2. [Saga](https://timurstrekalov.github.io/saga/) and [Cobertura](https://www.mojohaus.org/cobertura-maven-plugin/) support is not given because of the lack of updates in these plugins for more than 5 years.
3. Plugin will search for all jacoco.xml files located in the build directory.
4. If there are two reports with the same file reported, the result will be a sum.
5. Coveralls support
6. DOM processing instead of SAX. Using an event parser for XML can be quite cumbersome and if the XML document isn't correctly validated, we run the risk of having misleading or false results. In any case, when making a code report, we usually don't need to worry about performance and if we do, it
   is probably a sign that the codebase is too big and that our code is becoming a monolith.
7. Line Coverage