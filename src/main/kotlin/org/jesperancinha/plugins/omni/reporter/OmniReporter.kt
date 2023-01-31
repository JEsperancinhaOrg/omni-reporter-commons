package org.jesperancinha.plugins.omni.reporter

import org.jesperancinha.plugins.omni.reporter.logger.OmniLoggerConfig
import org.jesperancinha.plugins.omni.reporter.parsers.readCamelCaseJsonValue
import org.jesperancinha.plugins.omni.reporter.processors.CodacyProcessor
import org.jesperancinha.plugins.omni.reporter.processors.CodecovProcessor
import org.jesperancinha.plugins.omni.reporter.processors.CoverallsProcessor
import java.io.File

class ProjectDirectoryNotFoundException : RuntimeException()

class CoverallsUrlNotConfiguredException : RuntimeException()

class CodacyUrlNotConfiguredException : RuntimeException()

class CodecovUrlNotConfiguredException : RuntimeException()

class CoverallsReportNotGeneratedException(override val message: String? = null) : RuntimeException()

class CodacyReportNotGeneratedException(override val message: String? = null) : RuntimeException()

class CodecovPackageNotFoundException(override val message: String? = null) : RuntimeException()

class JacocoXmlParsingErrorException : RuntimeException()

class CoveragePyJsonParsingErrorException : RuntimeException()

class NullSourceFileException : RuntimeException()

class IncompleteCodacyApiTokenConfigurationException : RuntimeException()

class CoverallsTokenNotFoundException : RuntimeException()

class LanguageNotConfiguredException : RuntimeException()

/**
 * Definition of the Project
 */
interface OmniProject {
    val compileSourceRoots: MutableList<String>?
    val build: OmniBuild?
}

/**
 * Definition of the Build
 */
interface OmniBuild {
    val testOutputDirectory: String
    val directory: String
}

fun List<OmniProject>.injectExtraSourceFiles(extraSourceFolders: List<File>, root: File): List<OmniProject> =
    this.map { project ->
        val extraSourcesFoldersFound =
            extraSourceFolders.filter { sourceFolder ->
                !root.toPath().relativize(sourceFolder.toPath()).toString().contains("..")
            }
        project.compileSourceRoots?.addAll(extraSourcesFoldersFound.map { foundSourceFolder -> foundSourceFolder.absolutePath }
            .toMutableList())
        project
    }

fun List<File>.toExtraProjects(extraSourceFolders: List<File>): List<OmniProject> =
    map {
        OmniProjectGeneric(
            extraSourceFolders.map { src -> src.absolutePath }.toMutableList(),
            OmniBuildGeneric(it.absolutePath, it.absolutePath)
        )
    }

class OmniProjectGeneric(
    override val compileSourceRoots: MutableList<String> = mutableListOf(),
    override val build: OmniBuildGeneric? = null
) : OmniProject {
    constructor(compileSourceRootsString: String, omniBuildGeneric: OmniBuildGeneric?)
            : this(compileSourceRootsString.split(",").toMutableList(), omniBuildGeneric)
}

class OmniBuildGeneric(
    override val testOutputDirectory: String = "",
    override val directory: String = ""
) : OmniBuild

open class OmniReporterCommon(
    private var coverallsUrl: String = HTTPS_COVERALLS_IO_API_V_1_JOBS,
    private var codacyUrl: String = HTTPS_API_CODACY_COM,
    private var codecovUrl: String = HTTPS_CODECOV_IO_UPLOAD,
    var sourceEncoding: String? = null,
    var projectBaseDir: File? = null,
    var failOnNoEncoding: Boolean = false,
    var failOnUnknown: Boolean = false,
    var failOnReportNotFound: Boolean = false,
    var failOnReportSendingError: Boolean = false,
    var failOnXmlParsingError: Boolean = false,
    var disableCoveralls: Boolean = false,
    var disableCodacy: Boolean = false,
    var disableCodecov: Boolean = false,
    var ignoreTestBuildDirectory: Boolean = true,
    var useCoverallsCount: Boolean = true,
    var branchCoverage: Boolean = false,
    var fetchBranchNameFromEnv: Boolean = false,
    var coverallsToken: String? = null,
    var codecovToken: String? = null,
    var codacyToken: String? = null,
    var codacyApiToken: String? = null,
    var codacyOrganizationProvider: String? = null,
    var codacyUsername: String? = null,
    var codacyProjectName: String? = null,
    val parallelization: Int = 4,
    val extraSourceFolders: List<File> = emptyList(),
    val extraReportFolders: List<File> = emptyList(),
    val reportRejectList: List<String> = emptyList()
) {
    constructor(omniConfig: OmniConfig) : this(
        omniConfig.coverallsUrl ?: HTTPS_COVERALLS_IO_API_V_1_JOBS,
        omniConfig.codacyUrl ?: HTTPS_API_CODACY_COM,
        omniConfig.codecovUrl ?: HTTPS_CODECOV_IO_UPLOAD,
        omniConfig.sourceEncoding,
        omniConfig.projectBaseDir?.let { File(it) } ?: File("."),
        omniConfig.failOnNoEncoding ?: false,
        omniConfig.failOnUnknown ?: false,
        omniConfig.failOnReportNotFound ?: false,
        omniConfig.failOnReportSendingError ?: false,
        omniConfig.failOnXmlParsingError ?: false,
        omniConfig.disableCoveralls ?: false,
        omniConfig.disableCodacy ?: false,
        omniConfig.disableCodecov ?: false,
        omniConfig.ignoreTestBuildDirectory ?: false,
        omniConfig.useCoverallsCount ?: true,
        omniConfig.branchCoverage ?: false,
        omniConfig.fetchBranchNameFromEnv ?: false,
        omniConfig.coverallsToken,
        omniConfig.codecovToken,
        omniConfig.codacyToken,
        omniConfig.codacyApiToken,
        omniConfig.codacyOrganizationProvider,
        omniConfig.codacyUsername,
        omniConfig.codacyProjectName,
        omniConfig.parallelization,
        omniConfig.extraSourceFolders?.let { it.split(",").map { path -> File(path) } } ?: emptyList(),
        omniConfig.extraReportFolders?.let { it.split(",").map { path -> File(path) } } ?: emptyList(),
        omniConfig.reportRejectList?.split(",") ?: emptyList()
    )

    fun execute(inputOmniProjects: List<OmniProject>) {
        logLine()
        logger.info(javaClass.getResourceAsStream("/banner.txt")?.bufferedReader().use { it?.readText() })
        logLine()

        val extraProjects = extraReportFolders.toExtraProjects(extraSourceFolders)
        val allOmniProjects = inputOmniProjects.plus(extraProjects)

        val environment = System.getenv()
        coverallsToken = (coverallsToken ?: environment["COVERALLS_REPO_TOKEN"]) ?: environment["COVERALLS_TOKEN"]
        codacyToken = codacyToken ?: environment["CODACY_PROJECT_TOKEN"]
        codacyApiToken = codacyApiToken ?: environment["CODACY_API_TOKEN"]
        codacyOrganizationProvider = codacyOrganizationProvider ?: environment["CODACY_ORGANIZATION_PROVIDER"]
        codacyUsername = codacyUsername ?: environment["CODACY_USERNAME"]
        codacyProjectName = codacyProjectName ?: environment["CODACY_PROJECT_NAME"]
        codecovToken = codecovToken ?: environment["CODECOV_TOKEN"]

        logLine()
        logger.info("Coveralls URL: $coverallsUrl")
        logger.info("Codacy URL: $codacyUrl")
        logger.info("Codecov URL: $codecovUrl")
        logger.info("Coveralls token: ${checkToken(coverallsToken)}")
        logger.info("Codecov token: ${checkToken(codecovToken)}")
        logger.info("Codacy token: ${checkToken(codacyToken)}")
        logger.info("Codacy API token: ${checkToken(codacyApiToken)}")
        logger.info("Source Encoding: $sourceEncoding")
        logger.info("Parent Directory: $projectBaseDir")
        logger.info("failOnNoEncoding: $failOnNoEncoding")
        logger.info("failOnUnknown: $failOnUnknown")
        logger.info("failOnReportNotFound: $failOnReportNotFound")
        logger.info("failOnReportSendingError: $failOnReportSendingError")
        logger.info("failOnXmlParsingError: $failOnXmlParsingError")
        logger.info("disableCoveralls: $disableCoveralls")
        logger.info("disableCodacy: $disableCodacy")
        logger.info("disableCodecov: $disableCodecov")
        logger.info("ignoreTestBuildDirectory: $ignoreTestBuildDirectory")
        logger.info("branchCoverage: $branchCoverage")
        logger.info("useCoverallsCount: $useCoverallsCount")
        logger.info("parallelization: $parallelization")
        logger.info("extraSourceFolders: ${extraSourceFolders.joinToString(";")}")
        logger.info("extraReportFolders: ${extraReportFolders.joinToString(";")}")
        logger.info("reportRejectList: ${reportRejectList.joinToString(";")}")
        logLine()

        CoverallsProcessor(
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
            ignoreTestBuildDirectory = ignoreTestBuildDirectory,
            useCoverallsCount = useCoverallsCount,
            allProjects = allOmniProjects,
            reportRejectList = reportRejectList,
            parallelization = parallelization
        ).processReports()

        CodacyProcessor(
            codacyToken = codacyToken,
            codacyApiToken = codacyApiToken,
            codacyOrganizationProvider = codacyOrganizationProvider,
            codacyUsername = codacyUsername,
            codacyProjectName = codacyProjectName,
            disableCodacy = disableCodacy,
            codacyUrl = codacyUrl,
            projectBaseDir = projectBaseDir,
            failOnReportNotFound = failOnReportNotFound,
            failOnReportSending = failOnReportSendingError,
            failOnXmlParseError = failOnXmlParsingError,
            failOnUnknown = failOnUnknown,
            fetchBranchNameFromEnv = fetchBranchNameFromEnv,
            ignoreTestBuildDirectory = ignoreTestBuildDirectory,
            allProjects = allOmniProjects,
            reportRejectList = reportRejectList,
            parallelization = parallelization
        ).processReports()

        CodecovProcessor(
            codecovToken = codecovToken,
            disableCodecov = disableCodecov,
            codecovUrl = codecovUrl,
            projectBaseDir = projectBaseDir ?: throw ProjectDirectoryNotFoundException(),
            failOnReportNotFound = failOnReportNotFound,
            failOnReportSending = failOnReportSendingError,
            failOnUnknown = failOnUnknown,
            fetchBranchNameFromEnv = fetchBranchNameFromEnv,
            ignoreTestBuildDirectory = ignoreTestBuildDirectory,
            allProjects = allOmniProjects,
            reportRejectList = reportRejectList,
            parallelization = parallelization
        ).processReports()

    }

    private fun logLine() = let {
        logger.info("*".repeat(OMNI_CHARACTER_LINE_NUMBER))
    }

    private fun checkToken(token: String?) = token?.let { "found" } ?: "not found"

    companion object {
        private val logger = OmniLoggerConfig.getLogger(OmniReporterCommon::class.java)

        private const val OMNI_CHARACTER_LINE_NUMBER = 150
        const val HTTPS_COVERALLS_IO_API_V_1_JOBS = "https://coveralls.io/api/v1/jobs"
        const val HTTPS_API_CODACY_COM = "https://api.codacy.com"
        const val HTTPS_CODECOV_IO_UPLOAD = "https://codecov.io/upload"

        @JvmStatic
        fun readOmniConfig() = readCamelCaseJsonValue<OmniConfig>(File("./omni-config.json").inputStream())

        @JvmStatic
        fun createReporterFromJsonConfig() = OmniReporterCommon(readOmniConfig())
    }
}

data class OmniConfig(
    val coverallsUrl: String? = null,
    val codacyUrl: String? = null,
    val codecovUrl: String? = null,
    val sourceEncoding: String? = null,
    val projectBaseDir: String? = null,
    val failOnNoEncoding: Boolean? = null,
    val failOnUnknown: Boolean? = null,
    val failOnReportNotFound: Boolean? = null,
    val failOnReportSendingError: Boolean? = null,
    val failOnXmlParsingError: Boolean? = null,
    val disableCoveralls: Boolean? = null,
    val disableCodacy: Boolean? = null,
    val disableCodecov: Boolean? = null,
    val ignoreTestBuildDirectory: Boolean? = null,
    val useCoverallsCount: Boolean? = null,
    val branchCoverage: Boolean? = null,
    val fetchBranchNameFromEnv: Boolean? = null,
    val coverallsToken: String? = null,
    val codecovToken: String? = null,
    val codacyToken: String? = null,
    val codacyApiToken: String? = null,
    val codacyOrganizationProvider: String? = null,
    val codacyUsername: String? = null,
    val codacyProjectName: String? = null,
    val parallelization: Int = 4,
    val extraSourceFolders: String? = null,
    val extraReportFolders: String? = null,
    val reportRejectList: String? = null
)
