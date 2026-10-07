package builder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratedTest {
    @Test
    fun bundledDataIsValid() {
        val byYear = Curated.load()
        assertTrue(byYear.isNotEmpty())
        byYear.forEach { (year, items) ->
            items.groupBy { it.category }.forEach { (category, group) ->
                assertEquals("duplicate ranks in $year/$category", group.size, group.map { it.rank }.toSet().size)
            }
            assertTrue("$year has no movies", items.any { it.category == "MOVIE" })
        }
    }

    @Test
    fun parseSkipsCommentsAndReadsFields() {
        val items = Curated.parse("# comment\n\n2007|MOVIE|1|Some Film|Some Director\n2007|EVENT|1|Something happened|")
        assertEquals(2, items.size)
        assertEquals("Some Director", items[0].subtitle)
        assertEquals(null, items[1].subtitle)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parseRejectsUnknownCategory() {
        Curated.parse("2007|SONG|1|X|Y")
    }
}
