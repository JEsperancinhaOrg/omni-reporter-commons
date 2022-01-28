package org.jesperancinha.plugins.omni.reporter.domain.reports

import OmniJacocoReportParserCommand
import org.jesperancinha.plugins.omni.reporter.domain.api.TEMP_DIR_VARIABLE
import java.io.File
import java.util.*


internal val JAR_FILE_PATTERNS = listOf(".*classes\\.jar$", ".*libs\\/.*\\.jar$")

/**
 * Searches for a compatible Jar file to extract Jacoco.xml data
 */
internal fun File.findJarFile(): File? =
    JAR_FILE_PATTERNS.firstNotNullOfOrNull { pattern ->
        val first = this.walkTopDown()
            .toList().firstOrNull { file ->
                file.absolutePath.matches(Regex(pattern))
            }
        first
    }

abstract class OmniFileAdapter(
    open val report: File
) {
    abstract fun getParentAdapter(): OmniReportParentFileAdapter
}

class OmniGenericFileAdapter(
    override val report: File,
) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter = OmniGenericReportParentFileAdapter()
}

class OmniJacocoFileAdapter(
    override val report: File,
    val failOnXmlParseError: Boolean = false,
    val root: File,
    val projectBuildDirectory: File,
    private val includeBranchCoverage: Boolean = false,
) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter =
        OmniJacocoReportParentFileAdapter(
            report.inputStream().readJacocoReport(failOnXmlParseError),
            root,
            includeBranchCoverage,
        )
}

class OmniJacocoExecFileAdapter(
    override val report: File,
    val failOnXmlParseError: Boolean = false,
    val root: File,
    val projectBuildDirectory: File,
    private val includeBranchCoverage: Boolean = false,
    private val jarFile: File? = projectBuildDirectory.findJarFile()
) : OmniFileAdapter(report) {

    override fun getParentAdapter(): OmniReportParentFileAdapter =
        OmniJacocoReportParserCommand(
            execFiles = listOf(report),
            classFiles = jarFile?.run { listOf(jarFile) } ?: emptyList(),
            xmlReport = File(System.getProperty(TEMP_DIR_VARIABLE), "jacoco-${UUID.randomUUID()}.xml")
        ).parse().let {
            OmniJacocoReportParentFileAdapter(
                it,
                root,
                includeBranchCoverage,
            )
        }
}


class OmniLCovFileAdapter(
    override val report: File,
    val failOnXmlParseError: Boolean = false,
    val root: File,
    val projectBuildDirectory: File,
    private val includeBranchCoverage: Boolean = false
) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniLCovReportParentFileAdapter =
        OmniLCovReportParentFileAdapter(
            report.inputStream().readLCovReport(failOnXmlParseError),
            root,
            includeBranchCoverage,
        )
}

class OmniCloverFileAdapter(override val report: File) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter {
        TODO("Not yet implemented")
    }

}

class OmniCoveragePyFileAdapter(override val report: File) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter {
        TODO("Not yet implemented")
    }

}