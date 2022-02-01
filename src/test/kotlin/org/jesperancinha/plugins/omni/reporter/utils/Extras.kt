package org.jesperancinha.plugins.omni.reporter.utils

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Created by jofisaes on 01/02/2022
 */
internal class Extras {

    @Test
    fun `should encode decode URL`() {
        val buildUrl =
            "http://bitbucket.org/jesperancinha/international-airports-service-root/addon/pipelines/home%23!/results/364&service=custom"

        val encodedBuildUrl = URLEncoder.encode(buildUrl, Charsets.UTF_8.name())
        encodedBuildUrl shouldBe "http%3A%2F%2Fbitbucket.org%2Fjesperancinha%2Finternational-airports-service-root%2Faddon%2Fpipelines%2Fhome%2523%21%2Fresults%2F364%26service%3Dcustom"

        val decodedBuildUrl = URLDecoder.decode(encodedBuildUrl, Charsets.UTF_8.name())
        decodedBuildUrl shouldBe buildUrl
    }
}