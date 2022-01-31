package org.jesperancinha.plugins.omni.reporter.parsers

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jesperancinha.plugins.omni.reporter.domain.api.TEMP_DIR_VARIABLE
import org.jesperancinha.plugins.omni.reporter.domain.reports.*
import org.jesperancinha.plugins.omni.reporter.pipelines.LocalPipeline
import org.jesperancinha.plugins.omni.reporter.transformers.ReportingParserToCoveralls
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Test
import java.io.File

internal class JacocoParserTest {
    private val jacocoParser = ReportingParserToCoveralls(
        "token",
        LocalPipeline(fetchBranchNameFromEnv = false),
        root,
        failOnUnknown = false,
        includeBranchCoverage = false,
        useCoverallsCount = false
    )

    @Test
    fun parseSourceFile() {
        val tmpdir = System.getProperty(TEMP_DIR_VARIABLE)


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
        val omniJacocoSourcefile = OmniJacocoSourcefile(
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
        val omniJacocoSourcefile2 = OmniJacocoSourcefile(
            name = "Racoons.kt",
            lines = listOf(element21, element22)
        )

        val pack = Package(
            name = "/",
            sourcefiles = listOf(omniJacocoSourcefile)
        )

        val pack2 = Package(
            name = "/",
            sourcefiles = listOf(omniJacocoSourcefile2)
        )

        val report = Report(packages = listOf(pack, pack2))

        val jacocoResult = File(tmpdir, "jacoco_test.xml")
        jacocoResult.writeText(xmlObjectMapper.writeValueAsString(report))

        print(jacocoResult)
        jacocoParser.parseInput(OmniJacocoFileAdapter(jacocoResult, false, root, root), listOf(root))
            .sourceFiles[0].coverage shouldBe arrayOf(20, 22, null, null, null)

        jacocoParser.parseInput(OmniJacocoFileAdapter(jacocoResult, false, root, root), listOf(root))
            .sourceFiles[0].coverage shouldBe arrayOf(40, 44, null, null, null)

        jacocoParser.parseInput(OmniJacocoFileAdapter(jacocoResult, false, root, root), listOf(root))

        val sourceFiles = jacocoParser.parseInput(
            OmniJacocoFileAdapter(jacocoResult, false, root, root)
        ).sourceFiles

        sourceFiles.shouldNotBeNull()
        sourceFiles.shouldHaveSize(1)
        val sourceFile = sourceFiles[0]
        sourceFile.name.shouldNotBeNull()
        sourceFile.sourceDigest.shouldNotBeNull()
        sourceFile.coverage shouldBe arrayOf(60, 66, null, null, null)
//        sourceFile.branches shouldBe arrayOf(2, 5, 3, 11)
//        sourceFile.source shouldBe File(root, "Racoons.kt").bufferedReader().use { it.readText() }
    }
}