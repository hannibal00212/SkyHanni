package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.data.jsonobjects.repo.PetsJson
import at.hannibal2.skyhanni.data.model.TabWidget
import at.hannibal2.skyhanni.events.DebugDataCollectEvent
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
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
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.RegexUtils.matchMatcher
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.chat.Text.hover
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object PetAPI {
    private val patternGroup = RepoPattern.group("misc.pet")
    private val petMenuPattern by patternGroup.pattern(
        "menu.title",
        "Pets(?: \\(\\d+/\\d+\\) )?",
    )

    private var pet: PetData? = null
    private var inPetMenu = false

    private var xpLeveling: List<Int> = listOf()
    private var xpLevelingGoldenDragon: List<Int> = listOf()

    /**
     * REGEX-TEST: §e⭐ §7[Lvl 200] §6Golden Dragon§d ✦
     * REGEX-TEST: ⭐ [Lvl 100] Black Cat ✦
     */
    private val petItemName by patternGroup.pattern(
        "item.name",
        "(?<favorite>(?:§.)*⭐ )?(?:§.)*\\[Lvl (?<level>\\d+)] (?<name>.*)",
    )
    private val neuRepoPetItemName by patternGroup.pattern(
        "item.name.neu.format",
        "(?:§f§f)?§7\\[Lvl (?:1➡(?:100|200)|\\{LVL})] (?<name>.*)",
    )

    /**
     * REGEX-TEST: §e⭐ §7[Lvl 100] §6Ender Dragon
     * REGEX-TEST: §e⭐ §7[Lvl 100] §dBlack Cat§d ✦
     * REGEX-TEST: §7[Lvl 100] §6Mole
     */
    private val petNameMenu by patternGroup.pattern(
        "menu.pet.name",
        "^(?:§e(?<favorite>⭐) )?(?:§.)*\\[Lvl (?<level>\\d+)] §(?<rarity>.)(?<name>[\\w ]+)(?<skin>§. ✦)?\$"
    )

    /**
     * REGEX-TEST: §6Held Item: §9Mining Exp Boost
     * REGEX-TEST: §6Held Item: §fAll Skills Exp Boost
     * REGEX-TEST: §6Held Item: §9Dwarf Turtle Shelmet
     */
    private val petItemMenu by patternGroup.pattern(
        "menu.pet.item",
        "^§6Held Item: (?<item>§.[\\w -]+)\$"
    )

    /**
     * REGEX-TEST: §7Progress to Level 45: §e94.4%
     * REGEX-TEST: §8▸ 25,396,280 XP
     * REGEX-TEST: §7Progress to Level 58: §e3.3%
     */
    private val petXPMenu by patternGroup.pattern(
        "menu.pet.xp",
        "§.(?:Progress to Level (?<level>\\d+): §e(?<percentage>[\\d.]+)%|▸ (?<totalXP>[\\d,.]+) XP)\$"
    )

    /**
     * REGEX-TEST: §7§cClick to despawn!
     */
    private val petDespawnMenu by patternGroup.pattern(
        "menu.pet.despawn",
        "§7§cClick to despawn!"
    )

    /**
     * REGEX-TEST:  §r§7[Lvl 100] §r§dEndermite
     * REGEX-TEST:  §r§7[Lvl 200] §r§8[§r§6108§r§8§r§4✦§r§8] §r§6Golden Dragon
     * REGEX-TEST:  §r§7[Lvl 100] §r§dBlack Cat§r§d ✦
     */
    private val petWidget by patternGroup.pattern(
        "widget.pet",
        "^ §r§7\\[Lvl (?<level>\\d+)](?: (?:§.)+\\[(?:§.)+(?<overflow>\\d+)(?:§.)+✦(?:§.)+])? §r§(?<rarity>.)(?<name>[\\w ]+)(?:§r(?<skin>§. ✦))?\$",
    )

    /**
     * REGEX-TEST:  §r§7No pet selected
     * REGEX-TEST:  §r§6Washed-up Souvenir
     * REGEX-TEST:  §r§9Dwarf Turtle Shelmet
     */
    private val widgetString by patternGroup.pattern(
        "widget.string",
        "^ §r(?<string>§.[\\w -]+)\$",
    )

    /**
     * REGEX-TEST:  §r§b§lMAX LEVEL
     * REGEX-TEST:  §r§6+§r§e21,248,020.7 XP
     * REGEX-TEST:  §r§e15,986.6§r§6/§r§e29k XP §r§6(53.6%)
     */
    private val xpWidget by patternGroup.pattern(
        "widget.xp",
        "^ §r§.(?:§l(?<max>MAX LEVEL)|\\+§r§e(?<overflow>[\\d,.]+) XP|(?<currentXP>[\\d,.]+)§r§6/§r§e(?<maxXP>[\\d.km]+) XP §r§6\\((?<percentage>[\\d.%]+)\\))$",
    )

    /**
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 100] §6Scatha§e! §a§lVIEW RULE
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 99] §6Flying Fish§e! §a§lVIEW RULE
     * REGEX-TEST: §cAutopet §eequipped your §7[Lvl 100] §dBlack Cat§d ✦§e! §a§lVIEW RULE
     */
    private val autopetMessage by patternGroup.pattern(
        "chat.autopet",
        "^§cAutopet §eequipped your §7(?<pet>\\[Lvl \\d{1,3}] §.[\\w ]+)(?:§. ✦)?§e! §a§lVIEW RULE\$"
    )

    /**
     * REGEX-TEST: §r, §aEquip: §r, §7[Lvl 99] §r, §6Flying Fish
     * REGEX-TEST: §r, §aEquip: §r, §e⭐ §r, §7[Lvl 100] §r, §dBlack Cat§r, §d ✦
     * REGEX-TEST: §r, §aEquip: §r, §7[Lvl 47] §r, §5Lion
     */
    private val autopetHoverPet by patternGroup.pattern(
        "chat.autopet.hover.pet",
        "^§r, §aEquip: §r,(?: §e⭐ §r,)? §7\\[Lvl (?<level>\\d+)] §r, §(?<rarity>.)(?<pet>[\\w ]+)(?:§r, (?<skin>§. ✦))?\$"
    )

    /**
     * REGEX-TEST: §r, §aHeld Item: §r, §9Mining Exp Boost§r]
     * REGEX-TEST: §r, §aHeld Item: §r, §5Lucky Clover§r]
     * REGEX-TEST: §r, §aHeld Item: §r, §5Fishing Exp Boost§r]
     */
    private val autopetHoverPetItem by patternGroup.pattern(
        "chat.autopet.hover.item",
        "^§r, §aHeld Item: §r, (?<item>§.[\\w -]+)§r]\$"
    )

    private val ignoredPetStrings = listOf(
        "Archer",
        "Berserk",
        "Mage",
        "Tank",
        "Healer",
        "➡",
    )

    fun isPetMenu(inventoryTitle: String): Boolean = petMenuPattern.matches(inventoryTitle)

    // Contains color code + name and for older SkyHanni users maybe also the pet level
    @Deprecated(message = "use PetAPI.pet.name")
    var currentPet: String?
        get() = ProfileStorageData.profileSpecific?.currentPet?.takeIf { it.isNotEmpty() }
        set(value) {
            ProfileStorageData.profileSpecific?.currentPet = value
        }

    @Deprecated(message = "use PetAPI.pet.rawPetName",
        replaceWith = ReplaceWith("pet.name.contains(petName) ?: false", "at.hannibal2.skyhanni.data.PetAPI.pet")
    )
    fun isCurrentPet(petName: String): Boolean = currentPet?.contains(petName) ?: false

    fun getCleanName(nameWithLevel: String): String? {
        petItemName.matchMatcher(nameWithLevel) {
            return group("name")
        }
        neuRepoPetItemName.matchMatcher(nameWithLevel) {
            return group("name")
        }

        return null
    }

    @Deprecated(message = "use PetAPI.pet.level")
    fun getPetLevel(nameWithLevel: String): Int? = petItemName.matchMatcher(nameWithLevel) {
        group("level").toInt()
    }

    @Deprecated(message = "use PetAPI.pet.name")
    fun hasPetName(name: String): Boolean = petItemName.matches(name) && !ignoredPetStrings.any { name.contains(it) }

// ---
    @SubscribeEvent
    fun onWidgetUpdate(event: WidgetUpdateEvent) {
        if (!event.isWidget(TabWidget.PET)) return

        val newPetLine = event.lines.getOrNull(1)?.removePrefix(" ") ?: return //TODO don't hardcode this
        println("'${pet?.rawPetName}' '$newPetLine'")
        if (newPetLine == pet?.rawPetName) return

        var petItem = NEUInternalName.NONE
        var petXP: Double? = null
        event.lines.forEach { line ->
            val tempPetItem = handleWidgetStringLine(line)
            if (tempPetItem != NEUInternalName.NONE) {
                petItem = tempPetItem
                return@forEach
            }
            val tempPetXP = handleWidgetXPLine(line)
            if (tempPetXP != null) {
                petXP = tempPetXP
                return@forEach
            }
        }

        event.lines.forEach { line ->
            if (handleWidgetPetLine(line, petItem, petXP, newPetLine)) return@forEach
        }
    }

    private fun handleWidgetPetLine(line: String, petItem: NEUInternalName, petXP: Double?, newPetLine: String): Boolean {
        val xpOverLevel = petXP ?: 0.0
        petWidget.matchMatcher(line) {
            val xp = (levelToXP(
                group("level").toInt(),
                LorenzRarity.getByColorCode(group("rarity")[0]) ?: LorenzRarity.ULTIMATE,
                group("name").contains("Golden Dragon")
            ))

            fireEvent(
                PetData(
                    group("name"),
                    LorenzRarity.getByColorCode(group("rarity")[0]) ?: LorenzRarity.ULTIMATE,
                    petItem,
                    group("skin") != null,
                    group("level").toInt(),
                    (xp?.plus(xpOverLevel)) ?: 0.0,
                    newPetLine,
                )
            )
            return true
        }
        return false
    }

    private fun handleWidgetStringLine(line: String): NEUInternalName {
        widgetString.matchMatcher(line) {
            val string = group("string")
            if (string == "No pet selected") {
                PetChangeEvent(pet, null).post()
                pet = null
                return NEUInternalName.NONE
            }
            return NEUInternalName.fromItemNameOrNull(string) ?: NEUInternalName.NONE
        }
        return NEUInternalName.NONE
    }

    private fun handleWidgetXPLine(line: String): Double? {
        xpWidget.matchMatcher(line) {
            if (group("max") != null) return null

            val overflow = group("overflow")?.replace(",", "")?.toDoubleOrNull() ?: 0.0
            val currentXP = group("currentXP")?.replace(",", "")?.toDoubleOrNull() ?: 0.0

            return overflow + currentXP
        }
        return null
    }

    @SubscribeEvent
    fun onAutopet(event: LorenzChatEvent) {
        if (!autopetMessage.matches(event.message)) return

        val hoverMessage = buildList {
            event.chatComponent.hover?.siblings?.forEach {
                add(it.formattedText)
            }
        }.toString().split("\n")

        var petItem = NEUInternalName.NONE
        for (it in hoverMessage) {
            val item = handleAutopetItemMessage(it)
            if (item != null) {
                petItem = item
                break
            }
        }
        hoverMessage.forEach {
            if (handleAutopetMessage(it, petItem)) return
        }
    }

    private fun handleAutopetMessage(string: String, petItem: NEUInternalName): Boolean {
        autopetHoverPet.matchMatcher(string) {
            val level = group("level").toInt()
            val rarity = LorenzRarity.getByColorCode(group("rarity")[0]) ?: LorenzRarity.ULTIMATE
            val petName = group("pet")
            val hasSkin = group("skin") != null

            val fakePetLine = "§r§7[Lvl $level] §r${rarity.chatColorCode}$petName${if (hasSkin) "§r${group("skin")}" else ""}"

            val newPet = PetData(
                petName,
                rarity,
                petItem,
                hasSkin,
                level,
                levelToXP(level, rarity) ?: 0.0,
                fakePetLine,
            )
            fireEvent(newPet)
            return true
        }
        return false
    }

    private fun handleAutopetItemMessage(string: String): NEUInternalName? {
        autopetHoverPetItem.matchMatcher(string) {
            return NEUInternalName.fromItemNameOrNull(group("item"))
        }
        return null
    }

    @SubscribeEvent
    fun onOpenInventory(event: InventoryFullyOpenedEvent) {
        inPetMenu = isPetMenu(event.inventoryName)
    }

    @SubscribeEvent
    fun onCloseInventory(event: InventoryCloseEvent) {
        inPetMenu = false
    }

    @SubscribeEvent
    fun onItemClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!inPetMenu) return
        if (event.clickTypeEnum != GuiContainerEvent.ClickType.NORMAL) return
        val category = event.item?.getItemCategoryOrNull() ?: return
        if (category != ItemCategory.PET) return

        parsePetAsItem(event.item)
    }

    private fun parsePetAsItem(item: ItemStack) {
        val lore = item.getLore()

        if (lore.any { petDespawnMenu.matches(it) }) {
            fireEvent(null)
            return
        }

        getPetDataFromLore(item.displayName, lore)
    }

    private fun getPetDataFromLore(displayName: String, lore: List<String>) {
        var level = 0
        var rarity: LorenzRarity = LorenzRarity.ULTIMATE
        var petName = ""
        var petItem = NEUInternalName.NONE
        var petXP = 0.0
        var skin = ""

        petNameMenu.matchMatcher(displayName) {
            level = group("level").toInt()
            rarity = LorenzRarity.getByColorCode(group("rarity")[0]) ?: LorenzRarity.ULTIMATE
            petName = group("name")
            skin = group("skin") ?: ""
        }

        lore.forEach {
            petItemMenu.matchMatcher(it) {
                petItem = NEUInternalName.fromItemNameOrNull(group("item")) ?: ErrorManager.skyHanniError(
                    "Couldn't parse pet item name.",
                    Pair("lore", it),
                    Pair("item", group("item"))
                )
                return@forEach
            }
            petXPMenu.matchMatcher(it) {
                petXP = group("totalXP")?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                return@forEach
            }
        }

        fireEvent(PetData(
            petName,
            rarity,
            petItem,
            skin != "",
            level,
            petXP,
            "§r§7[Lvl $level] §r${rarity.chatColorCode}$petName${if (skin != "") "§r${skin}" else ""}",
        ))
    }

    fun testLevelToXP(input: Array<String>) {
        if (input.size == 3) {
            val level = input[0].toIntOrNull()
            val rarity = LorenzRarity.getByName(input[1])
            val isGoldenDragon = input[2].toBooleanStrictOrNull()
            if (level != null && rarity != null && isGoldenDragon != null) {
                val xp: Double = levelToXP(level, rarity, isGoldenDragon) ?: run {
                    ChatUtils.userError("bad input. invalid rarity or level")
                    return
                }
                ChatUtils.chat(xp.addSeparators())
                return
            }
        }
        ChatUtils.userError("bad usage. /shcalcpetxp <level> <rarity> <isGdrag>")
    }

    private fun levelToXP(level: Int, rarity: LorenzRarity, isGoldenDragon: Boolean = false): Double? {
        val rarityOffset = getRarityOffset(rarity) ?: return null
        if (!isValidLevel(level, isGoldenDragon)) return null

        return if (isGoldenDragon && level > 100) {
            xpLeveling.slice(0 + rarityOffset..<100 + rarityOffset - 1).sum() + getGoldenDragonXP(level - 100).toDouble()
        } else {
            xpLeveling.slice(0 + rarityOffset..<level + rarityOffset - 1).sum().toDouble()
        }
    }

    private fun isValidLevel(level: Int, isGoldenDragon: Boolean): Boolean {
        return if (isGoldenDragon) level in 1..200
        else level in 1..100
    }

    private fun getGoldenDragonXP(levelAbove100: Int): Int {
        return xpLevelingGoldenDragon.slice(0..<levelAbove100).sum()
    }

    private fun getRarityOffset(rarity: LorenzRarity): Int? = when (rarity) {
        LorenzRarity.COMMON -> 0
        LorenzRarity.UNCOMMON -> 6
        LorenzRarity.RARE -> 11
        LorenzRarity.EPIC -> 16
        LorenzRarity.LEGENDARY -> 20
        LorenzRarity.MYTHIC -> 20
        else -> {
            ChatUtils.userError("bad rarity. ${rarity.name}")
            null
        }
    }

    private fun fireEvent(newPet: PetData?) {
        PetChangeEvent(pet, newPet).post()
        pet = newPet
    }

    @SubscribeEvent
    fun onDebug(event: DebugDataCollectEvent) {
        event.title("PetAPI")
        if (pet != null) {
            event.addIrrelevant {
                add("petName: '${pet?.name}'")
                add("petRarity: '${pet?.rarity}'")
                add("petItem: '${pet?.petItem}'")
                add("petHasSkin: '${pet?.hasSkin}'")
                add("petLevel: '${pet?.level}'")
                add("petXP: '${pet?.xp}'")
                add("rawPetLine: '${pet?.rawPetName}'")
            }
        } else {
            event.addData("no pet equipped")
        }
    }

    @SubscribeEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        val data = event.getConstant<PetsJson>("Pets")
        xpLeveling = data.xpLeveling
        xpLevelingGoldenDragon = data.xpLevelingGoldenDragon
    }

    @HandleEvent
    fun onPetChange(event: PetChangeEvent) {
        ChatUtils.debug("oldPet: ${event.oldPet}, newPet: ${event.newPet}")
    }
}
