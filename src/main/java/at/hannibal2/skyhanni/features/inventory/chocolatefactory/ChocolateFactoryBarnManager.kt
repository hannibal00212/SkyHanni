package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityAPI
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionData
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggsManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ChocolateFactoryBarnManager {

    private val config get() = ChocolateFactoryAPI.config
    private val hoppityConfig get() = HoppityEggsManager.config
    private val profileStorage get() = ChocolateFactoryAPI.profileStorage

    /**
     * REGEX-TEST: §c§lBARN FULL! §fOlivette §7got §ccrushed§7! §6+290,241 Chocolate
     */
    private val rabbitCrashedPattern by ChocolateFactoryAPI.patternGroup.pattern(
        "rabbit.crushed",
        "§c§lBARN FULL! §f\\D+ §7got §ccrushed§7! §6\\+(?<amount>[\\d,]+) Chocolate",
    )

    var barnFull = false
    private var sentBarnFullWarning = false
    private var lastRabbit = ""

    fun processDataSet(dataSet: HoppityAPI.HoppityStateDataSet) {
        lastRabbit = dataSet.lastName
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return

        HoppityEggsManager.newRabbitFound.matchMatcher(event.message) {
            val profileStorage = profileStorage ?: return@matchMatcher
            profileStorage.currentRabbits += 1
            trySendBarnFullMessage(inventory = false)
            HoppityEggsManager.shareWaypointPrompt()
        }

        HoppityEggsManager.duplicateRabbitFound.matchMatcher(event.message) {
            HoppityEggsManager.shareWaypointPrompt()
            val amount = group("amount").formatLong()
            if (config.showDuplicateTime && !hoppityConfig.compactChat) {
                val format = ChocolateFactoryAPI.timeUntilNeed(amount).format(maxUnits = 2)
                DelayedRun.runNextTick {
                    ChatUtils.chat("§7(§a+§b$format §aof production§7)")
                }
            }
            ChocolateAmount.addToAll(amount)
            HoppityAPI.attemptFireRabbitFound(event, lastDuplicateAmount = amount)

            if (hoppityConfig.showDuplicateNumber && !hoppityConfig.compactChat) {
                (HoppityCollectionStats.getRabbitCount(lastRabbit)).takeIf { it > 0 }?.let {
                    event.chatComponent = ChatComponentText(
                        event.message.replace("§7§lDUPLICATE RABBIT!", "§7§lDUPLICATE RABBIT! §7(Duplicate §b#$it§7)§r"),
                    )
                }
            }
        }

        rabbitCrashedPattern.matchMatcher(event.message) {
            HoppityEggsManager.shareWaypointPrompt()
            ChocolateAmount.addToAll(group("amount").formatLong())
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        sentBarnFullWarning = false
    }

    fun trySendBarnFullMessage(inventory: Boolean) {
        if (!ChocolateFactoryAPI.isEnabled()) return

        if (config.barnCapacityThreshold <= 0) {
            return
        }

        val profileStorage = profileStorage ?: return

        // TODO rename maxRabbits to maxUnlockedBarnSpace
        if (profileStorage.maxRabbits >= ChocolateFactoryAPI.maxRabbits) return

        // when the unlocked barn space has already surpassed the total amount of rabbits
        val alreadyBigEnough = profileStorage.maxRabbits >= HoppityCollectionData.knownRabbitCount

        val remainingSpace = profileStorage.maxRabbits - profileStorage.currentRabbits
        barnFull = remainingSpace <= config.barnCapacityThreshold && !alreadyBigEnough
        if (!barnFull) return

        if (inventory && sentBarnFullWarning) return

        sentBarnFullWarning = true

        if (profileStorage.maxRabbits == -1) {
            ChatUtils.clickableChat(
                "Open your chocolate factory to see your barn's capacity status!",
                onClick = { HypixelCommands.chocolateFactory() },
                "§eClick to run /cf!",
            )
            return
        }

        if (config.rabbitCrushOnlyDuringHoppity && !HoppityAPI.isHoppityEvent()) return

        val fullLevel = if (profileStorage.currentRabbits == profileStorage.maxRabbits) "full" else "almost full"
        ChatUtils.clickableChat(
            "§cYour barn is $fullLevel §7(${barnStatus()}). §cUpgrade it so they don't get crushed!",
            onClick = { HypixelCommands.chocolateFactory() },
            "§eClick to run /cf!",
        )
        SoundUtils.playBeepSound()
    }

    fun barnStatus(): String {
        val profileStorage = profileStorage ?: return "Unknown"
        return "${profileStorage.currentRabbits}/${profileStorage.maxRabbits} Rabbits"
    }
}
