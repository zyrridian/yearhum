package builder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuilderTest {
    private val songsWikitext =
        """
        {| class="wikitable sortable" style="text-align: center"
        |-
        ! scope="col" | №
        ! scope="col" | Title
        ! scope="col" | Artist(s)
        |-
        |1 || "[[Irreplaceable]]" || [[Beyoncé]]
        |-
        |2 || "[[Umbrella (song)|Umbrella]]" || [[Rihanna]] featuring [[Jay-Z]]
        |-
        ! scope="row" | 3
        | "[[Fergalicious]]"<ref>x</ref> || [[Fergie (singer)|Fergie]]
        |}
        """.trimIndent()

    @Test fun parsesSongRows() {
        val rows = WikipediaClient.parseYearEndSongs(2007, songsWikitext)
        assertEquals(listOf(1, 2), rows.map { it.rank }.take(2))
        assertEquals("Umbrella", rows[1].title)
        assertEquals("Rihanna featuring Jay-Z", rows[1].artist)
    }

    @Test fun parsesHeaderStyleRankCell() {
        val rows = WikipediaClient.parseYearEndSongs(2007, songsWikitext)
        assertEquals(3, rows.size)
        assertEquals("Fergalicious", rows[2].title)
        assertEquals("Fergie", rows[2].artist)
    }

    @Test fun parsesAlbums() {
        val text =
            """
            {| class="wikitable"
            |-
            | rowspan="2" | 1
            | January 6
            | ''[[Back to Black]]'' || [[Amy Winehouse]] || <ref>r</ref>
            |-
            | January 13
            |-
            | 2 || January 20 || ''[[Back to Black]]'' || [[Amy Winehouse]]
            |-
            | 3 || February 3 || ''[[Dedication 2]]'' || [[Lil Wayne]]
            |}
            """.trimIndent()
        val albums = WikipediaClient.parseNumberOneAlbums(2007, text)
        assertEquals(listOf("Back to Black", "Dedication 2"), albums.map { it.title })
        assertEquals(listOf(1, 2), albums.map { it.rank })
    }

    @Test fun leadArtistDropsFeatures() {
        assertEquals("Rihanna", Matching.leadArtist("Rihanna featuring Jay-Z"))
        assertEquals("Hall & Oates", Matching.leadArtist("Hall & Oates"))
    }

    @Test fun matchesIgnoringPunctuationAndAccents() {
        assertTrue(Matching.titlesMatch("Umbrella", "Umbrella (feat. Jay-Z)"))
        assertTrue(Matching.artistsMatch("Beyoncé", "Beyoncé"))
        assertTrue(Matching.artistsMatch("Rihanna featuring Jay-Z", "Rihanna feat. Jay-Z"))
    }

    @Test fun overridesParse() {
        val map =
            Overrides.parse(
                """
                # comment
                "2007|SONG|Umbrella|Rihanna": 0c1d2e3f-0000-4000-8000-000000000000
                "1999|SONG|X|Y": skip
                """.trimIndent(),
            )
        assertEquals(2, map.size)
        assertEquals("skip", map["1999|SONG|X|Y"])
    }

    @Test fun validatorFlagsProblems() {
        val good = "0c1d2e3f-0000-4000-8000-000000000000"
        val capsule =
            BuiltCapsule(
                2007,
                "h",
                listOf(
                    BuiltItem(2007, "SONG", 1, "a", "b", good),
                    BuiltItem(2007, "SONG", 1, "c", "d", "not-a-uuid"),
                ),
            )
        val problems = Validator.validate(listOf(capsule), minSongs = 3)
        assertEquals(3, problems.size)
    }
}
