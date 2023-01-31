package org.jesperancinha.plugins.omni.reporter.processors

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.lib.RepositoryBuilder
import org.jesperancinha.plugins.omni.reporter.*
import org.jesperancinha.plugins.omni.reporter.domain.api.CodecovClient
import org.jesperancinha.plugins.omni.reporter.domain.api.redact
import org.jesperancinha.plugins.omni.reporter.logger.OmniLoggerConfig
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import org.jesperancinha.plugins.omni.reporter.pipelines.PipelineImpl
import org.jesperancinha.plugins.omni.reporter.transformers.AllParserToCodecov
import java.io.File

private const val CODECOV_EOF = "\n<<<<<< EOF\n"

/**
 * Created by jofisaes on 09/01/2022
 */
class CodecovProcessor(
    private val codecovToken: String?,
    private val disableCodecov: Boolean,
    private val codecovUrl: String?,
    private val projectBaseDir: File,
    private val failOnReportNotFound: Boolean,
    private val failOnReportSending: Boolean,
    private val failOnUnknown: Boolean,
    private val fetchBranchNameFromEnv: Boolean,
    private val ignoreTestBuildDirectory: Boolean,
    private val currentPipeline: Pipeline = PipelineImpl.currentPipeline(fetchBranchNameFromEnv),
    private val allProjects: List<OmniProject?>,
    private val reportRejectList: List<String>,
    private val parallelization: Int
) : Processor(ignoreTestBuildDirectory) {
    override fun processReports() {
        codecovToken?.let { token ->
            if (!disableCodecov)
                logger.info("* Omni Reporting to Codecov started!")

            val repo = RepositoryBuilder().findGitDir(projectBaseDir).build()
            val codacyReportsAggregate =
                allProjects.toAllCodecovSupportedFiles(supportedPredicate, projectBaseDir, reportRejectList, parallelization)
                    .filter { (project, _) -> project.compileSourceRoots != null }
                    .flatMap { (project, reports) ->
                        runBlocking {
                            reports.chunked(parallelization).flatMap {
                                it.map { report ->
                                    async {
                                        logger.info("- Parsing file: ${report.report.absolutePath}")
                                        AllParserToCodecov(
                                            token = token,
                                            pipeline = currentPipeline,
                                            root = projectBaseDir,
                                            failOnUnknown = failOnUnknown
                                        ).parseInput(
                                            report,
                                            project.compileSourceRoots?.map { file -> File(file) } ?: emptyList()
                                        )
                                    }
                                }.awaitAll()
                            }
                        }
                    }
                    .joinToString(CODECOV_EOF)
                    .plus(CODECOV_EOF)

            if (codacyReportsAggregate.trim().isEmpty()) {
                if (failOnReportNotFound) {
                    throw CodacyReportNotGeneratedException(reportNotFoundErrorMessage())
                } else {
                    logger.warn(reportNotFoundErrorMessage())
                    return
                }
            }
            val codecovClient = CodecovClient(
                url = codecovUrl ?: throw CodecovUrlNotConfiguredException(),
                token = token,
                pipeline = currentPipeline,
                repo = repo
            )

            try {
                val response =
                    codecovClient.submit(codacyReportsAggregate)

                logger.info("* Omni Reporting to Codecov complete!")
                logger.info("- Response")
                logger.info(response)
            } catch (ex: Exception) {
                val coverException = Exception(ex.message?.redact(token), ex.cause)
                logger.error(reportNotSentErrorMessage(), coverException)
                if (failOnReportSending) {
                    throw coverException
                }
            }

        }
    }

    override fun reportNotFoundErrorMessage(): String {
        return "Codacy report was not generated! This usually means that no reports have been found."
    }

    override fun reportNotSentErrorMessage(): String {
        return "Failed sending Codacy report!"
    }

    companion object {
        val logger = OmniLoggerConfig.getLogger(CodecovProcessor::class.java)

        @JvmStatic
        fun createProcessor(
            codecovToken: String?,
            disableCodecov: Boolean,
            codecovUrl: String?,
            locationsCSV: String,
            projectBaseDir: String?,
            failOnUnknown: Boolean,
            failOnReportNotFound: Boolean,
            failOnReportSendingError: Boolean,
            fetchBranchNameFromEnv: Boolean,
            ignoreTestBuildDirectory: Boolean,
            parallelization: Int,
            extraSourceFoldersCSV: String = "",
            extraReportFoldersCSV: String = "",
            reportRejectsCSV: String = ""
        ): CodecovProcessor {
            val extraSourceFolders = extraSourceFoldersCSV.split(",").map { File(it) }
            val extraReportFolders = extraReportFoldersCSV.split(",").map { File(it) }
            val allOmniProjects =
                locationsCSV.toOmniProjects.plus(extraReportFolders.toExtraProjects(extraSourceFolders))

            return CodecovProcessor(
                codecovToken = codecovToken,
                disableCodecov = disableCodecov,
                codecovUrl = codecovUrl,
                projectBaseDir = projectBaseDir?.let { File(it) } ?: throw ProjectDirectoryNotFoundException(),
                failOnReportNotFound = failOnReportNotFound,
                failOnReportSending = failOnReportSendingError,
                failOnUnknown = failOnUnknown,
                fetchBranchNameFromEnv = fetchBranchNameFromEnv,
                ignoreTestBuildDirectory = ignoreTestBuildDirectory,
                allProjects = allOmniProjects,
                reportRejectList = reportRejectsCSV.split(","),
                parallelization = parallelization
            )
        }
    }
}