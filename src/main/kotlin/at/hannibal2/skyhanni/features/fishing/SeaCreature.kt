package at.hannibal2.skyhanni.features.fishing

import at.hannibal2.skyhanni.utils.LorenzRarity

data class SeaCreature(
    val name: String,
    val fishingExperience: Int,
    val chatColor: String,
    val rare: Boolean,
    val rarity: LorenzRarity,
) {

    val displayName = chatColor + rare() + name

    private fun rare() = if (rare) "§l" else ""
}

