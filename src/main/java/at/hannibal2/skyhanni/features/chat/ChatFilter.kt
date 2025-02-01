package at.hannibal2.skyhanni.features.chat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.events.chat.SkyHanniChatEvent
import at.hannibal2.skyhanni.features.chat.ChatFilter.messagesMap
import at.hannibal2.skyhanni.features.chat.PowderMiningChatFilter.genericMiningRewardMessage
import at.hannibal2.skyhanni.features.dungeon.DungeonApi
import at.hannibal2.skyhanni.features.garden.GardenApi
import at.hannibal2.skyhanni.features.garden.pests.PestApi
import at.hannibal2.skyhanni.features.gifting.GiftProfitTracker
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrEmpty
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.util.ChatComponentText
import java.util.regex.Pattern

@SkyHanniModule
object ChatFilter {

    private val generalConfig get() = SkyHanniMod.feature.chat
    private val config get() = SkyHanniMod.feature.chat.filterType
    private val dungeonConfig get() = SkyHanniMod.feature.dungeon.messageFilter

    // <editor-fold desc="Regex Patterns & Messages">
    // Lobby Messages
    @Suppress("MaxLineLength")
    private val lobbyPatterns = listOf(
        // player join
        "(?: §b>§c>§a>§r §r)?.* §6(?:joined|(?:spooked|slid) into) the lobby!(?:§r §a<§c<§b<)?".toPattern(),

        // Hypixel SMP
        "§2[\\s]*?§aYou can now create your own Hypixel SMP server![\\s]*?".toPattern(),

        // Snow Particles in the Lobby
        "[\\s]*?.*§bFor the best experience, click the text below to enable Snow[\\s]§.*§bParticles in this lobby![\\s]*?.*§3§lClick to enable Snow Particles[\\s]*?".toPattern(),

        // mystery box
        "§b✦ §r.* §r§7found a §r§e.* §r§bMystery Box§r§7!".toPattern(),
        "§b✦ §r.* §r§7found (a|an) §r.* §r§7in a §r§a(Holiday )?Mystery Box§r§7!".toPattern(),

        // mystery dust
        "§b✦ §r§7You earned §r§b\\d+ §r§7Mystery Dust!".toPattern(),

        // pet consumables
        "§b✦ §r§7You earned §r§a\\d+ §r§7Pet Consumables?!".toPattern(),
    )

    private val lobbyMessages = listOf(
        // prototype
        "  §r§f§l➤ §r§6You have reached your Hype limit! Add Hype to Prototype Lobby minigames by right-clicking with the Hype Diamond!",
    )
    private val lobbyMessagesContains = listOf(
        // prototype
        "§r§6§lWelcome to the Prototype Lobby§r",

        // hypixel tournament notifications
        "§r§e§6§lHYPIXEL§e is hosting a §b§lBED WARS DOUBLES§e tournament!",
        "§r§e§6§lHYPIXEL BED WARS DOUBLES§e tournament is live!",
        "§r§e§6§lHYPIXEL§e is hosting a §b§lTNT RUN§e tournament!",

        // other
        "§aYou are still radiating with §bGenerosity§r§a!",
    )

    // Warping
    private val warpingPatterns = listOf(
        "§7Sending to server (.*)\\.\\.\\.".toPattern(),
        "§7Request join for Hub (.*)\\.\\.\\.".toPattern(),
        "§7Request join for Dungeon Hub #(.*)\\.\\.\\.".toPattern(),
        // warp portals on public islands
        // (canvas room – flower house, election room – community center, void sepulchre – the end)
        "§dWarped to (.*)§r§d!".toPattern(),
    )
    private val warpingMessages = listOf(
        "§7Warping...", "§7Warping you to your SkyBlock island...", "§7Warping using transfer token...",

        // visiting other players
        "§7Finding player...", "§7Sending a visit request...",
    )

    // Welcome
    private val welcomeMessages = listOf(
        "§eWelcome to §r§aHypixel SkyBlock§r§e!",
    )

    // Guild EXP
    /**
     * REGEX-TEST: §aYou earned §r§22 GEXP §r§afrom playing SkyBlock!
     * REGEX-TEST: §aYou earned §r§22 GEXP §r§a+ §r§c210 Event EXP §r§afrom playing SkyBlock!
     */
    private val guildExpPatterns = listOf(
        "§aYou earned §r§2.* GEXP (§r§a\\+ §r§.* Event EXP )?§r§afrom playing SkyBlock!".toPattern(),
    )

    // Kill Combo
    /**
     * REGEX-TEST: §a§l+5 Kill Combo §r§8+§r§b3% §r§b? Magic Find
     */
    private val killComboPatterns = listOf(
        "§.§l\\+(.*) Kill Combo (.*)".toPattern(),
        "§cYour Kill Combo has expired! You reached a (.*) Kill Combo!".toPattern(),
    )
    private val killComboMessages = listOf(
        "§6§l+50 Kill Combo",
    )

    // Profile Join
    private val profileJoinMessageStartsWith = listOf(
        "§aYou are playing on profile: §e", "§8Profile ID: ",
    )

    // OTHERS
    // Bazaar And AH Mini
    private val miniBazaarAndAHMessages = listOf(
        "§7Putting item in escrow...",
        "§7Putting coins in escrow...",

        // Auction House
        "§7Setting up the auction...",
        "§7Processing purchase...",
        "§7Processing bid...",
        "§7Claiming BIN auction...",

        // Bazaar
        "§6[Bazaar] §r§7Submitting sell offer...",
        "§6[Bazaar] §r§7Submitting buy order...",
        "§6[Bazaar] §r§7Executing instant sell...",
        "§6[Bazaar] §r§7Executing instant buy...",
        "§6[Bazaar] §r§7Cancelling order...",
        "§6[Bazaar] §r§7Claiming order...",
        "§6[Bazaar] §r§7Putting goods in escrow...",

        // Bank
        "§8Depositing coins...",
        "§8Withdrawing coins...",
    )

    // Slayer
    private val slayerPatterns = listOf(
        // start
        " {2}§r§5§lSLAYER QUEST STARTED!".toPattern(),
        " {3}§5§l» §7Slay §c(.*) Combat XP §7worth of (.*)§7.".toPattern(),

        // end
        " {2}§r§a§lSLAYER QUEST COMPLETE!".toPattern(),
        " {3}§r§e(.*)Slayer LVL 9 §r§5- §r§a§lLVL MAXED OUT!".toPattern(),
        " {3}§r§5§l» §r§7Talk to Maddox to claim your (.*) Slayer XP!".toPattern(),
    )
    private val slayerMessages = listOf(
        "  §r§6§lNICE! SLAYER BOSS SLAIN!", "§eYou received kill credit for assisting on a slayer miniboss!",
    )
    private val slayerMessageStartWith = listOf(
        "§e✆ RING... ",
    )

    // Slayer Drop
    @Suppress("MaxLineLength")
    private val slayerDropPatterns = listOf(
        // Zombie
        // TODO merge patterns together. Just because old ones are designed poorly doesnt mean new ones need to be poor as well
        "§b§lRARE DROP! §r§7\\(§r§f§r§7(.*)x §r§f§r§9Revenant Viscera§r§7\\) (.*)".toPattern(),
        "§b§lRARE DROP! §r§7\\(§r§f§r§9Revenant Viscera§r§7\\) (.*)".toPattern(),
        "§b§lRARE DROP! §r§7\\(§r§f§r§7(.*)x §r§f§r§9Foul Flesh§r§7\\) (.*)".toPattern(),
        "§b§lRARE DROP! §r§7\\(§r§f§r§9Foul Flesh§r§7\\) (.*)".toPattern(),
        "§6§lRARE DROP! §r§5Golden Powder (.*)".toPattern(),
        "§9§lVERY RARE DROP! {2}§r§7\\(§r§f§r§2(.*) Pestilence Rune I§r§7\\) (.*)".toPattern(),
        "§5§lVERY RARE DROP! {2}§r§7\\(§r§f§r§5Revenant Catalyst§r§7\\) (.*)".toPattern(),
        "§5§lVERY RARE DROP! {2}§r§7\\(§r§f§r§9Undead Catalyst§r§7\\) (.*)".toPattern(),
        "§5§lVERY RARE DROP! {2}§r§7\\(§r§f§r§2◆ Pestilence Rune I§r§7\\) §r§b(.*)".toPattern(),

        // Tarantula
        "§6§lRARE DROP! §r§9Arachne's Keeper Fragment (.+)".toPattern(),
        "§6§lRARE DROP! §r§5Travel Scroll to Spider's Den Top of Nest (.+)".toPattern(),
        "§9§lVERY RARE DROP! {2}§r§7\\(§r§f§r§a◆ Bite Rune I§r§7\\) (.+)".toPattern(),
        "§b§lRARE DROP! §r§7\\(§r§f§r§7(.+)x §r§f§r§aToxic Arrow Poison§r§7\\) (.+)".toPattern(),
        "§b§lRARE DROP! §r§7\\(§r§f§r§aToxic Arrow Poison§r§7\\) (.+)".toPattern(),
        "§5§lVERY RARE DROP! {2}§r§7\\(§r§9Bane of Arthropods VI§r§7\\) (.+)".toPattern(),

        // Enderman
        "§b§lRARE DROP! §r§7\\(§r§f§r§7(.*)x §r§f§r§aTwilight Arrow Poison§r§7\\) (.*)".toPattern(),
        "§5§lVERY RARE DROP! {2}§r§7\\(§r§fMana Steal I§r§7\\) (.*)".toPattern(),
        "§5§lVERY RARE DROP! {2}§r§7\\(§r§f§r§5Sinful Dice§r§7\\) (.*)".toPattern(),
        "§9§lVERY RARE DROP! {2}§r§7\\(§r§f§r§9Null Atom§r§7\\) (.*)".toPattern(),
        "§9§lVERY RARE DROP! {2}§r§7\\(§r§f§r§5Transmission Tuner§r§7\\) (.*)".toPattern(),
        "§9§lVERY RARE DROP! {2}§r§7\\(§r§fMana Steal I§r§7\\) (.*)".toPattern(),
        "§9§lVERY RARE DROP! {2}§r§7\\(§r§f§r§5◆ Endersnake Rune I§r§7\\) (.*)".toPattern(),
        "§d§lCRAZY RARE DROP! {2}§r§7\\(§r§f§r§fPocket Espresso Machine§r§7\\) (.*)".toPattern(),
        "§5§lVERY RARE DROP! {2}§r§7\\(§r§f§r§5◆ End Rune I§r§7\\) (.*)".toPattern(),
        "§5§lVERY RARE DROP! {2}§r§7\\(§r§f§r§6Hazmat Enderman§r§7\\) .*".toPattern(),

        // Blaze
        "§9§lVERY RARE DROP! {2}§r§7\\(§r§f§r§fWisp's Ice-Flavored Water I Splash Potion§r§7\\) (.*)".toPattern(),
        "§b§lRARE DROP! §r§7\\(§r§f§r§5Bundle of Magma Arrows§r§7\\) (.*)".toPattern(),
        "§9§lVERY RARE DROP! {2}§r§7\\(§r§f§r§7\\d+x §r§f§r§9(Glowstone|Blaze Rod|Magma Cream|Nether Wart) Distillate§r§7\\) (.*)".toPattern(),
    )

    // Useless Drop
    private val uselessDropPatterns = listOf(
        "§6§lRARE DROP! §r§aEnchanted Ender Pearl (.*)".toPattern(),
        "§6§lRARE DROP! §r§fCarrot (.*)".toPattern(),
        "§6§lRARE DROP! §r§fPotato (.*)".toPattern(),
        "§6§lRARE DROP! §r§9Machine Gun Bow (.*)".toPattern(),
        "§6§lRARE DROP! §r§5Earth Shard (.*)".toPattern(),
        "§6§lRARE DROP! §r§5Zombie Lord Chestplate (.*)".toPattern(),
    )
    private val uselessDropMessages = listOf(
        "§6§lRARE DROP! §r§aEnchanted Ender Pearl",
        "§6§lRARE DROP! §r§aEnchanted End Stone",
        "§6§lRARE DROP! §r§5Crystal Fragment",
    )

    // Useless Notification
    private val uselessNotificationPatterns = listOf(
        "§aYou tipped \\d+ players? in \\d+(?: different)? games?!".toPattern(),
    )
    private val uselessNotificationMessages = listOf(
        "§eYour previous §r§6Plasmaflux Power Orb §r§ewas removed!",
        "§aYou used your §r§6Mining Speed Boost §r§aPickaxe Ability!",
        "§cYour Mining Speed Boost has expired!",
        "§a§r§6Mining Speed Boost §r§ais now available!",
        "§aYou have just received §r§60 coins §r§aas interest in your personal bank account!",
        "§aSince you've been away you earned §r§60 coins §r§aas interest in your personal bank account!",
        "§aYou have just received §r§60 coins §r§aas interest in your co-op bank account!",
    )

    // Party
    private val partyMessages = listOf(
        "§9§m-----------------------------------------------------",
    )

    // MONEY
    // Auction House
    private val auctionHouseMessages = listOf(
        "§b-----------------------------------------------------", "§eVisit the Auction House to collect your item!",
    )

    // Bazaar
    private val bazaarPatterns = listOf(
        "§eBuy Order Setup! §r§a(.*)§r§7x (.*) §r§7for §r§6(.*) coins§r§7.".toPattern(),
        "§eSell Offer Setup! §r§a(.*)§r§7x (.*) §r§7for §r§6(.*) coins§r§7.".toPattern(),
        "§cCancelled! §r§7Refunded §r§6(.*) coins §r§7from cancelling buy order!".toPattern(),
        "§cCancelled! §r§7Refunded §r§a(.*)§r§7x (.*) §r§7from cancelling sell offer!".toPattern(),
    )

    // Winter Island
    private val winterIslandPatterns = listOf(
        "§r§f☃ §r§7§r(.*) §r§7mounted a §r§fSnow Cannon§r§7!".toPattern(),
    )

    // Useless Warning
    private val uselessWarningMessages = listOf(
        "§cYou are sending commands too fast! Please slow down.", // TODO prevent in the future
        "§cYou can't use this while in combat!",
        "§cYou can not modify your equipped armor set!",
        "§cPlease wait a few seconds between refreshing!",
        "§cThis item is not salvageable!", // TODO prevent in the future
        "§cPlace a Dungeon weapon or armor piece above the anvil to salvage it!",
        "§cWhoa! Slow down there!",
        "§cWait a moment before confirming!",
        "§cYou cannot open the SkyBlock menu while in combat!",
    )

    // Annoying Spam
    @Suppress("MaxLineLength")
    private val annoyingSpamPatterns = listOf(
        "§7Your Implosion hit (.*) for §r§c(.*) §r§7damage.".toPattern(),
        "§7Your Molten Wave hit (.*) for §r§c(.*) §r§7damage.".toPattern(),
        "§7Your Spirit Sceptre hit (.*) for §r§c(.*) §r§7damage.".toPattern(),
        "§cYou need a tool with a §r§aBreaking Power §r§cof §r§6(\\d)§r§c to mine (.*)§r§c! Speak to §r§dFragilis §r§cby the entrance to the Crystal Hollows to learn more!".toPattern(),
    )
    private val annoyingSpamMessages = listOf(
        "§cThere are blocks in the way!",
        "§aYour Blessing enchant got you double drops!",
        "§cYou can't use the wardrobe in combat!",
        "§6§lGOOD CATCH! §r§bYou found a §r§fFish Bait§r§b.",
        "§6§lGOOD CATCH! §r§bYou found a §r§aGrand Experience Bottle§r§b.",
        "§6§lGOOD CATCH! §r§bYou found a §r§aBlessed Bait§r§b.",
        "§6§lGOOD CATCH! §r§bYou found a §r§fDark Bait§r§b.",
        "§6§lGOOD CATCH! §r§bYou found a §r§fLight Bait§r§b.",
        "§6§lGOOD CATCH! §r§bYou found a §r§aHot Bait§r§b.",
        "§6§lGOOD CATCH! §r§bYou found a §r§fSpooky Bait§r§b.",
        "§e[NPC] Jacob§f: §rMy contest has started!",
        "§eObtain a §r§6Booster Cookie §r§efrom the community shop in the hub!",
        "Unknown command. Type \"/help\" for help. ('uhfdsolguhkjdjfhgkjhdfdlgkjhldkjhlkjhsldkjfhldshkjf')",
    )

    private val skymallMessages = listOf(
        "§bNew day! §r§eYour §r§2Sky Mall §r§ebuff changed!",
        "§8§oYou can disable this messaging by toggling Sky Mall in your /hotm!",
    )

    /**
     * REGEX-TEST: §e[NPC] Jacob§f: §rYour §9Anita's Talisman §fis giving you §6+25☘ Carrot Fortune §fduring the contest!
     */
    private val anitaFortunePattern by RepoPattern.pattern(
        "chat.jacobevent.accessory",
        "§e\\[NPC] Jacob§f: §rYour §9Anita's \\w+ §fis giving you §6\\+\\d{1,2}☘ .+ Fortune §fduring the contest!",
    )

    /**
     * REGEX-TEST: §eNew buff§r§r§r: §r§fGain §r§6+50☘ Mining Fortune§r§f.
     */
    private val skymallPerkPattern by RepoPattern.pattern(
        "chat.skymall.perk",
        "§eNew buff§r§r§r:.*",
    )

    // Winter Gift
    private val winterGiftPatterns = buildList {
        GiftProfitTracker.run {
            listOf(
                xpGainedPattern,
                coinsGainedPattern,
                northStarsPattern,
                boostPotionPattern,
                enchantmentBookPattern,
                genericRewardPattern
            ).forEach { add(it) }
        }
        addAll(GiftProfitTracker.spamPatterns)
    }

    private val fireSalePattern by RepoPattern.pattern(
        "chat.firesale",
        "§6§k§lA§r §c§lFIRE SALE §r§6§k§lA(?:\\n|.)*",
    )
    private val fireSalePatterns = listOf(
        "§c♨ §eFire Sales for .* §eare starting soon!".toPattern(),
        "§c\\s*♨ .* (?:Skin|Rune|Dye) §e(?:for a limited time )?\\(.* §eleft\\)(?:§c|!)".toPattern(),
        "§c♨ §eVisit the Community Shop in the next §c.* §eto grab yours! §a§l\\[WARP]".toPattern(),
        "§c♨ §eA Fire Sale for .* §eis starting soon!".toPattern(),
        "§c♨ §r§eFire Sales? for .* §r§eended!".toPattern(),
        "§c {3}♨ §eAnd \\d+ more!".toPattern(),
    )
    private val eventPatterns = listOf(
        "§f +§r§7You are now §r§.Event Level §r§.*§r§7!".toPattern(),
        "§f +§r§7You earned §r§.* Event Silver§r§7!".toPattern(),
        "§f +§r§.§k#§r§. LEVEL UP! §r§.§k#".toPattern(),
    )
    private val factoryUpgradePatterns = listOf(
        "§.* §r§7has been promoted to §r§7\\[.*§r§7] §r§.*§r§7!".toPattern(),
        "§7Your §r§aRabbit Barn §r§7capacity has been increased to §r§a.* Rabbits§r§7!".toPattern(),
        "§7You will now produce §r§6.* Chocolate §r§7per click!".toPattern(),
        "§7You upgraded to §r§d.*?§r§7!".toPattern(),
    )

    /**
     * REGEX-TEST: §c§lSACRIFICE! §r§6[MVP§r§d++§r§6] Mikecraft1224§r§f §r§eturned §r§6Young Dragon Boots §r§einto §r§d40 Dragon Essence§r§e!
     * REGEX-TEST: §c§lBONUS LOOT! §r§eThey also received §r§5Ritual Residue §r§efrom their sacrifice!
     */
    private val sacrificePatterns = listOf(
        "§c§lSACRIFICE! (.*) §r§eturned (.*) §r§einto (.*) Dragon Essence§r§e!".toPattern(),
        "§c§lBONUS LOOT! §r§eThey also received (.*) §r§efrom their sacrifice!".toPattern(),
    )
    private val powderMiningMessages = listOf(
        "§aYou uncovered a treasure chest!",
        "§aYou received §r§f1 §r§aWishing Compass§r§a.",
        "§aYou received §r§f1 §r§9Ascension Rope§r§a.",
        // Jungle
        "§aYou received §r§f1 §r§aOil Barrel§r§a.",
        // Useful, maybe in another chat
        "§6You have successfully picked the lock on this chest!",
    )
    private val fireSaleMessages = listOf(
        "§6§k§lA§r §c§lFIRE SALE §r§6§k§lA",
        "§c♨ §eSelling multiple items for a limited time!",
    )
    private val eventMessage = listOf(
        "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
    )

    /**
     * REGEX-TEST: §6§lRARE REWARD! §r§bLeebys §r§efound a §r§6Recombobulator 3000 §r§ein their Obsidian Chest§r§e!
     */
    private val rareDropsMessages = listOf(
        "§6§lRARE REWARD! (.*) §r§efound a (.*) §r§ein their (.*) Chest§r§e!".toPattern(),
    )

    // &r&6Your &r&aMage &r&6stats are doubled because you are the only player using this class!&r
    private val soloClassPatterns = listOf(
        "§6Your §r§a(Healer|Mage|Berserk|Archer|Tank) §r§6stats are doubled because you are the only player using this class!".toPattern(),
    )

    private val soloStatsPatterns = listOf(
        "§a\\[(Healer|Mage|Berserk|Archer|Tank)].*".toPattern(),
    )

    // &r&dGenevieve the Fairy&r&f: You killed me! Take this &r&6Revive Stone &r&fso that my death is not in vain!&r
    private val fairyPatterns = listOf(
        "§d[\\w']+ the Fairy§r§f: You killed me! Take this §r§6Revive Stone §r§fso that my death is not in vain!".toPattern(),
        "§d[\\w']+ the Fairy§r§f: You killed me! I'll revive you so that my death is not in vain!".toPattern(),
        "§d[\\w']+ the Fairy§r§f: Have a great life!".toPattern(),
    )

    // §e§ka§a>>   §aAchievement Unlocked: §6§r§6Agile§r§a   <<§e§ka
    private val achievementGetPatterns = listOf(
        "§e§k.§a>> {3}§aAchievement Unlocked: .* {3}<<§e§k.".toPattern(),
    )

    /**
     * REGEX-TEST: §aStarted parkour cocoa!
     * REGEX-TEST: §aFinished parkour cocoa in 12:34.567!
     * REGEX-TEST: §aReached checkpoint #4 for parkour cocoa!
     * REGEX-TEST: §4Wrong checkpoint for parkour cocoa!
     * REGEX-TEST: §4You haven't reached all checkpoints for parkour cocoa!
     */
    private val parkourPatterns = listOf(
        "§aStarted parkour (.*)!".toPattern(),
        "§aFinished parkour (.*) in (.*)!".toPattern(),
        "§aReached checkpoint #(.*) for parkour (.*)!".toPattern(),
        "§4Wrong checkpoint for parkour (.*)!".toPattern(),
        "§4You haven't reached all checkpoints for parkour (.*)!".toPattern(),
    )

    /**
     * REGEX-TEST: §4Cancelled parkour! You cannot fly.
     * REGEX-TEST: §4Cancelled parkour! You cannot use item abilities.
     * REGEX-TEST: §4Cancelled parkour!
     */
    private val parkourCancelMessages = listOf(
        "§4Cancelled parkour! You cannot fly.",
        "§4Cancelled parkour! You cannot use item abilities.",
        "§4Cancelled parkour!",
    )

    /**
     ** REGEX-TEST: §r§aWarped from the tpPadOne §r§ato the tpPadTwo§r§a!
     */
    private val teleportPadPatterns = listOf(
        "§aWarped from the (.*) §r§ato the (.*)§r§a!".toPattern(),
    )

    // §r§4This Teleport Pad does not have a destination set!
    private val teleportPadMessages = listOf(
        "§4This Teleport Pad does not have a destination set!",
    )

    private val patternsMap: Map<String, List<Pattern>> = mapOf(
        "lobby" to lobbyPatterns,
        "warping" to warpingPatterns,
        "guild_exp" to guildExpPatterns,
        "kill_combo" to killComboPatterns,
        "slayer" to slayerPatterns,
        "slayer_drop" to slayerDropPatterns,
        "useless_drop" to uselessDropPatterns,
        "useless_notification" to uselessNotificationPatterns,
        "money" to bazaarPatterns,
        "winter_island" to winterIslandPatterns,
        "annoying_spam" to annoyingSpamPatterns,
        "winter_gift" to winterGiftPatterns,
        "fire_sale" to fireSalePatterns,
        "event" to eventPatterns,
        "factory_upgrade" to factoryUpgradePatterns,
        "sacrifice" to sacrificePatterns,
        "rare_drops" to rareDropsMessages,
        "solo_class" to soloClassPatterns,
        "solo_stats" to soloStatsPatterns,
        "fairy" to fairyPatterns,
        "achievement_get" to achievementGetPatterns,
        "parkour" to parkourPatterns,
        "teleport_pads" to teleportPadPatterns,
    )

    private val messagesMap: Map<String, List<String>> = mapOf(
        "lobby" to lobbyMessages,
        "warping" to warpingMessages,
        "welcome" to welcomeMessages,
        "kill_combo" to killComboMessages,
        "bz_ah_minis" to miniBazaarAndAHMessages,
        "slayer" to slayerMessages,
        "useless_drop" to uselessDropMessages,
        "useless_notification" to uselessNotificationMessages,
        "party" to partyMessages,
        "money" to auctionHouseMessages,
        "useless_warning" to uselessWarningMessages,
        "annoying_spam" to annoyingSpamMessages,
        "powder_mining" to powderMiningMessages,
        "fire_sale" to fireSaleMessages,
        "event" to eventMessage,
        "skymall" to skymallMessages,
        "parkour" to parkourCancelMessages,
        "teleport_pads" to teleportPadMessages,
    )

    private val messagesContainsMap: Map<String, List<String>> = mapOf(
        "lobby" to lobbyMessagesContains,
    )

    private val messagesStartsWithMap: Map<String, List<String>> = mapOf(
        "slayer" to slayerMessageStartWith,
        "profile_join" to profileJoinMessageStartsWith,
    )
    // </editor-fold>

    @HandleEvent
    fun onChat(event: SkyHanniChatEvent) {
        var blockReason = block(event.message)
        if (blockReason == null && config.powderMining.enabled) blockReason = powderMiningBlock(event)
        if (blockReason == null && config.crystalNucleus.enabled) blockReason = crystalNucleusBlock(event)

        event.blockedReason = blockReason ?: return
    }

    /**
     * Checks if the message should be blocked
     * @param message The message to check
     * @return The reason why the message was blocked, empty if not blocked
     */
    @Suppress("CyclomaticComplexMethod", "MaxLineLength")
    private fun block(message: String): String? = when {
        config.hypixelHub && message.isPresent("lobby") -> "lobby"
        config.empty && StringUtils.isEmpty(message) -> "empty"
        config.warping && message.isPresent("warping") -> "warping"
        config.welcome && message.isPresent("welcome") -> "welcome"
        config.guildExp && message.isPresent("guild_exp") -> "guild_exp"
        config.killCombo && message.isPresent("kill_combo") -> "kill_combo"
        config.profileJoin && message.isPresent("profile_join") -> "profile_join"
        config.parkour && message.isPresent("parkour") -> "parkour"
        config.teleportPads && message.isPresent("teleport_pads") -> "teleport_pads"

        config.hideAlphaAchievements && HypixelData.hypixelAlpha && message.isPresent("achievement_get") -> "achievement_get"

        config.others && isOthers(message) -> othersMsg

        config.winterGift && message.isPresent("winter_gift") -> "winter_gift"

        // TODO need proper solution to hide empty messages in event text
        config.eventLevelUp && (message.isPresent("event")) -> "event"

        config.fireSale && (fireSalePattern.matches(message) || message.isPresent("fire_sale")) -> "fire_sale"
        config.factoryUpgrade && message.isPresent("factory_upgrade") -> "factory_upgrade"
        config.sacrifice && message.isPresent("sacrifice") -> "sacrifice"
        generalConfig.hideJacob && !GardenApi.inGarden() && anitaFortunePattern.matches(message) -> "jacob_event"
        generalConfig.hideSkyMall && !LorenzUtils.inMiningIsland() && (skymallPerkPattern.matches(message) || message.isPresent("skymall")) -> "skymall"
        dungeonConfig.rareDrops && message.isPresent("rare_drops") -> "rare_drops"
        dungeonConfig.soloClass && DungeonApi.inDungeon() && message.isPresent("solo_class") -> "solo_class"
        dungeonConfig.soloStats && DungeonApi.inDungeon() && message.isPresent("solo_stats") -> "solo_stats"
        dungeonConfig.fairy && DungeonApi.inDungeon() && message.isPresent("fairy") -> "fairy"
        config.gardenNoPest && GardenApi.inGarden() && PestApi.noPestsChatPattern.matches(message) -> "garden_pest"

        else -> null
    }

    /**
     * Checks if the message is a blocked powder mining message, as defined in PowderMiningChatFilter.
     * Will modify un-filtered Mining rewards, or return a resultant blocking code
     * @param event The event to check
     * @return Block reason if applicable
     * @see block
     */
    private fun powderMiningBlock(event: SkyHanniChatEvent): String? {
        val powderMiningMatchResult = PowderMiningChatFilter.block(event.message)
        if (powderMiningMatchResult == "no_filter") {
            genericMiningRewardMessage.matchMatcher(event.message) {
                val reward = groupOrEmpty("reward")
                val amountFormat = groupOrNull("amount")?.let {
                    "§a+ §b$it§r"
                } ?: "§a+§r"
                event.chatComponent = ChatComponentText("$amountFormat $reward")
            }
            return null
        }
        return powderMiningMatchResult
    }

    /**
     * Checks if the message is a blocked Crystal Nucleus Run message, as defined in CrystalNucleusChatFilter.
     * Will conditionally modify/compact messages in some cases, or return a blocking code
     * @param event The event to check
     * @return Block reason if applicable
     * @see block
     */
    private fun crystalNucleusBlock(event: SkyHanniChatEvent): String? {
        val (blockCode, newMessage) = CrystalNucleusChatFilter.block(event.message)?.getPair() ?: Pair(null, null)
        newMessage?.let { event.chatComponent = ChatComponentText(it) }
        blockCode?.let { return it }
        return null
    }

    private var othersMsg: String? = null

    /**
     * Checks if the message is an "other" message.
     * Will also set the variable othersMsg to the reason why the message was blocked,
     * so that it can be used in the block function.
     * @param message The message to check
     * @return True if the message is part of "other"
     * @see othersMsg
     * @see block
     */
    private fun isOthers(message: String): Boolean {
        othersMsg = when {
            message.isPresent("bz_ah_minis") -> "bz_ah_minis"
            message.isPresent("slayer") -> "slayer"
            message.isPresent("slayer_drop") -> "slayer_drop"
            message.isPresent("useless_drop") -> "useless_drop"
            message.isPresent("useless_notification") -> "useless_notification"
            message.isPresent("party") -> "party"
            message.isPresent("money") -> "money"
            message.isPresent("winter_island") -> "winter_island"
            message.isPresent("useless_warning") -> "useless_warning"
            message.isPresent("annoying_spam") -> "annoying_spam"
            else -> null
        }
        return othersMsg != null
    }

    /**
     * Checks if the message is present in the list of messages or patterns
     * Checks against four maps that compare in different ways.
     * @receiver message The message to check
     * @param key The key of the list to check
     * @return True if the message is present in any of the maps
     * @see messagesMap
     * @see patternsMap
     * @see messagesContainsMap
     * @see messagesStartsWithMap
     */
    private fun String.isPresent(key: String) = this in (messagesMap[key].orEmpty()) ||
        (patternsMap[key].orEmpty()).any { it.matches(this) } ||
        (messagesContainsMap[key].orEmpty()).any { this.contains(it) } ||
        (messagesStartsWithMap[key].orEmpty()).any { this.startsWith(it) }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "chat.hypixelHub", "chat.filterType.hypixelHub")
        event.move(3, "chat.empty", "chat.filterType.empty")
        event.move(3, "chat.warping", "chat.filterType.warping")
        event.move(3, "chat.guildExp", "chat.filterType.guildExp")
        event.move(3, "chat.friendJoinLeft", "chat.filterType.friendJoinLeft")
        event.move(3, "chat.winterGift", "chat.filterType.winterGift")
        event.move(3, "chat.powderMining", "chat.filterType.powderMining")
        event.move(3, "chat.killCombo", "chat.filterType.killCombo")
        event.move(3, "chat.profileJoin", "chat.filterType.profileJoin")
        event.move(3, "chat.others", "chat.filterType.others")
        event.move(52, "chat.filterType.powderMining", "chat.filterType.powderMiningFilter.enabled")
        event.transform(53, "chat.filterType.powderMiningFilter.gemstoneFilterConfig") { element ->
            element.asJsonObject.apply {
                entrySet().forEach { (key, value) ->
                    if (value.asString == "FINE_ONLY") addProperty(key, "FINE_UP")
                }
            }
        }
        event.move(61, "chat.filterType.powderMiningFilter", "chat.filterType.powderMining")
        event.move(61, "chat.filterType.gemstoneFilterConfig", "chat.filterType.powderMining.gemstone")
    }
}
