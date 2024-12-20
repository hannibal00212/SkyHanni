package at.hannibal2.skyhanni.features.bingo

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getCachedIngredients
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.PrimitiveRecipe
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

object FirstMinionTier {

    fun firstMinionTier(
        otherItems: Map<NEUInternalName, Int>,
        minions: MutableMap<String, NEUInternalName>,
        tierOneMinions: MutableList<NEUInternalName>,
        tierOneMinionsDone: MutableSet<NEUInternalName>,
    ) {
        val help = helpMap(otherItems)
        val tierOneMinionsFiltered = getTierOneMinionsFiltered(tierOneMinions, tierOneMinionsDone)
        addMinion(tierOneMinionsFiltered, minions, tierOneMinionsDone)
        addMoreMinions(tierOneMinionsFiltered, help, minions)
    }

    private fun addMoreMinions(
        tierOneMinionsFiltered: List<NEUInternalName>,
        help: Map<NEUInternalName, Int>,
        minions: MutableMap<String, NEUInternalName>,
    ) {
        for (minionId in tierOneMinionsFiltered) {
            for (recipe in NEUItems.getRecipes(minionId)) {
                if (!recipe.isCraftingRecipe()) continue
                checkOne(recipe, help, minions, minionId)
            }
        }
    }

    private fun checkOne(
        recipe: PrimitiveRecipe,
        help: Map<NEUInternalName, Int>,
        minions: MutableMap<String, NEUInternalName>,
        minionId: NEUInternalName,
    ) {
        if (recipe.getCachedIngredients().any { help.contains(it.internalName) }) {
            val name = recipe.output?.internalName?.getItemStackOrNull()?.displayName?.removeColor() ?: return
            val abc = name.replace(" I", " 0")
            minions[abc] = minionId.replace("_1", "_0")
        }
    }

    private fun addMinion(
        tierOneMinionsFiltered: List<NEUInternalName>,
        minions: MutableMap<String, NEUInternalName>,
        tierOneMinionsDone: MutableSet<NEUInternalName>,
    ) {
        for (minionId in tierOneMinionsFiltered) {
            val prefix = minionId.asString().dropLast(1)
            if (minions.any { it.value.startsWith(prefix) }) {
                val successful = tierOneMinionsDone.add(minionId)
                if (!successful) {
                    ErrorManager.logErrorWithData(
                        IllegalStateException("Attempted to add $minionId to tierOneMinionsDone when it already exists"),
                        "Attempted to add $minionId to tierOneMinionsDone when it already exists",
                        "tierOneMinionsFiltered" to tierOneMinionsFiltered,
                        "minions" to minions,
                        "tierOneMinionsDone" to tierOneMinionsDone
                    )
                }
            }
        }
    }

    private fun getTierOneMinionsFiltered(
        tierOneMinions: MutableList<NEUInternalName>,
        tierOneMinionsDone: MutableSet<NEUInternalName>,
    ) = tierOneMinions.filter { it !in tierOneMinionsDone }

    private fun helpMap(otherItems: Map<NEUInternalName, Int>) =
        otherItems.filter { !it.key.startsWith("WOOD_") }
}
