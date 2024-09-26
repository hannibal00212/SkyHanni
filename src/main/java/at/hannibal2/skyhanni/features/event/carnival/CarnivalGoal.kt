package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.Perk
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.ProfileJoinEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SkyBlockTime
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.intellij.lang.annotations.Language

private val repoGroup = RepoPattern.group("carnvial.goals")

enum class CarnivalGoal(
    private val type: GoalType,
    @Language("RegEXP") loreLine: String,
    @Language("RegEXP") chatLine: String,
    val display: String,
) {
    FRUIT_DIGGING_PLAY(
        GoalType.FRUIT_DIGGING,
        "§7Play §a3 games §7of §6Fruit Digging§7.",
        "(§8 - §r)?§7Play §r§a3 games §r§7of §r§6Fruit Digging§r§7.",
        "Play §a3 games",
    ),
    FRUIT_DIGGING_SCORE(
        GoalType.FRUIT_DIGGING,
        "§7Reach §a3,000 score §7in a single game",
        "(§8 - §r)?§7Reach §r§a3,000 score §r§7in a single game of §r§6Fruit Digging§r§7.",
        "Reach §a3,000 score",
    ),
    DIG_APPLE(
        GoalType.FRUIT_DIGGING,
        "§7Dig up §a3 Apples §7in a single game of",
        "(§8 - §r)?§7Dig up §r§a3 Apples §r§7in a single game of §r§6Fruit Digging§r§7.",
        "Dig up §a3 Apples",
    ),
    UNIQUE_FRUIT(
        GoalType.FRUIT_DIGGING,
        "§7Dig up §a5 unique Fruits §7in a single",
        "(§8 - §r)?§7Dig up §r§a5 unique Fruits §r§7in a single game of §r§6Fruit Digging§r§7.",
        "Dig up §a5 unique Fruits",
    ),
    DRAGONFRUIT(
        GoalType.FRUIT_DIGGING,
        "§7Dig up a §dDragonfruit§7.",
        "(§8 - §r)?§7Dig up a §r§dDragonfruit§r§7.",
        "Dig up a §dDragonfruit",
    ),
    CATCH_A_FISH_PLAY(
        GoalType.CATCH_A_FISH,
        "§7Play §a3 games §7of §3Catch a Fish§7.",
        "(§8 - §r)?§7Play §r§a3 games §r§7of §r§3Catch a Fish§r§7.",
        "Play §a3 games",
    ),
    CATCH_A_FISH_SCORE(
        GoalType.CATCH_A_FISH,
        "§7Reach §a3,000 score §7in a single game",
        "(§8 - §r)?§7Reach §r§a3,000 score §r§7in a single game of §r§3Catch a Fish§r§7.",
        "Reach §a3,000 score",
    ),
    CATCH_FISH(
        GoalType.CATCH_A_FISH,
        "§7Catch §a30 Fish §7in a single game of",
        "(§8 - §r)?§7Catch §r§a30 Fish §r§7in a single game of §r§3Catch a Fish§r§7.",
        "Catch §a30 Fish",
    ),
    CATCH_YELLOW_FISH(
        GoalType.CATCH_A_FISH,
        "§7Catch §a5 Yellow Fish §7in a single game",
        "(§8 - §r)?§7Catch §r§a5 Yellow Fish §r§7in a single game of §r§3Catch a Fish§r§7.",
        "Catch §a5 Yellow Fish",
    ),
    CATCH_STREAK(
        GoalType.CATCH_A_FISH,
        "§7Reach a §a5 Catch Streak §7in a single",
        "(§8 - §r)?§7Reach a §r§a5 Catch Streak §r§7in a single game of §r§3Catch a Fish§r§7.",
        "Reach a §a5 Catch Streak",
    ),
    ZOMBIE_SHOOTOUT_PLAY(
        GoalType.ZOMBIE_SHOOTOUT,
        "§7Play §a3 games §7of §cZombie Shootout§7.",
        "(§8 - §r)?§7Play §r§a3 games §r§7of §r§cZombie Shootout§r§7.",
        "Play §a3 games",
    ),
    ZOMBIE_SHOOTOUT_SCORE(
        GoalType.ZOMBIE_SHOOTOUT,
        "§7Reach §a3,000 score §7in a single game",
        "(§8 - §r)?§7Reach §r§a3,000 score §r§7in a single game of §r§cZombie Shootout§r§7.",
        "Reach §a3,000 score",
    ),
    SHOOT_ZOMBIE(
        GoalType.ZOMBIE_SHOOTOUT,
        "§7Shoot §a60 Zombies §7in a single game of",
        "(§8 - §r)?§7Shoot §r§a60 Zombies §r§7in a single game of §r§cZombie Shootout§r§7.",
        "Shoot §a60 Zombies",
    ),
    SHOOT_DIAMOND_ZOMBIE(
        GoalType.ZOMBIE_SHOOTOUT,
        "§7Shoot §a5 Diamond Zombies §7in a single",
        "(§8 - §r)?§7Shoot §r§a5 Diamond Zombies §r§7in a single game of §r§cZombie Shootout§r§7.",
        "Shoot §a5 Diamond Zombies",
    ),
    SHOOT_LAMPS(
        GoalType.ZOMBIE_SHOOTOUT,
        "§7Shoot §a5 Redstone Lamps §7in a single",
        "(§8 - §r)?§7Shoot §r§a5 Redstone Lamps §r§7in a single game of §r§cZombie Shootout§r§7.",
        "Shoot §a5 Redstone Lamps",
    ),
    ;

    private val patternKeyName = name.lowercase().replace("_", ".")

    private val lorePattern by repoGroup.pattern("lore.$patternKeyName", loreLine)
    private val chatPattern by repoGroup.pattern("chat.$patternKeyName", chatLine)

    private var isReached: Boolean
        get() {
            val year = SkyBlockTime.now().year
            if (year != storage?.carnivalYear) {
                storage?.goals?.clear()
                storage?.carnivalYear = year
            }
            return storage?.goals?.get(this) ?: false
        }
        set(value) {
            val year = SkyBlockTime.now().year
            if (year != storage?.carnivalYear) {
                storage?.goals?.clear()
                storage?.carnivalYear = year
            }
            storage?.goals?.set(this, value)
            dirty = true
        }

    @SkyHanniModule
    companion object {

        init {
            entries.forEach {
                it.chatPattern
                it.lorePattern
            }
        }

        private val config get() = SkyHanniMod.feature.event.carnival
        private val storage get() = ProfileStorageData.profileSpecific?.carnival

        private val inventoryPattern by repoGroup.pattern("inventory", "Carnival Goals")

        private val completePattern by repoGroup.pattern("complete", "§a§lCOMPLETE")

        private var dirty = true

        private fun getEntry(item: Item, lore: List<String>): CarnivalGoal? =
            entries.filter { it.type.item == item }.firstOrNull { it.lorePattern.matches(lore.firstOrNull()) }

        @SubscribeEvent
        fun onProfileJoin(event: ProfileJoinEvent) {
            dirty = true
        }

        @SubscribeEvent
        fun onInventoryFullyOpened(event: InventoryFullyOpenedEvent) {
            if (!isEnabled()) return
            if (!inventoryPattern.matches(event.inventoryName)) return
            for (stack in event.inventoryItems.values) {
                val lore = stack.getLore()
                val goal = getEntry(stack.item, lore) ?: continue
                val lastLine = lore.last()
                goal.isReached = completePattern.matches(lastLine)
            }
        }

        @SubscribeEvent
        fun onLorenzChat(event: LorenzChatEvent) {
            if (!isEnabled()) return
            entries.firstOrNull { it.chatPattern.matches(event.message) }?.isReached = true
        }

        private var display = emptyList<Renderable>()

        @SubscribeEvent
        fun onGuiRenderGuiOverlayRender(event: GuiRenderEvent.GuiOverlayRenderEvent) {
            if (!isEnabled()) return
            if (dirty) {
                display = buildList {
                    GoalType.entries.map { it.fullDisplay }.forEach { list -> addAll(list) }
                }
                dirty = false
            }
            config.goalsPosition.renderRenderables(display, posLabel = "Carnival Goals")
        }

        fun isEnabled() =
            LorenzUtils.inSkyBlock && config.showGoals && Perk.CHIVALROUS_CARNIVAL.isActive && LorenzUtils.skyBlockArea == "Carnival"

        private enum class GoalType(val item: Item, display: String) {
            FRUIT_DIGGING(Item.getItemFromBlock(Blocks.sand), "§6Fruit Digging"),
            CATCH_A_FISH(Items.fish, "§3Catch a Fish"),
            ZOMBIE_SHOOTOUT(Items.arrow, "§cZombie Shootout");

            val singleDisplay by lazy {
                Renderable.horizontalContainer(
                    listOf(
                        Renderable.itemStack(ItemStack(item)),
                        Renderable.string(display),
                    ),
                )
            }

            val fullDisplay: List<Renderable>
                get() {
                    val goals = getGoals.filterNot { it.isReached }
                    if (goals.isEmpty()) return emptyList()
                    return listOf(singleDisplay) + goals.map { Renderable.string(" " + it.display) }
                }

            val getGoals get() = CarnivalGoal.entries.filter { it.type == this }
        }
    }

}
