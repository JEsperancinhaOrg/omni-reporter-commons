import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.MultiSourceFileLocator
import org.jacoco.report.xml.XMLFormatter
import org.jesperancinha.plugins.omni.reporter.domain.reports.Report
import org.jesperancinha.plugins.omni.reporter.domain.reports.readJacocoReport
import org.jesperancinha.plugins.omni.reporter.logger.OmniLoggerConfig
import java.io.File
import java.io.FileOutputStream

/**
 * This class is inspired by the already existing in the Jacoco CLI source code
 *
 * @see <a href="https://github.com/jacoco/jacoco">Jacoco Open Source Project</a>
 */
class OmniJacocoReportParserCommand(
    val execFiles: List<File> = ArrayList(),
    val classFiles: List<File> = ArrayList(),
    val xmlReport: File,
    val name: String = "JaCoCo Coverage Report",
    val encoding: String = "UTF-8",
    private val failOnXmlParseError: Boolean = false
) {
    fun parse(): Report {
        val loader = ExecFileLoader()
        if (execFiles.isEmpty()) {
            logger.warn("No exec files requested!")
            return Report()
        } else {
            execFiles.forEach { loader.load(it) }
        }
        val builder = CoverageBuilder()
        val analyzer = Analyzer(loader.executionDataStore, builder)
        classFiles.forEach { analyzer.analyzeAll(it) }
        val bundle = builder.getBundle(name)
        val visitors: MutableList<IReportVisitor> = mutableListOf()
        val formatter = XMLFormatter()
        visitors.add(formatter.createVisitor(FileOutputStream(xmlReport)))
        val visitor: IReportVisitor = MultiReportVisitor(visitors)
        visitor.visitInfo(loader.sessionInfoStore.infos, loader.executionDataStore.contents)
        visitor.visitBundle(bundle, MultiSourceFileLocator(4))
        visitor.visitEnd()
        val readJacocoReport = this.xmlReport.inputStream().readJacocoReport(failOnXmlParseError)
        logger.debug("- Generated XML Jacoco report with content ${this.xmlReport.readText()}")
        return readJacocoReport
    }

    companion object {
        val logger = OmniLoggerConfig.getLogger(OmniJacocoReportParserCommand::class.java)
    }
}


