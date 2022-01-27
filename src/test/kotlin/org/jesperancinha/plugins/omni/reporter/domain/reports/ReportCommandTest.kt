package org.jesperancinha.plugins.omni.reporter.domain.reports

import OmniJacocoReportParserCommand
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jesperancinha.plugins.omni.reporter.domain.api.TEMP_DIR_VARIABLE
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.toPath

internal class ReportCommandTest {
    @Test
    fun `should return XML jacoco report exec file`() {
        val tmpdir = System.getProperty(TEMP_DIR_VARIABLE)
        val classes = javaClass.getResource("/classes.jar")
        val execFile = javaClass.getResource("/testReleaseUnitTest.exec")

        classes.shouldNotBeNull()
        execFile.shouldNotBeNull()

        val xmlFile = File(tmpdir, "jacoco.xml")
        val report = OmniJacocoReportParserCommand(
            execFiles = listOf(execFile.toURI().toPath().toFile()),
            classFiles = listOf(classes.toURI().toPath().toFile()),
            xmlReport = xmlFile
        ).parse()

        xmlFile.shouldExist()

        report.name shouldBe "JaCoCo Coverage Report"
    }
}