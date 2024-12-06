package at.hannibal2.skyhanni.features.inventory.chocolatefactory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.EntityMovementData
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryUpdatedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.MessageSendToServerEvent
import at.hannibal2.skyhanni.events.TablistFooterUpdateEvent
import at.hannibal2.skyhanni.features.event.hoppity.MythicRabbitPetWarning
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ChocolateFactoryBlockOpen {
    private val config get() = SkyHanniMod.feature.inventory.chocolateFactory
    private val profileStorage get() = ProfileStorageData.profileSpecific

    /**
     * REGEX-TEST: /cf
     * REGEX-TEST: /cf test
     * REGEX-TEST: /chocolatefactory
     * REGEX-TEST: /chocolatefactory123456789
     * REGEX-TEST: /factory
     */
    private val commandPattern by RepoPattern.pattern(
        "inventory.chocolatefactory.opencommand",
        "\\/(?:cf|(?:chocolate)?factory)(?: .*)?",
    )

    /**
     * REGEX-TEST: §6Chocolate Factory
     * REGEX-TEST: §6Open Chocolate Factory
     */
    private val openCfItemPattern by RepoPattern.pattern(
        "inventory.chocolatefactory.openitem",
        "§6(?:Open )?Chocolate Factory",
    )

    /**
     * REGEX-TEST: §a§lSCHLURP! §r§eThe effects of the §r§9Hot Chocolate Mixin §r§ehave been extended by §r§986h 24m§r§e!
     * They will pause if your §r§cGod Potion §r§eexpires.
     */
    private val hotChocolateMixinConsumePattern by RepoPattern.pattern(
        "stats.chatpatterns.hotchocolatemixinconsume",
        "(?:§.)+.*(?:§.)+Hot Chocolate Mixin ?(?:§.)+.*extended by (?:§.)+(?<time>[dhms0-9 ]*)(?:§.)+!.*",
    )

    /**
     * REGEX-TEST: §a§lGULP! §r§eThe §r§cGod Potion §r§egrants you powers for §r§928h 48m§r§e!
     * REGEX-TEST: §a§lSIP! §r§eThe §r§cGod Potion §r§egrants you powers for §r§928h 48m§r§e!
     * REGEX-TEST: §a§lSLURP! §r§eThe §r§cGod Potion §r§egrants you powers for §r§928h 48m§r§e!
     */
    private val godPotConsumePattern by RepoPattern.pattern(
        "stats.chatpatterns.godpotconsume",
        "(?:§.)+.*(?:§.)+God Potion ?(?:§.)+.*grants you powers for (?:§.)+(?<time>[dhms0-9 ]*)(?:§.)+!.*",
    )

    /**
     * REGEX-TEST: §cGod Potion§f: 4d
     */
    private val godPotTabPattern by RepoPattern.pattern(
        "stats.tabpatterns.godpot",
        "(?:§.)*God Potion(?:§.)*: (?:§.)*(?<time>[dhms0-9 ]+)(?:§.)*",
    )

    /**
     * REGEX-TEST: (1/2) Active Effects
     */
    private val effectsInventoryPattern by RepoPattern.pattern(
        "inventory.effects",
        "(?:§.)?(?:[(\\d\\/)]* )?Active Effects",
    )

    /**
     * REGEX-TEST: §aFilter
     */
    private val filterPattern by RepoPattern.pattern(
        "inventory.effects.filter",
        "§aFilter",
    )

    /**
     * REGEX-TEST: §b▶ God Potion Effects
     */
    private val godPotEffectsFilterSelectPattern by RepoPattern.pattern(
        "inventory.effects.filtergodpotselect",
        "§b▶ God Potion Effects",
    )

    /**
     * REGEX-TEST: §7Remaining: §f105:01:34
     */
    private val potionRemainingLoreTimerPattern by RepoPattern.pattern(
        "inventory.effects.effecttimeleft",
        "§7Remaining: §f(?<time>[\\d:]+)",
    )

    private var commandSentTimer = SimpleTimeMark.farPast()

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val slotDisplayName = event.slot?.stack?.displayName ?: return
        if (!openCfItemPattern.matches(slotDisplayName)) return

        if (checkIsBlocked()) event.cancel()
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        hotChocolateMixinConsumePattern.matchMatcher(event.message) {
            val durationAdded = TimeUtils.getDuration(group("time"))
            val asTimeMark = SimpleTimeMark.now().plus(durationAdded)
            val existingValue = profileStorage?.chocolateFactory?.hotChocolateMixinExpiry
            profileStorage?.chocolateFactory?.hotChocolateMixinExpiry =
                existingValue?.let { it + durationAdded } ?: asTimeMark
        }
        godPotConsumePattern.matchMatcher(event.message) {
            val durationAdded = TimeUtils.getDuration(group("time"))
            val asTimeMark = SimpleTimeMark.now().plus(durationAdded)
            val existingValue = profileStorage?.godPotExpiryTime
            profileStorage?.godPotExpiryTime = existingValue?.let { it + durationAdded } ?: asTimeMark
        }
    }

    @SubscribeEvent
    fun onTabUpdate(event: TablistFooterUpdateEvent) {
        if (!LorenzUtils.inSkyBlock) return
        for (line in event.footer.split("\n")) {
            godPotTabPattern.matchMatcher(line) {
                val expiryDuration = TimeUtils.getDuration(group("time"))
                val expiryTime = SimpleTimeMark.now().plus(expiryDuration)
                profileStorage?.godPotExpiryTime = expiryTime
            }
        }
    }

    @SubscribeEvent
    fun onInventoryUpdated(event: InventoryUpdatedEvent) {
        if (!LorenzUtils.inSkyBlock || !event.isGodPotEffectsFilterSelect()) return

        val potionLore = event.inventoryItems[10]?.getLore() ?: run {
            // No active god pot effects found, reset the expiry time
            profileStorage?.godPotExpiryTime = SimpleTimeMark.farPast()
            return
        }

        val expiryDuration =potionRemainingLoreTimerPattern.firstMatcher(potionLore) {
            TimeUtils.getDuration(group("time"))
        } ?: return

        profileStorage?.godPotExpiryTime = SimpleTimeMark.now().plus(expiryDuration)
    }

    private fun InventoryUpdatedEvent.isGodPotEffectsFilterSelect(): Boolean =
        effectsInventoryPattern.matches(this.inventoryName) &&
            this.inventoryItems.values.firstOrNull {
                filterPattern.matches(it.displayName)
            }?.getLore()?.any {
                godPotEffectsFilterSelectPattern.matches(it)
            } ?: false

    @SubscribeEvent
    fun onCommandSend(event: MessageSendToServerEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!commandPattern.matches(event.message)) return
        if (commandSentTimer.passedSince() < 5.seconds) return
        if (LorenzUtils.isBingoProfile) return

        if (checkIsBlocked()) {
            commandSentTimer = SimpleTimeMark.now()
            event.cancel()
        }
    }

    private fun checkIsBlocked() = tryBlock() != TryBlockResult.SUCCESS

    private enum class TryBlockResult {
        SUCCESS,
        FAIL_NO_RABBIT,
        FAIL_NO_BOOSTER_COOKIE,
        FAIL_NO_MIXIN,
    }

    private fun tryBlock(): TryBlockResult {
        return if (config.mythicRabbitRequirement && !MythicRabbitPetWarning.correctPet()) {
            ChatUtils.clickToActionOrDisable(
                "§cBlocked opening the Chocolate Factory without a §dMythic Rabbit Pet §cequipped!",
                config::mythicRabbitRequirement,
                actionName = "open pets menu",
                action = { HypixelCommands.pet() },
            )
            TryBlockResult.FAIL_NO_RABBIT
        } else if (config.boosterCookieRequirement) {
            profileStorage?.bits?.boosterCookieExpiryTime?.let {
                if (it.timeUntil() > 0.seconds) return TryBlockResult.SUCCESS
                ChatUtils.clickToActionOrDisable(
                    "§cBlocked opening the Chocolate Factory without a §dBooster Cookie §cactive!",
                    config::boosterCookieRequirement,
                    actionName = "warp to hub",
                    action = {
                        HypixelCommands.warp("hub")
                        EntityMovementData.onNextTeleport(IslandType.HUB) {
                            IslandGraphs.pathFind(LorenzVec(-32.5, 71.0, -76.5), "§aBazaar", condition = { true })
                        }
                    },
                )
                TryBlockResult.FAIL_NO_BOOSTER_COOKIE
            } ?: TryBlockResult.SUCCESS
        } else if (config.hotChocolateMixinRequirement) {
            val mixinExpiryTime = profileStorage?.chocolateFactory?.hotChocolateMixinExpiry ?: SimpleTimeMark.farPast()
            val godPotExpiryTime = profileStorage?.godPotExpiryTime ?: SimpleTimeMark.farPast()
            if (mixinExpiryTime.isInPast()) {
                ChatUtils.clickToActionOrDisable(
                    "§cBlocked opening the Chocolate Factory without a §dHot Chocolate Mix §cactive! " +
                        "§7You may need to open §c/effects §7to refresh mixin status.",
                    config::hotChocolateMixinRequirement,
                    actionName = "search AH for mixin",
                    action = { HypixelCommands.auctionSearch("hot chocolate mixin") },
                )
                TryBlockResult.FAIL_NO_MIXIN
            } else if (godPotExpiryTime.isInPast()) {
                ChatUtils.clickToActionOrDisable(
                    "§cBlocked opening the Chocolate Factory without a §dGod Potion §cactive! " +
                        "§7You may need to open §c/effects §7and cycle the §aFilter §7to §bGod Potion Effects §7to refresh potion status.",
                    config::hotChocolateMixinRequirement,
                    actionName = "search AH for god potion",
                    action = { HypixelCommands.auctionSearch("god potion") },
                )
                TryBlockResult.FAIL_NO_MIXIN
            } else TryBlockResult.SUCCESS
        } else TryBlockResult.SUCCESS
    }
}
