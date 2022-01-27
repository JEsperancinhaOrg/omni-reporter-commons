package org.jesperancinha.plugins.omni.reporter.domain.reports

import org.jesperancinha.plugins.omni.reporter.JacocoXmlParsingErrorException
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniCoveragePyDomain.Companion.logger
import org.jesperancinha.plugins.omni.reporter.parsers.readJsonValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream


data class Summary(
    val covered_lines: Int = 0,
    val num_statements: Int = 0,
    val percent_covered: Double = 0.0,
    val percent_covered_display: String? = null,
    val missing_lines: Int = 0,
    val excluded_lines: Int = 0
)

data class Meta(
    val version: String,
    val timestamp: String,
    val branchCoverage: Boolean,
    val showContexts: Boolean
)

data class Totals(
    val coveredLines: Int = 0,
    val num_statements: Int = 0,
    val percentCovered: Double = 0.0,
    val percentCovered_display: String? = null,
    val missingLines: Int = 0,
    val excludedLines: Int = 0
)

data class CoveragePyFile(
    val executedLines: List<Int> = emptyList(),
    val summary: Summary,
    val missingLines: List<Int> = emptyList(),
    val excludedLines: List<Int> = emptyList()
)

/**
 * Created by jofisaes on 26/01/2022
 */
data class OmniCoveragePy(
    val meta: Meta,
    val files: Map<String, CoveragePyFile>,
    val totals: Totals
)

class OmniCoveragePyDomain {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(OmniCoveragePyDomain::class.java)
    }
}

fun InputStream.readCoveragePyReport(failOnJsonParseError: Boolean = false) =
    readJsonValue<OmniCoveragePy>(this).apply {
        if (files.isEmpty()) {
            logger.warn("Failed to process CoveragePy file!")
            if (failOnJsonParseError)
                throw JacocoXmlParsingErrorException()
        }
    }
