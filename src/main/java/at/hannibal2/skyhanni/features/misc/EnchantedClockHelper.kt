package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.jsonobjects.repo.EnchantedClockJson
import at.hannibal2.skyhanni.events.InventoryOpenEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.misc.EnchantedClockHelper.BoostType.Companion.filterStatusSlots
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object EnchantedClockHelper {

    private val patternGroup = RepoPattern.group("misc.eclock")
    private val storage get() = ProfileStorageData.profileSpecific?.enchantedClockBoosts
    private val config get() = SkyHanniMod.feature.misc.enchantedClock

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: Enchanted Time Clock
     */
    val enchantedClockPattern by patternGroup.pattern(
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
        "boost.status",
        "§7Status: §(?<color>[a-f])§l(?<status>.+)",
    )

    /**
     * REGEX-TEST: §7§cOn cooldown: 20 hours
     * REGEX-TEST: §7§cOn cooldown: 41 minutes
     * REGEX-TEST: §7§cOn cooldown: 0 minutes
     */
    private val cooldownLorePattern by patternGroup.pattern(
        "boost.cooldown",
        "(?:§.)*On cooldown: (?<count>[\\d,]+) (?<type>[A-Za-z]+?)s?\\b",
    )
    // </editor-fold>

    enum class SimpleBoostType(private val displayString: String) {
        MINIONS("§bMinions"),
        CHOCOLATE_FACTORY("§6Chocolate Factory"),
        PET_TRAINING("§dPet Training"),
        PET_SITTER("§bPet Sitter"),
        AGING_ITEMS("§eAging Items"),
        FORGE("§6Forge"),
        ;

        override fun toString(): String = displayString
    }

    data class BoostType(
        val name: String,
        val displayName: String,
        val usageString: String,
        val color: LorenzColor,
        val displaySlot: Int,
        val statusSlot: Int,
        val cooldown: Duration = 48.hours,
        val formattedName: String = "§${color.chatColorCode}$displayName",
    ) {
        fun getCooldownFromNow() = SimpleTimeMark.now() + cooldown
        fun toSimple(): SimpleBoostType? = SimpleBoostType.entries.find { it.name == name }

        companion object {
            private var entries = listOf<BoostType>()

            fun byUsageStringOrNull(usageString: String) = entries.firstOrNull { it.usageString == usageString }
            fun byItemStackOrNull(stack: ItemStack) = entries.firstOrNull { it.formattedName == stack.displayName }
            fun bySimpleBoostType(simple: SimpleBoostType) = entries.firstOrNull { it.name == simple.name }

            fun populateFromJson(json: EnchantedClockJson) {
                entries = json.boosts.map {
                    BoostType(
                        name = it.name,
                        displayName = it.displayName,
                        usageString = it.usageString ?: it.displayName,
                        color = LorenzColor.valueOf(it.color),
                        displaySlot = it.displaySlot,
                        statusSlot = it.statusSlot,
                        cooldown = (it.cooldownHours.takeIf { cdh -> cdh > 0 } ?: 48).hours,
                    )
                }
            }

            fun Map<Int, ItemStack>.filterStatusSlots() = filterKeys { key ->
                BoostType.entries.any { entry ->
                    entry.statusSlot == key
                }
            }
        }
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        val readyNowBoosts = loadBoostsReadyNow().takeIf { it.isNotEmpty() } ?: return

        val boostListFormat = readyNowBoosts.joinToString(", ") { it.formattedName }
        val preamble = if (readyNowBoosts.size == 1) "boost is ready" else "boosts are ready"
        ChatUtils.chat("§6§lTIME WARP! §r§aThe following $preamble:\n$boostListFormat")
        SoundUtils.playPlingSound()

        // Set up repeating reminder if enabled in config
        config.repeatReminder.takeIf { it > 0 }?.let { interval ->
            val simpleBoostsReadyNow = readyNowBoosts.mapNotNull { it.toSimple() }
            DelayedRun.runDelayed(interval.minutes) {
                storage?.filterKeys { it in simpleBoostsReadyNow }?.values?.forEach { it.warned = false }
            }
        }
    }

    private fun loadBoostsReadyNow(): List<BoostType> {
        val storage = EnchantedClockHelper.storage ?: return emptyList()

        val readyNowBoosts: MutableList<BoostType> = mutableListOf()

        for ((type, status) in storage.filter { !it.value.warned }) {
            val inConfig = config.reminderBoosts.contains(type)
            val isProperState = status.state == State.CHARGING
            val inPast = status.availableAt?.isInPast() ?: false
            if (!inConfig || !isProperState || !inPast) continue

            val complexType = BoostType.bySimpleBoostType(type) ?: continue

            status.state = State.READY
            status.availableAt = null
            status.warned = true
            readyNowBoosts.add(complexType)
        }
        return readyNowBoosts
    }

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<EnchantedClockJson>("misc/EnchantedClock")
        BoostType.populateFromJson(data)
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        val usageString = boostUsedChatPattern.matchMatcher(event.message) { group("usagestring") } ?: return
        val boostType = BoostType.byUsageStringOrNull(usageString) ?: return
        val simpleType = boostType.toSimple() ?: return
        val storage = storage ?: return
        storage[simpleType] = Status(State.CHARGING, boostType.getCooldownFromNow(), exactTime = true)
    }

    private fun ItemStack.getTypePair(): Pair<BoostType?, SimpleBoostType?> {
        val boostType = BoostType.byItemStackOrNull(this) ?: return null to null
        val simpleType = boostType.toSimple() ?: return null to null
        return boostType to simpleType
    }

    private fun ItemStack.getBoostState(): State? = statusLorePattern.firstMatcher(getLore()) {
        group("status")?.let { statusStr ->
            runCatching { State.valueOf(statusStr) }.getOrElse {
                ErrorManager.skyHanniError("Invalid status string: $statusStr")
            }
        }
    }

    @HandleEvent
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (!enchantedClockPattern.matches(event.inventoryName)) return
        val storage = storage ?: return

        val statusStacks = event.inventoryItems.filterStatusSlots()
        for ((_, stack) in statusStacks) {
            val (boostType, simpleType) = stack.getTypePair()
            val currentBoostState = stack.getBoostState()
            val timeAlreadyExact = storage[simpleType]?.exactTime == true
            if (boostType == null || simpleType == null || currentBoostState == null || timeAlreadyExact) continue

            var exactUpdate = false
            val parsedCooldown: SimpleTimeMark? = when (currentBoostState) {
                State.READY, State.PROBLEM -> {
                    storage[simpleType]?.availableAt = SimpleTimeMark.now()
                    continue
                }

                else -> cooldownLorePattern.firstMatcher(stack.getLore()) {
                    val count = group("count").toInt()
                    val type = group("type")
                    if (type == "minute") exactUpdate = true
                    SimpleTimeMark.now() + when (type) {
                        "hour" -> count.hours
                        "minute" -> count.minutes
                        else -> 0.seconds
                    }
                }
            }

            storage[simpleType] = Status(currentBoostState, parsedCooldown, exactTime = exactUpdate)
        }
    }

    class Status(
        @Expose var state: State,
        @Expose var availableAt: SimpleTimeMark?,
        @Expose var exactTime: Boolean = false,
        @Expose var warned: Boolean = false,
    ) {
        override fun toString(): String = "Status(state=$state, availableAt=$availableAt, warned=$warned)"
    }

    enum class State(val displayName: String, val color: LorenzColor) {
        READY("Ready", LorenzColor.GREEN),
        CHARGING("Charging", LorenzColor.RED),
        PROBLEM("Problem", LorenzColor.YELLOW),
        ;

        override fun toString(): String = "§" + color.chatColorCode + displayName
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.reminder
}
