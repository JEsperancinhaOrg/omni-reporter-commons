package org.jesperancinha.plugins.omni.reporter.domain.reports

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

data class CloverFile(
    val name: String,
    val path: String
)

data class Metrics(
    val statements: Int,
    val coveredstatements: Int,
    val conditionals: Int,
    val coveredconditionals: Int,
    val methods: Int,
    val coveredmethods: Int
)

data class Project(
    val timestamp: String,
    val name: String,
    val metrics: Metrics,
    val file: CloverFile,
)

/**
 * Created by jofisaes on 26/01/2022
 */
@JsonRootName("coverage")
@JacksonXmlRootElement
data class OmniCloverReport(
    val generated: String,
    val clover: String,
    val project: Project
)