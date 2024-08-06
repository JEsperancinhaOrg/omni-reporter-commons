#!/usr/bin/env sh
export GPG_TTY=$(tty); \
mvn clean deploy -Prelease; \
mvn nexus-staging:release -Prelease
