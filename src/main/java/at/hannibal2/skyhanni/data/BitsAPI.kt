package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.BitsAvailableUpdateEvent
import at.hannibal2.skyhanni.events.BitsUpdateEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.formatInt
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.StringUtils.removeResets
import at.hannibal2.skyhanni.utils.StringUtils.trimWhiteSpace
import at.hannibal2.skyhanni.utils.TimeUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.days

@SkyHanniModule
object BitsAPI {
    private val profileStorage get() = ProfileStorageData.profileSpecific?.bits
    private val playerStorage get() = ProfileStorageData.playerSpecific

    var bits: Int
        get() = profileStorage?.bits ?: 0
        private set(value) {
            profileStorage?.bits = value
        }
    var fameRank: FameRank?
        get() = playerStorage?.fameRank?.let(FameRanks::getByInternalName)
        private set(value) {
            if (value != null) {
                playerStorage?.fameRank = value.internalName
            }
        }
    var bitsAvailable: Int
        get() = profileStorage?.bitsAvailable ?: 0
        private set(value) {
            profileStorage?.bitsAvailable = value
        }

    var cookieBuffTime: SimpleTimeMark?
        get() = profileStorage?.boosterCookieExpiryTime
        private set(value) {
            profileStorage?.boosterCookieExpiryTime = value
        }

    private const val DEFAULT_COOKIE_BITS = 4800

    private val bitsDataGroup = RepoPattern.group("data.bits")

    // Scoreboard patterns
    val bitsScoreboardPattern by bitsDataGroup.pattern(
        "scoreboard",
        "^Bits: §b(?<amount>[\\d,.]+).*$",
    )

    // Chat patterns
    private val bitsChatGroup = bitsDataGroup.group("chat")

    private val bitsFromFameRankUpChatPattern by bitsChatGroup.pattern(
        "rankup.bits",
        "§eYou gained §3(?<amount>.*) Bits Available §ecompounded from all your " +
            "§epreviously eaten §6cookies§e! Click here to open §6cookie menu§e!",
    )

    private val fameRankUpPattern by bitsChatGroup.pattern(
        "rankup.rank",
        "[§\\w\\s]+FAME RANK UP (?:§.)+(?<rank>.*)",
    )

    private val boosterCookieAte by bitsChatGroup.pattern(
        "boostercookieate",
        "§eYou consumed a §6Booster Cookie§e!.*",
    )

    // GUI patterns
    private val bitsGuiGroup = bitsDataGroup.group("gui")

    private val bitsAvailableMenuPattern by bitsGuiGroup.pattern(
        "availablemenu",
        "§7Bits Available: §b(?<toClaim>[\\d,]+)(§3.+)?",
    )

    private val fameRankSbMenuPattern by bitsGuiGroup.pattern(
        "sbmenufamerank",
        "§7Your rank: §e(?<rank>.*)",
    )

    /**
     * REGEX-TEST:  §7Duration: §a140d 8h 35m 36s
     */
    private val cookieDurationPattern by bitsGuiGroup.pattern(
        "cookieduration",
        "\\s*§7Duration: §a(?<time>.*)",
    )

    private val noCookieActiveSBMenuPattern by bitsGuiGroup.pattern(
        "sbmenunocookieactive",
        " §7Status: §cNot active!",
    )

    private val noCookieActiveCookieMenuPattern by bitsGuiGroup.pattern(
        "cookiemenucookieactive",
        "(§7§cYou do not currently have a|§cBooster Cookie active!)",
    )

    private val fameRankCommunityShopPattern by bitsGuiGroup.pattern(
        "communityshopfamerank",
        "§7Fame Rank: §e(?<rank>.*)",
    )

    private val bitsGuiNamePattern by bitsGuiGroup.pattern(
        "mainmenuname",
        "^SkyBlock Menu$",
    )

    private val cookieGuiStackPattern by bitsGuiGroup.pattern(
        "mainmenustack",
        "^§6Booster Cookie$",
    )

    private val bitsStackPattern by bitsGuiGroup.pattern(
        "bitsstack",
        "§bBits",
    )

    private val fameRankGuiNamePattern by bitsGuiGroup.pattern(
        "famerankmenuname",
        "^(Community Shop|Booster Cookie)$",
    )

    private val fameRankGuiStackPattern by bitsGuiGroup.pattern(
        "famerankmenustack",
        "^(§aCommunity Shop|§eFame Rank)$",
    )

    @SubscribeEvent
    fun onScoreboardChange(event: ScoreboardUpdateEvent) {
        if (!isEnabled()) return
        for (line in event.scoreboard) {
            val message = line.trimWhiteSpace().removeResets()

            bitsScoreboardPattern.matchMatcher(message) {
                val amount = group("amount").formatInt()
                val diff = amount - bits
                if (diff == 0) return

                if (diff > 0) {
                    bitsAvailable -= diff
                    bits = amount
                    sendBitsGainEvent(diff)
                } else {
                    bits = amount
                    sendBitsSpentEvent(diff)
                }
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        val message = event.message.trimWhiteSpace().removeResets()

        bitsFromFameRankUpChatPattern.matchMatcher(message) {
            val amount = group("amount").formatInt()
            bitsAvailable += amount
            sendBitsAvailableGainedEvent()

            return
        }

        fameRankUpPattern.matchMatcher(message) {
            val rank = group("rank")

            fameRank = FameRanks.getByName(rank)
                ?: return ErrorManager.logErrorWithData(
                    FameRankNotFoundException(rank),
                    "FameRank $rank not found",
                    "Rank" to rank,
                    "Message" to message,
                    "FameRanks" to FameRanks.fameRanksMap,
                )

            return
        }

        boosterCookieAte.matchMatcher(message) {
            bitsAvailable += bitsPerCookie()
            val cookieTime = cookieBuffTime
            cookieBuffTime = if (cookieTime == null) SimpleTimeMark.now() + 4.days else cookieTime + 4.days
            sendBitsAvailableGainedEvent()

            return
        }
    }

    fun bitsPerCookie(): Int = (DEFAULT_COOKIE_BITS * (fameRank?.bitsMultiplier ?: 1.0)).toInt()

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!isEnabled()) return

        val stacks = event.inventoryItems

        if (bitsGuiNamePattern.matches(event.inventoryName)) {
            val cookieStack = stacks.values.lastOrNull { cookieGuiStackPattern.matches(it.displayName) }

            // If the cookie stack is null, then the player should not have any bits to claim
            if (cookieStack == null) {
                bitsAvailable = 0
                cookieBuffTime = SimpleTimeMark.farPast()
                return
            }

            val lore = cookieStack.getLore()
            bitsAvailableMenuPattern.firstMatcher(lore) {
                val amount = group("toClaim").formatInt()
                if (bitsAvailable != amount) {
                    bitsAvailable = amount
                    sendBitsAvailableGainedEvent()

                    val difference = bits - bitsAvailable
                    if (difference > 0) {
                        bits += difference
                    }
                }
            }
            cookieDurationPattern.firstMatcher(lore) {
                val duration = TimeUtils.getDuration(group("time"))
                cookieBuffTime = SimpleTimeMark.now() + duration
            }
            noCookieActiveSBMenuPattern.firstMatcher(lore) {
                val cookieTime = cookieBuffTime
                if (cookieTime == null || cookieTime.isInFuture()) cookieBuffTime = SimpleTimeMark.farPast()
            }
            return
        }

        if (fameRankGuiNamePattern.matches(event.inventoryName)) {
            val bitsStack = stacks.values.lastOrNull { bitsStackPattern.matches(it.displayName) } ?: return
            val fameRankStack = stacks.values.lastOrNull { fameRankGuiStackPattern.matches(it.displayName) } ?: return
            val cookieStack = stacks.values.lastOrNull { cookieGuiStackPattern.matches(it.displayName) } ?: return

            line@ for (line in fameRankStack.getLore()) {
                for (pattern in listOf(fameRankCommunityShopPattern, fameRankSbMenuPattern)) {
                    pattern.matchMatcher(line) {
                        val rank = group("rank")

                        fameRank = FameRanks.getByName(rank)
                            ?: return ErrorManager.logErrorWithData(
                                FameRankNotFoundException(rank),
                                "FameRank $rank not found",
                                "Rank" to rank,
                                "Lore" to fameRankStack.getLore(),
                                "FameRanks" to FameRanks.fameRanksMap,
                            )
                        continue@line
                    }
                }
            }

            line@ for (line in bitsStack.getLore()) {
                bitsAvailableMenuPattern.matchMatcher(line) {
                    val amount = group("toClaim").formatInt()
                    if (amount != bitsAvailable) {
                        bitsAvailable = amount
                        sendBitsAvailableGainedEvent()
                    }

                    continue@line
                }
            }

            line@ for (line in cookieStack.getLore()) {
                cookieDurationPattern.matchMatcher(line) {
                    val duration = TimeUtils.getDuration(group("time"))
                    cookieBuffTime = SimpleTimeMark.now().plus(duration)
                }
                if (noCookieActiveCookieMenuPattern.matches(line)) {
                    val nextLine = cookieStack.getLore().nextAfter(line) ?: continue@line
                    if (noCookieActiveCookieMenuPattern.matches(nextLine)) cookieBuffTime = SimpleTimeMark.farPast()
                }
            }
        }
    }

    fun hasCookieBuff() = cookieBuffTime?.isInFuture() ?: false

    private fun sendBitsGainEvent(difference: Int) =
        BitsUpdateEvent.BitsGain(bits, bitsAvailable, difference).post()

    private fun sendBitsSpentEvent(difference: Int) =
        BitsUpdateEvent.BitsSpent(bits, bitsAvailable, difference).post()

    private fun sendBitsAvailableGainedEvent() = BitsAvailableUpdateEvent(bitsAvailable).post()

    fun isEnabled() = LorenzUtils.inSkyBlock && profileStorage != null

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(35, "#profile.bits.bitsToClaim", "#profile.bits.bitsAvailable")
    }

    class FameRankNotFoundException(rank: String) : Exception("FameRank not found: $rank")
}
