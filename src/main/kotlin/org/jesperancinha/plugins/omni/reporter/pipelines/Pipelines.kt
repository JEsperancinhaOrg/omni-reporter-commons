package org.jesperancinha.plugins.omni.reporter.pipelines

import org.jesperancinha.plugins.omni.reporter.domain.api.CUSTOM
import org.jesperancinha.plugins.omni.reporter.domain.api.GITLAB
import org.jesperancinha.plugins.omni.reporter.logger.OmniLoggerConfig
import org.jesperancinha.plugins.omni.reporter.pipelines.BitBucketPipeline.Companion.BITBUCKET_BUILD_NUMBER
import org.jesperancinha.plugins.omni.reporter.pipelines.CircleCIPipeline.Companion.CIRCLE_BRANCH
import org.jesperancinha.plugins.omni.reporter.pipelines.CircleCIPipeline.Companion.CIRCLE_BUILD_NUM
import org.jesperancinha.plugins.omni.reporter.pipelines.CircleCIPipeline.Companion.CIRCLE_BUILD_URL
import org.jesperancinha.plugins.omni.reporter.pipelines.CircleCIPipeline.Companion.CIRCLE_WORKFLOW_ID
import org.jesperancinha.plugins.omni.reporter.pipelines.GitHubPipeline.Companion.GITHUB_BASE_REF
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

private val logger = OmniLoggerConfig.getLogger(Pipeline::class.java)
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

    //GitHub
    GITHUB_RUN_NUMBER,
    GITHUB_RUN_ID,
    GITHUB_BASE_REF,
    GITHUB_SERVER_URL,
    GITHUB_REPOSITORY,

    //CircleCI
    CircleCIPipeline.CIRCLECI,
    CIRCLE_WORKFLOW_ID,
    CIRCLE_BUILD_NUM,
    CIRCLE_BRANCH,
    CIRCLE_BUILD_URL

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
    val fetchBranchNameFromEnv: Boolean
}

abstract class PipelineImpl(
    override val branchName: String? = null,
    override val buildUrl: String? = null,
    override val fetchBranchNameFromEnv: Boolean = false,
) : Pipeline {

    override fun toString() = "* System Variables\n" +
            "${findAllVariables()}\n" +
            "* Service Found\n" +
            "- Service Name = $serviceName\n" +
            "- Service Number (Build) = $serviceNumber\n" +
            "- Service Job Id = $serviceJobId"


    companion object {

        @JvmStatic
        fun currentPipeline(fetchBranchNameFromEnv: Boolean): Pipeline = when {
            environment[CircleCIPipeline.CIRCLECI] != null -> CircleCIPipeline(fetchBranchNameFromEnv = fetchBranchNameFromEnv)
            environment[BITBUCKET_BUILD_NUMBER] != null -> BitBucketPipeline(fetchBranchNameFromEnv = fetchBranchNameFromEnv)
            environment[GITHUB_RUN_ID] != null -> GitHubPipeline(fetchBranchNameFromEnv = fetchBranchNameFromEnv)
            environment[CI_JOB_ID] != null -> GitLabPipeline(fetchBranchNameFromEnv = fetchBranchNameFromEnv)
            else -> LocalPipeline(fetchBranchNameFromEnv = fetchBranchNameFromEnv)
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
    override val fetchBranchNameFromEnv: Boolean,
    override val branchRef: String? = if (fetchBranchNameFromEnv) findSystemVariableValue(GITHUB_BASE_REF) else null,
    override val codecovServiceName: String? = CUSTOM,
    override val buildUrl: String? = "${findSystemVariableValue(GITHUB_SERVER_URL)}/${
        findSystemVariableValue(
            GITHUB_REPOSITORY
        )
    }/actions/runs/${findSystemVariableValue(GITHUB_RUN_ID)}",
) : PipelineImpl() {
    companion object {
        const val GITHUB_RUN_NUMBER = "GITHUB_RUN_NUMBER"
        const val GITHUB_RUN_ID = "GITHUB_RUN_ID"
        const val GITHUB_BASE_REF = "GITHUB_BASE_REF"
        const val GITHUB_SERVER_URL = "GITHUB_SERVER_URL"
        const val GITHUB_REPOSITORY = "GITHUB_REPOSITORY"
    }
}

class GitLabPipeline(
    override val serviceName: String = findServiceName { "gitlab-ci" },
    override val serviceNumber: String? = findServiceNumber { findSystemVariableValue(CI_CONCURRENT_ID) },
    override val serviceJobId: String? = findServiceJobId { findSystemVariableValue(CI_JOB_ID) },
    override val fetchBranchNameFromEnv: Boolean,
    override val branchRef: String? = if (fetchBranchNameFromEnv) findSystemVariableValue(CI_COMMIT_REF_NAME) else null,
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

class CircleCIPipeline(
    override val serviceName: String = findServiceName { "circle-ci" },
    override val serviceNumber: String? = findServiceNumber { findSystemVariableValue(CIRCLE_WORKFLOW_ID) },
    override val serviceJobId: String? = findServiceJobId { findSystemVariableValue(CIRCLE_BUILD_NUM) },
    override val fetchBranchNameFromEnv: Boolean,
    override val branchRef: String? = if (fetchBranchNameFromEnv) findSystemVariableValue(CIRCLE_BRANCH) else null,
    override val branchName: String? = findSystemVariableValue(CIRCLE_BRANCH),
    override val codecovServiceName: String? = org.jesperancinha.plugins.omni.reporter.domain.api.CIRCLECI,
    override val buildUrl: String? = findSystemVariableValue(CIRCLE_BUILD_URL)
) : PipelineImpl() {
    companion object {
        const val CIRCLECI = "CIRCLECI"
        const val CIRCLE_WORKFLOW_ID = "CIRCLE_WORKFLOW_ID"
        const val CIRCLE_BUILD_NUM = "CIRCLE_BUILD_NUM"
        const val CIRCLE_BRANCH = "CIRCLE_BRANCH"
        const val CIRCLE_BUILD_URL = "CIRCLE_BUILD_URL"
    }
}

class BitBucketPipeline(
    override val serviceName: String = findServiceName { "bitbucket-ci" },
    override val serviceNumber: String? = findServiceNumber { findSystemVariableValue(BITBUCKET_BUILD_NUMBER) },
    override val serviceJobId: String? = findServiceJobId { findSystemVariableValue(BITBUCKET_BUILD_NUMBER) },
    override val fetchBranchNameFromEnv: Boolean,
    override val branchRef: String? = if (fetchBranchNameFromEnv) findSystemVariableValue(BITBUCKET_BRANCH) else null,
    override val branchName: String? = findSystemVariableValue(BITBUCKET_BRANCH),
    override val codecovServiceName: String? = CUSTOM,
    override val buildUrl: String? =
        "${findSystemVariableValue(BITBUCKET_GIT_HTTP_ORIGIN)}/addon/pipelines/home#!/results/$serviceJobId"
) : PipelineImpl() {
    companion object {
        const val BITBUCKET_BUILD_NUMBER = "BITBUCKET_BUILD_NUMBER"
        const val BITBUCKET_BRANCH = "BITBUCKET_BRANCH"
        const val BITBUCKET_GIT_HTTP_ORIGIN = "BITBUCKET_GIT_HTTP_ORIGIN"
    }
}

class LocalPipeline(
    override val serviceName: String = findServiceName { "local-ci" },
    override val serviceNumber: String? = environment[CI_BUILD_NUMBER],
    override val serviceJobId: String? = findServiceJobId { null },
    override val branchRef: String? = null,
    override val codecovServiceName: String? = CUSTOM,
    override val fetchBranchNameFromEnv: Boolean,
) : PipelineImpl() {

    companion object {
        const val CI_NAME = "CI_NAME"
        const val CI_BUILD_NUMBER = "CI_BUILD_NUMBER"
        const val JOB_NUM = "JOB_NUM"
    }

}