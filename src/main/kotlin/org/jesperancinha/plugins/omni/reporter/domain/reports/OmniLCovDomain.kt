package org.jesperancinha.plugins.omni.reporter.domain.reports


data class LineData(
    /**
     * Line Number
     */
    val lineNumber: Int,
    /**
     * Hit Count
     */
    val hitCount: Int
)

data class Function(
    /**
     * Line Number
     */
    val line: Int,
    /**
     * Function Name
     */
    val name: String
)

data class BranchData(
    /**
     * Line Number
     */
    val line: Int,
    /**
     * Block Number
     */
    val block: Int,
    /**
     * Number of Expressions
     */
    val expressions: Int,
    /**
     * Blocck Count
     */
    val count: Int
)

/**
 * Created by jofisaes on 26/01/2022
 */
data class OmniLCovReport(
    /**
     * TN
     */
    val testName: String,
    /**
     * SF
     */
    val sourceFilePath: String,
    /**
     * FN
     */
    val functionName: Function,
    /**
     * FNF
     */
    val functionNumberFound: Int,
    /**
     * FNH
     */
    val functionNumberHits: Int,
    /**
     * BRDA
     */
    val branchData: BranchData,
    /**
     * BRF
     */
    val branchFound: Int,
    /**
     * DA
     */
    val data: LineData,
    /**
     * LF
     */
    val linesFound: Int,
    /**
     * LH
     */
    val linesHit: Int
)