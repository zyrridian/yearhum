package com.example.yearhum.core.designsystem.component

import androidx.annotation.StringRes
import com.example.yearhum.R
import com.example.yearhum.domain.model.Category

@StringRes
fun Category.labelRes(): Int = when (this) {
    Category.SONG -> R.string.category_song
    Category.ALBUM -> R.string.category_album
    Category.ARTIST -> R.string.category_artist
    Category.GAME -> R.string.category_game
    Category.MOVIE -> R.string.category_movie
    Category.TV -> R.string.category_tv
    Category.EVENT -> R.string.category_event
    Category.TECH -> R.string.category_tech
}

/** Songs and albums come from real year-end charts; the rest are editorial picks. */
val Category.isChartRanked: Boolean get() = this == Category.SONG || this == Category.ALBUM
