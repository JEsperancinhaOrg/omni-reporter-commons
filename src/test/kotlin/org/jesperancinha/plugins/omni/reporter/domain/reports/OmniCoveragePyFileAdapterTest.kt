package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldNotBeNull
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.io.path.toPath

internal class OmniCoveragePyFileAdapterTest {

    @Test
    fun `should generate Coverage PY test`() {
        val resource = javaClass.getResource("/coverage.json")
        resource.shouldNotBeNull()
        val coveragePy = resource.toURI().toPath().toFile()
        val omniCoveragePyFileAdapter = OmniCoveragePyFileAdapter(coveragePy, false, root, root)
        val generatePayload = omniCoveragePyFileAdapter.generatePayload(false, listOf(root))

        generatePayload.shouldNotBeNull()
    }
}