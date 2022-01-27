package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

internal class OmniCoveragePyDomainKtTest {

    @Test
    fun readCoveragePyReport() {
        val coveragePyStream = javaClass.getResourceAsStream("/coverage.json")

        coveragePyStream.shouldNotBeNull()

        val coveragePyReport = coveragePyStream.readCoveragePyReport()

        coveragePyReport.shouldNotBeNull()
    }
}