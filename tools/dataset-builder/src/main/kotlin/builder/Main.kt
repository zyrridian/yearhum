package builder

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.system.exitProcess

private const val USAGE = """
Usage: dataset-builder --from 2007 --to 2007 [options]
  --songs N          songs per year (default 100)
  --schema FILE      Room schema JSON (default app/schemas/.../1.json)
  --out FILE         asset DB (default app/src/main/assets/databases/capsules.db)
  --json FILE        JSON snapshot (default tools/dataset-builder/build/capsules.json)
  --cache DIR        HTTP cache dir (default tools/dataset-builder/.cache)
  --overrides FILE   overrides.yaml (default tools/dataset-builder/overrides.yaml)
  --review FILE      review report (default tools/dataset-builder/build/review.tsv)
  --min-resolution P fail if resolved ratio < P (default 0.85)
  --skip-mb          skip MusicBrainz queries (placeholder images only)
"""

private const val DATASET_VERSION = 1

fun main(args: Array<String>) {
    val opts = mutableMapOf<String, String>()
    var argIndex = 0
    while (argIndex < args.size) {
        val key = args[argIndex].removePrefix("--")
        if (argIndex + 1 < args.size && !args[argIndex + 1].startsWith("--")) {
            opts[key] = args[argIndex + 1]
            argIndex += 2
        } else {
            opts[key] = "true"
            argIndex += 1
        }
    }
    if ("help" in opts || "from" !in opts) {
        println(USAGE)
        return
    }
    val from = opts.getValue("from").toInt()
    val to = opts["to"]?.toInt() ?: from
    val songLimit = opts["songs"]?.toInt() ?: 100
    val schema = File(opts["schema"] ?: "app/schemas/com.example.yearhum.data.local.AppDatabase/1.json")
    val out = File(opts["out"] ?: "app/src/main/assets/databases/capsules.db")
    val jsonOut = File(opts["json"] ?: "tools/dataset-builder/build/capsules.json")
    val reviewOut = File(opts["review"] ?: "tools/dataset-builder/build/review.tsv")
    val overrides = Overrides.load(File(opts["overrides"] ?: "tools/dataset-builder/overrides.yaml"))
    val skipMb = opts["skip-mb"] == "true"
    val minResolution = if (skipMb) 0.0 else (opts["min-resolution"]?.toDouble() ?: 0.85)

    val http = CachedHttp(
        File(opts["cache"] ?: "tools/dataset-builder/.cache"),
        mapOf("musicbrainz.org" to 1_100L, "en.wikipedia.org" to 250L),
    )
    val wiki = WikipediaClient(http)
    val resolver = MusicBrainzResolver(http, overrides)
    val curated = Curated.load()

    val capsules = mutableListOf<BuiltCapsule>()
    val review = StringBuilder("year\tcategory\trank\ttitle\tartist\treason\tcandidates\n")
    var total = 0
    var resolved = 0
    for (year in from..to) {
        println("== $year")
        val raw = wiki.yearEndSongs(year, songLimit) + wiki.numberOneAlbums(year)
        if (raw.none { it.category == "SONG" }) {
            println("  no songs found, skipping year")
            continue
        }
        val items = mutableListOf<BuiltItem>()
        for (entry in raw) {
            total++
            if (skipMb) {
                items += BuiltItem(year, entry.category, entry.rank, entry.title, entry.artist, null)
            } else {
                when (val r = resolver.resolve(entry)) {
                    is Resolution.Resolved -> {
                        resolved++
                        items += BuiltItem(year, entry.category, entry.rank, entry.title, entry.artist, r.mbid)
                    }
                    is Resolution.Unresolved -> {
                        if (r.reason != "skipped by override") {
                            // Keep the item (it is still part of the year); it just gets a placeholder image.
                            items += BuiltItem(year, entry.category, entry.rank, entry.title, entry.artist, null)
                        }
                        val cands = r.candidates.take(3).joinToString(" | ") { "${it.mbid} ${it.title} / ${it.artist} (${it.score})" }
                        review.append("$year\t${entry.category}\t${entry.rank}\t${entry.title}\t${entry.artist}\t${r.reason}\t$cands\n")
                    }
                }
            }
        }
        val top = items.firstOrNull { it.category == "SONG" && it.rank == 1 }
        val headline = top?.let { "Year-end No. 1: \"${it.title}\" by ${it.subtitle}" } ?: "Music of $year"
        // Games, movies, TV and culture are curated by hand (see curated.tsv), not resolved via MusicBrainz.
        val extras = curated[year].orEmpty()
        items += extras
        capsules += BuiltCapsule(year, headline, items.sortedWith(compareBy({ it.category }, { it.rank })))
        println("  ${items.size} items (${extras.size} curated), resolved so far $resolved/$total")
    }
    if (capsules.isEmpty()) {
        System.err.println("Nothing built.")
        exitProcess(1)
    }
    reviewOut.parentFile.mkdirs()
    reviewOut.writeText(review.toString())
    jsonOut.parentFile.mkdirs()
    jsonOut.writeText(Json { prettyPrint = true }.encodeToString(Snapshot(DATASET_VERSION, capsules)))
    RoomAssetWriter.write(schema, out, capsules)

    val ratio = if (total > 0) resolved.toDouble() / total else 0.0
    println("Wrote ${out.path}: ${capsules.size} capsules, resolution ${"%.1f".format(ratio * 100)}% ($resolved/$total)")
    println("Review report: ${reviewOut.path}")
    val problems = Validator.validate(capsules, minSongs = minOf(songLimit, 20))
    problems.forEach { System.err.println("VALIDATION: $it") }
    if (problems.isNotEmpty() || (!skipMb && ratio < minResolution)) exitProcess(2)
}
