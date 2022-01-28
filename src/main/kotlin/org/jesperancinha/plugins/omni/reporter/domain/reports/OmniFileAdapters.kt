package org.jesperancinha.plugins.omni.reporter.domain.reports

import java.io.File

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
    private val includeBranchCoverage: Boolean = false,
) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter = OmniJacocoReportParentFileAdapter(
        report.inputStream().readJacocoReport(failOnXmlParseError),
        root,
        includeBranchCoverage,
    )
}

class OmniJacocoExecFileAdapter(override val report: File) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter {
        TODO("Not yet implemented")
    }

}

class OmniLCovFileAdapter(override val report: File) : OmniFileAdapter(report) {
    override fun getParentAdapter(): OmniReportParentFileAdapter {
        TODO("Not yet implemented")
    }

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