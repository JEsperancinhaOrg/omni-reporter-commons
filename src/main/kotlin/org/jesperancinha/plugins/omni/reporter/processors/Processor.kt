package org.jesperancinha.plugins.omni.reporter.processors

import org.jesperancinha.plugins.omni.reporter.OmniProject
import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.reports.*
import java.io.File

private val CODECOV_SUPPORTED_REPORTS = arrayOf(
    "jacoco" to "xml",
    "lcov" to "txt",
    "lcov" to "info",
    "gcov" to "txt",
    "golang" to "txt",
    "lcov" to "txt",
    "coverage" to "xml",
    "coverage" to "json",
    "cobertura" to "xml",
    "clover" to "xml"
)

private val CODECOV_UNSUPPORTED_REPORTS = arrayOf(
    "coverage-final" to "json"
)


/**
 * Created by jofisaes on 05/01/2022
 */
abstract class Processor(
    ignoreTestBuildDirectory: Boolean,
) {
    abstract fun processReports()

    open fun reportNotFoundErrorMessage(): String? = null

    open fun reportNotSentErrorMessage(): String? = null

    internal val supportedPredicate = supportedPredicate(ignoreTestBuildDirectory)


    companion object {
        fun supportedPredicate(ignoreTestBuildDirectory: Boolean) =
            if (ignoreTestBuildDirectory) { testDirectory: String, report: File ->
                (report.parentFile.absolutePath == testDirectory ||
                        !report.absolutePath.contains(testDirectory))
            } else { _, _ -> true }
    }
}

internal fun List<OmniProject?>.toReportFiles(
    supportedPredicate: (String, File) -> Boolean,
    failOnXmlParseError: Boolean,
    root: File
): Map<OmniProject, List<OmniFileAdapter>> =
    this.filterNotNull()
        .map { project ->
            project to File(project.build?.directory ?: throw ProjectDirectoryNotFoundException())
                .walkTopDown()
                .toList()
                .mapNotNull { report -> mapReportFile(report, project, supportedPredicate, failOnXmlParseError, root) }
                .distinct()
        }.distinct()
        .toMap()

private fun mapReportFile(
    report: File,
    project: OmniProject,
    supportedPredicate: (String, File) -> Boolean,
    failOnXmlParseError: Boolean,
    root: File
) = if (report.isFile && project.build?.let { build ->
        supportedPredicate(
            build.testOutputDirectory,
            report
        )
    } == true) {
    val projectBuildDirectory =
        File(project.build?.directory ?: throw ProjectDirectoryNotFoundException())
    when {
        report.name.startsWith("jacoco") && report.extension == "xml" -> {
            OmniJacocoFileAdapter(
                report,
                failOnXmlParseError,
                root,
                projectBuildDirectory
            )
        }
        report.name.startsWith("") && report.extension == "exec" -> OmniJacocoExecFileAdapter(
            report,
            failOnXmlParseError,
            root,
            projectBuildDirectory
        )
        report.name.startsWith("lcov") && report.extension == "info" -> OmniLCovFileAdapter(
            report,
            failOnXmlParseError,
            root,
            projectBuildDirectory
        )
        report.name.startsWith("clover") && report.extension == "xml" -> OmniCloverFileAdapter(
            report,
            failOnXmlParseError,
            root,
            projectBuildDirectory
        )
        report.name.startsWith("coverage") && report.extension == "json" -> OmniCoveragePyFileAdapter(
            report,
            failOnXmlParseError,
            root,
            projectBuildDirectory
        )
        else -> null
    }?.let { if (it.isValid()) it else null }
} else null

internal fun List<OmniProject?>.toAllCodecovSupportedFiles(
    supportedPredicate: (String, File) -> Boolean,
    root: File
): List<Pair<OmniProject, List<OmniFileAdapter>>> =
    this.filterNotNull()
        .map { project ->
            project to File(project.build?.directory ?: throw ProjectDirectoryNotFoundException()).walkTopDown()
                .toList()
                .filter { report ->
                    report.isFile
                            && CODECOV_SUPPORTED_REPORTS.any { (name, ext) -> report.name.startsWith(name) && report.extension == ext }
                            && !CODECOV_UNSUPPORTED_REPORTS.any { (name, ext) -> report.name.startsWith(name) && report.extension == ext }
                            && project.build?.let { build ->
                        supportedPredicate(
                            build.testOutputDirectory,
                            report
                        )
                    } ?: false
                }
                .map { report ->
                    mapReportFile(report, project, supportedPredicate, false, root) ?: OmniGenericFileAdapter(report)
                }
                .distinct()
        }.distinct()
