package org.jesperancinha.plugins.omni.reporter.processors

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jesperancinha.plugins.omni.reporter.OmniBuildGeneric
import org.jesperancinha.plugins.omni.reporter.OmniProject
import org.jesperancinha.plugins.omni.reporter.OmniProjectGeneric
import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.reports.*
import java.io.File


val String.toOmniProjects: List<OmniProject>
    get() = split(",")
        .map {
            OmniProjectGeneric(
                compileSourceRoots = this.split(",").toMutableList(),
                build = OmniBuildGeneric(
                    it,
                    it
                )
            )
        }

private val KNOWN_TEST_DIRECTORIES = arrayOf(
    "test-classes"
)

private val STATIC_REJECT_FOLDERS = arrayOf("node_modules")

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

        val nonGenericTestDirectoryPredicate =
            { report: File -> KNOWN_TEST_DIRECTORIES.none { report.absolutePath.contains(it) } }


        val nonTestDirectoryPredicate = { testDirectory: String, report: File ->
            (report.parentFile.absolutePath == testDirectory ||
                    !report.absolutePath.contains(testDirectory))
        }

        fun supportedPredicate(ignoreTestBuildDirectory: Boolean) =
            if (ignoreTestBuildDirectory) { testDirectory: String, report: File ->
                nonGenericTestDirectoryPredicate(report) &&
                        nonTestDirectoryPredicate(testDirectory, report)
            } else { _, _ -> true }
    }
}


enum class ReportType(
    val extension: String,
    val validation: (File) -> Boolean,
    val createTypedReportFileAdapter: (File, Boolean, File, File) -> OmniFileAdapter
) {
    JACOCO("xml",
        {
            val readText = it.readText().trim()
            readText.startsWith("<") && readText.endsWith(">") && readText.contains("report name") && readText.contains(
                "package name"
            )
        },
        { file,
          failOnXmlParseError,
          root,
          projectBuildDirectory ->
            OmniJacocoFileAdapter(
                file,
                failOnXmlParseError,
                root,
                projectBuildDirectory
            )
        }),
    JACOCO_EXEC("exec", { true },
        { file,
          failOnXmlParseError,
          root,
          projectBuildDirectory ->
            OmniJacocoExecFileAdapter(
                file,
                failOnXmlParseError,
                root,
                projectBuildDirectory
            )
        }),
    LCOV("info",
        {
            val readText = it.readText()
            readText.startsWith("TN") && readText.contains("\nSF:") && readText.contains("\nend_of_record")
        },
        { file,
          failOnXmlParseError,
          root,
          projectBuildDirectory ->
            OmniLCovFileAdapter(
                file,
                failOnXmlParseError,
                root,
                projectBuildDirectory
            )
        }),
    CLOVER("xml",
        {
            val readText = it.readText().trim()
            readText.startsWith("<") && readText.endsWith(">") && readText.contains("coverage generated") && readText.contains(
                "project timestamp"
            )
        },
        { file,
          failOnXmlParseError,
          root,
          projectBuildDirectory ->
            OmniCloverFileAdapter(
                file,
                failOnXmlParseError,
                root,
                projectBuildDirectory
            )
        }),
    COVERAGE_PY("json",
        {
            val readText = it.readText().trim()
            readText.startsWith("{") && readText.endsWith("}") && readText.contains("\"meta\":") && readText.contains("\"files\":")
        },
        { file,
          failOnXmlParseError,
          root,
          projectBuildDirectory ->
            OmniCoveragePyFileAdapter(
                file,
                failOnXmlParseError,
                root,
                projectBuildDirectory
            )
        });

    companion object {
        fun createReportFileAdapter(
            file: File,
            failOnXmlParseError: Boolean = false,
            root: File,
            projectBuildDirectory: File,
        ): OmniFileAdapter? =
            values().firstOrNull { file.extension == it.extension && it.validation(file) }?.let {
                it.createTypedReportFileAdapter(file, failOnXmlParseError, root, projectBuildDirectory)
            }
    }
}

private fun notRejectable(file: File) =
    STATIC_REJECT_FOLDERS.none { rejectFolder -> file.absolutePath.contains(rejectFolder) }

internal fun List<OmniProject?>.toReportFiles(
    supportedPredicate: (String, File) -> Boolean,
    failOnXmlParseError: Boolean,
    root: File,
    reportRejectList: List<String>,
    parallelization: Int
): Map<OmniProject, List<OmniFileAdapter>> =
    this.filterNotNull()
        .map { project ->
            project to File(project.build?.directory ?: throw ProjectDirectoryNotFoundException())
                .walkTopDown()
                .let { walk ->
                    runBlocking {
                        walk
                            .chunked(parallelization)
                            .map { fileList ->
                                async {
                                    fileList.filter { notRejectable(it) && !reportRejectList.contains(it.name) }
                                }
                            }.toList().awaitAll()
                    }
                }
                .flatten()
                .toList()
                .mapNotNull { report ->
                    mapReportFile(report, project, supportedPredicate, failOnXmlParseError, root)
                }
                .distinct()
        }.distinct()
        .toMap()

internal fun List<OmniProject?>.toAllCodecovSupportedFiles(
    supportedPredicate: (String, File) -> Boolean,
    root: File,
    reportRejectList: List<String>,
    parallelization: Int
): List<Pair<OmniProject, List<OmniFileAdapter>>> =
    this.filterNotNull()
        .map { project ->
            project to File(project.build?.directory ?: throw ProjectDirectoryNotFoundException())
                .walkTopDown()
                .let { walk ->
                    runBlocking {
                        walk
                            .chunked(parallelization)
                            .map { fileList ->
                                async {
                                    fileList.filter { notRejectable(it) && !reportRejectList.contains(it.name) }
                                }
                            }.toList().awaitAll()
                    }
                }
                .asSequence()
                .flatten()
                .toList()
                .filter { report ->
                    report.isFile
                            && project.build?.let { build ->
                        supportedPredicate(
                            build.testOutputDirectory,
                            report
                        )
                    } ?: false
                }
                .mapNotNull { report ->
                    mapReportFile(report, project, supportedPredicate, false, root)
                }
                .distinct()
                .toList()
        }.distinct()

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
    val projectBuildDirectory = File(project.build?.directory ?: throw ProjectDirectoryNotFoundException())
    ReportType.createReportFileAdapter(
        report, failOnXmlParseError, root, projectBuildDirectory
    )
} else null
