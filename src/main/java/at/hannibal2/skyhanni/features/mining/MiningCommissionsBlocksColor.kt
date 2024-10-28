package at.hannibal2.skyhanni.features.mining

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.MiningAPI.inCrystalHollows
import at.hannibal2.skyhanni.data.MiningAPI.inDwarvenMines
import at.hannibal2.skyhanni.data.MiningAPI.inGlacite
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.features.mining.MiningCommissionsBlocksColor.CommissionBlock.Companion.onColor
import at.hannibal2.skyhanni.features.mining.OreType.Companion.isOreType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.TimeLimitedSet
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.block.BlockCarpet
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object MiningCommissionsBlocksColor {

    private val config get() = SkyHanniMod.feature.mining.commissionsBlocksColor

    private var enabled = false
    private var active = false

    private val patternGroup = RepoPattern.group("mining.commissions")

    /**
     * REGEX-TEST: §a§lCITRINE GEMSTONE COLLECTOR §r§eCommission Complete! Visit the King §r§eto claim your rewards!
     */
    private val commissionCompletePattern by patternGroup.pattern(
        "complete",
        "§a§l(?<name>.*) §r§eCommission Complete! Visit the King §r§eto claim your rewards!",
    )

    private var color = EnumDyeColor.RED

    private fun glass(state: IBlockState, result: Boolean): IBlockState = if (result) {
        state.withProperty(BlockCarpet.COLOR, color)
    } else {
        state.withProperty(BlockCarpet.COLOR, EnumDyeColor.GRAY)
    }

    private fun block(result: Boolean): IBlockState {
        val wool = Blocks.wool.defaultState
        return if (result) {
            wool.withProperty(BlockCarpet.COLOR, color)
        } else {
            wool.withProperty(BlockCarpet.COLOR, EnumDyeColor.GRAY)
        }
    }

    private var oldSneakState = false
    private var dirty = false
    private var replaceBlocksMapCache = mutableMapOf<IBlockState, IBlockState>()

    // TODO Commission API
    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        for (block in CommissionBlock.entries) {
            val tabList = " §r§f${block.commissionName}: "
            val newValue = event.tabList.any { it.startsWith(tabList) && !it.contains("DONE") }
            if (block.highlight != newValue) {
                if (newValue && block in ignoredTabListCommissions) continue
                block.highlight = newValue
                dirty = true
            }
        }
    }

    private val ignoredTabListCommissions = TimeLimitedSet<CommissionBlock>(5.seconds)

    // TODO Commission API
    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!enabled) return
        commissionCompletePattern.matchMatcher(event.message) {
            val name = group("name")
            val block = CommissionBlock.entries.find { it.commissionName.equals(name, ignoreCase = true) } ?: return
            block.highlight = false
            dirty = true
            ignoredTabListCommissions.add(block)
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        val newEnabled = (inCrystalHollows || inGlacite) && config.enabled
        var reload = false
        if (newEnabled != enabled) {
            enabled = newEnabled
            reload = true
            if (enabled) {
                active = true
            }
        }

        if (enabled) {
            if (config.sneakQuickToggle.get()) {
                val sneaking = Minecraft.getMinecraft().thePlayer.isSneaking
                if (sneaking != oldSneakState) {
                    oldSneakState = sneaking
                    if (oldSneakState) {
                        active = !active
                        dirty = true
                    }
                }
            }
            if (dirty) {
                reload = true
            }
        }

        if (reload) {
            replaceBlocksMapCache = mutableMapOf()
            Minecraft.getMinecraft().renderGlobal.loadRenderers()
            dirty = false
        }
    }

    @SubscribeEvent
    fun onConfigReload(event: ConfigLoadEvent) {
        color = config.color.get().toDyeColor()
        config.sneakQuickToggle.onToggle {
            oldSneakState = false
            if (!active) {
                active = true
                dirty = true
            }
        }
        config.color.onToggle {
            color = config.color.get().toDyeColor()
            dirty = true
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        enabled = false
        inDwarvenMines = false
        inCrystalHollows = false
        inGlacite = false
        replaceBlocksMapCache = mutableMapOf()
    }

    @SubscribeEvent
    fun onDebugDataCollect(event: DebugDataCollectEvent) {
        event.title("Mining Commissions Blocks Color")
        if (!enabled) {
            event.addIrrelevant("not enabled")
            return
        }

        event.addData {
            add("inDwarvenMines: $inDwarvenMines")
            add("inCrystalHollows: $inCrystalHollows")
            add("inGlaciteArea: $inGlacite")
            add("active: $active")
        }
    }

    enum class CommissionBlock(
        val commissionName: String,
        val oreType: OreType,
        var highlight: Boolean = false,
    ) {
        // Dwarven Mines
        MITHRIL(
            "Mithril Everywhere",
            OreType.MITHRIL,
        ),

        // Crystal Hollows
        AMBER(
            "Amber Gemstone Collector",
            OreType.AMBER,
        ),
        TOPAZ(
            "Topaz Gemstone Collector",
            OreType.TOPAZ,
        ),
        AMETHYST(
            "Amethyst Gemstone Collector",
            OreType.AMETHYST,
        ),
        RUBY(
            "Ruby Gemstone Collector",
            OreType.RUBY,
        ),
        JADE(
            "Jade Gemstone Collector",
            OreType.JADE,
        ),
        SAPPHIRE(
            "Sapphire Gemstone Collector",
            OreType.SAPPHIRE,
        ),

        // Glacite Tunnels
        GLACITE(
            "Glacite Collector",
            OreType.GLACITE,
        ),
        UMBER(
            "Umber Collector",
            OreType.UMBER,
        ),
        TUNGSTON(
            "Tungsten Collector",
            OreType.TUNGSTEN,
        ),
        PERIDOT(
            "Peridot Gemstone Collector",
            OreType.PERIDOT,
        ),
        AQUAMARINE(
            "Aquamarine Gemstone Collector",
            OreType.AQUAMARINE,
        ),
        CITRINE(
            "Citrine Gemstone Collector",
            OreType.CITRINE,
        ),
        ONYX(
            "Onyx Gemstone Collector",
            OreType.ONYX,
        ),
        ;

        companion object {
            fun CommissionBlock.onColor(state: IBlockState): IBlockState =
                if (oreType.isGemstone()) glass(state, highlight) else block(highlight)
        }
    }

    fun processState(state: IBlockState?): IBlockState? {
        if (!enabled || !active) return state
        if (state == null) return null
        try {
            return replaceBlocksMapCache.getOrPut(state) {
                CommissionBlock.entries.firstOrNull {
                    state.isOreType(it.oreType)
                }?.onColor(state) ?: state
            }
        } catch (e: Exception) {
            ErrorManager.logErrorWithData(e, "Error in MiningCommissionsBlocksColor")
            return state
        }
    }
}
