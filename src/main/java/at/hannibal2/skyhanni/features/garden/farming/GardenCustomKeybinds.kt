package at.hannibal2.skyhanni.features.garden.farming

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.config.ConfigUpdaterMigrator
import at.hannibal2.skyhanni.config.features.garden.keybinds.KeyBindLayout
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import at.hannibal2.skyhanni.events.GardenToolChangeEvent
import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.SecondPassedEvent
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.garden.GardenAPI
import at.hannibal2.skyhanni.features.garden.farming.keybinds.KeyBindLayouts
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
import at.hannibal2.skyhanni.utils.KeyboardManager
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyClicked
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.SimpleTimeMark
import io.github.notenoughupdates.moulconfig.observer.Property
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

    private var cropInHand: CropType? = null
    private var currentLayout: Map<KeyBinding, Int>? = null
    private var lastWindowOpenTime = SimpleTimeMark.farPast()
    private var lastDuplicateKeybindsWarnTime = SimpleTimeMark.farPast()
    private var isDuplicate = false

    @JvmStatic
    fun isKeyDown(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = currentLayout?.get(keyBinding) ?: run {
            val layout = currentLayout ?: return
            if (layout.containsValue(keyBinding.keyCode)) {
                cir.returnValue = false
            }
            return
        }
        cir.returnValue = override.isKeyHeld()
    }

    @JvmStatic
    fun isKeyPressed(keyBinding: KeyBinding, cir: CallbackInfoReturnable<Boolean>) {
        if (!isActive()) return
        val override = currentLayout?.get(keyBinding) ?: run {
            val layout = currentLayout ?: return
            if (layout.containsValue(keyBinding.keyCode)) {
                cir.returnValue = false
            }
            return
        }
        cir.returnValue = override.isKeyClicked()
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        val screen = Minecraft.getMinecraft().currentScreen ?: return
        if (screen !is GuiEditSign) return
        lastWindowOpenTime = SimpleTimeMark.now()
    }

    @HandleEvent
    fun onSecondPassed(event: SecondPassedEvent) {
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
        cropInHand = event.crop
        currentLayout = cropInHand?.getKebindLayoutMap()
    }

    private fun CropType.getKebindLayoutMap() = getKebindLayout().get().map

    private fun CropType.getKebindLayout(): Property<KeyBindLayouts> = with(config.cropLayoutSelection) {
        when (this@getKebindLayout) {
            CropType.WHEAT -> wheat
            CropType.CARROT -> carrot
            CropType.POTATO -> potato
            CropType.NETHER_WART -> netherWart
            CropType.PUMPKIN -> pumpkin
            CropType.MELON -> melon
            CropType.COCOA_BEANS -> cocoaBeans
            CropType.SUGAR_CANE -> sugarCane
            CropType.CACTUS -> cactus
            CropType.MUSHROOM -> mushroom
        }
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        val allKeybindings = KeyBindLayouts.entries.flatMap { it.layout.allKeybindingFields }

        ConditionalUtils.onToggle(
            allKeybindings,
        ) {
            update()
        }

        with(config.cropLayoutSelection) {
            ConditionalUtils.onToggle(
                listOf(wheat, carrot, potato, netherWart, pumpkin, melon, cocoaBeans, sugarCane, cactus, mushroom)
            ) {
                update()
            }
        }

        update()
    }

    private fun update() {
        KeyBindLayouts.update()

        calculateDuplicates()
        lastDuplicateKeybindsWarnTime = SimpleTimeMark.farPast()
        KeyBinding.unPressAllKeys()

        currentLayout = cropInHand?.getKebindLayoutMap()
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
        event.move(3, "garden.keyBindAttack", "garden.keyBind.layout1.attack")
        event.move(3, "garden.keyBindUseItem", "garden.keyBind.layout1.useItem")
        event.move(3, "garden.keyBindLeft", "garden.keyBind.layout1.left")
        event.move(3, "garden.keyBindRight", "garden.keyBind.layout1.right")
        event.move(3, "garden.keyBindForward", "garden.keyBind.layout1.forward")
        event.move(3, "garden.keyBindBack", "garden.keyBind.layout1.back")
        event.move(3, "garden.keyBindJump", "garden.keyBind.layout1.jump")
        event.move(3, "garden.keyBindSneak", "garden.keyBind.layout1.sneak")

        event.move(71, "garden.keyBind.attack", "garden.keyBind.layout1.attack")
        event.move(71, "garden.keyBind.useItem", "garden.keyBind.layout1.useItem")
        event.move(71, "garden.keyBind.left", "garden.keyBind.layout1.left")
        event.move(71, "garden.keyBind.right", "garden.keyBind.layout1.right")
        event.move(71, "garden.keyBind.forward", "garden.keyBind.layout1.forward")
        event.move(71, "garden.keyBind.back", "garden.keyBind.layout1.back")
        event.move(71, "garden.keyBind.jump", "garden.keyBind.layout1.jump")
        event.move(71, "garden.keyBind.sneak", "garden.keyBind.layout1.sneak")
    }
}
