package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class OmniLCovDomainKtTest {

    @Test
    fun readLCovReport() {
        val lcovFileStream = javaClass.getResourceAsStream("/lcov.info")

        lcovFileStream.shouldNotBeNull()
        val readLCovReport = lcovFileStream.readLCovReport()

        readLCovReport.shouldNotBeNull()
    }
}