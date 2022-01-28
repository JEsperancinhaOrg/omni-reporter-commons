package org.jesperancinha.plugins.omni.reporter.domain.reports

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jesperancinha.plugins.omni.reporter.utils.Utils.Companion.root
import org.junit.jupiter.api.Test
import java.io.File

internal class OmniFileAdaptersKtTest {

    @Test
    fun `should find classes file classes jar`() {
        val jarFile = File(root, "jacoco/exec/one").findJarFile()
        jarFile.shouldNotBeNull()
        jarFile.name shouldBe "classes.jar"
    }

    @Test
    fun `should find nothing`() {
        val jarFile = File(root, "jacoco/exec/two").findJarFile()
        jarFile.shouldBeNull()
    }

    @Test
    fun `should find string paradigm expression api 0 0 0 SNAPSHOT jar`() {
        val jarFile = File(root, "jacoco/exec/three").findJarFile()
        jarFile.shouldNotBeNull()
        jarFile.name shouldBe "string-paradigm-expression-api-0.0.0-SNAPSHOT.jar"
    }

}