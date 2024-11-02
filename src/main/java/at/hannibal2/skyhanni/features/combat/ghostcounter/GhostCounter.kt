package at.hannibal2.skyhanni.features.combat.ghostcounter

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.combat.ghostcounter.GhostCounterConfig.GhostDisplayEntry
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.data.SkillExperience
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.PurseChangeCause
import at.hannibal2.skyhanni.events.PurseChangeEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.events.skyblock.GraphAreaChangeEvent
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostData.Option
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostData.Option.KILLS
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostData.bestiaryData
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostUtil.formatBestiary
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostUtil.formatText
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostUtil.isUsingCTGhostCounter
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostUtil.preFormat
import at.hannibal2.skyhanni.features.combat.ghostcounter.GhostUtil.prettyTime
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.getBazaarData
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ChatUtils.chat
import at.hannibal2.skyhanni.utils.CollectionUtils.addAsSingletonList
import at.hannibal2.skyhanni.utils.CombatUtils._isKilling
import at.hannibal2.skyhanni.utils.CombatUtils.calculateETA
import at.hannibal2.skyhanni.utils.CombatUtils.calculateXP
import at.hannibal2.skyhanni.utils.CombatUtils.interp
import at.hannibal2.skyhanni.utils.CombatUtils.isKilling
import at.hannibal2.skyhanni.utils.CombatUtils.killGainHour
import at.hannibal2.skyhanni.utils.CombatUtils.killGainHourLast
import at.hannibal2.skyhanni.utils.CombatUtils.lastKillUpdate
import at.hannibal2.skyhanni.utils.CombatUtils.lastUpdate
import at.hannibal2.skyhanni.utils.CombatUtils.xpGainHour
import at.hannibal2.skyhanni.utils.CombatUtils.xpGainHourLast
import at.hannibal2.skyhanni.utils.ConfigUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.NumberUtil.formatLong
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import com.google.gson.JsonPrimitive
import io.github.moulberry.notenoughupdates.util.XPInformation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@SkyHanniModule
object GhostCounter {

    val config get() = SkyHanniMod.feature.combat.ghostCounter
    val storage get() = ProfileStorageData.profileSpecific?.ghostCounter
    private var display = emptyList<List<Any>>()
    var ghostCounterV3File =
        File(FilenameUtils.separatorsToSystem("./config/ChatTriggers/modules/GhostCounterV3/.persistantData.json"))

    private val patternGroup = RepoPattern.group("combat.ghostcounter")
    private val skillXPPattern by patternGroup.pattern(
        "skillxp",
        "[+](?<gained>[0-9,.]+) \\((?<current>[0-9,.]+)(?:/(?<total>[0-9,.]+))?\\)",
    )

    @Suppress("MaxLineLength")
    private val combatSectionPattern by patternGroup.pattern(
        "combatsection",
        ".*[+](?<gained>[0-9,.]+) (?<skillName>[A-Za-z]+) \\((?<progress>(?<current>[0-9.,]+)/(?<total>[0-9.,]+)|(?<percent>[0-9.]+)%)\\).*",
    )
    private val killComboExpiredPattern by patternGroup.pattern(
        "killcomboexpired",
        "§cYour Kill Combo has expired! You reached a (?<combo>.*) Kill Combo!",
    )
    private val ghostXPPattern by patternGroup.pattern(
        "ghostxp",
        "(?<current>\\d+(?:\\.\\d+)?(?:,\\d+)?[kK]?)/(?<total>\\d+(?:\\.\\d+)?(?:,\\d+)?[kKmM]?)",
    )
    private val bestiaryPattern by patternGroup.pattern(
        "bestiary",
        ".*(?:§\\d|§\\w)+BESTIARY (?:§\\d|§\\w)+Ghost (?:§\\d|§\\w)(?<previousLevel>\\d+)➜(?:§\\d|§\\w)(?<nextLevel>\\d+).*",
    )
    private val skillLevelPattern by patternGroup.pattern(
        "skilllevel",
        ".*§e§lSkills: §r§a(?<skillName>.*) (?<skillLevel>\\d+).*",
    )

    private val format = NumberFormat.getInstance()
    private var percent: Float = 0.0f
    private var totalSkillXp = 0
    private var currentSkillXp = 0.0f
    private var skillText = ""
    private var lastParsedSkillSection = ""
    private var lastSkillProgressString: String? = null
    private var lastXp: Long = 0
    private var gain: Int = 0
    private var num: Double = 0.0
    private var inMist = false
    private var notifyCTModule = true
    var bestiaryCurrentKill = 0
    private var killETA = ""
    private var currentSkill = ""
    private var currentSkillLevel = -1
    private const val CONFIG_VALUE_VERSION = 1
    private val SORROW = "SORROW".asInternalName()
    private val PLASMA = "PLASMA".asInternalName()
    private val VOLTA = "VOLTA".asInternalName()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        if (config.onlyOnMist && !inMist) return
        config.position.renderStringsAndItems(
            display,
            extraSpace = config.extraSpace,
            posLabel = "Ghost Counter",
        )
    }

    private fun formatDisplay(map: List<List<Any>>): List<List<Any>> {
        val newList = mutableListOf<List<Any>>()
        for (index in config.ghostDisplayText) {
            // TODO, change functionality to use enum rather than ordinals
            newList.add(map[index.ordinal])
        }
        return newList
    }

    fun update() {
        display = formatDisplay(drawDisplay())
    }

    private fun drawDisplay() = buildList<List<Any>> {
        val textFormatting = config.textFormatting
        val ghostKillPerSorrow: Int = when (Option.SORROWCOUNT.get()) {
            0.0 -> 0
            else -> "${((((KILLS.get() / Option.SORROWCOUNT.get()) + Math.ulp(1.0)) * 100) / 100).roundToInt()}".toInt()
        }
        val avgMagicFind = when (Option.TOTALDROPS.get()) {
            0.0 -> "0"
            else -> {
                val mf = (((storage?.totalMF!! / Option.TOTALDROPS.get()) + Math.ulp(1.0)) * 100) / 100
                mf.roundTo(2).toString()
            }
        }

        val xpHourFormatting = textFormatting.xpHourFormatting
        val xpInterp: Float
        val xp = if (xpGainHourLast == xpGainHour && xpGainHour <= 0) {
            xpHourFormatting.noData
        } else {
            xpInterp = interp(xpGainHour, xpGainHourLast, lastUpdate)
            val part = "([0-9]{3,}[^,]+)".toRegex().find(format.format(xpInterp))?.groupValues?.get(1) ?: "N/A"
            "$part ${if (isKilling) "" else xpHourFormatting.paused}"
        }

        val killHourFormatting = textFormatting.killHourFormatting
        val killHour: String
        var killInterp: Long = 0
        if (killGainHourLast == killGainHour && killGainHour <= 0) {
            killHour = killHourFormatting.noData
        } else {
            killInterp = interp(killGainHour.toFloat(), killGainHourLast.toFloat(), lastKillUpdate).toLong()
            killHour = "${format.format(killInterp)} ${if (_isKilling) "" else killHourFormatting.paused}"
        }

        val bestiaryFormatting = textFormatting.bestiaryFormatting
        val currentKill = storage?.bestiaryCurrentKill?.toInt() ?: 0
        val killNeeded = storage?.bestiaryKillNeeded?.toInt() ?: 0
        val nextLevel = storage?.bestiaryNextLevel?.toInt() ?: -1
        val bestiary = if (config.showMax) {
            when (nextLevel) {
                26 -> bestiaryFormatting.maxed.replace("%currentKill%", currentKill.addSeparators())
                in 1..25 -> {
                    val sum = bestiaryData.filterKeys { it <= nextLevel - 1 }.values.sum()

                    val cKill = sum + currentKill
                    bestiaryCurrentKill = cKill
                    bestiaryFormatting.showMax_progress
                }

                else -> bestiaryFormatting.openMenu
            }
        } else {
            when (nextLevel) {
                26 -> bestiaryFormatting.maxed
                in 1..25 -> bestiaryFormatting.progress
                else -> bestiaryFormatting.openMenu
            }
        }

        val etaFormatting = textFormatting.etaFormatting
        val remaining: Int = when (config.showMax) {
            true -> 100_000 - bestiaryCurrentKill
            false -> killNeeded - currentKill
        }

        val eta = if (remaining < 0) {
            etaFormatting.maxed
        } else {
            if (killGainHour < 1) {
                etaFormatting.noData
            } else {
                val timeMap = prettyTime(remaining.toLong() * 1000 * 60 * 60 / killInterp)
                val time = buildString {
                    if (timeMap.isNotEmpty()) {
                        val formatMap = mapOf(
                            "%days%" to "days",
                            "%hours%" to "hours",
                            "%minutes%" to "minutes",
                            "%seconds%" to "seconds",
                        )
                        for ((format, key) in formatMap) {
                            if (etaFormatting.time.contains(format)) {
                                timeMap[key]?.let { value ->
                                    append("$value${format[1]}")
                                }
                            }
                        }
                    } else {
                        append("§cEnded!")
                    }
                }
                killETA = time
                etaFormatting.progress + if (_isKilling) "" else etaFormatting.paused
            }
        }

        addAsSingletonList(textFormatting.titleFormat.replace("&", "§"))
        addAsSingletonList(textFormatting.ghostKilledFormat.formatText(KILLS))
        addAsSingletonList(textFormatting.sorrowsFormat.formatText(Option.SORROWCOUNT))
        addAsSingletonList(textFormatting.ghostSinceSorrowFormat.formatText(Option.GHOSTSINCESORROW.getInt()))
        addAsSingletonList(textFormatting.ghostKillPerSorrowFormat.formatText(ghostKillPerSorrow))
        addAsSingletonList(textFormatting.voltasFormat.formatText(Option.VOLTACOUNT))
        addAsSingletonList(textFormatting.plasmasFormat.formatText(Option.PLASMACOUNT))
        addAsSingletonList(textFormatting.ghostlyBootsFormat.formatText(Option.GHOSTLYBOOTS))
        addAsSingletonList(textFormatting.bagOfCashFormat.formatText(Option.BAGOFCASH))
        addAsSingletonList(textFormatting.avgMagicFindFormat.formatText(avgMagicFind))
        addAsSingletonList(textFormatting.scavengerCoinsFormat.formatText(Option.SCAVENGERCOINS))
        addAsSingletonList(textFormatting.killComboFormat.formatText(Option.MAXKILLCOMBO))
        addAsSingletonList(textFormatting.highestKillComboFormat.formatText(Option.MAXKILLCOMBO))
        addAsSingletonList(textFormatting.skillXPGainFormat.formatText(Option.SKILLXPGAINED))
        addAsSingletonList(
            bestiaryFormatting.base.preFormat(bestiary, nextLevel - 1, nextLevel).formatBestiary(currentKill, killNeeded),
        )

        addAsSingletonList(xpHourFormatting.base.formatText(xp))
        addAsSingletonList(killHourFormatting.base.formatText(killHour))
        addAsSingletonList(etaFormatting.base.formatText(eta).formatText(killETA))

        val rate = 0.12 * (1 + (avgMagicFind.toDouble() / 100))
        val sorrowValue = SORROW.getBazaarData()?.sellOfferPrice?.toLong() ?: 0L
        val final: String = (killInterp * sorrowValue * (rate / 100)).toLong().addSeparators()
        val plasmaValue = PLASMA.getBazaarData()?.sellOfferPrice?.toLong() ?: 0L
        val voltaValue = VOLTA.getBazaarData()?.sellOfferPrice?.toLong() ?: 0L
        var moneyMade: Long = 0
        val priceMap = listOf(
            Triple("Sorrow", Option.SORROWCOUNT.getInt(), sorrowValue),
            Triple("Plasma", Option.PLASMACOUNT.getInt(), plasmaValue),
            Triple("Volta", Option.VOLTACOUNT.getInt(), voltaValue),
            Triple("Bag Of Cash", Option.BAGOFCASH.getInt(), 1_000_000),
            Triple("Scavenger Coins", Option.SCAVENGERCOINS.getInt(), 1),
            Triple("Ghostly Boots", Option.GHOSTLYBOOTS.getInt(), 77_777),
        )
        val moneyMadeTips = buildList {
            for ((name, count, value) in priceMap) {
                moneyMade += (count.toLong() * value.toLong())
                add(
                    "$name: §b${value.addSeparators()} §fx §b${count.addSeparators()} §f= " +
                        "§6${(value.toLong() * count.toLong()).addSeparators()}"
                )
            }
            add("§bTotal: §6${moneyMade.addSeparators()}")
            add("§eClick to copy to clipboard!")
        }
        val moneyMadeWithClickableTips = Renderable.clickAndHover(
            textFormatting.moneyMadeFormat.formatText(moneyMade.addSeparators()),
            moneyMadeTips,
            onClick = { OSUtils.copyToClipboard(moneyMadeTips.joinToString("\n").removeColor()) },
        )
        addAsSingletonList(textFormatting.moneyHourFormat.formatText(final))
        addAsSingletonList(moneyMadeWithClickableTips)
    }

    @SubscribeEvent
    fun onSecondPassed(event: SecondPassedEvent) {
        if (!isEnabled()) return

        skillXPPattern.matchMatcher(skillText) {
            val gained = group("gained").formatDouble()
            val current = group("current").formatLong()
            if (current != lastXp) {
                gain = (current - lastXp).toDouble().roundToInt()
                num = (gain.toDouble() / gained)
                if (gained in 150.0..450.0 && lastXp != 0L && num >= 0) {
                    KILLS.add(num)
                    KILLS.add(num, true)
                    Option.GHOSTSINCESORROW.add(num)
                    Option.KILLCOMBO.add(num)
                    Option.SKILLXPGAINED.add(gained * num.roundToLong())
                    Option.SKILLXPGAINED.add(gained * num.roundToLong(), true)
                    storage?.bestiaryCurrentKill = storage?.bestiaryCurrentKill?.plus(num) ?: num
                }
                lastXp = current
            }
        }

        if (notifyCTModule && ProfileStorageData.profileSpecific?.ghostCounter?.ctDataImported != true) {
            notifyCTModule = false
            if (isUsingCTGhostCounter()) {
                ChatUtils.clickableChat(
                    "GhostCounterV3 ChatTriggers module has been detected, do you want to import saved data? Click here to import data",
                    onClick = {
                        GhostUtil.importCTGhostCounterData()
                    },
                    "§eClick to import data!",
                    prefixColor = "§6",
                    oneTimeClick = true,
                )
            }
        }

        update()

        if (event.repeatSeconds(2)) {
            calculateXP()
            calculateETA()
        }
    }

    @HandleEvent
    fun onAreaChange(event: GraphAreaChangeEvent) {
        inMist = event.area == "The Mist"
    }

    @SubscribeEvent
    fun onActionBarUpdate(event: ActionBarUpdateEvent) {
        if (!isEnabled()) return
        if (!inMist) return
        combatSectionPattern.matchMatcher(event.actionBar) {
            if (group("skillName").lowercase() != "combat") return
            parseCombatSection(event.actionBar)
        }
    }

    private fun parseCombatSection(section: String) {
        val sb = StringBuilder()
        val nf = NumberFormat.getInstance(Locale.US)
        nf.maximumFractionDigits = 2
        if (lastParsedSkillSection == section) {
            sb.append(lastSkillProgressString)
        } else if (combatSectionPattern.matcher(section).find()) {
            combatSectionPattern.matchMatcher(section) {
                sb.append("+").append(group("gained"))
                val skillName = group("skillName")
                val skillPercent = group("percent") != null
                var parse = true
                if (skillPercent) {
                    percent = nf.parse(group("percent")).toFloat()
                    val level = if (currentSkill == "Combat" && currentSkillLevel != -1) currentSkillLevel else XPInformation.getInstance()
                        .getSkillInfo(skillName)?.level ?: 0
                    if (level > 0) {
                        totalSkillXp = SkillExperience.getExpForNextLevel(level)
                        currentSkillXp = totalSkillXp * percent / 100
                    } else {
                        parse = false
                    }
                } else {
                    currentSkillXp = nf.parse(group("current")).toFloat()
                    totalSkillXp = nf.parse(group("total")).toInt()
                }
                percent = 100f.coerceAtMost(percent)
                if (!parse) {
                    sb.append(" (").append(String.format(Locale.US, "%.2f", percent)).append("%)")
                } else {
                    sb.append(" (").append(nf.format(currentSkillXp))
                    if (totalSkillXp != 0) {
                        sb.append("/")
                        sb.append(nf.format(totalSkillXp))
                    }
                    sb.append(")")
                }
                lastParsedSkillSection = section
                lastSkillProgressString = sb.toString()
            }
            if (sb.toString().isNotEmpty()) {
                skillText = sb.toString()
            }
        }
    }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!isEnabled()) return
        for (line in event.tabList) {
            skillLevelPattern.matchMatcher(line) {
                currentSkill = group("skillName")
                currentSkillLevel = group("skillLevel").toInt()
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        for (opt in Option.entries) {
            val pattern = opt.pattern ?: continue
            pattern.matchMatcher(event.message) {
                when (opt) {
                    Option.SORROWCOUNT, Option.VOLTACOUNT, Option.PLASMACOUNT, Option.GHOSTLYBOOTS -> {
                        opt.add(1.0)
                        opt.add(1.0, true)
                        storage?.totalMF =
                            storage?.totalMF?.plus(group("mf").substring(4).toDouble()) ?: group("mf").substring(4).toDouble()
                        Option.TOTALDROPS.add(1.0)
                        if (opt == Option.SORROWCOUNT) Option.GHOSTSINCESORROW.set(0.0)
                        update()
                    }

                    Option.BAGOFCASH -> {
                        Option.BAGOFCASH.add(1.0)
                        Option.BAGOFCASH.add(1.0, true)
                        update()
                    }

                    Option.KILLCOMBOCOINS -> {
                        Option.KILLCOMBOCOINS.set(Option.KILLCOMBOCOINS.get() + group("coin").toDouble())
                        update()
                    }

                    else -> {}
                }
            }
        }
        killComboExpiredPattern.matchMatcher(event.message) {
            if (Option.KILLCOMBO.getInt() > Option.MAXKILLCOMBO.getInt()) {
                Option.MAXKILLCOMBO.set(group("combo").formatDouble())
            }
            if (Option.KILLCOMBO.getInt() > Option.MAXKILLCOMBO.getInt(true)) {
                Option.MAXKILLCOMBO.set(group("combo").formatDouble(), true)
            }
            Option.KILLCOMBOCOINS.set(0.0)
            Option.KILLCOMBO.set(0.0)
            update()
        }
        // replace with BestiaryLevelUpEvent ?
        bestiaryPattern.matchMatcher(event.message) {
            val currentLevel = group("nextLevel").toInt()
            when (val nextLevel = if (currentLevel >= 25) 26 else currentLevel + 1) {
                26 -> {
                    storage?.bestiaryNextLevel = 26.0
                    storage?.bestiaryCurrentKill = 100_000.0
                    storage?.bestiaryKillNeeded = 0.0
                }

                else -> {
                    val killNeeded: Int = bestiaryData[nextLevel] ?: -1
                    storage?.bestiaryNextLevel = nextLevel.toDouble()
                    storage?.bestiaryCurrentKill = 0.0
                    storage?.bestiaryKillNeeded = killNeeded.toDouble()
                }
            }
            update()
        }
    }

    @SubscribeEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!isEnabled()) return
        if (LorenzUtils.skyBlockArea != "The Mist") return
        if (event.reason != PurseChangeCause.GAIN_MOB_KILL) return
        Option.SCAVENGERCOINS.add(event.coins, true)
        Option.SCAVENGERCOINS.add(event.coins)
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val inventoryName = event.inventoryName
        if (inventoryName != "Bestiary ➜ Dwarven Mines") return
        val stacks = event.inventoryItems
        val ghostStack = stacks.values.find { it.displayName.contains("Ghost") } ?: return
        val bestiaryNextLevel =
            if ("§\\wGhost".toRegex().matches(ghostStack.displayName)) 1 else ghostStack.displayName.substring(8).romanToDecimal() + 1
        storage?.bestiaryNextLevel = bestiaryNextLevel.toDouble()
        var kills = 0.0
        for (line in ghostStack.getLore()) {
            val l = line.removeColor().trim()
            if (l.startsWith("Kills: ")) {
                kills = "Kills: (.*)".toRegex().find(l)?.groupValues?.get(1)?.formatDouble() ?: 0.0
            }
            ghostXPPattern.matchMatcher(line.removeColor().trim()) {
                storage?.bestiaryCurrentKill = if (kills > 0) kills else group("current").formatDouble()
                storage?.bestiaryKillNeeded = group("total").formatDouble()
            }
        }
        update()
    }

    @SubscribeEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        if (storage?.configUpdateVersion == 0) {
            config.textFormatting.bestiaryFormatting.base = "  &6Bestiary %display%: &b%value%"
            chat("Your GhostCounter config has been automatically adjusted.")
            storage?.configUpdateVersion = CONFIG_VALUE_VERSION
        }
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(2, "ghostCounter", "combat.ghostCounter")
        event.transform(11, "combat.ghostCounter.ghostDisplayText") { element ->
            ConfigUtils.migrateIntArrayListToEnumArrayList(element, GhostDisplayEntry::class.java)
        }

        event.transform(58, "combat.ghostCounter.textFormatting.bestiaryFormatting.showMax_progress") {
            JsonPrimitive("%currentKill%/100k (%percentNumber%%)")
        }
    }

    fun isEnabled() = IslandType.DWARVEN_MINES.isInIsland() && config.enabled
}
