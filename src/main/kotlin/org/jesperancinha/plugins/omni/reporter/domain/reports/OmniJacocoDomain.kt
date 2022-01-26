package org.jesperancinha.plugins.omni.reporter.domain.reports

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import org.jesperancinha.plugins.omni.reporter.JacocoXmlParsingErrorException
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniJacocoDomain.Companion.logger
import org.jesperancinha.plugins.omni.reporter.parsers.readXmlValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream

/**
 * Created by jofisaes on 04/01/2022
 */

data class Class(
    @JsonProperty("method")
    val methods: List<Method> = emptyList(),
    @JsonProperty("counter")
    val counters: List<Counter> = emptyList(),
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String? = null,
    @JacksonXmlProperty(localName = "sourcefilename", isAttribute = true)
    val sourcefilename: String? = null
)

data class Counter(
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    val type: String? = null,
    @JacksonXmlProperty(localName = "missed", isAttribute = true)
    val missed: Int,
    @JacksonXmlProperty(localName = "covered", isAttribute = true)
    val covered: Int
)

data class Line(
    @JacksonXmlProperty(localName = "nr", isAttribute = true)
    val nr: Int,
    @JacksonXmlProperty(localName = "mi", isAttribute = true)
    val mi: Int = 0,
    @JacksonXmlProperty(localName = "ci", isAttribute = true)
    val ci: Int,
    @JacksonXmlProperty(localName = "mb", isAttribute = true)
    val mb: Int = 0,
    @JacksonXmlProperty(localName = "cb", isAttribute = true)
    val cb: Int = 0
)

data class Method(
    @JsonProperty("counter")
    val counters: List<Counter> = emptyList(),
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String? = null,
    @JacksonXmlProperty(localName = "desc", isAttribute = true)
    val desc: String? = null,
    @JacksonXmlProperty(localName = "line", isAttribute = true)
    val line: String? = null
)

data class Package(
    @JsonProperty("class")
    val clazzs: List<Class> = emptyList(),
    @JsonProperty("sourcefile")
    val sourcefiles: List<Sourcefile> = emptyList(),
    @JsonProperty("counter")
    val counters: List<Counter> = emptyList(),
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String
)

data class Sessioninfo(
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    val id: String? = null,
    @JacksonXmlProperty(localName = "start", isAttribute = true)
    val start: String? = null,
    @JacksonXmlProperty(localName = "dump", isAttribute = true)
    val dump: String? = null
)

data class Sourcefile(
    @JsonProperty("line")
    val lines: List<Line> = emptyList(),
    @JsonProperty("counter")
    val counters: List<Counter> = emptyList(),
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String
)

@JsonRootName("report")
@JacksonXmlRootElement
data class Report(
    val sessioninfo: Sessioninfo? = null,
    @JsonProperty("package")
    val packages: List<Package> = emptyList(),
    @JsonProperty("counter")
    val counters: List<Counter> = emptyList(),
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    val name: String? = null
)

class OmniJacocoDomain {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(OmniJacocoDomain::class.java)
    }
}

fun InputStream.readJacocoPackages(failOnXmlParseError: Boolean) =
    readXmlValue<Report>(this).packages.apply {
        if (isEmpty()) {
            logger.warn("Failed to process Jacoco file!")
            if (failOnXmlParseError)
                throw JacocoXmlParsingErrorException()
        }
    }

fun InputStream.readReport(failOnXmlParseError: Boolean) =
    readXmlValue<Report>(this).apply {
        if (packages.isEmpty()) {
            logger.warn("Failed to process Jacoco file!")
            if (failOnXmlParseError)
                throw JacocoXmlParsingErrorException()
        }
    }
