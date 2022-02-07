package org.jesperancinha.plugins.omni.reporter

import java.io.File

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
    val compileSourceRoots: MutableList<String>?
    val build: OmniBuild?
}

/**
 * Definition of the Build
 */
interface OmniBuild {
    val testOutputDirectory: String
    val directory: String
}

fun List<OmniProject>.injectExtraSourceFiles(extraSourceFolders: List<File>, root: File): List<OmniProject> =
    this.map { project ->
        val extraSourcesFoldersFound =
            extraSourceFolders.filter { sourceFolder ->
                !root.toPath().relativize(sourceFolder.toPath()).toString().contains("..")
            }
        project.compileSourceRoots?.addAll(extraSourcesFoldersFound.map { foundSourceFolder -> foundSourceFolder.absolutePath }
            .toMutableList())
        project
    }

fun List<File>.toExtraProjects(extraSourceFolders: List<File>): List<OmniProject> =
    map {
        OmniProjectGeneric(
            extraSourceFolders.map { src -> src.absolutePath }.toMutableList(),
            OmniBuildGeneric(it.absolutePath, it.absolutePath)
        )
    }

class OmniProjectGeneric(
    override val compileSourceRoots: MutableList<String> = mutableListOf(),
    override val build: OmniBuildGeneric? = null
) : OmniProject

class OmniBuildGeneric(
    override val testOutputDirectory: String = "",
    override val directory: String = ""
) : OmniBuild