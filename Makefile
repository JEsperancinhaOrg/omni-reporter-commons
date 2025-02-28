SHELL := /bin/bash

b: build
build:
	mvn clean install
test:
	mvn test
local:
	mkdir -p bin
coverage-old:
	mvn clean jacoco:prepare-agent install package jacoco:report omni-coveragereporter:report
coverage:
	mvn clean jacoco:prepare-agent install jacoco:report
report:
	mvn omni-coveragereporter:report
no-test:
	mvn clean install -DskipTests
release-old:
	export GPG_TTY=$(tty); \
	export MAVEN_OPTS=--illegal-access=permit; \
	mvn clean deploy -Prelease; \
	mvn nexus-staging:release -Prelease
release:
	export GPG_TTY=$(tty); \
	mvn clean deploy -Prelease; \
	mvn nexus-staging:release -Prelease
update-repo-prs:
	curl -sL https://raw.githubusercontent.com/jesperancinha/project-signer/master/update-all-repo-prs.sh | bash
