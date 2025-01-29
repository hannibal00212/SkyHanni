package at.hannibal2.skyhanni.features.bingo

import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.NeuInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NeuItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.PrimitiveRecipe
import at.hannibal2.skyhanni.utils.StringUtils.removeColor

object FirstMinionTier {

    fun firstMinionTier(
        otherItems: Map<NeuInternalName, Int>,
        minions: MutableMap<String, NeuInternalName>,
        tierOneMinions: MutableList<NeuInternalName>,
        tierOneMinionsDone: MutableSet<NeuInternalName>,
    ) {
        val help = helpMap(otherItems)
        val tierOneMinionsFiltered = getTierOneMinionsFiltered(tierOneMinions, tierOneMinionsDone)
        addMinion(tierOneMinionsFiltered, minions, tierOneMinionsDone)
        addMoreMinions(tierOneMinionsFiltered, help, minions)
    }

    private fun addMoreMinions(
        tierOneMinionsFiltered: List<NeuInternalName>,
        help: Map<NeuInternalName, Int>,
        minions: MutableMap<String, NeuInternalName>,
    ) {
        for (minionId in tierOneMinionsFiltered) {
            for (recipe in NeuItems.getRecipes(minionId)) {
                if (!recipe.isCraftingRecipe()) continue
                checkOne(recipe, help, minions, minionId)
            }
        }
    }

    private fun checkOne(
        recipe: PrimitiveRecipe,
        help: Map<NeuInternalName, Int>,
        minions: MutableMap<String, NeuInternalName>,
        minionId: NeuInternalName,
    ) {
        if (recipe.ingredients.any { help.contains(it.internalName) }) {
            val name = recipe.output?.internalName?.getItemStackOrNull()?.displayName?.removeColor() ?: return
            val abc = name.replace(" I", " 0")
            minions[abc] = minionId.replace("_1", "_0")
        }
    }

    private fun addMinion(
        tierOneMinionsFiltered: List<NeuInternalName>,
        minions: MutableMap<String, NeuInternalName>,
        tierOneMinionsDone: MutableSet<NeuInternalName>,
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
        tierOneMinions: MutableList<NeuInternalName>,
        tierOneMinionsDone: MutableSet<NeuInternalName>,
    ) = tierOneMinions.filter { it !in tierOneMinionsDone }

    private fun helpMap(otherItems: Map<NeuInternalName, Int>) =
        otherItems.filter { !it.key.startsWith("WOOD_") }
}
