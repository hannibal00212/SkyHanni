package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.RecipeType.CRAFTING
import io.github.moulberry.notenoughupdates.recipes.EssenceUpgrades
import io.github.moulberry.notenoughupdates.recipes.ForgeRecipe
import io.github.moulberry.notenoughupdates.recipes.ItemShopRecipe
import io.github.moulberry.notenoughupdates.recipes.KatRecipe
import io.github.moulberry.notenoughupdates.recipes.MobLootRecipe
import io.github.moulberry.notenoughupdates.recipes.NeuRecipe
import io.github.moulberry.notenoughupdates.recipes.VillagerTradeRecipe

data class PrimitiveRecipe(
    val ingredients: Set<PrimitiveIngredient>,
    val outputs: Set<PrimitiveIngredient>,
    val recipeType: RecipeType,
) {

    val output by lazy { outputs.firstOrNull() }

    companion object {
        fun fromNeuRecipe(neuRecipe: NeuRecipe): PrimitiveRecipe {
            val ingredients = neuRecipe.ingredients.map { PrimitiveIngredient.fromNeuIngredient(it) }.toSet()
            val outputs = neuRecipe.outputs.map { PrimitiveIngredient.fromNeuIngredient(it) }.toSet()

            val recipeType = when (neuRecipe::class.java) {
                ForgeRecipe::class.java -> RecipeType.FORGE
                VillagerTradeRecipe::class.java -> RecipeType.TRADE
                EssenceUpgrades::class.java -> RecipeType.ESSENCE
                MobLootRecipe::class.java -> RecipeType.MOB_DROP
                ItemShopRecipe::class.java -> RecipeType.NPC_SHOP
                KatRecipe::class.java -> RecipeType.KAT_UPGRADE
                else -> CRAFTING
            }

            return PrimitiveRecipe(ingredients, outputs, recipeType)
        }

        fun convertMultiple(neuRecipes: Collection<NeuRecipe>) = neuRecipes.map { fromNeuRecipe(it) }
    }

    fun isCraftingRecipe() = this.recipeType == CRAFTING
}

enum class RecipeType {
    FORGE,
    TRADE,
    MOB_DROP,
    NPC_SHOP,
    KAT_UPGRADE,
    ESSENCE,
    CRAFTING,
    ;
}
