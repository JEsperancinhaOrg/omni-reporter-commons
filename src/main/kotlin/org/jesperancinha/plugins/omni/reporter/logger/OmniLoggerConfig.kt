package org.jesperancinha.plugins.omni.reporter.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Created by jofisaes on 13/02/2022
 */
class OmniLoggerConfig(
    val java: Class<*>,
    val logger: Logger = LoggerFactory.getLogger(java),
    private val environment: MutableMap<String, String> = System.getenv()
) {

    private fun appendType(type: String) {
        if (environment[OMNI_LOG].toBoolean()) {
            file?.write("$type ".toByteArray())
        }
    }

    private fun appendMessage(message: String?) {
        if (environment[OMNI_LOG].toBoolean()) {
            file?.write((message ?: "").toByteArray())
            file?.write("\n".toByteArray())
        }
    }

    fun info(message: String?) {
        logger.info(message)
        appendType("[info]")
        appendMessage(message)
    }

    fun error(message: String, exception: Exception) {
        logger.error(message, exception)
        appendType("[error]")
        appendMessage(message)
    }

    fun error(message: String) {
        logger.error(message)
        appendType("[error]")
        appendMessage(message)
    }

    fun warn(message: String) {
        logger.warn(message)
        appendType("[warn]")
        appendMessage(message)
    }

    fun debug(message: String) {
        logger.debug(message)
        appendType("[debug]")
        appendMessage(message)
    }

    companion object {
        const val OMNI_LOG = "OMNI_LOG"
        fun <T> getLogger(java: Class<T>) = OmniLoggerConfig(java)
        val file = if (System.getenv()[OMNI_LOG].toBoolean()) File("target/omni.log").outputStream() else null
    }
}