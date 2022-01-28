package org.jesperancinha.plugins.omni.reporter.transformers

import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyApiTokenConfig
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyFileReport
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyReport
import org.jesperancinha.plugins.omni.reporter.domain.reports.*
import org.jesperancinha.plugins.omni.reporter.parsers.Language
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import java.io.File
import java.io.InputStream


private val OmniJacocoSourcefile.calculateLinePercentage: Int
    get() = counters.first { it.type == "LINE" }.run { (covered * 100) / (covered + missed) }

private val Report.calculateTotalPercentage: Int
    get() = counters.first { it.type == "LINE" }.run { (covered * 100) / (covered + missed) }

val List<Line?>.toCodacyCoverage: MutableMap<String, Int>
    get() = if (isNotEmpty()) {
        filterNotNull().associate { line -> line.nr.toString() to if (line.ci > 0) 1 else 0 }.toMutableMap()
    } else {
        mutableMapOf()
    }

/**
 * Created by jofisaes on 05/01/2022
 */
class JacocoParserToCodacy(
    token: String? = null,
    apiToken: CodacyApiTokenConfig? = null,
    pipeline: Pipeline,
    root: File,
    failOnUnknown: Boolean,
    private val failOnXmlParseError: Boolean = false,
    private val language: Language
) : OmniReporterParserImpl<InputStream, CodacyReport>(
    token = token, apiToken = apiToken, pipeline = pipeline, root = root
) {

    private var codacyReport: CodacyReport? = null

    private var codacySources = mutableMapOf<String, CodacyFileReport>()

    val failOnUnknownPredicate = createFailOnUnknownPredicate(failOnUnknown)

    /**
     * Comparison is based on the size. If there is a missmatch then the size is different.
     */
    private val failOnUnknownPredicateFilePack = createFailOnUnknownPredicateFilePack(failOnUnknown)

    override fun parseInput(input: InputStream, compiledSourcesDirs: List<File>): CodacyReport {
        val report = input.readJacocoReport(failOnXmlParseError)
        return report.packages
            .asSequence()
            .map { it.name to it.sourcefiles.map { OmniJacocoReportFileAdapter(it) } }
            .mapToGenericSourceCodeFiles(compiledSourcesDirs, failOnUnknownPredicateFilePack)
            .filter { (sourceCodeFile) -> failOnUnknownPredicate(sourceCodeFile) }
            .map { (sourceCodeFile, sourceFile) ->
                val coverage = sourceFile.lines.toCodacyCoverage
                if (coverage.isEmpty() || !sourceFile.name.endsWith(language.ext)) {
                    null
                } else {
                    CodacyFileReport(
                        filename = "${sourceCodeFile.packageName}/${sourceFile.name}".replace("//", "/"),
                        total = sourceFile.calculateLinePercentage,
                        coverage = coverage
                    )
                }
            }
            .filterNotNull()
            .toList()
            .let { fileReports ->
                val keys = codacySources.keys
                val (existing, nonExisting) = fileReports.partition { source -> keys.contains(source.filename) }
                existing.forEach { source ->
                    ((codacySources[source.filename] mergeCodacySourceTo source).also {
                        codacySources[source.filename] = it
                    })
                }
                nonExisting.forEach { codacySources[it.filename] = it }
                if (codacyReport == null) {
                    codacyReport = CodacyReport(
                        total = report.calculateTotalPercentage,
                        fileReports = fileReports.toTypedArray()
                    )
                } else {
                    val fileReportValues = codacySources.values.toList()
                    val fileReportsNew = fileReportValues.toTypedArray()
                    codacyReport =
                        codacyReport?.copy(
                            fileReports = fileReportsNew
                        )
                }

                codacyReport ?: throw ProjectDirectoryNotFoundException()
            }
    }
}

private infix fun CodacyFileReport?.mergeCodacySourceTo(source: CodacyFileReport): CodacyFileReport {
    this?.coverage?.entries?.forEach { (line, cov) ->
        val origCov = source.coverage[line] ?: 0
        source.coverage[line] = cov or origCov
    }
    return source
}