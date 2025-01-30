package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.events.minecraft.packet.PacketReceivedEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SoundUtils.playPlingSound
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.TimeUtils.timerColor
import at.hannibal2.skyhanni.utils.Timer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SkyHanniModule
object NonGodPotEffectDisplay {

    private val config get() = SkyHanniMod.feature.misc.potionEffect
    private var checkFooter = false
    private val effectDuration = mutableMapOf<NonGodPotEffect, Timer>()
    private var display = emptyList<String>()

    // TODO move the whole list into the repo
    enum class NonGodPotEffect(
        val tabListName: String,
        val isMixin: Boolean = false,
        val inventoryItemName: String = tabListName,
    ) {

        SMOLDERING("§aSmoldering Polarization I"),
        GLOWY("§2Mushed Glowy Tonic I"),
        WISP("§bWisp's Ice-Flavored Water I"),
        GOBLIN("§2King's Scent I"),

        INVISIBILITY("§8Invisibility I"), // when wearing sorrow armor

        REV("§cZombie Brain Mixin", true),
        TARA("§6Spider Egg Mixin", true),
        SVEN("§bWolf Fur Mixin", true),
        VOID("§6End Portal Fumes", true),
        BLAZE("§fGabagoey", true),
        GLOWING_MUSH("§2Glowing Mush Mixin", true),
        HOT_CHOCOLATE("§6Hot Chocolate Mixin", true),

        DEEP_TERROR("§4Deepterror", true),

        GREAT_SPOOK("§fGreat Spook I", inventoryItemName = "§fGreat Spook Potion"),

        HARVEST_HARBINGER("§6Harvest Harbinger V"),

        PEST_REPELLENT("§6Pest Repellent I§r"),
        PEST_REPELLENT_MAX("§6Pest Repellent II"),

        CURSE_OF_GREED("§4Curse of Greed I"),

        COLD_RESISTANCE_4("§bCold Resistance IV"),

        POWDER_PUMPKIN("§fPowder Pumpkin I"),
        FILET_O_FORTUNE("§fFilet O' Fortune I"),
        CHILLED_PRISTINE_POTATO("§fChilled Pristine Potato I"),
    }

    /**
     * REGEX-TEST:  Repellent: §r§9MAX §r§7(12s)
     */
    private val repellentPattern by RepoPattern.pattern(
        "misc.nongodpot.repellant",
        " Repellent: §r§[97a](?<tier>\\w+)?(?: §r§7\\((?<time>\\d)s\\))?",
    )

    private val effectsCountPattern by RepoPattern.pattern(
        "misc.nongodpot.effects",
        "§7You have §e(?<name>\\d+) §7non-god effects\\.",
    )
    private var totalEffectsCount = 0

    @HandleEvent
    fun onProfileJoin(event: ProfileJoinEvent) {
        effectDuration.clear()
        display = emptyList()
    }

    // todo : cleanup and add support for poison candy I, and add support for splash / other formats
    @HandleEvent
    @Suppress("MaxLineLength")
    fun onChat(event: SkyHanniChatEvent) {
        when (event.message) {
            "§aYou cleared all of your active effects!" -> {
                effectDuration.clear()
                update()
            }

            "§aYou ate a §r§aRe-heated Gummy Polar Bear§r§a!" -> {
                effectDuration[NonGodPotEffect.SMOLDERING] = Timer(1.hours)
                update()
            }

            "§a§lBUFF! §fYou have gained §r§2Mushed Glowy Tonic I§r§f! Press TAB or type /effects to view your active effects!" -> {
                effectDuration[NonGodPotEffect.GLOWY] = Timer(1.hours)
                update()
            }

            "§a§lBUFF! §fYou splashed yourself with §r§bWisp's Ice-Flavored Water I§r§f! Press TAB or type /effects to view your active effects!" -> {
                effectDuration[NonGodPotEffect.WISP] = Timer(5.minutes)
                update()
            }

            "§eYou consumed a §r§fGreat Spook Potion§r§e!" -> {
                effectDuration[NonGodPotEffect.GREAT_SPOOK] = Timer(24.hours)
                update()
            }

            "§a§lBUFF! §fYou have gained §r§6Harvest Harbinger V§r§f! Press TAB or type /effects to view your active effects!" -> {
                effectDuration[NonGodPotEffect.HARVEST_HARBINGER] = Timer(25.minutes)
                update()
            }

            "§a§lYUM! §r§2Pests §r§7will now spawn §r§a2x §r§7less while you break crops for the next §r§a60m§r§7!" -> {
                effectDuration[NonGodPotEffect.PEST_REPELLENT] = Timer(1.hours)
                update()
            }

            "§a§lYUM! §r§2Pests §r§7will now spawn §r§a4x §r§7less while you break crops for the next §r§a60m§r§7!" -> {
                effectDuration[NonGodPotEffect.PEST_REPELLENT_MAX] = Timer(1.hours)
                update()
            }

            "§e[NPC] §6King Yolkar§f: §rThis egg will help me stomach my pain." -> {
                effectDuration[NonGodPotEffect.GOBLIN] = Timer(20.minutes)
                update()
            }

            "§cThe Goblin King's §r§afoul stench §r§chas dissipated!" -> {
                effectDuration.remove(NonGodPotEffect.GOBLIN)
                update()
            }
        }
    }

    private fun update() {
        if (effectDuration.values.removeIf { it.ended }) {
            // to fetch the real amount of active pots
            totalEffectsCount = 0
            checkFooter = true
        }

        display = drawDisplay()
    }

    private fun drawDisplay(): MutableList<String> {
        val newDisplay = mutableListOf<String>()
        for ((effect, time) in effectDuration.sorted()) {
            if (time.ended) continue
            if (effect == NonGodPotEffect.INVISIBILITY) continue

            if (effect.isMixin && !config.nonGodPotEffectShowMixins) continue

            val remaining = time.remaining.coerceAtLeast(0.seconds)
            val format = remaining.format(TimeUnit.HOUR)
            val color = remaining.timerColor()

            val displayName = effect.tabListName
            newDisplay.add("$displayName $color$format")
        }
        val diff = totalEffectsCount - effectDuration.size
        if (diff > 0) {
            newDisplay.add("§eOpen the /effects inventory")
            newDisplay.add("§eto show the missing $diff effects!")
            checkFooter = true
        }
        return newDisplay
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!ProfileStorageData.loaded) return

        if (config.nonGodPotEffectDisplay) update()

        val effectWarning = config.expireWarning
        val effectSound = config.expireSound

        if (!effectWarning && !effectSound) return

        effectDuration.sorted().forEach { (effect, time) ->
            if (time.remaining.inWholeSeconds != config.expireWarnTime.toLong()) return

            if (effectWarning) LorenzUtils.sendTitle(effect.tabListName, 3.seconds)
            if (effectSound) repeat(5) { playPlingSound() }
        }
    }

    @HandleEvent
    fun onWorldChange(event: WorldChangeEvent) {
        checkFooter = true
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
        if (!event.inventoryName.endsWith("Active Effects")) return

        for (stack in event.inventoryItems.values) {
            val name = stack.name
            for (effect in NonGodPotEffect.entries) {
                if (!name.contains(effect.inventoryItemName)) continue
                for (line in stack.getLore()) {
                    if (!line.contains("Remaining") || line == "§7Time Remaining: §aCompleted!" || line.contains("Remaining Uses")) continue
                    val duration = try {
                        TimeUtils.getDuration(line.split("§f")[1])
                    } catch (e: IndexOutOfBoundsException) {
                        ErrorManager.logErrorWithData(
                            e, "Error while reading Non God-Potion effects from tab list",
                            "line" to line,
                        )
                        continue
                    }
                    effectDuration[effect] = Timer(duration)
                    update()
                }
            }
        }
    }

    @HandleEvent(onlyOnIsland = IslandType.GARDEN)
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.PESTS)) return

        event.lines.firstNotNullOfOrNull {
            repellentPattern.matchMatcher(it) {
                // Update repellent timer when near expiration to sync with the in-game countdown delay (which is slow)
                val time = group("time")?.toIntOrNull() ?: return@matchMatcher
                val tier = group("tier")
                val duration = time.toDuration(DurationUnit.SECONDS)
                when (tier) {
                    "MAX" -> effectDuration[NonGodPotEffect.PEST_REPELLENT_MAX] = Timer(duration)
                    "REGULAR" -> effectDuration[NonGodPotEffect.PEST_REPELLENT] = Timer(duration)
                }
                update()
            }
        }
    }

    // TODO use TablistFooterUpdateEvent instead
    @HandleEvent(onlyOnSkyblock = true, priority = HandleEvent.LOW, receiveCancelled = true)
    fun onPacketReceive(event: PacketReceivedEvent) {
        val packet = event.packet
        if (!checkFooter) return
        if (packet is S47PacketPlayerListHeaderFooter) {
            val formattedText = packet.footer.formattedText
            val lines = formattedText.replace("§r", "").split("\n")

            if (!lines.any { it.contains("§a§lActive Effects") }) return
            checkFooter = false

            var effectsCount = 0
            for (line in lines) {
                for (effect in NonGodPotEffect.entries) {
                    val tabListName = effect.tabListName
                    if ("$line§r".startsWith(tabListName)) {
                        val string = line.substring(tabListName.length)
                        try {
                            val duration = TimeUtils.getDuration(string.split("§f")[1])
                            effectDuration[effect] = Timer(duration)
                            update()
                        } catch (e: IndexOutOfBoundsException) {
                            ChatUtils.debug("Error while reading non god pot effects from tab list! line: '$line'")
                        }
                    }
                }
                effectsCountPattern.matchMatcher(line) {
                    val group = group("name")
                    effectsCount = group.toInt()
                }
            }
            totalEffectsCount = effectsCount
        }
    }

    @HandleEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled() || !config.nonGodPotEffectDisplay) return
        if (RiftApi.inRift()) return

        config.nonGodPotEffectPos.renderStrings(
            display,
            extraSpace = 3,
            posLabel = "Non God Pot Effects",
        )
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "misc.nonGodPotEffectDisplay", "misc.potionEffect.nonGodPotEffectDisplay")
        event.move(3, "misc.nonGodPotEffectShowMixins", "misc.potionEffect.nonGodPotEffectShowMixins")
        event.move(3, "misc.nonGodPotEffectPos", "misc.potionEffect.nonGodPotEffectPos")
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && !DungeonApi.inDungeon() && !LorenzUtils.inKuudraFight
}
