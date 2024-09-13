package at.hannibal2.skyhanni.features.fame

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.anyMatches
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object UpgradeReminder {
    private val config get() = SkyHanniMod.feature.misc

    private val patternGroup = RepoPattern.group("fame.upgrades")

    private val accountUpgradePattern by patternGroup.pattern(
        "account",
        "§8Account Upgrade",
    )
    private val profileUpgradePattern by patternGroup.pattern(
        "profile",
        "§8Profile Upgrade",
    )
    private val upgradeDurationPattern by patternGroup.pattern(
        "duration",
        "§8Duration: (?<duration>.+)",
    )
    private val upgradeStartedPattern by patternGroup.pattern(
        "started",
        "§eYou started the §r§a(?<upgrade>.+) §r§eupgrade!",
    )
    private val upgradeClaimedPattern by patternGroup.pattern(
        "claimed",
        "§eYou claimed the §r§a(?<upgrade>.+) §r§eupgrade!",
    )
    private val upgradePattern by patternGroup.pattern(
        "upgrade",
        "§eClick to start upgrade!",
    )

    private var currentProfileUpgrade: CommunityShopUpgrade?
        get() = ProfileStorageData.profileSpecific?.communityShopProfileUpgrade
        set(value) {
            ProfileStorageData.profileSpecific?.communityShopProfileUpgrade = value
        }

    private var currentAccountUpgrade: CommunityShopUpgrade?
        get() = ProfileStorageData.playerSpecific?.communityShopAccountUpgrade
        set(value) {
            ProfileStorageData.playerSpecific?.communityShopAccountUpgrade = value
        }

    private var inInventory = false
    private var clickedUpgradeType: UpgradeType? = null
    private var clickedUpgrade: CommunityShopUpgrade? = null
    private var lastReminderSend = SimpleTimeMark.farPast()

    // TODO: (for 0.27) merge this logic with reminder manager
    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (ReminderUtils.isBusy()) return
        if (inInventory || LorenzUtils.skyBlockArea == "Community Center") return
        if (lastReminderSend.passedSince() < 30.seconds) return

        currentProfileUpgrade?.sendReminderIfClaimable()
        currentAccountUpgrade?.sendReminderIfClaimable()

        lastReminderSend = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        inInventory = event.inventoryName == "Community Shop"
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inInventory = false
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!inInventory) return
        val item = event.item ?: return
        clickedUpgradeType = UpgradeType.fromItem(item) ?: return
        clickedUpgrade = CommunityShopUpgrade.fromItem(item) ?: return
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (upgradeStartedPattern.matches(event.message)) {
            clickedUpgrade?.start()
            when (clickedUpgradeType) {
                UpgradeType.PROFILE -> currentProfileUpgrade = clickedUpgrade
                UpgradeType.ACCOUNT -> currentAccountUpgrade = clickedUpgrade
                null -> {}
            }
            return
        }

        upgradeClaimedPattern.matchMatcher(event.message) {
            val claimedUpgradeName = group("upgrade")
            when (claimedUpgradeName) {
                currentProfileUpgrade?.name -> currentProfileUpgrade = null
                currentAccountUpgrade?.name -> currentAccountUpgrade = null
            }
        }
    }

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.accountUpgradeReminder

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(
            49,
            "#player.currentAccountUpgrade",
            "#player.communityShopAccountUpgrade.name",
        )

        event.move(
            49,
            "#player.nextAccountUpgradeCompletionTime",
            "#player.communityShopAccountUpgrade.completionTime",
        )
    }

    class CommunityShopUpgrade(
        @Expose val name: String?,
        @Expose var completionTime: SimpleTimeMark = SimpleTimeMark.farFuture(),
    ) {
        private var duration: Duration = Duration.ZERO

        fun start() {
            this.completionTime = SimpleTimeMark.now() + duration
        }

        fun sendReminderIfClaimable() {
            if (this.name == null || this.completionTime.isInFuture()) return
            ChatUtils.clickToActionOrDisable(
                "The §a$name §eupgrade has completed!",
                config::accountUpgradeReminder,
                actionName = "warp to Hub",
                action = { HypixelCommands.warp("hub") },
            )
        }

        companion object {
            fun fromItem(item: ItemStack): CommunityShopUpgrade? {
                val name = item.displayName
                val lore = item.getLore()
                val upgrade = CommunityShopUpgrade(name)
                upgrade.duration = lore.matchFirst(upgradeDurationPattern) {
                    val durationStr = group("duration")
                    if (durationStr == "Instant!") return null
                    TimeUtils.getDuration(durationStr)
                } ?: Duration.ZERO
                return upgrade
            }
        }
    }

    enum class UpgradeType {
        PROFILE,
        ACCOUNT,
        ;

        companion object {
            fun fromItem(item: ItemStack): UpgradeType? {
                val lore = item.getLore()
                return when {
                    accountUpgradePattern.anyMatches(lore) -> ACCOUNT
                    profileUpgradePattern.anyMatches(lore) -> PROFILE
                    else -> null
                }
            }
        }
    }
}
