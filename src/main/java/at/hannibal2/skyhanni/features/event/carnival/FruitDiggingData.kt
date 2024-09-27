package at.hannibal2.skyhanni.features.event.carnival

import at.hannibal2.skyhanni.features.event.carnival.FruitDigging.config
import net.minecraft.item.ItemStack

data class PosInfo(
    var uncovered: Boolean,
    var dropTypes: MutableSet<DropType>?,
    var lowestFruit: DropType?,
    var highestFruit: DropType?,
    var minesNear: Int?,
)

data class EntityInfo(
    val stack: ItemStack,
    val type: DropType,
)

data class Skin(val url: String)
data class Textures(val SKIN: Skin)
data class MinecraftTextures(val textures: Textures)

val amountOnTheBoard = mapOf(
    DropType.MANGO to 10,
    DropType.APPLE to 8,
    DropType.WATERMELON to 4,
    DropType.POMEGRANATE to 4,
    DropType.COCONUT to 3,
    DropType.CHERRY to 2,
    DropType.DURIAN to 2,
    DropType.DRAGONFRUIT to 1,
    DropType.RUM to 5,
    DropType.BOMB to 15,
    DropType.NOT_BOMB to 34,
    DropType.NONE to 0,
)

class DropTypeManager {
    private val amountsLeft = amountOnTheBoard.toMutableMap()

    fun getAmountLeft(dropType: DropType): Int {
        return amountsLeft[dropType] ?: 0
    }

    fun setAmountLeft(dropType: DropType, amount: Int) {
        amountsLeft[dropType] = amount
    }

    fun getAllAmountsLeft(): IntArray {
        return amountsLeft.values.toIntArray()
    }
}

enum class DropType(
    val basePoints: Int,
    val skullTexture: String,
    val display: String,
) {
    MANGO(
        300,
        "http://textures.minecraft.net/texture/f363a62126a35537f8189343a22660de75e810c6ac004a7d3da65f1c040a839", "§aMango",
    ),
    APPLE(
        100,
        "http://textures.minecraft.net/texture/17ea278d6225c447c5943d652798d0bbbd1418434ce8c54c54fdac79994ddd6c", "§aApple",
    ),
    WATERMELON(
        100,
        "http://textures.minecraft.net/texture/efe4ef83baf105e8dee6cf03dfe7407f1911b3b9952c891ae34139560f2931d6", "§9Watermelon",
    ),
    POMEGRANATE(
        200,
        "" +
            "http://textures.minecraft.net/texture/40824d18079042d5769f264f44394b95b9b99ce689688cc10c9eec3f882ccc08",
        "§9Pomegranate",
    ),
    COCONUT(
        200,
        "http://textures.minecraft.net/texture/10ceb1455b471d016a9f06d25f6e468df9fcf223e2c1e4795b16e84fcca264ee", "§5Coconut",
    ),
    CHERRY(
        200,
        "http://textures.minecraft.net/texture/c92b099a62cd2fbf8ada09dec145c75d7fda4dc57b968bea3a8fa11e37aa48b2", "§5Cherry",
    ),
    DURIAN(
        800,
        "http://textures.minecraft.net/texture/ac268d36c2c6047ffeec00124096376b56dbb4d756a55329363a1b27fcd659cd", "§5Durian",
    ),
    DRAGONFRUIT(
        1200,
        "http://textures.minecraft.net/texture/3cc761bcb0579763d9b8ab6b7b96fa77eb6d9605a804d838fec39e7b25f95591", "§dDragonfruit",
    ),
    RUM(
        0,
        "http://textures.minecraft.net/texture/407b275d28b927b1bf7f6dd9f45fbdad2af8571c54c8f027d1bff6956fbf3c16", "§eRum",
    ),
    BOMB(
        0,
        "http://textures.minecraft.net/texture/a76a2811d1e176a07b6d0a657b910f134896ce30850f6e80c7c83732d85381ea",
        "§${config.mineText.chatColorCode}Bomb",
    ),
    NOT_BOMB(0, "", "§${config.safeText.chatColorCode}Safe"),
    NONE(0, "", "")
}
