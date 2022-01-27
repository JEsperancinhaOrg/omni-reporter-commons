package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test

internal class OmniCloverDomainKtTest {

    @Test
    fun readCloverReport() {
        val cloverStream = javaClass.getResourceAsStream("/clover.xml")

        cloverStream.shouldNotBeNull()
        val cloverReport = cloverStream.readCloverReport()

        cloverReport.shouldNotBeNull()
    }
}