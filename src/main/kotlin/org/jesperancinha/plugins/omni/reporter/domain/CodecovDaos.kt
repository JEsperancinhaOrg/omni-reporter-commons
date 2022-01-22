package org.jesperancinha.plugins.omni.reporter.domain

import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpContent
import com.google.api.client.http.HttpResponse
import org.apache.http.entity.ContentType
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.jesperancinha.plugins.omni.reporter.domain.CodecovEndpoint.V4
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import org.slf4j.LoggerFactory


val TRAVIS = "travis"
val BUILDBOT = "buildbot"
val CIRCLECI = "circleci"
val BUDDYBUILD = "buddybuild"
val SOLANO = "solano"
val TEAMCITY = "teamcity"
val APPVEYOR = "appveyor"
val WERCKER = "wercker"
val MAGNUM = "magnum"
val SHIPPABLE = "shippable"
val CODESHIP = "codeship"
val DRONE_IO = "drone.io"
val JENKINS = "jenkins"
val SEMAPHORE = "semaphore"
val GITLAB = "gitlab"
val BAMBOO = "bamboo"
val SNAP = "snap"
val BUILDKITE = "buildkite"
val BITRISE = "bitrise"
val GREENHOUSE = "greenhouse"
val CUSTOM = "custom"

enum class CodecovEndpoint(val param: String) {
    V2("v2"),
    V4("v4"),
    V5("v5")
}

/**
 * Created by jofisaes on 30/12/2021
 */
class CodecovClient(
    override val url: String,
    override val token: String,
    private val pipeline: Pipeline,
    private val repo: Repository,
    private val version: CodecovEndpoint = V4
) : ApiClientImpl<String, String>() {
    override fun submit(report: String): String {
        val revision = repo.resolve(Constants.HEAD)
        val commitId = RevWalk(repo).parseCommit(revision).id.name
        val codacyReportUrl = "$url/${version.param}"
        logger.debug(report.redact(token))
        val emptyContent: HttpContent = ByteArrayContent(ContentType.TEXT_PLAIN.mimeType, "".toByteArray())
        val genericUrl = GenericUrl(codacyReportUrl)
        genericUrl.set("commit", commitId)
        genericUrl.set("token", token)
        genericUrl.set("branch", pipeline.branchName ?: repo.branch)
        pipeline.serviceNumber?.apply {
            genericUrl.set("build", this)
        }
        pipeline.serviceJobId?.apply {
            genericUrl.set("job", this)
        }
        pipeline.buildUrl?.apply {
            genericUrl.set("build_url", this)
        }
        pipeline.codecovServiceName?.apply {
            genericUrl.set("service", this)
        }
        val httpRequest = httpRequestFactory.buildPostRequest(genericUrl, emptyContent)
        httpRequest.headers.accept = ContentType.TEXT_PLAIN.mimeType
        httpRequest.isLoggingEnabled = false
        val redactedURL = genericUrl.toURL().toExternalForm().redact(token)
        logger.info("Sending report to codecov server at $redactedURL")
        val httpResponse = httpRequest?.execute()
        val (viewUrl, s3UploadUrl) = (httpResponse?.content?.bufferedReader()?.readText() ?: "").split("\n")

        val reportContent: HttpContent = ByteArrayContent(ContentType.TEXT_PLAIN.mimeType, report.toByteArray())
        val httpRequestToS3 = httpRequestFactory.buildPutRequest(GenericUrl(s3UploadUrl), reportContent)
        httpRequestToS3.headers.contentType = ContentType.TEXT_PLAIN.mimeType
        httpRequestToS3.isLoggingEnabled = false
        logger.info("Report is being sent to Codecov's S3 servert")
        logger.debug("Sending report to Codecov's S3 server at ${s3UploadUrl.redact(token)}")
        logger.info("Your report will then be visible at $viewUrl")
        val httpResponseFromS3 = httpRequestToS3?.execute()
        return (httpResponseFromS3?.content?.bufferedReader()?.readText()?.let {
            it.ifEmpty {
                responseText(httpResponseFromS3)
            }
        } ?: httpResponseFromS3?.let { responseText(httpResponseFromS3) } ?: "")
    }

    override fun responseText(httpResponse: HttpResponse): String {
        return "Received response with code ${httpResponse.statusCode}"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CodecovClient::class.java)
    }
}