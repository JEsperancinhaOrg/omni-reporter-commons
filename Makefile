b: build
build:
	mvn clean install
test:
	mvn test
local:
	mkdir -p bin
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
