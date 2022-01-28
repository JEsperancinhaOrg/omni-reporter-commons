package org.jesperancinha.plugins.omni.reporter

class ProjectDirectoryNotFoundException : RuntimeException()

class CoverallsUrlNotConfiguredException : RuntimeException()

class CodacyUrlNotConfiguredException : RuntimeException()

class CodecovUrlNotConfiguredException : RuntimeException()

class CoverallsReportNotGeneratedException(override val message: String? = null) : RuntimeException()

class CodacyReportNotGeneratedException(override val message: String? = null) : RuntimeException()

class CodecovPackageNotFoundException(override val message: String? = null) : RuntimeException()

class JacocoXmlParsingErrorException : RuntimeException()

class CoveragePyJsonParsingErrorException : RuntimeException()

class NullSourceFileException : RuntimeException()

class IncompleteCodacyApiTokenConfigurationException : RuntimeException()

class CoverallsTokenNotFoundException : RuntimeException()

class LanguageNotConfiguredException : RuntimeException()
/**
 * Definition of the Project
 */
interface OmniProject {
    val compileSourceRoots: List<String>?
    val build: OmniBuild?
}

/**
 * Definition of the Build
 */
interface OmniBuild {
    val testOutputDirectory: String
    val directory: String
}
