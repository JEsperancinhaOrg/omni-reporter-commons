package org.jesperancinha.plugins.omni.reporter.processors

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import org.jesperancinha.plugins.omni.reporter.OmniBuild
import org.jesperancinha.plugins.omni.reporter.OmniProject
import org.jesperancinha.plugins.omni.reporter.processors.Processor.Companion.nonTestDirectoryPredicate
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.toPath

internal class ProcessorKtTest {

    @Test
    fun `should find clover xml file`() {
        val thisRoot = javaClass.getResource("/root")
        thisRoot.shouldNotBeNull()
        val rootFolder = thisRoot.toURI().toPath().toFile()
        val test3Folder = File(rootFolder, "test3").absolutePath
        val omniProject = MavenOmniProject(mutableListOf(test3Folder), MavenOmniBuild(test3Folder, test3Folder))
        val supportedPredicate = { _: String, _: File -> true }
        val reportRejectList = emptyList<String>()

        val toReportFiles = listOf(omniProject).toReportFiles(supportedPredicate, false, rootFolder, reportRejectList, 4)

        toReportFiles.shouldNotBeNull()
            .entries.first().value.shouldHaveSize(1)
    }

    @Test
    fun `should find clover xml file with predicate`() {
        val thisRoot = javaClass.getResource("/root")
        thisRoot.shouldNotBeNull()
        val rootFolder = thisRoot.toURI().toPath().toFile()
        val test3Folder = File(rootFolder, "test3").absolutePath
        val omniProject = MavenOmniProject(mutableListOf(test3Folder), MavenOmniBuild(test3Folder, test3Folder))
        val reportRejectList = emptyList<String>()

        val toReportFiles = listOf(omniProject).toReportFiles(
            nonTestDirectoryPredicate,
            false,
            rootFolder,
            reportRejectList,
            4
        )
        toReportFiles.shouldNotBeNull()
        toReportFiles.entries.first().value.shouldHaveSize(1)
    }
}

private class MavenOmniBuild(
    override val testOutputDirectory: String,
    override val directory: String
) : OmniBuild

private class MavenOmniProject(
    override val compileSourceRoots: MutableList<String>?,
    override val build: OmniBuild?
) : OmniProject
