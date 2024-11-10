package at.hannibal2.skyhanni.api.enoughupdates

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuPetsJson
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.extraAttributes
import at.hannibal2.skyhanni.utils.PrimitiveRecipe
import at.hannibal2.skyhanni.utils.StringUtils.cleanString
import at.hannibal2.skyhanni.utils.StringUtils.removeUnusedDecimal
import at.hannibal2.skyhanni.utils.system.PlatformUtils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT
import net.minecraft.nbt.NBTException
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.TreeMap
import kotlin.math.floor

@SkyHanniModule
object EnoughUpdatesManager {

    val configLocation = File("config/notenoughupdates")
    val repoLocation = File(configLocation, "repo")

    val itemMap = TreeMap<String, JsonObject>()
    private val itemStackCache = mutableMapOf<String, ItemStack>()
    private val displayNameCache = mutableMapOf<String, String>()
    private val recipes = mutableSetOf<PrimitiveRecipe>()

    private var neuPetsJson: NeuPetsJson? = null
    private var neuPetNums: JsonObject? = null

    val titleWordMap = TreeMap<String, MutableMap<String, MutableList<Int>>>()

    fun downloadRepo() {
        EnoughUpdatesRepo.downloadRepo()
    }

    fun reloadRepo() {
        itemStackCache.clear()
        displayNameCache.clear()
        itemMap.clear()
        titleWordMap.clear()

        val tempItemMap = TreeMap<String, JsonObject>()
        SkyHanniMod.coroutineScope.launch {
            loadItemMap(tempItemMap)
            synchronized(itemMap) {
                itemMap.clear()
                itemMap.putAll(tempItemMap)
            }
            NeuRepositoryReloadEvent().postAndCatch()
        }
    }

    private fun loadItemMap(tempItemMap: TreeMap<String, JsonObject>) {
        val itemDir = File(repoLocation, "items")
        if (!itemDir.exists()) return
        for (file in itemDir.listFiles() ?: return) {
            if (file.extension != "json") continue
            try {
                InputStreamReader(FileInputStream(file)).use { reader ->
                    val json = ConfigManager.gson.fromJson(reader, JsonObject::class.java)
                    tempItemMap[file.nameWithoutExtension] = parseItem(file.nameWithoutExtension, json) ?: continue
                }
            } catch (e: Exception) {
                ErrorManager.logErrorWithData(e, "Error while loading neu repo")
            }
        }
        return
    }

    private fun parseItem(internalName: String, json: JsonObject): JsonObject? {
        if (json.get("itemid") == null) return null
        var itemId = json["itemid"].asString
        val mcItem = Item.getByNameOrId(itemId)
        if (mcItem != null) {
            itemId = mcItem.registryName
        }
        json.addProperty("itemid", itemId)

        json["recipe"]?.asJsonObject?.let { recipe ->
            PrimitiveRecipe.loadRecipeFromJson(recipe.asJsonObject, json)
        }
        json["recipes"]?.asJsonArray?.forEach { recipe ->
            PrimitiveRecipe.loadRecipeFromJson(recipe.asJsonObject, json)
        }

        if (json.has("displayname")) {
            synchronized(titleWordMap) {

                for ((index, str) in json["displayname"].asString.split(" ").withIndex()) {
                    val cleanedStr = str.cleanString()
                    val internalMap = titleWordMap.getOrPut(cleanedStr) { TreeMap() }
                    val indexList = internalMap.getOrPut(internalName) { mutableListOf() }
                    indexList.add(index)
                }
            }
        }
        return json
    }

    fun registerRecipe(recipe: PrimitiveRecipe) {
        recipes.add(recipe)
    }

    fun getItemById(id: String): JsonObject? {
        return itemMap[id]
    }

    fun jsonToStack(json: JsonObject?, useCache: Boolean = true, useReplacements: Boolean = false): ItemStack {
        json ?: return ItemStack(Items.painting)
        var usingCache = useCache && !useReplacements
        val internalName = json["internalname"].asString
        if (internalName == "_") usingCache = false

        if (usingCache) {
            val cachedStack = itemStackCache[internalName]
            if (cachedStack != null) return cachedStack.copy()
        }

        val stack = ItemStack(Item.getByNameOrId(json["itemid"].asString))
        stack.item ?: return ItemStack(Item.getItemFromBlock(Blocks.stone), 0, 255)

        json["count"]?.asInt?.let { stack.stackSize = it }
        json["damage"]?.asInt?.let { stack.itemDamage = it }
        json["nbttag"]?.asString?.let { nbt ->
            try {
                val tag = JsonToNBT.getTagFromJson(nbt)
                stack.tagCompound = tag
            } catch (_: NBTException) {
            }
        }

        var replacements = mapOf<String, String>()
        if (useReplacements) {
            replacements = getPetLoreReplacements(stack.tagCompound, -1)
            json["displayname"]?.asString?.let {
                var name = it
                for ((key, value) in replacements) {
                    name = name.replace("{$key}", value)
                }
                stack.setStackDisplayName(name)
            }
        }

        json["lore"]?.asJsonArray?.let { lore ->
            val displayTag = stack.tagCompound?.getCompoundTag("display") ?: NBTTagCompound()
            displayTag.setTag("Lore", processLore(lore, replacements))
            val tag = stack.tagCompound ?: NBTTagCompound()
            tag.setTag("display", displayTag)
            stack.tagCompound = tag
        }


        if (usingCache) itemStackCache[internalName] = stack
        return stack.copy()
    }

    private fun getPetLoreReplacements(tag: NBTTagCompound?, level: Int): Map<String, String> {
        tag ?: return emptyMap()
        var petName: String? = null
        var tier: String? = null

        val extraAttributes = tag.extraAttributes
        if (extraAttributes.hasKey("petInfo")) {
            val petInfoStr = extraAttributes.getString("petInfo")
            val petInfo = ConfigManager.gson.fromJson(petInfoStr, JsonObject::class.java)
            petName = petInfo["name"]?.asString
            tier = petInfo["tier"]?.asString
            petInfo["heldItem"]?.asString?.let { it == "PET_ITEM_TIER_BOOST" }?.let {
                tier = when (tier) {
                    "COMMON" -> "UNCOMMON"
                    "UNCOMMON" -> "RARE"
                    "RARE" -> "EPIC"
                    "EPIC" -> "LEGENDARY"
                    else -> "MYTHIC"
                }
            }
        }

        return getPetLoreReplacements(petName, tier, level)
    }

    private fun getPetLoreReplacements(petName: String?, tier: String?, level: Int): Map<String, String> {

        val replacements = mutableMapOf<String, String>()

        if (level != -1) {
            replacements["LVL"] = level.toString()
        } else {
            neuPetsJson?.customPetLeveling?.get(petName)?.let { petLeveling ->
                val maxLevel = petLeveling.asJsonObject.get("maxLevel")?.asInt ?: 100
                replacements["LVL"] = "1➡$maxLevel"
            } ?: run { replacements["LVL"] = "1➡100" }
        }

        if (petName == null || tier == null) return replacements

        val petNums = neuPetNums ?: return replacements
        petNums[petName]?.asJsonObject?.let { petInfo ->
            petInfo[tier]?.asJsonObject?.let { petInfoTier ->
                val min = petInfoTier["1"]?.asJsonObject ?: return replacements
                val max = petInfoTier["100"]?.asJsonObject ?: return replacements

                if (level < 1) {
                    val otherNumsMin = min["otherNums"]?.asJsonArray ?: return replacements
                    val otherNumsMax = max["otherNums"]?.asJsonArray ?: return replacements
                    var addZero = false
                    petInfoTier["stats_levelling_curve"]?.asString?.split(":")?.let { split ->
                        if (split.size == 3 && split[2].toInt() == 1) {
                            addZero = true
                        }
                    }
                    for (i in 0..otherNumsMax.size()) {
                        val start = if (addZero) "0➡" else ""
                        replacements[i.toString()] = "$start${otherNumsMin[i].asDouble}➡${otherNumsMax[i].asDouble}"
                    }
                    for (entry in max["statNums"].asJsonObject.entrySet()) {
                        val statMax = entry.value.asDouble
                        val statMin = min["statNums"].asJsonObject[entry.key].asDouble
                        val start = "${if (addZero) "0➡" else ""}${if (statMin > 0) "+" else ""}"
                        replacements[entry.key] = "$start$statMin➡$statMax"
                    }
                } else {
                    var minStatsLevel = 0
                    var maxStatsLevel = 100
                    var statLevelingType = -1

                    var statsLevel = level

                    petInfoTier["stats_levelling_curve"]?.asString?.split(":")?.let { split ->
                        if (split.size == 3) {
                            minStatsLevel = split[0].toInt()
                            maxStatsLevel = split[1].toInt()
                            statLevelingType = split[2].toInt()

                            if (statLevelingType in 0..1) {
                                statsLevel = if (level < minStatsLevel) {
                                    1
                                } else if (level < maxStatsLevel) {
                                    level - minStatsLevel + 1
                                } else {
                                    maxStatsLevel - minStatsLevel + 1
                                }
                            }
                        }
                    }

                    val minMix = (maxStatsLevel - (minStatsLevel - (if (statLevelingType == -1) 0 else 1)) - statsLevel) / 99f
                    val maxMix = (statsLevel - 1) / 99f

                    val otherNumsMin = min["otherNums"].asJsonArray
                    val otherNumsMax = max["otherNums"].asJsonArray
                    for (i in 0 until otherNumsMax.size()) {
                        val num = otherNumsMin[i].asFloat * minMix + otherNumsMax[i].asFloat * maxMix
                        if (statLevelingType == 1 && level < minStatsLevel) {
                            replacements[i.toString()] = "0"
                        } else {
                            replacements[i.toString()] = (floor((num * 10).toDouble()) / 10f).removeUnusedDecimal()
                        }
                    }

                    for ((key, value) in max["statNums"].asJsonObject.entrySet()) {
                        if (statLevelingType == 1 && level < minStatsLevel) {
                            replacements[key] = "0"
                        } else {
                            val statMax = value.asFloat
                            val statMin = min["statNums"].asJsonObject[key].asFloat
                            val num = statMin * minMix + statMax * maxMix
                            val statStr = (if (statMin > 0) "+" else "") + (floor((num * 10).toDouble()) / 10).removeUnusedDecimal()
                            replacements[key] = statStr
                        }
                    }
                }
            }
        }

        return replacements
    }

    private fun processLore(lore: JsonArray, replacements: Map<String, String>): NBTTagList {
        val loreList = NBTTagList()
        for (line in lore) {
            val loreLine = line.asString
            for ((key, value) in replacements) {
                loreLine.replace("{$key}", value)
            }
            loreList.appendTag(NBTTagString(loreLine))
        }
        return loreList
    }

    fun getDisplayName(internalName: String): String {
        return displayNameCache.getOrPut(internalName) {
            val itemInfo = getItemById(internalName) ?: return@getOrPut internalName
            itemInfo["displayname"]?.asString ?: run {
                ErrorManager.skyHanniError("No displayname for $internalName")
            }
        }
    }

    @SubscribeEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        neuPetsJson = event.readConstant<NeuPetsJson>("pets")
        neuPetNums = event.readConstant<JsonObject>("petnums")
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        if (!PlatformUtils.isNeuLoaded()) {
            event.register("neureloadrepo") {
                description = "Reloads the NEU repo"
                category = CommandCategory.DEVELOPER_TEST
                callback {
                    reloadRepo()
                    ChatUtils.chat("Reloaded NEU repo")
                }
            }
            event.register("neuresetrepo") {
                description = "Redownload the NEU repo"
                category = CommandCategory.DEVELOPER_TEST
                callback {
                    downloadRepo()
                    reloadRepo()
                    ChatUtils.chat("Updated NEU repo")
                }
            }
        }
    }
}
