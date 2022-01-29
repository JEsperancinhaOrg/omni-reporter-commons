package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.shouldNotBe
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.toPath

internal class OmniCloverReportFileAdapterTest {

    @Test
    fun `should show relative path`() {
        val resource = javaClass.getResource("/src/app/App.tsx")
        resource.shouldNotBeNull()
        val appFile = resource.toURI().toPath().absolutePathString()
        val omniCloverReportFileAdapter = OmniCloverReportFileAdapter(CloverFile(name = "name", path = appFile), root, root)

        omniCloverReportFileAdapter.name() shouldBe "src/app/App.tsx"
    }
}