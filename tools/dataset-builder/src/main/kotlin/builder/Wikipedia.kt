package builder

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object WikiText {
    /** Strips wiki markup from a table cell down to display text. */
    fun clean(raw: String): String {
        var s = raw
        s = s.replace(Regex("<ref[^>]*?/>"), "")
        s = s.replace(Regex("<ref[^>]*>.*?</ref>", RegexOption.DOT_MATCHES_ALL), "")
        s = s.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        s = s.replace(Regex("<br\\s*/?>"), " ")
        s = s.replace(Regex("<[^>]+>"), "")
        // {{sort|key|text}} style templates: keep the last argument; drop everything else.
        repeat(3) { s = s.replace(Regex("\\{\\{[^{}]*\\}\\}")) { m -> templateText(m.value) } }
        s = s.replace(Regex("\\[\\[File:[^\\]]*\\]\\]"), "")
        s = s.replace(Regex("\\[\\[([^\\]|]*)\\|([^\\]]*)\\]\\]")) { it.groupValues[2] }
        s = s.replace(Regex("\\[\\[([^\\]]*)\\]\\]")) { it.groupValues[1] }
        s = s.replace(Regex("\\[https?://[^\\s\\]]+\\s*([^\\]]*)\\]"), "$1")
        s = s.replace("'''", "").replace("''", "")
        s = s.replace("&amp;", "&").replace("&nbsp;", " ").replace("&quot;", "\"").replace("&#39;", "'")
        s = s.replace("&ndash;", "–").replace("&mdash;", "—")
        return s.replace(Regex("\\s+"), " ").trim().trim('"', '“', '”').trim()
    }

    private fun templateText(t: String): String {
        val parts = t.removePrefix("{{").removeSuffix("}}").split("|")
        return when (parts.first().trim().lowercase()) {
            "sort", "sortname", "nowrap", "small", "sc" -> parts.last()
            "nbsp" -> " "
            else -> ""
        }
    }

    /** Splits a wikitext table into rows of cell strings. */
    fun tableRows(wikitext: String): List<List<String>> {
        val body = wikitext.substringAfter("{|", wikitext)
        return body.split(Regex("(?m)^\\|-.*$")).map { block ->
            val cells = mutableListOf<String>()
            for (line in block.lines()) {
                val l = line.trim()
                when {
                    l.startsWith("|}") -> break
                    l.startsWith("!") || (l.startsWith("|") && !l.startsWith("|+") && !l.startsWith("|-")) -> {
                        val content = l.drop(1)
                        val separator = if (l.startsWith("!")) "!!" else "||"
                        content.split(separator).forEach { cells.add(stripAttributes(it)) }
                    }
                    cells.isNotEmpty() && l.isNotEmpty() -> cells[cells.lastIndex] = cells.last() + " " + l
                }
            }
            cells
        }.filter { it.isNotEmpty() }
    }

    /** `scope="row" | text` or `style="x"| text` → `text` (but keep `[[a|b]]` links intact). */
    private fun stripAttributes(cell: String): String {
        val idx = cell.indexOf('|')
        if (idx < 0) return cell.trim()
        val before = cell.substring(0, idx)
        return if (before.contains("[[") || before.contains("{{")) cell.trim() else cell.substring(idx + 1).trim()
    }
}

/** Fetches wikitext through the MediaWiki API (cached). */
class WikipediaClient(private val http: CachedHttp) {
    fun wikitext(title: String): String? {
        val url = "https://en.wikipedia.org/w/api.php?action=parse&page=${title.replace(' ', '_').urlEncode()}" +
            "&prop=wikitext&format=json&formatversion=2&redirects=1"
        val body = http.get(url) ?: return null
        val obj: JsonObject = Json.parseToJsonElement(body).jsonObject
        val parse = obj["parse"]?.jsonObject ?: return null // page missing → API error object
        return parse["wikitext"]?.jsonPrimitive?.content
    }

    fun yearEndSongs(year: Int, limit: Int): List<RawEntry> {
        val text = wikitext("Billboard Year-End Hot 100 singles of $year") ?: return emptyList()
        return parseYearEndSongs(year, text).take(limit)
    }

    fun numberOneAlbums(year: Int): List<RawEntry> {
        val candidates = listOf(
            "List of Billboard 200 number-one albums of $year",
            "List of Billboard number-one albums of $year",
        )
        for (title in candidates) {
            val text = wikitext(title) ?: continue
            val entries = parseNumberOneAlbums(year, text)
            if (entries.isNotEmpty()) return entries
        }
        return emptyList()
    }

    companion object {
        fun parseYearEndSongs(year: Int, wikitext: String): List<RawEntry> {
            val seen = mutableSetOf<Int>()
            return WikiText.tableRows(wikitext).mapNotNull { cells ->
                if (cells.size < 3) return@mapNotNull null
                val rank = WikiText.clean(cells[0]).toIntOrNull() ?: return@mapNotNull null
                val title = WikiText.clean(cells[1])
                val artist = WikiText.clean(cells[2])
                if (title.isBlank() || artist.isBlank() || !seen.add(rank)) return@mapNotNull null
                RawEntry(year, "SONG", rank, title, artist)
            }.sortedBy { it.rank }
        }

        /** Albums are italicised links; the artist is the cell after the album cell. */
        fun parseNumberOneAlbums(year: Int, wikitext: String): List<RawEntry> {
            val seen = mutableSetOf<String>()
            val result = mutableListOf<RawEntry>()
            for (cells in WikiText.tableRows(wikitext)) {
                val albumIdx = cells.indexOfFirst { it.contains("''[[") || it.contains("''\"[[") }
                if (albumIdx < 0 || albumIdx + 1 >= cells.size) continue
                val title = WikiText.clean(cells[albumIdx])
                val artist = WikiText.clean(cells[albumIdx + 1])
                if (title.isBlank() || artist.isBlank() || !seen.add(title.lowercase())) continue
                result += RawEntry(year, "ALBUM", result.size + 1, title, artist)
            }
            return result
        }
    }
}
