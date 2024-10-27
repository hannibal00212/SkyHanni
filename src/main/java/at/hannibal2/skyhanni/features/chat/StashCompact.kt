package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatIntOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object StashCompact {

    // <editor-fold desc="Patterns">
    private val patternGroup = RepoPattern.group("stash.compact")

    /**
     * REGEX-TEST: §f                 §7You have §3226 §7materials stashed away!
     * REGEX-TEST: §f                 §7You have §31,000 §7items stashed away!
     * REGEX-TEST: §f                     §7You have §a2 §7items stashed away!
     */
    private val materialCountPattern by patternGroup.pattern(
        "material.count",
        "§f *§7You have §.(?<count>[\\d,]+) (?:§.)+(?<type>item|material)s? stashed away!.*",
    )

    /**
     * REGEX-TEST: §f               §8(This totals 1 type of material stashed!)
     * REGEX-TEST: §f               §8(This totals 2 types of items stashed!)
     * REGEX-TEST: §f               §8(This totals 3 types of materials stashed!)
     * REGEX-TEST: §f               §8(This totals 4 types of items stashed!)
     */
    private val differingMaterialsCountPattern by patternGroup.pattern(
        "differing.materials.count",
        "§f *§8\\(This totals (?<count>[\\d,]+) types? of (?<type>item|material)s? stashed!\\).*",
    )

    /**
     * REGEX-TEST: §f                §3§l>>> §3§lCLICK HERE§b to pick them up! §3§l<<<
     * REGEX-TEST: §f                §6§l>>> §6§lCLICK HERE§e to pick them up! §6§l<<<
     */
    private val pickupStashPattern by patternGroup.pattern(
        "pickup.stash",
        "§f *§.§l>>> §.§lCLICK HERE§. to pick (?:them|it) up! §.§l<<<.*",
    )

    /**
     * REGEX-TEST: §eOne or more items didn't fit in your inventory and were added to your item stash! §6Click here to pick them up!
     * REGEX-TEST: §eOne or more materials didn't fit in your inventory and were added to your material stash! §6Click here to pick them up!
     */
    @Suppress("MaxLineLength")
    private val genericAddedToStashPattern by patternGroup.pattern(
        "generic",
        "§eOne or more (?:item|material)s? didn't fit in your inventory and were added to your (?:item|material) stash! §6Click here §eto pick them up!",
    )
    // </editor-fold>

    private val config get() = SkyHanniMod.feature.chat.filterType.stashMessages

    private var lastMaterialCount = -1
    private var lastDifferingMaterialsCount: Int? = null
    private var lastType = "error"

    private var lastSentMaterialCount = -1
    private var lastSentType = "error"

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return

        // TODO make a system for detecting message "groups" (multiple consecutive messages)
        materialCountPattern.matchMatcher(event.message) {
            group("count").formatIntOrNull()?.let { lastMaterialCount = it }
            lastType = group("type")
            event.blockedReason = "stash_compact"
        }

        differingMaterialsCountPattern.matchMatcher(event.message) {
            group("count").formatIntOrNull()?.let { lastDifferingMaterialsCount = it }
            event.blockedReason = "stash_compact"
        }

        if (pickupStashPattern.matches(event.message)) {
            event.blockedReason = "stash_compact"
            if (lastMaterialCount <= config.hideLowWarningsThreshold) return
            if (config.hideDuplicateCounts && lastMaterialCount == lastSentMaterialCount && lastType == lastSentType) return

            sendCompactedStashMessage()
        }

        if (!config.hideAddedMessages) return
        genericAddedToStashPattern.matchMatcher(event.message) {
            event.blockedReason = "stash_compact"
        }
    }

    private fun sendCompactedStashMessage() {
        val typeNameFormat = StringUtils.pluralize(lastMaterialCount, lastType)
        val (mainColor, accentColor) = if (lastType == "item") "§e" to "§6" else "§b" to "§3"
        val typeStringExtra = lastDifferingMaterialsCount?.let {
            ", ${mainColor}totalling $accentColor$it ${StringUtils.pluralize(it, "type")}$mainColor"
        }.orEmpty()
        val action = if (config.useViewStash) "view" else "pickup"

        ChatUtils.clickableChat(
            "${mainColor}You have $accentColor${lastMaterialCount.shortFormat()} ${mainColor}$typeNameFormat in stash$typeStringExtra. " +
                "${mainColor}Click to $accentColor$action ${mainColor}your stash!",
            onClick = {
                if (config.useViewStash) HypixelCommands.viewStash(lastType)
                else HypixelCommands.pickupStash()
            },
            hover = "§eClick to $action your $lastType stash!",
        )
        lastSentMaterialCount = lastMaterialCount
        lastSentType = lastType
        // Dirty, but item stash doesn't have differing materials count,
        // and we don't compare this value to the last one, so we can reset it here
        lastDifferingMaterialsCount = null
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
