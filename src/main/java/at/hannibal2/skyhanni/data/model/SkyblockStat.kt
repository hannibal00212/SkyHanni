package at.hannibal2.skyhanni.data.model

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.intellij.lang.annotations.Language
import java.util.EnumMap
import java.util.regex.Pattern
import kotlin.math.roundToInt

@Suppress("MaxLineLength")
enum class SkyblockStat(
    val icon: String,
    @Language("RegExp") tabListPatternS: String,
    @Language("RegExp") menuPatternS: String,
) {
    DAMAGE("§c❁", "", ""), // Weapon only
    HEALTH("§c❤", " Health: §r§c❤(?<value>\\d+)(?: .*)?", " §c❤ Health §f(?<value>\\d+)(?: .*)?"), // TODO get from action bar
    DEFENSE("§a❈", " Defense: §r§a❈(?<value>\\d+)(?: .*)?", " §a❈ Defense §f(?<value>\\d+)(?: .*)?"), // TODO get from action bar
    STRENGTH("§c❁", " Strength: §r§c❁(?<value>\\d+)(?: .*)?", " §c❁ Strength §f(?<value>\\d+)(?: .*)?"),
    INTELLIGENCE("§b✎", " Intelligence: §r§b✎(?<value>\\d+)(?: .*)?", " §b✎ Intelligence §f(?<value>\\d+)(?: .*)?"), // TODO get from action bar
    CRIT_DAMAGE("§9☠", " Crit Damage: §r§9☠(?<value>\\d+)(?: .*)?", " §9☠ Crit Damage §f(?<value>\\d+)(?: .*)?"),
    CRIT_CHANCE("§9☣", " Crit Chance: §r§9☣(?<value>\\d+)(?: .*)?", " §9☣ Crit Chance §f(?<value>\\d+)(?: .*)?"),
    FEROCITY("§c⫽", " Ferocity: §r§c⫽(?<value>\\d+)(?: .*)?", " §c⫽ Ferocity §f(?<value>\\d+)(?: .*)?"),
    BONUS_ATTACK_SPEED("§e⚔", " Attack Speed: §r§e⚔(?<value>\\d+)(?: .*)?", " §e⚔ Bonus Attack Speed §f(?<value>\\d+)(?: .*)?"),
    ABILITY_DAMAGE("§c๑", " Ability Damage: §r§c๑(?<value>\\d+)(?: .*)?", " §c๑ Ability Damage §f(?<value>\\d+)(?: .*)?"),
    HEALTH_REGEN("§c❣", " Health Regen: §r§c❣(?<value>\\d+)(?: .*)?", " §c❣ Health Regen §f(?<value>\\d+)(?: .*)?"),
    VITALITY("§4♨", " Vitality: §r§4♨(?<value>\\d+)(?: .*)?", " §4♨ Vitality §f(?<value>\\d+)(?: .*)?"),
    MENDING("§a☄", " Mending: §r§a☄(?<value>\\d+)(?: .*)?", " §a☄ Mending §f(?<value>\\d+)(?: .*)?"),
    TRUE_DEFENSE("§7❂", " True Defense: §r§f❂(?<value>\\d+)(?: .*)?", " §f❂ True Defense §f(?<value>\\d+)(?: .*)?"),
    SWING_RANGE("§eⓈ", " Swing Range: §r§eⓈ(?<value>\\d+)(?: .*)?", " §eⓈ Swing Range §f(?<value>\\d+)(?: .*)?"),
    SPEED("§f✦", " Speed: §r§f✦(?<value>\\d+)(?: .*)?", " §f✦ Speed §f(?<value>\\d+)(?: .*)?"), // TODO add the way sba did get it (be careful with 500+ Speed)
    SEA_CREATURE_CHANCE("§3α", " Sea Creature Chance: §r§3α(?<value>\\d+)(?: .*)?", " §3α Sea Creature Chance §f(?<value>\\d+)(?: .*)?"),
    MAGIC_FIND("§b✯", " Magic Find: §r§b✯(?<value>\\d+)(?: .*)?", " §b✯ Magic Find §f(?<value>\\d+)(?: .*)?"),
    PET_LUCK("§d♣", " Pet Luck: §r§d♣(?<value>\\d+)(?: .*)?", " §d♣ Pet Luck §f(?<value>\\d+)(?: .*)?"),
    FISHING_SPEED("§b☂", " Fishing Speed: §r§b☂(?<value>\\d+)(?: .*)?", " §b☂ Fishing Speed §f(?<value>\\d+)(?: .*)?"),
    DOUBLE_HOOK_CHANCE("§9⚓", " Double Hook Chance: §r§9⚓(?<value>\\d+)(?: .*)?", ""),
    BONUS_PEST_CHANCE("§2ൠ", " (?:§r§7§m)?Bonus Pest Chance: (?:§r§2)?ൠ(?<value>\\d+)(?: .*)?", " (?:§7§m|§2)ൠ Bonus Pest Chance (?:§f)?(?<value>\\d+)(?: .*)?"),
    COMBAT_WISDOM("§3☯", " Combat Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Combat Wisdom §f(?<value>\\d+)(?: .*)?"),
    MINING_WISDOM("§3☯", " Mining Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Mining Wisdom §f(?<value>\\d+)(?: .*)?"),
    FARMING_WISDOM("§3☯", " Farming Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Farming Wisdom §f(?<value>\\d+)(?: .*)?"),
    FORAGING_WISDOM("§3☯", " Foraging Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Foraging Wisdom §f(?<value>\\d+)(?: .*)?"),
    FISHING_WISDOM("§3☯", " Fishing Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Fishing Wisdom §f(?<value>\\d+)(?: .*)?"),
    ENCHANTING_WISDOM("§3☯", " Enchanting Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Enchanting Wisdom §f(?<value>\\d+)(?: .*)?"),
    ALCHEMY_WISDOM("§3☯", " Alchemy Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Alchemy Wisdom §f(?<value>\\d+)(?: .*)?"),
    CARPENTRY_WISDOM("§3☯", " Carpentry Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Carpentry Wisdom §f(?<value>\\d+)(?: .*)?"),
    RUNECRAFTING_WISDOM("§3☯", " Runecrafting Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Runecrafting Wisdom §f(?<value>\\d+)(?: .*)?"),
    SOCIAL_WISDOM("§3☯", " Social Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Social Wisdom §f(?<value>\\d+)(?: .*)?"),
    TAMING_WISDOM("§3☯", " Taming Wisdom: §r§3☯(?<value>\\d+)(?: .*)?", " §3☯ Taming Wisdom §f(?<value>\\d+)(?: .*)?"),
    MINING_SPEED("§6⸕", " Mining Speed: §r§6⸕(?<value>\\d+)(?: .*)?", " §6⸕ Mining Speed §f(?<value>\\d+)(?: .*)?"),
    BREAKING_POWER("§2Ⓟ", "", " §2Ⓟ Breaking Power §f(?<value>\\d+)(?: .*)?"),
    PRISTINE("§5✧", " Pristine: §r§5✧(?<value>\\d+)(?: .*)?", " §5✧ Pristine §f(?<value>\\d+)(?: .*)?"),
    FORAGING_FORTUNE("§☘", " Foraging Fortune: §r§6☘(?<value>\\d+)(?: .*)?", " §6☘ Foraging Fortune §f(?<value>\\d+)(?: .*)?"),
    FARMING_FORTUNE("§6☘", " (?:§r§7§m)?Farming Fortune: (?:§r§6)?☘(?<value>\\d+)(?: .*)?", " (?:§7§m|§6)☘ Farming Fortune (?:§f)?(?<value>\\d+)(?: .*)?"),
    MINING_FORTUNE("§6☘", " Mining Fortune: §r§6☘(?<value>\\d+)(?: .*)?", " §6☘ Mining Fortune §f(?<value>\\d+)(?: .*)?"),
    FEAR("§5☠", " Fear: §r§5☠(?<value>\\d+)(?: .*)?", " §5☠ Fear §f(?<value>\\d+)(?: .*)?"),
    COLD_RESISTANCE("§b❄", " Cold Resistance: §r§b❄(?<value>\\d+)(?: .*)?", ""),
    WHEAT_FORTUNE("§7☘", "", " §7§m☘ Wheat Fortune (?<value>\\d+)(?: .*)?"),
    CARROT_FORTUNE("§7☘", "", " §7§m☘ Carrot Fortune (?<value>\\d+)(?: .*)?"),
    POTATO_FORTUNE("§7☘", "", " §7§m☘ Potato Fortune (?<value>\\d+)(?: .*)?"),
    PUMPKIN_FORTUNE("§7☘", "", " §7§m☘ Pumpkin Fortune (?<value>\\d+)(?: .*)?"),
    MELON_FORTUNE("§7☘", "", " §7§m☘ Melon Fortune (?<value>\\d+)(?: .*)?"),
    MUSHROOM_FORTUNE("§7☘", "", " §7§m☘ Mushroom Fortune (?<value>\\d+)(?: .*)?"),
    CACTUS_FORTUNE("§7☘", "", " §7§m☘ Cactus Fortune (?<value>\\d+)(?: .*)?"),
    NETHER_WART_FORTUNE("§7☘", "", " §7§m☘ Nether Wart Fortune (?<value>\\d+)(?: .*)?"),
    COCOA_BEANS_FORTUNE("§7☘", "", " §7§m☘ Cocoa Beans Fortune (?<value>\\d+)(?: .*)?"),
    SUGAR_CANE_FORTUNE("§7☘", "", " §7§m☘ Sugar Cane Fortune (?<value>\\d+)(?: .*)?"),

    MINING_SPREAD("§e▚", " (§r§7§m)?Mining Spread: (§r§e)?▚(?<value>\\d+)(?: .*)?", " (§7§m|§e)▚ Mining Spread (§f)?(?<value>\\d+)(?: .*)?"),
    GEMSTONE_SPREAD("§e▚", " (§r§7§m)?Mining Spread: (§r§e)?▚(?<value>\\d+)(?: .*)?", " (§7§m|§e)▚ Gemstone Spread (§f)?(?<value>\\d+)(?: .*)?"),
    ORE_FORTUNE("§6☘", " Ore Fortune: §r§6☘(?<value>\\d+)(?: .*)?", " §6☘ Ore Fortune §f103"),
    DWARVEN_METAL_FORTUNE("§6☘", " Dwarven Metal Fortune: §r§6☘(?<value>\\d+)(?: .*)?", " §6☘ Dwarven Metal Fortune §f(?<value>\\d+)(?: .*)?"),
    BLOCK_FORTUNE("§6☘", " Block Fortune: §r§6☘(?<value>\\d+)(?: .*)?", " §6☘ Block Fortune §f(?<value>\\d+)(?: .*)?"),
    GEMSTONE_FORTUNE("§6☘", " Gemstone Fortune: §r§6☘(?<value>\\d+)(?: .*)?", " §6☘ Gemstone Fortune §f(?<value>\\d+)(?: .*)?"),
    HEAT_RESISTANCE("§c♨", " Heat Resistance: §r§c♨(?<value>\\d+)(?: .*)?", " §c♨ Heat Resistance §f(?<value>\\d+)(?: .*)?"),

    UNKNOWN("§c?", "", "")
    ;

    var lastKnownValue: Double?
        get() = ProfileStorageData.profileSpecific?.stats?.get(this)
        set(value) {
            ProfileStorageData.profileSpecific?.stats?.set(this, value)
        }

    var lastSource: StatSourceType = StatSourceType.UNKNOWN

    private val capitalizedName = name.lowercase().allLettersFirstUppercase()

    val iconWithName = "$icon $capitalizedName"

    private val keyName = name.lowercase().replace('_', '.')

    val displayValue get() = lastKnownValue?.let { icon + it.roundToInt() }

    val tablistPattern by RepoPattern.pattern("stats.tablist.$keyName", tabListPatternS)
    val menuPattern by RepoPattern.pattern("stats.menu.$keyName", menuPatternS)

    fun asString(value: Int) = (if (value > 0) "+" else "") + value.toString() + " " + this.icon

    @SkyHanniModule
    companion object {

        val fontSizeOfLargestIcon by lazy {
            entries.maxOf { Minecraft.getMinecraft().fontRendererObj.getStringWidth(it.icon) } + 1
        }

        fun getValueOrNull(string: String): SkyblockStat? = entries.firstOrNull { it.name == string }

        fun getValue(string: String): SkyblockStat = getValueOrNull(string) ?: UNKNOWN

        init {
            entries.forEach {
                it.tablistPattern
                it.menuPattern
            }
        }

        @SubscribeEvent
        fun onInventory(event: InventoryFullyOpenedEvent) {
            if (!LorenzUtils.inSkyBlock) return
            onSkyblockMenu(event)
            onStatsMenu(event)
        }

        private const val PLAYER_STATS_SLOT_INDEX = 13

        private fun onSkyblockMenu(event: InventoryFullyOpenedEvent) {
            if (event.inventoryName != "SkyBlock Menu") return
            val list = event.inventoryItems[PLAYER_STATS_SLOT_INDEX]?.getLore() ?: return
            DelayedRun.runNextTick { // Delayed to not impact opening time
                assignEntry(list, StatSourceType.SKYBLOCK_MENU) { it.menuPattern }
            }
        }

        private val statsMenuRelevantSlotIndexes = listOf(15, 16, 24, 25)

        private fun onStatsMenu(event: InventoryFullyOpenedEvent) {
            if (event.inventoryName != "Your Equipment and Stats") return
            val list = statsMenuRelevantSlotIndexes.mapNotNull { event.inventoryItems[it]?.getLore() }.flatten()
            if (list.isEmpty()) return
            DelayedRun.runNextTick { // Delayed to not impact opening time
                assignEntry(list, StatSourceType.STATS_MENU) { it.menuPattern }
            }
        }

        @HandleEvent
        fun onTabList(event: WidgetUpdateEvent) {
            if (!event.isWidget(TabWidget.STATS, TabWidget.DUNGEON_SKILLS_AND_STATS)) return
            val type = if (event.isWidget(TabWidget.DUNGEON_SKILLS_AND_STATS)) StatSourceType.TABLIST_DUNGEON else StatSourceType.TABLIST
            assignEntry(event.lines, type) { it.tablistPattern }
        }

        private fun assignEntry(lines: List<String>, type: StatSourceType, pattern: (SkyblockStat) -> Pattern) {
            for (line in lines) for (entry in entries) {
                val matchResult = pattern(entry).matchMatcher(line) {
                    groupOrNull("value")?.replace("[,%]".toRegex(), "")?.toDouble()
                } ?: continue
                entry.lastKnownValue = matchResult
                entry.lastSource = type
                break // Exit the inner loop once a match is found
            }
        }

        @HandleEvent
        fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
            event.move(69, "#profile.stats.TRUE_DEFENCE", "#profile.stats.TRUE_DEFENSE")
        }
    }
}

class SkyblockStatList : EnumMap<SkyblockStat, Double>(SkyblockStat::class.java), Map<SkyblockStat, Double> {
    operator fun minus(other: SkyblockStatList): SkyblockStatList {
        return SkyblockStatList().apply {
            val keys = this.keys + other.keys
            for (key in keys) {
                this[key] = (this@SkyblockStatList[key] ?: 0.0) - (other[key] ?: 0.0)
            }
        }
    }

    companion object {
        fun mapOf(vararg list: Pair<SkyblockStat, Double>) = SkyblockStatList().apply {
            for ((key, value) in list) {
                this[key] = value
            }
        }
    }
}

enum class StatSourceType {
    UNKNOWN,
    SKYBLOCK_MENU,
    STATS_MENU,
    TABLIST,
    TABLIST_DUNGEON,
}
