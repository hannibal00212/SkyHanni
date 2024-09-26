package at.hannibal2.skyhanni.features.misc.discordrpc

import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.features.dungeon.DungeonAPI
import at.hannibal2.skyhanni.utils.LorenzUtils

object DiscordLocationKey {

    // normal keys follow a distinct pattern: lowercase the skyblock location and replace spaces with -
    private val normalRPC = setOf(
        "auction-house",
        "bank",
        "canvas-room",
        "coal-mine",
        "crimson-isle",
        "farm",
        "fashion-shop",
        "flower-house",
        "forest",
        "graveyard",
        "library",
        "mountain",
        "ruins",
        "tavern",
        "unincorporated",
        "village",
        "wilderness",
        "birch-park",
        "spruce-woods",
        "savanna-woodland",
        "dark-thicket",
        "jungle-island",
        "gold-mine",
        "slimehill",
        "the-barn",
        "mushroom-desert",
        "the-end",
        "around-colosseum",
        "barrier-street",
        "dreadfarm",
        "empty-bank",
        "lagoon-hut",
        "living-cave",
        "rift-gallery",
        "the-rift",
        "village-plaza",
        "wyld-woods",
        "glacite-tunnels",
        "jungle",
        "mithril-deposits",
        "precursor-remnants",
        "goblin-holdout",
        "magma-fields",
        "crystal-nucleus",
        "dungeon-hub"
    )
    // list of tokens where the name can be lowercased and spaces can be replaced with dashes

    private val specialRPC = mapOf(
        "Fisherman's Hut" to "fishermans-hut",
        "Farmhouse" to "farm",
        "Dragon's Nest" to "dragons-nest",
        "Windmill" to "the-barn",
        "Dark Auction" to "wilderness",
        "Catacombs Entrance" to "coal-mine",
        "Colosseum Arena" to "colosseum",
        "Museum" to "unincorporated",
        "Personal Garden" to "garden",

        "Jerry's Workshop" to "winter-island",
        "Jerry Pond" to "winter-island",
        "Mount Jerry" to "winter-island",
        "Gary's Shack" to "winter-island",
        "Glacial Cave" to "winter-island",
        "Terry's Shack" to "winter-island",
        "Hot Springs" to "winter-island",
        "Jerry Pond" to "winter-island",
        "Reflective Pond" to "winter-island",
        "Sunken Jerry Pond" to "winter-island",
        "Sherry's Showroom" to "winter-island",
        "Einary's Emporium" to "winter-island",

        "Archery Range" to "village",
        "Bazaar Alley" to "village",
        "Blacksmith" to "village",
        "Builder's House" to "village",
        "Election Room" to "village",
        "Hexatorum" to "village",
        "Thaumaturgist" to "village",
        "Weaponsmith" to "village",

        "Void Sepulture" to "the-end",
        "Void Slate" to "the-end",
        "Zealot Bruiser Hideout" to "dragons-nest",

        "Desert Settlement" to "mushroom-desert",
        "Oasis" to "mushroom-desert",
        "Desert Mountain" to "mushroom-desert",
        "Jake's House" to "mushroom-desert",
        "Trapper's Den" to "mushroom-desert",
        "Mushroom Gorge" to "mushroom-desert",
        "Glowing Mushroom Cave" to "mushroom-desert",
        "Overgrown Mushroom Cave" to "mushroom-desert",
        "Shepherd's Keep" to "mushroom-desert",
        "Treasure Hunter Camp" to "mushroom-desert",

        "Spider's Den" to "spiders-den",
        "Arachne's Burrow" to "spiders-den",
        "Arachne's Sanctuary" to "spiders-den",
        "Archaeologist's Camp" to "spiders-den",
        "Grandma's House" to "spiders-den",
        "Gravel Mines" to "spiders-den",
        "Spider Mound" to "spiders-den",

        "Melody's Plateau" to "savanna-woodland",
        "Viking Longhouse" to "spruce-woods",
        "Lonely Island" to "spruce-woods",
        "Howling Cave" to "birch-park",

        "Aristocrat Passage" to "dwarven-mines",
        "Barracks of Heroes" to "dwarven-mines",
        "C&C Minecarts Co." to "dwarven-mines",
        "Cliffside Veins" to "dwarven-mines",
        "Divan's Gateway" to "dwarven-mines",
        "Dwarven Tavern" to "dwarven-mines",
        "Dwarven Village" to "dwarven-mines",
        "Far Reserve" to "dwarven-mines",
        "Forge Basin" to "dwarven-mines",
        "Gates to the Mines" to "dwarven-mines",
        "Goblin Burrows" to "dwarven-mines",
        "Grand Library" to "dwarven-mines",
        "Great Ice Wall" to "dwarven-mines",
        "Hanging Court" to "dwarven-mines",
        "Lava Springs" to "dwarven-mines",
        "Miner's Guild" to "dwarven-mines",
        "Palace Bridge" to "dwarven-mines",
        "Rampart's Quarry" to "dwarven-mines",
        "Royal Mines" to "dwarven-mines",
        "Royal Palace" to "dwarven-mines",
        "Royal Quarters" to "dwarven-mines",
        "The Forge" to "dwarven-mines",
        "The Lift" to "dwarven-mines",
        "The Mist" to "dwarven-mines",
        "Upper Mines" to "dwarven-mines",

        "Dragon's Lair" to "mithril-deposits",
        "Fairy Grotto" to "crystal-nucleus",
        "Goblin Queen's Den" to "goblin-holdout",
        "Jungle Temple" to "jungle",
        "Khazad-dûm" to "magma-fields",
        "Lost Precursor City" to "precursor-remnants",
        "Mines of Divan" to "mithril-deposits",

        "Diamond Reserve" to "coal-mine",
        "Gunpowder Mines" to "coal-mine",
        "Lapis Quarry" to "coal-mine",
        "Obsidian Sanctuary" to "coal-mine",
        "Pigmen's Den" to "coal-mine",

        "Dwarven Base Camp" to "glacite-tunnels",
        "Fossil Research Center" to "glacite-tunnels",
        "Great Glacite Lake" to "glacite-tunnels",
        "Glacite Mineshafts" to "glacite-tunnels",
    ) // maps sublocations to their broader image

    private val specialNetherRPC = arrayOf(
        "Aura's Lab",
        "Barbarian Outpost",
        "Belly of the Beast",
        "Blazing Volcano",
        "Burning Desert",
        "Cathedral",
        "Chief's Hut",
        "Courtyard",
        "Crimson Fields",
        "Dojo",
        "Dragontail Auction House",
        "Dragontail Bank",
        "Dragontail Bazaar",
        "Dragontail Blacksmith",
        "Dragontail Townsquare",
        "Dragontail",
        "Forgotten Skull",
        "Igrupan's Chicken Coop",
        "Igrupan's House",
        "Kuudra's Hollow",
        "Mage Council",
        "Mage Outpost",
        "Magma Chamber",
        "Matriarch's Lair",
        "Minion Shop",
        "Mystic Marsh",
        "Odger's Hut",
        "Plhlegblast Pool",
        "Ruins of Ashfang",
        "Scarleton Auction House",
        "Scarleton Bank",
        "Scarleton Bazaar",
        "Scarleton Blacksmith",
        "Scarleton Minion Shop",
        "Scarleton Plaza",
        "Scarleton",
        "Smoldering Tomb",
        "Stronghold",
        "The Dukedom",
        "The Wasteland",
        "Throne Room"
    )
    // list of nether locations because there are soo many (truncated some according to scoreboard)

    private val specialRiftRPC = mapOf(
        "Enigma's Crib" to "wyld-woods",
        "Broken Cage" to "wyld-woods",
        "Shifted Tavern" to "wyld-woods",
        "Pumpgrotto" to "wyld-woods",
        "Otherside" to "wyld-woods",

        "Black Lagoon" to "the-rift",
        "Lagoon Cave" to "the-rift",
        "Leeches Lair" to "the-rift",
        "Dolphin Trainer" to "the-rift",
        "Mirrorverse" to "the-rift",
        "Book In A Book" to "the-rift",
        "\"Your\" Island" to "the-rift",

        "Lagoon Hut" to "lagoon-hut",
        "Around Colosseum" to "around-colosseum",
        "Rift Gallery Entrance" to "rift-gallery",
        "Great Beanstalk" to "dreadfarm",
        "Taylor's" to "taylors",
        "Lonely Terrace" to "taylors",
        "Half-Eaten Cave" to "half-cave",
        "Déjà Vu Alley" to "living-cave",
        "Living Stillness" to "living-cave",

        "West Village" to "village-plaza",
        "Cake House" to "village-plaza",
        "Infested House" to "village-plaza",
        "Murder House" to "village-plaza",
        "Barry Center" to "village-plaza",
        "Barry HQ" to "village-plaza",

        "Photon Pathway" to "stillgore-chateau",
        "Stillgore Château" to "stillgore-chateau",
        "Oubliette" to "stillgore-chateau",
        "Fairylosopher Tower" to "stillgore-chateau"
    )

    private fun getAmbiguousKey(location: String): String {
        val island = LorenzUtils.skyBlockIsland

        DungeonAPI.dungeonFloor?.lowercase()?.let {
            if (it.startsWith("m")) {
                return "master-mode"
            }
            if (it.startsWith("f")) {
                return "dungeon"
            }
        }

        return when (location) {
            "Wizard Tower" -> {
                when (island) {
                    IslandType.THE_RIFT -> "rift-tower"
                    IslandType.HUB -> "wizard-tower"
                    else -> "skyblock-logo"
                }
            }

            "The Bastion" -> {
                when (island) {
                    IslandType.THE_RIFT -> "wyld-woods"
                    IslandType.CRIMSON_ISLE -> "crimson-isle"
                    else -> "skyblock-logo"
                }
            }

            "Community Center" -> {
                when (island) {
                    IslandType.HUB -> "village"
                    IslandType.CRIMSON_ISLE -> "crimson-isle"
                    else -> "skyblock-logo"
                }
            }

            "Colosseum" -> {
                when (island) {
                    IslandType.HUB -> "colosseum"
                    IslandType.THE_RIFT -> "around-colosseum"
                    else -> "skyblock-logo"
                }
            }

            else -> "skyblock-logo"
        }
    }

    fun getDiscordIconKey(location: String): String {
        val keyIfNormal = location.lowercase().replace(' ', '-')

        return if (normalRPC.contains(keyIfNormal)) {
            keyIfNormal
        } else if (specialRPC.containsKey(location)) {
            specialRPC[location]!!
        } else if (specialNetherRPC.contains(location)) {
            "crimson-isle"
        } else if (specialRiftRPC.containsKey(location)) {
            specialRiftRPC[location]!!
        } else {
            getAmbiguousKey(location) // will return skyblock-logo if not found
        }
    }
}
