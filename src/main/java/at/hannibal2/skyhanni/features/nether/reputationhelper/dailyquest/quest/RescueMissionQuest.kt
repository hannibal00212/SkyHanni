package at.hannibal2.skyhanni.features.nether.reputationhelper.dailyquest.quest

import at.hannibal2.skyhanni.utils.SkyHanniVec3d
import at.hannibal2.skyhanni.utils.NeuInternalName

class RescueMissionQuest(displayItem: NeuInternalName, location: SkyHanniVec3d?, state: QuestState) :
    Quest(displayItem, location, QuestCategory.RESCUE, "Rescue Mission", state, "Rescue the NPC")
