package builder

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class Candidate(val mbid: String, val title: String, val artist: String, val date: String?, val score: Int)

sealed interface Resolution {
    data class Resolved(val mbid: String, val matched: Candidate) : Resolution

    data class Unresolved(val reason: String, val candidates: List<Candidate>) : Resolution
}

object Matching {
    private val featuring = Regex("\\s+(featuring|feat\\.?|ft\\.?|with|and his|and her)\\s+.*$", RegexOption.IGNORE_CASE)

    fun leadArtist(artist: String): String = artist.replace(featuring, "").trim()

    fun normalize(s: String): String = java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
        .replace(Regex("\\(.*?\\)|\\[.*?]"), " ")
        .replace("&", " and ")
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()

    fun escapeLucene(s: String): String = s.replace(Regex("[+\\-&|!(){}\\[\\]^\"~*?:\\\\/]"), " ")
        .replace(Regex("\\s+"), " ").trim()

    fun titlesMatch(wanted: String, found: String): Boolean {
        val a = normalize(wanted)
        val b = normalize(found)
        return a.isNotEmpty() && (a == b || b.startsWith("$a ") || a.startsWith("$b "))
    }

    fun artistsMatch(wanted: String, found: String): Boolean {
        val a = normalize(leadArtist(wanted))
        val b = normalize(found)
        if (a.isEmpty() || b.isEmpty()) return false
        return b.contains(a) || a.contains(b) || normalize(wanted).split(" ").toSet().let { w ->
            b.split(" ").count { it in w } >= minOf(2, b.split(" ").size)
        }
    }

    fun yearOf(date: String?): Int? = date?.take(4)?.toIntOrNull()
}

class MusicBrainzResolver(private val http: CachedHttp, private val overrides: Map<String, String>) {
    fun resolve(entry: RawEntry): Resolution {
        overrides[overrideKey(entry)]?.let { value ->
            return if (value == "skip") {
                Resolution.Unresolved("skipped by override", emptyList())
            } else {
                Resolution.Resolved(value, Candidate(value, entry.title, entry.artist, null, 100))
            }
        }
        val lead = Matching.leadArtist(entry.artist)
        val types = if (entry.category == "SONG") listOf("single", null) else listOf("album", null)
        val all = mutableListOf<Candidate>()
        for (type in types) {
            val candidates = search(entry.title, lead, type)
            all += candidates
            pick(entry, candidates)?.let { return Resolution.Resolved(it.mbid, it) }
        }
        return Resolution.Unresolved(if (all.isEmpty()) "no results" else "no confident match", all.distinctBy { it.mbid })
    }

    private fun pick(entry: RawEntry, candidates: List<Candidate>): Candidate? {
        val valid = candidates.filter {
            it.score >= MIN_SCORE &&
                Matching.titlesMatch(entry.title, it.title) &&
                Matching.artistsMatch(entry.artist, it.artist)
        }
        // Prefer a release near the chart year; fall back to the best-scored match.
        val maxGap = if (entry.category == "SONG") SONG_YEAR_WINDOW else ALBUM_YEAR_WINDOW
        return valid.filter { c -> Matching.yearOf(c.date)?.let { it in (entry.year - maxGap)..(entry.year + 1) } == true }
            .maxByOrNull { it.score }
            ?: valid.maxByOrNull { it.score }
    }

    private fun search(title: String, artist: String, primaryType: String?): List<Candidate> {
        val query = buildString {
            append("releasegroup:\"${Matching.escapeLucene(title)}\" AND artist:\"${Matching.escapeLucene(artist)}\"")
            if (primaryType != null) append(" AND primarytype:$primaryType")
        }
        val url = "https://musicbrainz.org/ws/2/release-group/?query=${query.urlEncode()}&limit=10&fmt=json"
        val body = http.get(url) ?: return emptyList()
        val groups: JsonArray = Json.parseToJsonElement(body).jsonObject["release-groups"]?.jsonArray ?: return emptyList()
        return groups.map { it.jsonObject }.map { g -> toCandidate(g) }
    }

    private fun toCandidate(g: JsonObject) = Candidate(
        mbid = g["id"]!!.jsonPrimitive.content,
        title = g["title"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        artist = g["artist-credit"]?.jsonArray?.joinToString(" ") { c ->
            c.jsonObject["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
        }.orEmpty(),
        date = g["first-release-date"]?.jsonPrimitive?.contentOrNull,
        score = g["score"]?.jsonPrimitive?.intOrNull ?: 0,
    )

    companion object {
        const val MIN_SCORE = 90
        const val SONG_YEAR_WINDOW = 3
        const val ALBUM_YEAR_WINDOW = 3

        fun overrideKey(e: RawEntry) = "${e.year}|${e.category}|${e.title}|${e.artist}"
    }
}
