package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.HotmData
import at.hannibal2.skyhanni.data.HotmReward
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.CollectionUtils.distribute
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.NumberUtil.fractionOf
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderAndScale
import at.hannibal2.skyhanni.utils.renderables.RenderableUtils.renderXYAligned
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object BlockStrengthGuide {

    private enum class DisplayOres(private val iconDel: () -> ItemStack, val oreBlocks: Set<OreBlock>) {
        VANILLA_ORES(
            { ItemStack(Blocks.redstone_block) },
            setOf(
                OreBlock.COAL_ORE,
                OreBlock.IRON_ORE,
                OreBlock.LAPIS_ORE,
                OreBlock.GOLD_ORE,
                OreBlock.EMERALD_ORE,
                OreBlock.DIAMOND_ORE,
                OreBlock.QUARTZ_ORE,
                OreBlock.REDSTONE_ORE,
            ),
        ),
        PURE_ORES(
            { ItemStack(Blocks.gold_block) },
            setOf(
                OreBlock.PURE_COAL,
                OreBlock.PURE_IRON,
                OreBlock.PURE_LAPIS,
                OreBlock.PURE_GOLD,
                OreBlock.PURE_EMERALD,
                OreBlock.PURE_DIAMOND,
                OreBlock.PURE_REDSTONE,
            ),
        ),
        TITANIUM(
            { ItemStack(Blocks.stone, 1, net.minecraft.block.BlockStone.EnumType.DIORITE_SMOOTH.metadata) },
            setOf(OreBlock.HIGH_TIER_MITHRIL),
        ),
        GRAY_MITHRIL(
            { ItemStack(Blocks.wool, 1, EnumDyeColor.GRAY.metadata) },
            setOf(OreBlock.LOW_TIER_MITHRIL),
        ),
        GREEN_MITHRIL(
            { ItemStack(Blocks.prismarine) },
            setOf(OreBlock.MID_TIER_MITHRIL),
        ),
        BLUE_MITHRIL(
            { ItemStack(Blocks.wool, 1, EnumDyeColor.LIGHT_BLUE.metadata) },
            setOf(OreBlock.HIGH_TIER_MITHRIL),
        ),
        TUNGSTEN_UMBER(
            { ItemStack(Blocks.clay) },
            setOf(
                OreBlock.LOW_TIER_UMBER,
                OreBlock.HIGH_TIER_UMBER,
                OreBlock.LOW_TIER_TUNGSTEN_MINESHAFT,
                OreBlock.LOW_TIER_TUNGSTEN_MINESHAFT,
                OreBlock.HIGH_TIER_TUNGSTEN,
            ),
        ),
        GLACITE(
            { ItemStack(Blocks.packed_ice) },
            setOf(OreBlock.GLACITE),
        ),
        OBSIDIAN(
            { ItemStack(Blocks.obsidian) },
            setOf(OreBlock.OBSIDIAN),
        ),
        RUBY(
            { ItemStack(Blocks.stained_glass, 1, EnumDyeColor.RED.metadata) },
            setOf(OreBlock.RUBY),
        ),
        NUCLEUS_GEMSTONES(
            { ItemStack(Blocks.stained_glass, 1, EnumDyeColor.LIGHT_BLUE.metadata) },
            setOf(OreBlock.AMBER, OreBlock.AMETHYST, OreBlock.JADE, OreBlock.SAPPHIRE),
        ),
        OPAL(
            { ItemStack(Blocks.stained_glass, 1, EnumDyeColor.WHITE.metadata) },
            setOf(OreBlock.OPAL),
        ),
        TOPAZ(
            { ItemStack(Blocks.stained_glass, 1, EnumDyeColor.YELLOW.metadata) },
            setOf(OreBlock.TOPAZ),
        ),
        JASPER(
            { ItemStack(Blocks.stained_glass, 1, EnumDyeColor.MAGENTA.metadata) },
            setOf(OreBlock.JASPER),
        ),
        TUNNEL_GEMSTONES(
            { ItemStack(Blocks.stained_glass, 1, EnumDyeColor.BLACK.metadata) },
            setOf(OreBlock.ONYX, OreBlock.PERIDOT, OreBlock.CITRINE, OreBlock.AQUAMARINE),
        ),
        HARD_STONE(
            { ItemStack(Blocks.stone) },
            setOf(OreBlock.HARD_STONE_HOLLOWS, OreBlock.HARD_STONE_TUNNELS, OreBlock.HARD_STONE_MINESHAFT),
        ),
        COBBLE_STONE(
            { ItemStack(Blocks.cobblestone) },
            setOf(OreBlock.COBBLESTONE),
        ),
        STONE(
            { ItemStack(Blocks.stone) },
            setOf(OreBlock.STONE),
        ),
        SULPHUR(
            { ItemStack(Blocks.sponge) },
            setOf(OreBlock.SULPHUR),
        ),
        NETHERRACK(
            { ItemStack(Blocks.netherrack) },
            setOf(OreBlock.NETHERRACK),
        ),
        END_STONE(
            { ItemStack(Blocks.end_stone) },
            setOf(OreBlock.END_STONE),
        );

        val icon by lazy(this.iconDel)

        val hoverText get() = "Block ids: " + oreBlocks.joinToString(", ") { it.name.allLettersFirstUppercase() }

        fun renderable(rawSpeed: SpeedClass): Renderable {
            val ore = oreBlocks.first()

            val speed = rawSpeed.base + when (ore.category) {
                OreCategory.DWARVEN_METAL -> rawSpeed.dwarven
                OreCategory.GEMSTONE -> rawSpeed.gemstone
                OreCategory.ORE -> rawSpeed.ore
                OreCategory.BLOCK -> rawSpeed.block
            }

            val ticks = ore.miningTicks(speed)

            val (progressBar, percentLine) = when (ticks) {
                1 -> Renderable.progressBar(1.0, InstantMineColor, InstantMineColor, width = 100) to "§6Instant Mine"
                4 -> Renderable.progressBar(
                    speed.fractionOf(ore.speedForInstantMine),
                    SoftCapColor,
                    InstantMineColor,
                    width = 100,
                ) to "§a${speed.fractionOf(ore.speedForInstantMine).times(100).roundTo(1)}% §fto Instant Mine"

                else -> Renderable.progressBar(
                    speed.fractionOf(ore.speedSoftCap),
                    BaseColor,
                    SoftCapColor,
                    width = 100,
                ) to "§a${speed.fractionOf(ore.speedSoftCap).times(100).roundTo(1)}% §fto Soft Cap"
            }

            return Renderable.hoverTips(
                Renderable.horizontalContainer(
                    listOf(
                        Renderable.itemStack(icon),
                        progressBar,
                        Renderable.string("$ticks"),
                    ),
                ),
                listOf(
                    Renderable.string(name.allLettersFirstUppercase()),
                    Renderable.placeholder(0, 5),
                    Renderable.string("${SkyblockStat.MINING_SPEED.icon} Your: ${speed.toInt()}"),
                    Renderable.string(percentLine),
                    Renderable.placeholder(0, 5),
                    Renderable.string("Block Strength: ${ore.strength}"),
                    Renderable.string("${SkyblockStat.MINING_SPEED.icon} Softcap: ${ore.speedSoftCap}"),
                    Renderable.string("${SkyblockStat.MINING_SPEED.icon} Instant: ${ore.speedForInstantMine}"),
                    Renderable.placeholder(0, 5),
                    Renderable.string("Category: ${ore.category.toString().allLettersFirstUppercase()}"),
                    Renderable.string(hoverText),
                ),
            )
        }

    }

    private val InstantMineColor = Color(0x1E, 0x90, 0xFF)
    private val SoftCapColor = Color(0x00, 0xFA, 0x9A)
    private val BaseColor = Color(0xFF, 0x63, 0x37)

    private lateinit var speed: SpeedClass

    private var inMineshaft = false

    // TODO Dwarven Equip (Needs a Equipment API) , Goblin Pet and Mithril Pet (need the PetAPI v2)
    private fun requestSpeed(): SpeedClass {
        val itemInHand = InventoryUtils.getItemInHand()
        speed = SpeedClass(
            base = (
                SkyblockStat.MINING_SPEED.lastKnownValue ?: 0.0
                ) + if (inMineshaft) HotmData.EAGER_ADVENTURER.getReward()[HotmReward.MINING_SPEED] ?: 0.0 else 0.0,
            dwarven = HotmData.STRONG_ARM.getReward()[HotmReward.MINING_SPEED] ?: 0.0,
            gemstone = (
                HotmData.PROFESSIONAL.getReward()[HotmReward.MINING_SPEED] ?: 0.0
                ) + (
                itemInHand?.getEnchantments()?.get("lapidary")?.times(20.0) ?: 0.0
                ) + when (itemInHand?.getInternalNameOrNull()?.asString()) {
                "GEMSTONE_DRILL_1", "GEMSTONE_DRILL_2", "GEMSTONE_DRILL_3", "GEMSTONE_DRILL_4" -> 800.0
                else -> 0.0
            },
            ore = 0.0,
            block = 0.0,
        )

        return speed
    }

    private data class SpeedClass(
        val base: Double,
        val dwarven: Double,
        val gemstone: Double,
        val ore: Double,
        val block: Double,
    ) {
        fun toRenderables() = listOf(
            base.toInt().toString(),
            gemstone.toInt().toString(),
            dwarven.toInt().toString(),
        ).map { Renderable.string("§6$it") }
    }

    private val headerHeaderLine = listOf("Base", "Gemstone", "Dwarven").map {
        Renderable.string(
            it,
            horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
            scale = 0.75,
        )
    }

    private var display: Renderable? = null

    private fun createDisplay(): Renderable {
        requestSpeed()
        return Renderable.drawInsideRoundedRectWithOutline(
            Renderable.verticalContainer(
                listOf(
                    Renderable.verticalContainer(
                        createHeader(),
                        horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
                    ),
                    Renderable.table(
                        createTableContent(), 5, 3,
                    ),
                ),
                spacing = 8,
            ),
            color = LorenzColor.GRAY.addOpacity(180),
            topOutlineColor = Color(0, 0, 0, 200).rgb,
            bottomOutlineColor = Color(0, 0, 0, 200).rgb,
            borderOutlineThickness = 3,
            verticalAlign = RenderUtils.VerticalAlignment.CENTER,
            horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
        )
    }

    private fun createTableContent(): List<List<Renderable>> = DisplayOres.entries.map {
        it.renderable(speed)
    }.distribute(3)

    private fun createHeader(): List<Renderable> = listOf(
        Renderable.string(
            SkyblockStat.MINING_SPEED.iconWithName,
            horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
        ),
        Renderable.horizontalContainer(
            listOf(
                Renderable.table(
                    listOf(
                        headerHeaderLine,
                        speed.toRenderables(),
                    ),
                    xPadding = 5,
                ),
                Renderable.clickable(
                    Renderable.string(
                        "§${if (inMineshaft) 'b' else '7'}Mineshaft",
                        scale = 0.5,
                        verticalAlign = RenderUtils.VerticalAlignment.CENTER,
                    ),
                    {
                        inMineshaft = !inMineshaft
                        display = createDisplay()
                    },
                ),
            ),
            spacing = 3,
        ),
    )

    private var shouldBlockSHMenu = false
        set(value) {
            field = value
            if (!value) {
                display = null
            } else {
                lastSet = SimpleTimeMark.now()
            }
        }

    private var sbMenuOpened = false

    private var lastSet = SimpleTimeMark.farPast()

    fun onCommand() {
        shouldBlockSHMenu = true
        sbMenuOpened = false
        HypixelCommands.skyblockMenu()
    }

    @SubscribeEvent
    fun onGuiContainerPreDraw(event: GuiContainerEvent.PreDraw) {
        if (!shouldBlockSHMenu) return

        event.cancel()

        if (!sbMenuOpened) {
            sbMenuOpened = SkyblockStat.MINING_SPEED.lastAssignment.passedSince() < 1.0.seconds
            Renderable.string(
                "Loading...",
                scale = 2.0,
                verticalAlign = RenderUtils.VerticalAlignment.CENTER,
                horizontalAlign = RenderUtils.HorizontalAlignment.CENTER,
            ).renderXYAligned(0, 0, event.gui.width, event.gui.height)
            return
        }

        val display = display ?: createDisplay().also {
            display = it
            println(SkyblockStat.MINING_SPEED.lastSource)
        }

        Renderable.withMousePosition(event.mouseX, event.mouseY) {
            display.renderAndScale(0, 0, event.gui.width, event.gui.height, 20)
        }
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (!sbMenuOpened) return
        shouldBlockSHMenu = false
    }

    @SubscribeEvent
    fun onIslandChange(event: IslandChangeEvent) {
        shouldBlockSHMenu = false
    }

    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shblockstrength") {
            description = "Shows how many ticks you need to break any block with your mining speed."
            category = CommandCategory.USERS_ACTIVE
            callback { onCommand() }
        }
    }

}
