package org.jesperancinha.plugins.omni.reporter.utils

import java.io.File
import java.io.FileNotFoundException

class Utils {
    companion object {
        val root = File(
            Utils::class.java.getResource("/")?.toURI() ?: throw FileNotFoundException()
        )
    }
}