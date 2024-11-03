package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.events.ActionBarUpdateEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.PlaySoundEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.findMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.replace
import at.hannibal2.skyhanni.utils.RenderUtils.renderStrings
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.TimeUtils.format
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object ArmorStackDisplay {
    private val config get() = SkyHanniMod.feature.combat.armorStackDisplay
    private var stackCount = 0
    private var stackSymbol = ""
    private var stackType = ""
    private var armorPieceCount = 0
    private var display = listOf<String>()
    private var stackDecayTimeCurrent = SimpleTimeMark.farPast()

    private val pistonSounds = setOf("tile.piston.out", "tile.piston.in")

    /**
     * REGEX-TEST: §66,171/4,422❤  §6§l10ᝐ§r     §a1,295§a❈ Defense     §b525/1,355✎ §3400ʬ
     * REGEX-TEST: §66,171/4,422❤  §65ᝐ     §b-150 Mana (§6Wither Impact§b)     §b1,016/1,355✎ §3400ʬ
     */
    private val stackPattern by RepoPattern.pattern(
        "combat.armorstack.actionbar",
        " (?:§6|§6§l)(?<stack>\\d+)(?<symbol>[ᝐ⁑|҉Ѫ⚶])",
    )

    /**
     * REGEX-TEST: §7Health: §a+205, §7Defense: §a+55, §7Intelligence: §a+125, §7Combat Wisdom: §a+0.75,  §8[§8⚔§8] §8[§8⚔§8], , §bArachno Resistance II, §7Grants §a+3❈ Defense §7against §aspiders§7., §bVeteran I, §7Grants §3+0.75☯ Combat Wisdom§7., , §6Tiered Bonus: Arcane Vision (2/4), §7Gives you the ability to see the runic, §7affinity of enemies., §7, §7Using the proper §bRune §7when casting spells, §7from §bRunic Items §7grants 1 stack of §6Arcane, §6Vision Ѫ§7., §7, §7Each §6Arcane Vision Ѫ §7stack grants you §c+2%, §c§7damage on your §bRunic Spells§7., §7, §7At §c10 §7stacks, the spells also explode on hit., §7, §7Lose 1 stack after §c4s §7of not gaining a stack., , §7§8This item can be reforged!, §6§lLEGENDARY LEGGINGS
     */
    private val armorStackTierBonus by RepoPattern.pattern(
        "combat.armorstack.armor",
        "§6Tiered Bonus: (?<type>.*) \\((?<amount>\\d)\\/4\\)"
    )

    @SubscribeEvent
    fun onActionBar(event: ActionBarUpdateEvent) {
        if (!isEnabled()) return
        stackPattern.findMatcher(event.actionBar) {
            updateStack(group("stack").toInt(), group("symbol"))
        } ?: resetStack()

        if (config.showArmorStackCount) event.changeActionBar(event.actionBar.replace(stackPattern, ""))
    }

    @SubscribeEvent
    fun onPlaySound(event: PlaySoundEvent) {
        if (!isEnabled() || !config.armorStackDecayTimer) return
        if (event.soundName in pistonSounds && event.pitch == 1.0f && event.volume == 3.0f) {
            resetDecayTime()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        resetStack()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.position.renderStrings(display, posLabel = "Armor Stack Display")
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled() && !event.isMod(2)) return
        display = drawDisplay()
    }

    private fun drawDisplay(): List<String> {
        if (stackCount == 0 || armorPieceCount < 2) return emptyList()
        val isMaxStack = stackCount == 10

        val decayTimeString = if (config.armorStackDecayTimer) {
            val remainingTime = stackDecayTimeCurrent.timeUntil().coerceAtLeast(0.milliseconds)
            remainingTime.format(showMilliSeconds = true, showSmallerUnits = true)
        } else {
            ""
        }

        val colorCode = when {
            isMaxStack && config.maxStackOnly -> "§b"
            isMaxStack -> "§9"
            else -> "§b"
        }

        return if (config.showInSingleLine) {
            listOf(
                buildString {
                    if (config.showArmorStackCount) {
                        append("§6")
                        if (config.armorStackType) append("$stackType: ")
                        append("§l$stackCount$stackSymbol ")
                    }

                    if (config.armorStackDecayTimer && (!config.maxStackOnly || isMaxStack)) {
                        append("$colorCode($decayTimeString)")
                    }
                }
            )
        } else {
            buildList {
                if (config.showArmorStackCount) {
                    add(
                        buildString {
                            append("§6")
                            if (config.armorStackType) append("$stackType: ")
                            append("§l$stackCount$stackSymbol ")
                        }
                    )
                }

                if (config.armorStackDecayTimer && (!config.maxStackOnly || isMaxStack)) {
                    add("$colorCode$decayTimeString")
                }
            }
        }
    }

    private fun resetDecayTime() {
        armorPieceCount = InventoryUtils.getArmor().firstNotNullOfOrNull { armor ->
            armor?.getLore()?.let {
                armorStackTierBonus.firstMatcher(it) {
                    stackType = group("type")
                    group("amount")
                }?.toInt()
            }
        } ?: 0

        val stackDecayTime = when (armorPieceCount) {
            2 -> 5
            3 -> 8
            4 -> 11
            else -> 0
        }.seconds
        stackDecayTimeCurrent = SimpleTimeMark.now() + stackDecayTime
    }

    private fun resetStack() {
        stackCount = 0
        stackSymbol = ""
    }

    private fun updateStack(newStackCount: Int, newStackSymbol: String) {
        if (stackCount != newStackCount && config.armorStackDecayTimer) resetDecayTime()
        stackCount = newStackCount
        stackSymbol = newStackSymbol
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(65, "combat.stackDisplayConfig", "combat.armorStackDisplay")
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
