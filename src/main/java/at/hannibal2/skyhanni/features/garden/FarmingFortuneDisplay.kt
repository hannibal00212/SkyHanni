package at.hannibal2.skyhanni.features.garden

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.data.GardenCropMilestones
import at.hannibal2.skyhanni.data.GardenCropMilestones.getCounter
import at.hannibal2.skyhanni.events.CropClickEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.LorenzWorldChangeEvent
import at.hannibal2.skyhanni.events.TabListUpdateEvent
import at.hannibal2.skyhanni.features.garden.CropType.Companion.getTurboCrop
import at.hannibal2.skyhanni.features.garden.pests.PestAPI
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.nextAfter
import at.hannibal2.skyhanni.utils.HypixelCommands
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getFarmingForDummiesCount
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getHoeCounter
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor
import kotlin.math.log10
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object FarmingFortuneDisplay {
    private val config get() = GardenAPI.config.farmingFortunes

    private val patternGroup = RepoPattern.group("garden.fortunedisplay")
    private val universalTabFortunePattern by patternGroup.pattern(
        "tablist.universal",
        " Farming Fortune: §r§6☘(?<fortune>\\d+)",
    )
    private val cropSpecificTabFortunePattern by patternGroup.pattern(
        "tablist.cropspecific",
        " (?<crop>Wheat|Carrot|Potato|Pumpkin|Sugar Cane|Melon|Cactus|Cocoa Beans|Mushroom|Nether Wart) Fortune: §r§6☘(?<fortune>\\d+)",
    )
    private val collectionPattern by patternGroup.pattern(
        "collection",
        "§7You have §6\\+(?<ff>\\d{1,3})☘ .*",
    )
    @Suppress("MaxLineLength")
    private val tooltipFortunePattern by patternGroup.pattern(
        "tooltip.new",
        "^§7Farming Fortune: §a\\+(?<display>[\\d.]+)(?: §2\\(\\+\\d\\))?(?: §9\\(\\+(?<reforge>\\d+)\\))?(?: §d\\(\\+(?<gemstone>\\d+)\\))?\$",
    )
    private val armorAbilityPattern by patternGroup.pattern(
        "armorability",
        "Tiered Bonus: .* [(](?<pieces>.*)/4[)]",
    )
    private val lotusAbilityPattern by patternGroup.pattern(
        "lotusability",
        "§7Piece Bonus: §6+(?<bonus>.*)☘",
    )

    // todo make pattern work on Melon and Cropie armor
    private val armorAbilityFortunePattern by patternGroup.pattern(
        "armorabilityfortune",
        "§7.*§7Grants §6(?<bonus>.*)☘.*",
    )

    private var display = emptyList<Renderable>()

    private var lastToolSwitch = SimpleTimeMark.farPast()

    private val latestFF: MutableMap<CropType, Double>? get() = GardenAPI.storage?.latestTrueFarmingFortune

    private var currentCrop: CropType? = null

    private var tabFortuneUniversal: Double = 0.0
    private var tabFortuneCrop: Double = 0.0

    var displayedFortune = 0.0
    var reforgeFortune = 0.0
    var gemstoneFortune = 0.0
    var itemBaseFortune = 0.0
    var greenThumbFortune = 0.0
    var pesterminatorFortune = 0.0

    private var foundTabUniversalFortune = false
    private var foundTabCropFortune = false
    private var gardenJoinTime = SimpleTimeMark.farPast()
    private var firstBrokenCropTime = SimpleTimeMark.farPast()
    private var lastUniversalFortuneMissingError = SimpleTimeMark.farPast()
    private var lastCropFortuneMissingError = SimpleTimeMark.farPast()

    private val ZORROS_CAPE by lazy { "ZORROS_CAPE".toInternalName() }

    @SubscribeEvent
    fun onTabListUpdate(event: TabListUpdateEvent) {
        if (!GardenAPI.inGarden()) return
        event.tabList.firstNotNullOfOrNull {
            universalTabFortunePattern.matchMatcher(it) {
                val fortune = group("fortune").toDouble()
                foundTabUniversalFortune = true
                if (fortune != tabFortuneUniversal) {
                    tabFortuneUniversal = fortune
                    update()
                }
            }
            cropSpecificTabFortunePattern.matchMatcher(it) {
                val crop = CropType.getByName(group("crop"))
                val cropFortune = group("fortune").toDouble()

                currentCrop = crop
                foundTabCropFortune = true
                if (cropFortune != tabFortuneCrop) {
                    tabFortuneCrop = cropFortune
                    update()
                }
                if (GardenAPI.cropInHand == crop) {
                    latestFF?.put(crop, getCurrentFarmingFortune())
                }
            }
        }
    }

    @SubscribeEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        lastToolSwitch = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        if (GardenAPI.hideExtraGuis()) return
        if (GardenAPI.toolInHand == null) return
        config.pos.renderRenderables(display, posLabel = "True Farming Fortune")
    }

    private fun update() {
        display =
            if (gardenJoinTime.passedSince() > 5.seconds && !foundTabUniversalFortune && !gardenJoinTime.isFarPast()) {
                drawMissingFortuneDisplay(false)
            } else if (firstBrokenCropTime.passedSince() > 10.seconds && !foundTabCropFortune && !firstBrokenCropTime.isFarPast()) {
                drawMissingFortuneDisplay(true)
            } else drawDisplay()
    }

    private fun drawDisplay() = buildList {
        val displayCrop = GardenAPI.cropInHand ?: currentCrop ?: return@buildList

        val list = mutableListOf<Renderable>()
        list.add(Renderable.itemStack(displayCrop.icon))

        var recentlySwitchedTool = lastToolSwitch.passedSince() < 1.5.seconds
        val wrongTabCrop = GardenAPI.cropInHand != null && GardenAPI.cropInHand != currentCrop

        val farmingFortune = if (wrongTabCrop) {
            (displayCrop.getLatestTrueFarmingFortune() ?: -1.0).also {
                recentlySwitchedTool = false
            }
        } else getCurrentFarmingFortune()

        list.add(
            Renderable.string(
                "§6Farming Fortune§7: §e" + if (!recentlySwitchedTool && farmingFortune != -1.0) {
                    farmingFortune.roundTo(0).addSeparators()
                } else "§7" + (displayCrop.getLatestTrueFarmingFortune()?.addSeparators() ?: "?"),
            ),
        )
        add(Renderable.horizontalContainer(list))

        val ffReduction = getPestFFReduction()
        if (ffReduction > 0) {
            add(Renderable.string("§cPests are reducing your fortune by §e$ffReduction%§c!"))
        }

        if (wrongTabCrop) {
            var text = "§cBreak §e${GardenAPI.cropInHand?.cropName}§c to see"
            if (farmingFortune != -1.0) text += " latest"
            text += " fortune!"

            add(Renderable.string(text))
        }
    }

    private fun drawMissingFortuneDisplay(cropFortune: Boolean) = buildList {
        if (cropFortune) {
            add(
                Renderable.clickAndHover(
                    "§cNo Crop Fortune Found! Enable The Stats Widget",
                    listOf(
                        "§cEnable the Stats widget and enable",
                        "§cshowing latest Crop Fortune.",
                    ),
                    onClick = {
                        HypixelCommands.widget()
                    },
                ),
            )
        } else {
            add(
                Renderable.clickAndHover(
                    "§cNo Farming Fortune Found! Enable The Stats Widget",
                    listOf(
                        "§cEnable the Stats widget and enable",
                        "§cshowing the Farming Fortune stat.",
                    ),
                    onClick = {
                        HypixelCommands.widget()
                    },
                ),
            )
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!GardenAPI.inGarden()) return
        if (event.isMod(2)) update()
        if (gardenJoinTime.passedSince() > 5.seconds && !foundTabUniversalFortune && !gardenJoinTime.isFarPast()) {
            if (lastUniversalFortuneMissingError.passedSince() < 20.seconds) return
            ChatUtils.clickableChat(
                "§cCan not read Farming Fortune from tab list! Open /widget, enable the Stats Widget and " +
                    "show the Farming Fortune stat, also give the widget enough priority.",
                onClick = { HypixelCommands.widget() },
                "§eClick to run /widget!",
                replaceSameMessage = true,
            )
            lastUniversalFortuneMissingError = SimpleTimeMark.now()
        }
        if (firstBrokenCropTime.passedSince() > 10.seconds && !foundTabCropFortune && !firstBrokenCropTime.isFarPast()) {
            if (lastCropFortuneMissingError.passedSince() < 20.seconds || !GardenAPI.isCurrentlyFarming()) return
            ChatUtils.clickableChat(
                "§cCan not read Crop Fortune from tab list! Open /widget, enable the Stats Widget and " +
                    "show latest Crop Fortune, also give the widget enough priority.",
                onClick = { HypixelCommands.widget() },
                "§eClick to run /widget!",
                replaceSameMessage = true,
            )
            lastCropFortuneMissingError = SimpleTimeMark.now()
        }
    }

    @SubscribeEvent
    fun onCropClick(event: CropClickEvent) {
        if (firstBrokenCropTime == SimpleTimeMark.farPast()) firstBrokenCropTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onWorldChange(event: LorenzWorldChangeEvent) {
        display = emptyList()
        gardenJoinTime = SimpleTimeMark.now()
        firstBrokenCropTime = SimpleTimeMark.farPast()
        foundTabUniversalFortune = false
        foundTabCropFortune = false
    }

    private fun isEnabled(): Boolean = GardenAPI.inGarden() && config.display

    private fun getPestFFReduction(): Int = when (PestAPI.scoreboardPests) {
        in 0..3 -> 0
        4 -> 5
        5 -> 15
        6 -> 30
        7 -> 50
        else -> 75
    }

    fun getToolFortune(tool: ItemStack?): Double = getToolFortune(tool?.getInternalName())
    fun getToolFortune(internalName: NEUInternalName?): Double {
        if (internalName == null) return 0.0
        val string = internalName.asString()
        if (string == "THEORETICAL_HOE") {
            return 0.0
        }
        return if (string.startsWith("THEORETICAL_HOE")) {
            listOf(10.0, 25.0, 50.0)[string.last().digitToInt() - 1]
        } else when (string) {
            "FUNGI_CUTTER" -> 30.0
            "COCO_CHOPPER" -> 20.0
            else -> 0.0
        }
    }

    fun getTurboCropFortune(tool: ItemStack?, cropType: CropType?): Double {
        val crop = cropType ?: return 0.0
        return tool?.getEnchantments()?.get(crop.getTurboCrop())?.let { it * 5.0 } ?: 0.0
    }

    fun getCollectionFortune(tool: ItemStack?): Double {
        val string = tool?.getLore()?.nextAfter("§6Collection Analysis", 3) ?: return 0.0
        return collectionPattern.matchMatcher(string) { group("ff").toDoubleOrNull() } ?: 0.0
    }

    fun getCounterFortune(tool: ItemStack?): Double {
        val counter = tool?.getHoeCounter() ?: return 0.0
        val digits = floor(log10(counter.toDouble()))
        return (16 * digits - 48).coerceAtLeast(0.0)
    }

    fun getDedicationFortune(tool: ItemStack?, cropType: CropType?): Double {
        if (cropType == null) return 0.0
        val dedicationLevel = tool?.getEnchantments()?.get("dedication") ?: 0
        val dedicationMultiplier = listOf(0.0, 0.5, 0.75, 1.0, 2.0)[dedicationLevel]
        val cropMilestone = GardenCropMilestones.getTierForCropCount(
            cropType.getCounter(), cropType,
        )
        return dedicationMultiplier * cropMilestone
    }

    fun getSunderFortune(tool: ItemStack?) = (tool?.getEnchantments()?.get("sunder") ?: 0) * 12.5
    fun getHarvestingFortune(tool: ItemStack?) = (tool?.getEnchantments()?.get("harvesting") ?: 0) * 12.5
    fun getCultivatingFortune(tool: ItemStack?) = (tool?.getEnchantments()?.get("cultivating") ?: 0) * 2.0
    fun getPesterminatorFortune(tool: ItemStack?) = (tool?.getEnchantments()?.get("pesterminator") ?: 0) * 1.0

    fun getAbilityFortune(item: ItemStack?) = item?.let {
        getAbilityFortune(it.getInternalName(), it.getLore())
    } ?: 0.0

    fun getAbilityFortune(internalName: NEUInternalName, lore: List<String>): Double {
        var pieces = 0

        for (line in lore) {
            if (internalName.contains("LOTUS")) {
                lotusAbilityPattern.matchMatcher(line) {
                    return group("bonus").toDouble()
                }
            }
            armorAbilityPattern.matchMatcher(line.removeColor()) {
                pieces = group("pieces").toInt()
            }

            armorAbilityFortunePattern.matchMatcher(line) {
                return if (pieces < 2) 0.0 else group("bonus").toDouble() / pieces
            }
        }

        return 0.0
    }

    fun loadFortuneLineData(tool: ItemStack?, enchantmentFortune: Double) {
        displayedFortune = 0.0
        reforgeFortune = 0.0
        gemstoneFortune = 0.0
        itemBaseFortune = 0.0
        greenThumbFortune = 0.0
        pesterminatorFortune = getPesterminatorFortune(tool)

        // TODO code cleanup (after ff rework)

        val lore = tool?.getLore() ?: return
        for (line in lore) {
            tooltipFortunePattern.matchMatcher(line) {
                displayedFortune = group("display")?.toDouble() ?: 0.0
                reforgeFortune = groupOrNull("reforge")?.toDouble() ?: 0.0
                gemstoneFortune = groupOrNull("gemstone")?.toDouble() ?: 0.0
            } ?: continue

            itemBaseFortune = if (tool.getInternalName().contains("LOTUS")) {
                5.0
            } else if (tool.getInternalName() == ZORROS_CAPE) {
                10.0
            } else {
                val dummiesFF = (tool.getFarmingForDummiesCount() ?: 0) * 1.0
                displayedFortune - reforgeFortune - gemstoneFortune - pesterminatorFortune - enchantmentFortune - dummiesFF
            }

            greenThumbFortune = if (tool.getInternalName().let { it.contains("LOTUS") || it == ZORROS_CAPE }) {
                displayedFortune - reforgeFortune - itemBaseFortune
            } else 0.0
        }
    }

    fun getCurrentFarmingFortune() = tabFortuneUniversal + tabFortuneCrop

    fun CropType.getLatestTrueFarmingFortune() = latestFF?.get(this)

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.farmingFortuneDisplay", "garden.farmingFortunes.display")
        event.move(3, "garden.farmingFortuneDropMultiplier", "garden.farmingFortunes.dropMultiplier")
        event.move(3, "garden.farmingFortunePos", "garden.farmingFortunes.pos")
    }
}
