package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage.EnchantedClockStats.ClockBoostStatus.ClockBoostState
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.EnchantedClockJson
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.misc.EnchantedClockHelper.ClockBoostType.Companion.filterStatusSlots
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@SkyHanniModule
object EnchantedClockHelper {

    private val patternGroup = RepoPattern.group("misc.eclock")
    private val storage get() = ProfileStorageData.profileSpecific?.enchantedClockStats
    private val config get() = SkyHanniMod.feature.misc

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: Enchanted Time Clock
     */
    private val enchantedClockPattern by patternGroup.pattern(
        "inventory.name",
        "Enchanted Time Clock",
    )

    /**
     * REGEX-TEST: §6§lTIME WARP! §r§aYou have successfully warped time for your Chocolate Factory!
     * REGEX-TEST: §6§lTIME WARP! §r§aYou have successfully warped time for your minions!
     * REGEX-TEST: §6§lTIME WARP! §r§aYou have successfully warped time for your forges!
     * REGEX-TEST: §6§lTIME WARP! §r§aYou have successfully warped time for your aging items!
     * REGEX-TEST: §6§lTIME WARP! §r§aYou have successfully warped time for your training pets!
     * REGEX-TEST: §6§lTIME WARP! §r§aYou have successfully warped time for your pets being taken care of by Kat!
     */
    private val boostUsedChatPattern by patternGroup.pattern(
        "chat.boostused",
        "§6§lTIME WARP! §r§aYou have successfully warped time for your (?<usagestring>.+?)!",
    )

    /**
     * REGEX-TEST: §7Status: §c§lCHARGING
     * REGEX-TEST: §7Status: §e§lPROBLEM
     * REGEX-TEST: §7Status: §a§lREADY
     */
    private val statusLorePattern by patternGroup.pattern(
        "inventory.status",
        "§7Status: §(?<color>[a-f])§l(?<status>.+)",
    )

    /**
     * REGEX-TEST: §8Cooldown: §a48 hours
     * REGEX-TEST: §8Cooldown: §a47 hours
     */
    private val cooldownLorePattern by patternGroup.pattern(
        "inventory.cooldown",
        "§8Cooldown: §.(?<hours>\\d+) hours",
    )
    // </editor-fold>

    enum class SimpleClockBoostType(
        private val displayString: String
    ) {
        MINIONS("§bMinions"),
        CHOCOLATE_FACTORY("§6Chocolate Factory"),
        PET_TRAINING("§dPet Training"),
        PET_SITTER("§bPet Sitter"),
        AGING_ITEMS("§eAging Items"),
        FORGE("§6Forge"),
        ;

        override fun toString(): String = displayString
    }

    data class ClockBoostType(
        val name: String,
        val displayName: String,
        val usageString: String,
        val color: LorenzColor,
        val displaySlot: Int,
        val statusSlot: Int,
        val cooldown: Duration = 48.hours
    ) {
        val formattedName: String = "§${color.chatColorCode}$displayName"

        fun getCooldownFromNow() = SimpleTimeMark.now() + cooldown

        fun toSimple(): SimpleClockBoostType? {
            return try {
                SimpleClockBoostType.valueOf(name.uppercase())
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        companion object {
            private val entries = mutableListOf<ClockBoostType>()

            fun clear() = entries.clear()

            fun populateFromJson(json: EnchantedClockJson) {
                entries.clear()
                entries.addAll(json.boosts.map { boost ->
                    ClockBoostType(
                        name = boost.name,
                        displayName = boost.displayName,
                        usageString = boost.usageString ?: boost.displayName,
                        color = LorenzColor.valueOf(boost.color),
                        displaySlot = boost.displaySlot,
                        statusSlot = boost.statusSlot,
                        cooldown = boost.cooldownHours.hours
                    )
                })
            }

            fun byUsageStringOrNull(usageString: String) = entries.firstOrNull { it.usageString == usageString }

            fun byItemStackOrNull(stack: ItemStack) = entries.firstOrNull { it.formattedName == stack.displayName }

            fun Map<Int, ItemStack>.filterStatusSlots() = filterKeys { key ->
                ClockBoostType.entries.any { entry ->
                    entry.statusSlot == key
                }
            }
        }
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        val storage = storage ?: return

        val readyNowBoosts: MutableList<ClockBoostType> = mutableListOf()

        for ((boostType, status) in storage.clockBoosts) {
            val simpleType = boostType.toSimple() ?: continue
            if (!config.enchantedClockReminder.contains(simpleType)) continue
            if (status.state != ClockBoostState.CHARGING) continue
            if (status.availableAt == null || status.availableAt?.isInFuture() == true) continue

            status.state = ClockBoostState.READY
            status.availableAt = null
            readyNowBoosts.add(boostType)
        }

        if (readyNowBoosts.isEmpty()) return
        val boostList = readyNowBoosts.joinToString(", ") { it.formattedName }
        val starter = if (readyNowBoosts.size == 1) "boost is ready" else "boosts are ready"
        ChatUtils.chat("§6§lTIME WARP! §r§aThe following $starter:\n$boostList")
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<EnchantedClockJson>("EnchantedClock")
        ClockBoostType.clear()
        ClockBoostType.populateFromJson(data)
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        boostUsedChatPattern.matchMatcher(event.message) {
            val usageString = group("usagestring") ?: return@matchMatcher
            val boostType = ClockBoostType.byUsageStringOrNull(usageString) ?: return@matchMatcher
            val storage = storage ?: return@matchMatcher
            storage.clockBoosts.getOrPut(boostType) {
                ProfileSpecificStorage.EnchantedClockStats.ClockBoostStatus(
                    ClockBoostState.CHARGING,
                    boostType.getCooldownFromNow()
                )
            }
        }
    }

    @SubscribeEvent
    fun onInventoryUpdatedEvent(event: InventoryUpdatedEvent) {
        if (!enchantedClockPattern.matches(event.inventoryName)) return
        val storage = storage ?: return

        val statusStacks = event.inventoryItems.filterStatusSlots()
        for ((_, stack) in statusStacks) {
            val boostType = ClockBoostType.byItemStackOrNull(stack) ?: continue

            val currentStatus: ClockBoostState = statusLorePattern.firstMatcher(stack.getLore()) {
                group("status")?.let { statusStr ->
                    runCatching { ClockBoostState.valueOf(statusStr) }.getOrElse {
                        ErrorManager.skyHanniError("Invalid status string: $statusStr"); null
                    }
                }
            } ?: continue

            val parsedCooldown: SimpleTimeMark? = cooldownLorePattern.firstMatcher(stack.getLore()) {
                group("hours")?.toIntOrNull()?.hours?.let { SimpleTimeMark.now() + it }
            }

            // Because the times provided by the clock UI suck ass (we only get hour count)
            //  We only want to set it if the current time is horribly incorrect.
            storage.clockBoosts[boostType]?.availableAt?.let { existing ->
                parsedCooldown?.let { parsed ->
                    if (existing.absoluteDifference(parsed) < 2.hours) return
                }
            }

            storage.clockBoosts.getOrPut(boostType) {
                ProfileSpecificStorage.EnchantedClockStats.ClockBoostStatus(
                    currentStatus,
                    parsedCooldown
                )
            }
        }
    }
}
