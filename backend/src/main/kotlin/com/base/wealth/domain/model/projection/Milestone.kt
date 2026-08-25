package com.base.wealth.domain.model.projection

import com.base.wealth.domain.model.Money
import java.time.YearMonth

enum class MilestoneStatus { ACHIEVED, REACHABLE, OUT_OF_HORIZON }

/** [monthsRequired] is 0 when [MilestoneStatus.ACHIEVED], `null` when [MilestoneStatus.OUT_OF_HORIZON]. */
data class Milestone(
    val amount: Money,
    val status: MilestoneStatus,
    val monthsRequired: Int?,
    val targetMonth: YearMonth?,
)

data class ProjectionPoint(
    val year: Int,
    val futureValue: Money,
    val totalContributed: Money,
    val interestEarned: Money,
)
