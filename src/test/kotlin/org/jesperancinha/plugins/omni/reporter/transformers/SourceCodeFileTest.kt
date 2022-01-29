package org.jesperancinha.plugins.omni.reporter.transformers

import io.kotest.matchers.booleans.shouldBeTrue
import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyFileReport
import org.jesperancinha.plugins.omni.reporter.domain.api.CoverallsSourceFile
import org.jesperancinha.plugins.omni.reporter.domain.reports.CloverFile
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniCloverReportFileAdapter
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniReportFileAdapter
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Test
import java.io.File

internal class SourceCodeFileTest {


    @Test
    fun `should exist in upper layers`() {
        val projectBaseDir = File(root, "/app1")
        val sourceCodeFile = SourceCodeFile(
            projectBaseDir, "",
            OmniCloverReportFileTestAdapter()
        )

        sourceCodeFile.exists().shouldBeTrue()

    }
}

class OmniCloverReportFileTestAdapter : OmniReportFileAdapter {
    override fun name(): String = "../src/App.tsx"
    override fun toCoveralls(sourceCodeFile: SourceCodeFile): CoverallsSourceFile? {
        TODO("Not yet implemented")
    }

    override fun toCodacy(sourceCodeFile: SourceCodeFile): CodacyFileReport? {
        TODO("Not yet implemented")
    }
}