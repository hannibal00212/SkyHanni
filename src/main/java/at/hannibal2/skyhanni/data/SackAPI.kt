package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.data.jsonobjects.repo.neu.NeuSacksJson
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.SackChangeEvent
import at.hannibal2.skyhanni.events.SackDataUpdateEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import at.hannibal2.skyhanni.features.inventory.SackDisplay
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPrice
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.itemNameWithoutColor
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchAll
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.StringUtils.removeNonAscii
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object SackAPI {

    private val sackDisplayConfig get() = SkyHanniMod.feature.inventory.sackDisplay
    private val chatConfig get() = SkyHanniMod.feature.chat
    private var lastOpenedInventory = ""

    var inSackInventory = false

    private val patternGroup = RepoPattern.group("data.sacks")

    /**
     * REGEX-TEST: Fishing Sack
     * REGEX-TEST: Enchanted Agronomy Sack
     */
    private val sackPattern by patternGroup.pattern(
        "sack",
        "^(?:.* Sack|Enchanted .* Sack)\$",
    )

    /**
     * REGEX-TEST: §7Stored: §e28,183§7/60.5k
     * REGEX-TEST: §7Stored: §80§7/60.5k
     */
    @Suppress("MaxLineLength")
    private val numPattern by patternGroup.pattern(
        "number",
        "(?:(?:§[0-9a-f](?<level>I{1,3})§7:)?|(?:§7Stored:)?) (?<color>§[0-9a-f])(?<stored>[0-9.,kKmMbB]+)§7/(?<total>\\d+(?:[0-9.,]+)?[kKmMbB]?)",
    )

    /**
     * REGEX-TEST:  §fRough: §e78,999 §8(78,999)
     * REGEX-TEST:  §aFlawed: §e604 §8(48,320)
     * REGEX-TEST:  §9Fine: §e35 §8(224,000)
     */
    @Suppress("MaxLineLength")
    private val gemstonePattern by patternGroup.pattern(
        "gemstone",
        " §[0-9a-f](?<gemrarity>[A-z]*): §[0-9a-f](?<stored>\\d+(?:\\.\\d+)?(?:(?:,\\d+)?)+[kKmM]?)(?: §[0-9a-f]\\(\\d+(?:\\.\\d+)?(?:(?:,\\d+)?)+[kKmM]?\\))?",
    )

    private var isRuneSack = false
    private var isGemstoneSack = false
    var isTrophySack = false
    private var sackRarity: TrophyRarity? = null

    /**
     * TODO merge all 3 lists into one:
     *
     * move item name (currently key) into AbstractSackItem
     * work with instance check
     * add custom function for render behaviour.
     * have only one render display function
     */
    //
    val sackItem = mutableMapOf<String, SackOtherItem>()
    val runeItem = mutableMapOf<String, SackRune>()
    val gemstoneItem = mutableMapOf<String, SackGemstone>()
    private val stackList = mutableMapOf<Int, ItemStack>()

    var sackListInternalNames = emptySet<String>()
        private set

    var sackListNames = emptySet<String>()
        private set

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inSackInventory = false
        isRuneSack = false
        isGemstoneSack = false
        isTrophySack = false
        runeItem.clear()
        gemstoneItem.clear()
        sackItem.clear()
        stackList.clear()
    }

    @HandleEvent
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        val inventoryName = event.inventoryName
        val isNewInventory = inventoryName != lastOpenedInventory
        lastOpenedInventory = inventoryName
        val match = sackPattern.matches(inventoryName)
        if (!match) return
        val stacks = event.inventoryItems
        isRuneSack = inventoryName == "Runes Sack"
        isGemstoneSack = inventoryName == "Gemstones Sack"
        isTrophySack = inventoryName.contains("Trophy Fishing Sack")
        sackRarity = inventoryName.getTrophyRarity()
        inSackInventory = true
        stackList.putAll(stacks)
        SackDisplay.update(isNewInventory)
    }

    private fun String.getTrophyRarity(): TrophyRarity? = when {
        this.startsWith("Bronze") -> TrophyRarity.BRONZE
        this.startsWith("Silver") -> TrophyRarity.SILVER
        else -> null
    }

    private fun NEUInternalName.sackPrice(stored: Int): Long {
        return getPrice(sackDisplayConfig.priceSource).toLong() * stored
    }

    fun getSacksData(savingSacks: Boolean) {
        if (savingSacks) sackData = ProfileStorageData.sackProfiles?.sackContents ?: return
        for ((slot, stack) in stackList) {
            val name = stack.name
            val lore = stack.getLore()

            if (isGemstoneSack) {
                val gem = SackGemstone()
                gemstonePattern.matchAll(lore) {
                    val rarity = group("gemrarity")
                    val stored = group("stored").formatInt()
                    gem.internalName = gemstoneMap[name.removeColor()] ?: NEUInternalName.NONE
                    if (gemstoneMap.containsKey(name.removeColor())) {
                        val internalName = "${rarity.uppercase()}_${
                            name.uppercase().split(" ")[0].removeColor()
                        }_GEM".toInternalName()

                        gem.slot = slot

                        when (rarity) {
                            "Rough" -> {
                                gem.rough = stored
                                gem.stored += (stored * 1)
                                gem.roughPrice = internalName.sackPrice(stored)
                                gem.price += gem.roughPrice
                                if (savingSacks) setSackItem(internalName, stored)
                            }

                            "Flawed" -> {
                                gem.flawed = stored
                                gem.stored += (stored * 80)
                                gem.flawedPrice = internalName.sackPrice(stored)
                                gem.price += gem.flawedPrice
                                if (savingSacks) setSackItem(internalName, stored)
                            }

                            "Fine" -> {
                                gem.fine = stored
                                gem.stored += (stored * 80 * 80)
                                gem.finePrice = internalName.sackPrice(stored)
                                gem.price += gem.finePrice
                                if (savingSacks) setSackItem(internalName, stored)
                                gemstoneItem[name] = gem
                            }
                        }
                    }
                }
            } else if (isRuneSack) {
                val rune = SackRune()
                for (line in lore) {
                    numPattern.matchMatcher(line) {
                        val level = group("level").romanToDecimal()
                        val stored = group("stored").formatInt()
                        rune.stack = stack
                        rune.stored += stored

                        when (level) {
                            1 -> rune.lvl1 = stored
                            2 -> rune.lvl2 = stored
                            3 -> {
                                rune.slot = slot
                                rune.lvl3 = stored
                                runeItem[name] = rune
                            }
                        }
                    }
                }
            } else {
                // normal sack
                numPattern.firstMatcher(lore) {
                    val item = SackOtherItem()
                    val stored = group("stored").formatInt()
                    val internalName = stack.getInternalName()
                    item.internalName = internalName
                    item.colorCode = group("color")
                    item.stored = group("stored").formatInt()
                    item.total = group("total").formatInt()

                    if (savingSacks) setSackItem(item.internalName, item.stored)
                    item.price = if (isTrophySack) {
                        val filletPerTrophy = FishingAPI.getFilletPerTrophy(stack.getInternalName())
                        val filletValue = filletPerTrophy * stored
                        item.magmaFish = filletValue
                        "MAGMA_FISH".toInternalName().sackPrice(filletValue)
                    } else {
                        internalName.sackPrice(stored).coerceAtLeast(0)
                    }
                    item.slot = slot
                    sackItem[name] = item
                }
            }
        }
        if (savingSacks) saveSackData()
    }

    var sackData = mapOf<NEUInternalName, SackItem>()
        private set

    data class SackChange(val delta: Int, val internalName: NEUInternalName, val sacks: List<String>)

    private val sackChangeRegex = Regex("""([+-][\d,]+) (.+) \((.+)\)""")

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!event.message.removeColor().startsWith("[Sacks]")) return

        val sackAddText = event.chatComponent.siblings.firstNotNullOfOrNull { sibling ->
            sibling.chatStyle?.chatHoverEvent?.value?.formattedText?.removeColor()?.takeIf {
                it.startsWith("Added")
            }
        }.orEmpty()
        val sackRemoveText = event.chatComponent.siblings.firstNotNullOfOrNull { sibling ->
            sibling.chatStyle?.chatHoverEvent?.value?.formattedText?.removeColor()?.takeIf {
                it.startsWith("Removed")
            }
        }.orEmpty()

        val sackChangeText = sackAddText + sackRemoveText
        if (sackChangeText.isEmpty()) return

        val otherItemsAdded = sackAddText.contains("other items")
        val otherItemsRemoved = sackRemoveText.contains("other items")

        val sackChanges = ArrayList<SackChange>()
        for (match in sackChangeRegex.findAll(sackChangeText)) {
            val delta = match.groups[1]!!.value.formatInt()
            val item = match.groups[2]!!.value
            val sacks = match.groups[3]!!.value.split(", ")

            val internalName = NEUInternalName.fromItemName(item)
            sackChanges.add(SackChange(delta, internalName, sacks))
        }
        val sackEvent = SackChangeEvent(sackChanges, otherItemsAdded, otherItemsRemoved)
        updateSacks(sackEvent)
        sackEvent.post()
        if (chatConfig.hideSacksChange) {
            event.blockedReason = "sacks_change"
        }
    }

    @HandleEvent
    fun onNeuRepoReload(event: NeuRepositoryReloadEvent) {
        val sacksData = event.readConstant<NeuSacksJson>("sacks").sacks
        val uniqueSackItems = mutableSetOf<NEUInternalName>()

        sacksData.values.flatMap { it.contents }.forEach { uniqueSackItems.add(it) }

        sackListInternalNames = uniqueSackItems.map { it.asString() }.toSet()
        sackListNames = uniqueSackItems.map { it.itemNameWithoutColor.removeNonAscii().trim().uppercase() }.toSet()
    }

    private fun updateSacks(changes: SackChangeEvent) {
        sackData = ProfileStorageData.sackProfiles?.sackContents ?: return

        // if it gets added and subtracted but only 1 shows it will be outdated
        val justChanged = mutableMapOf<NEUInternalName, Int>()

        for (change in changes.sackChanges) {
            if (change.internalName in justChanged) {
                justChanged[change.internalName] = (justChanged[change.internalName] ?: 0) + change.delta
            } else {
                justChanged[change.internalName] = change.delta
            }
        }

        for (item in justChanged) {
            if (sackData.containsKey(item.key)) {
                val oldData = sackData[item.key]
                var newAmount = oldData!!.amount + item.value
                var changed = (newAmount - oldData.amount)
                if (newAmount < 0) {
                    newAmount = 0
                    changed = 0
                }
                sackData = sackData.editCopy { this[item.key] = SackItem(newAmount, changed, oldData.getStatus()) }
            } else {
                val newAmount = if (item.value > 0) item.value else 0
                sackData =
                    sackData.editCopy { this[item.key] = SackItem(newAmount, newAmount, SackStatus.OUTDATED) }
            }
        }

        if (changes.otherItemsAdded || changes.otherItemsRemoved) {
            for (item in sackData) {
                if (item.key in justChanged) continue
                val oldData = sackData[item.key]
                sackData = sackData.editCopy { this[item.key] = SackItem(oldData!!.amount, 0, SackStatus.ALRIGHT) }
            }
        }
        saveSackData()
    }

    private fun setSackItem(item: NEUInternalName, amount: Int) {
        sackData = sackData.editCopy { this[item] = SackItem(amount, 0, SackStatus.CORRECT) }
    }

    fun fetchSackItem(item: NEUInternalName): SackItem {
        sackData = ProfileStorageData.sackProfiles?.sackContents ?: return SackItem(0, 0, SackStatus.MISSING)

        if (sackData.containsKey(item)) {
            return sackData[item] ?: return SackItem(0, 0, SackStatus.MISSING)
        }

        sackData = sackData.editCopy { this[item] = SackItem(0, 0, SackStatus.MISSING) }
        return sackData[item] ?: return SackItem(0, 0, SackStatus.MISSING)
    }

    private fun saveSackData() {
        ProfileStorageData.sackProfiles?.sackContents = sackData
        SkyHanniMod.configManager.saveConfig(ConfigFileType.SACKS, "saving-data")

        SackDataUpdateEvent.post()
    }

    data class SackGemstone(
        var internalName: NEUInternalName = NEUInternalName.NONE,
        var rough: Int = 0,
        var flawed: Int = 0,
        var fine: Int = 0,
        var roughPrice: Long = 0,
        var flawedPrice: Long = 0,
        var finePrice: Long = 0,
    ) : AbstractSackItem() {
        val priceSum: Long
            get() = roughPrice + flawedPrice + finePrice
    }

    data class SackRune(
        var stack: ItemStack? = null,
        var lvl1: Int = 0,
        var lvl2: Int = 0,
        var lvl3: Int = 0,
    ) : AbstractSackItem()

    data class SackOtherItem(
        var internalName: NEUInternalName = NEUInternalName.NONE,
        var colorCode: String = "",
        var total: Int = 0,
        var magmaFish: Int = 0,
    ) : AbstractSackItem()

    abstract class AbstractSackItem(
        var stored: Int = 0,
        var price: Long = 0,
        var slot: Int = -1,
    )

    fun NEUInternalName.getAmountInSacksOrNull(): Int? =
        fetchSackItem(this).takeIf { it.statusIsCorrectOrAlright() }?.amount

    fun NEUInternalName.getAmountInSacks(): Int = getAmountInSacksOrNull() ?: 0

    fun testSackAPI(args: Array<String>) {
        if (args.size == 1) {
            if (sackListInternalNames.contains(args[0].uppercase())) {
                ChatUtils.chat("Sack data for ${args[0]}: ${fetchSackItem(args[0].toInternalName())}")
            } else {
                ChatUtils.userError("That item isn't a valid sack item.")
            }
        } else ChatUtils.userError("/shtestsackapi <internal name>")
    }
}

data class SackItem(
    @Expose val amount: Int,
    @Expose val lastChange: Int,
    @Expose private val status: SackStatus?,
) {

    fun getStatus() = status ?: SackStatus.MISSING
    fun statusIsCorrectOrAlright() = getStatus().let { it == SackStatus.CORRECT || it == SackStatus.ALRIGHT }
}

// TODO repo
private val gemstoneMap = mapOf(
    "Jade Gemstones" to "ROUGH_JADE_GEM".toInternalName(),
    "Amber Gemstones" to "ROUGH_AMBER_GEM".toInternalName(),
    "Topaz Gemstones" to "ROUGH_TOPAZ_GEM".toInternalName(),
    "Sapphire Gemstones" to "ROUGH_SAPPHIRE_GEM".toInternalName(),
    "Amethyst Gemstones" to "ROUGH_AMETHYST_GEM".toInternalName(),
    "Jasper Gemstones" to "ROUGH_JASPER_GEM".toInternalName(),
    "Ruby Gemstones" to "ROUGH_RUBY_GEM".toInternalName(),
    "Opal Gemstones" to "ROUGH_OPAL_GEM".toInternalName(),
    "Onyx Gemstones" to "ROUGH_ONYX_GEM".toInternalName(),
    "Aquamarine Gemstones" to "ROUGH_AQUAMARINE_GEM".toInternalName(),
    "Citrine Gemstones" to "ROUGH_CITRINE_GEM".toInternalName(),
    "Peridot Gemstones" to "ROUGH_PERIDOT_GEM".toInternalName(),
)

// ideally should be correct but using alright should also be fine unless they sold their whole sacks
enum class SackStatus {
    MISSING,
    CORRECT,
    ALRIGHT,
    OUTDATED,
}
