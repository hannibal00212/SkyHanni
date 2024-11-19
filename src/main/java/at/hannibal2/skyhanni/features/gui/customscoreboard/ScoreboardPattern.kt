package at.hannibal2.skyhanni.features.gui.customscoreboard

import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object ScoreboardPattern {
    private val group = RepoPattern.group("features.gui.customscoreboard")

    // Lines from the scoreboard
    private val scoreboardGroup by group.exclusiveGroup("scoreboard")

    @SubscribeEvent
    fun onRepositoryReload(event: RepositoryReloadEvent) {
        UnknownLinesHandler.remoteOnlyPatterns = scoreboardGroup.getUnusedPatterns().toTypedArray()
    }

    // Main scoreboard
    private val mainSb = scoreboardGroup.group("main")

    /**
     * REGEX-TEST: Motes: §5137,242
     */
    val motesPattern by mainSb.pattern(
        "motes",
        "(?:§.)*Motes: (?:§.)*(?<motes>[\\d,]+).*",
    )

    /**
     * REGEX-TEST: Heat: §c1♨
     */
    val heatPattern by mainSb.pattern(
        "heat",
        "Heat: (?<heat>.*)",
    )

    /**
     * REGEX-TEST: Copper: §c3,416
     */
    val copperPattern by mainSb.pattern(
        "copper",
        "(?:§.)*Copper: (?:§.)*(?<copper>[\\d,]+).*",
    )

    /**
     * REGEX-TEST:  §5ф §dWizard Tower
     */
    val locationPattern by mainSb.pattern(
        "location",
        "\\s*(?<location>(?:§7⏣|§5ф) .*)",
    )

    /**
     * REGEX-TEST: §711/15/24 §8m10DH
     */
    val lobbyCodePattern by mainSb.pattern(
        "lobbycode",
        "\\s*§.(?:\\d{2}/?){3} §8(?<code>.*)",
    )

    /**
     * REGEX-TEST:  Early Spring 13th
     */
    val datePattern by mainSb.pattern(
        "date",
        "\\s*(?:(?:Late|Early) )?(?:Spring|Summer|Autumn|Winter) \\d+(?:st|nd|rd|th)?.*",
    )

    /**
     * REGEX-TEST:  §78:50am
     * REGEX-TEST:  §75:50am §b☽
     */
    val timePattern by mainSb.pattern(
        "time",
        "\\s*§7\\d+:\\d+(?:am|pm)\\s*(?<symbol>§b☽|§e☀|§.⚡|§.☔)?.*",
    )

    /**
     * REGEX-TEST: §ewww.hypixel.net
     * REGEX-TEST: §ealpha.hypixel.net
     */
    val footerPattern by mainSb.pattern(
        "footer",
        "§e(?:www|alpha)\\.hypixel\\.net",
    )

    /**
     * REGEX-TEST: §6Year 384 Votes
     */
    val yearVotesPattern by mainSb.pattern(
        "yearvotes",
        "§6Year \\d+ Votes",
    )

    /**
     * REGEX-TEST: §f||||||||||||||| §aFoxy
     * REGEX-TEST: §d|§f|||||||||||||| §dDiaz
     */
    val votesPattern by mainSb.pattern(
        "votes",
        "§.\\|+(?:§f)?\\|+ §.+",
    )

    /**
     * REGEX-TEST: §7Waiting for
     * REGEX-TEST: §7your vote...
     */
    val waitingForVotePattern by mainSb.pattern(
        "waitingforvote",
        "§7Waiting for|§7your vote\\.\\.\\.",
    )

    /**
     * REGEX-TEST: North Stars: §d1,539
     */
    val northstarsPattern by mainSb.pattern(
        "northstars",
        "North Stars: §d(?<northstars>[\\w,]+).*",
    )

    /**
     * REGEX-TEST:  §7♲ §7Ironman
     * REGEX-TEST:  §a☀ §aStranded
     * REGEX-TEST:  §9Ⓑ §9Bingo
     */
    val profileTypePattern by mainSb.pattern(
        "profiletype",
        "\\s*(?:§7♲ §7Ironman|§a☀ §aStranded|§.Ⓑ §.Bingo).*",
    )

    // multi use
    private val multiUseSb = scoreboardGroup.group("multiuse")

    /**
     * REGEX-TEST: Auto-closing in: §c1:58
     */
    val autoClosingPattern by multiUseSb.pattern(
        "autoclosing",
        "(?:§.)*Auto-closing in: §c(?:\\d+:)?\\d+",
    )

    /**
     * REGEX-TEST: Starting in: §a0:02
     */
    val startingInPattern by multiUseSb.pattern(
        "startingin",
        "(?:§.)*Starting in: §.(?:\\d+:)?\\d+",
    )

    /**
     * REGEX-TEST: Time Elapsed: §a48s
     */
    val timeElapsedPattern by multiUseSb.pattern(
        "timeelapsed",
        "(?:§.)*Time Elapsed: (?:§.)*(?<time>(\\w+[ydhms] ?)+)",
    )

    /**
     * REGEX-TEST: Instance Shutdown In: §a01m 59s
     */
    val instanceShutdownPattern by multiUseSb.pattern(
        "instanceshutdown",
        "(?:§.)*Instance Shutdown In: (?:§.)*(?<time>(?:\\w+[ydhms] ?)+)",
    )

    /**
     * REGEX-TEST: Time Left: §b11
     */
    val timeLeftPattern by multiUseSb.pattern(
        "timeleft",
        "(?:§.)*Time Left: (?:§.)*[\\w:,.\\s]+",
    )

    // dungeon scoreboard
    private val dungeonSb = scoreboardGroup.group("dungeon")

    /**
     * REGEX-TEST: §8- §c§4Power Dragon§a 497.3M§c❤
     * REGEX-TEST: §8- §c§4Power Dragon§a 497.3M
     */
    val m7dragonsPattern by dungeonSb.pattern(
        "m7dragons",
        "§cNo Alive Dragons|§8- (?:§.)+[\\w\\s]+Dragon§a [\\w,.]+(?:§.❤)?",
    )
    val keysPattern by dungeonSb.pattern(
        "keys",
        "Keys: §.■ §.[✗✓] §.■ §a.x",
    )
    val clearedPattern by dungeonSb.pattern(
        "cleared",
        "(?:§.)*Cleared: (?:§.)*(?<percent>[\\w,.]+)% (?:§.)*\\((?:§.)*(?<score>[\\w,.]+)(?:§.)*\\)",
    )
    val soloPattern by dungeonSb.pattern(
        "solo",
        "§3§lSolo",
    )

    @Suppress("MaxLineLength")
    val teammatesPattern by dungeonSb.pattern(
        "teammates",
        "(?:§.)*(?<classAbbv>\\[\\w]) (?:§.)*(?<username>\\w{2,16}) ((?:§.)*(?<classLevel>\\[Lvl?(?<level>[\\w,.]+)?]?)|(?:§.)*(?<health>[\\w,.]+)(?:§.)*.?)",
    )

    /**
     * REGEX-TEST: §8 - §cChaos§a 1
     */
    val floor3GuardiansPattern by dungeonSb.pattern(
        "floor3guardians",
        "§. - §.(?:Healthy|Reinforced|Laser|Chaos)§a [\\w,.]*(?:§c❤)?",
    )

    // kuudra
    private val kuudraSb = scoreboardGroup.group("kuudra")

    /**
     * REGEX-TEST: §f§lWave: §c§l2 §8- §a0:09
     */
    val wavePattern by kuudraSb.pattern(
        "wave",
        "(?:§.)*Wave: (?:§.)*\\d+(?:§.)*(?: §.- §.\\d+:\\d+)?",
    )

    /**
     * REGEX-TEST: §fTokens: §565
     */
    val tokensPattern by kuudraSb.pattern(
        "tokens",
        "(?:§.)*Tokens: §.[\\w,]+",
    )

    /**
     * REGEX-TEST: Submerges In: §e01m 00s
     * REGEX-TEST: Submerges In: §e???
     */
    val submergesPattern by kuudraSb.pattern(
        "submerges",
        "(?:§.)*Submerges In: (?:§.)*[\\w\\s?]+",
    )

    // farming
    private val farmingSb = scoreboardGroup.group("farming")

    /**
     * REGEX-TEST: §6§lGOLD §fmedals: §6111
     * REGEX-TEST: §f§lSILVER §fmedals: §f1,154
     * REGEX-TEST: §c§lBRONZE §fmedals: §c268
     */
    val medalsPattern by farmingSb.pattern(
        "medals",
        "§[6fc]§l(GOLD|SILVER|BRONZE) §fmedals: §[6fc][\\d.,]+",
    )

    /**
     * REGEX-TEST:    §cLocked
     */
    val lockedPattern by farmingSb.pattern(
        "locked",
        "\\s*§cLocked",
    )

    /**
     * REGEX-TEST:    §fCleanup§7: §e0.3%
     */
    val cleanUpPattern by farmingSb.pattern(
        "cleanup",
        "\\s*(?:§.)*Cleanup(?:§.)*: (?:§.)*[\\d,.]*%?",
    )

    /**
     * REGEX-TEST:    §fPasting§7: §e41.9%
     * REGEX-TEST:    §fBarn Pasting§7: §e10.2%
     */
    val pastingPattern by farmingSb.pattern(
        "pasting",
        "\\s*§f(?:Barn )?Pasting§7: (?:§.)*[\\d,.]+%?",
    )

    /**
     * REGEX-TEST: Pelts: §5160
     */
    val peltsPattern by farmingSb.pattern(
        "pelts",
        "(?:§.)*Pelts: (?:§.)*[\\d,]+.*",
    )

    /**
     * REGEX-TEST: Tracker Mob Location:
     */
    val mobLocationPattern by farmingSb.pattern(
        "moblocation",
        "(?:§.)*Tracker Mob Location:",
    )
    val jacobsContestPattern by farmingSb.pattern(
        "jacobscontest",
        "§eJacob's Contest",
    )

    /**
     * REGEX-TEST:    §aPlot §7- §b3 §4§lൠ§7 x8
     */
    val plotPattern by farmingSb.pattern(
        "plot",
        "\\s*§aPlot §7-.*",
    )

    // mining
    private val miningSb = scoreboardGroup.group("mining")

    /**
     * REGEX-TEST: §2᠅ §fMithril§f: §235,448
     * REGEX-TEST: §d᠅ §fGemstone§f: §d36,758
     * REGEX-TEST: §b᠅ §fGlacite§f: §b29,537
     * REGEX-TEST: §2᠅ §fMithril Powder§f: §235,448
     * REGEX-TEST: §d᠅ §fGemstone Powder§f: §d36,758
     * REGEX-TEST: §b᠅ §fGlacite Powder§f: §b29,537
     */
    val powderPattern by miningSb.pattern(
        "powder",
        "(?:§.)*᠅ §.(?<type>Gemstone|Mithril|Glacite)(?: Powder)?(?:§.)*:? (?:§.)*(?<amount>[\\d,.]*)",
    )

    /**
     * REGEX-TEST: §2᠅ §fMithril§f:§695
     * REGEX-TEST: §d᠅ §fGemstone§f
     * REGEX-TEST: §d᠅ §fGemstone§f§e(+1)
     */
    val powderGreedyPattern by miningSb.pattern(
        "powdergreedy",
        "(?:§.)*᠅ §.(?<type>Gemstone|Mithril|Glacite)(?: Powder)?.*",
    )
    val windCompassPattern by miningSb.pattern(
        "windcompass",
        "§9Wind Compass",
    )

    /**
     * REGEX-TEST:   ≈
     */
    val windCompassArrowPattern by miningSb.pattern(
        "windcompassarrow",
        "\\s*(?:§.|[⋖⋗≈])+\\s*(?:§.|[⋖⋗≈])*\\s*",
    )

    /**
     * REGEX-TEST: Event: §6§LRAFFLE
     * REGEX-TEST: Event: §C§LGOBLIN RAID
     * REGEX-TEST: Event: §B§LMITHRIL GOURMAND
     */
    val miningEventPattern by miningSb.pattern(
        "miningevent",
        "Event: §.§L.*",
    )

    /**
     * REGEX-TEST: Zone: §bGoblin Burrows
     */
    val miningEventZonePattern by miningSb.pattern(
        "miningeventzone",
        "Zone: §.*",
    )

    /**
     * REGEX-TEST: Find tickets on the
     * REGEX-TEST: ground and bring them
     * REGEX-TEST: to the raffle box
     */
    val raffleUselessPattern by miningSb.pattern(
        "raffleuseless",
        "Find tickets on the|ground and bring them|to the raffle box",
    )

    /**
     * REGEX-TEST: Tickets: §a8 §7(17.4%)
     */
    val raffleTicketsPattern by miningSb.pattern(
        "raffletickets",
        "Tickets: §a\\d+ §7\\(\\d+(\\.\\d)?%\\)",
    )

    /**
     * REGEX-TEST: Pool: §646
     */
    val rafflePoolPattern by miningSb.pattern(
        "rafflepool",
        "Pool: §6\\d+",
    )
    val mithrilUselessPattern by miningSb.pattern(
        "mithriluseless",
        "§7Give Tasty Mithril to Don!",
    )

    /**
     * REGEX-TEST: Remaining: §a80 Tasty Mithril
     * REGEX-TEST: Remaining: §aFULL
     */
    val mithrilRemainingPattern by miningSb.pattern(
        "mithrilremaining",
        "Remaining: §a(?:\\d+ Tasty Mithril|FULL)",
    )

    /**
     * REGEX-TEST: Your Tasty Mithril: §c70 §a(+70)
     */
    val mithrilYourMithrilPattern by miningSb.pattern(
        "mithrilyourmithril",
        "Your Tasty Mithril: §c\\d+.*",
    )

    /**
     * REGEX-TEST: Nearby Players: §a0
     * REGEX-TEST: Nearby Players: §cN/A
     */
    val nearbyPlayersPattern by miningSb.pattern(
        "nearbyplayers",
        "Nearby Players: §.(?:\\d+|N/A)",
    )
    val uselessGoblinPattern by miningSb.pattern(
        "uselessgoblin",
        "§7Kill goblins!",
    )

    /**
     * REGEX-TEST: Remaining: §a1 goblin
     * REGEX-TEST: Remaining: §a2 goblins
     */
    val remainingGoblinPattern by miningSb.pattern(
        "remaininggoblin",
        "Remaining: §a\\d+ goblins?",
    )

    /**
     * REGEX-TEST: Your kills: §c85 ☠
     */
    val yourGoblinKillsPattern by miningSb.pattern(
        "yourgoblin",
        "Your kills: §c\\d+ ☠(?: §a\\(\\+\\d+\\))?",
    )

    /**
     * REGEX-TEST: §7§lNot started.§7§l..
     */
    val mineshaftNotStartedPattern by miningSb.pattern(
        "mineshaft.notstarted",
        "(?:§.)*Not started.*",
    )

    /**
     * REGEX-TEST: Event Bonus: §6+4☘
     */
    val fortunateFreezingBonusPattern by miningSb.pattern(
        "fortunatefreezing.bonus",
        "Event Bonus: §6\\+\\d+☘",
    )

    /**
     * REGEX-TEST: Fossil Dust: §f3,281 §e(+1)
     */
    val fossilDustPattern by miningSb.pattern(
        "fossildust",
        "Fossil Dust: §f[\\d.,]+.*",
    )

    // combat
    private val combatSb = scoreboardGroup.group("combat")
    val magmaChamberPattern by combatSb.pattern(
        "magmachamber",
        "Magma Chamber",
    )

    /**
     * REGEX-TEST: §7Boss: §c45%
     */
    val magmaBossPattern by combatSb.pattern(
        "magmaboss",
        "§7Boss: §[c6e]\\d+%",
    )
    val damageSoakedPattern by combatSb.pattern(
        "damagesoaked",
        "§7Damage Soaked:",
    )
    val killMagmasPattern by combatSb.pattern(
        "killmagmas",
        "§6Kill the Magmas:",
    )

    /**
     * REGEX-TEST: §a▎▎▎▎▎§7▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎
     * REGEX-TEST: §a§7▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎▎
     */
    val killMagmasDamagedSoakedBarPattern by combatSb.pattern(
        "killmagmasbar",
        "(?:(?:§.)*▎+)+.*",
    )

    /**
     * REGEX-TEST: §cThe boss is reforming!
     * REGEX-TEST: §cThe boss is forming!
     */
    val reformingPattern by combatSb.pattern(
        "magmareforming",
        "§cThe boss is (?:re)?forming!",
    )
    val bossHealthPattern by combatSb.pattern(
        "magmabosshealth",
        "§7Boss Health:",
    )

    /**
     * REGEX-TEST: §e389.6k§f/§a10M§c❤
     */
    val bossHealthBarPattern by combatSb.pattern(
        "magmabosshealthbar",
        "§.[\\w,.]+§f/§a10M§c❤",
    )

    /**
     * REGEX-TEST: Dragon HP: §a2,317,156 §c❤
     * REGEX-TEST: Dragon HP: §a8,612,684 §c❤
     */
    val bossHPPattern by combatSb.pattern(
        "bosshp",
        "(?:Protector|Dragon) HP: §a[\\d,.]* §c❤",
    )

    /**
     * REGEX-TEST: Your Damage: §c0
     * REGEX-TEST: Your Damage: §c439,753.6
     */
    val bossDamagePattern by combatSb.pattern(
        "bossdamage",
        "Your Damage: §c[\\d,.]+",
    )
    val slayerQuestPattern by combatSb.pattern(
        "slayerquest",
        "Slayer Quest",
    )

    // misc
    private val miscSb = scoreboardGroup.group("misc")

    /**
     * REGEX-TEST: Dragon Essence: §d2,442
     */
    val essencePattern by miscSb.pattern(
        "essence",
        "\\s*.*Essence: §.(?<essence>-?\\d+(?::?,\\d{3})*(?:\\.\\d+)?)",
    )

    /**
     * REGEX-TEST:  §e§l⚡ §cRedstone: §e§b4%
     */
    val redstonePattern by miscSb.pattern(
        "redstone",
        "\\s*(?:§.)*⚡ §cRedstone: (?:§.)*\\d+%",
    )

    /**
     * REGEX-TEST:  §a✌ §7(§a9§7/20)
     */
    val visitingPattern by miscSb.pattern(
        "visiting",
        "\\s*§a✌ §7\\(§.\\d+(?:§.)?/\\d+(?:§.)?\\)",
    )

    /**
     * REGEX-TEST: Flight Duration: §a202:46:12
     */
    val flightDurationPattern by miscSb.pattern(
        "flightduration",
        "^\\s*Flight Duration: §a(:?\\d{1,3})*$",
    )

    /**
     * REGEX-TEST: Challenge: §6Force
     */
    val dojoChallengePattern by miscSb.pattern(
        "dojochallenge",
        "(?:§.)*Challenge: (?:§.)*(?<challenge>.+)",
    )

    /**
     * REGEX-TEST: Difficulty: §aEasy
     */
    val dojoDifficultyPattern by miscSb.pattern(
        "dojodifficulty",
        "(?:§.)*Difficulty: (?:§.)*(?<difficulty>.+)",
    )

    /**
     * REGEX-TEST: Points: §a0
     * REGEX-TEST: Points: §a10 §7(§a+§a10§7)
     */
    val dojoPointsPattern by miscSb.pattern(
        "dojopoints",
        "(?:§.)*Points: (?:§.)*[\\w.]+.*",
    )

    /**
     * REGEX-TEST: Time: §a20s
     * REGEX-TEST: Time: §a7s §7(§a0s§7)
     */
    val dojoTimePattern by miscSb.pattern(
        "dojotime",
        "(?:§.)*Time: (?:§.)*[\\w.]+.*",
    )

    /**
     * REGEX-TEST: Objective
     * REGEX-TEST: Objective §a§l⬇
     */
    val objectivePattern by miscSb.pattern(
        "objective",
        "(?:§.)*(?:Objective|Quest).*",
    )

    /**
     * REGEX-TEST: Queued: §aThe Catacombs
     */
    val queuePattern by miscSb.pattern(
        "queued",
        "Queued:.*",
    )

    /**
     * REGEX-TEST: Tier: §eFloor VI
     */
    val queueTierPattern by miscSb.pattern(
        "queuetier",
        "Tier: §e.*",
    )

    /**
     * REGEX-TEST: Position: §b#2 §fSince: §a00:01
     */
    val queuePositionPattern by miscSb.pattern(
        "queueposition",
        "Position: (?:§.)*#\\d+ (?:§.)*Since: .*",
    )

    /**
     * REGEX-TEST: §d5th Anniversary§f 167:59:54
     */
    val anniversaryPattern by miscSb.pattern(
        "anniversary",
        "§d\\d+(?:st|nd|rd|th) Anniversary§f (?:\\d|:)+",
    )

    // this thirdObjectiveLinePattern includes all those weird objective lines that go into a third (and fourth) scoreboard line
    /**
     * REGEX-TEST: §eProtect Elle §7(§a98%§7)
     * REGEX-TEST: §fFish 1 Flyfish §c✖
     * REGEX-TEST: §fFish 1 Skeleton Fish §c✖
     * REGEX-TEST:   §7(§e1§7/§a100§7)
     */
    @Suppress("MaxLineLength")
    val thirdObjectiveLinePattern by miscSb.pattern(
        "thirdobjectiveline",
        "§eProtect Elle §7\\(§.\\d+%§7\\)|\\s*§.\\(§.\\w+§.\\/§.\\w+§.\\)|§f Mages.*|§f Barbarians.*|§edefeat Kuudra|§eand stun him|§.Fish \\d .*[fF]ish §.[✖✔]",
    )

    /**
     * collection of lines that just randomly exist and I have no clue how on earth to effectively remove them
     * REGEX-TEST: §eKill 100 Automatons
     * REGEX-TEST: §eMine 10 Rubies
     * REGEX-TEST: §eFind a Jungle Key
     * REGEX-TEST: §eFind the 4 Missing Pieces
     * REGEX-TEST: §eTalk to the Goblin King
     */
    val wtfAreThoseLinesPattern by miscSb.pattern(
        "wtfarethoselines",
        "§eMine \\d+ .*|§eKill 100 Automatons|§eFind a Jungle Key|§eFind the \\d+ Missing Pieces?|§eTalk to the Goblin King",
    )
    val darkAuctionCurrentItemPattern by miscSb.pattern(
        "darkauction.currentitem",
        "Current Item:",
    )

    // events
    private val eventsSb = scoreboardGroup.group("events")

    /**
     * REGEX-TEST: §aTraveling Zoo§f 43:41
     */
    val travelingZooPattern by eventsSb.pattern(
        "travelingzoo",
        "§aTraveling Zoo§f \\d*:\\d+",
    )

    /**
     * REGEX-TEST: §dNew Year Event!§f 17:53
     */
    val newYearPattern by eventsSb.pattern(
        "newyear",
        "§dNew Year Event!§f \\d*?:?\\d+",
    )

    /**
     * REGEX-TEST: §6Spooky Festival§f 50:54
     */
    val spookyPattern by eventsSb.pattern(
        "spooky",
        "§6Spooky Festival§f \\d*?:?\\d+",
    )

    /**
     * REGEX-TEST: Event Start: §a2:38
     */
    val winterEventStartPattern by eventsSb.pattern(
        "wintereventstart",
        "(?:§.)*Event Start: §.[\\d:]+$",
    )

    /**
     * REGEX-TEST: Next Wave: §a§aSoon!
     */
    val winterNextWavePattern by eventsSb.pattern(
        "wintereventnextwave",
        "(?:§.)*Next Wave: (?:§.)*(?:[\\d:]+|Soon!)",
    )

    /**
     * REGEX-TEST: §cWave 5
     */
    val winterWavePattern by eventsSb.pattern(
        "wintereventwave",
        "(?:§.)*Wave \\d+",
    )

    /**
     * REGEX-TEST: Magma Cubes Left: §c-4
     * REGEX-TEST: Magma Cubes Left: §c3
     */
    val winterMagmaLeftPattern by eventsSb.pattern(
        "wintereventmagmaleft",
        "(?:§.)*Magma Cubes Left: §.-?\\d+",
    )

    /**
     * REGEX-TEST: Your Total Damage: §c13,804 §e(#
     */
    val winterTotalDmgPattern by eventsSb.pattern(
        "wintereventtotaldmg",
        "(?:§.)*Your Total Damage: §.[\\d+,.]+.*$",
    )

    /**
     * REGEX-TEST: Your Cube Damage: §c303
     */
    val winterCubeDmgPattern by eventsSb.pattern(
        "wintereventcubedmg",
        "(?:§.)*Your Cube Damage: §.[\\d+,.]+$",
    )

    // rift
    private val riftSb = scoreboardGroup.group("rift")

    /**
     * REGEX-TEST:  §fRift Dimension
     */
    val riftDimensionPattern by riftSb.pattern(
        "dimension",
        "\\s*§fRift Dimension",
    )
    val riftHotdogTitlePattern by riftSb.pattern(
        "hotdogtitle",
        "§6Hot Dog Contest",
    )

    /**
     * REGEX-TEST: Eaten: §c2/50
     */
    val riftHotdogEatenPattern by riftSb.pattern(
        "hotdogeaten",
        "Eaten: §.\\d+/\\d+",
    )

    /**
     * REGEX-TEST: Time spent sitting
     * REGEX-TEST: with Ävaeìkx: §a32m15s
     */
    val riftAveikxPattern by riftSb.pattern(
        "aveikx",
        "Time spent sitting|with Ävaeìkx: .*",
    )

    /**
     * REGEX-TEST: Hay Eaten: §e2,477/3,000
     */
    val riftHayEatenPattern by riftSb.pattern(
        "hayeaten",
        "Hay Eaten: §.[\\d,.]+/[\\d,.]+",
    )

    /**
     * REGEX-TEST: Clues: §a0/8
     */
    val cluesPattern by riftSb.pattern(
        "clues",
        "Clues: §.\\d+/\\d+",
    )

    /**
     * REGEX-TEST: §eFirst Up
     * REGEX-TEST: Find and talk with Barry
     */
    val barryProtestorsQuestlinePattern by riftSb.pattern(
        "protestors.quest",
        "§eFirst Up|Find and talk with Barry",
    )

    /**
     * REGEX-TEST: Protestors handled: §b5/7
     */
    val barryProtestorsHandledPattern by riftSb.pattern(
        "protestors.handled",
        "Protestors handled: §b\\d+\\/\\d+",
    )

    private val carnivalSb = scoreboardGroup.group("carnival")

    /**
     * REGEX-TEST: §eCarnival§f 85:33:57
     */
    val carnivalPattern by carnivalSb.pattern(
        "carnival",
        "§eCarnival§f (?:\\d+:?)*",
    )

    /**
     * REGEX-TEST: §3§lCatch a Fish
     * REGEX-TEST: §6§lFruit Digging
     * REGEX-TEST: §c§lZombie Shootout
     */
    val carnivalTasksPattern by carnivalSb.pattern(
        "tasks",
        "§.§l(?:Catch a Fish|Fruit Digging|Zombie Shootout)",
    )

    /**
     * REGEX-TEST: §fCarnival Tokens: §e129
     * REGEX-TEST: §fCarnival Tokens: §e1,031
     */
    val carnivalTokensPattern by carnivalSb.pattern(
        "tokens",
        "(?:§f)*Carnival Tokens: §e[\\d,]+",
    )

    /**
     * REGEX-TEST: §fFruits: §a2§7/§c10
     */
    val carnivalFruitsPattern by carnivalSb.pattern(
        "fruits",
        "(?:§f)?Fruits: §.\\d+§./§.\\d+",
    )

    /**
     * REGEX-TEST: §fScore: §e600 §6(+300)
     * REGEX-TEST: §fScore: §e600
     */
    val carnivalScorePattern by carnivalSb.pattern(
        "score",
        "(?:§f)?Score: §.\\d+.*",
    )

    /**
     * REGEX-TEST: §fCatch Streak: §a0
     */
    val carnivalCatchStreakPattern by carnivalSb.pattern(
        "catchstreak",
        "(?:§f)?Catch Streak: §.\\d+",
    )

    /**
     * REGEX-TEST: §fAccuracy: §a81.82%
     * REGEX-TEST: §fAccuracy: §a81%
     */
    val carnivalAccuracyPattern by carnivalSb.pattern(
        "accuracy",
        "(?:§f)?Accuracy: §.\\d+(?:\\.\\d+)?%",
    )

    /**
     * REGEX-TEST: §fKills: §a8
     */
    val carnivalKillsPattern by carnivalSb.pattern(
        "kills",
        "(?:§f)?Kills: §.\\d+",
    )

    // Lines from the tablist
    private val tablistGroup = group.group("tablist")

    /**
     * REGEX-TEST:  Ends In: §r§e27h
     */
    val eventTimeEndsPattern by tablistGroup.pattern(
        "eventtime",
        "\\s+Ends In: §r§e(?<time>.*)",
    )

    /**
     * REGEX-TEST:  Starts In: §r§e7h
     */
    val eventTimeStartsPattern by tablistGroup.pattern(
        "eventtimestarts",
        "\\s+Starts In: §r§e(?<time>.*)",
    )
}
