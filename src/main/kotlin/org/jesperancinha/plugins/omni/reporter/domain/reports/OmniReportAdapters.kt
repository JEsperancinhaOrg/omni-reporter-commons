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
 * Coveralls Branch Extension Function
 */
private val List<Line>.toBranchCoverageArray: Array<Int?>
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
 * Coveralls Line Extension Function
 */
private fun List<Line?>.toCoverallsCoverage(lines: Int): Array<Int?> = let {
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
 * Codacy Line Extension Function
 */
val List<Line?>.toCodacyCoverage: MutableMap<String, Int>
    get() = if (isNotEmpty()) {
        filterNotNull().associate { line -> line.nr.toString() to if (line.ci > 0) 1 else 0 }.toMutableMap()
    } else {
        mutableMapOf()
    }

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
        val coverage = reportFile.lines.toCoverallsCoverage(lines)
        val branchCoverage = reportFile.lines.toBranchCoverageArray
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
        val coverage = reportFile.lines.toCodacyCoverage
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


