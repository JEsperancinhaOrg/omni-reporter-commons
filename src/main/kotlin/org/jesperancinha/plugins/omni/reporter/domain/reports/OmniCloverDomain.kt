package org.jesperancinha.plugins.omni.reporter.domain.reports

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


data class CloverLine(
    @JacksonXmlProperty(localName = "num", isAttribute = true)
    val num: Int,
    @JacksonXmlProperty(localName = "count", isAttribute = true)
    val count: Int,
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    val type: String
)

data class CloverFile(
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String,
    @JacksonXmlProperty(localName = "path", isAttribute = true)
    val path: String,
    val line: CloverLine
)

data class Metrics(
    @JacksonXmlProperty(localName = "statements", isAttribute = true)
    val statements: Int,
    @JacksonXmlProperty(localName = "coveredstatements", isAttribute = true)
    val coveredstatements: Int,
    @JacksonXmlProperty(localName = "conditionals", isAttribute = true)
    val conditionals: Int,
    @JacksonXmlProperty(localName = "coveredconditionals", isAttribute = true)
    val coveredconditionals: Int,
    @JacksonXmlProperty(localName = "methods", isAttribute = true)
    val methods: Int,
    @JacksonXmlProperty(localName = "coveredmethods", isAttribute = true)
    val coveredmethods: Int
)

data class Project(
    @JacksonXmlProperty(localName = "timestamp", isAttribute = true)
    val timestamp: String,
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String,
    val metrics: Metrics,
    @JsonProperty("package")
    val files: List<CloverFile> = emptyList(),
)

/**
 * Created by jofisaes on 26/01/2022
 */
@JsonRootName("coverage")
@JacksonXmlRootElement
data class OmniCloverReport(
    @JacksonXmlProperty(localName = "generated", isAttribute = true)
    val generated: String,
    @JacksonXmlProperty(localName = "clover", isAttribute = true)
    val clover: String,
    val project: Project
)