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
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils
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
    private val mcSettings get() = Minecraft.getMinecraft().gameSettings

    private val layouts: MutableMap<String, Map<KeyBinding, Int>> = mutableMapOf()

    private var cropLayoutSelection: Map<CropType?, String> = emptyMap()
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
    fun onTick(event: LorenzTickEvent) {
        if (!isEnabled()) return
        val screen = Minecraft.getMinecraft().currentScreen ?: return
        if (screen !is GuiEditSign) return
        lastWindowOpenTime = SimpleTimeMark.now()
    }

    @SubscribeEvent
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
        lastToolSwitch = SimpleTimeMark.now()
        cropInHand = event.crop
        event.crop?.let { lastCrop = it }
        currentLayout = layouts[cropLayoutSelection[cropInHand]]
    }

    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        fun getAllKeybindingsFromLayout(layout: KeyBindLayout) = listOf(
            layout.attack, layout.useItem, layout.left, layout.right,
            layout.forward, layout.back, layout.jump, layout.sneak
        )

        ConditionalUtils.onToggle(
            *listOf(
                config.layout1, config.layout2, config.layout3, config.layout4, config.layout5
            ).flatMap(::getAllKeybindingsFromLayout).toTypedArray()) {
            update()
        }
        update()
    }

    private fun update() {
        fun buildKeybindLayoutMap(
            layout: KeyBindLayout
        ): Map<KeyBinding, Int> {
            with(mcSettings) {
                val keyBindings = listOf(
                    keyBindAttack, keyBindUseItem, keyBindLeft, keyBindRight,
                    keyBindForward, keyBindBack, keyBindJump, keyBindSneak
                )

                return buildMap {
                    keyBindings.zip(
                        listOf(
                            layout.attack, layout.useItem, layout.left, layout.right,
                            layout.forward, layout.back, layout.jump, layout.sneak
                        )
                    ) { keyBinding, setKeyProperty ->
                        put(keyBinding, setKeyProperty.get()) // Add key-value pair
                    }
                }
            }
        }

        layouts["Layout 1"] = buildKeybindLayoutMap(config.layout1)
        layouts["Layout 2"] = buildKeybindLayoutMap(config.layout2)
        layouts["Layout 3"] = buildKeybindLayoutMap(config.layout3)
        layouts["Layout 4"] = buildKeybindLayoutMap(config.layout4)
        layouts["Layout 5"] = buildKeybindLayoutMap(config.layout5)

        cropLayoutSelection = mapOf(
            CropType.WHEAT to config.cropLayoutSelection.wheat.toString(),
            CropType.CARROT to config.cropLayoutSelection.carrot.toString(),
            CropType.POTATO to config.cropLayoutSelection.potato.toString(),
            CropType.NETHER_WART to config.cropLayoutSelection.netherWart.toString(),
            CropType.PUMPKIN to config.cropLayoutSelection.pumpkin.toString(),
            CropType.MELON to config.cropLayoutSelection.melon.toString(),
            CropType.COCOA_BEANS to config.cropLayoutSelection.cocoaBeans.toString(),
            CropType.SUGAR_CANE to config.cropLayoutSelection.sugarCane.toString(),
            CropType.CACTUS to config.cropLayoutSelection.cactus.toString(),
            CropType.MUSHROOM to config.cropLayoutSelection.mushroom.toString(),
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
        for (layout in layouts.values) {
            if (isDuplicateInLayout(layout)) {
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
