package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.features.About
import at.hannibal2.skyhanni.config.features.chat.ChatConfig
import at.hannibal2.skyhanni.config.features.combat.CombatConfig
import at.hannibal2.skyhanni.config.features.crimsonisle.CrimsonIsleConfig
import at.hannibal2.skyhanni.config.features.dev.DevConfig
import at.hannibal2.skyhanni.config.features.dungeon.DungeonConfig
import at.hannibal2.skyhanni.config.features.event.EventConfig
import at.hannibal2.skyhanni.config.features.fishing.FishingConfig
import at.hannibal2.skyhanni.config.features.garden.GardenConfig
import at.hannibal2.skyhanni.config.features.gui.GUIConfig
import at.hannibal2.skyhanni.config.features.inventory.InventoryConfig
import at.hannibal2.skyhanni.config.features.mining.MiningConfig
import at.hannibal2.skyhanni.config.features.misc.MiscConfig
import at.hannibal2.skyhanni.config.features.rift.RiftConfig
import at.hannibal2.skyhanni.config.features.skillprogress.SkillProgressConfig
import at.hannibal2.skyhanni.config.features.slayer.SlayerConfig
import at.hannibal2.skyhanni.config.storage.Storage
import at.hannibal2.skyhanni.utils.LorenzUtils.isAprilFoolsDay
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.Social
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.gui.HorizontalAlign
import io.github.notenoughupdates.moulconfig.processor.ProcessedCategory

class Features : Config() {
    private val discord = MyResourceLocation("notenoughupdates", "social/discord.png")
    private val github = MyResourceLocation("notenoughupdates", "social/github.png")
    private val patreon = MyResourceLocation("notenoughupdates", "social/patreon.png")

    // in moulconfig, this value is currently bugged (version 3.5.0)
    override fun shouldAutoFocusSearchbar(): Boolean {
        return false
    }

    override fun alignCategory(category: ProcessedCategory, isSelected: Boolean): HorizontalAlign {
        if (isAprilFoolsDay) return HorizontalAlign.RIGHT
        return super.alignCategory(category, isSelected)
    }

    override fun getSocials(): List<Social> {
        return listOf(
            Social.forLink("Discord", discord, "https://discord.com/invite/skyhanni-997079228510117908"),
            Social.forLink("GitHub", github, "https://github.com/hannibal002/SkyHanni"),
            Social.forLink("Patreon", patreon, "https://www.patreon.com/hannibal2"),
        )
    }

    override fun saveNow() {
        SkyHanniMod.configManager.saveConfig(ConfigFileType.FEATURES, "close-gui")
    }

    override fun getTitle(): String {
        // Minecraft does not render RTL strings very nicely, so we reverse the string here. Not authentic, but close enough.
        val modName = if (isAprilFoolsDay) StringBuilder().append("اسکای هانی").reverse().toString()
        else "SkyHanni"

        return "$modName ${SkyHanniMod.VERSION} by §channibal2§r, config by §5Moulberry §rand §5nea89"
    }

    /*
     * If you are adding a new category, please insert it alphabetically
     * The only exceptions to this are About and GUI, which are pinned to the top
     * and Misc and Dev, which are to be at the bottom. Thanks!
     */
    // Top
    @Expose
    @Category(name = "About", desc = "Information about SkyHanni and updates.")
    var about: About = About()

    @JvmField
    @Expose
    @Category(name = "GUI", desc = "Change the locations of GUI elements (§e/sh gui§7).")
    var gui: GUIConfig = GUIConfig()

    // Islands
    @Expose
    @Category(name = "Garden", desc = "Features for the Garden island.")
    var garden: GardenConfig = GardenConfig()

    @Expose
    @Category(name = "Crimson Isle", desc = "Things to do on the Crimson Isle/Nether island.")
    var crimsonIsle: CrimsonIsleConfig = CrimsonIsleConfig()

    @Expose
    @Category(name = "The Rift", desc = "Features for The Rift dimension.")
    var rift: RiftConfig = RiftConfig()

    // Skills
    @Expose
    @Category(name = "Fishing", desc = "Fishing stuff.")
    var fishing: FishingConfig = FishingConfig()

    @Expose
    @Category(name = "Mining", desc = "Features that help you break blocks.")
    var mining: MiningConfig = MiningConfig()

    // Combat like
    @Expose
    @Category(name = "Combat", desc = "Everything combat and PvE related.")
    var combat: CombatConfig = CombatConfig()

    @Expose
    @Category(name = "Slayer", desc = "Slayer features.")
    var slayer: SlayerConfig = SlayerConfig()

    @Expose
    @Category(name = "Dungeon", desc = "Features that change the Dungeons experience in The Catacombs.")
    var dungeon: DungeonConfig = DungeonConfig()

    // Misc
    @Expose
    @Category(name = "Inventory", desc = "Change the behavior of items and the inventory.")
    var inventory: InventoryConfig = InventoryConfig()

    @Expose
    @Category(name = "Events", desc = "Stuff that is not always available.")
    var event: EventConfig = EventConfig()

    @Expose
    @Category(name = "Skill Progress", desc = "Skill Progress related config options.")
    var skillProgress: SkillProgressConfig = SkillProgressConfig()

    @Expose
    @Category(name = "Chat", desc = "Change how the chat looks.")
    var chat: ChatConfig = ChatConfig()

    @JvmField
    @Expose
    @Category(name = "Misc", desc = "Settings without a category.")
    var misc: MiscConfig = MiscConfig()

    // Bottom
    @Expose
    @Category(name = "Dev", desc = "Debug and test stuff. Developers are cool.")
    var dev: DevConfig = DevConfig()

    @Expose
    var storage: Storage = Storage()

    @Expose
    @Suppress("unused")
    var lastVersion: Int = ConfigUpdaterMigrator.CONFIG_VERSION
}
