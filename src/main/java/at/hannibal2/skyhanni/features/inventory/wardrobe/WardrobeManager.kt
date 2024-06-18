package at.hannibal2.skyhanni.features.inventory.wardrobe

import at.hannibal2.skyhanni.SkyHanniMod

object WardrobeManager {

    val configSpacing get() = SkyHanniMod.feature.inventory.customWardrobe.spacing
    private val configColor get() = SkyHanniMod.feature.inventory.customWardrobe.color

    @JvmStatic
    fun resetWardrobeSpacing() {
        with(configSpacing) {
            globalScale.set(100)
            outlineThickness.set(5)
            outlineBlur.set(0.5f)
            slotWidth.set(75)
            slotHeight.set(140)
            playerScale.set(75)
            maxPlayersPerRow.set(9)
            horizontalSpacing.set(3)
            verticalSpacing.set(3)
            buttonSlotsVerticalSpacing.set(10)
            buttonHorizontalSpacing.set(10)
            buttonVerticalSpacing.set(10)
            buttonWidth.set(50)
            buttonHeight.set(20)
            backgroundPadding.set(10)
        }
    }

    @JvmStatic
    fun resetWardrobeColor() {
        with(configColor) {
            backgroundColor = "0:127:0:0:0"
            equippedColor = "0:127:85:255:85"
            favoriteColor = "0:127:255:85:85"
            samePageColor = "0:127:94:108:255"
            otherPageColor = "0:127:0:0:0"
            topBorderColor = "0:255:255:200:0"
            bottomBorderColor = "0:255:255:0:0"
        }
    }
}
