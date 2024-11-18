package at.hannibal2.skyhanni.features.inventory

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.ReforgeAPI
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.data.model.SkyblockStatList
import at.hannibal2.skyhanni.events.GuiContainerEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.InventoryCloseEvent
import at.hannibal2.skyhanni.events.InventoryFullyOpenedEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.ItemUtils.cleanName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.name
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzRarity
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.NumberUtil.toStringWithPlus
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.RenderUtils.drawSlotText
import at.hannibal2.skyhanni.utils.RenderUtils.highlight
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.SkyBlockItemModifierUtils.getReforgeName
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.TimeUtils.ticks
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.concurrent.atomic.AtomicBoolean
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.string as renderableString

@SkyHanniModule
object ReforgeHelper {

    private val config get() = SkyHanniMod.feature.inventory.helper.reforge

    private val repoGroup = RepoPattern.group("reforge")

    private val reforgeMenu by repoGroup.pattern(
        "menu.blacksmith",
        "Reforge Item",
    )
    private val reforgeHexMenu by repoGroup.pattern(
        "menu.hex",
        "The Hex ➜ Reforges",
    )

    /**
     * REGEX-TEST: §aYou reforged your §r§9Gentle Dreadlord Sword §r§ainto a §r§9Heroic Dreadlord Sword§r§a!
     */
    private val reforgeChatMessage by repoGroup.pattern(
        "chat.success",
        "§aYou reforged your .* §r§ainto a .*!|§aYou applied a .* §r§ato your .*!",
    )

    /**
     * REGEX-TEST: §cWait a moment before reforging again!
     */
    private val reforgeChatFail by repoGroup.pattern(
        "chat.fail",
        "§cWait a moment before reforging again!|§cWhoa! Slow down there!",
    )

    private var isInReforgeMenu = false
    private var isInHexReforgeMenu = false

    private fun isReforgeMenu(chestName: String) = reforgeMenu.matches(chestName)
    private fun isHexReforgeMenu(chestName: String) = reforgeHexMenu.matches(chestName)

    private fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled && isInReforgeMenu

    private var itemToReforge: ItemStack? = null
    private var inventoryContainer: Container? = null

    private var currentReforge: ReforgeAPI.Reforge? = null
    private var reforgeToSearch: ReforgeAPI.Reforge? = null

    private var hoveredReforge: ReforgeAPI.Reforge? = null

    private val reforgeItem get() = if (isInHexReforgeMenu) 19 else 13
    private val reforgeButton get() = if (isInHexReforgeMenu) 48 else 22

    private const val HEX_REFORGE_NEXT_DOWN_BUTTON = 35
    private const val HEX_REFORGE_NEXT_UP_BUTTON = 17

    private const val EXIT_BUTTON = 40

    private var waitForChat = AtomicBoolean(false)

    /** Gatekeeps instant double switches of the state */
    private var waitDelay = false

    private var sortAfter: SkyblockStat? = null

    private var display: List<Renderable> = generateDisplay()

    private val hoverColor = LorenzColor.GOLD.addOpacity(50)
    private val selectedColor = LorenzColor.BLUE.addOpacity(100)
    private val finishedColor = LorenzColor.GREEN.addOpacity(75)

    private fun itemUpdate() {
        val newItem = inventoryContainer?.getSlot(reforgeItem)?.stack
        if (newItem?.getInternalName() != itemToReforge?.getInternalName()) {
            reforgeToSearch = null
        }
        itemToReforge = newItem
        val newReforgeName = itemToReforge?.getReforgeName().orEmpty()
        if (newReforgeName == currentReforge?.lowercaseName) return
        currentReforge = ReforgeAPI.reforgeList.firstOrNull { it.lowercaseName == newReforgeName }
        updateDisplay()
    }

    @SubscribeEvent
    fun onClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!isEnabled()) return
        if (event.slot?.slotNumber == reforgeButton) {
            if (event.slot.stack?.name == "§eReforge Item" || event.slot.stack?.name == "§cError!") return
            if (handleReforgeButtonClick(event)) return
        }

        DelayedRun.runNextTick {
            itemUpdate()
        }
    }

    private fun handleReforgeButtonClick(event: GuiContainerEvent.SlotClickEvent): Boolean {
        if (currentReforge == reforgeToSearch) {
            event.cancel()
            waitForChat.set(false)
            SoundUtils.playBeepSound()
        } else if (waitForChat.get()) {
            waitDelay = true
            event.cancel()
        } else {
            if (event.clickedButton == 2) return true
            if (waitDelay) {
                waitDelay = false
            } else {
                waitForChat.set(true)
            }
        }
        return false
    }

    @SubscribeEvent
    fun onChat(event: LorenzChatEvent) {
        if (!isEnabled()) return
        when {
            reforgeChatMessage.matches(event.message) -> {
                DelayedRun.runDelayed(2.ticks) {
                    itemUpdate()
                    waitForChat.set(false)
                }
                if (config?.hideChat == true) {
                    event.blockedReason = "reforge_hide"
                }
            }

            reforgeChatFail.matches(event.message) -> {
                DelayedRun.runDelayed(2.ticks) {
                    waitForChat.set(false)
                }
                if (config?.hideChat == true) {
                    event.blockedReason = "reforge_hide"
                }
            }
        }
    }

    @SubscribeEvent
    fun onOpen(event: InventoryFullyOpenedEvent) {
        if (!LorenzUtils.inSkyBlock) return
        when {
            isHexReforgeMenu(event.inventoryName) -> {
                isInHexReforgeMenu = true
                DelayedRun.runDelayed(2.ticks) {
                    itemUpdate() // update since an item must already be in place
                }
            }

            isReforgeMenu(event.inventoryName) -> {
                itemToReforge = null
                currentReforge = null
            }

            else -> return
        }
        isInReforgeMenu = true
        waitForChat.set(false)
        DelayedRun.runNextTick {
            inventoryContainer = Minecraft.getMinecraft().thePlayer.openContainer
        }
    }

    @SubscribeEvent
    fun onClose(event: InventoryCloseEvent) {
        if (!isInReforgeMenu) return
        isInReforgeMenu = false
        isInHexReforgeMenu = false
        reforgeToSearch = null
        currentReforge = null
        hoveredReforge = null
        sortAfter = null
        itemToReforge = null
        display = emptyList()
    }

    private fun updateDisplay() {
        display = generateDisplay()
    }

    private fun generateDisplay() = buildList<Renderable> {
        this.add(renderableString("§6Reforge Overlay"))

        val item = itemToReforge ?: run {
            reforgeToSearch = null
            return@buildList
        }

        val internalName = item.getInternalName()
        val itemType = item.getItemCategoryOrNull()
        val itemRarity = item.getItemRarityOrNull() ?: return@buildList

        val rawReforgeList =
            if (!isInHexReforgeMenu && config.reforgeStonesOnlyHex) ReforgeAPI.nonePowerStoneReforge else ReforgeAPI.reforgeList
        val reforgeList = rawReforgeList.filter { it.isValid(itemType, internalName) }

        val statTypes = reforgeList.mapNotNull { it.stats[itemRarity]?.keys }.flatten().toSet()

        val statTypeButtons = (listOf(getStatButton(null)) + statTypes.map { getStatButton(it) }).chunked(9)
        this.add(Renderable.table(statTypeButtons, xPadding = 3, yPadding = 2))

        val list = reforgeList.sortedWith(getSortSelector(itemRarity, sortAfter)).map(getReforgeView(itemRarity))
        this.addAll(list)
    }

    private fun getReforgeColor(reforge: ReforgeAPI.Reforge) = when {
        currentReforge == reforge -> "§6"
        reforgeToSearch == reforge -> "§3"
        reforge.isReforgeStone -> "§9"
        else -> "§7"
    }

    private fun getReforgeView(itemRarity: LorenzRarity): (ReforgeAPI.Reforge) -> Renderable = { reforge ->
        val text = getReforgeColor(reforge) + reforge.name
        val tips = getReforgeTips(reforge, itemRarity)
        val onHover = if (!isInHexReforgeMenu) {
            {}
        } else {
            { hoveredReforge = reforge }
        }

        Renderable.clickAndHover(
            text, tips,
            onClick = {
                SoundUtils.playClickSound()
                reforgeToSearch = reforge
                updateDisplay()
            },
            onHover = onHover,
        )
    }

    private fun getReforgeTips(
        reforge: ReforgeAPI.Reforge,
        itemRarity: LorenzRarity,
    ): List<Renderable> {
        val stats: List<Renderable>
        val removedEffect: List<Renderable>
        val addEffectText: String
        val click: List<Renderable>
        if (currentReforge == reforge) {
            stats = currentReforge?.stats?.get(itemRarity)?.print().orEmpty()
            removedEffect = emptyList()
            addEffectText = "§aEffect:"
            click = listOf(renderableString(""), renderableString("§3Reforge is currently applied!"))
        } else {
            stats = reforge.stats[itemRarity]?.print(currentReforge?.stats?.get(itemRarity)).orEmpty()
            removedEffect = getReforgeEffect(
                currentReforge,
                itemRarity,
            )?.let { listOf(renderableString("§cRemoves Effect:")) + it }?.takeIf { config.showDiff }.orEmpty()
            addEffectText = "§aAdds Effect:"
            click = if (reforgeToSearch != reforge) {
                listOf(renderableString(""), renderableString("§eClick to select!"))
            } else emptyList()
        }

        val addedEffect = getReforgeEffect(reforge, itemRarity)?.let {
            listOf(renderableString(addEffectText)) + it
        }.orEmpty()

        return listOf(renderableString("§6Reforge Stats")) + stats + removedEffect + addedEffect + click
    }

    private fun getReforgeEffect(reforge: ReforgeAPI.Reforge?, rarity: LorenzRarity) =
        reforge?.extraProperty?.get(rarity)?.let {
            Renderable.wrappedString(
                it,
                190,
                color = LorenzColor.GRAY.toColor(),
            )
        }

    private fun getSortSelector(
        itemRarity: LorenzRarity,
        sorting: SkyblockStat?,
    ): Comparator<ReforgeAPI.Reforge> =
        if (sorting != null) {
            Comparator.comparing<ReforgeAPI.Reforge, Double> { it.stats[itemRarity]?.get(sorting) ?: 0.0 }.reversed()
        } else {
            Comparator.comparing { it.isReforgeStone }
        }

    private fun getStatButton(stat: SkyblockStat?): Renderable {
        val icon: String
        val tip: String
        if (stat == null) {
            icon = "§7D"
            tip = "§7Default"
        } else {
            icon = stat.icon
            tip = stat.iconWithName
        }

        val alreadySelected = sortAfter == stat
        val fieldColor = if (alreadySelected) LorenzColor.GRAY else LorenzColor.DARK_GRAY


        val tips = if (alreadySelected) {
            listOf("§6Sort by", tip)
        } else {
            listOf("§6Sort by", tip, "", "§eClick to apply sorting!")
        }
        val sortField =
            Renderable.drawInsideRoundedRect(
                Renderable.hoverTips(
                    Renderable.fixedSizeLine(
                        renderableString(icon, horizontalAlign = RenderUtils.HorizontalAlignment.CENTER),
                        SkyblockStat.fontSizeOfLargestIcon,
                    ),
                    tips,
                ),
                fieldColor.toColor(), radius = 15, padding = 1,
            )
        return if (alreadySelected) {
            sortField
        } else {
            Renderable.clickable(
                sortField,
                {
                    sortAfter = stat
                    updateDisplay()
                },
            )
        }
    }

    @SubscribeEvent
    fun onRender(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        if (!isEnabled()) return
        config.position.renderRenderables(display, posLabel = "Reforge Overlay")
    }

    @SubscribeEvent
    fun onForegroundDrawn(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!isEnabled()) return
        if (currentReforge == null) return

        inventoryContainer?.getSlot(reforgeItem)?.let {
            event.drawSlotText(it.xDisplayPosition - 5, it.yDisplayPosition, "§e${currentReforge?.name}", 1f)
        }
    }

    @SubscribeEvent
    fun onGuiContainerBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (hoveredReforge != null && isInHexReforgeMenu) {
            if (hoveredReforge != currentReforge) {
                colorReforgeStone(hoverColor, hoveredReforge?.rawReforgeStoneName ?: "Random Basic Reforge")
            } else {
                inventoryContainer?.getSlot(reforgeItem)?.highlight(hoverColor)
            }
            hoveredReforge = null
        }

        if (reforgeToSearch == null) return
        if (reforgeToSearch != currentReforge) {
            colorSelected()
        } else {
            inventoryContainer?.getSlot(reforgeItem)?.highlight(finishedColor)
        }
    }

    private fun colorSelected() = if (reforgeToSearch?.isReforgeStone == true) {
        if (isInHexReforgeMenu) {
            colorReforgeStone(selectedColor, reforgeToSearch?.rawReforgeStoneName)
        } else {
            inventoryContainer?.getSlot(EXIT_BUTTON)?.highlight(selectedColor)
        }
    } else {
        inventoryContainer?.getSlot(reforgeButton)?.highlight(selectedColor)
    }

    private fun colorReforgeStone(color: Color, reforgeStone: String?) {
        val inventory = inventoryContainer?.inventorySlots ?: return
        val slot = inventory.firstOrNull { it?.stack?.cleanName() == reforgeStone }
        if (slot != null) {
            slot highlight color
        } else {
            inventory[HEX_REFORGE_NEXT_DOWN_BUTTON]?.takeIf { it.stack.item == Items.skull }?.highlight(color)
            inventory[HEX_REFORGE_NEXT_UP_BUTTON]?.takeIf { it.stack.item == Items.skull }?.highlight(color)
        }
    }

    private fun SkyblockStatList.print(appliedReforge: SkyblockStatList? = null): List<Renderable> {
        val diff = appliedReforge?.takeIf { config.showDiff }?.let { this - it }
        val main = (
            (diff ?: this).mapNotNull {
                val key = it.key
                val value = this[key] ?: 0.0
                buildList {
                    add(renderableString("§9${value.toStringWithPlus().removeSuffix(".0")}"))
                    diff?.get(key)?.let { add(renderableString((if (it < 0) "§c" else "§a") + it.toStringWithPlus().removeSuffix(".0"))) }
                    add(renderableString(key.iconWithName))
                }
            }
            )
        val table = Renderable.table(main, 5)
        return listOf(table)
    }
}

