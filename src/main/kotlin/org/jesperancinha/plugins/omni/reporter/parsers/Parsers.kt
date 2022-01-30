package org.jesperancinha.plugins.omni.reporter.parsers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.InputStream
import java.security.MessageDigest


val xmlObjectMapper: ObjectMapper = XmlMapper(JacksonXmlModule().apply {
    setDefaultUseWrapper(false)
}).registerKotlinModule()
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

val snakeCaseJsonObjectMapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategies.SnakeCaseStrategy()
}

val camelCaseJsonObjectMapper = jacksonObjectMapper().apply {
    propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
}

internal inline fun <reified T : Any> readXmlValue(inputStream: InputStream): T {
    return xmlObjectMapper.readValue(inputStream)
}

internal inline fun <reified T : Any> readJsonValue(inputStream: InputStream): T {
    return snakeCaseJsonObjectMapper.readValue(inputStream)
}

internal inline fun <reified T : Any> readJsonValue(byteArray: ByteArray): T {
    return snakeCaseJsonObjectMapper.readValue(byteArray)
}

internal inline fun <reified T : Any> writeSnakeCaseJsonValueAsString(objectValue: T): String {
    return snakeCaseJsonObjectMapper.writeValueAsString(objectValue)
}

internal inline fun <reified T : Any> writeCamelCaseJsonValueAsString(objectValue: T): String {
    return camelCaseJsonObjectMapper.writeValueAsString(objectValue)
}

internal val messageDigester: MessageDigest = MessageDigest.getInstance("MD5")

internal val String.toFileDigest: String
    get() = messageDigester.digest(toByteArray())
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
        .uppercase()

enum class Language(val ext: String, val lang: String) {
    JAVA("java", "Java"),
    KOTLIN("kt", "Kotlin"),
    SCALA("scala", "Scala"),
    PYTHON("py", "Python"),
    JAVASCRIPT("js", "JavaScript"),
    TYPESCRIPT("ts", "TypeScript")
}
