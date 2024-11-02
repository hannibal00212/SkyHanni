package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.features.nether.reputationhelper.miniboss.CrimsonMiniBoss

class MiniBossQuest(val miniBoss: CrimsonMiniBoss, state: QuestState, needAmount: Int) :
    ProgressQuest(
        miniBoss.displayItem,
        miniBoss.location,
        QuestCategory.MINIBOSS,
        miniBoss.displayName,
        state,
        needAmount
    )
