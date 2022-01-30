package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.toPath

internal class OmniLCovFileAdapterTest {

    @Test
    fun `should correct paths relative to root for LCov in Codecov report 2 sending`() {
        val resource = javaClass.getResource("/app1/app2/coverage/lcov.info")
        resource.shouldNotBeNull()
        val lcovFile = resource.toURI().toPath().toFile()
        val omniLCovFileAdapter = OmniLCovFileAdapter(lcovFile, false, root, File(root, "app1/app2"))
        val generatePayload =
            omniLCovFileAdapter.generatePayload(false, listOf(File(root, "app1/app22"), File(root, "app1/app2")))

        generatePayload.shouldContain("SF:app1/app2/src/App.tsx")

    }

    @Test
    fun `should correct paths relative to root for LCov in Codecov report 22 sending`() {
        val resource = javaClass.getResource("/app1/app22/coverage/lcov.info")
        resource.shouldNotBeNull()
        val lcovFile = resource.toURI().toPath().toFile()
        val omniLCovFileAdapter = OmniLCovFileAdapter(lcovFile, false, root, File(root, "app1/app22"))
        val generatePayload =
            omniLCovFileAdapter.generatePayload(false, listOf(File(root, "app1/app2"), File(root, "app1/app22")))

        generatePayload.shouldContain("SF:app1/app22/src/App.tsx")

    }
}