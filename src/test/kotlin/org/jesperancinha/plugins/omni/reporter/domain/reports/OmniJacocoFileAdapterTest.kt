package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldHaveMinLength
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jesperancinha.plugins.omni.reporter.processors.ReportType
import org.jesperancinha.plugins.omni.reporter.utils.toFile
import org.junit.jupiter.api.Test

internal class OmniJacocoFileAdapterTest {

    @Test
    fun `should generate itg jacoco report`() {

        val jacocoItg = javaClass.getResource("/jacoco.formats/jacoco-itg.xml")
        jacocoItg.shouldNotBeNull()
        val rootJacocoItg = javaClass.getResource("/jacoco.formats")
        rootJacocoItg.shouldNotBeNull()

        val reportFileAdapter = ReportType.createReportFileAdapter(
            jacocoItg.toFile(),
            false,
            rootJacocoItg.toFile(),
            rootJacocoItg.toFile()
        )

        reportFileAdapter.shouldBeInstanceOf<OmniJacocoFileAdapter>()

        val generatePayload = reportFileAdapter.generatePayload(false, listOf(rootJacocoItg.toFile()))

        generatePayload.shouldNotBeEmpty()
        generatePayload.shouldHaveMinLength(10)
        generatePayload.shouldStartWith("<")
        generatePayload.shouldEndWith(">")
    }

    @Test
    fun `should generate pbr jacoco report`() {

        val jacocoItg = javaClass.getResource("/jacoco.formats/jacoco-pbr.xml")
        jacocoItg.shouldNotBeNull()
        val rootJacocoItg = javaClass.getResource("/jacoco.formats")
        rootJacocoItg.shouldNotBeNull()

        val reportFileAdapter = ReportType.createReportFileAdapter(
            jacocoItg.toFile(),
            false,
            rootJacocoItg.toFile(),
            rootJacocoItg.toFile()
        )

        reportFileAdapter.shouldBeInstanceOf<OmniJacocoFileAdapter>()

        val generatePayload = reportFileAdapter.generatePayload(false, listOf(rootJacocoItg.toFile()))

        generatePayload.shouldNotBeEmpty()
        generatePayload.shouldHaveMinLength(10)
        generatePayload.shouldStartWith("<")
        generatePayload.shouldEndWith(">")
    }

    @Test
    fun `should generate pjs jacoco report`() {

        val jacocoItg = javaClass.getResource("/jacoco.formats/jacoco-pjs.xml")
        jacocoItg.shouldNotBeNull()
        val rootJacocoItg = javaClass.getResource("/jacoco.formats")
        rootJacocoItg.shouldNotBeNull()

        val reportFileAdapter = ReportType.createReportFileAdapter(
            jacocoItg.toFile(),
            false,
            rootJacocoItg.toFile(),
            rootJacocoItg.toFile()
        )

        reportFileAdapter.shouldBeInstanceOf<OmniJacocoFileAdapter>()

        val generatePayload = reportFileAdapter.generatePayload(false, listOf(rootJacocoItg.toFile()))

        generatePayload.shouldNotBeEmpty()
        generatePayload.shouldHaveMinLength(10)
        generatePayload.shouldStartWith("<")
        generatePayload.shouldEndWith(">")
    }

    @Test
    fun `should generate report with correct paths`() {
        val resource = javaClass.getResource("/multi-module")
        resource.shouldNotBeNull()

        val jacocoResource = javaClass.getResource("/multi-module/vertx-module/target/site/jacoco/jacoco.xml")
        jacocoResource.shouldNotBeNull()

        val buildResource = javaClass.getResource("/multi-module/vertx-module/target")
        buildResource.shouldNotBeNull()

        val srcResource = javaClass.getResource("/multi-module/vertx-module/src/main/java/")
        srcResource.shouldNotBeNull()

        val reportFileAdapter = ReportType.createReportFileAdapter(
            jacocoResource.toFile(),
            false,
            resource.toFile(),
            buildResource.toFile()
        )

        reportFileAdapter.shouldNotBeNull()
        reportFileAdapter.shouldBeInstanceOf<OmniJacocoFileAdapter>()

        val generatePayload = reportFileAdapter.generatePayload(false, listOf(srcResource.toFile()))

        generatePayload.shouldNotBeNull()

    }
}
