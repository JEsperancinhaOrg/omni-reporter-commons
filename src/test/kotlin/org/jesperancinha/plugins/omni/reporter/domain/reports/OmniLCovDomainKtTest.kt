package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class OmniLCovDomainKtTest {

    @Test
    fun `should read LCov report 1`() {
        val lcovFileStream = javaClass.getResourceAsStream("/lcov.info")

        lcovFileStream.shouldNotBeNull()
        val readLCovReport = lcovFileStream.readLCovReport()

        readLCovReport.shouldNotBeNull()
    }

    @Test
    fun `should read LCov report 2`() {
        val lcovFileStream = javaClass.getResourceAsStream("/all.reports/lcov2.info")

        lcovFileStream.shouldNotBeNull()
        val readLCovReport = lcovFileStream.readLCovReport()

        readLCovReport.shouldNotBeNull()
    }
}