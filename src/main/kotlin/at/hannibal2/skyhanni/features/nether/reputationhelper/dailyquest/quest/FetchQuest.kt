package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName

class FetchQuest(
    val itemName: String,
    location: LorenzVec?,
    displayItem: NEUInternalName,
    state: QuestState,
    needAmount: Int,
) :
    ProgressQuest(displayItem, location, QuestCategory.FETCH, itemName, state, needAmount)
