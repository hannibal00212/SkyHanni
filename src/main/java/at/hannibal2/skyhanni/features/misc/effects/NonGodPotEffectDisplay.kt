package at.hannibal2.skyhanni.features.misc.effects

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.EffectApi.NonGodPotEffect
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.TablistFooterUpdateEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.events.effects.EffectDurationChangeEvent
import at.hannibal2.skyhanni.events.effects.EffectDurationChangeType
import at.hannibal2.skyhanni.events.minecraft.WorldChangeEvent
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.rift.RiftApi
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.sorted
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SoundUtils.playPlingSound
import at.hannibal2.skyhanni.utils.TimeUnit
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.TimeUtils.timerColor
import at.hannibal2.skyhanni.utils.Timer
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object NonGodPotEffectDisplay {

    private val config get() = SkyHanniMod.feature.misc.potionEffect
    private var checkFooter = false
    private val effectDuration = mutableMapOf<NonGodPotEffect, Timer>()
    private var display = emptyList<String>()

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

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        if (event.message == "§aYou cleared all of your active effects!") {
            effectDuration.clear()
            update()
        }
    }

    @HandleEvent
    fun onEffectUpdate(event: EffectDurationChangeEvent) {
        when (event.durationChangeType) {
            EffectDurationChangeType.ADD -> {
                event.duration?.let {
                    val existing = effectDuration[event.effect]?.duration ?: Duration.ZERO
                    effectDuration[event.effect] = Timer(existing + it)
                }
            }

            EffectDurationChangeType.SET -> {
                event.duration?.let {
                    effectDuration[event.effect] = Timer(it)
                }
            }

            EffectDurationChangeType.REMOVE -> {
                effectDuration.remove(event.effect)
            }
        }
        update()
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
    fun onTabUpdate(event: TablistFooterUpdateEvent) {
        if (!checkFooter) return
        val lines = event.footer.split("\n")
        if (!lines.any { it.contains("§a§lActive Effects") }) return

        checkFooter = false
        var effectsCount = 0
        for (line in lines) {
            effectsCountPattern.matchMatcher(line) {
                val group = group("name")
                effectsCount = group.toInt()
            }
        }
        totalEffectsCount = effectsCount
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
