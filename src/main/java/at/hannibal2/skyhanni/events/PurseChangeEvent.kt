package at.hannibal2.skyhanni.events

class PurseChangeEvent(val coins: Int, val purse: Long, val reason: PurseChangeCause) : LorenzEvent() {
    val oldCoins: Double get() = coins.toDouble()
}

enum class PurseChangeCause {
    GAIN_MOB_KILL,
    GAIN_TALISMAN_OF_COINS,
    GAIN_DICE_ROLL,
    GAIN_UNKNOWN,

    LOSE_SLAYER_QUEST_STARTED,
    LOSE_DICE_ROLL_COST,
    LOSE_UNKNOWN,
}
