package org.jesperancinha.plugins.omni.reporter.domain

import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyReport
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniJacocoFileAdapter
import org.jesperancinha.plugins.omni.reporter.parsers.Language.KOTLIN
import org.jesperancinha.plugins.omni.reporter.parsers.camelCaseJsonObjectMapper
import org.jesperancinha.plugins.omni.reporter.pipelines.GitHubPipeline
import org.jesperancinha.plugins.omni.reporter.transformers.JacocoParserToCodacy
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.io.path.toPath

@Disabled
internal class CodacyCoverallsSourceFileTest {

    @Test
    fun `should parse Codacy file correctly`() {
        val inputStream = javaClass.getResourceAsStream("/codacy-coverage.json")
        inputStream.shouldNotBeNull()
        val codacyReport = camelCaseJsonObjectMapper.readValue<CodacyReport>(inputStream)
        codacyReport.total.shouldBe(34)
        codacyReport.fileReports.shouldNotBeEmpty()
        codacyReport.fileReports.forEach { report ->
            report.shouldNotBeNull()
            report.filename.shouldNotBeNull()
            report.total.shouldNotBeNull()
            report.coverage.shouldNotBeNull()
            report.coverage.entries.shouldHaveAtLeastSize(1)
        }
    }

    @Test
    fun `should be the same after parsing`() {
        val inputStream = javaClass.getResourceAsStream("/codacy-coverage.json")
        inputStream.shouldNotBeNull()
        val codacyReport = camelCaseJsonObjectMapper.readValue<CodacyReport>(inputStream)

        val jacocoResource = javaClass.getResource("/jacoco-for-codacy.xml")
        jacocoResource.shouldNotBeNull()
        val jacocoFileResource = jacocoResource.toURI().toPath().toFile()
        jacocoFileResource.shouldNotBeNull()

        val resource = javaClass.getResource("/")
        resource.shouldNotBeNull()
        val root = resource.toURI().toPath().toFile()
        JacocoParserToCodacy(
            "",
            pipeline = GitHubPipeline(fetchBranchNameFromEnv = false),
            root = root,
            failOnUnknown = false,
            language = KOTLIN
        ).parseInput(
            OmniJacocoFileAdapter(
                jacocoFileResource, false, root, root
            ), listOf(root)
        ).let {
            val reportResult = it.copy(
                fileReports = it.fileReports.sortedBy { name -> name.filename }.toTypedArray()
                    .map { fileReport ->
                        fileReport.copy(coverage = fileReport.coverage.entries.sortedBy { entry -> entry.key }
                            .associate { entry -> entry.key to entry.value }.toMutableMap())
                    }.toTypedArray()
            )
            val expectedReport = codacyReport.copy(
                fileReports = codacyReport.fileReports.sortedBy { name -> name.filename }.toTypedArray()
                    .map { fileReport ->
                        fileReport.copy(coverage = fileReport.coverage.entries.sortedBy { entry -> entry.key }
                            .associate { entry -> entry.key to entry.value }.toMutableMap())
                    }.toTypedArray()
            )
            reportResult shouldBe expectedReport
        }

    }
}