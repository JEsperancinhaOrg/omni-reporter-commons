package org.jesperancinha.plugins.omni.reporter.processors

import org.jesperancinha.plugins.omni.reporter.OmniProject
import org.jesperancinha.plugins.omni.reporter.ProjectDirectoryNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniFileAdapter
import java.io.File

class ReportFilesContainer(
    allProjects: List<OmniProject>,
    ignoreTestBuildDirectory: Boolean,
    failOnXmlParsingError: Boolean,
    projectBaseDir: File?,
    reportRejectList: List<String>,
    parallelization: Int
) {

    internal val supportedPredicate = Processor.supportedPredicate(ignoreTestBuildDirectory)

    val allReportFiles: Map<OmniProject, List<OmniFileAdapter>> by lazy {
        allProjects.toReportFiles(
            supportedPredicate,
            failOnXmlParsingError,
            projectBaseDir ?: throw ProjectDirectoryNotFoundException(),
            reportRejectList,
            parallelization
        )
    }

    val allCodecovSupportedFiles by lazy {
        allProjects.toAllCodecovSupportedFiles(
            supportedPredicate,
            projectBaseDir ?: throw ProjectDirectoryNotFoundException(),
            reportRejectList,
            parallelization
        )
    }
}
