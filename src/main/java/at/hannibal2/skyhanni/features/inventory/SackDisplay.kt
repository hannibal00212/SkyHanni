package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.inventory.SackDisplayConfig.NumberFormatEntry
import at.hannibal2.skyhanni.config.features.inventory.SackDisplayConfig.PriceFormatEntry
import at.hannibal2.skyhanni.config.features.inventory.SackDisplayConfig.SortingTypeEntry
import at.hannibal2.skyhanni.data.SackApi
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.addButton
import at.hannibal2.skyhanni.utils.CollectionUtils.addItemStack
import at.hannibal2.skyhanni.utils.CollectionUtils.addSelector
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemPriceSource
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NeuInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NeuItems
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RenderDisplayHelper
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.SearchTextInput
import at.hannibal2.skyhanni.utils.renderables.buildSearchBox
import at.hannibal2.skyhanni.utils.renderables.toSearchable

@SkyHanniModule
object SackDisplay {

    private var display = emptyList<Renderable>()
    private val config get() = SkyHanniMod.feature.inventory.sackDisplay

    private val MAGMA_FISH = "MAGMA_FISH".toInternalName()
    private val normalSacksTextInput = SearchTextInput()
    private val runeSacksTextInput = SearchTextInput()
    private val gemstoneSacksTextInput = SearchTextInput()

    init {
        RenderDisplayHelper(
            inventory = SackApi.inventory,
            condition = { isEnabled() },
        ) {
            config.position.renderRenderables(
                display, extraSpace = config.extraSpace, posLabel = "Sacks Items",
            )
        }
    }

    @HandleEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!SackApi.inventory.isInside()) return
        if (!config.highlightFull) return
        for (slot in InventoryUtils.getItemsInOpenChest()) {
            val lore = slot.stack.getLore()
            if (lore.any { it.startsWith("§7Stored: §a") }) {
                slot highlight LorenzColor.RED
            }
        }
    }

    fun update(savingSacks: Boolean) {
        display = drawDisplay(savingSacks)
    }

    private fun drawDisplay(savingSacks: Boolean) = buildList {
        var totalPrice = 0L
        totalPrice += drawNormalList(savingSacks)
        totalPrice += drawGemstoneDisplay()
        drawRunesDisplay()
        drawOptions(totalPrice)
    }

    private fun MutableList<Renderable>.drawNormalList(savingSacks: Boolean): Long {
        SackApi.getSacksData(savingSacks)
        val sackItems = SackApi.sackItem.toList()
        if (sackItems.isEmpty()) return 0L

        var totalPrice = 0L
        var rendered = 0
        var totalMagmaFish = 0L
        val sortedPairs = sort(sackItems)
        val amountShowing = if (config.itemToShow > sortedPairs.size) sortedPairs.size else config.itemToShow
        addString("§7Items in Sacks: §o(Rendering $amountShowing of ${sortedPairs.size} items)")
        val searchables = buildList {
            for ((itemName, item) in sortedPairs) {
                val (internalName, colorCode, total, magmaFish) = item
                val stored = item.stored
                val price = item.price
                val slot = item.slot

                totalPrice += price
                if (rendered >= config.itemToShow) continue
                if (stored == 0 && !config.showEmpty) continue

                val searchable = buildList {
                    addString(" §7- ")
                    addItemStack(internalName)
                    // TODO move replace into itemName
                    val nameText = Renderable.optionalLink(
                        itemName.replace("§k", ""),
                        onClick = {
                            if (!SackApi.isTrophySack) {
                                BazaarApi.searchForBazaarItem(itemName)
                            }
                        },
                        highlightsOnHoverSlots = listOf(slot),
                    ) { !NeuItems.neuHasFocus() }
                    add(nameText)


                    when (config.numberFormat) {
                        NumberFormatEntry.DEFAULT -> {
                            addAlignedNumber("$colorCode${stored.addSeparators()}")
                            addString("§7/")
                            addAlignedNumber("§b${total.shortFormat()}")
                        }

                        NumberFormatEntry.FORMATTED -> {
                            addAlignedNumber("$colorCode${stored.shortFormat()}")
                            addString("§7/")
                            addAlignedNumber("§b${total.shortFormat()}")
                        }

                        NumberFormatEntry.UNFORMATTED -> {
                            addAlignedNumber("$colorCode${stored.addSeparators()}")
                            addString("§7/")
                            addAlignedNumber("§b${total.addSeparators()}")
                        }

                        else -> {
                            addAlignedNumber("$colorCode${stored.addSeparators()}")
                            addString("§7/")
                            addAlignedNumber("§b${total.addSeparators()}")
                        }
                    }

                    // TODO change color of amount if full
                    // if (colorCode == "§a") addString("§c§l(Full!)")

                    if (SackApi.isTrophySack && magmaFish > 0) {
                        totalMagmaFish += magmaFish
                        add(
                            Renderable.hoverTips(
                                Renderable.string(
                                    "§d$magmaFish",
                                    horizontalAlign = config.alignment,
                                ),
                                listOf(
                                    "§6Magmafish: §b${magmaFish.addSeparators()}",
                                    "§6Magmafish value: §b${price / magmaFish}",
                                    "§6Magmafish per: §b${magmaFish / stored}",
                                ),
                            ),
                        )
                        addItemStack(MAGMA_FISH)
                    }
                    if (config.showPrice && price != 0L) addAlignedNumber("§6${format(price)}")
                }.toSearchable(itemName)
                add(searchable)
                rendered++
            }
        }

        add(searchables.buildSearchBox(normalSacksTextInput))

        if (SackApi.isTrophySack) addString("§cTotal Magmafish: §6${totalMagmaFish.addSeparators()}")
        return totalPrice
    }

    private fun <T : SackApi.AbstractSackItem> sort(sackItems: List<Pair<String, T>>): MutableMap<String, T> {
        val sortedPairs: MutableMap<String, T> = when (config.sortingType) {
            SortingTypeEntry.DESC_STORED -> sackItems.sortedByDescending { it.second.stored }
            SortingTypeEntry.ASC_STORED -> sackItems.sortedBy { it.second.stored }
            SortingTypeEntry.DESC_PRICE -> sackItems.sortedByDescending { it.second.price }
            SortingTypeEntry.ASC_PRICE -> sackItems.sortedBy { it.second.price }
            else -> sackItems.sortedByDescending { it.second.stored }
        }.toMap().toMutableMap()

        for ((k, v) in sortedPairs.toList()) {
            if (v.stored == 0 && !config.showEmpty) {
                sortedPairs.remove(k)
            }
        }
        return sortedPairs
    }

    private fun MutableList<Renderable>.drawOptions(totalPrice: Long) {
        val name = SortType.entries[config.sortingType.ordinal].longName // todo avoid ordinal
        addString("§7Sorted By: §c$name")

        addSelector<SortType>(
            " ",
            getName = { type -> type.shortName },
            isCurrent = { it.ordinal == config.sortingType.ordinal }, // todo avoid ordinal
            onChange = {
                config.sortingType = SortingTypeEntry.entries[it.ordinal] // todo avoid ordinals
                update(false)
            },
        )

        addButton(
            prefix = "§7Number format: ",
            getName = NumberFormat.entries[config.numberFormat.ordinal].displayName, // todo avoid ordinal
            onChange = {
                // todo avoid ordinal
                config.numberFormat =
                    NumberFormatEntry.entries[(config.numberFormat.ordinal + 1) % 3]
                update(false)
            },
        )

        if (config.showPrice) {
            addSelector<ItemPriceSource>(
                " ",
                getName = { type -> type.sellName },
                isCurrent = { it.ordinal == config.priceSource.ordinal }, // todo avoid ordinal
                onChange = {
                    config.priceSource = ItemPriceSource.entries[it.ordinal] // todo avoid ordinal
                    update(false)
                },
            )
            addButton(
                prefix = "§7Price Format: ",
                getName = PriceFormat.entries[config.priceFormat.ordinal].displayName, // todo avoid ordinal
                onChange = {
                    // todo avoid ordinal
                    config.priceFormat =
                        PriceFormatEntry.entries[(config.priceFormat.ordinal + 1) % 2]
                    update(false)
                },
            )
            addString("§eTotal price: §6${format(totalPrice)}")
        }
    }

    private fun MutableList<Renderable>.drawRunesDisplay() {
        if (SackApi.runeItem.isEmpty()) return
        addString("§7Runes:")
        val searchables = buildList {
            for ((name, rune) in sort(SackApi.runeItem.toList())) {
                val (stack, lv1, lv2, lv3) = rune
                val searchable = buildList {
                    addString(" §7- ")
                    stack?.let { addItemStack(it) }
                    add(
                        Renderable.optionalLink(
                            name,
                            onClick = {},
                            highlightsOnHoverSlots = listOf(rune.slot),
                        ),
                    )
                    addAlignedNumber("§e$lv1")
                    addAlignedNumber("§e$lv2")
                    addAlignedNumber("§e$lv3")
                }.toSearchable(name)
                add(searchable)
            }
        }
        add(searchables.buildSearchBox(runeSacksTextInput))
    }

    private fun MutableList<Renderable>.drawGemstoneDisplay(): Long {
        if (SackApi.gemstoneItem.isEmpty()) return 0L
        addString("§7Gemstones:")
        var totalPrice = 0L
        val searchables = buildList {
            for ((name, gem) in sort(SackApi.gemstoneItem.toList())) {
                val searchable = buildList {
                    addString(" §7- ")
                    addItemStack(gem.internalName)
                    add(
                        Renderable.optionalLink(
                            name,
                            onClick = {
                                BazaarApi.searchForBazaarItem(name.dropLast(1))
                            },
                            highlightsOnHoverSlots = listOf(gem.slot),
                        ) { !NeuItems.neuHasFocus() },
                    )
                    addAlignedNumber(gem.rough.addSeparators())
                    addAlignedNumber("§a${gem.flawed.addSeparators()}")
                    addAlignedNumber("§9${gem.fine.addSeparators()}")
                    val price = gem.priceSum
                    totalPrice += price
                    if (config.showPrice && price != 0L) addAlignedNumber("§7(§6${format(price)}§7)")
                }.toSearchable(name)
                add(searchable)
            }
        }

        add(searchables.buildSearchBox(gemstoneSacksTextInput))
        return totalPrice
    }

    private fun MutableList<Renderable>.addAlignedNumber(string: String) {
        addString(string, horizontalAlign = config.alignment)
    }

    private fun format(price: Long) = if (config.priceFormat == PriceFormatEntry.FORMATTED) {
        price.shortFormat()
    } else {
        price.addSeparators()
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled

    // TODO: one mod wide enum for sorttype, priceformat, etc instead of one per feature (e.g. ChestValue)
    enum class SortType(val shortName: String, val longName: String) {
        STORED_DESC("Stored D", "Stored Descending"),
        STORED_ASC("Stored A", "Stored Ascending"),
        PRICE_DESC("Price D", "Price Descending"),
        PRICE_ASC("Price A", "Price Ascending"),
    }

    enum class PriceFormat(val displayName: String) {
        FORMATTED("Formatted"),
        UNFORMATTED("Unformatted"),
    }

    enum class NumberFormat(val displayName: String) {
        DEFAULT("Default"),
        FORMATTED("Formatted"),
        UNFORMATTED("Unformatted"),
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(15, "inventory.sackDisplay.numberFormat") { element ->
            ConfigUtils.migrateIntToEnum(element, NumberFormatEntry::class.java)
        }
        event.transform(15, "inventory.sackDisplay.priceFormat") { element ->
            ConfigUtils.migrateIntToEnum(element, PriceFormatEntry::class.java)
        }
        event.transform(15, "inventory.sackDisplay.sortingType") { element ->
            ConfigUtils.migrateIntToEnum(element, SortingTypeEntry::class.java)
        }
    }
}
