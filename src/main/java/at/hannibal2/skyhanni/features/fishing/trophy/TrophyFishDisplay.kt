package at.hannibal2.skyhanni.features.fishing.trophy

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.features.fishing.trophyfishing.TrophyFishDisplayConfig.HideCaught
import at.hannibal2.skyhanni.config.features.fishing.trophyfishing.TrophyFishDisplayConfig.TextPart
import at.hannibal2.skyhanni.config.features.fishing.trophyfishing.TrophyFishDisplayConfig.TrophySorting
import at.hannibal2.skyhanni.config.features.fishing.trophyfishing.TrophyFishDisplayConfig.WhenToShow
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.fishing.TrophyFishCaughtEvent
import at.hannibal2.skyhanni.features.fishing.FishingAPI
import at.hannibal2.skyhanni.features.misc.items.EstimatedItemValue
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.addSingleString
import at.hannibal2.skyhanni.utils.CollectionUtils.addString
import at.hannibal2.skyhanni.utils.CollectionUtils.sumAllValues
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.renderables.Renderable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TrophyFishDisplay {
    private val config get() = SkyHanniMod.feature.fishing.trophyFishing.display

    private val recentlyDroppedTrophies = TimeLimitedCache<NEUInternalName, TrophyRarity>(5.seconds)
    private val itemNameCache = mutableMapOf<String, NEUInternalName>()

    private var display = emptyList<Renderable>()

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.CRIMSON_ISLE) {
            DelayedRun.runDelayed(200.milliseconds) {
                update()
            }
        }
    }

    @HandleEvent
    fun onTrophyFishCaught(event: TrophyFishCaughtEvent) {
        recentlyDroppedTrophies[getInternalName(event.trophyFishName)] = event.rarity
        update()
        DelayedRun.runDelayed(5.1.seconds) {
            update()
        }
    }

    @SubscribeEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        display = emptyList()
        update()
    }

    @SubscribeEvent
    fun onConfigReload(event: ConfigLoadEvent) {
        with(config) {
            ConditionalUtils.onToggle(
                enabled,
                highlightNew,
                extraSpace,
                sortingType,
                reverseOrder,
                textOrder,
                showCross,
                showCheckmark,
                onlyShowMissing,
            ) {
                update()
            }
        }
    }


    fun update() {
        if (!isEnabled()) return
        val list = mutableListOf<Renderable>()
        list.addString("§e§lTrophy Fish Display")
        list.add(Renderable.table(createTable(), yPadding = config.extraSpace.get()))

        display = list
    }

    private fun createTable(): List<List<Renderable>> {
        val trophyFishes = TrophyFishManager.fish ?: return emptyList()
        val table = mutableListOf<List<Renderable>>()
        for ((rawName, data) in getOrder(trophyFishes)) {
            addRow(rawName, data, table)
        }
        if (table.isEmpty()) {
            get(config.onlyShowMissing.get())?.let { rarity ->
                val name = rarity.formattedString
                table.addSingleString("§eYou caught all $name Trophy Fishes")
                if (rarity != TrophyRarity.DIAMOND) {
                    table.addSingleString("§cChange §eOnly Show Missing §cin the config to show more.")
                }
            }
        }
        return table
    }

    private fun addRow(
        rawName: String,
        data: MutableMap<TrophyRarity, Int>,
        table: MutableList<List<Renderable>>,
    ) {
        get(config.onlyShowMissing.get())?.let { atLeast ->
            val list = TrophyRarity.entries.filter { it == atLeast }
            if (list.all { (data[it] ?: 0) > 0 }) {
                return
            }
        }
        val hover = TrophyFishAPI.hoverInfo(rawName)
        fun string(string: String): Renderable = hover?.let {
            Renderable.hoverTips(Renderable.string(string), tips = it.split("\n"))
        } ?: Renderable.string(string)

        val row = mutableMapOf<TextPart, Renderable>()
        row[TextPart.NAME] = string(getItemName(rawName))

        val internalName = getInternalName(rawName)
        row[TextPart.ICON] = Renderable.itemStack(internalName.getItemStack())

        val recentlyDroppedRarity = recentlyDroppedTrophies.getOrNull(internalName).takeIf { config.highlightNew.get() }

        for (rarity in TrophyRarity.entries) {
            val amount = data[rarity] ?: 0
            val recentlyDropped = rarity == recentlyDroppedRarity
            val format = if (config.showCross.get() && amount == 0) "§c✖" else {
                val color = if (recentlyDropped) "§a" else rarity.formatCode
                val numberFormat = if (config.showCheckmark.get() && amount >= 1) "§l✔" else amount.addSeparators()
                "$color$numberFormat"
            }
            row[get(rarity)] = string(format)
        }
        val total = data.sumAllValues()
        val color = if (recentlyDroppedRarity != null) "§a" else "§5"
        row[TextPart.TOTAL] = string("$color${total.addSeparators()}")

        table.add(config.textOrder.get().mapNotNull { row[it] })
    }

    private fun get(value: TrophyRarity) = when (value) {
        TrophyRarity.BRONZE -> TextPart.BRONZE
        TrophyRarity.SILVER -> TextPart.SILVER
        TrophyRarity.GOLD -> TextPart.GOLD
        TrophyRarity.DIAMOND -> TextPart.DIAMOND
    }

    private fun get(value: HideCaught) = when (value) {
        HideCaught.NONE -> null
        HideCaught.BRONZE -> TrophyRarity.BRONZE
        HideCaught.SILVER -> TrophyRarity.SILVER
        HideCaught.GOLD -> TrophyRarity.GOLD
        HideCaught.DIAMOND -> TrophyRarity.DIAMOND
    }

    private fun getOrder(trophyFishes: MutableMap<String, MutableMap<TrophyRarity, Int>>) = sort(trophyFishes).let {
        if (config.reverseOrder.get()) it.reversed() else it
    }

    private fun sort(trophyFishes: Map<String, MutableMap<TrophyRarity, Int>>): List<Map.Entry<String, MutableMap<TrophyRarity, Int>>> =
        when (config.sortingType.get()!!) {
            TrophySorting.TOTAL_AMOUNT -> trophyFishes.entries.sortedBy { it.value.sumAllValues() }

            TrophySorting.BRONZE_AMOUNT -> count(trophyFishes, TrophyRarity.BRONZE)
            TrophySorting.SILVER_AMOUNT -> count(trophyFishes, TrophyRarity.SILVER)
            TrophySorting.GOLD_AMOUNT -> count(trophyFishes, TrophyRarity.GOLD)
            TrophySorting.DIAMOND_AMOUNT -> count(trophyFishes, TrophyRarity.DIAMOND)

            TrophySorting.ITEM_RARITY -> {
                trophyFishes.entries.sortedBy { data ->
                    val name = getInternalName(data.key)
                    name.getItemStack().getItemRarityOrNull()
                }
            }

            TrophySorting.HIGHEST_RARITY -> {
                trophyFishes.entries.sortedBy { data ->
                    TrophyRarity.entries.filter {
                        data.value.contains(it)
                    }.maxByOrNull { it.ordinal }
                }
            }

            TrophySorting.NAME -> {
                trophyFishes.entries.sortedBy { data ->
                    getItemName(data.key).removeColor()
                }
            }
        }

    private fun count(
        trophyFishes: Map<String, MutableMap<TrophyRarity, Int>>, rarity: TrophyRarity,
    ) = trophyFishes.entries.sortedBy { it.value[rarity] ?: 0 }

    private fun getItemName(rawName: String): String {
        val name = getInternalName(rawName).itemName
        return name.split(" ").dropLast(1).joinToString(" ")
    }

    private fun getInternalName(name: String): NEUInternalName {
        itemNameCache[name]?.let {
            return it
        }
        // getOrPut does not support our null check
        readInternalName(name)?.let {
            itemNameCache[name] = it
            return it
        }

        ErrorManager.skyHanniError(
            "No Trophy Fishing name found",
            "name" to name
        )
    }

    private fun readInternalName(rawName: String): NEUInternalName? {
        for ((name, internalName) in NEUItems.allItemsCache) {
            val test = name.removeColor().replace(" ", "").replace("-", "")
            if (test.startsWith(rawName)) {
                return internalName
            }
        }
        if (rawName.endsWith("1")) return "OBFUSCATED_FISH_1_BRONZE".toInternalName()
        if (rawName.endsWith("2")) return "OBFUSCATED_FISH_2_BRONZE".toInternalName()
        if (rawName.endsWith("3")) return "OBFUSCATED_FISH_3_BRONZE".toInternalName()

        return null
    }

    @SubscribeEvent
    fun onGuiRender(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (!canRender()) return
        if (EstimatedItemValue.isCurrentlyShowing()) return

        if (config.requireHunterArmor.get() && !FishingAPI.wearingTrophyArmor) return

        config.position.renderRenderables(
            display,
            extraSpace = config.extraSpace.get(),
            posLabel = "Trophy Fishing Display"
        )
    }

    fun canRender(): Boolean = when (config.whenToShow.get()!!) {
        WhenToShow.ALWAYS -> true
        WhenToShow.ONLY_IN_INVENTORY -> Minecraft.getMinecraft().currentScreen is GuiInventory
        WhenToShow.ONLY_WITH_ROD_IN_HAND -> FishingAPI.holdingLavaRod
        WhenToShow.ONLY_WITH_KEYBIND -> config.keybind.isKeyHeld()
    }

    fun isEnabled() = (IslandType.CRIMSON_ISLE.isInIsland() || LorenzUtils.isStrandedProfile) && config.enabled.get()
}
