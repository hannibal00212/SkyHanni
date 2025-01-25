package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NeuInternalName

abstract class ProgressQuest(
    displayItem: NeuInternalName,
    location: LorenzVec?,
    questCategory: QuestCategory,
    displayName: String,
    state: QuestState,
    val needAmount: Int,
    var haveAmount: Int = 0,
) : Quest(displayItem, location, questCategory, displayName, state)
