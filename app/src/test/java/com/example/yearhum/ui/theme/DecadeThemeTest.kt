package com.example.yearhum.ui.theme

import com.example.yearhum.core.designsystem.theme.decadePaletteFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DecadeThemeTest {
    @Test
    fun picksPaletteByDecade() {
        assertEquals(1980, decadePaletteFor(1985)?.decade)
        assertEquals(1990, decadePaletteFor(1999)?.decade)
        assertEquals(2010, decadePaletteFor(2010)?.decade)
        assertNotNull(decadePaletteFor(1960))
    }

    @Test
    fun noPaletteOutsideSupportedDecades() {
        assertNull(decadePaletteFor(1959))
        assertNull(decadePaletteFor(2024))
    }
}
