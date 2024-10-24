package at.hannibal2.skyhanni.features.misc.items.enchants

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.inventory.EnchantParsingConfig
import at.hannibal2.skyhanni.config.features.inventory.EnchantParsingConfig.CommaFormat
import at.hannibal2.skyhanni.events.ChatHoverEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.item.ItemHoverEvent
import at.hannibal2.skyhanni.features.chroma.ChromaManager
import at.hannibal2.skyhanni.mixins.hooks.GuiChatHook
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.isEnchanted
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getExtraAttributes
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.event.HoverEvent
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.TreeSet

/**
 * Modified Enchant Parser from [SkyblockAddons](https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/java/codes/biscuit/skyblockaddons/features/enchants/EnchantManager.java)
 */
@SkyHanniModule
object EnchantParser {

    private val config get() = SkyHanniMod.feature.inventory.enchantParsing

    val patternGroup = RepoPattern.group("misc.items.enchantparsing")
    val enchantmentPattern by patternGroup.pattern(
        "enchants.new",
        "(§7§l|§d§l|§9)(?<enchant>[A-Za-z][A-Za-z '-]+) (?<levelNumeral>[IVXLCDM]+|[0-9]+)(?<stacking>(§r)?§9, |\$| §8\\d{1,3}(,\\d{3})*)",
    )
    private val grayEnchantPattern by patternGroup.pattern(
        "grayenchants", "^(Respiration|Aqua Affinity|Depth Strider|Efficiency).*",
    )

    private var currentItem: ItemStack? = null

    private var indexOfLastGrayEnchant = -1
    private var startEnchant = -1
    private var endEnchant = -1

    // Stacking enchants with their progress visible should have the
    // enchants stacked in a single column
    private var shouldBeSingleColumn = false

    // Used to determine how many enchants are used on each line
    // for this particular item, since consistency is not Hypixel's strong point
    private var maxEnchantsPerLine = 0
    private var loreLines: MutableList<String> = mutableListOf()
    private var orderedEnchants: TreeSet<FormattedEnchant> = TreeSet()

    private val loreCache: Cache = Cache()

    val isSbaLoaded by lazy { Loader.isModLoaded("skyblockaddons") }

    // Maps for all enchants
    private var enchants: EnchantsJson = EnchantsJson()

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        this.enchants = event.getConstant<EnchantsJson>("Enchants")
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        // Add observers to config options that would need us to mark cache dirty
        ConditionalUtils.onToggle(
            config.colorParsing,
            config.format,
            config.perfectEnchantColor,
            config.greatEnchantColor,
            config.goodEnchantColor,
            config.poorEnchantColor,
            config.commaFormat,
            config.hideVanillaEnchants,
            config.hideEnchantDescriptions,
            ChromaManager.config.enabled,
        ) {
            markCacheDirty()
        }
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onTooltipEvent(event: ItemHoverEvent) {
        // If enchants doesn't have any enchant data then we have no data to parse enchants correctly
        if (!this.enchants.hasEnchantData()) return

        currentItem = event.itemStack

        // The enchants we expect to find in the lore, found from the items NBT data
        val enchants = event.itemStack.getEnchantments() ?: return

        // Check for any vanilla gray enchants at the top of the tooltip
        indexOfLastGrayEnchant = accountForAndRemoveGrayEnchants(event.toolTip, event.itemStack)

        parseEnchants(event.toolTip, enchants, null)
    }

    /**
     * For tooltips that are shown when hovering over an item from /show
     */
    @SubscribeEvent
    fun onChatHoverEvent(event: ChatHoverEvent) {
        if (event.getHoverEvent().action != HoverEvent.Action.SHOW_TEXT) return
        if (!isEnabled() || !this.enchants.hasEnchantData()) return

        currentItem = null

        val lore = event.getHoverEvent().value.formattedText.split("\n").toMutableList()

        // Check for any vanilla gray enchants at the top of the tooltip
        indexOfLastGrayEnchant = accountForAndRemoveGrayEnchants(lore, null)

        // Since we don't get given an item stack from /show, we pass an empty enchants map and
        // use all enchants from the Enchants class instead
        parseEnchants(lore, mapOf(), event.component)
    }

    private fun parseEnchants(
        loreList: MutableList<String>,
        enchants: Map<String, Int>,
        chatComponent: IChatComponent?,
    ) {
        // Check if the lore is already cached so continuous hover isn't 1 fps
        if (loreCache.isCached(loreList)) {
            loreList.clear()
            if (loreCache.cachedLoreAfter.isNotEmpty()) {
                loreList.addAll(loreCache.cachedLoreAfter)
            } else {
                loreList.addAll(loreCache.cachedLoreBefore)
            }
            // Need to still set replacement component even if its cached
            if (chatComponent != null) editChatComponent(chatComponent, loreList)
            return
        }
        loreCache.updateBefore(loreList)

        // Find where the enchants start and end
        enchantStartAndEnd(loreList, enchants)

        if (endEnchant == -1) {
            loreCache.updateAfter(loreList)
            return
        }

        shouldBeSingleColumn = false
        loreLines = mutableListOf()
        orderedEnchants = TreeSet()
        maxEnchantsPerLine = 0

        // Order all enchants
        orderEnchants(loreList)

        if (orderedEnchants.isEmpty()) {
            loreCache.updateAfter(loreList)
            return
        }

        // If we have color parsing off and hide enchant descriptions on, remove them and return from method
        if (!config.colorParsing.get()) {
            if (config.hideEnchantDescriptions.get()) {
                if (itemIsBook()) {
                    loreCache.updateAfter(loreList)
                    return
                }
                loreList.removeAll(loreLines)
                loreCache.updateAfter(loreList)
                if (chatComponent != null) editChatComponent(chatComponent, loreList)
                return
            }
            loreCache.updateAfter(loreList)
            return
        }

        // Remove enchantment lines so we can insert ours
        try {
            loreList.subList(startEnchant, endEnchant + 1).clear()
        } catch (e: IndexOutOfBoundsException) {
            ErrorManager.logErrorWithData(
                e,
                "Error parsing enchantment info from item",
                "loreList" to loreList,
                "startEnchant" to startEnchant,
                "endEnchant" to endEnchant,
            )
            return
        }

        val insertEnchants: MutableList<String> = mutableListOf()

        // Format enchants based on format config option
        try {
            formatEnchants(insertEnchants)
        } catch (e: ArithmeticException) {
            ErrorManager.logErrorWithData(
                e,
                "Item has enchants in nbt but none were found?",
                "item" to currentItem,
                "loreList" to loreList,
                "nbt" to currentItem?.getExtraAttributes()
            )
            return
        } catch (e: ConcurrentModificationException) {
            ErrorManager.logErrorWithData(
                e,
                "ConcurrentModificationException whilst formatting enchants",
                "loreList" to loreList,
                "format" to config.format.get(),
                "orderedEnchants" to orderedEnchants
            )
        }

        // Add our parsed enchants back into the lore
        loreList.addAll(startEnchant, insertEnchants)
        // Cache parsed lore
        loreCache.updateAfter(loreList)

        // Alter the chat component value if one was passed
        if (chatComponent != null) {
            editChatComponent(chatComponent, loreList)
        }
    }

    private fun enchantStartAndEnd(loreList: MutableList<String>, enchants: Map<String, Int>) {
        var startEnchant = -1
        var endEnchant = -1

        val startIndex = if (indexOfLastGrayEnchant == -1) 0 else indexOfLastGrayEnchant + 1
        for (i in startIndex until loreList.size) {
            val strippedLine = loreList[i].removeColor()

            if (startEnchant == -1) {
                if (this.enchants.containsEnchantment(enchants, loreList[i])) startEnchant = i
            } else if (strippedLine.trim().isEmpty() && endEnchant == -1) endEnchant = i - 1
        }

        this.startEnchant = startEnchant
        this.endEnchant = endEnchant
    }

    private fun orderEnchants(loreList: MutableList<String>) {
        var lastEnchant: FormattedEnchant? = null

        for (i in startEnchant..endEnchant) {
            val matcher = enchantmentPattern.matcher(loreList[i])
            var containsEnchant = false
            var enchantsOnThisLine = 0
            var isRoman = true

            while (matcher.find()) {
                // Pull enchant, enchant level and stacking amount if applicable
                val enchant = this.enchants.getFromLore(matcher.group("enchant"))
                val level = try {
                    // If one enchant is not a roman numeral we assume all are not roman numerals (idk a situation where this wouldn't be the case)
                    matcher.group("levelNumeral").toInt().also { isRoman = false }
                } catch (e: NumberFormatException) {
                    matcher.group("levelNumeral").romanToDecimal()
                }
                val stacking = if (matcher.group("stacking").trimStart().removeColor().matches("[\\d,]+\$".toRegex())) {
                    shouldBeSingleColumn = true
                    matcher.group("stacking")
                } else "empty"

                // Last found enchant
                lastEnchant = FormattedEnchant(enchant, level, stacking, isRoman)

                if (!orderedEnchants.add(lastEnchant)) {
                    for (e: FormattedEnchant in orderedEnchants) {
                        if (lastEnchant?.let { e.compareTo(it) } == 0) {
                            lastEnchant = e
                            break
                        }
                    }
                }

                containsEnchant = true
                enchantsOnThisLine++
            }

            maxEnchantsPerLine = if (enchantsOnThisLine > maxEnchantsPerLine) enchantsOnThisLine else maxEnchantsPerLine

            if (!containsEnchant && lastEnchant != null) {
                lastEnchant.addLore(loreList[i])
                loreLines.add(loreList[i])
            }
        }
    }

    private fun formatEnchants(insertEnchants: MutableList<String>) {
        // Normal is leaving the formatting as Hypixel provides it
        if (config.format.get() == EnchantParsingConfig.EnchantFormat.NORMAL) {
            normalFormatting(insertEnchants)
            // Compressed is always forcing 3 enchants per line, except when there is stacking enchant progress visible
        } else if (config.format.get() == EnchantParsingConfig.EnchantFormat.COMPRESSED && !shouldBeSingleColumn) {
            compressedFormatting(insertEnchants)
            // Stacked is always forcing 1 enchant per line
        } else {
            stackedFormatting(insertEnchants)
        }
    }

    private fun normalFormatting(insertEnchants: MutableList<String>) {
        val commaFormat = config.commaFormat.get()
        var builder = StringBuilder()

        for ((i, orderedEnchant: FormattedEnchant) in orderedEnchants.withIndex()) {
            val comma = if (commaFormat == CommaFormat.COPY_ENCHANT) ", " else "§9, "

            builder.append(orderedEnchant.getFormattedString(currentItem))
            if (i % maxEnchantsPerLine != maxEnchantsPerLine - 1) {
                builder.append(comma)
            } else {
                insertEnchants.add(builder.toString())

                // This will only add enchant descriptions if there were any to begin with
                if (!config.hideEnchantDescriptions.get() || itemIsBook()) insertEnchants.addAll(orderedEnchant.getLore())

                builder = StringBuilder()
            }
        }

        finishFormatting(insertEnchants, builder, commaFormat)
    }

    private fun compressedFormatting(insertEnchants: MutableList<String>) {
        val commaFormat = config.commaFormat.get()
        var builder = StringBuilder()

        for ((i, orderedEnchant: FormattedEnchant) in orderedEnchants.withIndex()) {
            val comma = if (commaFormat == CommaFormat.COPY_ENCHANT) ", " else "§9, "

            builder.append(orderedEnchant.getFormattedString(currentItem))

            if (itemIsBook() && maxEnchantsPerLine == 1) {
                insertEnchants.add(builder.toString())
                insertEnchants.addAll(orderedEnchant.getLore())
                builder = StringBuilder()
            } else {
                if (i % 3 != 2) {
                    builder.append(comma)
                } else {
                    insertEnchants.add(builder.toString())
                    builder = StringBuilder()
                }
            }
        }

        finishFormatting(insertEnchants, builder, commaFormat)
    }

    private fun stackedFormatting(insertEnchants: MutableList<String>) {
        if (!config.hideEnchantDescriptions.get() || itemIsBook()) {
            for (enchant: FormattedEnchant in orderedEnchants) {
                insertEnchants.add(enchant.getFormattedString(currentItem))
                insertEnchants.addAll(enchant.getLore())
            }
        } else {
            for (enchant: FormattedEnchant in orderedEnchants) {
                insertEnchants.add(enchant.getFormattedString(currentItem))
            }
        }
    }

    private fun finishFormatting(
        insertEnchants: MutableList<String>,
        builder: StringBuilder,
        commaFormat: CommaFormat,
    ) {
        if (builder.isNotEmpty()) insertEnchants.add(builder.toString())

        // Check if there is a trailing space (therefore also a comma) and remove the last 2 chars
        if (insertEnchants.last().last() == ' ') {
            insertEnchants[insertEnchants.lastIndex] =
                insertEnchants.last().dropLast(if (commaFormat == CommaFormat.COPY_ENCHANT) 2 else 4)
        }
    }

    private fun editChatComponent(chatComponent: IChatComponent, loreList: MutableList<String>) {
        val text = loreList.joinToString("\n").dropLast(2)

        // Just set the component text to the entire lore list instead of reconstructing the entire siblings tree
        val chatComponentText = ChatComponentText(text)
        val hoverEvent = HoverEvent(chatComponent.chatStyle.chatHoverEvent?.action, chatComponentText)

        GuiChatHook.replaceOnlyHoverEvent(hoverEvent)
    }

    /**
     * Finds where the gray enchants (vanilla) end and optionally remove them from the tooltip.
     *
     * Allow for a null item stack in odd situations like when other mods add them in chat components, i.e,
     * Skytils party finder feature showing a players inventory in chat
     */
    private fun accountForAndRemoveGrayEnchants(loreList: MutableList<String>, item: ItemStack?): Int {
        if (item != null) {
            // If the item has no enchantmentTagList then there will be no gray enchants
            if (!item.isEnchanted() || item.enchantmentTagList.tagCount() == 0) return -1
        }

        var lastGrayEnchant = -1
        val removeGrayEnchants = config.hideVanillaEnchants.get()

        var i = 1
        repeat(2) { // Using the fact that there should be at most 2 vanilla enchants
            if (i + 1 >= loreList.size) return@repeat // In case the tooltip is very short (i.e, hovering over a short chat component)
            val line = loreList[i]
            if (grayEnchantPattern.matcher(line).matches()) {
                lastGrayEnchant = i

                if (removeGrayEnchants) loreList.removeAt(i) else i++
            } else {
                i++
            }
        }

        return if (removeGrayEnchants) -1 else lastGrayEnchant
    }

    private fun itemIsBook(): Boolean {
        return currentItem?.getItemCategoryOrNull() == ItemCategory.ENCHANTED_BOOK
    }

    // We don't check if the main toggle here since we still need to go into
    // the parseEnchants method to deal with hiding vanilla enchants
    // and enchant descriptions
    fun isEnabled() = LorenzUtils.inSkyBlock

    private fun markCacheDirty() {
        loreCache.configChanged = true
    }
}
