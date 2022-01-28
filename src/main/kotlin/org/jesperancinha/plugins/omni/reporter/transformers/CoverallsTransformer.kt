package org.jesperancinha.plugins.omni.reporter.transformers

import org.jesperancinha.plugins.omni.reporter.CoverallsTokenNotFoundException
import org.jesperancinha.plugins.omni.reporter.NullSourceFileException
import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsReport
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsSourceFile
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniJacocoReportParentFileAdapter
import org.jesperancinha.plugins.omni.reporter.domain.reports.readJacocoReport
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import java.io.File
import java.io.InputStream
import kotlin.math.max

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
        OmniJacocoReportParentFileAdapter(
            input.readJacocoReport(failOnXmlParseError),
            root,
            includeBranchCoverage,
        ).parseAllFiles()
            .mapToGenericSourceCodeFiles(compiledSourcesDirs, failOnUnknownPredicateFilePack)
            .filter { (sourceCodeFile) -> failOnUnknownPredicate(sourceCodeFile) }
            .map { (sourceCodeFile, omniReportFileAdapter) -> omniReportFileAdapter.toCoveralls(sourceCodeFile) }
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
