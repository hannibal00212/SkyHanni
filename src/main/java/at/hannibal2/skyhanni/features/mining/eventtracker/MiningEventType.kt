package at.hannibal2.skyhanni.features.mining.eventtracker

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.utils.ItemUtils
import at.hannibal2.skyhanni.utils.ItemUtils.overrideId
import at.hannibal2.skyhanni.utils.RenderUtils
import at.hannibal2.skyhanni.utils.SkullTextureHolder
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
    var itemStack: ItemStack? = null,
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

    GOBLIN_RAID(
        "GOBLIN RAID", "Raid", 5.minutes, 'c', true,
        ItemUtils.createSkull(
            "Goblin",
            "32518c29-6127-3c71-b2a7-be4c3251e76f",
            "" // Late init when SkullTextureHolder is loaded
        ),
    ),

    BETTER_TOGETHER(
        "BETTER TOGETHER", "Better", 18.minutes, 'd', false,
        object : Renderable {
            override val width = 10
            override val height = 10
            override val horizontalAlign = RenderUtils.HorizontalAlignment.LEFT
            override val verticalAlign = RenderUtils.VerticalAlignment.CENTER

            val steveHead = Renderable.itemStack(Items.skull.toItemStack(3), 0.36)
            val alexHead by lazy {
                Renderable.itemStack(
                    ItemUtils.createSkull(
                        "Alex",
                        "6ab43178-89fd-4905-97f6-0f67d9d76fd9",
                        SkullTextureHolder.getTexture("ALEX_SKIN_TEXTURE"),
                    ),
                    0.36,
                )
            }

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

        // Because we don't want to hard-code the goblin texture, this gets called by SkullTextureHolder when the repository is loaded
        fun fixGoblinItemStack() {
            GOBLIN_RAID.itemStack = ItemUtils.createSkull(
                "Goblin",
                "32518c29-6127-3c71-b2a7-be4c3251e76f",
                SkullTextureHolder.getTexture("GOBLIN_RAID"),
            )
        }

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
