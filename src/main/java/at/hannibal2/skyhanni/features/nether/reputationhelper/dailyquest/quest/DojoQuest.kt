package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import at.hannibal2.skyhanni.utils.NeuInternalName

class DojoQuest(
    val dojoName: String,
    location: SkyHanniVec3d?,
    displayItem: NeuInternalName,
    dojoRankGoal: String,
    state: QuestState,
) :
    Quest(
        displayItem,
        location,
        QuestCategory.DOJO,
        "$dojoName Rank $dojoRankGoal",
        state,
        "$dojoName §7(§e" + LorenzUtils.getPointsForDojoRank(dojoRankGoal) + " points§7)"
    )
