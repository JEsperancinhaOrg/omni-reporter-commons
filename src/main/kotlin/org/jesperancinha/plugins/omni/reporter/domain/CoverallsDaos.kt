package org.jesperancinha.plugins.omni.reporter.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.google.api.client.http.FileContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.MultipartContent
import org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM
import org.jesperancinha.plugins.omni.reporter.domain.jacoco.Line
import org.jesperancinha.plugins.omni.reporter.parsers.readJsonValue
import org.jesperancinha.plugins.omni.reporter.parsers.writeSnakeCaseJsonValueAsString
import org.slf4j.LoggerFactory
import java.io.File


data class CoverallsResponse(
    val message: String,
    val error: Boolean,
    val url: String?,
)

data class CoverallsSourceFile(
    val name: String,
    val sourceDigest: String,
    @JsonInclude(Include.NON_EMPTY)
    val coverage: Array<Int?> = arrayOf(),
    @JsonInclude(Include.NON_EMPTY)
    val branches: Array<Int?> = arrayOf(),
    @JsonInclude(Include.NON_NULL)
    val source: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoverallsSourceFile) return false
        if (name != other.name) return false
        if (sourceDigest != other.sourceDigest) return false
        if (!coverage.contentEquals(other.coverage)) return false
        if (!branches.contentEquals(other.branches)) return false
        if (source != other.source) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + sourceDigest.hashCode()
        result = 31 * result + coverage.contentHashCode()
        result = 31 * result + branches.contentHashCode()
        result = 31 * result + (source?.hashCode() ?: 0)
        return result
    }
}

data class Git(
    val head: Head? = null,
    val branch: String? = null,
    val remotes: List<Remote>? = null,
)

data class Head(
    val id: String? = null,
    val authorName: String? = null,
    val authorEmail: String? = null,
    val committerName: String? = null,
    val committerEmail: String? = null,
    val message: String? = null,
)

data class Remote(
    val name: String,
    val url: String,
)

data class CoverallsReport(
    val repoToken: String,
    val serviceName: String,
    val sourceFiles: MutableList<CoverallsSourceFile> = mutableListOf(),
    @JsonInclude(Include.NON_NULL)
    val git: Git? = null,
    @JsonInclude(Include.NON_NULL)
    val serviceNumber: String? = null,
    @JsonInclude(Include.NON_NULL)
    val serviceJobId: String? = null,
    @JsonInclude(Include.NON_NULL)
    val servicePullRequest: String? = null,
    @JsonInclude(Include.NON_NULL)
    val parallel: Boolean? = null,
    @JsonInclude(Include.NON_NULL)
    val flagName: String? = null,
    @JsonInclude(Include.NON_NULL)
    val commitSha: String? = null,
    @JsonInclude(Include.NON_NULL)
    val runAt: String? = null
)

open class CoverallsClient(
    override val url: String,
    override val token: String,
) : ApiClientImpl<CoverallsReport, CoverallsResponse?>() {
    override fun submit(report: CoverallsReport): CoverallsResponse? {
        val url = GenericUrl(url)
        val tmpdir = System.getProperty(TEMP_DIR_VARIABLE)
        val writeValueAsString = writeSnakeCaseJsonValueAsString(report)
        val file = File(tmpdir, COVERALLS_FILE)
        file.deleteOnExit()
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        file.bufferedWriter().use {
            it.write(writeSnakeCaseJsonValueAsString(report))
        }
        logger.debug(writeValueAsString.redact(token))
        logger.debug(file.absolutePath)
        val content = MultipartContent()
        val part = MultipartContent.Part(FileContent(APPLICATION_OCTET_STREAM.toString(), file))
        part.headers =
            HttpHeaders().set("Content-Disposition", "form-data; name=\"json_file\"; filename=\"$COVERALLS_FILE\"")
        content.addPart(part)
        logger.info("Sending reporting to Coveralls at $url")
        val httpRequest = httpRequestFactory.buildPostRequest(url, content)
        val httpResponse = httpRequest?.execute()
        val readAllBytes = httpResponse?.content?.readAllBytes() ?: byteArrayOf()
        return readJsonValue(readAllBytes)

    }

    companion object {
        private const val COVERALLS_FILE = "coveralls.json"
        private const val TEMP_DIR_VARIABLE = "java.io.tmpdir"
        private val logger = LoggerFactory.getLogger(CoverallsClient::class.java)
    }
}

internal fun isBranch(it: Line) =
    it.mb > 0 || it.cb > 0
