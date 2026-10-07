package com.yearhum.app.domain.usecase

import com.yearhum.app.domain.model.LifeMilestone
import javax.inject.Inject

/** Turns a birth year into the list of capsule years to show on the "Your life in music" screen. */
class BuildLifeTimelineUseCase
@Inject
constructor() {
    operator fun invoke(
        birthYear: Int,
        availableYears: List<Int>,
        currentYear: Int,
    ): List<LifeMilestone> {
        val available = availableYears.toSet()
        return MILESTONE_AGES
            .map { age -> LifeMilestone(age, birthYear + age) }
            .filter { it.year <= currentYear && it.year in available }
    }

    companion object {
        val MILESTONE_AGES =
            listOf(0, 5, 10, 15, 18, 21, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90)
    }
}
