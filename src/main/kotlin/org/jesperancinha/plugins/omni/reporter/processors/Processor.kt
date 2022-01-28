package org.jesperancinha.plugins.omni.reporter.processors

import org.jesperancinha.plugins.omni.reporter.OmniProject
import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import java.io.File

private val String.isSupported: Boolean
    get() = equals("xml")

private val CODECOV_SUPPORTED_REPORTS = arrayOf(
    "jacoco" to "xml",
    "lcov" to "txt",
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

internal fun List<OmniProject?>.toJacocoReportFiles(supportedPredicate: (String, File) -> Boolean): Map<OmniProject, List<File>> =
    this.filterNotNull()
        .map { project ->
            project to File(project.build?.directory ?: throw ProjectDirectoryNotFoundException())
                .walkTopDown()
                .toList()
                .filter { report ->
                    report.isFile
                            && report.name.startsWith("jacoco")
                            && report.extension.isSupported
                            && project.build?.let { build ->
                        supportedPredicate(
                            build.testOutputDirectory,
                            report
                        )
                    } ?: false
                }
                .distinct()
        }.distinct()
        .toMap()

internal fun List<OmniProject?>.toAllCodecovSupportedFiles(supportedPredicate: (String, File) -> Boolean): List<Pair<OmniProject, List<File>>> =
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
                }.distinct()
        }.distinct()
