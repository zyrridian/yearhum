package com.example.yearhum.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildLifeTimelineUseCaseTest {
    private val useCase = BuildLifeTimelineUseCase()
    private val years = (1990..2025).toList()

    @Test
    fun mapsAgesToYearsWithinDataset() {
        val result = useCase(birthYear = 1995, availableYears = years, currentYear = 2026)
        assertEquals(listOf(0, 5, 10, 15, 18, 21, 25, 30), result.map { it.age })
        assertEquals(1995, result.first().year)
        assertEquals(2025, result.last().year)
    }

    @Test
    fun skipsFutureYears() {
        val result = useCase(birthYear = 2010, availableYears = years, currentYear = 2018)
        assertEquals(listOf(0, 5), result.map { it.age })
    }

    @Test
    fun skipsYearsMissingFromDataset() {
        val result = useCase(birthYear = 1975, availableYears = years, currentYear = 2026)
        // Ages 0, 5, 10 land before 1990 and are dropped.
        assertEquals(15, result.first().age)
        assertTrue(result.all { it.year in years })
    }

    @Test
    fun emptyWhenNothingMatches() {
        assertTrue(useCase(1900, years, 2026).isEmpty())
    }
}
