package at.hannibal2.skyhanni.features.garden.fortuneguide

import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.PetAPI
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.FarmingFortuneDisplay
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.GardenAPI.getCropType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimal
import at.hannibal2.skyhanni.utils.NumberUtil.romanToDecimalIfNecessary
import at.hannibal2.skyhanni.utils.RegexUtils.matchFirst
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.SimpleTimeMark.Companion.fromNow
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getEnchantments
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.TabListData
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.round
import kotlin.time.Duration.Companion.days

@SkyHanniModule
object CaptureFarmingGear {
    private val outdatedItems get() = GardenAPI.storage?.fortune?.outdatedItems

    private val patternGroup = RepoPattern.group("garden.fortuneguide.capture")
    private val farmingLevelUpPattern by patternGroup.pattern(
        "farminglevel",
        "SKILL LEVEL UP Farming .*➜(?<level>.*)",
    )
    private val fortuneUpgradePattern by patternGroup.pattern(
        "fortuneupgrade",
        "You claimed the Garden Farming Fortune (?<level>.*) upgrade!",
    )
    private val bestiaryPattern by patternGroup.pattern(
        "bestiary",
        ".*§6+(?<fortune>.*)☘ Farming Fortune.*",
    )
    private val anitaBuffPattern by patternGroup.pattern(
        "anitabuff",
        "You tiered up the Extra Farming Drops upgrade to [+](?<level>.*)%!",
    )
    private val anitaMenuPattern by patternGroup.pattern(
        "anitamenu",
        "§7You have: §6\\+(?<level>.*)☘ Farming Fortune",
    )
    private val lotusUpgradePattern by patternGroup.pattern(
        "lotusupgrade",
        "Lotus (?<piece>.*) upgraded to [+].*☘!",
    )
    private val petLevelUpPattern by patternGroup.pattern(
        "petlevelup",
        "Your (?<pet>.*) leveled up to level .*!",
    )
    private val cakePattern by patternGroup.pattern(
        "cake",
        "(?:Big )?Yum! You (?:gain|refresh) [+]5☘ Farming Fortune for 48 hours!",
    )
    private val strengthPattern by patternGroup.pattern(
        "strength",
        " Strength: §r§c❁(?<strength>.*)",
    )
    private val tierPattern by patternGroup.pattern(
        "uniquevisitors.tier",
        "§7Progress to Tier (?<nextTier>\\w+):.*",
    )
    private val tierProgressPattern by patternGroup.pattern(
        "uniquevisitors.tierprogress",
        ".* §e(?<having>.*)§6/(?<total>.*)",
    )

    private val farmingSets = arrayListOf(
        "FERMENTO", "SQUASH", "CROPIE", "MELON", "FARM",
        "RANCHERS", "FARMER", "RABBIT",
    )

    init {
        CarrolynTable.entries.forEach {
            it.completeMessagePattern
            it.thxMessagePattern
        }
    }

    // TODO upadte armor on equpment/wardeobe update as well
    fun captureFarmingGear() {
        for (armor in InventoryUtils.getArmor()) {
            if (armor == null) continue
            val split = armor.getInternalName().asString().split("_")
            if (split.first() in farmingSets) {
                val category = armor.getItemCategoryOrNull() ?: continue
                FarmingItems.getFromItemCategoryOne(category)?.setItem(armor)
            }
        }

        val itemStack = InventoryUtils.getItemInHand() ?: return

        val currentCrop = itemStack.getCropType()

        if (currentCrop == null) {
            // todo better fall back items
            // todo Daedalus axe
        } else {
            currentCrop.farmingItem.setItem(itemStack)
        }

        TabListData.getTabList().matchFirst(strengthPattern) {
            GardenAPI.storage?.fortune?.farmingStrength = group("strength").toInt()
        }
    }

    fun handelCarrolyn(input: Array<String>) {
        val string = input.joinToString("_").uppercase()
        val crop = CropType.entries.firstOrNull { it.name == string }
            ?: ChatUtils.userError("Invalid Argument, no crop with the name: $string").run { return }
        val carrolyn = CarrolynTable.getByCrop(crop)
            ?: ChatUtils.userError("Invalid Argument, crop is not valid").run { return }
        carrolyn.setVisibleActive(!carrolyn.get())
    }

    private fun getUniqueVisitorsForTier(tier: Int): Int {
        return when {
            tier == 0 -> 0
            tier == 1 -> 1
            tier == 2 -> 5
            tier >= 3 -> 10 * (tier - 2)
            else -> throw IllegalStateException("Unexpected unique visitors tier: $tier")
        }
    }

    @SubscribeEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        captureFarmingGear()
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val storage = GardenAPI.storage?.fortune ?: return
        val outdatedItems = outdatedItems ?: return
        val items = event.inventoryItems
        if (PetAPI.isPetMenu(event.inventoryName)) {
            pets(items, outdatedItems)
            return
        }
        when (event.inventoryName) {
            "Your Equipment and Stats" -> equipmentAndStats(items, outdatedItems)
            "Your Skills" -> skills(items, storage)
            "Community Shop" -> communityShop(items)
            "Configure Plots" -> configurePlots(items, storage)
            "Anita" -> anita(items, storage)
            "Visitor Milestones" -> visitorMilestones(items)
            "Bestiary", "Bestiary ➜ Garden" -> bestiary(items, storage)
        }
    }

    private fun bestiary(
        items: Map<Int, ItemStack>,
        storage: ProfileSpecificStorage.GardenStorage.Fortune,
    ) {
        for ((_, item) in items) {
            if (item.displayName.contains("Garden")) {
                var fortune = -1.0
                for (line in item.getLore()) {
                    bestiaryPattern.matchMatcher(line) {
                        fortune = group("fortune").toDouble()
                    }
                }
                if (fortune > -1.0) {
                    storage.bestiary = fortune
                }
            }
        }
    }

    private fun visitorMilestones(items: Map<Int, ItemStack>) {
        for ((_, item) in items) {
            if (item.displayName != "§aUnique Visitors Served") continue

            var tier = -1
            var tierProgress = -1
            for (line in item.getLore()) {
                tierPattern.matchMatcher(line) {
                    tier = group("nextTier").romanToDecimalIfNecessary() - 1
                }
                tierProgressPattern.matchMatcher(line) {
                    tierProgress = group("having").toInt()
                }
            }
            if (tier > -1 && tierProgress > -1) {
                GardenAPI.storage?.uniqueVisitors = getUniqueVisitorsForTier(tier) + tierProgress
            }
        }
    }

    private fun anita(
        items: Map<Int, ItemStack>,
        storage: ProfileSpecificStorage.GardenStorage.Fortune,
    ) {
        var level = -1
        for ((_, item) in items) {
            if (item.displayName.contains("Extra Farming Fortune")) {
                level = 0

                item.getLore().matchFirst(anitaMenuPattern) {
                    level = group("level").toInt() / 4
                }
            }
        }
        if (level == -1) {
            storage.anitaUpgrade = 15
        } else {
            storage.anitaUpgrade = level
        }
    }

    private fun configurePlots(
        items: Map<Int, ItemStack>,
        storage: ProfileSpecificStorage.GardenStorage.Fortune,
    ) {
        var plotsUnlocked = 24
        for (slot in items) {
            if (slot.value.getLore().contains("§7Cost:")) {
                plotsUnlocked -= 1
            }
        }
        storage.plotsUnlocked = plotsUnlocked
    }

    private fun communityShop(items: Map<Int, ItemStack>) {
        for ((_, item) in items) {
            if (item.displayName.contains("Garden Farming Fortune")) {
                if (item.getLore().contains("§aMaxed out!")) {
                    ProfileStorageData.playerSpecific?.gardenCommunityUpgrade =
                        item.displayName.split(" ").last().romanToDecimal()
                } else {
                    ProfileStorageData.playerSpecific?.gardenCommunityUpgrade =
                        item.displayName.split(" ").last().romanToDecimal() - 1
                }
            }
        }
    }

    private fun skills(
        items: Map<Int, ItemStack>,
        storage: ProfileSpecificStorage.GardenStorage.Fortune,
    ) {
        for ((_, item) in items) {
            if (item.displayName.contains("Farming ")) {
                storage.farmingLevel = item.displayName.split(" ").last().romanToDecimalIfNecessary()
            }
        }
    }

    private fun pets(
        items: Map<Int, ItemStack>,
        outdatedItems: MutableMap<FarmingItems, Boolean>,
    ) {
        // If they've 2 of same pet, one will be overwritten

        // setting to current saved level -1 to stop later pages saving low rarity pets
        var highestElephantRarity = (FarmingItems.ELEPHANT.getItemOrNull()?.getItemRarityOrNull()?.id ?: -1) - 1
        var highestMooshroomRarity = (FarmingItems.MOOSHROOM_COW.getItemOrNull()?.getItemRarityOrNull()?.id ?: -1) - 1
        var highestRabbitRarity = (FarmingItems.RABBIT.getItemOrNull()?.getItemRarityOrNull()?.id ?: -1) - 1
        var highestBeeRarity = (FarmingItems.BEE.getItemOrNull()?.getItemRarityOrNull()?.id ?: -1) - 1
        var highestSlugRarity = (FarmingItems.SLUG.getItemOrNull()?.getItemRarityOrNull()?.id ?: -1) - 1

        for ((_, item) in items) {
            if (item.getItemCategoryOrNull() != ItemCategory.PET) continue
            val (name, rarity) = item.getInternalName().asString().split(";")
            if (name == "ELEPHANT" && rarity.toInt() > highestElephantRarity) {
                FarmingItems.ELEPHANT.setItem(item)
                outdatedItems[FarmingItems.ELEPHANT] = false
                highestElephantRarity = rarity.toInt()
            }
            if (name == "MOOSHROOM_COW" && rarity.toInt() > highestMooshroomRarity) {
                FarmingItems.MOOSHROOM_COW.setItem(item)
                outdatedItems[FarmingItems.MOOSHROOM_COW] = false
                highestMooshroomRarity = rarity.toInt()
            }
            if (name == "RABBIT" && rarity.toInt() > highestRabbitRarity) {
                FarmingItems.RABBIT.setItem(item)
                outdatedItems[FarmingItems.RABBIT] = false
                highestRabbitRarity = rarity.toInt()
            }
            if (name == "BEE" && rarity.toInt() > highestBeeRarity) {
                FarmingItems.BEE.setItem(item)
                outdatedItems[FarmingItems.BEE] = false
                highestBeeRarity = rarity.toInt()
            }
            if (name == "SLUG" && rarity.toInt() > highestSlugRarity) {
                FarmingItems.SLUG.setItem(item)
                outdatedItems[FarmingItems.SLUG] = false
                highestSlugRarity = rarity.toInt()
            }
        }
    }

    private fun equipmentAndStats(
        items: Map<Int, ItemStack>,
        outdatedItems: MutableMap<FarmingItems, Boolean>,
    ) {
        for ((_, slot) in items) {
            val split = slot.getInternalName().asString().split("_")
            val category = slot.getItemCategoryOrNull() ?: continue
            if (split.first() == "LOTUS") {
                val item = FarmingItems.getFromItemCategoryOne(category) ?: continue
                item.setItem(slot)
                outdatedItems[item] = false
                FarmingFortuneDisplay.loadFortuneLineData(slot, 0.0)
                val enchantments = slot.getEnchantments() ?: emptyMap()
                val greenThumbLvl = (enchantments["green_thumb"] ?: continue)
                val visitors = FarmingFortuneDisplay.greenThumbFortune / (greenThumbLvl * 0.05)
                GardenAPI.storage?.uniqueVisitors = round(visitors).toInt()
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!LorenzUtils.inSkyBlock) return
        val storage = GardenAPI.storage?.fortune ?: return
        val outdatedItems = outdatedItems ?: return
        val msg = event.message.removeColor().trim()
        fortuneUpgradePattern.matchMatcher(msg) {
            ProfileStorageData.playerSpecific?.gardenCommunityUpgrade = group("level").romanToDecimal()
            return
        }
        farmingLevelUpPattern.matchMatcher(msg) {
            storage.farmingLevel = group("level").romanToDecimalIfNecessary()
            return
        }
        bestiaryPattern.matchMatcher(msg) {
            storage.bestiary += group("fortune").toDouble()
            return
        }
        anitaBuffPattern.matchMatcher(msg) {
            storage.anitaUpgrade = group("level").toInt() / 4
            return
        }
        lotusUpgradePattern.matchMatcher(msg) {
            val piece = group("piece").uppercase()
            for (item in FarmingItems.entries) {
                if (item.name == piece) {
                    outdatedItems[item] = true
                }
            }
            return
        }
        petLevelUpPattern.matchMatcher(msg) {
            val pet = group("pet").uppercase().replace("✦", "").trim().replace(" ", "_")
            for (item in FarmingItems.entries) {
                if (item.name.contains(pet)) {
                    outdatedItems[item] = true
                }
            }
            return
        }
        cakePattern.matchMatcher(msg) {
            FFStats.cakeExpireTime = 2.days.fromNow()
            return
        }
        CarrolynTable.entries.forEach {
            it.completeMessagePattern.matchMatcher(msg) {
                it.set(true)
                return
            }
            it.thxMessagePattern.matchMatcher(msg) {
                it.set(true)
                ChatUtils.chat(it.thxResponse)
                return
            }
        }
    }

    @SubscribeEvent
    fun onConfigUpdaterMigratorConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(48, "#profile.garden.fortune.carrotFortune", "#profile.garden.fortune.carrolyn.CARROT")
        event.move(48, "#profile.garden.fortune.pumpkinFortune", "#profile.garden.fortune.carrolyn.PUMPKIN")
        event.move(48, "#profile.garden.fortune.cocoaBeansFortune", "#profile.garden.fortune.carrolyn.COCOA_BEANS")
    }
}
