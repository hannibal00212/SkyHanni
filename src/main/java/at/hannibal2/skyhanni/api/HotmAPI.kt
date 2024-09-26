package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.HotmData
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.mining.PowderGainEvent
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getDrillUpgrades
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.seconds

object HotmAPI {

    fun copyCurrentTree() = HotmData.storage?.deepCopy()

    val activeMiningAbility get() = HotmData.abilities.firstOrNull { it.enabled }

    private val blueGoblinEgg = "GOBLIN_OMELETTE_BLUE_CHEESE".asInternalName()

    private val blueEggCache = TimeLimitedCache<ItemStack, Boolean>(10.0.seconds)
    val isBlueEggActive
        get() = InventoryUtils.getItemInHand()?.let {
            blueEggCache.getOrPut(it) {
                it.getItemCategoryOrNull() == ItemCategory.DRILL && it.getDrillUpgrades()
                    ?.contains(blueGoblinEgg) == true
            }
        } == true

    enum class PowderType(val displayName: String, val color: String) {
        MITHRIL("Mithril", "§2"),
        GEMSTONE("Gemstone", "§d"),
        GLACITE("Glacite", "§b"),

        ;

        val heartPattern by RepoPattern.pattern(
            "inventory.${name.lowercase()}.heart",
            "§7$displayName Powder: §a§.(?<powder>[\\d,]+)"
        )
        val resetPattern by RepoPattern.pattern(
            "inventory.${name.lowercase()}.reset",
            "\\s+§8- §.(?<powder>[\\d,]+) $displayName Powder"
        )

        fun pattern(isHeart: Boolean) = if (isHeart) heartPattern else resetPattern

        fun getStorage() = ProfileStorageData.profileSpecific?.mining?.powder?.get(this)

        fun getCurrent() = getStorage()?.available ?: 0L

        fun setCurrent(value: Long) {
            getStorage()?.available = value
        }

        fun addCurrent(value: Long) {
            setCurrent(getCurrent() + value)
        }

        fun getTotal() = getStorage()?.total ?: 0L

        fun setTotal(value: Long) {
            getStorage()?.total = value
        }

        fun addTotal(value: Long) {
            setTotal(getTotal() + value)
        }

        /** Use when new powder gets collected*/
        fun gain(difference: Long) {
            ChatUtils.debug("Gained §a${difference.addSeparators()} §e${displayName} Powder")
            addTotal(difference)
            addCurrent(difference)
            PowderGainEvent(this, difference).post()
        }

        fun reset() {
            setCurrent(0)
            setTotal(0)
        }
    }

    var skymall: SkymallPerk? = null

    var mineshaftMayhem: MayhemPerk? = null

    enum class SkymallPerk(chat: String, itemString: String) {
        MINING_SPEED("Gain §r§6\\+100⸕ Mining Speed§r§f\\.", "Gain §6\\+100⸕ Mining Speed§7\\."),
        MINING_FORTUNE("Gain §r§6\\+50☘ Mining Fortune§r§f\\.", "Gain §6\\+50☘ Mining Fortune§7\\."),
        EXTRA_POWDER("Gain §r§a\\+15% §r§fmore Powder while mining\\.", "Gain §a\\+15% §7more Powder while mining\\."),
        ABILITY_COOLDOWN("§r§a-20%§r§f Pickaxe Ability cooldowns\\.", "§a-20%§7 Pickaxe Ability cooldowns\\."),
        GOBLIN_CHANCE("§r§a10x §r§fchance to find Golden and Diamond Goblins\\.", "§a10x §7chance to find Golden and"),
        TITANIUM("Gain §r§a5x §r§9Titanium §r§fdrops", "Gain §a5x §9Titanium §7drops\\.")
        ;

        private val patternName = name.lowercase().replace("_", ".")

        val chatPattern by RepoPattern.pattern("mining.hotm.skymall.chat.$patternName", chat)
        val itemPattern by RepoPattern.pattern("mining.hotm.skymall.item.$patternName", itemString)
    }

    enum class MayhemPerk(chat: String) {
        SCRAP_CHANCE("Your §r§9Suspicious Scrap §r§7chance was buffed by your §r§aMineshaft Mayhem §r§7perk!"),
        MINING_FORTUNE("You received a §r§a§r§6☘ Mining Fortune §r§7buff from your §r§aMineshaft Mayhem §r§7perk!"),
        MINING_SPEED("You received a §r§a§r§6⸕ Mining Speed §r§7buff from your §r§aMineshaft Mayhem §r§7perk!"),
        COLD_RESISTANCE("You received a §r§a§r§b❄ Cold Resistance §r§7buff from your §r§aMineshaft Mayhem §r§7perk!"),
        ABILITY_COOLDOWN("Your Pickaxe Ability cooldown was reduced §r§7from your §r§aMineshaft Mayhem §r§7perk!");

        private val patternName = name.lowercase().replace("_", ".")

        val chatPattern by RepoPattern.pattern("mining.hotm.mayhem.chat.$patternName", chat)
    }
}
