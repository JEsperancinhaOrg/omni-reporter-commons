package org.jesperancinha.plugins.omni.reporter.transformers

import org.jesperancinha.plugins.omni.reporter.domain.api.CodacyApiTokenConfig
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniJacocoSourcefile
import org.jesperancinha.plugins.omni.reporter.domain.reports.OmniReportFileAdapter
import org.jesperancinha.plugins.omni.reporter.pipelines.Pipeline
import org.jesperancinha.plugins.omni.reporter.repository.GitRepository
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

class SourceCodeFile(projectBaseDir: File, val packageName: String?, sourceFile: OmniReportFileAdapter) :
    File(projectBaseDir, "${(packageName ?: "").replace("//", "/")}/${sourceFile.name()}")

fun Sequence<Pair<String?, List<OmniReportFileAdapter>>>.mapToGenericSourceCodeFiles(
    compiledSourcesDirs: List<File>,
    failOnUnknownPredicateFilePack: (List<Pair<SourceCodeFile, OmniReportFileAdapter>>, List<OmniReportFileAdapter>) -> Boolean
): Sequence<Pair<SourceCodeFile, OmniReportFileAdapter>> = flatMap { (packageName, sourceFiles) ->
    val foundSources = sourceFiles.map { omniJacocoSourceFile ->
        compiledSourcesDirs.map { compiledSourcesDir ->
            SourceCodeFile(compiledSourcesDir, packageName, omniJacocoSourceFile)
        }.filter { it.exists() }.map { sourceCodeFile -> sourceCodeFile to omniJacocoSourceFile }
    }.flatten()
    if (foundSources.size != sourceFiles.size) {
        failOnUnknownPredicateFilePack(foundSources, sourceFiles)
    }
    foundSources
}


/**
 * Created by jofisaes on 05/01/2022
 */
interface OmniReportParser<INPUT, OUTPUT> {
    fun parseInput(input: INPUT, compiledSourcesDirs: List<File> = emptyList()): OUTPUT
}

abstract class OmniReporterParserImpl<INPUT, OUTPUT>(
    internal val token: String?,
    internal val apiToken: CodacyApiTokenConfig? = null,
    internal val pipeline: Pipeline,
    internal val root: File,
    internal val includeBranchCoverage: Boolean = false,
) : OmniReportParser<INPUT, OUTPUT> {
    val gitRepository = GitRepository(root, pipeline)

    companion object {
        private val logger = LoggerFactory.getLogger(OmniReporterParserImpl::class.java)

        internal fun createFailOnUnknownPredicateFilePack(failOnUnknown: Boolean) =
            { foundSources: List<Pair<SourceCodeFile, OmniReportFileAdapter>>, sourceFiles: List<OmniReportFileAdapter> ->
                val jacocoSourcesFound = foundSources.map { (_, foundJacocoFile) -> foundJacocoFile }
                val sourceFilesNotFound = sourceFiles.filter { !jacocoSourcesFound.contains(it) }
                sourceFilesNotFound
                    .forEach { foundSource ->
                        logger.warn("File ${foundSource.name()} has not been found. Please activate flag `failOnUnknown` in your maven configuration if you want reporting to fail in these cases.")
                        logger.warn("Files not found are not included in the complete coverage report. They are sometimes included in the report due to bugs from reporting frameworks and in those cases it is safe to ignore them")
                    }
                if (failOnUnknown) {
                    logger.error("Stopping build due to one or more files not being found")
                    throw FileNotFoundException()
                }
                sourceFilesNotFound.isEmpty()
            }

        internal fun createFailOnUnknownPredicate(failOnUnknown: Boolean) = if (failOnUnknown) { file: File ->
            if (!file.exists()) throw FileNotFoundException(file.absolutePath) else true
        } else { file: File ->
            if (!file.exists()) {
                logger.warn("File ${file.absolutePath} has not been found. Please activate flag `failOnUnknown` in your maven configuration if you want reporting to fail in these cases.")
                logger.warn("Files not found are not included in the complete coverage report. They are sometimes included in the report due to bugs from reporting frameworks and in those cases it is safe to ignore them")
            }
            file.exists()
        }
    }
}