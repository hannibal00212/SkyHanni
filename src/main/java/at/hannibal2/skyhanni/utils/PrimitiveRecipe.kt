package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.enoughupdates.EnoughUpdatesManager
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.RecipeType.CRAFTING
import com.google.gson.JsonObject

data class PrimitiveRecipe(
    val ingredients: Set<PrimitiveIngredient>,
    val outputs: Set<PrimitiveIngredient>,
    val recipeType: RecipeType,
    val shouldUseForCraftCost: Boolean = true,
) {

    val output by lazy { outputs.firstOrNull() }

    companion object {
        fun loadRecipeFromJson(recipeJson: JsonObject, itemJson: JsonObject) {
            val type = recipeJson["type"]?.asString

            when (type) {
                "forge" -> {
                    val ingredients = mutableSetOf<PrimitiveIngredient>()
                    for (ingredient in recipeJson["inputs"].asJsonArray) {
                        ingredients.add(PrimitiveIngredient(ingredient.asString))
                    }

                    submitRecipe(ingredients, recipeJson, itemJson, RecipeType.FORGE)
                }

                "trade" -> {
                    val output = setOf(PrimitiveIngredient(recipeJson["result"].asString))
                    if (recipeJson.has("max")) {
                        val minAmount = recipeJson["min"].asInt
                        val maxAmount = recipeJson["max"].asInt
                        val average = (minAmount + maxAmount) / 2

                        val recipe = PrimitiveRecipe(
                            setOf(PrimitiveIngredient(recipeJson["cost"].asString.toInternalName(), average)),
                            output,
                            RecipeType.TRADE,
                            false,
                        )
                        EnoughUpdatesManager.registerRecipe(recipe)
                    } else {
                        val recipe =
                            PrimitiveRecipe(setOf(PrimitiveIngredient(recipeJson["cost"].asString)), output, RecipeType.TRADE, false)
                        EnoughUpdatesManager.registerRecipe(recipe)
                    }
                }

                "drops" -> {
                    val ingredient = setOf(PrimitiveIngredient(itemJson["internalname"].asString))
                    val outputs = mutableSetOf<PrimitiveIngredient>()

                    for (output in recipeJson["drops"].asJsonArray) {
                        outputs.add(PrimitiveIngredient(output.asJsonObject["id"].asString))
                    }
                    val recipe = PrimitiveRecipe(ingredient, outputs, RecipeType.MOB_DROP, false)
                    EnoughUpdatesManager.registerRecipe(recipe)
                }

                "npc_shop" -> {
                    val ingredients = mutableSetOf<PrimitiveIngredient>()
                    for (ingredient in recipeJson["cost"].asJsonArray) {
                        ingredients.add(PrimitiveIngredient(ingredient.asString))
                    }
                    val output = setOf(PrimitiveIngredient(recipeJson["result"].asString))
                    val recipe = PrimitiveRecipe(ingredients, output, RecipeType.NPC_SHOP)
                    EnoughUpdatesManager.registerRecipe(recipe)
                }

                "katgrade" -> {
                    val ingredients = mutableSetOf<PrimitiveIngredient>()
                    for (ingredient in recipeJson["items"].asJsonArray) {
                        ingredients.add(PrimitiveIngredient(ingredient.asString))
                    }
                    ingredients.add(PrimitiveIngredient(recipeJson["input"].asString))
                    ingredients.add(PrimitiveIngredient.coinIngredient(recipeJson["coins"].asDouble))

                    val output = setOf(PrimitiveIngredient(recipeJson["output"].asString))
                    val recipe = PrimitiveRecipe(ingredients, output, RecipeType.KAT_UPGRADE, false)
                    EnoughUpdatesManager.registerRecipe(recipe)
                }

                else -> {
                    val ingredients = mutableSetOf<PrimitiveIngredient>()

                    val x = arrayOf("1", "2", "3")
                    val y = arrayOf("A", "B", "C")
                    for (i in 0..8) {
                        val name = y[i / 3] + x[i % 3]
                        recipeJson[name]?.asString?.let {
                            if (it.isNotEmpty()) ingredients.add(PrimitiveIngredient(it))
                        }
                    }

                    submitRecipe(ingredients, recipeJson, itemJson, CRAFTING)
                }
            }
        }

        private fun submitRecipe(
            ingredients: Set<PrimitiveIngredient>,
            recipeJson: JsonObject,
            itemJson: JsonObject,
            recipeType: RecipeType,
        ) {
            val craftAmount = if (recipeJson.has("count")) recipeJson.get("count").asInt else 1
            val outputInternalName = if (recipeJson.has("overrideOutputId")) {
                recipeJson.get("overrideOutputId").asString
            } else {
                itemJson.get("internalname").asString
            }
            val outputItem = PrimitiveIngredient(outputInternalName.toInternalName(), craftAmount)

            val recipe = PrimitiveRecipe(ingredients, setOf(outputItem), recipeType)
            EnoughUpdatesManager.registerRecipe(recipe)
        }
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
}
