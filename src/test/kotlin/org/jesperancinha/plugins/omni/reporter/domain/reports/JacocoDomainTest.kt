package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jesperancinha.plugins.omni.reporter.JacocoXmlParsingErrorException
import org.jesperancinha.plugins.omni.reporter.domain.api.*
import org.jesperancinha.plugins.omni.reporter.parsers.readXmlValue
import org.jesperancinha.plugins.omni.reporter.parsers.writeSnakeCaseJsonValueAsString
import org.junit.jupiter.api.Test

internal class JacocoDomainTest {
    @Test
    fun `should parse basic JacocoReport`() {
        val inputStream = javaClass.getResourceAsStream("/jacoco.xml")
        inputStream.shouldNotBeNull()
        val readValue = readXmlValue<Report>(inputStream)
        readValue.shouldNotBeNull()
        readValue.name shouldBe "Advanced Library Management Reactive MVC"
        readValue.packages.forEach {
            it.name.shouldNotBeNull()
            it.sourcefiles.shouldNotBeNull()
        }
    }

    @Test
    fun `should not fail when parsing report`() {
        val inputStream = javaClass.getResourceAsStream("/jacoco-bad.xml")
        inputStream.shouldNotBeNull()
        val readValue = inputStream.readJacocoReport()
        readValue.shouldNotBeNull()
        readValue.packages.shouldNotBeNull()
        readValue.packages.shouldBeEmpty()
    }

    @Test
    fun `should not fail when parsing packages`() {
        val inputStream = javaClass.getResourceAsStream("/jacoco-bad.xml")
        inputStream.shouldNotBeNull()
        val readValue = inputStream.readJacocoPackages()
        readValue.shouldNotBeNull()
        readValue.shouldBeEmpty()
    }

    @Test
    fun `should parse basic JacocoReport 2`() {
        val inputStream = javaClass.getResourceAsStream("/jacoco2.xml")
        inputStream.shouldNotBeNull()
        val readValue = readXmlValue<Report>(inputStream)
        readValue.shouldNotBeNull()
        readValue.name shouldBe "Advanced Library Management Gate"
        readValue.packages.shouldNotBeNull()
        readValue.packages.shouldHaveSize(6)
        readValue.packages.forEach {
            it.name.shouldNotBeNull()
            it.sourcefiles.shouldNotBeNull()
        }
        val value = readValue.packages[1]
        value.shouldNotBeNull()
        value.sourcefiles.shouldNotBeNull()
        value.sourcefiles.shouldHaveSize(5)
        val sourcefile = value.sourcefiles[0]
        sourcefile.shouldNotBeNull()
        sourcefile.lines.shouldNotBeNull()
        sourcefile.lines.shouldHaveSize(5)
        val line = sourcefile.lines[0]
        line.shouldNotBeNull()
        line.nr shouldBe 11
        line.ci shouldBe 2
        line.mi shouldBe 0
        line.cb shouldBe 0
        line.mb shouldBe 0
    }

    @Test
    fun `should generate snake case source object`() {
        val coverallsSourceFile = CoverallsSourceFile("name", "sourceDigest")
        val writeValueAsString = writeSnakeCaseJsonValueAsString(coverallsSourceFile)
        writeValueAsString.shouldBe("{\"name\":\"name\",\"source_digest\":\"sourceDigest\"}")
    }

    @Test
    fun `should generate snake case coveralls object`() {
        val coverallsSourceFile = CoverallsReport(
            "repoToken", "serviceName",
            mutableListOf(CoverallsSourceFile("name", "sourceDigest")),
            Git(
                Head("id", "authorName", "authorEmail", "committerName", "committerEmail"),
                "branch", listOf(Remote("remote", "url"))
            )
        )
        val writeValueAsString = writeSnakeCaseJsonValueAsString(coverallsSourceFile)
        writeValueAsString.shouldBe("{\"repo_token\":\"repoToken\",\"service_name\":\"serviceName\",\"source_files\":[{\"name\":\"name\",\"source_digest\":\"sourceDigest\"}],\"git\":{\"head\":{\"id\":\"id\",\"author_name\":\"authorName\",\"author_email\":\"authorEmail\",\"committer_name\":\"committerName\",\"committer_email\":\"committerEmail\",\"message\":null},\"branch\":\"branch\",\"remotes\":[{\"name\":\"remote\",\"url\":\"url\"}]}}")
    }
}