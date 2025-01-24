package at.hannibal2.skyhanni.events.bingo

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.bingo.card.goals.BingoGoal

class BingoGoalReachedEvent(val goal: BingoGoal) : SkyHanniEvent()
