package at.hannibal2.skyhanni.features.inventory.tiarelay

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.RegexUtils.matches
import at.hannibal2.skyhanni.utils.repopatterns.RepoPattern

enum class Relay(
    val relayName: String,
    val waypoint: LorenzVec,
    val island: IslandType,
    chatMessage: String,
) {

    RELAY_1(
        "1st Relay", LorenzVec(143.5, 108.0, 93.0), IslandType.HUB,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fThe first relay is on a branch of the large tree on the north-east of the fairy pond."
    ),
    RELAY_2(
        "2nd Relay", LorenzVec(-246.5, 123.0, 55.5), IslandType.HUB,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fThe next relay is in the castle ruins!"
    ),
    RELAY_3(
        "3rd Relay", LorenzVec(128.5, 232.0, 200.5), IslandType.DWARVEN_MINES,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fThe next relay is in the §bRoyal Palace §rwithin the Dwarven Mines."
    ),
    RELAY_4(
        "4th Relay", LorenzVec(-560, 164, -287), IslandType.THE_END,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fThe next relay is on the highest spike of §dThe End§r."
    ),
    RELAY_5(
        "5th Relay", LorenzVec(-375, 207, -799), IslandType.CRIMSON_ISLE,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fThe next relay was placed by our consultant, Odger."
    ),
    RELAY_6(
        "6th Relay", LorenzVec(-69, 157, -879), IslandType.CRIMSON_ISLE,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fScarleton itself has one of the most robust connection to the 9f™ Network."
    ),
    RELAY_7(
        "7th Relay", LorenzVec(93, 86, 187), IslandType.HUB,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fThe next relay is on top of the shack next to the shady inn right here close to the pond."
    ),
    RELAY_8(
        "8th Relay", LorenzVec(0, 146, -75), IslandType.DUNGEON_HUB,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fThe next relay is on top of a statue in the dungeon hub."
    ),
    RELAY_9(
        "9th Relay", LorenzVec(-19.0, 88.5, -91.0), IslandType.HUB,
        "§e[NPC] §dTia the Fairy§f: §b✆ §f§r§fThe next relay is on top of the Auction House."
    );

    val chatPattern by RepoPattern.pattern(
        "relay.chat." + relayName.takeWhile { it != ' ' },
        chatMessage
    )

    fun checkChatMessage(string: String) = chatPattern.matches(string)
}
