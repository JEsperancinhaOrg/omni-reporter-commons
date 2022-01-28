package org.jesperancinha.plugins.omni.reporter.transformers

import org.jesperancinha.plugins.omni.reporter.CoverallsTokenNotFoundException
import org.jesperancinha.plugins.omni.reporter.NullSourceFileException
import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsReport
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsSourceFile
import org.jesperancinha.plugins.omni.reporter.domain.api.isBranch
import org.jesperancinha.plugins.omni.reporter.domain.reports.Line
import org.jesperancinha.plugins.omni.reporter.domain.reports.readJacocoPackages
import org.jesperancinha.plugins.omni.reporter.parsers.toFileDigest
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import java.io.File
import java.io.InputStream
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

class JacocoParserToCoveralls(
    token: String,
    pipeline: Pipeline,
    root: File,
    failOnUnknown: Boolean,
    val failOnXmlParseError: Boolean = false,
    includeBranchCoverage: Boolean,
    val useCoverallsCount: Boolean
) :
    OmniReporterParserImpl<InputStream, CoverallsReport>(
        token = token, pipeline = pipeline, root = root, includeBranchCoverage = includeBranchCoverage
    ) {

    internal var coverallsReport: CoverallsReport? = null

    private var coverallsSources = mutableMapOf<String, CoverallsSourceFile>()

    val failOnUnknownPredicate = createFailOnUnknownPredicate(failOnUnknown)

    /**
     * Comparison is based on the size. If there is a missmatch then the size is different.
     */
    private val failOnUnknownPredicateFilePack = createFailOnUnknownPredicateFilePack(failOnUnknown)

    override fun parseInput(input: InputStream, compiledSourcesDirs: List<File>): CoverallsReport =
        input.readJacocoPackages(failOnXmlParseError)
            .asSequence()
            .map { it.name to it.sourcefiles }
            .mapToGenericSourceCodeFiles(compiledSourcesDirs, failOnUnknownPredicateFilePack)
            .filter { (sourceCodeFile) -> failOnUnknownPredicate(sourceCodeFile) }
            .map { (sourceCodeFile, sourceFile) ->
                val sourceCodeText = sourceCodeFile.bufferedReader().use { it.readText() }
                val lines = sourceCodeText.split("\n").size
                val coverage = sourceFile.lines.toCoverallsCoverage(lines)
                val branchCoverage = sourceFile.lines.toBranchCoverageArray
                if (coverage.isEmpty()) {
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
            .filterNotNull()
            .toList()
            .let { sourceFiles ->
                val keys = coverallsSources.keys
                val (existing, nonExisting) = sourceFiles.partition { source -> keys.contains(source.name) }
                existing.forEach { source ->
                    ((coverallsSources[source.name] mergeCoverallsSourceTo source).also {
                        coverallsSources[source.name] = it
                    })
                }
                nonExisting.forEach { coverallsSources[it.name] = it }
                if (coverallsReport == null) {
                    coverallsReport = CoverallsReport(
                        repoToken = token ?: throw CoverallsTokenNotFoundException(),
                        serviceName = pipeline.serviceName,
                        serviceNumber = if (useCoverallsCount) null else pipeline.serviceNumber,
                        serviceJobId = if (useCoverallsCount) null else pipeline.serviceJobId,
                        sourceFiles = sourceFiles.toMutableList(),
                        git = gitRepository.git
                    )
                } else {
                    coverallsReport?.sourceFiles?.clear()
                    coverallsReport?.sourceFiles?.addAll(coverallsSources.values)
                }

                coverallsReport ?: throw ProjectDirectoryNotFoundException()
            }
}

private infix fun CoverallsSourceFile?.mergeCoverallsSourceTo(source: CoverallsSourceFile): CoverallsSourceFile {
    val nextSize = max(source.coverage.size, this?.coverage?.size ?: throw NullSourceFileException())
    val newCoverage = arrayOfNulls<Int>(nextSize)
    source.coverage.forEachIndexed { index, value -> newCoverage[index] = value }
    this.coverage.forEachIndexed { index, value ->
        newCoverage[index] = newCoverage[index]?.let { it + (value ?: 0) } ?: value
    }
    return source.copy(coverage = newCoverage)
}
