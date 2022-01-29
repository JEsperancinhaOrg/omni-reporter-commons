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
    "cobertura" to "xml"
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

    val supportedPredicate =
        if (ignoreTestBuildDirectory) { testDirectory: String, report: File ->
            !report.absolutePath.contains(testDirectory)
        } else { _, _ -> true }
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
                .mapNotNull { report ->
                    if (report.isFile && project.build?.let { build ->
                            supportedPredicate(
                                build.testOutputDirectory,
                                report
                            )
                        } == true) {
                        val projectBuildDirectory = File(project.build?.directory ?: throw ProjectDirectoryNotFoundException())
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
                                projectBuildDirectory                            )
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
                        }
                    } else null

                }
                .distinct()
        }.distinct()
        .toMap()

internal fun List<OmniProject?>.toAllCodecovSupportedFiles(supportedPredicate: (String, File) -> Boolean): List<Pair<OmniProject, List<OmniGenericFileAdapter>>> =
    this.filterNotNull()
        .map { project ->
            project to File(project.build?.directory ?: throw ProjectDirectoryNotFoundException()).walkTopDown()
                .toList()
                .filter { report ->
                    report.isFile
                            && CODECOV_SUPPORTED_REPORTS.any { (name, ext) -> report.name.startsWith(name) && report.extension == ext }
                            && project.build?.let { build ->
                        supportedPredicate(
                            build.testOutputDirectory,
                            report
                        )
                    } ?: false
                }
                .map { OmniGenericFileAdapter(it) }
                .distinct()
        }.distinct()
