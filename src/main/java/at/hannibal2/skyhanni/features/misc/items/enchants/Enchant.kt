package at.hannibal2.skyhanni.features.misc.items.enchants

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.chroma.ChromaManager
import at.hannibal2.skyhanni.utils.ItemUtils.extraAttributes
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzUtils.round
import at.hannibal2.skyhanni.utils.NumberUtil.shortFormat
import at.hannibal2.skyhanni.utils.StringUtils.insert
import at.hannibal2.skyhanni.utils.StringUtils.splitCamelCase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import com.google.gson.annotations.Expose
import net.minecraft.item.ItemStack
import io.github.notenoughupdates.moulconfig.observer.Property
import java.util.TreeSet

open class Enchant : Comparable<Enchant> {
    @Expose
    var nbtName = ""

    @Expose
    var loreName = ""

    @Expose
    private var goodLevel = 0

    @Expose
    private var maxLevel = 0

    private fun isNormal() = this is Normal
    private fun isUltimate() = this is Ultimate
    private fun isStacking() = this is Stacking

    open fun getFormattedName(level: Int, itemStack: ItemStack?) = getFormat(level, itemStack) + loreName

    open fun getFormat(level: Int, itemStack: ItemStack? = null): String {
        val config = SkyHanniMod.feature.inventory.enchantParsing

        // TODO change color to string (support for bold)
        var color = when {
            level >= maxLevel -> config.perfectEnchantColor
            level > goodLevel -> config.greatEnchantColor
            level == goodLevel -> config.goodEnchantColor
            else -> config.poorEnchantColor
        }

        // Exceptions
        color = checkExceptions(color, level, itemStack)

        // TODO when chroma is disabled maybe use the neu chroma style instead of gold
        if (color.get() == LorenzColor.CHROMA && !(ChromaManager.config.enabled.get() || EnchantParser.isSbaLoaded)) return "§6§l"
        return color.get().getChatColor()
    }

    /**
     * Method to check for certain or unique exceptions that need to be handled explicitly.
     *
     * *(There isn't much of a convention to adding exceptions, except try to include relevant exceptions under
     * a corresponding enchantment conditional, unless the exception is not specific to a certain enchant. i.e.
     * Efficiency exceptions should be within the `if (this.nbtName == "efficiency")` conditional)*
     *
     * @param color The original coloring based on default behaviour, for when no exception is met
     * @param level The level of the enchant currently being parsed
     * @param itemStack The ItemStack of the hovered item. Can be null, e.g. when hovering over `/show` items
     */
    private fun checkExceptions(color: Property<LorenzColor>, level: Int, itemStack: ItemStack?): Property<LorenzColor> {
        val config = SkyHanniMod.feature.inventory.enchantParsing

        val itemCategory = itemStack?.getItemCategoryOrNull()
        val internalName = itemStack?.getInternalNameOrNull()
        val itemName = internalName?.itemName?.removeColor()

        if (this.nbtName == "efficiency") {
            // If the item is a Stonk, or a non-mining tool with Efficiency 5 (whilst not being a Promising Shovel),
            // color the enchant as max
            if (itemName == "Stonk" ||
                (itemCategory != null && !ItemCategory.miningTools.contains(itemCategory) && level == 5 && itemName != "Promising Shovel")
            ) {
                return config.perfectEnchantColor
            }
        }

        return color
    }

    override fun toString() = "$nbtName $goodLevel $maxLevel\n"

    override fun compareTo(other: Enchant): Int {
        if (this.isUltimate() == other.isUltimate()) {
            if (this.isStacking() == other.isStacking()) {
                return this.loreName.compareTo(other.loreName)
            }
            return if (this.isStacking()) -1 else 1
        }
        return if (this.isUltimate()) -1 else 1
    }

    class Normal : Enchant()

    class Ultimate : Enchant() {
        override fun getFormat(level: Int, itemStack: ItemStack?) = "§d§l"
    }

    class Stacking : Enchant() {
        @Expose
        private var nbtNum: String? = null

        @Expose
        @Suppress("UnusedPrivateProperty")
        private val statLabel: String? = null

        @Expose
        private var stackLevel: TreeSet<Int>? = null

        override fun toString() = "$nbtNum $stackLevel ${super.toString()}"

        fun progressString(item: ItemStack): String {
            val nbtKey = nbtNum ?: return ""
            val levels = stackLevel ?: return ""
            val label = statLabel?.splitCamelCase()?.replaceFirstChar { it.uppercase() }?.replace("Xp", "XP") ?: return ""
            val progress = item.extraAttributes.getDouble(nbtKey).round(0).toInt()
            if (progress == 0) return ""
            val nextLevel = levels.higher(progress)
            val tail = nextLevel?.shortFormat()?.insert(0, "/ ") ?: "(Maxed)"
            return "§7$label: §c${progress.shortFormat()} §7$tail"
        }
    }

    class Dummy(name: String) : Enchant() {
        init {
            loreName = name
            nbtName = name
        }

        // Ensures enchants not yet in repo stay as vanilla formatting
        // (instead of that stupid dark red lowercase formatting *cough* sba *cough*)
        override fun getFormattedName(level: Int, itemStack: ItemStack?): String = "§9$loreName"
    }
}
