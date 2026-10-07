package builder

import java.io.File

/**
 * Minimal YAML-subset overrides file: one `"key": value` per line, `#` comments.
 *   "2007|SONG|Umbrella|Rihanna featuring Jay-Z": 0c1d2e3f-0000-0000-0000-000000000000
 *   "1999|SONG|Some Title|Some Artist": skip      # drop item from the dataset
 */
object Overrides {
    private val line = Regex("^\"(.+)\":\\s*([0-9a-fA-F-]{36}|skip)\\s*(#.*)?$")

    fun parse(text: String): Map<String, String> = text.lines().map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { l -> line.matchEntire(l)?.let { it.groupValues[1] to it.groupValues[2].lowercase() } }
        .toMap()

    fun load(file: File): Map<String, String> = if (file.exists()) parse(file.readText()) else emptyMap()
}
