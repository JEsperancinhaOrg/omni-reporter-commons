package org.jesperancinha.plugins.omni.reporter.domain

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpContent
import org.apache.http.entity.ContentType
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.jesperancinha.plugins.omni.reporter.parsers.Language
import org.jesperancinha.plugins.omni.reporter.parsers.readJsonValue
import org.jesperancinha.plugins.omni.reporter.parsers.writeCamelCaseJsonValueAsString
import org.slf4j.LoggerFactory


data class CodacyResponse(
    val success: String?,
    val error: String?
)

data class CodacyFileReport(
    val filename: String,
    val total: Int,
    val coverage: MutableMap<String, Int> = mutableMapOf()
)

data class CodacyReport(
    val total: Int,
    val fileReports: Array<CodacyFileReport> = emptyArray()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CodacyReport) return false

        if (total != other.total) return false
        if (!fileReports.contentEquals(other.fileReports)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = total
        result = 31 * result + fileReports.contentHashCode()
        return result
    }

}

data class CodacyApiTokenConfig(
    val codacyApiToken: String,
    val codacyOrganizationProvider: String,
    val codacyUsername: String,
    val codacyProjectName: String
) {
    companion object {
        fun isApiTokenConfigure(
            codacyApiToken: String?,
            codacyOrganizationProvider: String?,
            codacyUsername: String?,
            codacyProjectName: String?
        ) = !(codacyApiToken.isNullOrBlank() ||
                codacyOrganizationProvider.isNullOrBlank() ||
                codacyUsername.isNullOrBlank() ||
                codacyProjectName.isNullOrBlank())
    }
}

/**
 * Created by jofisaes on 06/01/2022
 */
open class CodacyClient(
    override val token: String?,
    override val apiToken: CodacyApiTokenConfig?,
    override val url: String,
    val language: Language,
    val repo: Repository,
    val partial: Boolean = false
) : ApiClientImpl<CodacyReport, CodacyResponse>() {

    override fun submit(report: CodacyReport): CodacyResponse {
        val revision = repo.resolve(Constants.HEAD)
        val commitId = RevWalk(repo).parseCommit(revision).id.name
        return apiToken?.let {
            submitReportWithApiToken(report, commitId)
        } ?: submitReportWithProjectRepoToken(report, commitId)

    }

    private fun submitReportWithApiToken(
        report: CodacyReport,
        commitId: String?,
    ): CodacyResponse {
        val codacyReportUrl =
            "$url/2.0/${apiToken?.codacyOrganizationProvider}/${apiToken?.codacyUsername}/${apiToken?.codacyProjectName}/commit/$commitId/coverage/${language.lang}?partial=${partial}"
        logger.info("Sending ${language.name.lowercase()} report to codacy at $codacyReportUrl")
        val jsonReport = writeCamelCaseJsonValueAsString(report)
        logger.debug(jsonReport.redact(token).redact(apiToken?.codacyApiToken))
        val content: HttpContent = ByteArrayContent(ContentType.APPLICATION_JSON.mimeType, jsonReport.toByteArray())
        val httpRequest = httpRequestFactory.buildPostRequest(GenericUrl(codacyReportUrl), content)
        httpRequest.headers.contentType = ContentType.APPLICATION_JSON.mimeType
        apiToken?.apply {
            httpRequest.headers["api-token"] = this.codacyApiToken
        }
        val httpResponse = httpRequest?.execute()
        val readAllBytes = httpResponse?.content?.readAllBytes() ?: byteArrayOf()
        return readJsonValue(readAllBytes)
    }

    private fun submitReportWithProjectRepoToken(
        report: CodacyReport,
        commitId: String?
    ): CodacyResponse {
        val codacyReportUrl = "$url/2.0/coverage/$commitId/${language.lang}?partial=${partial}"
        logger.info("Sending ${language.name.lowercase()} report to codacy at $codacyReportUrl")
        val jsonReport = writeCamelCaseJsonValueAsString(report)
        logger.debug(jsonReport.redact(token).redact(apiToken?.codacyApiToken))
        val content: HttpContent = ByteArrayContent(ContentType.APPLICATION_JSON.mimeType, jsonReport.toByteArray())
        val httpRequest = httpRequestFactory.buildPostRequest(GenericUrl(codacyReportUrl), content)
        httpRequest.headers.contentType = ContentType.APPLICATION_JSON.mimeType
        httpRequest.headers["project-token"] = token
        val httpResponse = httpRequest?.execute()
        val readAllBytes = httpResponse?.content?.readAllBytes() ?: byteArrayOf()
        return readJsonValue(readAllBytes)
    }

    fun submitEndReport(): CodacyResponse {
        val revision = repo.resolve(Constants.HEAD)
        val commitId = RevWalk(repo).parseCommit(revision).id.name
        return apiToken?.let {
            submitEndReportWithApiToken(commitId)
        } ?: submitEndReportWithProjectRepoToken(commitId)
    }

    private fun submitEndReportWithApiToken(commitId: String?): CodacyResponse {
        val codacyReportUrl = "$url/2.0/${apiToken?.codacyOrganizationProvider}/${apiToken?.codacyUsername}/${apiToken?.codacyProjectName}/commit/$commitId/coverageFinal"
        logger.info("Sending Final ${language.name.lowercase()} report to codacy at $codacyReportUrl")
        val httpRequest = httpRequestFactory.buildPostRequest(
            GenericUrl(codacyReportUrl),
            ByteArrayContent(ContentType.APPLICATION_JSON.mimeType, "".toByteArray())
        )
        httpRequest.headers.contentType = ContentType.APPLICATION_JSON.mimeType
        apiToken?.apply {
            httpRequest.headers["api-token"] = this.codacyApiToken
        }
        val httpResponse = httpRequest?.execute()
        val readAllBytes = httpResponse?.content?.readAllBytes() ?: byteArrayOf()
        return readJsonValue(readAllBytes)
    }

    private fun submitEndReportWithProjectRepoToken(commitId: String?): CodacyResponse {
        val codacyReportUrl = "$url/2.0/commit/$commitId/coverageFinal"
        logger.info("Sending Final ${language.name.lowercase()} report to codacy at $codacyReportUrl")
        val httpRequest = httpRequestFactory.buildPostRequest(
            GenericUrl(codacyReportUrl),
            ByteArrayContent(ContentType.APPLICATION_JSON.mimeType, "".toByteArray())
        )
        httpRequest.headers.contentType = ContentType.APPLICATION_JSON.mimeType
        httpRequest.headers["project-token"] = token
        val httpResponse = httpRequest?.execute()
        val readAllBytes = httpResponse?.content?.readAllBytes() ?: byteArrayOf()
        return readJsonValue(readAllBytes)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CoverallsClient::class.java)
    }
}