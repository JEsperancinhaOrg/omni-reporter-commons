package org.jesperancinha.plugins.omni.reporter.transformers

import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniFileAdapter
import org.jesperancinha.plugins.omni.reporter.domain.reports.Report
import org.jesperancinha.plugins.omni.reporter.parsers.readXmlValue
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream

/**
 * Codecov allows sending reports from many brands
 * This means that the codecov transformer can provide support for types of reports supported by codecov
 * It will eventually provide support for all of them.
 * Detection is still based on filename
 * This is because no transformation is necessary
 * The final report is an aggregate of all original raw files
 *
 * Created by jofisaes on 09/01/2022
 */
class AllParserToCodecov(
    token: String,
    pipeline: Pipeline,
    root: File,
    val failOnUnknown: Boolean,
    includeBranchCoverage: Boolean = false,
) : OmniReporterParserImpl<InputStream, String>
    (token = token, pipeline = pipeline, root = root, includeBranchCoverage = includeBranchCoverage) {
    override fun parseInput(input: OmniFileAdapter, compiledSourcesDirs: List<File>): String =  input.generatePayload(failOnUnknown, compiledSourcesDirs)

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AllParserToCodecov::class.java)
    }
}