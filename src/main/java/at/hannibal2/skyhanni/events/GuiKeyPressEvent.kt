package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.api.event.CancellableSkyHanniEvent
import net.minecraft.client.gui.inventory.GuiContainer

class GuiKeyPressEvent(val guiContainer: GuiContainer) : CancellableSkyHanniEvent()
