package org.jesperancinha.plugins.omni.reporter.utils

import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import kotlin.io.path.toPath

class Utils {
    companion object {
        val root = File(
            Utils::class.java.getResource("/")?.toURI() ?: throw FileNotFoundException()
        )
    }
}

fun URL.toFile(): File = this.toURI().toPath().toFile()
