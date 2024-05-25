package at.hannibal2.skyhanni.api

import at.hannibal2.skyhanni.data.HotmData
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getDrillUpgrades
import at.hannibal2.skyhanni.utils.StringUtils.firstLetterUppercase
import at.hannibal2.skyhanni.utils.TimeLimitedCache
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.seconds

object HotmAPI {

    private val repoGroup = RepoPattern.group("data.hotmapi")

    private val hotmGuiPattern by repoGroup.pattern(
        "gui.name",
        "Heart of the Mountain"
    )

    fun copyCurrentTree() = HotmData.storage?.deepCopy()

    val activeMiningAbility get() = HotmData.abilities.firstOrNull { it.enabled }

    private val blueGoblinEgg = "goblin_omelette_blue_cheese".asInternalName()

    private val blueEggCache = TimeLimitedCache<ItemStack, Boolean>(10.0.seconds)
    val isBlueEggActive
        get() = InventoryUtils.getItemInHand()?.let {
            blueEggCache.getOrPut(it) {
                it.getItemCategoryOrNull() == ItemCategory.DRILL && it.getDrillUpgrades()
                    ?.contains(blueGoblinEgg) == true
            }
        } == true

    enum class Powder() {
        MITHRIL,
        GEMSTONE,
        GLACITE,

        ;

        val lowName = name.lowercase().firstLetterUppercase()

        val heartPattern by repoGroup.pattern(
            "inventory.${name.lowercase()}.heart",
            "§7$lowName Powder: §a§.(?<powder>[\\d,]+)"
        )
        val resetPattern by repoGroup.pattern(
            "inventory.${name.lowercase()}.reset",
            "\\s+§8- §.(?<powder>[\\d,]+) $lowName Powder"
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
        fun gain(value: Long) {
            addTotal(value)
            addCurrent(value)
        }

        fun reset() {
            setCurrent(0)
            setTotal(0)
        }
    }

    var skymall: SkymallPerk? = null

    var mineshaftMayhem: MayhemPerk? = null

    enum class SkymallPerk(chat: String, itemString: String) {
        MINING_SPEED("Gain §r§a+100 §r§6⸕ Mining Speed§r§f.", "Gain §a+100 §6⸕ Mining Speed§7."),
        MINING_FORTUNE("Gain §r§a+50 §r§6☘ Mining Fortune§r§f.", "Gain §a+50 §6☘ Mining Fortune§7."),
        EXTRA_POWDER("Gain §r§a+15% §r§fmore Powder while mining.", "Gain §a+15% §7more Powder while mining."),
        ABILITY_COOLDOWN("Reduce Pickaxe Ability cooldown by §r§a20%§r§f.", "Reduce Pickaxe Ability cooldown by"),
        GOBLIN_CHANCE("§r§a10x §r§fchance to find Golden and Diamond Goblins.", "§a10x §7chance to find Golden and"),
        TITANIUM("Gain §r§a5x §r§9Titanium §r§fdrops", "Gain §a+15% §7more Powder while mining.")
        ;

        val chatPattern by RepoPattern.pattern("mining.hotm.skymall.chat.$name", chat)
        val itemPattern by RepoPattern.pattern("mining.hotm.skymall.item.$name", itemString)
    }

    enum class MayhemPerk(chat: String) {
        SCRAP_CHANCE("Your §r§9Suspicious Scrap §r§7chance was buffed by your §r§aMineshaft Mayhem §r§7perk!"),
        MINING_FORTUNE("You received a §r§a§r§6☘ Mining Fortune §r§7buff from your §r§aMineshaft Mayhem §r§7perk!"),
        MINING_SPEED("You received a §r§a§r§6⸕ Mining Speed §r§7buff from your §r§aMineshaft Mayhem §r§7perk!"),
        COLD_RESISTANCE("You received a §r§a§r§b❄ Cold Resistance §r§7buff from your §r§aMineshaft Mayhem §r§7perk!"),
        ABILITY_COOLDOWN("Your Pickaxe Ability cooldown was reduced §r§7from your §r§aMineshaft Mayhem §r§7perk!");

        val chatPattern by RepoPattern.pattern("mining.hotm.mayhem.chat.$name", chat)
    }
}
