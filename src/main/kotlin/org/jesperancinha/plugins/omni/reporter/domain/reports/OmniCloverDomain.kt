package org.jesperancinha.plugins.omni.reporter.domain.reports

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.jesperancinha.plugins.omni.reporter.JacocoXmlParsingErrorException
import java.io.InputStream


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
    val metrics: Metrics = Metrics(),
    @JsonProperty("line")
    val cloverLines: List<CloverLine> = emptyList()
)

data class Metrics(
    @JacksonXmlProperty(localName = "statements", isAttribute = true)
    val statements: Int = 0,
    @JacksonXmlProperty(localName = "coveredstatements", isAttribute = true)
    val coveredstatements: Int = 0,
    @JacksonXmlProperty(localName = "conditionals", isAttribute = true)
    val conditionals: Int = 0,
    @JacksonXmlProperty(localName = "coveredconditionals", isAttribute = true)
    val coveredconditionals: Int = 0,
    @JacksonXmlProperty(localName = "methods", isAttribute = true)
    val methods: Int = 0,
    @JacksonXmlProperty(localName = "coveredmethods", isAttribute = true)
    val coveredmethods: Int = 0
)

data class Project(
    @JacksonXmlProperty(localName = "timestamp", isAttribute = true)
    val timestamp: String,
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String,
    val metrics: Metrics,
    @JsonProperty("file")
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


fun InputStream.readCloverReport(failOnXmlParseError: Boolean = false) =
    org.jesperancinha.plugins.omni.reporter.parsers.readXmlValue<OmniCloverReport>(this).apply {
        if (project.files.isEmpty()) {
            OmniJacocoDomain.logger.warn("Failed to process Clover file!")
            if (failOnXmlParseError)
                throw JacocoXmlParsingErrorException()
        }
    }
