package at.hannibal2.skyhanni.features.mining.eventtracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.overrideId
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.StringUtils.allLettersFirstUppercase
import at.hannibal2.skyhanni.utils.StringUtils.removeColor
import at.hannibal2.skyhanni.utils.renderables.Renderable
import at.hannibal2.skyhanni.utils.renderables.Renderable.Companion.darken
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private fun Item.toItemStack(meta: Int = 0): ItemStack = ItemStack(this, 1, meta)

enum class MiningEventType(
    val eventName: String,
    private val shortName: String,
    val defaultLength: Duration,
    // TODO change to LorenzColor
    private val colorCode: Char,
    val dwarvenSpecific: Boolean,
    iconInput: Renderable,
    val itemStack: ItemStack? = null,
) {
    GONE_WITH_THE_WIND(
        "GONE WITH THE WIND", "Wind", 18.minutes, '9', false,
        object : Renderable {
            override val width = 10
            override val height = 10
            override val horizontalAlign = RenderUtils.HorizontalAlignment.LEFT
            override val verticalAlign = RenderUtils.VerticalAlignment.CENTER

            val compass = Renderable.itemStack(Items.compass.toItemStack(), 0.45)
            val wind = Renderable.string("§9≈", scale = 0.75)

            override fun render(posX: Int, posY: Int) {
                GlStateManager.translate(1f, 1f, -2f)
                compass.render(posX, posY)
                GlStateManager.translate(-1f, -2f, 2f)
                wind.render(posX, posY)
                GlStateManager.translate(0f, 1f, 0f)
            }
        },
    ),
    DOUBLE_POWDER(
        "2X POWDER", "2x", 15.minutes, 'b', false,
        object : Renderable {
            override val width = 10
            override val height = 10
            override val horizontalAlign = RenderUtils.HorizontalAlignment.LEFT
            override val verticalAlign = RenderUtils.VerticalAlignment.CENTER

            val dyeGreen = Renderable.itemStack(Items.dye.toItemStack(10), 0.45)
            val dyePink = Renderable.itemStack(Items.dye.toItemStack(9), 0.45)

            override fun render(posX: Int, posY: Int) {
                GlStateManager.translate(1f, 0f, 0f)
                dyePink.render(posX + 1, posY - 1)
                GlStateManager.translate(-2f, 1.5f, 0f)
                dyeGreen.render(posX, posY)
                GlStateManager.translate(1f, -1.5f, 0f)
            }

        },
    ),

    @Suppress("MaxLineLength")
    GOBLIN_RAID(
        "GOBLIN RAID", "Raid", 5.minutes, 'c', true,
        ItemUtils.createSkull( // TODO: Move skull texture to repo
            "Goblin",
            "32518c29-6127-3c71-b2a7-be4c3251e76f",
            "ewogICJ0aW1lc3RhbXAiIDogMTYwNzQ2NDg4MTMwOCwKICAicHJvZmlsZUlkIiA6ICJhMmY4MzQ1OTVjODk0YTI3YWRkMzA0OTcxNmNhOTEwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJiUHVuY2giLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTcyODUwOTA2YjdmMGQ5NTJjMGU1MDgwNzNjYzQzOWZkMzM3NGNjZjViODg5YzA2ZjdlOGQ5MGNjMGNjMjU1YyIKICAgIH0KICB9Cn0=",
        ),
    ),

    @Suppress("MaxLineLength")
    BETTER_TOGETHER(
        "BETTER TOGETHER", "Better", 18.minutes, 'd', false,
        object : Renderable {
            override val width = 10
            override val height = 10
            override val horizontalAlign = RenderUtils.HorizontalAlignment.LEFT
            override val verticalAlign = RenderUtils.VerticalAlignment.CENTER

            val steveHead = Renderable.itemStack(Items.skull.toItemStack(3), 0.36)
            val alexHead = Renderable.itemStack(
                ItemUtils.createSkull( // TODO: Move skull texture to repo
                    "Alex",
                    "6ab43178-89fd-4905-97f6-0f67d9d76fd9",
                    "fRBfVNlIWW6cL478st/8NsNEHVxjvwQDp4+MbKbFj1tPZvxXgpIXRaQsLeDl/0+E4tipPKNANAbmqj9EKAVx3b3gDqLLrTTk/NfuH2RD3I5ppzio8w5oYk1022SopaayGBP4+kuwktDHzlR8IgAUb1RiavldKp+TGRdCbqw8vHHBm9pnuOePzTOOADQgdanRj98bOcfIXe69tSS/VHxDe9tkpYFPkQR8zsJcjUxf+nS83iFU9CW9lKtQlyoU6/BPbHFILvcR1KDR5Imj7GJe2OJefghI6OqtHNZP2tzkia2IDU0Yc4ikwC+7yN3i6I3Do4G3gTtCZVfNXiSdFyU9nCMyBxggTaG9zaljZpN0BynG4FzYMujIVgeNa6FLqwoaFT0iELW2w9JgJFgyVlaDKEqMSGyxgqtcQMPBuvCwMFFjeFd2EhtfTjQ4hcpva+NXXoYPP7yfTk/0DErNZV2dUTasekar8lH6U58B7ECNxDUwcon4z7sSO5mdlPJoiT7zllgpwQn5NUPaxZxaKkGdUIFEGzjmBfnCmk6MOqzi05Rr18wnkdic9hz/fIzzTMhn9mbMG6VF9eBkE4mNu1K5jai6II5Mz9BV49U0ZcA874N1VHpJpQE6762TYv+u7ICTRIOf2LD9wEgu3py/nX+IHma5j22ClUtXH3hYdZmHg+s=\",Value:\"ewogICJ0aW1lc3RhbXAiIDogMTcxMTY1OTI2NDg1NSwKICAicHJvZmlsZUlkIiA6ICI2YWI0MzE3ODg5ZmQ0OTA1OTdmNjBmNjdkOWQ3NmZkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQWxleCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84M2NlZTVjYTZhZmNkYjE3MTI4NWFhMDBlODA0OWMyOTdiMmRiZWJhMGVmYjhmZjk3MGE1Njc3YTFiNjQ0MDMyIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                ),
                0.36,
            )

            override fun render(posX: Int, posY: Int) {
                GlStateManager.translate(-1f, 0f, 0f)
                alexHead.render(posX, posY)
                GlStateManager.translate(+4f, +3f, 0f)
                steveHead.render(posX, posY)
                GlStateManager.translate(-3f, -3f, 0f)
            }

        },
    ),
    RAFFLE(
        "RAFFLE",
        "Raffle",
        160.seconds,
        colorCode = '6',
        dwarvenSpecific = true,
        iconInput = Items.name_tag.toItemStack().overrideId("MINING_RAFFLE_TICKET"),
    ),
    MITHRIL_GOURMAND(
        "MITHRIL GOURMAND",
        "Gourmand", 10.minutes,
        colorCode = 'b',
        dwarvenSpecific = true,
        iconInput = Items.dye.toItemStack(6).overrideId("MITHRIL_GOURMAND")
    ),
    ;

    constructor(
        eventName: String,
        shortName: String,
        defaultLength: Duration,
        // TODO change to LorenzColor
        colorCode: Char,
        dwarvenSpecific: Boolean,
        iconInput: ItemStack,
    ) : this(
        eventName, shortName, defaultLength, colorCode, dwarvenSpecific,
        Renderable.itemStack(
            iconInput, xSpacing = 0,
        ),
        iconInput,
    )

    val icon = Renderable.hoverTips(iconInput, listOf(eventName))
    val compactText = Renderable.string("§$colorCode$shortName")
    val normalText = Renderable.string("§$colorCode$eventName")

    val compactTextWithIcon = Renderable.horizontalContainer(listOf(icon, compactText), 0)
    val normalTextWithIcon = Renderable.horizontalContainer(listOf(icon, normalText), 0)

    fun getRenderable(): Renderable = when (config.compressedFormat) {
        CompressFormat.COMPACT_TEXT -> compactTextWithIcon
        CompressFormat.ICON_ONLY -> icon
        CompressFormat.TEXT_WITHOUT_ICON -> normalText
        CompressFormat.COMPACT_TEXT_WITHOUT_ICON -> compactText
        CompressFormat.DEFAULT, null -> normalTextWithIcon
    }

    fun getRenderableAsPast(): Renderable = getRenderable().darken(0.4f)

    companion object {
        private val config get() = SkyHanniMod.feature.mining.miningEvent

        enum class CompressFormat {
            DEFAULT,
            COMPACT_TEXT,
            ICON_ONLY,
            TEXT_WITHOUT_ICON,
            COMPACT_TEXT_WITHOUT_ICON;

            override fun toString(): String {
                return name.lowercase().allLettersFirstUppercase()
            }
        }

        fun fromEventName(bossbarName: String): MiningEventType? {
            return MiningEventType.entries.find { it.eventName == bossbarName.removeColor() }
        }
    }
}
