package at.hannibal2.skyhanni.features.misc.trevor

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.misc.TrevorTheTrapperConfig.TrackerEntry
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.events.CheckRenderEntityEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzKeyPressEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.mixins.hooks.RenderLivingEntityHelper
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ColorUtils.withAlpha
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.EntityUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LocationUtils.distanceToPlayer
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUItems
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawString
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.getLorenzVec
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object TrevorFeatures {
    private val patternGroup = RepoPattern.group("misc.trevor")
    private val trapperPattern by patternGroup.pattern(
        "trapper",
        "\\[NPC] Trevor: You can find your (?<rarity>.*) animal near the (?<location>.*).",
    )
    private val talbotPatternAbove by patternGroup.pattern(
        "above",
        "The target is around (?<height>.*) blocks above, at a (?<angle>.*) degrees angle!",
    )
    private val talbotPatternBelow by patternGroup.pattern(
        "below",
        "The target is around (?<height>.*) blocks below, at a (?<angle>.*) degrees angle!",
    )
    private val talbotPatternAt by patternGroup.pattern(
        "at",
        "You are at the exact height!",
    )
    private val locationPattern by patternGroup.pattern(
        "zone",
        "Location: (?<zone>.*)",
    )
    private val mobDiedPattern by patternGroup.pattern(
        "mob.died",
        "§aReturn to the Trapper soon to get a new animal to hunt!",
    )
    private val startDialoguePattern by patternGroup.pattern(
        "start.dialogue",
        "[NPC] Trevor: You will have 10 minutes to find the mob from when you accept the task.",
    )
    private val outOfTimePattern by patternGroup.pattern(
        "outoftime",
        "You ran out of time and the animal disappeared!",
    )
    private val clickOptionPattern by patternGroup.pattern(
        "clickoption",
        "Click an option: §r§a§l\\[YES]§r§7 - §r§c§l\\[NO]",
    )
    private val areaTrappersDenPattern by patternGroup.pattern(
        "area.trappersden",
        "Trapper's Den",
    )

    private var timeUntilNextReady = 0
    private var trapperReady: Boolean = true
    private var currentStatus = TrapperStatus.READY
    private var currentLabel = "§2Ready"
    private var trapperID: Int = 56
    private var backupTrapperID: Int = 17
    private var timeLastWarped = SimpleTimeMark.farPast()
    private var lastChatPrompt = ""
    private var lastChatPromptTime = SimpleTimeMark.farPast()
    private var teleportBlock = SimpleTimeMark.farPast()

    var questActive = false
    var inBetweenQuests = false
    var inTrapperDen = false

    private val config get() = SkyHanniMod.feature.misc.trevorTheTrapper

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!onFarmingIsland()) return
        updateTrapper()
        TrevorTracker.update()
        TrevorTracker.calculatePeltsPerHour()
        if (config.trapperSolver && questActive) {
            TrevorSolver.findMob()
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!onFarmingIsland()) return

        val formattedMessage = event.message.removeColor()

        mobDiedPattern.matchMatcher(event.message) {
            TrevorSolver.resetLocation()
            if (config.trapperMobDiedMessage) {
                LorenzUtils.sendTitle("§2Mob Died ", 5.seconds)
                SoundUtils.playBeepSound()
            }
            trapperReady = true
            TrevorSolver.mobLocation = TrapperMobArea.NONE
            if (timeUntilNextReady <= 0) {
                currentStatus = TrapperStatus.READY
                currentLabel = "§2Ready"
            } else {
                currentStatus = TrapperStatus.WAITING
                currentLabel = if (timeUntilNextReady == 1) "§31 second left" else "§3$timeUntilNextReady seconds left"
            }
            TrevorSolver.mobLocation = TrapperMobArea.NONE
        }

        trapperPattern.matchMatcher(formattedMessage) {
            timeUntilNextReady = if (Perk.PELT_POCALYPSE.isActive) 16 else 21
            currentStatus = TrapperStatus.ACTIVE
            currentLabel = "§cActive Quest"
            trapperReady = false
            TrevorTracker.startQuest(this)
            updateTrapper()
            lastChatPromptTime = SimpleTimeMark.farPast()
        }

        talbotPatternAbove.matchMatcher(formattedMessage) {
            val height = group("height").toInt()
            TrevorSolver.findMobHeight(height, true)
        }
        talbotPatternBelow.matchMatcher(formattedMessage) {
            val height = group("height").toInt()
            TrevorSolver.findMobHeight(height, false)
        }
        talbotPatternAt.matchMatcher(formattedMessage) {
            TrevorSolver.averageHeight = LocationUtils.playerLocation().y
        }

        startDialoguePattern.matchMatcher(formattedMessage) {
            teleportBlock = SimpleTimeMark.now()
        }
        outOfTimePattern.matchMatcher(formattedMessage) {
            resetTrapper()
        }

        clickOptionPattern.findMatcher(event.message) {
            for (sibling in event.chatComponent.siblings) {
                if (sibling.chatStyle.chatClickEvent != null && sibling.chatStyle.chatClickEvent.value.contains("YES")) {
                    lastChatPromptTime = SimpleTimeMark.now()
                    lastChatPrompt = sibling.chatStyle.chatClickEvent.value.substringAfter(" ")
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!config.trapperCooldownGui) return
        if (!onFarmingIsland()) return

        val cooldownMessage = if (timeUntilNextReady <= 0) "Trapper Ready"
        else if (timeUntilNextReady == 1) "1 second left"
        else "$timeUntilNextReady seconds left"

        config.trapperCooldownPos.renderString(
            "${currentStatus.colorCode}Trapper Cooldown: $cooldownMessage",
            posLabel = "Trapper Cooldown GUI",
        )
    }

    private fun updateTrapper() {
        timeUntilNextReady -= 1
        if (trapperReady && timeUntilNextReady > 0) {
            currentStatus = TrapperStatus.WAITING
            currentLabel = if (timeUntilNextReady == 1) "§31 second left" else "§3$timeUntilNextReady seconds left"
        }

        if (timeUntilNextReady <= 0 && trapperReady) {
            if (timeUntilNextReady == 0) {
                LorenzUtils.sendTitle("§2Trapper Ready", 3.seconds)
                SoundUtils.playBeepSound()
            }
            currentStatus = TrapperStatus.READY
            currentLabel = "§2Ready"
        }

        var found = false
        var active = false
        val previousLocation = TrevorSolver.mobLocation
        // TODO work wioth trapper widget, widget api, repo patterns, when not found, warn in chat and dont update
        for (line in TabListData.getTabList()) {
            val formattedLine = line.removeColor().drop(1)
            if (formattedLine.startsWith("Time Left: ")) {
                trapperReady = false
                currentStatus = TrapperStatus.ACTIVE
                currentLabel = "§cActive Quest"
                active = true
            }

            TrapperMobArea.entries.firstOrNull { it.location == formattedLine }?.let {
                TrevorSolver.mobLocation = it
                found = true
            }
            locationPattern.matchMatcher(formattedLine) {
                val zone = group("zone")
                TrevorSolver.mobLocation = TrapperMobArea.entries.firstOrNull { it.location == zone } ?: TrapperMobArea.NONE
                found = true
            }
        }
        if (!found) TrevorSolver.mobLocation = TrapperMobArea.NONE
        if (!active) {
            trapperReady = true
        } else {
            inBetweenQuests = true
        }
        if (TrevorSolver.mobCoordinates != LorenzVec(0.0, 0.0, 0.0) && active) {
            TrevorSolver.mobLocation = previousLocation
        }
        questActive = active
    }

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        if (!onFarmingIsland()) return
        var entityTrapper = EntityUtils.getEntityByID(trapperID)
        if (entityTrapper !is EntityLivingBase) entityTrapper = EntityUtils.getEntityByID(backupTrapperID)
        if (entityTrapper is EntityLivingBase && config.trapperTalkCooldown) {
            RenderLivingEntityHelper.setEntityColorWithNoHurtTime(entityTrapper, currentStatus.color) { config.trapperTalkCooldown }
            entityTrapper.getLorenzVec().let {
                if (it.distanceToPlayer() < 15) {
                    event.drawString(it.add(y = 2.23), currentLabel)
                }
            }
        }

        if (config.trapperSolver) {
            var location = TrevorSolver.mobLocation.coordinates
            if (TrevorSolver.mobLocation == TrapperMobArea.NONE) return
            if (TrevorSolver.averageHeight != 0.0) {
                location = LorenzVec(location.x, TrevorSolver.averageHeight, location.z)
            }
            if (TrevorSolver.mobLocation == TrapperMobArea.FOUND) {
                val displayName = if (TrevorSolver.currentMob == null) "Mob Location" else {
                    TrevorSolver.currentMob!!.mobName
                }
                location = TrevorSolver.mobCoordinates
                event.drawWaypointFilled(location.add(y = -2), LorenzColor.GREEN.toColor(), true, true)
                event.drawDynamicText(location.add(y = 1), displayName, 1.5)
            } else {
                event.drawWaypointFilled(location, LorenzColor.GOLD.toColor(), true, true)
                event.drawDynamicText(location.add(y = 1), TrevorSolver.mobLocation.location, 1.5)
            }
        }
    }

    @SubscribeEvent
    fun onKeyClick(event: LorenzKeyPressEvent) {
        if (!onFarmingIsland()) return
        if (Minecraft.getMinecraft().currentScreen != null) return
        if (NEUItems.neuHasFocus()) return

        if (event.keyCode != config.keyBindWarpTrapper) return

        if (config.acceptQuest) {
            val timeSince = lastChatPromptTime.passedSince()
            if (timeSince > 200.milliseconds && timeSince < 5.seconds) {
                lastChatPromptTime = SimpleTimeMark.farPast()
                HypixelCommands.chatPrompt(lastChatPrompt)
                lastChatPrompt = ""
                timeLastWarped = SimpleTimeMark.now()
                return
            }
        }

        if (config.warpToTrapper && timeLastWarped.passedSince() > 3.seconds && teleportBlock.passedSince() > 5.seconds) {
            HypixelCommands.warp("trapper")
            timeLastWarped = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!inTrapperDen) return
        if (!config.trapperTalkCooldown) return
        val entity = event.entity
        if (entity is EntityArmorStand && entity.name == "§e§lCLICK") {
            event.cancel()
        }
    }

    private fun resetTrapper() {
        TrevorSolver.resetLocation()
        currentStatus = TrapperStatus.READY
        currentLabel = "§2Ready"
        questActive = false
        inBetweenQuests = false
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        resetTrapper()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        inTrapperDen = areaTrappersDenPattern.matches(LorenzUtils.skyBlockArea)
    }

    enum class TrapperStatus(baseColor: LorenzColor) {
        READY(LorenzColor.DARK_GREEN),
        WAITING(LorenzColor.DARK_AQUA),
        ACTIVE(LorenzColor.DARK_RED),
        ;

        val color = baseColor.toColor().withAlpha(75)
        val colorCode = baseColor.getChatColor()
    }

    fun onFarmingIsland() = IslandType.THE_FARMING_ISLANDS.isInIsland()

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.transform(11, "misc.trevorTheTrapper.textFormat") { element ->
            ConfigUtils.migrateIntArrayListToEnumArrayList(element, TrackerEntry::class.java)
        }
    }
}
