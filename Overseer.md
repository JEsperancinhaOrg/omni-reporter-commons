# omni-reporter-commons - Overseer rules

This is a long time coming option. For the moment, the rules below represent a draft on what's to come once this version is available.

## Intro - DRAFT
 
Almost all frameworks use specific standards and most projects use those standards. The way the folders are setup, should reflect these standards. Sometimes, however, the implemented structure moves away from that standard. The Overseer will even try to recognize these differences.

For the first attempt, the Overseer will check for the reports following the rules according to the technology:

### 1. Maven

The plugin supports Maven and so nothing external will be accounted for.

### 2. Gradle

The plugin supports Gradle and so nothing external will be accounted for

### 3. Python

There may or may not be a build file like `requirements.txt`. This makes it difficult to find a potential root sources of the project.
However, coverage files generated with CoveragePy are usually located in the root and the `Overseer` will establish the found report to be located in both the report and the sources' location.

### 4. Node

Using Jest, the reports are usually generated in a `coverage` folder. Once a report for `ts`, `js`, `tsx` or `jsx` files is found, the `Overseer` will check if the sources are located one folder above. It will do so by detecting the presence of the `package.json` file. 
Should that not be the case, then the overseer will establish that the source folder is located where the common prefix of all the files is found. It will first attempt to find the location of any file anywhere and then from that location, it will extract the root.
This last resort usage should remain very uncommon to use and will only happen if the project is structured in very uncommon fashion.

### 5. Lost Jacoco files

If there are lost Jacoco files unrelated to the sources or build directories, which are also not in the rejected list, then the `Overseer` will try to find any of the files in any location.

## 2. Extra Notes
For all examples, should 1 or more files be found in different locations but the same postfix, the file considered will be the closest one in the File system.

This option will remain optional given that for more complex project or projects that don't follow conventional standards, it will always be very unpredictable the outcome of the reports. In these cases I suggest to try the `Overseer` option a few times locally and check how it reports.
If that's not possible, then at least try it a few couple of times in your test environment. If everything works after a few tries, then it should be ok to use it, if the project structure does not change. If it changes though (i.e. one more module, module location changes, new module from a different technology, new reporting framework added, etc...), then you should repeat the process.
