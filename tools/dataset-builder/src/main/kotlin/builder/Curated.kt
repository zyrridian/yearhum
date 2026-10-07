package builder

/**
 * Hand-curated games, movies, TV and culture items bundled as `curated.tsv`.
 * They carry no MusicBrainz id, so the app shows generated placeholders for them.
 */
object Curated {
    val CATEGORIES = setOf("GAME", "MOVIE", "TV", "EVENT")

    fun parse(text: String): List<BuiltItem> = text.lines().map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapIndexed { index, line ->
            val parts = line.split("|").map { it.trim() }
            require(parts.size >= 4) { "curated.tsv entry #${index + 1} is malformed: '$line'" }
            require(parts[1] in CATEGORIES) { "curated.tsv entry #${index + 1} has unknown category '${parts[1]}'" }
            BuiltItem(
                year = parts[0].toInt(),
                category = parts[1],
                rank = parts[2].toInt(),
                title = parts[3],
                subtitle = parts.getOrNull(4)?.ifEmpty { null },
                mbid = null,
            )
        }

    fun load(): Map<Int, List<BuiltItem>> {
        val stream = Curated::class.java.getResourceAsStream("/curated.tsv")
            ?: error("curated.tsv is missing from the dataset-builder resources")
        return parse(stream.bufferedReader(Charsets.UTF_8).use { it.readText() }).groupBy { it.year }
    }
}
