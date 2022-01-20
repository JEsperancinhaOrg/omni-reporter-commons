package org.jesperancinha.plugins.omni.reporter.parsers

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jesperancinha.plugins.omni.reporter.domain.jacoco.Line
import org.jesperancinha.plugins.omni.reporter.domain.jacoco.Package
import org.jesperancinha.plugins.omni.reporter.domain.jacoco.Report
import org.jesperancinha.plugins.omni.reporter.domain.jacoco.Sourcefile
import org.jesperancinha.plugins.omni.reporter.pipelines.LocalPipeline
import org.jesperancinha.plugins.omni.reporter.transformers.JacocoParserToCoveralls
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Test

internal class JacocoParserTest {
    private val jacocoParser = JacocoParserToCoveralls(
        "token",
        LocalPipeline(),
        root,
        failOnUnknown = false,
        includeBranchCoverage = false,
        useCoverallsCount = false
    )

    @Test
    fun parseSourceFile() {


        val element = Line(
            nr = 1,
            ci = 10,
        )
        val element2 = Line(
            nr = 2,
            ci = 11,
            cb = 3,
            mb = 2
        )
        val sourcefile = Sourcefile(
            name = "Racoons.kt",
            lines = listOf(element, element2)
        )


        val element21 = Line(
            nr = 1,
            ci = 10
        )
        val element22 = Line(
            nr = 2,
            ci = 11,
            cb = 3,
            mb = 2
        )
        val sourcefile2 = Sourcefile(
            name = "Racoons.kt",
            lines = listOf(element21, element22)
        )

        val pack = Package(
            name = "/",
            sourcefiles = listOf(sourcefile)
        )

        val pack2 = Package(
            name = "/",
            sourcefiles = listOf(sourcefile2)
        )

        val report = Report(packages = listOf(pack, pack2))

        jacocoParser.parseInput(xmlObjectMapper.writeValueAsString(report).byteInputStream(), listOf(root))
        jacocoParser.parseInput(xmlObjectMapper.writeValueAsString(report).byteInputStream(), listOf(root))

        val sourceFiles = jacocoParser.parseInput(
            xmlObjectMapper.writeValueAsString(report).byteInputStream(),
            listOf(root)
        ).sourceFiles

        sourceFiles.shouldNotBeNull()
        sourceFiles.shouldHaveSize(1)
        val sourceFile = sourceFiles[0]
        sourceFile.name.shouldNotBeNull()
        sourceFile.sourceDigest.shouldNotBeNull()
        sourceFile.coverage shouldBe arrayOf(50, 55, null, null, null)
//        sourceFile.branches shouldBe arrayOf(2, 5, 3, 11)
//        sourceFile.source shouldBe File(root, "Racoons.kt").bufferedReader().use { it.readText() }
    }
}