package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.keybinds.KeyBindLayout
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.IslandChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.keybinds.KeyBindLayouts
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.DelayedRun
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@SkyHanniModule
object GardenCustomKeybinds {

    private val config get() = GardenAPI.config.keyBind
    val mcSettings get() = Minecraft.getMinecraft().gameSettings

    private var cropLayoutSelection: Map<CropType?, Map<KeyBinding, Int>> = emptyMap()
    private var cropInHand: CropType? = null
    private var lastCrop: CropType? = null
    private var lastToolSwitch = SimpleTimeMark.farPast()
    private var currentLayout: Map<KeyBinding, Int>? = null
    private var lastWindowOpenTime = SimpleTimeMark.farPast()
    private var lastDuplicateKeybindsWarnTime = SimpleTimeMark.farPast()
    private var isDuplicate = false

    @JvmStatic
    fun isKeyDown(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = currentLayout?.get(keyBinding) ?: return
        cir.returnValue = override.isKeyHeld()
    }

    @JvmStatic
    fun isKeyPressed(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = currentLayout?.get(keyBinding) ?: return
        cir.returnValue = override.isKeyClicked()
    }

    @SubscribeEvent
    fun onTick(@Suppress("unused") event: LorenzTickEvent) {
        if (!isEnabled()) return
        val screen = Minecraft.getMinecraft().currentScreen ?: return
        if (screen !is GuiEditSign) return
        lastWindowOpenTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
    fun onSecondPassed(@Suppress("unused") event: SecondPassedEvent) {
        if (!isEnabled()) return
        if (!isDuplicate || lastDuplicateKeybindsWarnTime.passedSince() < 30.seconds) return
        ChatUtils.chatAndOpenConfig(
            "Duplicate Custom Keybinds aren't allowed!",
            GardenAPI.config::keyBind,
        )
        lastDuplicateKeybindsWarnTime = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onGardenToolChange(event: GardenToolChangeEvent) {
        lastToolSwitch = SimpleTimeMark.now()
        cropInHand = event.crop
        event.crop?.let { lastCrop = it }
        currentLayout = cropLayoutSelection[cropInHand]
    }

//     TODO: remove the need for this workaround, as GardenAPI should call GardenToolChangeEvent on island change
    @HandleEvent
    fun onIslandChange(event: IslandChangeEvent) {
        if (event.newIsland == IslandType.GARDEN) {
            DelayedRun.runDelayed(2.seconds) {
                cropInHand = GardenAPI.cropInHand
                currentLayout = cropLayoutSelection[cropInHand]
            }
        }
    }

    @HandleEvent
    fun onConfigLoad(@Suppress("unused") event: ConfigLoadEvent) {
        val allKeybindings = KeyBindLayouts.entries.flatMap { it.layout.allKeybindingFields }

        ConditionalUtils.onToggle(
            allKeybindings,
        ) {
            update()
        }
        update()
    }

    private fun update() {
        KeyBindLayouts.update()

        cropLayoutSelection = mapOf(
            CropType.WHEAT to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.wheat.toString()).map,
            CropType.CARROT to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.carrot.toString()).map,
            CropType.POTATO to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.potato.toString()).map,
            CropType.NETHER_WART to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.netherWart.toString()).map,
            CropType.PUMPKIN to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.pumpkin.toString()).map,
            CropType.MELON to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.melon.toString()).map,
            CropType.COCOA_BEANS to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.cocoaBeans.toString()).map,
            CropType.SUGAR_CANE to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.sugarCane.toString()).map,
            CropType.CACTUS to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.cactus.toString()).map,
            CropType.MUSHROOM to KeyBindLayouts.getLayoutByDisplayName(config.cropLayoutSelection.mushroom.toString()).map,
        )

        calculateDuplicates()
        lastDuplicateKeybindsWarnTime = SimpleTimeMark.farPast()
        KeyBinding.unPressAllKeys()
    }

    private fun isDuplicateInLayout(layout: Map<KeyBinding, Int>) =
        layout.values
            .filter { it != Keyboard.KEY_NONE }
            .let { values -> values.size != values.toSet().size }

    private fun calculateDuplicates() {
        for (layout in KeyBindLayouts.entries) {
            if (isDuplicateInLayout(layout.map)) {
                isDuplicate = true
                return
            }
        }
        isDuplicate = false
    }

    private fun isEnabled() = GardenAPI.inGarden() && config.enabled && !(GardenAPI.onBarnPlot && config.excludeBarn)

    private fun isActive(): Boolean =
        isEnabled() && GardenAPI.toolInHand != null && !isDuplicate && !hasGuiOpen() && lastWindowOpenTime.passedSince() > 300.milliseconds

    private fun hasGuiOpen() = Minecraft.getMinecraft().currentScreen != null

    @JvmStatic
    fun disableAll(layout: KeyBindLayout) {
        with(layout) {
            attack.set(Keyboard.KEY_NONE)
            useItem.set(Keyboard.KEY_NONE)
            left.set(Keyboard.KEY_NONE)
            right.set(Keyboard.KEY_NONE)
            forward.set(Keyboard.KEY_NONE)
            back.set(Keyboard.KEY_NONE)
            jump.set(Keyboard.KEY_NONE)
            sneak.set(Keyboard.KEY_NONE)
        }
    }

    @JvmStatic
    fun defaultAll(layout: KeyBindLayout) {
        with(layout) {
            attack.set(KeyboardManager.LEFT_MOUSE)
            useItem.set(KeyboardManager.RIGHT_MOUSE)
            left.set(Keyboard.KEY_A)
            right.set(Keyboard.KEY_D)
            forward.set(Keyboard.KEY_W)
            back.set(Keyboard.KEY_S)
            jump.set(Keyboard.KEY_SPACE)
            sneak.set(Keyboard.KEY_LSHIFT)
        }
    }

    @HandleEvent
    fun onConfigFix(event: ConfigUpdaterMigrator.ConfigFixEvent) {
        event.move(3, "garden.keyBindEnabled", "garden.keyBind.enabled")
        event.move(3, "garden.keyBindAttack", "garden.keyBind.attack")
        event.move(3, "garden.keyBindUseItem", "garden.keyBind.useItem")
        event.move(3, "garden.keyBindLeft", "garden.keyBind.left")
        event.move(3, "garden.keyBindRight", "garden.keyBind.right")
        event.move(3, "garden.keyBindForward", "garden.keyBind.forward")
        event.move(3, "garden.keyBindBack", "garden.keyBind.back")
        event.move(3, "garden.keyBindJump", "garden.keyBind.jump")
        event.move(3, "garden.keyBindSneak", "garden.keyBind.sneak")
    }
}
