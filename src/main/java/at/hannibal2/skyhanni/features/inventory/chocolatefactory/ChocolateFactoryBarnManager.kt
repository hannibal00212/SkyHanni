package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.event.hoppity.HoppityApi
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionData
import at.hannibal2.skyhanni.features.event.hoppity.HoppityCollectionStats
import at.hannibal2.skyhanni.features.event.hoppity.HoppityEggsManager
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TimeUtils.format
import net.minecraft.util.ChatComponentText

@SkyHanniModule
object ChocolateFactoryBarnManager {

    private val config get() = ChocolateFactoryApi.config
    private val hoppityChatConfig get() = HoppityEggsManager.config.chat
    private val profileStorage get() = ChocolateFactoryApi.profileStorage

    /**
     * REGEX-TEST: §c§lBARN FULL! §fOlivette §7got §ccrushed§7! §6+290,241 Chocolate
     */
    private val rabbitCrashedPattern by ChocolateFactoryApi.patternGroup.pattern(
        "rabbit.crushed",
        "§c§lBARN FULL! §f\\D+ §7got §ccrushed§7! §6\\+(?<amount>[\\d,]+) Chocolate",
    )

    var barnFull = false
    private var sentBarnFullWarning = false
    private var lastRabbit = ""

    fun processDataSet(dataSet: HoppityApi.HoppityStateDataSet) {
        lastRabbit = dataSet.lastName
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onChat(event: SkyHanniChatEvent) {
        HoppityEggsManager.newRabbitFound.matchMatcher(event.message) {
            val profileStorage = profileStorage ?: return@matchMatcher
            profileStorage.currentRabbits += 1
            trySendBarnFullMessage(inventory = false)
            HoppityEggsManager.shareWaypointPrompt()
        }

        HoppityEggsManager.duplicateRabbitFound.matchMatcher(event.message) {
            HoppityEggsManager.shareWaypointPrompt()
            val amount = group("amount").formatLong()
            if (config.showDuplicateTime && !hoppityChatConfig.compact) {
                val format = ChocolateFactoryApi.timeUntilNeed(amount).format(maxUnits = 2)
                DelayedRun.runNextTick {
                    ChatUtils.chat("§7(§a+§b$format §aof production§7)")
                }
            }
            ChocolateAmount.addToAll(amount)
            HoppityApi.attemptFireRabbitFound(event, lastDuplicateAmount = amount)

            var changedMessage = event.message

            if (hoppityChatConfig.showDuplicateNumber && !hoppityChatConfig.compact) {
                // Add duplicate number to the duplicate rabbit message
                (HoppityCollectionStats.getRabbitCount(lastRabbit)).takeIf { it > 0 }?.let {
                    changedMessage = changedMessage.replace(
                        "§7§lDUPLICATE RABBIT!",
                        "§7§lDUPLICATE RABBIT! §7(Duplicate §b#$it§7)§r"
                    )
                }
            }

            if (hoppityChatConfig.recolorTTChocolate && ChocolateFactoryTimeTowerManager.timeTowerActive()) {
                // Replace §6\+(?<amount>[\d,]+) Chocolate with §6\+§d(?<amount>[\d,]+) §6Chocolate
                changedMessage = changedMessage.replace(
                    "§6\\+(?<amount>[\\d,]+) Chocolate",
                    "§6\\+§d${group("amount")} §6Chocolate"
                )
            }

            if (event.message != changedMessage) event.chatComponent = ChatComponentText(changedMessage)
        }

        rabbitCrashedPattern.matchMatcher(event.message) {
            HoppityEggsManager.shareWaypointPrompt()
            ChocolateAmount.addToAll(group("amount").formatLong())
        }
    }

    @HandleEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        sentBarnFullWarning = false
    }

    fun trySendBarnFullMessage(inventory: Boolean) {
        if (!ChocolateFactoryApi.isEnabled()) return

        if (config.barnCapacityThreshold <= 0) {
            return
        }

        val profileStorage = profileStorage ?: return

        // TODO rename maxRabbits to maxUnlockedBarnSpace
        if (profileStorage.maxRabbits >= ChocolateFactoryApi.maxRabbits) return

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

        if (config.rabbitCrushOnlyDuringHoppity && !HoppityApi.isHoppityEvent()) return

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
