package org.jesperancinha.plugins.omni.reporter.processors

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.types.shouldBeInstanceOf
import org.jesperancinha.plugins.omni.reporter.domain.reports.*
import org.jesperancinha.plugins.omni.reporter.processors.ReportType.Companion.createReportFileAdapter
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Test
import kotlin.io.path.toPath

internal class ReportTypeTest {

    @Test
    fun `should detect Jacoco Report 1`() {
        val resource = javaClass.getResource("/all.reports/jacoco.xml")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniJacocoFileAdapter>()

    }
    @Test
    fun `should detect Jacoco Report 2`() {
        val resource = javaClass.getResource("/all.reports/jacoco-test-1.xml")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniJacocoFileAdapter>()

    }

    @Test
    fun `should detect Jacoco Exec Report`() {
        val resource = javaClass.getResource("/all.reports/testReleaseUnitTest.exec")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniJacocoExecFileAdapter>()

    }

    @Test
    fun `should detect LCov Report 1`() {
        val resource = javaClass.getResource("/all.reports/lcov.info")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniLCovFileAdapter>()

    }

    @Test
    fun `should detect LCov Report 2`() {
        val resource = javaClass.getResource("/all.reports/lcov2.info")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniLCovFileAdapter>()

    }


    @Test
    fun `should detect Clover Report`() {
        val resource = javaClass.getResource("/all.reports/clover.xml")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniCloverFileAdapter>()

    }

    @Test
    fun `should detect Coverage Py Report 1`() {
        val resource = javaClass.getResource("/all.reports/coverage.json")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniCoveragePyFileAdapter>()

    }

    @Test
    fun `should detect Coverage Py Report 2`() {
        val resource = javaClass.getResource("/all.reports/coverage-demo.json")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniCoveragePyFileAdapter>()

    }

    @Test
    fun `should detect Coverage Py Report 3`() {
        val resource = javaClass.getResource("/all.reports/coverage-simulation.json")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeInstanceOf<OmniCoveragePyFileAdapter>()

    }
    @Test
    fun `should not detect unsupported file coverage final`() {
        val resource = javaClass.getResource("/all.reports/coverage-final.json")
        resource.shouldNotBeNull()
        val testFile = resource.toURI().toPath().toFile()

        createReportFileAdapter(testFile, false, root, root)
            .shouldBeNull()
    }
}