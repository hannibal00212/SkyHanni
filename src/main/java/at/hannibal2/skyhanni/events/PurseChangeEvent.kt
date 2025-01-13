package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.SkyHanniEvent

class PurseChangeEvent(val coins: Int, val purse: Long, val reason: PurseChangeCause) : SkyHanniEvent() {
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
