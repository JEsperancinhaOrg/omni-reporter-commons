package org.jesperancinha.plugins.omni.reporter.domain

import io.kotest.matchers.nulls.shouldNotBeNull
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsClient
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniJacocoFileAdapter
import org.jesperancinha.plugins.omni.reporter.parsers.writeSnakeCaseJsonValueAsString
import org.jesperancinha.plugins.omni.reporter.pipelines.LocalPipeline
import org.jesperancinha.plugins.omni.reporter.transformers.ReportingParserToCoveralls
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import kotlin.io.path.toPath

internal class CoverallsClientTest {

    @Test
    @Disabled
    fun `should submit test file to coveralls`() {
        val coverallsClient = CoverallsClient("https://coveralls.io/api/v1/jobs", "token")
        val resource = javaClass.getResource("/jacoco.xml")
        resource.shouldNotBeNull()
        val jacocoReport = resource.toURI().toPath().toFile()
        jacocoReport.shouldNotBeNull()
        val jacocoParser = ReportingParserToCoveralls(
            token = "token", LocalPipeline(fetchBranchNameFromEnv = false), root,
            failOnUnknown = false,
            includeBranchCoverage = false,
            useCoverallsCount = false
        )

        val report = jacocoParser.parseInput(
            OmniJacocoFileAdapter(jacocoReport, false, root, root), listOf(root)
        )
        logger.info(writeSnakeCaseJsonValueAsString(report))

        coverallsClient.submit(report)
    }

    companion object {
        val logger = LoggerFactory.getLogger(CoverallsClientTest::class.java)
    }
}