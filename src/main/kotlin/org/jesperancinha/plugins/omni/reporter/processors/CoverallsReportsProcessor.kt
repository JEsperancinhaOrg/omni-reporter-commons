package org.jesperancinha.plugins.omni.reporter.processors

import org.jesperancinha.plugins.omni.reporter.CoverallsReportNotGeneratedException
import org.jesperancinha.plugins.omni.reporter.CoverallsUrlNotConfiguredException
import org.jesperancinha.plugins.omni.reporter.OmniProject
import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsClient
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import org.jesperancinha.plugins.omni.reporter.transformers.ReportingParserToCoveralls
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Created by jofisaes on 06/01/2022
 */
class CoverallsReportsProcessor(
    private val coverallsToken: String,
    private val coverallsUrl: String?,
    private val currentPipeline: Pipeline,
    private val allProjects: List<OmniProject?>,
    private val projectBaseDir: File?,
    private val failOnUnknown: Boolean,
    private val failOnReportNotFound: Boolean,
    private val failOnReportSending: Boolean,
    private val failOnXmlParseError: Boolean,
    private val branchCoverage: Boolean,
    private val useCoverallsCount: Boolean,
    ignoreTestBuildDirectory: Boolean
) : Processor(ignoreTestBuildDirectory) {

    override fun processReports() {
        logger.info("* Omni Reporting to Coveralls started!")

        val reportingParserToCoveralls =
            ReportingParserToCoveralls(
                token = coverallsToken,
                pipeline = currentPipeline,
                root = projectBaseDir ?: throw ProjectDirectoryNotFoundException(),
                failOnUnknown = failOnUnknown,
                includeBranchCoverage = branchCoverage,
                useCoverallsCount = useCoverallsCount,
                failOnXmlParseError = failOnXmlParseError
            )

        allProjects.toReportFiles(supportedPredicate, failOnXmlParseError, projectBaseDir)
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
            CoverallsClient(coverallsUrl ?: throw CoverallsUrlNotConfiguredException(), coverallsToken)
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

    override fun reportNotFoundErrorMessage(): String {
        return "Coveralls report was not generated! This usually means that no jacoco.xml reports have been found."

    }

    companion object {
        private val logger = LoggerFactory.getLogger(CoverallsReportsProcessor::class.java)
    }
}