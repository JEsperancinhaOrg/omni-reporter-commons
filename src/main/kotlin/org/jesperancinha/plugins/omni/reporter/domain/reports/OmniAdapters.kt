package org.jesperancinha.plugins.omni.reporter.domain.reports

import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsSourceFile
import org.jesperancinha.plugins.omni.reporter.domain.api.isBranch
import org.jesperancinha.plugins.omni.reporter.parsers.toFileDigest
import org.jesperancinha.plugins.omni.reporter.transformers.SourceCodeFile
import java.io.File
import kotlin.math.max


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
 * Created by jofisaes on 28/01/2022
 */
interface OmniReportFileAdapter {
    fun name(): String
    fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile?
}

class OmniJacocoReportFileAdapter(
    private val reportFile: OmniJacocoSourcefile,
    val root: File,
    private val includeBranchCoverage: Boolean = false
) : OmniReportFileAdapter {
    override fun name() = reportFile.name
    override fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile? {
        val sourceCodeText = sourceCodeFile.bufferedReader().use { it.readText() }
        val lines = sourceCodeText.split("\n").size
        val coverage = reportFile.lines.toCoverallsCoverage(lines)
        val branchCoverage = reportFile.lines.toBranchCoverageArray
        if (coverage.isEmpty()) {
            return null
        } else {
            return CoverallsSourceFile(
                name = sourceCodeFile.toRelativeString(root),
                coverage = coverage,
                branches = if (includeBranchCoverage) branchCoverage else emptyArray(),
                sourceDigest = sourceCodeText.toFileDigest,
            )
        }
    }
}

