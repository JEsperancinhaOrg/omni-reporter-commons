package org.jesperancinha.plugins.omni.reporter.domain.reports

import org.jesperancinha.plugins.omni.reporter.LanguageNotConfiguredException
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyFileReport
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsSourceFile
import org.jesperancinha.plugins.omni.reporter.domain.api.isBranch
import org.jesperancinha.plugins.omni.reporter.parsers.Language
import org.jesperancinha.plugins.omni.reporter.parsers.toFileDigest
import org.jesperancinha.plugins.omni.reporter.transformers.SourceCodeFile
import java.io.File
import kotlin.math.max

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
        filterNotNull().associate { line -> line.lineNumber.toString() to if (line.hitCount > 0) 1 else 0 }.toMutableMap()
    } else {
        mutableMapOf()
    }


/**
 * Codacy Percentage Extension Function
 */
private val OmniLCovReport.calculateLinePercentage: Int
    get()  = (linesFound * 100)/ linesHit

/**
 * Codacy Percentage Extension Function
 */
private val OmniJacocoSourcefile.calculateLinePercentage: Int
    get() = counters.first { it.type == "LINE" }.run { (covered * 100) / (covered + missed) }

/**
 * Codacy Total Percentage Extension Function
 */
private val Report.calculateTotalPercentage: Int
    get() = counters.first { it.type == "LINE" }.run { (covered * 100) / (covered + missed) }


/**
 * Created by jofisaes on 28/01/2022
 */
interface OmniReportFileAdapter {
    fun name(): String
    fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile?
    fun toCodacy(sourceCodeFile: SourceCodeFile): CodacyFileReport?
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
    private val language: Language? = null
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
                        language
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
    private val language: Language? = null
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
            CoverallsSourceFile(
                name = sourceCodeFile.toRelativeString(root),
                coverage = coverage,
                branches = if (includeBranchCoverage) branchCoverage else emptyArray(),
                sourceDigest = sourceCodeText.toFileDigest,
            )
        }
    }

    override fun toCodacy(sourceCodeFile: SourceCodeFile): CodacyFileReport? {
        val coverage = reportFile.lines.fromJacocoToCodacyCoverage
        return if (coverage.isEmpty() || !reportFile.name.endsWith(
                language?.ext ?: throw LanguageNotConfiguredException()
            )
        ) {
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
        listOf("" to report.map { OmniLCovReportFileAdapter(it, root) }).asSequence()


    override fun calculateTotalPercentage(): Int =
        (report.sumOf { it.linesHit } * 100 )/ report.sumOf { it.linesFound }

}

class OmniLCovReportFileAdapter(
    private val reportFile: OmniLCovReport,
    val root: File,
    private val includeBranchCoverage: Boolean = false,
    private val language: Language? = null
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
            CoverallsSourceFile(
                name = sourceCodeFile.toRelativeString(root),
                coverage = coverage,
                branches = if (includeBranchCoverage) branchCoverage else emptyArray(),
                sourceDigest = sourceCodeText.toFileDigest,
            )
        }
    }

    override fun toCodacy(sourceCodeFile: SourceCodeFile): CodacyFileReport? {
        val coverage = reportFile.lineData.fromLCovToCodacyCoverage
        return if (coverage.isEmpty() || !reportFile.sourceFilePath.endsWith(
                language?.ext ?: throw LanguageNotConfiguredException()
            )
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
