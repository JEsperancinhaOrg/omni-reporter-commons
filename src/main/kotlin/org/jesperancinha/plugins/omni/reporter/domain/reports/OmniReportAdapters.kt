package org.jesperancinha.plugins.omni.reporter.domain.reports

import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyFileReport
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsSourceFile
import org.jesperancinha.plugins.omni.reporter.domain.api.isBranch
import org.jesperancinha.plugins.omni.reporter.logger.OmniLoggerConfig
import org.jesperancinha.plugins.omni.reporter.parsers.Language
import org.jesperancinha.plugins.omni.reporter.parsers.toFileDigest
import org.jesperancinha.plugins.omni.reporter.transformers.SourceCodeFile
import java.io.File
import kotlin.math.max

/**
 * TODO: Coveralls Branch Extension Function From Clover
 */
private val CoveragePyFile.fromCoveragePyToBranchCoverageArray: Array<Int?>
    get() = emptyArray()

/**
 * TODO: Coveralls Branch Extension Function From Clover
 */
private val List<CloverLine>.fromCloverToBranchCoverageArray: Array<Int?>
    get() = emptyArray()

/**
 * TODO: Coveralls Branch Extension Function From LCov
 */
private val List<LineData>.fromLCovToBranchCoverageArray: Array<Int?>
    get() = emptyArray()

/**
 * TODO: Coveralls Branch Extension Function From Jacoco
 */
private val List<Line>.fromJacocoToBranchCoverageArray: Array<Int?>
    get() = let {
        val branchesArray = filter { isBranch(it) }
        val coverageArray = Array<Int?>(branchesArray.size * 4) { null }
        branchesArray.forEachIndexed { lineNumber, line ->
            coverageArray[lineNumber] = line.nr
            coverageArray[lineNumber + 1] = line.mb + line.cb
            coverageArray[lineNumber + 2] = line.cb
            coverageArray[lineNumber + 3] = line.ci
        }
        coverageArray
    }

/**
 * Coveralls Line Extension Function From Jacoco
 */
private fun List<Line?>.fromJacocoToCoverallsCoverage(lines: Int): Array<Int?> = let {
    if (isNotEmpty()) {
        val calculatedLength = map { it?.nr ?: 0 }.maxOf { it }
        val coverageArray = Array<Int?>(max(lines, calculatedLength)) { null }
        forEach { line ->
            line?.let { coverageArray[line.nr - 1] = line.ci }
        }
        coverageArray
    } else {
        emptyArray()
    }
}

/**
 * Coveralls Line Extension Function From LCov
 */
private fun List<LineData?>.fromLCovToCoverallsCoverage(lines: Int): Array<Int?> = let {
    if (isNotEmpty()) {
        val calculatedLength = map { it?.lineNumber ?: 0 }.maxOf { it }
        val coverageArray = Array<Int?>(max(lines, calculatedLength)) { null }
        forEach { line ->
            line?.let { coverageArray[line.lineNumber - 1] = line.hitCount }
        }
        coverageArray
    } else {
        emptyArray()
    }
}

/**
 * Coveralls Line Extension Function From Clover
 */
private fun List<CloverLine?>.fromCloverToCoverallsCoverage(lines: Int): Array<Int?> = let {
    if (isNotEmpty()) {
        val calculatedLength = map { it?.num ?: 0 }.maxOf { it }
        val coverageArray = Array<Int?>(max(lines, calculatedLength)) { null }
        forEach { line ->
            line?.let { coverageArray[line.num - 1] = line.count }
        }
        coverageArray
    } else {
        emptyArray()
    }
}

private fun CoveragePyFile.fromCoveragePyToCoverallsCoverage(lines: Int): Array<Int?> {
    val calculatedLength = max(executedLines.maxOfOrNull { it } ?: 0, missingLines.maxOfOrNull { it } ?: 0)
    val coverageArray = Array<Int?>(max(lines, calculatedLength)) { null }
    executedLines.forEach { line ->
        coverageArray[line - 1] = 1
    }
    missingLines.forEach { line ->
        coverageArray[line - 1] = 0
    }
    return coverageArray

}

/**
 * Codacy Line Extension Function from Jacoco
 */
val List<Line?>.fromJacocoToCodacyCoverage: MutableMap<String, Int>
    get() = if (isNotEmpty()) {
        filterNotNull().associate { line -> line.nr.toString() to if (line.ci > 0) 1 else 0 }.toMutableMap()
    } else {
        mutableMapOf()
    }


private val List<LineData>.fromLCovToCodacyCoverage: MutableMap<String, Int>
    get() = if (isNotEmpty()) {
        filterNotNull().associate { line -> line.lineNumber.toString() to if (line.hitCount > 0) 1 else 0 }
            .toMutableMap()
    } else {
        mutableMapOf()
    }

private val List<CloverLine>.fromCloverToCodacyCoverage: MutableMap<String, Int>
    get() = if (isNotEmpty()) {
        filterNotNull().associate { line -> line.num.toString() to if (line.count > 0) 1 else 0 }
            .toMutableMap()
    } else {
        mutableMapOf()
    }


private val CoveragePyFile.fromCoveragePyToCodacyCoverage: MutableMap<String, Int>
    get() = (missingLines.map { it.toString() to 0 } + executedLines.map { it.toString() to 1 })
        .sortedBy { (line, _) -> line }
        .toMap()
        .toMutableMap()


/**
 * Codacy Percentage Extension Function
 */
private val CloverFile.calculateLinePercentage: Int
    get() = (metrics.coveredstatements * 100) / metrics.statements


/**
 * Codacy Percentage Extension Function
 */
private val OmniLCovReport.calculateLinePercentage: Int
    get() = (linesHit * 100) / linesFound

/**
 * Codacy Percentage Extension Function
 */
private val OmniJacocoSourcefile.calculateLinePercentage: Int
    get() = counters.firstOrNull { it.type == "LINE" }?.run { (covered * 100) / (covered + missed) } ?: 0

/**
 * Codacy Total Percentage Extension Function
 */
private val Report.calculateTotalPercentage: Int
    get() = counters.firstOrNull { it.type == "LINE" }?.run { (covered * 100) / (covered + missed) } ?: 0

/**
 * Codacy Total Percentage Extension Function
 */
private val Map.Entry<String, CoveragePyFile>.calculateLinePercentage: Int
    get() = (value.summary.covered_lines * 100) / value.summary.num_statements

/**
 * Created by jofisaes on 28/01/2022
 */
interface OmniReportFileAdapter {
    fun name(): String
    fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile?
    fun toCodacy(sourceCodeFile: SourceCodeFile, language: Language): CodacyFileReport?
}

interface OmniReportParentFileAdapter {
    fun parseAllFiles(): Sequence<Pair<String, List<OmniReportFileAdapter>>>
    fun calculateTotalPercentage(): Int
}

class OmniGenericReportParentFileAdapter : OmniReportParentFileAdapter {
    override fun parseAllFiles(): Sequence<Pair<String, List<OmniReportFileAdapter>>> = emptySequence()

    override fun calculateTotalPercentage(): Int = 0

}

class OmniJacocoReportParentFileAdapter(
    private val reportFile: Report,
    val root: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniReportParentFileAdapter {
    override fun parseAllFiles(): Sequence<Pair<String, List<OmniReportFileAdapter>>> {
        return reportFile.packages
            .asSequence()
            .map {
                it.name to it.sourcefiles.map { report ->
                    OmniJacocoReportFileAdapter(
                        report,
                        root,
                        includeBranchCoverage,
                    )
                }
            }
    }

    override fun calculateTotalPercentage() = reportFile.calculateTotalPercentage
}


class OmniJacocoReportFileAdapter(
    private val reportFile: OmniJacocoSourcefile,
    val root: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniReportFileAdapter {
    override fun name() = reportFile.name
    override fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile? {
        val sourceCodeText = sourceCodeFile.bufferedReader().use { it.readText() }
        val lines = sourceCodeText.split("\n").size
        val coverage = reportFile.lines.fromJacocoToCoverallsCoverage(lines)
        val branchCoverage = reportFile.lines.fromJacocoToBranchCoverageArray
        return if (coverage.isEmpty()) {
            null
        } else {
            try {
                val relativeName = sourceCodeFile.toRelativeString(root)
                CoverallsSourceFile(
                    name = relativeName,
                    coverage = coverage,
                    branches = if (includeBranchCoverage) branchCoverage else emptyArray(),
                    sourceDigest = sourceCodeText.toFileDigest,
                )
            } catch (ex: IllegalArgumentException) {
                null
            }
        }
    }

    override fun toCodacy(sourceCodeFile: SourceCodeFile, language: Language): CodacyFileReport? {
        val coverage = reportFile.lines.fromJacocoToCodacyCoverage
        return if (coverage.isEmpty() || language.ext.none { reportFile.name.endsWith(it) }) {
            null
        } else {
            CodacyFileReport(
                filename = "${sourceCodeFile.packageName}/${reportFile.name}".replace("//", "/"),
                total = reportFile.calculateLinePercentage,
                coverage = coverage
            )
        }
    }
}

class OmniLCovReportParentFileAdapter(
    private val report: List<OmniLCovReport>,
    val root: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniReportParentFileAdapter {
    override fun parseAllFiles(): Sequence<Pair<String, List<OmniReportFileAdapter>>> =
        listOf("" to report.map {
            OmniLCovReportFileAdapter(
                it,
                root,
                includeBranchCoverage = includeBranchCoverage
            )
        }).asSequence()


    override fun calculateTotalPercentage(): Int =
        (report.sumOf { it.linesHit } * 100) / report.sumOf { it.linesFound }

}

class OmniLCovReportFileAdapter(
    private val reportFile: OmniLCovReport,
    val root: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniReportFileAdapter {
    override fun name(): String = reportFile.sourceFilePath
    override fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile? {
        val sourceCodeText = sourceCodeFile.bufferedReader().use { it.readText() }
        val lines = sourceCodeText.split("\n").size
        val coverage = reportFile.lineData.fromLCovToCoverallsCoverage(lines)
        val branchCoverage = reportFile.lineData.fromLCovToBranchCoverageArray
        return if (coverage.isEmpty()) {
            null
        } else {
            try {
                CoverallsSourceFile(
                    name = sourceCodeFile.toRelativeString(root),
                    coverage = coverage,
                    branches = if (includeBranchCoverage) branchCoverage else emptyArray(),
                    sourceDigest = sourceCodeText.toFileDigest,
                )
            } catch (ex: IllegalArgumentException) {
                null
            }
        }
    }

    override fun toCodacy(sourceCodeFile: SourceCodeFile, language: Language): CodacyFileReport? {
        val coverage = reportFile.lineData.fromLCovToCodacyCoverage
        return if (coverage.isEmpty() || language.ext.none { reportFile.sourceFilePath.endsWith(it) }
        ) {
            null
        } else {
            CodacyFileReport(
                filename = "${sourceCodeFile.packageName}/${reportFile.sourceFilePath}".replace("//", "/"),
                total = reportFile.calculateLinePercentage,
                coverage = coverage
            )
        }
    }
}

class OmniCloverReportParentFileAdapter(
    private val report: OmniCloverReport,
    val root: File,
    val projectBuildDirectory: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniReportParentFileAdapter {
    override fun parseAllFiles(): Sequence<Pair<String, List<OmniReportFileAdapter>>> =
        listOf("" to report.project.files
            .map {
                OmniCloverReportFileAdapter(
                    it,
                    root,
                    projectBuildDirectory,
                    includeBranchCoverage = includeBranchCoverage
                )
            }).asSequence()


    override fun calculateTotalPercentage(): Int =
        (report.project.metrics.coveredstatements * 100) / report.project.metrics.statements

}

class OmniCloverReportFileAdapter(
    private val reportFile: CloverFile,
    val root: File,
    val projectBuildDirectory: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniReportFileAdapter {
    override fun name(): String =
        projectBuildDirectory.toPath()
            .relativize(File(reportFile.path).toPath()).toString()

    override fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile? {
        logger.info("- Processing file ${sourceCodeFile.absolutePath}")
        val sourceCodeText = sourceCodeFile.bufferedReader().use { it.readText() }
        val lines = sourceCodeText.split("\n").size
        val coverage = reportFile.cloverLines.fromCloverToCoverallsCoverage(lines)
        val branchCoverage = reportFile.cloverLines.fromCloverToBranchCoverageArray
        return if (coverage.isEmpty()) {
            null
        } else {
            try {
                CoverallsSourceFile(
                    name = sourceCodeFile.toRelativeString(root),
                    coverage = coverage,
                    branches = if (includeBranchCoverage) branchCoverage else emptyArray(),
                    sourceDigest = sourceCodeText.toFileDigest,
                )
            } catch (ex: java.lang.IllegalArgumentException) {
                null
            }
        }
    }


    override fun toCodacy(sourceCodeFile: SourceCodeFile, language: Language): CodacyFileReport? {
        val coverage = reportFile.cloverLines.fromCloverToCodacyCoverage
        return if (coverage.isEmpty() || language.ext.none {
                reportFile.path
                    .endsWith(it)
            }
        ) {
            null
        } else {
            CodacyFileReport(
                filename = "${sourceCodeFile.packageName}/${name()}".replace("//", "/"),
                total = reportFile.calculateLinePercentage,
                coverage = coverage
            )
        }
    }

    companion object {
        val logger = OmniLoggerConfig.getLogger(OmniCloverReportFileAdapter::class.java)
    }
}

class OmniCoveragePyReportParentFileAdapter(
    private val report: OmniCoveragePy,
    val root: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniReportParentFileAdapter {
    override fun parseAllFiles(): Sequence<Pair<String, List<OmniReportFileAdapter>>> =
        listOf("" to report.files.entries
            .map {
                OmniCoveragePyReportFileAdapter(
                    it,
                    root,
                    includeBranchCoverage = includeBranchCoverage
                )
            }).asSequence()


    override fun calculateTotalPercentage(): Int =
        (report.totals.coveredLines * 100) / report.totals.num_statements

}

class OmniCoveragePyReportFileAdapter(
    private val reportFile: Map.Entry<String, CoveragePyFile>,
    val root: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniReportFileAdapter {
    override fun name(): String = reportFile.key

    override fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile? {
        if (sourceCodeFile.absolutePath.endsWith("__init__.py") || sourceCodeFile.name.startsWith("_test")) {
            return null
        }
        val sourceCodeText = sourceCodeFile.bufferedReader().use { it.readText() }
        val lines = sourceCodeText.split("\n").size
        val coverage = reportFile.value.fromCoveragePyToCoverallsCoverage(lines)
        val branchCoverage = reportFile.value.fromCoveragePyToBranchCoverageArray
        return if (coverage.isEmpty()) {
            null
        } else {
            CoverallsSourceFile(
                name = sourceCodeFile.toRelativeString(root),
                coverage = coverage,
                branches = if (includeBranchCoverage) branchCoverage else emptyArray(),
                sourceDigest = sourceCodeText.toFileDigest,
            )
        }
    }


    override fun toCodacy(sourceCodeFile: SourceCodeFile, language: Language): CodacyFileReport? {
        val coverage = reportFile.value.fromCoveragePyToCodacyCoverage
        return if (coverage.isEmpty() || language.ext.none { reportFile.key.endsWith(it) }
        ) {
            null
        } else {
            CodacyFileReport(
                filename = "${sourceCodeFile.packageName}/${name()}".replace("//", "/"),
                total = reportFile.calculateLinePercentage,
                coverage = coverage
            )
        }
    }
}

