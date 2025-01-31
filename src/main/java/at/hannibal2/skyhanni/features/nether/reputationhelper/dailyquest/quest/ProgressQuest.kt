package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import at.hannibal2.skyhanni.utils.NeuInternalName

abstract class ProgressQuest(
    displayItem: NeuInternalName,
    location: SkyHanniVec3d?,
    questCategory: QuestCategory,
    displayName: String,
    state: QuestState,
    val needAmount: Int,
    var haveAmount: Int = 0,
) : Quest(displayItem, location, questCategory, displayName, state)
