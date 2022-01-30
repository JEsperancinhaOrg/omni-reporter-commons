package org.jesperancinha.plugins.omni.reporter.domain.reports

import OmniJacocoReportParserCommand
import org.jesperancinha.plugins.omni.reporter.CodecovPackageNotFoundException
import org.jesperancinha.plugins.omni.reporter.domain.api.TEMP_DIR_VARIABLE
import org.jesperancinha.plugins.omni.reporter.parsers.readXmlValue
import org.jesperancinha.plugins.omni.reporter.parsers.xmlObjectMapper
import org.slf4j.LoggerFactory
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
    fun isValid() = try {
        getParentAdapter()
        true
    } catch (ex: Exception) {
        false
    }

    abstract fun generatePayload(failOnUnknown: Boolean, compiledSourcesDirs: List<File>): String
}

internal fun findNewPackageName(root: File, p: Package, compiledSourcesDirs: List<File>) =
    compiledSourcesDirs
        .map { File(it, p.name) }
        .filter { file -> file.exists() }
        .map { file -> file.toRelativeString(root) }
        .firstOrNull()


class OmniGenericFileAdapter(
    override val report: File,
) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter = OmniGenericReportParentFileAdapter()
    override fun generatePayload(failOnUnknown: Boolean, compiledSourcesDirs: List<File>): String = report.readText()
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
            report.inputStream().readJacocoReport(),
            root,
            includeBranchCoverage,
        )

    override fun generatePayload(failOnUnknown: Boolean, compiledSourcesDirs: List<File>): String {
        val reportObject: Report = readXmlValue(report.inputStream())
        val copy = reportObject.copy(
            packages = reportObject.packages.mapNotNull { p: Package ->
                val newName = findNewPackageName(root, p, compiledSourcesDirs)
                newName?.let { p.copy(name = newName) }
                    ?: if (failOnUnknown) throw CodecovPackageNotFoundException(p.name) else null
            }
        )
        return xmlObjectMapper.writeValueAsString(copy)
    }
}

class OmniJacocoExecFileAdapter(
    override val report: File,
    val failOnXmlParseError: Boolean = false,
    val root: File,
    val projectBuildDirectory: File,
    private val includeBranchCoverage: Boolean = false,
    private val jarFile: File? = projectBuildDirectory.findJarFile()
) : OmniFileAdapter(report) {

    private val xmlReport = File(System.getProperty(TEMP_DIR_VARIABLE), "jacoco-${UUID.randomUUID()}.xml")

    override fun getParentAdapter(): OmniReportParentFileAdapter {

        return OmniJacocoReportParserCommand(
            execFiles = listOf(report),
            classFiles = jarFile?.run { listOf(jarFile) } ?: emptyList(),
            xmlReport = xmlReport
        ).parse().let {
            OmniJacocoReportParentFileAdapter(
                it,
                root,
                includeBranchCoverage,
            )
        }
    }

    override fun generatePayload(failOnUnknown: Boolean, compiledSourcesDirs: List<File>): String {
        val reportObject: Report = readXmlValue(xmlReport.inputStream())
        val copy = reportObject.copy(
            packages = reportObject.packages.mapNotNull { p: Package ->
                val newName = findNewPackageName(root, p, compiledSourcesDirs)
                newName?.let { p.copy(name = newName) }
                    ?: if (failOnUnknown) throw CodecovPackageNotFoundException(p.name) else null
            }
        )
        return xmlObjectMapper.writeValueAsString(copy)
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

    override fun generatePayload(failOnUnknown: Boolean, compiledSourcesDirs: List<File>): String {
        val reportObject = report.inputStream().readLCovReport(false)
        val reportText = report.readText()

        logger.debug("- Correcting LCov payload with source folders ${compiledSourcesDirs.joinToString(";")}}")
        val allFiles = reportObject.map { it.sourceFilePath }
            .map { sp ->
                sp to root.toPath()
                    .relativize(File(compiledSourcesDirs
                        .first {
                                cps -> File(cps, sp).exists() && File(cps, sp).absoluteFile.startsWith(projectBuildDirectory.absoluteFile)}, sp).toPath())
                    .toString()
            }
        return allFiles.fold(reportText) { text, replacePair ->
            text.replace(replacePair.first, replacePair.second)
        }
    }

    companion object {
        val logger: org.slf4j.Logger = LoggerFactory.getLogger(OmniLCovFileAdapter::class.java)
    }
}

class OmniCloverFileAdapter(
    override val report: File,
    val failOnXmlParseError: Boolean = false,
    val root: File,
    val projectBuildDirectory: File,
    private val includeBranchCoverage: Boolean = false

) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter =
        OmniCloverReportParentFileAdapter(
            report.inputStream().readCloverReport(failOnXmlParseError),
            root,
            projectBuildDirectory,
            includeBranchCoverage
        )

    override fun generatePayload(failOnUnknown: Boolean, compiledSourcesDirs: List<File>): String {
        return report.readText()
    }
}

class OmniCoveragePyFileAdapter(
    override val report: File,
    val failOnXmlParseError: Boolean = false,
    val root: File,
    val projectBuildDirectory: File,
    private val includeBranchCoverage: Boolean = false

) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniCoveragePyReportParentFileAdapter =
        OmniCoveragePyReportParentFileAdapter(
            report.inputStream().readCoveragePyReport(failOnXmlParseError),
            root,
            includeBranchCoverage
        )

    override fun generatePayload(failOnUnknown: Boolean, compiledSourcesDirs: List<File>): String {
        return report.readText()
    }
}