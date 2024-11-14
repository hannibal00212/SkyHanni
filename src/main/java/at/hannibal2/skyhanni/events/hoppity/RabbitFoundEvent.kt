package at.hannibal2.skyhanni.events.hoppity

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggType

class RabbitFoundEvent(
    val eggType: HoppityEggType,
    val duplicate: Boolean,
    val rabbitName: String,
    val chocGained: Long = 0,
) : SkyHanniEvent() {

    constructor(dataSet: HoppityAPI.HoppityStateDataSet) : this(
        dataSet.lastMeal ?: HoppityEggType.BREAKFAST,
        dataSet.duplicate,
        dataSet.lastName,
        dataSet.lastDuplicateAmount ?: 0,
    )

    override fun toString(): String =
        "§fType§7: ${eggType.coloredName}\n§fDuplicate§7: §b$duplicate\n§fRabbit§7: $rabbitName\n§fChoc Gained§7: §6$chocGained"
}
