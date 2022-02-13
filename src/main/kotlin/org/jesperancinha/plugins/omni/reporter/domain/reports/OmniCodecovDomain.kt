package org.jesperancinha.plugins.omni.reporter.domain.reports

/**
 * Created by jofisaes on 30/01/2022
 */
data class CodecovReport(
    val coverage: Map<String, Map<String, Int?>>
)

