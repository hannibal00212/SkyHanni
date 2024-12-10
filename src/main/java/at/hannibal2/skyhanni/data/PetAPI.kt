package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.commands.CommandCategory
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.PetData.Companion.parsePetAsItem
import at.hannibal2.skyhanni.data.PetData.Companion.parsePetData
import at.hannibal2.skyhanni.data.PetData.Companion.parsePetDataLists
import at.hannibal2.skyhanni.data.PetData.Companion.petNameToInternalName
import at.hannibal2.skyhanni.data.jsonobjects.repo.NEUPetData
import at.hannibal2.skyhanni.data.jsonobjects.repo.NEUPetsJson
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.NeuRepositoryReloadEvent
import at.hannibal2.skyhanni.events.WidgetUpdateEvent
import at.hannibal2.skyhanni.events.skyblock.PetChangeEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ItemCategory
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getLore
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.formatDouble
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatchGroup
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.firstMatches
import at.hannibal2.skyhanni.utils.RegexUtils.groupOrNull
import at.hannibal2.skyhanni.utils.RegexUtils.hasGroup
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.StringUtils.convertToUnformatted
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PetAPI {
    private val config get() = SkyHanniMod.feature.misc.pets
    private const val FORGE_BACK_SLOT = 48

    val patternGroup = RepoPattern.group("misc.pet")

    private var inPetMenu = false
    private var baseXpLevelReqs: List<Int> = listOf()
    private var customXpLevelReqs: Map<String, NEUPetData>? = null

    var currentPet: PetData?
        get() = ProfileStorageData.profileSpecific?.currentPet?.takeIf { it.isInitialized() }
        set(value) {
            ProfileStorageData.profileSpecific?.currentPet = value
        }

    // <editor-fold desc="Patterns">
    /**
     * REGEX-TEST: Pets (1/3)
     * REGEX-TEST: Pets
     * REGEX-TEST: Pets (1/4)
     * REGEX-TEST: Pets (1/2)
     */
    private val petMenuPattern by patternGroup.pattern(
        "menu.title",
        "Pets(?: \\(\\d+/\\d+\\) )?",
    )

    /**
     * REGEX-TEST: §e⭐ §7[Lvl 200] §6Golden Dragon§d ✦
     * REGEX-TEST: ⭐ [Lvl 100] Black Cat ✦
     */
    val petItemNamePattern by patternGroup.pattern(
        "item.name",
        "(?<favorite>(?:§.)*⭐ )?(?:§.)*\\[Lvl (?<level>\\d+)] (?<name>.*)",
    )

    /**
     * REGEX-TEST: §7[Lvl 1➡200] §6Golden Dragon
     * REGEX-TEST: §7[Lvl {LVL}] §6Golden Dragon
     */
    private val neuRepoPetItemNamePattern by patternGroup.pattern(
        "item.name.neu.format",
        "(?:§f§f)?§7\\[Lvl (?:1➡(?:100|200)|\\{LVL})] (?<name>.*)",
    )

    /**
     * REGEX-TEST: §7To Select Process (Slot #2)
     * REGEX-TEST: §7To Select Process (Slot #4)
     * REGEX-TEST: §7To Select Process (Slot #7)
     * REGEX-TEST: §7To Select Process
     */
    private val forgeBackMenuPattern by patternGroup.pattern(
        "menu.forge.goback",
        "§7To Select Process(?: \\(Slot #\\d\\))?",
    )

    /**
     * REGEX-TEST:  §r§7[Lvl 100] §r§dEndermite
     * REGEX-TEST:  §r§7[Lvl 200] §r§8[§r§6108§r§8§r§4✦§r§8] §r§6Golden Dragon
     * REGEX-TEST:  §r§7[Lvl 100] §r§dBlack Cat§r§d ✦
     */
    @Suppress("MaxLineLength")
    private val petWidgetPattern by patternGroup.pattern(
        "widget.pet",
        "^ §r§7\\[Lvl (?<level>\\d+)](?: (?:§.)+\\[(?:§.)+(?<overflow>\\d+)(?:§.)+✦(?:§.)+])? §r§(?<rarity>.)(?<name>[\\w ]+)(?:§r(?<skin>§. ✦))?\$",
    )

    /**
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 100] §6Scatha§e! §a§lVIEW RULE
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 99] §6Flying Fish§e! §a§lVIEW RULE
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 100] §dBlack Cat§d ✦§e! §a§lVIEW RULE
     */
    private val autopetMessagePattern by patternGroup.pattern(
        "chat.autopet",
        "^§cAutopet §eequipped your §7(?<pet>\\[Lvl \\d{1,3}] §.[\\w ]+)(?:§. ✦)?§e! §a§lVIEW RULE\$",
    )

    /**
     * REGEX-TEST: §aYou despawned your §r§6Golden Dragon§r§a!
     * REGEX-TEST: §aYou despawned your §r§6Silverfish§r§5 ✦§r§a!
     */
    private val chatDespawnPattern by patternGroup.pattern(
        "chat.despawn",
        "§aYou despawned your §r.*§r§a!",
    )

    /**
     * REGEX-TEST: §aYou summoned your §r§6Silverfish§r§5 ✦§r§a!
     * REGEX-TEST: §aYou summoned your §r§6Golden Dragon§r§a!
     */
    private val chatSpawnPattern by patternGroup.pattern(
        "chat.spawn",
        "§aYou summoned your §r(?<pet>.*)§r§a!"
    )

    /**
     * REGEX-TEST: §aYour pet is now holding §r§9Bejeweled Collar§r§a.
     */
    private val petItemMessagePattern by patternGroup.pattern(
        "chat.pet.item.equip",
        "^§aYour pet is now holding §r(?<petItem>§.[\\w -]+)§r§a\\.\$",
    )

    /**
     * REGEX-TEST: §7§7Selected pet: §6Hedgehog
     */
    private val inventorySelectedPetPattern by PetAPI.patternGroup.pattern(
        "inventory.selected",
        "§7§7Selected pet: §?(?<rarity>.)?(?<pet>.*)"
    )

    /**
     * REGEX-TEST: §7Progress to Level 91: §e0%
     * REGEX-TEST: §7Progress to Level 147: §e37.1%
     * REGEX-TEST: §b§lMAX LEVEL
     */
    private val inventorySelectedProgressPattern by PetAPI.patternGroup.pattern(
        "inventory.selected.progress",
        "§b§lMAX LEVEL|§7Progress to Level (?<level>\\d+): §e(?<percentage>[\\d.]+)%"
    )

    /**
     * REGEX-TEST: §2§l§m             §f§l§m            §r §e713,241.8§6/§e1.4M
     * REGEX-TEST: §2§l§m          §f§l§m               §r §e699,742.8§6/§e1.9M
     * REGEX-TEST: §f§l§m                         §r §e0§6/§e660
     * REGEX-TEST: §8▸ 30,358,983 XP'
     */
    private val inventorySelectedXpPattern by PetAPI.patternGroup.pattern(
        "inventory.selected.xp",
        "(?:§8▸ |(?:§.§l§m *)*)(?:§r §e)?(?<current>[\\d,.kM]+)(?:§6\\/§e)?(?<next>[\\d,.kM]+)?"
    )

    /**
     * REGEX-TEST:  §r§7No pet selected
     * REGEX-TEST:  §r§6Washed-up Souvenir
     * REGEX-TEST:  §r§9Dwarf Turtle Shelmet
     */
    private val widgetStringPattern by PetAPI.patternGroup.pattern(
        "widget.string",
        "^ §r(?<string>§.[\\w -]+)\$",
    )

    /**
     * REGEX-TEST:  §r§b§lMAX LEVEL
     * REGEX-TEST:  §r§6+§r§e21,248,020.7 XP
     * REGEX-TEST:  §r§e15,986.6§r§6/§r§e29k XP §r§6(53.6%)
     */
    @Suppress("MaxLineLength")
    private val xpWidgetPattern by PetAPI.patternGroup.pattern(
        "widget.xp",
        "^ §r§.(?:§l(?<max>MAX LEVEL)|\\+§r§e(?<overflow>[\\d,.]+) XP|(?<currentXP>[\\d,.]+)§r§6/§r§e(?<maxXP>[\\d.km]+) XP §r§6\\((?<percentage>[\\d.%]+)\\))$",
    )

    /**
     * REGEX-TEST: §r, §aEquip: §r, §7[Lvl 99] §r, §6Flying Fish
     * REGEX-TEST: §r, §aEquip: §r, §e⭐ §r, §7[Lvl 100] §r, §dBlack Cat§r, §d ✦
     * REGEX-TEST: §r, §aEquip: §r, §7[Lvl 47] §r, §5Lion
     */
    private val autopetHoverPetPattern by PetAPI.patternGroup.pattern(
        "chat.autopet.hover.pet",
        "^§r, §aEquip: §r,(?: §e⭐ §r,)? §7\\[Lvl (?<level>\\d+)] §r, §(?<rarity>.)(?<pet>[\\w ]+)(?:§r, (?<skin>§. ✦))?\$",
    )

    /**
     * REGEX-TEST: §r, §aHeld Item: §r, §9Mining Exp Boost§r]
     * REGEX-TEST: §r, §aHeld Item: §r, §5Lucky Clover§r]
     * REGEX-TEST: §r, §aHeld Item: §r, §5Fishing Exp Boost§r]
     */
    private val autopetHoverPetItemPattern by PetAPI.patternGroup.pattern(
        "chat.autopet.hover.item",
        "^§r, §aHeld Item: §r, (?<item>§.[\\w -]+)§r]\$",
    )
    // </editor-fold>

    // <editor-fold desc="Helpers">
    fun isPetMenu(inventoryTitle: String, inventoryItems: Map<Int, ItemStack>): Boolean {
        if (!petMenuPattern.matches(inventoryTitle)) return false

        // Otherwise make sure they're not in the Forge menu looking at pets
        return inventoryItems[FORGE_BACK_SLOT]?.getLore().orEmpty().none {
            forgeBackMenuPattern.matches(it)
        }
    }

    fun isCurrentPet(petName: String): Boolean = currentPet?.cleanName?.contains(petName) ?: false

    fun getCleanName(nameWithLevel: String): String? {
        petItemNamePattern.matchMatcher(nameWithLevel) {
            return group("name")
        }
        neuRepoPetItemNamePattern.matchMatcher(nameWithLevel) {
            return group("name")
        }

        return null
    }

    private fun getFakePetLine(level: Int, rarity: LorenzRarity, petName: String, skin: String? = null): String {
        return "§r§7[Lvl $level] §r${rarity.chatColorCode}$petName§r${skin.orEmpty()}"
    }

    fun rarityByColorGroup(color: String): LorenzRarity = LorenzRarity.getByColorCode(color[0])
        ?: ErrorManager.skyHanniError(
            "Unknown rarity",
            Pair("rarity", color),
        )

    private fun levelToXPCommand(input: Array<String>) {
        if (input.size < 3) {
            ChatUtils.userError("Usage: /shcalcpetxp <level> <rarity> <pet>")
            return
        }

        val level = input[0].toIntOrNull()
        if (level == null) {
            ChatUtils.userError("Invalid level '${input[0]}'.")
            return
        }
        val rarity = LorenzRarity.getByName(input[1])
        if (rarity == null) {
            ChatUtils.userError("Invalid rarity '${input[1]}'.")
            return
        }

        val petName = input.slice(2..<input.size).joinToString(" ")
        val xp: Double = levelToXP(level, rarity, petName) ?: run {
            ChatUtils.userError("Invalid level or rarity.")
            return
        }
        ChatUtils.chat(xp.addSeparators())
        return
    }

    private fun levelToXP(level: Int, rarity: LorenzRarity, petName: String): Double? {
        val newPetName = petName.toInternalName().toString()

        val rarityOffset = getRarityOffset(rarity, newPetName) ?: return null
        if (!isValidLevel(level, newPetName)) return null

        val xpList = baseXpLevelReqs + getCustomLeveling(newPetName)

        return xpList.slice(0 + rarityOffset..<level + rarityOffset - 1).sum().toDouble()
    }

    private fun isValidLevel(level: Int, petName: String): Boolean {
        val petsData = customXpLevelReqs ?: run {
            ErrorManager.skyHanniError("NEUPetsData is null")
        }

        val maxLevel = petsData[petName]?.maxLevel ?: return false
        return maxLevel >= level
    }

    private fun getCustomLeveling(petName: String): List<Int> {
        return customXpLevelReqs?.get(petName)?.petLevels.orEmpty()
    }

    private fun getRarityOffset(rarity: LorenzRarity, petName: String): Int? {
        val petsData = customXpLevelReqs ?: run {
            ErrorManager.skyHanniError("NEUPetsData is null")
        }

        return if (petName in petsData.keys) {
            val petData = petsData[petName]
            petData?.rarityOffset?.get(rarity)
        } else {
            when (rarity) {
                LorenzRarity.COMMON -> 0
                LorenzRarity.UNCOMMON -> 6
                LorenzRarity.RARE -> 11
                LorenzRarity.EPIC -> 16
                LorenzRarity.LEGENDARY -> 20
                LorenzRarity.MYTHIC -> 20
                else -> {
                    ChatUtils.userError("Invalid Rarity \"${rarity.name}\"")
                    null
                }
            }
        }
    }

    private fun updatePet(newPet: PetData?) {
        if (newPet == currentPet) return
        val oldPet = currentPet
        currentPet = newPet
        if (SkyHanniMod.feature.dev.debug.petEventMessages) {
            ChatUtils.debug("oldPet: " + oldPet.toString().convertToUnformatted())
            ChatUtils.debug("newPet: " + newPet?.toString()?.convertToUnformatted())
        }
        PetChangeEvent(oldPet, newPet).post()
    }

    private fun handlePetMessageBlock(event: LorenzChatEvent) {
        if (!config.hideAutopet) return
        val spawnMatches = chatSpawnPattern.matches(event.message)
        val despawnMatches = chatDespawnPattern.matches(event.message)
        val autoPetMatches = autopetMessagePattern.matches(event.message)
        if (spawnMatches || despawnMatches || autoPetMatches) {
            event.blockedReason = "pets"
        }
    }
    // </editor-fold>

    // <editor-fold desc="Pet Data Extractors (Widget)">
    private fun handleWidgetPetLine(line: String, newPetLine: String): PetData? = petWidgetPattern.matchMatcher(line) {
        val rarity = rarityByColorGroup(group("rarity"))
        val petName = groupOrNull("name").orEmpty()
        val level = groupOrNull("level")?.toInt() ?: 0
        val xp = levelToXP(level, rarity, petName) ?: return null

        return PetData(
            petNameToInternalName(petName, rarity),
            petName,
            rarity,
            null,
            level,
            xp,
            newPetLine,
        )
    }

    private fun handleWidgetStringLine(line: String): NEUInternalName? = widgetStringPattern.matchMatcher(line) {
        val string = group("string")
        if (string == "No pet selected") {
            updatePet(null)
            return null
        }
        return NEUInternalName.fromItemNameOrNull(string)
    }

    private fun handleWidgetXPLine(line: String): Double? = xpWidgetPattern.matchMatcher(line) {
        if (hasGroup("max")) return null

        group("overflow")?.formatDouble() ?: group("currentXP")?.formatDouble()
    }
    // </editor-fold>

    // <editor-fold desc="Pet Data Extractors (AutoPet)">
    private fun onAutopetMessage(event: LorenzChatEvent) {
        val hoverMessage = event.chatComponent.hover?.siblings?.joinToString("")?.split("\n") ?: return

        val (petData, _) = parsePetData(
            hoverMessage,
            { readAutopetItemMessage(it) },
            { null }, // No overflow XP handling in this case
            { readAutopetMessage(it) }
        ) ?: return

        updatePet(petData)
    }

    private fun readAutopetMessage(string: String): PetData? = autopetHoverPetPattern.matchMatcher(string) {
        val level = group("level").toInt()
        val rarity = rarityByColorGroup(group("rarity"))
        val petName = group("pet")
        val skin = groupOrNull("skin").takeIf { it != null }

        return PetData(
            internalName = petNameToInternalName(petName, rarity),
            cleanName = petName,
            rarity = rarity,
            level = level,
            xp = levelToXP(level, rarity, petName) ?: 0.0,
            rawPetName = getFakePetLine(level, rarity, petName, skin),
        )
    }

    private fun readAutopetItemMessage(string: String): NEUInternalName? = autopetHoverPetItemPattern.matchMatcher(string) {
        NEUInternalName.fromItemNameOrNull(group("item"))
    }
    // </editor-fold>

    // <editor-fold desc="Pet Data Extractors (Selected Pet)">
    private fun extractSelectedPetData(lore: List<String>): Triple<Int, LorenzRarity, String>? {
        val level = inventorySelectedProgressPattern.firstMatchGroup(lore, "level")?.toInt()
        val rarity = inventorySelectedPetPattern.firstMatchGroup(lore, "rarity")?.let { rarityByColorGroup(it) }
        val petName = inventorySelectedPetPattern.firstMatchGroup(lore, "pet")

        return if (level != null && rarity != null && petName != null) {
            Triple(level, rarity, petName)
        } else null
    }

    private fun handleSelectedPetName(lore: List<String>): NEUInternalName? = inventorySelectedPetPattern.firstMatcher(lore) {
        val (_, rarity, petName) = extractSelectedPetData(lore) ?: return null
        petNameToInternalName(petName, rarity)
    }

    private fun handleSelectedPetOverflowXp(lore: List<String>): Double? {
        // Only have overflow if `next` group is absent
        if (inventorySelectedXpPattern.firstMatchGroup(lore, "next") != null) return 0.0
        val (level, rarity, petName) = extractSelectedPetData(lore) ?: return null
        val maxXpNeeded = levelToXP(level, rarity, petName)
        val currentXp = inventorySelectedXpPattern.firstMatchGroup(lore, "current")?.formatDouble() ?: 0.0
        return maxXpNeeded?.minus(currentXp) ?: 0.0
    }

    private fun handleSelectedPetData(lore: List<String>): PetData? {
        val (level, rarity, petName) = extractSelectedPetData(lore) ?: return null
        val partialXp = inventorySelectedXpPattern.firstMatchGroup(lore, "current")?.formatDouble() ?: 0.0
        val nextExists = inventorySelectedXpPattern.firstMatchGroup(lore, "next") != null
        val totalXp = partialXp + if (nextExists) (levelToXP(level, rarity, petName) ?: return null) else 0.0
        return PetData(
            internalName = petNameToInternalName(petName, rarity),
            cleanName = petName,
            rarity = rarity,
            petItem = null,
            rawPetName = getFakePetLine(level, rarity, petName),
            level = level,
            xp = totalXp,
        )
    }
    // </editor-fold>

    // <editor-fold desc="Event Handlers">
    @HandleEvent
    fun onCommandRegistration(event: CommandRegistrationEvent) {
        event.register("shpetxp") {
            description = "Calculates the pet xp from a given level and rarity."
            category = CommandCategory.DEVELOPER_TEST
            callback { levelToXPCommand(it) }
        }
    }

    @HandleEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.PET)) return

        val newPetLine = petWidgetPattern.firstMatches(event.lines)?.trim() ?: return
        if (newPetLine == currentPet?.rawPetName) return

        val (petData, overflowXP) = parsePetData(
            event.lines,
            { handleWidgetStringLine(it) },
            { handleWidgetXPLine(it) },
            { handleWidgetPetLine(it, newPetLine) }
        ) ?: return

        updatePet(petData.copy(xp = petData.xp?.plus(overflowXP)))
    }

    @HandleEvent
    fun onNEURepoReload(event: NeuRepositoryReloadEvent) {
        val data = event.getConstant<NEUPetsJson>("pets")
        baseXpLevelReqs = data.petLevels
        customXpLevelReqs = data.customPetLeveling
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        handlePetMessageBlock(event)
        if (autopetMessagePattern.matches(event.message)) {
            onAutopetMessage(event)
            return
        }
        petItemMessagePattern.matchMatcher(event.message) {
            val item = NEUInternalName.fromItemNameOrNull(group("petItem")) ?: ErrorManager.skyHanniError(
                "Couldn't parse pet item name.",
                Pair("message", event.message),
                Pair("item", group("petItem")),
            )
            val newPet = currentPet?.copy(petItem = item) ?: return
            updatePet(newPet)
        }
    }

    @SubscribeEvent
    fun onInventoryOpen(event: InventoryFullyOpenedEvent) {
        inPetMenu = isPetMenu(event.inventoryName, event.inventoryItems)
        if (!inPetMenu) return

        val lore = event.inventoryItems[4]?.getLore() ?: return
        val (petData, overflowXp) = parsePetDataLists(
            lore,
            { handleSelectedPetName(lore) },
            { handleSelectedPetOverflowXp(lore) },
            { handleSelectedPetData(lore) }
        ) ?: return
        updatePet(petData.copy(xp = petData.xp?.plus(overflowXp)))
    }

    @SubscribeEvent
    fun onInventoryClose(event: InventoryCloseEvent) {
        inPetMenu = false
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!inPetMenu) return
        if (event.clickTypeEnum != GuiContainerEvent.ClickType.NORMAL) return
        val category = event.item?.getItemCategoryOrNull() ?: return
        if (category != ItemCategory.PET) return

        updatePet(parsePetAsItem(event.item))
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("PetAPI")
        if (currentPet?.isInitialized() == false) {
            event.addIrrelevant("no pet equipped")
            return
        }
        event.addIrrelevant {
            add("petName: '${currentPet?.internalName ?: ""}'")
            add("petRarity: '${currentPet?.rarity?.rawName.orEmpty()}'")
            add("petItem: '${currentPet?.petItem ?: ""}'")
            add("petLevel: '${currentPet?.level ?: 0}'")
            add("petXP: '${currentPet?.xp ?: 0.0}'")
            add("rawPetLine: '${currentPet?.rawPetName.orEmpty()}'")
        }
    }
    // </editor-fold>
}
