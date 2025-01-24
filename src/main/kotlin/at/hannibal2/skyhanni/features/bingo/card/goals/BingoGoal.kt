package at.hannibal2.skyhanni.features.bingo.card.goals

import com.google.gson.annotations.Expose

class BingoGoal {

    @Expose
    lateinit var type: GoalType

    @Expose
    var displayName = ""

    @Expose
    var description = ""

    @Expose
    var guide = emptyList<String>()

    @Expose
    var done = false

    @Expose
    var highlight = false

    @Expose
    lateinit var hiddenGoalData: HiddenGoalData

    // TODO rename communityGoalPercentage
    @Expose
    var communtyGoalPercentage: Double? = null

    override fun toString(): String = displayName
}
