package org.jesperancinha.plugins.omni.reporter.domain.reports


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
data class OmniCoveragePyDomain(
    val meta: Meta,
    val files: Map<String, CoveragePyFile>,
    val totals: Totals
)