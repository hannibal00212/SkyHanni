package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.SkyHanniChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

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

    private var currentMessage: StashMessage? = null
    private var lastMessage: StashMessage? = null

    data class StashMessage(val materialCount: Int, val type: String) {
        var differingMaterialsCount: Int? = null
    }

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        if (!isEnabled()) return

        // TODO make a system for detecting message "groups" (multiple consecutive messages)
        materialCountPattern.matchMatcher(event.message) {
            currentMessage = StashMessage(group("count").formatInt(), group("type"))
            event.blockedReason = "stash_compact"
        }

        differingMaterialsCountPattern.matchMatcher(event.message) {
            currentMessage?.differingMaterialsCount = group("count").formatInt()
            event.blockedReason = "stash_compact"
        }

        if (pickupStashPattern.matches(event.message)) {
            event.blockedReason = "stash_compact"
            val current = currentMessage ?: return
            if (current.materialCount <= config.hideLowWarningsThreshold) return
            if (config.hideDuplicateCounts && current == lastMessage) return

            current.sendCompactedStashMessage()
        }

        if (!config.hideAddedMessages) return
        genericAddedToStashPattern.matchMatcher(event.message) {
            event.blockedReason = "stash_compact"
        }
    }

    private fun StashMessage.sendCompactedStashMessage() {
        val typeNameFormat = StringUtils.pluralize(materialCount, type)
        val (mainColor, accentColor) = if (type == "item") "§e" to "§6" else "§b" to "§3"
        val typeStringExtra = differingMaterialsCount?.let {
            ", ${mainColor}totalling $accentColor$it ${StringUtils.pluralize(it, "type")}$mainColor"
        }.orEmpty()
        val action = if (config.useViewStash) "view" else "pickup"

        ChatUtils.clickableChat(
            "${mainColor}You have $accentColor${materialCount.shortFormat()} $mainColor$typeNameFormat in stash$typeStringExtra. " +
                "${mainColor}Click to $accentColor$action ${mainColor}your stash!",
            onClick = {
                if (config.useViewStash) HypixelCommands.viewStash(type)
                else HypixelCommands.pickupStash()
            },
            hover = "§eClick to $action your $type stash!",
        )
        currentMessage = null
        lastMessage = this
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
