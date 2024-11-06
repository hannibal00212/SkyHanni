package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ScoreboardUpdateEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.formatPercentage
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object DragonFeatures {

    private val config get() = SkyHanniMod.feature.combat.dragon
    private val configProtector get() = SkyHanniMod.feature.combat.endstoneProtectorChat

    private val dragonNames = listOf("Protector", "Old", "Wise", "Unstable", "Young", "Strong", "Superior")

    private val dragonNamesAsRegex = dragonNames.joinToString("|")
    private val dragonNamesUpperCaseAsRegex = dragonNames.joinToString("|") { it.uppercase() }

    private val protectorRepoGroup = RepoPattern.group("combat.boss.protector")
    private val repoGroup = RepoPattern.group("combat.boss.dragon")
    private val chatGroup = repoGroup.group("chat")
    private val scoreBoardGroup = repoGroup.group("scoreboard")
    private val tabListGroup = repoGroup.group("tablist")

    /** REGEX-TEST: §5☬ §r§dYou placed a Summoning Eye! §r§7(§r§e2§r§7/§r§a8§r§7)
     * REGEX-TEST: §5☬ §r§dYou placed a Summoning Eye! Brace yourselves! §r§7(§r§a8§r§7/§r§a8§r§7)
     */
    private val eyePlaced by chatGroup.pattern(
        "eye.placed.you",
        "§5☬ §r§dYou placed a Summoning Eye! §r§7\\(§r§e\\d§r§7\\/§r§a8§r§7\\)|§5☬ §r§dYou placed a Summoning Eye! Brace yourselves! §r§7\\(§r§a8§r§7\\/§r§a8§r§7\\)",
    )

    /** REGEX-TEST: §5You recovered a Summoning Eye!
     */
    private val eyeRemoved by chatGroup.pattern("eye.removed.you", "§5You recovered a Summoning Eye!")

    /** REGEX-TEST: §5☬ §r§dThe Dragon Egg has spawned!
     */
    private val eggSpawned by chatGroup.pattern("egg.spawn", "§5☬ §r§dThe Dragon Egg has spawned!")

    /** REGEX-TEST: §f                      §r§6§lPROTECTOR DRAGON DOWN!
     */
    private val endStartLineDragon by chatGroup.pattern(
        "end.boss",
        "§f +§r§6§l(?<Dragon>${dragonNamesUpperCaseAsRegex}) DRAGON DOWN!",
    )

    /** REGEX-TEST: §f                    §r§6§lENDSTONE PROTECTOR DOWN!
     */
    private val endStartLineProtector by protectorRepoGroup.pattern(
        "chat.end.boss",
        "§f +§r§6§l ENDSTONE PROTECTOR DOWN!",
    )

    /** REGEX-TEST: §f                   §r§eYour Damage: §r§a88,966 §r§7(Position #5)
     */
    private val endPosition by chatGroup.pattern(
        "end.position",
        "§f +§r§eYour Damage: §r§a(?<Damage>[\\d.,]+) (?:§r§d§l\\(NEW RECORD!\\) )?§r§7\\(Position #(?<Position>\\d+)\\)",
    )

    // val endFinalHit by chatGroup.pattern("end.final", "§f                 §r§b[^ ]+ (?<Name>.*)§r§f §r§7dealt the final blow.")
    /** REGEX-TEST: §f             §r§e§l1st Damager §r§7- §r§a[VIP] Jarre07§r§f §r§7- §r§e9,659,033
     * REGEX-TEST: §f          §r§6§l2nd Damager §r§7- §r§b[MVP§r§9+§r§b] FlamingZoom§r§f §r§7- §r§e1,459,691
     * REGEX-TEST: §f          §r§c§l3rd Damager §r§7- §r§b[MVP§r§f+§r§b] Dustbringer§r§f §r§7- §r§e1,091,163
     */
    private val endLeaderboard by chatGroup.pattern(
        "end.place",
        "§f +§r§.§l(?<Position>\\d+).. Damager §r§7- §r§.(?:\\[[^ ]+\\] )?(?<Name>.*)§r§. §r§7- §r§e(?<Damage>[\\d.,]+)",
    )

    /** REGEX-TEST: §f                       §r§eZealots Contributed: §r§a27§r§e/100
     */
    private val endZealots by protectorRepoGroup.pattern(
        "chat.end.zealot",
        "§f +§r§eZealots Contributed: §r§a(?<Amount>\\d+)§r§e/100",
    )

    /** REGEX-TEST: §5☬ §r§d§lThe §r§5§c§lProtector Dragon§r§d§l has spawned!
     */
    private val dragonSpawn by chatGroup.pattern(
        "spawn",
        "§5☬ §r§d§lThe §r§5§c§l(?<Dragon>${dragonNamesAsRegex}) Dragon§r§d§l has spawned!",
    )
    private val scoreDamage by scoreBoardGroup.pattern("damage", "Your Damage: §c(?<Damage>[\\w,.]+)")
    private val scoreDragon by scoreBoardGroup.pattern("dragon", "Dragon HP: .*")

    // private val scoreProtector by protectorRepoGroup.pattern("scoreboard.protector", "Protector HP: .*")
    private val tabDamage by tabListGroup.pattern(
        "fight.player",
        ".*§r§f(?<Name>.+): §r§c(?<Damage>[\\d.]+)(?<Unit>[kM])?❤",
    )

    private var yourEyes = 0

    private var dragonSpawned = false
        set(value) {
            field = value
            if (dragonSpawned) {
                egg = false
            }
        }

    private enum class Type {
        GOLEM,
        DRAGON
    }

    private var endType: Type? = null
    private var endTopDamage = 0.0
    private var endDamage = 0.0
    private var endPlace = 0

    private var currentDamage = 0.0
    private var currentTopDamage = 0.0
    private var currentPlace: Int? = null
    private var widgetActive = false
    private var egg = true

    private fun resetEnd() {
        endType = null
        endTopDamage = 0.0
        endDamage = 0.0
        endPlace = 0
    }

    private fun reset() {
        resetEnd()
        dragonSpawned = false
        currentTopDamage = 0.0
        currentDamage = 0.0
        currentPlace = null
        widgetActive = false
        yourEyes = 0
    }

    private fun enable() = LorenzUtils.inSkyBlock && IslandType.THE_END.isInIsland()

    private fun enableDisplay() = enable() && config.display

    private fun dragonWeightMap(place: Int) = when (place) {
        -1 -> 10
        1 -> 300
        2 -> 250
        3 -> 200
        4 -> 125
        5 -> 110
        6, 7, 8 -> 100
        9, 10 -> 90
        11, 12 -> 80
        else -> 70
    }

    private fun protectorWeightMap(place: Int) = when (place) {
        -1 -> 10
        1 -> 200
        2 -> 175
        3 -> 150
        4 -> 125
        5 -> 110
        6, 7, 8 -> 100
        9, 10 -> 90
        11, 12 -> 80
        else -> 70
    }

    private fun calculateDragonWeight(eyes: Int, place: Int, firstDamage: Double, yourDamage: Double) =
        dragonWeightMap(if (yourDamage == 0.0) -1 else place) + 100 * (eyes + yourDamage / (firstDamage.takeIf { it != 0.0 }
            ?: 1.0))

    private fun calculateProtectorWeight(zealots: Int, place: Int, firstDamage: Double, yourDamage: Double) =
        protectorWeightMap(if (yourDamage == 0.0) -1 else place) + 50 * (yourDamage / (firstDamage.takeIf { it != 0.0 }
            ?: 1.0)) + if (zealots > 100) 100 else zealots

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!enable()) return
        val message = event.message
        if (!config.chat && !config.display && !config.superiorNotify && !configProtector) return
        dragonSpawn.matchMatcher(message) {
            dragonSpawned = true
            if (config.superiorNotify && this.group("Dragon") == "Superior") {
                LorenzUtils.sendTitle("§6Superior Dragon Spawned!", 1.5.seconds)
            }
            return
        }
        if (!config.chat && !config.display && !configProtector) return
        when {
            eyePlaced.matches(message) -> {
                yourEyes++
            }

            eyeRemoved.matches(message) -> {
                yourEyes--
            }

            eggSpawned.matches(message) -> {
                egg = true
            }

            endStartLineDragon.matches(message) -> {
                if (!config.chat) {
                    reset()
                    return
                }
                endType = Type.DRAGON
            }

            endStartLineProtector.matches(message) -> {
                if (!configProtector) return
                endType = Type.GOLEM
            }

            else -> {
                endLeaderboard.matchMatcher(message) {
                    if (endType == null) return@matchMatcher
                    if (this.group("Position") != "1") return@matchMatcher
                    endTopDamage = this.group("Damage").replace(",", "").toDouble()
                    return
                }
                endPosition.matchMatcher(message) {
                    if (endType == null) return@matchMatcher
                    endPlace = this.group("Position")?.toInt() ?: -1
                    endDamage = this.group("Damage").replace(",", "").toDouble()
                    when (endType) {
                        Type.DRAGON -> {
                            val weight = calculateDragonWeight(
                                yourEyes, endPlace, endTopDamage, endDamage,
                            )

                            printWeight(weight)
                            DragonFeatures.reset() // love name collisions
                        }

                        Type.GOLEM -> {

                            // NO reset because of Zealot Line
                        }

                        null -> return@matchMatcher
                    }
                    return
                }
                endZealots.matchMatcher(message) {
                    if (endType != Type.GOLEM) return@matchMatcher
                    val zealots = this.group("Amount").toInt()
                    val weight = calculateProtectorWeight(zealots, endPlace, endTopDamage, endDamage)
                    printWeight(weight)
                    resetEnd()
                }
            }
        }

    }

    private fun printWeight(weight: Double) {
        ChatUtils.chat("§f                §r§eYour Weight: §r§a${weight.round(0).addSeparators()}")
    }

    @SubscribeEvent
    fun onScoreBoard(event: ScoreboardUpdateEvent) {
        if (!(enableDisplay())) return
        val index = event.full.indexOfFirst { scoreDragon.matches(it) }
        if (index == -1) return
        if (egg) {
            dragonSpawned = true
        }
        scoreDamage.matchMatcher(event.full[index + 1]) {
            currentDamage = this.group("Damage").replace(",", "").toDouble()
        }

    }

    @SubscribeEvent
    fun onTabList(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.DRAGON)) return
        if (!(enableDisplay() && dragonSpawned)) return
        widgetActive = true
        for (i in 1 until event.lines.size) {
            tabDamage.matchMatcher(event.lines[i]) {
                if (i == 1) {
                    currentTopDamage = this.group("Damage").toDouble() * this.group("Unit").let {
                        when (it) {
                            "k" -> 1_000
                            "M" -> 1_000_000
                            else -> 1
                        }
                    }
                }
                if (this.group("Name") == LorenzUtils.getPlayerName()) {
                    currentPlace = i
                }
            }
        }
    }

    private val widgetErrorGUI = listOf(Renderable.string("§cDragon Widget is disabled!"))

    @SubscribeEvent
    fun onRender(event: GuiRenderEvent) {
        if (!(enableDisplay() && dragonSpawned)) return
        config.displayPosition.renderRenderables(
            if (!widgetActive) widgetErrorGUI else display(),
            posLabel = "Dragon Weight",
        )
    }

    private fun display() = listOf(
        Renderable.hoverTips(
            "§6Current Weight: §f${
                calculateDragonWeight(yourEyes, currentPlace ?: 6, currentTopDamage, currentDamage)
                    .round(1).addSeparators()
            }",
            listOf(
                "Eyes: $yourEyes",
                "Place: ${currentPlace ?: if (currentDamage != 0.0) "unknown, assuming 6th" else "not damaged yet"}",
                "Damage Ratio: ${formatPercentage(currentDamage / (currentTopDamage.takeIf { it != 0.0 } ?: 1.0))}%",
            ),
        ),
    )

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        reset()
        egg = true
    }
}
