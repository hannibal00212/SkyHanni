package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.NeuInternalName

class UnknownQuest(unknownName: String) :
    Quest(NeuInternalName.MISSING_ITEM, null, QuestCategory.UNKNOWN, unknownName, QuestState.ACCEPTED)
