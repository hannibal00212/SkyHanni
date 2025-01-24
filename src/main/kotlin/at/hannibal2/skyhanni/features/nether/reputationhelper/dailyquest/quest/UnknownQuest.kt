package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.NEUInternalName

class UnknownQuest(unknownName: String) :
    Quest(NEUInternalName.MISSING_ITEM, null, QuestCategory.UNKNOWN, unknownName, QuestState.ACCEPTED)
