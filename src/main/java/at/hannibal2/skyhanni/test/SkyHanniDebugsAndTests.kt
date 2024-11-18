package at.hannibal2.skyhanni.test

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.ConfigGuiManager
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.core.config.Position
import at.hannibal2.skyhanni.data.HypixelData
import at.hannibal2.skyhanni.data.IslandGraphs
import at.hannibal2.skyhanni.events.GuiKeyPressEvent
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.events.LorenzChatEvent
import at.hannibal2.skyhanni.events.LorenzRenderWorldEvent
import at.hannibal2.skyhanni.events.LorenzToolTipEvent
import at.hannibal2.skyhanni.events.ReceiveParticleEvent
import at.hannibal2.skyhanni.events.mining.OreMinedEvent
import at.hannibal2.skyhanni.features.garden.GardenNextJacobContest
import at.hannibal2.skyhanni.features.garden.visitor.GardenVisitorColorNames
import at.hannibal2.skyhanni.features.inventory.bazaar.BazaarApi.getBazaarData
import at.hannibal2.skyhanni.features.mining.OreBlock
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.BlockUtils
import at.hannibal2.skyhanni.utils.BlockUtils.getBlockStateAt
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.editCopy
import at.hannibal2.skyhanni.utils.InventoryUtils
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getNpcPriceOrNull
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getPriceOrNull
import at.hannibal2.skyhanni.utils.ItemPriceUtils.getRawCraftCostOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalName
import at.hannibal2.skyhanni.utils.ItemUtils.getInternalNameOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemCategoryOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.getItemRarityOrNull
import at.hannibal2.skyhanni.utils.ItemUtils.itemName
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LocationUtils
import at.hannibal2.skyhanni.utils.LorenzColor
import at.hannibal2.skyhanni.utils.LorenzDebug
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.toInternalName
import at.hannibal2.skyhanni.utils.NEUItems.getItemStack
import at.hannibal2.skyhanni.utils.NEUItems.getItemStackOrNull
import at.hannibal2.skyhanni.utils.NumberUtil.addSeparators
import at.hannibal2.skyhanni.utils.NumberUtil.roundTo
import at.hannibal2.skyhanni.utils.OSUtils
import at.hannibal2.skyhanni.utils.ReflectionUtils.makeAccessible
import at.hannibal2.skyhanni.utils.RenderUtils.drawDynamicText
import at.hannibal2.skyhanni.utils.RenderUtils.drawWaypointFilled
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderables
import at.hannibal2.skyhanni.utils.RenderUtils.renderString
import at.hannibal2.skyhanni.utils.RenderUtils.renderStringsAndItems
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import at.hannibal2.skyhanni.utils.SoundUtils
import at.hannibal2.skyhanni.utils.renderables.DragNDrop
import at.hannibal2.skyhanni.utils.renderables.Droppable
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.renderBounds
import at.hannibal2.skyhanni.utils.renderables.toDragItem
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object SkyHanniDebugsAndTests {

    private val config get() = SkyHanniMod.feature.dev
    private val debugConfig get() = config.debug
    var displayLine = ""
    var displayList = emptyList<List<Any>>()

    var globalRender = true

    var a = 1.0
    var b = 60.0
    var c = 0.0

    val debugLogger = LorenzLogger("debug/test")

    private fun run(compound: NBTTagCompound, text: String) {
        print("$text'$compound'")
        for (s in compound.keySet) {
            val element = compound.getCompoundTag(s)
            run(element, "$text  ")
        }
    }

    private fun print(text: String) {
        LorenzDebug.log(text)
    }

    private var testLocation: LorenzVec? = null

    @SubscribeEvent
    fun onRenderWorld(event: LorenzRenderWorldEvent) {
        testLocation?.let {
            event.drawWaypointFilled(it, LorenzColor.WHITE.toColor())
            event.drawDynamicText(it, "Test", 1.5)
        }
    }

    fun waypoint(args: Array<String>) {
        SoundUtils.playBeepSound()

        if (args.isEmpty()) {
            testLocation = null
            ChatUtils.chat("reset test waypoint")
            IslandGraphs.stop()
            return
        }

        val x = args[0].toDouble()
        val y = args[1].toDouble()
        val z = args[2].toDouble()
        val location = LorenzVec(x, y, z)
        testLocation = location
        if (args.getOrNull(3) == "pathfind") {
            IslandGraphs.pathFind(location, "/shtestwaypoint", condition = { true })
        }
        ChatUtils.chat("set test waypoint")
    }

    fun testCommand(args: Array<String>) {
        SkyHanniMod.coroutineScope.launch {
            asyncTest(args)
        }
    }

    @Suppress("EmptyFunctionBlock")
    private fun asyncTest(args: Array<String>) {
    }

    fun findNullConfig(args: Array<String>) {
        println("start null finder")
        findNull(SkyHanniMod.feature, "config")
        println("stop null finder")
    }

    private fun findNull(obj: Any, path: String) {

        val blockedNames = listOf(
            "TRUE",
            "FALSE",
            "SIZE",
            "MIN_VALUE",
            "MAX_VALUE",
            "BYTES",
            "POSITIVE_INFINITY",
            "NEGATIVE_INFINITY",
            "NaN",
            "MIN_NORMAL",
        )

        val javaClass = obj.javaClass
        if (javaClass.isEnum) return
        for (field in javaClass.fields) {
            val name = field.name
            if (name in blockedNames) continue

            // funny thing
            if (obj is Position) {
                if (name == "internalName") continue
            }

            val other = field.makeAccessible().get(obj)
            val newName = "$path.$name"
            if (other == null) {
                println("config null at $newName")
            } else {
                findNull(other, newName)
            }
        }
    }

    fun resetConfigCommand() {
        ChatUtils.clickableChat(
            "§cTHIS WILL RESET YOUR SkyHanni CONFIG! Click here to proceed.",
            onClick = { resetConfig() },
            "§eClick to confirm.",
            prefix = false,
            oneTimeClick = true,
        )
    }

    private fun resetConfig() {
        // saving old config state
        SkyHanniMod.configManager.saveConfig(ConfigFileType.FEATURES, "reload config manager")
        SkyHanniMod.configManager.saveConfig(ConfigFileType.SACKS, "reload config manager")
        Thread {
            Thread.sleep(500)
            SkyHanniMod.configManager.disableSaving()

            // initializing a new config manager, calling firstLoad, and setting it as the config manager in use.
            val configManager = ConfigManager()
            configManager.firstLoad()
            SkyHanniMod.Companion::class.java.enclosingClass.getDeclaredField("configManager").makeAccessible()
                .set(SkyHanniMod, configManager)

            // resetting the MoulConfigProcessor in use
            ConfigGuiManager.editor = null
            ChatUtils.chat("Reset the config manager!")
        }.start()
    }

    fun testGardenVisitors() {
        if (displayList.isNotEmpty()) {
            displayList = mutableListOf()
            return
        }

        val bigList = mutableListOf<List<Any>>()
        var list = mutableListOf<Any>()
        var i = 0
        var errors = 0
        for (item in GardenVisitorColorNames.visitorItems) {
            val name = item.key
            i++
            if (i == 5) {
                i = 0
                bigList.add(list)
                list = mutableListOf()
            }

            val coloredName = GardenVisitorColorNames.getColoredName(name)
            list.add("$coloredName§7 (")
            for (itemName in item.value) {
                try {
                    val internalName = NEUInternalName.fromItemName(itemName)
                    list.add(internalName.getItemStack())
                } catch (e: Error) {
                    ChatUtils.debug("itemName '$itemName' is invalid for visitor '$name'")
                    errors++
                }
            }
            if (item.value.isEmpty()) {
                list.add("Any")
            }
            list.add("§7) ")
        }
        bigList.add(list)
        displayList = bigList
        if (errors == 0) {
            ChatUtils.debug("Test garden visitor renderer: no errors")
        } else {
            ChatUtils.debug("Test garden visitor renderer: $errors errors")
        }
    }

    fun reloadListeners() {
        val blockedFeatures = try {
            File("config/skyhanni/blocked-features.txt").readLines().toList()
        } catch (e: Exception) {
            emptyList()
        }

        val modules = SkyHanniMod.modules
        for (original in modules.toMutableList()) {
            val javaClass = original.javaClass
            val simpleName = javaClass.simpleName
            MinecraftForge.EVENT_BUS.unregister(original)
            println("Unregistered listener $simpleName")

            if (simpleName !in blockedFeatures) {
                modules.remove(original)
                val module = javaClass.newInstance()
                modules.add(module)

                MinecraftForge.EVENT_BUS.register(module)
                println("Registered listener $simpleName")
            } else {
                println("Skipped registering listener $simpleName")
            }
        }
        ChatUtils.chat("reloaded ${modules.size} listener classes.")
    }

    fun stopListeners() {
        val modules = SkyHanniMod.modules
        for (original in modules.toMutableList()) {
            val javaClass = original.javaClass
            val simpleName = javaClass.simpleName
            MinecraftForge.EVENT_BUS.unregister(original)
            println("Unregistered listener $simpleName")
        }
        ChatUtils.chat("stopped ${modules.size} listener classes.")
    }

    fun whereAmI() {
        if (LorenzUtils.inSkyBlock) {
            ChatUtils.chat("§eYou are currently in ${LorenzUtils.skyBlockIsland}.")
            return
        }
        ChatUtils.chat("§eYou are not in Skyblock.")
    }

    private var lastManualContestDataUpdate = SimpleTimeMark.farPast()

    fun resetContestData() {
        if (lastManualContestDataUpdate.passedSince() < 30.seconds) {
            ChatUtils.userError("§cYou already reset Jacob's Contest data recently!")
            return
        }
        lastManualContestDataUpdate = SimpleTimeMark.now()

        GardenNextJacobContest.contests.clear()
        GardenNextJacobContest.fetchedFromElite = false
        GardenNextJacobContest.isFetchingContests = true
        SkyHanniMod.coroutineScope.launch {
            GardenNextJacobContest.fetchUpcomingContests()
            GardenNextJacobContest.lastFetchAttempted = System.currentTimeMillis()
            GardenNextJacobContest.isFetchingContests = false
        }
    }

    fun copyLocation(args: Array<String>) {
        val location = LocationUtils.playerLocation()
        val x = (location.x + 0.001).roundTo(1)
        val y = (location.y + 0.001).roundTo(1)
        val z = (location.z + 0.001).roundTo(1)
        val (clipboard, format) = formatLocation(x, y, z, args.getOrNull(0))
        OSUtils.copyToClipboard(clipboard)
        ChatUtils.chat("Copied the current location to clipboard ($format format)!", replaceSameMessage = true)
    }

    private fun formatLocation(x: Double, y: Double, z: Double, parameter: String?): Pair<String, String> = when (parameter) {
        "json" -> "$x:$y:$z" to "json"
        "pathfind" -> "`/shtestwaypoint $x $y $z pathfind`" to "pathfind"
        else -> "LorenzVec($x, $y, $z)" to "LorenzVec"
    }

    fun debugVersion() {
        val name = "SkyHanni ${SkyHanniMod.version}"
        ChatUtils.chat("§eYou are using $name")
        OSUtils.copyToClipboard(name)
    }

    fun copyItemInternalName() {
        val hand = InventoryUtils.getItemInHand()
        if (hand == null) {
            ChatUtils.userError("No item in hand!")
            return
        }

        val internalName = hand.getInternalName().asString()
        OSUtils.copyToClipboard(internalName)
        ChatUtils.chat("§eCopied internal name §7$internalName §eto the clipboard!")
    }

    fun toggleRender() {
        globalRender = !globalRender
        if (globalRender) {
            ChatUtils.chat("§aEnabled global renderer!")
        } else {
            ChatUtils.chat("§cDisabled global renderer! Run this command again to show SkyHanni rendering again.")
        }
    }

    fun testItemCommand(args: Array<String>) {
        if (args.isEmpty()) {
            ChatUtils.userError("Usage: /shtestitem <item name or internal name>")
            return
        }

        val input = args.joinToString(" ")
        val result = buildList {
            add("")
            add("§bSkyHanni Test Item")
            add("§einput: '§f$input§e'")

            NEUInternalName.fromItemNameOrNull(input)?.let { internalName ->
                add("§eitem name -> internalName: '§7${internalName.asString()}§e'")
                add("  §eitemName: '${internalName.itemName}§e'")
                val price = internalName.getPriceOrNull()?.let { "§6" + it.addSeparators() } ?: "§7null"
                add("  §eprice: '§6$price§e'")
                return@buildList
            }

            input.toInternalName().getItemStackOrNull()?.let { item ->
                val itemName = item.itemName
                val internalName = item.getInternalName()
                add("§einternal name: §7${internalName.asString()}")
                add("§einternal name -> item name: '$itemName§e'")
                val price = internalName.getPriceOrNull()?.let { "§6" + it.addSeparators() } ?: "§7null"
                add("  §eprice: '§6$price§e'")
                return@buildList
            }

            add("§cNothing found!")
        }
        ChatUtils.chat(result.joinToString("\n"), prefix = false)
    }

    @SubscribeEvent
    fun onKeybind(event: GuiKeyPressEvent) {
        if (!debugConfig.copyInternalName.isKeyHeld()) return
        val focussedSlot = event.guiContainer.slotUnderMouse ?: return
        val stack = focussedSlot.stack ?: return
        val internalName = stack.getInternalNameOrNull() ?: return
        val rawInternalName = internalName.asString()
        OSUtils.copyToClipboard(rawInternalName)
        ChatUtils.chat("§eCopied internal name §7$rawInternalName §eto the clipboard!")
    }

    @SubscribeEvent
    fun onShowInternalName(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!debugConfig.showInternalName) return
        val itemStack = event.itemStack
        val internalName = itemStack.getInternalName()
        if ((internalName == NEUInternalName.NONE) && !debugConfig.showEmptyNames) return
        event.toolTip.add("Internal Name: '${internalName.asString()}'")
    }

    @SubscribeEvent
    fun showItemRarity(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!debugConfig.showItemRarity) return
        val itemStack = event.itemStack

        val rarity = itemStack.getItemRarityOrNull()
        event.toolTip.add("Item rarity: $rarity")
    }

    @SubscribeEvent
    fun showItemCategory(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!debugConfig.showItemCategory) return
        val itemStack = event.itemStack

        val category = itemStack.getItemCategoryOrNull()?.name ?: "UNCLASSIFIED"
        event.toolTip.add("Item category: $category")
    }

    @SubscribeEvent
    fun onShowNpcPrice(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!debugConfig.showNpcPrice) return
        val internalName = event.itemStack.getInternalNameOrNull() ?: return

        val npcPrice = internalName.getNpcPriceOrNull() ?: return
        event.toolTip.add("§7NPC price: ${npcPrice.addSeparators()}")
    }

    @SubscribeEvent
    fun onShowCraftPrice(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!debugConfig.showCraftPrice) return
        val price = event.itemStack.getInternalNameOrNull()?.getRawCraftCostOrNull() ?: return

        event.toolTip.add("§7Craft price: ${price.addSeparators()}")
    }

    @SubscribeEvent
    fun onShowBzPrice(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!debugConfig.showBZPrice) return
        val internalName = event.itemStack.getInternalNameOrNull() ?: return

        val data = internalName.getBazaarData() ?: return
        val instantBuyPrice = data.instantBuyPrice
        val sellOfferPrice = data.sellOfferPrice

        event.toolTip.add("§7BZ instantBuyPrice: ${instantBuyPrice.addSeparators()}")
        event.toolTip.add("§7BZ sellOfferPrice: ${sellOfferPrice.addSeparators()}")
    }

    @SubscribeEvent
    fun onShowItemName(event: LorenzToolTipEvent) {
        if (!LorenzUtils.inSkyBlock) return
        if (!debugConfig.showItemName) return
        val itemStack = event.itemStack
        val internalName = itemStack.getInternalName()
        if (internalName == NEUInternalName.NONE) {
            event.toolTip.add("Item name: no item.")
            return
        }
        val name = itemStack.itemName
        event.toolTip.add("Item name: '$name§7'")
    }

    @SubscribeEvent
    @Suppress("EmptyFunctionBlock")
    fun onChat(event: LorenzChatEvent) {
    }

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent.GuiOverlayRenderEvent) {
        if (!LorenzUtils.inSkyBlock) return

        @Suppress("ConstantConditionIf")
        if (false) {
            itemRenderDebug()
        }

        if (Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            if (debugConfig.currentAreaDebug) {
                config.debugLocationPos.renderString(
                    "Current Area: ${HypixelData.skyBlockArea}",
                    posLabel = "SkyBlock Area (Debug)",
                )
            }

            if (debugConfig.raytracedOreblock) {
                BlockUtils.getBlockLookingAt(50.0)?.let { pos ->
                    OreBlock.getByStateOrNull(pos.getBlockStateAt())?.let { ore ->
                        config.debugOrePos.renderString(
                            "Looking at: ${ore.name} (${pos.toCleanString()})",
                            posLabel = "OreBlock",
                        )
                    }
                }
            }
        }


        if (!debugConfig.enabled) return

        if (displayLine.isNotEmpty()) {
            config.debugPos.renderString("test: $displayLine", posLabel = "Test")
        }
        config.debugPos.renderStringsAndItems(displayList, posLabel = "Test Display")
    }

    @SubscribeEvent
    fun onGuiRenderChestGuiOverlayRender(event: GuiRenderEvent.ChestGuiOverlayRenderEvent) {
        @Suppress("ConstantConditionIf")
        if (false) {
            dragAbleTest()
        }
    }

    private fun dragAbleTest() {
        val bone = ItemStack(Items.bone, 1).toDragItem()
        val leaf = ItemStack(Blocks.leaves, 1).toDragItem()

        config.debugItemPos.renderRenderables(
            listOf(
                DragNDrop.draggable(Renderable.string("A Bone"), { bone }),
                Renderable.placeholder(0, 30),
                DragNDrop.draggable(Renderable.string("A Leaf"), { leaf }),
                Renderable.placeholder(0, 30),
                DragNDrop.droppable(
                    Renderable.string("Feed Dog"),
                    object : Droppable {
                        override fun handle(drop: Any?) {
                            val unit = drop as ItemStack
                            if (unit.item == Items.bone) {
                                LorenzDebug.chatAndLog("Oh, a bone!")
                            } else {
                                LorenzDebug.chatAndLog("Disgusting that is not a bone!")
                            }
                        }

                        override fun validTarget(item: Any?) = item is ItemStack

                    },
                ),
            ),
            posLabel = "Item Debug",
        )
    }

    private fun itemRenderDebug() {
        val scale = 0.1
        val renderables = listOf(
            ItemStack(Blocks.glass_pane), ItemStack(Items.diamond_sword), ItemStack(Items.skull),
            ItemStack(Blocks.melon_block),
        ).map { item ->
            generateSequence(scale) { it + 0.1 }.take(25).map {
                Renderable.itemStack(item, it, xSpacing = 0).renderBounds()
            }.toList()
        }.editCopy {
            this.add(
                0,
                generateSequence(scale) { it + 0.1 }.take(25).map { Renderable.string(it.roundTo(1).toString()) }.toList(),
            )
        }
        config.debugItemPos.renderRenderables(
            listOf(
                Renderable.table(renderables),
                Renderable.horizontalContainer(
                    listOf(
                        Renderable.string("Test:").renderBounds(),
                        Renderable.itemStack(ItemStack(Items.diamond_sword)).renderBounds(),
                    ),
                    spacing = 1,
                ),
            ),
            posLabel = "Item Debug",
        )
    }

    @HandleEvent(onlyOnSkyblock = true)
    fun onOreMined(event: OreMinedEvent) {
        if (!debugConfig.oreEventMessages) return
        val originalOre = event.originalOre?.let { "$it " } ?: ""
        val extraBlocks = event.extraBlocks.map { "${it.key.name}: ${it.value}" }
        ChatUtils.debug("Mined: $originalOre(${extraBlocks.joinToString()})")
    }

    @SubscribeEvent
    fun onReceiveParticle(event: ReceiveParticleEvent) {
//        val particleType = event.type
//        val distance = LocationUtils.playerLocation().distance(event.location).roundTo(2)
//
//        println("")
//        println("particleType: $particleType")
//
//        val particleCount = event.count
//
//        println("distance: $distance")
//
//        val particleArgs = event.particleArgs
//        println("args: " + particleArgs.size)
//        for ((i, particleArg) in particleArgs.withIndex()) {
//            println("$i $particleArg")
//        }
//
//        val particleSpeed = event.speed
//        val offset = event.offset
//        println("particleCount: $particleCount")
//        println("particleSpeed: $particleSpeed")
//        println("offset: $offset")
    }

    @SubscribeEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "dev.debugEnabled", "dev.debug.enabled")
        event.move(3, "dev.showInternalName", "dev.debug.showInternalName")
        event.move(3, "dev.showEmptyNames", "dev.debug.showEmptyNames")
        event.move(3, "dev.showItemRarity", "dev.debug.showItemRarity")
        event.move(3, "dev.copyInternalName", "dev.debug.copyInternalName")
        event.move(3, "dev.showNpcPrice", "dev.debug.showNpcPrice")
    }
}
