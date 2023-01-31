package org.jesperancinha.plugins.omni.reporter.processors

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.jesperancinha.plugins.omni.reporter.*
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyApiTokenConfig
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyClient
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyReport
import org.jesperancinha.plugins.omni.reporter.domain.api.redact
import org.jesperancinha.plugins.omni.reporter.logger.OmniLoggerConfig
import org.jesperancinha.plugins.omni.reporter.parsers.Language
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import org.jesperancinha.plugins.omni.reporter.pipelines.PipelineImpl
import org.jesperancinha.plugins.omni.reporter.transformers.JacocoParserToCodacy
import java.io.File

/**
 * Created by jofisaes on 07/01/2022
 */
class CodacyProcessor(
    private val codacyToken: String?,
    val codacyApiToken: String?,
    val codacyOrganizationProvider: String?,
    val codacyUsername: String?,
    val codacyProjectName: String?,
    val disableCodacy: Boolean,
    private val codacyUrl: String?,
    private val allProjects: List<OmniProject?>,
    private val projectBaseDir: File?,
    private val failOnReportNotFound: Boolean,
    private val failOnReportSending: Boolean,
    private val failOnUnknown: Boolean,
    private val failOnXmlParseError: Boolean,
    private val fetchBranchNameFromEnv: Boolean,
    private val ignoreTestBuildDirectory: Boolean,
    private val reportRejectList: List<String>,
    private val currentPipeline: Pipeline = PipelineImpl.currentPipeline(fetchBranchNameFromEnv),
    private val parallelization: Int
) : Processor(ignoreTestBuildDirectory) {
    override fun processReports() {
        logger.info("Codacy API fully configured: ${this.isCodacyAPIConfigured}")
        if ((this.isCodacyAPIConfigured || codacyToken != null) && !disableCodacy) {
            val apiToken = codacyApiToken?.let {
                CodacyApiTokenConfig(
                    codacyApiToken = codacyApiToken,
                    codacyOrganizationProvider = codacyOrganizationProvider
                        ?: throw IncompleteCodacyApiTokenConfigurationException(),
                    codacyUsername = codacyUsername ?: throw IncompleteCodacyApiTokenConfigurationException(),
                    codacyProjectName = codacyProjectName ?: throw IncompleteCodacyApiTokenConfigurationException()
                )
            }
            logger.info("* Omni Reporting to Codacy started!")

            val repo = RepositoryBuilder().findGitDir(projectBaseDir).build()

            Language.values().forEach { language ->
                val reportsPerLanguage = allProjects.toReportFiles(
                    supportedPredicate,
                    failOnXmlParseError,
                    projectBaseDir ?: throw ProjectDirectoryNotFoundException(),
                    reportRejectList,
                    parallelization
                )
                    .filter { (project, _) -> project.compileSourceRoots != null }
                    .flatMap { (project, reports) ->
                        runBlocking {
                            reports.chunked(parallelization).flatMap {
                                it.map { report ->
                                    async {
                                        logger.info("- Parsing file: ${report.report.absolutePath}")
                                        JacocoParserToCodacy(
                                            token = codacyToken,
                                            apiToken = apiToken,
                                            pipeline = currentPipeline,
                                            root = projectBaseDir,
                                            failOnUnknown = failOnUnknown,
                                            failOnXmlParseError = failOnXmlParseError,
                                            language = language
                                        ).parseInput(
                                            report,
                                            project.compileSourceRoots?.map { file -> File(file) } ?: emptyList()
                                        )
                                    }
                                }.awaitAll()
                            }
                        }
                    }
                    .filter {
                        it.fileReports.isNotEmpty()
                    }

                logger.info("- Found ${reportsPerLanguage.size} reports for language ${language.lang}")
                if (reportsPerLanguage.size > 1) {
                    reportsPerLanguage.forEach { codacyReport ->
                        sendCodacyReport(
                            language,
                            repo,
                            codacyReport,
                            apiToken,
                            true
                        )
                    }
                    val response = CodacyClient(
                        token = codacyToken,
                        apiToken = apiToken,
                        language = language,
                        url = codacyUrl ?: throw CodacyUrlNotConfiguredException(),
                        repo = repo
                    ).submitEndReport()
                    logger.info("- Response")
                    logger.info(response.success)
                } else if (reportsPerLanguage.size == 1) {
                    sendCodacyReport(language, repo, reportsPerLanguage[0], apiToken, false)
                }

                logger.info("* Omni Reporting processing for Codacy complete!")
            }

        }
    }


    private fun sendCodacyReport(
        language: Language,
        repo: Repository,
        codacyReport: CodacyReport,
        apiToken: CodacyApiTokenConfig?,
        partial: Boolean
    ) {
        try {
            val codacyClient = CodacyClient(
                token = codacyToken,
                apiToken = apiToken,
                language = language,
                url = codacyUrl ?: throw CodacyUrlNotConfiguredException(),
                repo = repo,
                partial = partial
            )
            val response =
                codacyClient.submit(codacyReport)
            logger.info("* Omni Reporting to Codacy for language $language complete!")
            logger.info("- Response")
            logger.info(response.success)
        } catch (ex: Exception) {
            val coverException = Exception(ex.message?.redact(codacyToken), ex.cause)
            logger.error("Failed sending Codacy report!", coverException)
            if (failOnReportSending) {
                throw coverException
            }
        }
    }

    companion object {
        private val logger = OmniLoggerConfig.getLogger(CodacyProcessor::class.java)

        @JvmStatic
        fun createProcessor(
            codacyToken: String?,
            codacyApiToken: String?,
            codacyOrganizationProvider: String?,
            codacyUsername: String?,
            codacyProjectName: String?,
            disableCodacy: Boolean,
            codacyUrl: String?,
            locationsCSV: String,
            projectBaseDir: String?,
            failOnUnknown: Boolean,
            failOnReportNotFound: Boolean,
            failOnReportSendingError: Boolean,
            failOnXmlParsingError: Boolean,
            fetchBranchNameFromEnv: Boolean,
            ignoreTestBuildDirectory: Boolean,
            parallelization: Int,
            extraSourceFoldersCSV: String = "",
            extraReportFoldersCSV: String = "",
            reportRejectsCSV: String = ""
        ): CodacyProcessor {
            val extraSourceFolders = extraSourceFoldersCSV.split(",").map { File(it) }
            val extraReportFolders = extraReportFoldersCSV.split(",").map { File(it) }
            val allOmniProjects =
                locationsCSV.toOmniProjects.plus(extraReportFolders.toExtraProjects(extraSourceFolders))
            return CodacyProcessor(
                codacyToken = codacyToken,
                codacyApiToken = codacyApiToken,
                codacyOrganizationProvider = codacyOrganizationProvider,
                codacyUsername = codacyUsername,
                codacyProjectName = codacyProjectName,
                disableCodacy = disableCodacy,
                codacyUrl = codacyUrl,
                projectBaseDir = projectBaseDir?.let { File(it) } ?: throw ProjectDirectoryNotFoundException(),
                failOnReportNotFound = failOnReportNotFound,
                failOnReportSending = failOnReportSendingError,
                failOnXmlParseError = failOnXmlParsingError,
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

private val CodacyProcessor.isCodacyAPIConfigured: Boolean
    get() = CodacyApiTokenConfig.isApiTokenConfigure(
        codacyApiToken = codacyApiToken,
        codacyOrganizationProvider = codacyOrganizationProvider,
        codacyUsername = codacyUsername,
        codacyProjectName = codacyProjectName
    )