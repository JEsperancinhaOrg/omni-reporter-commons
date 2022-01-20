package org.jesperancinha.plugins.omni.reporter.pipelines

import org.jesperancinha.plugins.omni.reporter.domain.CUSTOM
import org.jesperancinha.plugins.omni.reporter.domain.GITLAB
import org.jesperancinha.plugins.omni.reporter.pipelines.GitHubPipeline.Companion.GITHUB_REPOSITORY
import org.jesperancinha.plugins.omni.reporter.pipelines.GitHubPipeline.Companion.GITHUB_RUN_ID
import org.jesperancinha.plugins.omni.reporter.pipelines.GitHubPipeline.Companion.GITHUB_RUN_NUMBER
import org.jesperancinha.plugins.omni.reporter.pipelines.GitHubPipeline.Companion.GITHUB_SERVER_URL
import org.jesperancinha.plugins.omni.reporter.pipelines.GitLabPipeline.Companion.CI_COMMIT_REF_NAME
import org.jesperancinha.plugins.omni.reporter.pipelines.GitLabPipeline.Companion.CI_CONCURRENT_ID
import org.jesperancinha.plugins.omni.reporter.pipelines.GitLabPipeline.Companion.CI_EXTERNAL_PULL_REQUEST_ID
import org.jesperancinha.plugins.omni.reporter.pipelines.GitLabPipeline.Companion.CI_EXTERNAL_PULL_REQUEST_IID
import org.jesperancinha.plugins.omni.reporter.pipelines.GitLabPipeline.Companion.CI_JOB_ID
import org.jesperancinha.plugins.omni.reporter.pipelines.GitLabPipeline.Companion.CI_PIPELINE_ID
import org.jesperancinha.plugins.omni.reporter.pipelines.GitLabPipeline.Companion.CI_PROJECT_URL
import org.jesperancinha.plugins.omni.reporter.pipelines.LocalPipeline.Companion.CI_BUILD_NUMBER
import org.jesperancinha.plugins.omni.reporter.pipelines.LocalPipeline.Companion.CI_NAME
import org.jesperancinha.plugins.omni.reporter.pipelines.LocalPipeline.Companion.JOB_NUM
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(Pipeline::class.java)
private val environment: MutableMap<String, String> = System.getenv()
private val allEnv = listOf(
    // General
    CI_NAME,
    CI_BUILD_NUMBER,
    JOB_NUM,

    //GitLab
    CI_CONCURRENT_ID,
    CI_JOB_ID,
    CI_COMMIT_REF_NAME,
    CI_PROJECT_URL,
    CI_PIPELINE_ID,
    CI_EXTERNAL_PULL_REQUEST_ID,
    CI_EXTERNAL_PULL_REQUEST_IID,

    //GitHubs
    GITHUB_RUN_NUMBER,
    GITHUB_RUN_ID,
    GITHUB_SERVER_URL,
    GITHUB_REPOSITORY

)
private val rejectWords = listOf("BUILD")

interface Pipeline {

    val branchRef: String?
    val branchName: String?
    val serviceName: String
    val serviceNumber: String?
    val serviceJobId: String?
    val codecovServiceName: String?
    val buildUrl: String?
}

abstract class PipelineImpl(
    override val branchName: String? = null,
    override val buildUrl: String? = null
) : Pipeline {

    override fun toString() = "* System Variables\n" +
            "${findAllVariables()}\n" +
            "* Service Found\n" +
            "- Service Name = $serviceName\n" +
            "- Service Number (Build) = $serviceNumber\n" +
            "- Service Job Id = $serviceJobId"

    companion object {
        val currentPipeline: Pipeline = when {
            environment[GITHUB_RUN_ID] != null -> GitHubPipeline()
            environment[CI_JOB_ID] != null -> GitLabPipeline()
            else -> LocalPipeline()
        }.also {
            it.toString().lines().forEach { logLine -> logger.info(logLine) }
        }

        internal fun findAllVariables() = allEnv.joinToString("\n") { "- $it = ${environment[it] ?: "null"}" }

        internal fun findSystemVariableValue(name: String): String? =
            environment[name]?.let {
                when {
                    rejectWords.contains(it.uppercase()) -> null
                    it.isEmpty() -> null
                    else -> it
                }
            }

        internal fun findServiceName(fallback: () -> String) = findSystemVariableValue(CI_NAME) ?: fallback()

        internal fun findServiceNumber(fallback: () -> String?) = findSystemVariableValue(CI_BUILD_NUMBER) ?: fallback()

        internal fun findServiceJobId(fallback: () -> String?) = findSystemVariableValue(JOB_NUM) ?: fallback()
    }
}

class GitHubPipeline(
    override val serviceName: String = findServiceName { "github-ci" },
    override val serviceNumber: String? = findServiceNumber {
        findSystemVariableValue(GITHUB_RUN_NUMBER)
    },
    override val serviceJobId: String? = findServiceJobId {
        findSystemVariableValue(GITHUB_RUN_ID)
    },
    override val branchRef: String? = null,
    override val codecovServiceName: String? = CUSTOM,
    override val buildUrl: String? = "${findSystemVariableValue(GITHUB_SERVER_URL)}/${
        findSystemVariableValue(
            GITHUB_REPOSITORY
        )
    }/actions/runs/${findSystemVariableValue(GITHUB_RUN_ID)}"
) : PipelineImpl() {
    companion object {
        const val GITHUB_RUN_NUMBER = "GITHUB_RUN_NUMBER"
        const val GITHUB_RUN_ID = "GITHUB_RUN_ID"
        const val GITHUB_SERVER_URL = "GITHUB_SERVER_URL"
        const val GITHUB_REPOSITORY = "GITHUB_REPOSITORY"
    }
}

class GitLabPipeline(
    override val serviceName: String = findServiceName { "gitlab-ci" },
    override val serviceNumber: String? = findServiceNumber {
        findSystemVariableValue(CI_CONCURRENT_ID)
    },
    override val serviceJobId: String? = findServiceJobId {
        findSystemVariableValue(CI_JOB_ID)
    },
    override val branchRef: String? = null,
    override val branchName: String? = findSystemVariableValue(CI_COMMIT_REF_NAME),
    override val codecovServiceName: String? = GITLAB,
    override val buildUrl: String? = "${findSystemVariableValue(CI_PROJECT_URL)}/-/pipelines/${
        findSystemVariableValue(
            CI_PIPELINE_ID
        )
    }"

) : PipelineImpl() {
    companion object {
        const val CI_CONCURRENT_ID = "CI_CONCURRENT_ID"
        const val CI_JOB_ID = "CI_JOB_ID"
        const val CI_COMMIT_REF_NAME = "CI_COMMIT_REF_NAME"
        const val CI_PROJECT_URL = "CI_PROJECT_URL"
        const val CI_PIPELINE_ID = "CI_PIPELINE_ID"
        const val CI_EXTERNAL_PULL_REQUEST_ID = "CI_EXTERNAL_PULL_REQUEST_ID"
        const val CI_EXTERNAL_PULL_REQUEST_IID = "CI_EXTERNAL_PULL_REQUEST_IID"
    }
}

class LocalPipeline(
    override val serviceName: String = findServiceName { "local-ci" },
    override val serviceNumber: String? = environment[CI_BUILD_NUMBER],
    override val serviceJobId: String? = findServiceJobId { null },
    override val branchRef: String? = null,
    override val codecovServiceName: String? = CUSTOM,
) : PipelineImpl() {

    companion object {
        const val CI_NAME = "CI_NAME"
        const val CI_BUILD_NUMBER = "CI_BUILD_NUMBER"
        const val JOB_NUM = "JOB_NUM"
    }

}