package org.jesperancinha.plugins.omni.reporter.processors

import org.jesperancinha.plugins.omni.reporter.CoverallsReportNotGeneratedException
import org.jesperancinha.plugins.omni.reporter.CoverallsUrlNotConfiguredException
import org.jesperancinha.plugins.omni.reporter.OmniProject
import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsClient
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import org.jesperancinha.plugins.omni.reporter.pipelines.PipelineImpl
import org.jesperancinha.plugins.omni.reporter.transformers.ReportingParserToCoveralls
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Created by jofisaes on 06/01/2022
 */
class CoverallsReportsProcessor(
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
    extraSourceFolders: List<File>,
    extraReportFolders: List<File>

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

                allProjects.toReportFiles(supportedPredicate, failOnXmlParseError, projectBaseDir, reportRejectList)
                    .filter { (project, _) -> project.compileSourceRoots != null }
                    .forEach { (project, reports) ->
                        reports.forEach { report ->
                            logger.info("- Parsing file: ${report.report.absolutePath}")
                            reportingParserToCoveralls.parseInput(
                                report,
                                project.compileSourceRoots?.map { file -> File(file) } ?: emptyList()
                            )
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
        private val logger = LoggerFactory.getLogger(CoverallsReportsProcessor::class.java)

        @JvmStatic
        fun createProcessor(
            coverallsToken: String?,
            disableCoveralls: Boolean,
            coverallsUrl: String?,
            locationsCSV: String,
            projectBaseDir: File?,
            failOnUnknown: Boolean,
            failOnReportNotFound: Boolean,
            failOnReportSendingError: Boolean,
            failOnXmlParsingError: Boolean,
            fetchBranchNameFromEnv: Boolean,
            branchCoverage: Boolean,
            ignoreTestBuildDirectory: Boolean,
            useCoverallsCount: Boolean,
            extraSourceFoldersCSV: String = "",
            extraReportFoldersCSV: String = "",
            reportRejectsCSV: String
        ): CoverallsReportsProcessor {
            val allOmniProjects = locationsCSV.toOmniProjects
            return CoverallsReportsProcessor(
                coverallsToken = coverallsToken,
                disableCoveralls = disableCoveralls,
                coverallsUrl = coverallsUrl,
                projectBaseDir = projectBaseDir,
                failOnUnknown = failOnUnknown,
                failOnReportNotFound = failOnReportNotFound,
                failOnReportSending = failOnReportSendingError,
                failOnXmlParseError = failOnXmlParsingError,
                fetchBranchNameFromEnv = fetchBranchNameFromEnv,
                branchCoverage = branchCoverage,
                useCoverallsCount = useCoverallsCount,
                ignoreTestBuildDirectory = ignoreTestBuildDirectory,
                allProjects = allOmniProjects,
                extraSourceFolders = extraSourceFoldersCSV.split(",").map { File(it) },
                extraReportFolders = extraReportFoldersCSV.split(",").map { File(it) },
                reportRejectList = reportRejectsCSV.split(",")
            )
        }
    }
}