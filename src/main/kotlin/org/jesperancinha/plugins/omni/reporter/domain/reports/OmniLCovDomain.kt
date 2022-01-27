package org.jesperancinha.plugins.omni.reporter.domain.reports

import com.fasterxml.jackson.module.kotlin.readValue
import org.jesperancinha.plugins.omni.reporter.parsers.xmlObjectMapper
import java.io.InputStream


data class LineData(
    /**
     * Line Number
     */
    val lineNumber: Int,
    /**
     * Hit Count
     */
    val hitCount: Int
)

data class Function(
    /**
     * Line Number
     */
    val line: Int,
    /**
     * Function Name
     */
    val name: String
)

data class BranchData(
    /**
     * Line Number
     */
    val line: Int,
    /**
     * Block Number
     */
    val block: Int,
    /**
     * Number of Expressions
     */
    val expressions: Int,
    /**
     * Blocck Count
     */
    val count: Int
)

/**
 * Created by jofisaes on 26/01/2022
 */
data class OmniLCovReport(
    /**
     * TN
     */
    val testName: String,
    /**
     * SF
     */
    val sourceFilePath: String,
    /**
     * FN
     */
    val functionNames: List<Function>,
    /**
     * FNF
     */
    val functionNumberFound: Int,
    /**
     * FNH
     */
    val functionNumberHits: Int,
    /**
     * FNDA
     */
    val functionNameData: List<Function>,
    /**
     * BRDA
     */
    val branchData: List<BranchData>,
    /**
     * BRF
     */
    val branchFound: Int,
    /**
     * BRF
     */
    val branchHit: Int,
    /**
     * DA
     */
    val lineData: List<LineData>,
    /**
     * LF
     */
    val linesFound: Int,
    /**
     * LH
     */
    val linesHit: Int
)


fun InputStream.readLCovReport(failOnXmlParseError: Boolean = false): List<OmniLCovReport> = bufferedReader()
    .use { it.readText() }
    .split("end_of_record")
    .filter { it.trim().isNotEmpty() }
    .map { testFileText ->
        val allLines = testFileText.split("\n")
        val testName = allLines.lCovValue("TN")
        val sourceFile = allLines.lCovValue("SF")
        val functionNames: List<Function> = allLines.lCovValues("FN") { text ->
            text.split(",").let { line -> Function(line[0].toInt(), line[1]) }
        }
        val functionNumberFound = allLines.lCovValue("FNF").toInt()
        val functionNumberHits = allLines.lCovValue("FNH").toInt()
        val functionNameData: List<Function> = allLines.lCovValues("FNDA") { text ->
            text.split(",").let { line -> Function(line[0].toInt(), line[1]) }
        }
        val lineData = allLines.lCovValues("DA") { text ->
            text.split(",").let { line -> LineData(line[0].toInt(), line[1].toInt()) }
        }
        val linesFound = allLines.lCovValue("LF").toInt()
        val linesHit = allLines.lCovValue("LH").toInt()
        val branchData = allLines.lCovValues("BRDA") { text ->
            text.split(",")
                .let { line -> BranchData(line[0].toInt(), line[1].toInt(), line[2].toInt(), line[3].toInt()) }
        }
        val branchFound = allLines.lCovValue("BRF").toInt()
        val branchHit = allLines.lCovValue("BRF").toInt()
        OmniLCovReport(
            testName = testName,
            sourceFilePath = sourceFile,
            functionNames = functionNames,
            functionNumberFound = functionNumberFound,
            functionNumberHits = functionNumberHits,
            functionNameData = functionNameData,
            lineData = lineData,
            linesFound = linesFound,
            linesHit = linesHit,
            branchData = branchData,
            branchFound = branchFound,
            branchHit = branchHit
        )
    }

private fun <T> List<String>.lCovValues(param: String, function: (String) -> T): List<T> =
    filter { it.startsWith("$param:") }.map { function(it.split(":")[1]) }

internal inline fun <reified T : Any> readXmlValue(inputStream: InputStream): T {
    return xmlObjectMapper.readValue(inputStream)
}

fun List<String>.lCovValue(param: String): String =
    first { it.startsWith(param) }.split(":")[1]
