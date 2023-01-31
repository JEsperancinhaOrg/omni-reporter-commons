package org.jesperancinha.plugins.omni.reporter.processors

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jesperancinha.plugins.omni.reporter.*
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsClient
import org.jesperancinha.plugins.omni.reporter.logger.OmniLoggerConfig
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import org.jesperancinha.plugins.omni.reporter.pipelines.PipelineImpl
import org.jesperancinha.plugins.omni.reporter.transformers.ReportingParserToCoveralls
import java.io.File

/**
 * Created by jofisaes on 06/01/2022
 */
class CoverallsProcessor (
    private val coverallsToken: String?,
    private val disableCoveralls: Boolean,
    private val coverallsUrl: String?,
    private val allProjects: List<OmniProject>,
    private val projectBaseDir: File?,
    private val failOnUnknown: Boolean,
    private val failOnReportNotFound: Boolean,
    private val failOnReportSending: Boolean,
    private val failOnXmlParseError: Boolean,
    private val fetchBranchNameFromEnv: Boolean,
    private val branchCoverage: Boolean,
    private val useCoverallsCount: Boolean,
    private val ignoreTestBuildDirectory: Boolean,
    private val reportRejectList: List<String>,
    private val currentPipeline: Pipeline = PipelineImpl.currentPipeline(fetchBranchNameFromEnv),
    private val parallelization: Int
) : Processor(ignoreTestBuildDirectory) {

    override fun processReports() {
        coverallsToken?.let { token ->
            if (!disableCoveralls) {
                logger.info("* Omni Reporting to Coveralls started!")


                val reportingParserToCoveralls =
                    ReportingParserToCoveralls(
                        token = token,
                        pipeline = currentPipeline,
                        root = projectBaseDir ?: throw ProjectDirectoryNotFoundException(),
                        failOnUnknown = failOnUnknown,
                        includeBranchCoverage = branchCoverage,
                        useCoverallsCount = useCoverallsCount,
                        failOnXmlParseError = failOnXmlParseError,
                    )

                allProjects.toReportFiles(supportedPredicate, failOnXmlParseError, projectBaseDir, reportRejectList, parallelization)
                    .filter { (project, _) -> project.compileSourceRoots != null }
                    .forEach { (project, reports) ->
                        runBlocking {
                            reports.chunked(parallelization).flatMap {
                                it.map { report ->
                                    async {
                                        logger.info("- Parsing file: ${report.report.absolutePath}")
                                        reportingParserToCoveralls.parseInput(
                                            report,
                                            project.compileSourceRoots?.map { file -> File(file) } ?: emptyList()
                                        )
                                    }
                                }.awaitAll()
                            }
                        }

                    }

                val coverallsClient =
                    CoverallsClient(coverallsUrl ?: throw CoverallsUrlNotConfiguredException(), token)
                try {

                    val coverallsReport = reportingParserToCoveralls.coverallsReport

                    coverallsReport?.let {
                        if (it.sourceFiles.isEmpty()) return
                    }

                    val response =
                        coverallsClient.submit(coverallsReport ?: let {
                            if (failOnReportNotFound) {
                                throw CoverallsReportNotGeneratedException(reportNotFoundErrorMessage())
                            } else {
                                logger.warn(reportNotFoundErrorMessage())
                                return
                            }
                        })

                    logger.info("* Omni Reporting to Coveralls complete!")
                    logger.info("- Response")
                    logger.info(response?.url)
                    logger.info(response?.message)
                } catch (ex: Exception) {
                    logger.error("Failed sending Coveralls report!", ex)
                    if (failOnReportSending) {
                        throw ex
                    }
                }
            }
        }
    }

    override fun reportNotFoundErrorMessage(): String {
        return "Coveralls report was not generated! This usually means that no jacoco.xml reports have been found."

    }

    companion object {
        private val logger = OmniLoggerConfig.getLogger(CoverallsProcessor::class.java)

        @JvmStatic
        fun createProcessor(
            coverallsToken: String?,
            disableCoveralls: Boolean,
            coverallsUrl: String?,
            locationsCSV: String,
            projectBaseDir: String?,
            failOnUnknown: Boolean,
            failOnReportNotFound: Boolean,
            failOnReportSendingError: Boolean,
            failOnXmlParsingError: Boolean,
            fetchBranchNameFromEnv: Boolean,
            branchCoverage: Boolean,
            ignoreTestBuildDirectory: Boolean,
            useCoverallsCount: Boolean,
            parallelization: Int,
            extraSourceFoldersCSV: String = "",
            extraReportFoldersCSV: String = "",
            reportRejectsCSV: String = ""
        ): CoverallsProcessor {
            val extraSourceFolders = extraSourceFoldersCSV.split(",").map { File(it) }
            val extraReportFolders = extraReportFoldersCSV.split(",").map { File(it) }
            val allOmniProjects =
                locationsCSV.toOmniProjects.plus(extraReportFolders.toExtraProjects(extraSourceFolders))
            return CoverallsProcessor(
                coverallsToken = coverallsToken,
                disableCoveralls = disableCoveralls,
                coverallsUrl = coverallsUrl,
                projectBaseDir = projectBaseDir?.let { File(it) } ?: throw ProjectDirectoryNotFoundException(),
                failOnUnknown = failOnUnknown,
                failOnReportNotFound = failOnReportNotFound,
                failOnReportSending = failOnReportSendingError,
                failOnXmlParseError = failOnXmlParsingError,
                fetchBranchNameFromEnv = fetchBranchNameFromEnv,
                branchCoverage = branchCoverage,
                useCoverallsCount = useCoverallsCount,
                ignoreTestBuildDirectory = ignoreTestBuildDirectory,
                allProjects = allOmniProjects,
                reportRejectList = reportRejectsCSV.split(","),
                parallelization = parallelization
            )
        }
    }
}