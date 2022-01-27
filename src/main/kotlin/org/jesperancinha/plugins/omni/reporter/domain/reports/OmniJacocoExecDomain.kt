import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.IReportVisitor
import org.jacoco.report.MultiReportVisitor
import org.jacoco.report.MultiSourceFileLocator
import org.jacoco.report.xml.XMLFormatter
import org.jesperancinha.plugins.omni.reporter.domain.reports.Report
import org.jesperancinha.plugins.omni.reporter.domain.reports.readJacocoReport
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
        } else {
            execFiles.forEach {
                it.absolutePath
                loader.load(it)
            }
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
        return this.xmlReport.inputStream().readJacocoReport(failOnXmlParseError = failOnXmlParseError)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(OmniJacocoReportParserCommand::class.java)
    }
}


