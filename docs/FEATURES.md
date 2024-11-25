# SkyHanni - List of all Features

Use `/sh` or `/skyhanni` to open the SkyHanni config in game.
<details open><summary>

## Chat

</summary>

+ Options to change the player chat format (show prefix for channel 'all', hide player rank color, hide colon after
  player name, hide/change SkyBlock level format, hide/change elite position format, edit channel prefix design)
+ Using a clean chat format for player messages (removing the rank prefix, every player writes in the same color)
+ Dungeon Filter (Removing annoying chat messages from the dungeon)
+ Dungeon Boss Message hider (includes The Watcher as well)
+ Option to hide the death messages of other players, except for players who are close to the player, inside the dungeon
  or during a Kuudra fight.
+ Scan messages sent by players in all-chat for blacklisted words and greys out the message.
+ Chat peeking (holding key to display chat without opening the chat gui)
+ Compact Potion Effect Messages
+ **Arachne Chat Hider**
    + Hide chat messages about the Arachne Fight while outside of Arachne's Sanctuary
+ Option to shorten the **bestiary level-up** message.
+ Chat **Translator** - NetheriteMiner
    + Click on any chat message sent by another player to translate it to English.
+ **Sack Change** chat message hider. - hannibal2
    + Enable this option instead of Hypixel's own setting to hide the chat message while enabling mods to utilize sack
      data for future features.
+ Adds chat symbols such as iron man/bingo/nether faction like SBA had/has. - CalMWolfs
    + Will not break with emblems.
    + Optional if left or right side of name.
    + Should not break with other mods.
+ Hide the repeating fire sale reminder chat messages. - hannibal2
+ Add tab list fire sale advertisement hider. - nea
+ SkyBlock XP Chat. - Thunderblade73
    + Sends the SkyBlock XP message from the action bar into the chat.
+ Rarity text to pet drop messages. - Empa (https://github.com/hannibal002/SkyHanni/pull/1136)
+ Bits Gained Chat Message. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1487)
+ Option to reorder or hide every part of a player chat message. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + Parts to move around: SkyBlock Level, Emblem, player name, guild rank, private island rank, crimson faction, iron man mode, bingo level and Private Island Guest.
    + Player messages impacted by this: all chat, party, guild, private chat, /show.
    + This might break hover/click on chat actions (Will be fixed later).
+ Hide Level Brackets. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + Hide the gray brackets in front of and behind the level numbers.
+ Level Color As Name. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + Use the color of the SkyBlock level for the player color.

#### Chat Filter

- Hiding annoying messages in the Hub of Hypixel (MVP player joins, other player loot boxes, prototype message,
  radiating generosity, Hypixel tournaments)
- Hiding Empty messages.
- Warping messages (Sending requests and switching servers)
- Welcome Message when entering SkyBlock.
- Powder Mining messages hider
- Winter gift messages hider
- Many other messages (Not separated into own categories yet)
+ More chat filters. - Mikecraft1224 (https://github.com/hannibal002/SkyHanni/pull/1750)
    + Filters for rare dungeon chest rewards and sacrifice messages from other players.
+ Add config option to hide achievement spam on Alpha. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2076)
+ Added chat compacting for 'items in stash' warnings. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2639)

</details>
<details open><summary>

## Dungeon

</summary>

+ Clicked Blocks (Showing the block behind walls AFTER clicked on a chest, Wither Essence or a lever)
+ Current milestone display.
+ Death Counter (Changing color depending on amount, hidden at 0 deaths)
+ Clean Ending (After the last dungeon boss has died, all entities and particles are no longer displayed and the music
  stops playing, but the dungeon chests are still displayed)
+ Option to exclude guardians in F3 and M3 from the clean end feature (only when sneaking)
+ Hiding damage splashes while inside the boss room (replacing a broken feature from Skytils)
+ Highlight deathmites in red color.
+ Hide Superboom TNT lying around in dungeon.
+ Hide Blessings lying around in dungeon.
+ Hide Revive Stones lying around in dungeon.
+ Hide Premium Flesh lying around in dungeon.
+ Hide Journal Entry pages lying around in dungeon.
+ Dungeon Copilot (Suggests to you what to do next in dungeon)
+ Option to hide key pickup and door open messages in dungeon.
+ Hide Skeleton Skulls lying around in dungeon.
+ Highlight Skeleton Skulls in dungeon when combining into a skeleton in orange color (not useful combined with feature
  Hide Skeleton Skull)
+ Hide the damage, ability damage and defense orbs that spawn when the healer is killing mobs.
+ Hide the golden fairy that follows the healer in dungeon.
+ Catacombs class level color in party finder inventory.
+ Visual highlight chests that have not yet been opened in the Croesus inventory.
+ Croesus Limit Warning. - saga (https://github.com/hannibal002/SkyHanni/pull/1908)
    + Sends a warning when you are close to the Croesus Chest Limit.
+ Outline Dungeon Teammates. - Cad
+ Dungeon Colored Class Level. - hannibal2
    + Color class levels in the tab list. (Also hide rank colors and emblems because who needs that in dungeons anyway?)
+ Soulweaver Skull Hider in the Dungeon Object Hider. - nea
    + Hide the annoying soulweaver skulls that float around you if you have the soulweaver gloves equipped.
+ Hide particles and damage splashes during the terracotta phase in dungeons F6 and M6. - hannibal2
+ Show available classes in the tooltip. - Conutik
    + Shows in the dungeon party finder when hovering over a group.
    + Highlights your selected class in green if it's available.
+ Kismet tracking for dungeon chests. - Thunderblade73
    + Highlight chests which have been rerolled inside Croesus
    + Shows kismet amount at the re-roll button
+ SA Jump Notification. - CarsCupcake (https://github.com/hannibal002/SkyHanni/pull/852)
    + Warn shorty before a Shadow Assassin jumps to you in dungeons.
+ Notifications for architect on puzzle fail. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1197)
    + Shows Title.
    + Shows button in chat to retrieve from sack.
    + Only works when having enough Architect First Drafts in the sack.
+ Dungeon hub race waypoints. - seraid (https://github.com/hannibal002/SkyHanni/pull/1471)
    + Only works for Nothing; No return races.
+ Added the ability to hide solo class, solo class stats and fairy dialogue chat messages in Dungeons. - raven (https://github.com/hannibal002/SkyHanni/pull/1702)
+ Added highlight for starred dungeon mobs. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1558)
+ Added highlight for Fel skulls. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1558)
    + Optionally draws a line to them as well.
+ Added a Secret Chime for Dungeons with adjustable pitch and sound. - Ovi_1 (https://github.com/hannibal002/SkyHanni/pull/2478)
    + The sound and pitch of chimes in dungeons are customizable.
+ Added Terminal Waypoints. - Stella (https://github.com/hannibal002/SkyHanni/pull/2719)
    +  Displays waypoints during the F7/M7 Goldor Phase.

</details>
<details open><summary>

## Inventory

</summary>

+ Not Clickable Items
    + Mark items gray in your inventory when they are not supposed to be moved in certain GUIs, and make green lines
      around items that meet that requirement.
    + Works in:
        + In NPC sell inventories, ender chests and backpacks, salvaging in the dungeon hub, player trade
        + bazaar, action house, accessory bag, sack of sacks, fishing bag, potion bag,
        + chests on the private island, attribute fusion, equipment GUI, Rift Motes Grubber
    + Option to allow/block clicks
+ Option to change the gray-out opacity for 'Not Clickable Items'.
+ Set stack number for specific items (stars for crimson armor, minion tier, pet level, new year cake, for golden and
  diamond dungeon heads the floor number, the tier of master skull and master star, kuudra keys, skill level, and
  collection level)
+ Sack name (show short name of sacks)
+ Anvil Combine Helper (When putting an enchanted book into the first slot of the anvil, all items with the same
  enchantment are highlighted in the inventory)
+ compact star counter on all items (not only on items with dungeon stars and master stars but also on crimson armors,
  cloaks and fishing rods)
+ RNG meter features (in the catacombs RNG meter inventory show the dungeon floor number and highlight floors without a
  drop selected and highlighting the selected drop in the RNG meter inventory for slayer or catacombs)
+ Show the tuning stats in the Thaumaturgy inventory.
+ Show the amount of selected tuning points in the stats tuning inventory.
+ Highlight the selected template in the stats tuning inventory.
+ Show the stats for the tuning point templates.
+ Highlight depleted Bonzo's Masks in your inventory.
+ Highlight stuff that is missing in the SkyBlock level guide inventory.
+ **Auction Highlighter** - Highlight own items that are sold in green and that are expired in red.
+ Warning when outbid on an auction. - seraid (https://github.com/hannibal002/SkyHanni/pull/1818)
+ Highlight your own lowest BIN auctions that are outbid. - hannibal2
+ **Unclaimed Rewards** - Highlight contests with unclaimed rewards in the jacob inventory.
+ **Contest Time** - Show the real time format to the farming contest description.
+ **Pet Candies Used number**
    + Works even after Hypixel removed the `10 pet candies applied` line
+ **Estimated Armor Value display**
    + Shows the price of all 4 armor pieces combined inside the wardrobe
+ Show numbers of Pocket Sack-In-A-Sack applied on a sack (Default disabled, contributed by HiZe)
+ **Bestiary overlay** - HiZe
    + Options for change number format, display time, number type and hide maxed.
    + Highlight maxed bestiaries.
+ Chest Value - HiZe
    + Shows a list of all items and their price when inside a chest on your private island.
+ In Melody's Harp, show buttons as stack size. - NetheriteMiner
    + Intended to be used with Harp Keybinds
+ Harp GUI Scale. - Thunderblade73
    + Automatically sets the GUI scale to AUTO when entering the Harp.
+ Harp Quick Restart. - Thunderblade73
    + Once you've launched the harp, quickly hit the close button in the harp menu to initiate the selected song.
+ **Quick Craft Confirmation**. - Cad
    + Require Ctrl+Click to craft items that aren't often quick crafted (e.g. armor, weapons, accessories).
    + Sack items can be crafted normally.
+ **Shift Click Equipment**. - Thunderblade73
    + This removes the need to shift-click to swap the equipment items, without the annoying "pick up animation".
+ Copy Underbid Price. - hannibal2
    + Copies the price of an item in the "Create BIN Auction" minus 1 coin into the clipboard for faster under-bidding.
+ Power Stone Guide features. - hannibal2
    + Highlight missing power stones, show their total bazaar price, and allows to open the bazaar when clicking on the
      items in the Power Stone Guide.
+ Option to make normal clicks to shift clicks in equipment inventory. - Thunderblade73
+ Show pet items XP Share and Tier Boost as small icons next to the pet in an inventory. - Thunderblade73
+ Shift Click Brewing. - Thunderblade73
    + Makes normal clicks to shift clicks in Brewing Stand inventory.
+ Low Quiver Notification. - CarsCupcake
    + This will notify you via title if your quiver is low on arrows according to chat message.
+ Added not fully completed tasks in Way to gain SkyBlock XP menus. - Thunderblade73
    + Works in the subcategories.
    + It does not work with infinite tasks.
+ Max Items With Purse. - NetheriteMiner
    + Calculates the maximum number of items that can be purchased from the Bazaar with the number of coins in your
      purse.
+ Copy Underbid Keybind. - Obsidian
    + Copies the price of the hovered item in Auction House minus 1 coin into the clipboard for easier under-bidding.
+ Option in the Auction House search browser to search for the item on coflnet.com. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1743)
+ Gfs message after super crafting. — Zickles
    + Adding a clickable message to pick up the super crafted items from sacks.
+ Added Inferno Minion Fuel pickup prevention. - Zickles (https://github.com/hannibal002/SkyHanni/pull/1103)
    + Blocks picking up the Inferno Minion or replacing the fuel inside when expensive minion fuels are in use.
+ AH Show Price Comparison. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/339)
    + Highlight auctions based on the difference between their estimated value and the value they are listed for.
    + Options to change the colors
+ Highlight options in /tab. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1175)
    + Green for enabled
    + Red for disabled
+ SBA style Enchant Parsing. - Vixid (https://github.com/hannibal002/SkyHanni/pull/654)
    + Option to remove vanilla enchants in tooltip.
    + Option to remove enchant descriptions.
    + Option to change enchant formatting.
    + Also parses tooltips from /show.
+ Favorite Power Stones. - saga (https://github.com/hannibal002/SkyHanni/pull/2002)
    + Highlighted in the Thaumaturgy inventory.
    + Shift-click to add/remove them.
+ Add Page Scrolling. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2164)
    + Allows scrolling in any inventory with multiple pages.
+ Add Reforge Helper. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1437)
    + Add reforge locking similar to SkyblockAddons.
    + Work with Hex.
    + Show a list of all possible reforges for the item, with stats.
    + The reforge list can be sorted by a specific stat.
    + Optionally show the different stat values compared to the current reforge.
+ Show Bestiary Level as Stack Size. - saga (https://github.com/hannibal002/SkyHanni/pull/1945)
    + Can be enabled/disabled on the Item Number list.
+ Item Pickup Log. - catgirlseraid (https://github.com/hannibal002/SkyHanni/pull/1937)
+ Display the price per Stonk when taking the minimum bid in the Stonks Auction (Richard Menu). - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2195)
+ Added Ultimate Enchant Star. - Empa (https://github.com/hannibal002/SkyHanni/pull/2612)
    + Shows a star on Enchanted Books with an Ultimate Enchant.
+ Added Focus Mode. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2694)
    +  In Focus Mode, only the name of the item is displayed instead of the full description.
+ Added WASD keybinds to the Abiphone Snake Game. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1968)
+ Added Attribute Overlay. - Empa (https://github.com/hannibal002/SkyHanni/pull/2001)
    +  Options to highlight good rolls in different colors, show only certain attributes, etc.
+ Added helpers for Essence Shops and Carnival Event Upgrade Shops. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2423)
    +  Assists with maxing upgrades.
+ Added New Year Cake Tracker. - Daveed. (https://github.com/hannibal002/SkyHanni/pull/2237)
    +  Highlights unowned cakes in AH.
    +  Displays a list of missing cake years.

</details>
<details open><summary>

## Item Abilities

</summary>

+ Show the **cooldown of items** in the inventory. - hannibal2
    + Option to change the item background according to the cooldown.
    + Supports dungeon mage cooldown reduction. - Cad
+ Hiding the flame particles when using the Fire Veil Wand ability.
+ Circle around the player when having the Fire Veil Wand ability active.
+ Lesser Orb of Healing Hider. - jani
+ Wand of Strength cooldown is now displayed. - saga (https://github.com/hannibal002/SkyHanni/pull/1948)
    + The cooldown displayed is for the buff, not the item usage.
+ Added Tactical Insertion Ability Cooldown feature. - DungeonHub (https://github.com/hannibal002/SkyHanni/pull/2278)

</details>
<details open><summary>

## Summoning Mobs

</summary>

- Summoning Soul Display (Show the name of dropped soul lying on the ground, not working in dungeon when Skytils' "Hide
  Non-Starred Mobs Nametags" is enabled)
- Option to hide the nametag of your spawned summoning mobs.
- Option to mark the own summoning mobs in green.
- Summoning Mob Display (Show the health of your spawned summoning mobs listed in an extra GUI element and hiding the
  corresponding spawning/de-spawning chat messages)

</details>
<details open><summary>

## Ashfang

</summary>

- Show a cooldown when the player gets an ability block effect during the ashfang fight.
- Display a timer until Ashfang brings his underlings back to him.
- Ashfang Gravity Orbs display.
- Ashfang Blazing Souls display.
- Highlight the different ashfang blazes in their respective color.
- Option to hide all the particles around the ashfang boss.
- Option to hide the name of full health blazes around ashfang (only useful when highlight blazes is enabled)
- Option to hide damage splashes around ashfang.

</details>
<details open><summary>

## Minion

</summary>

- A display that show the last time the hopper inside a minion has been emptied.
- A marker to the last opened minion for a couple of seconds (seen through walls)
- Option to hide mob nametags close to minions.
- Minion hopper coins per day display (Using the held coins in the hopper and the last time the hopper was collected to
  calculate the coins a hopper collects in a day)
- Minion name display with minion tier.
- **Minion Craft Helper** - Show how many more items you need to upgrade the minion in your inventory. Especially useful
  for bingo.

+ Shows how much skill experience you will get when picking up items from the minion storage. - Thunderblade73

</details>
<details open><summary>

## Bazaar

</summary>

+ Showing colors in the order inventory for outbid or fully bought/sold items.
+ Best Sell Method (Calculating the difference between instant-selling or using sell order for a selected bazaar item)
+ Saves missing items from cancelled buy orders to clipboard for faster re-entry.
+ Update Timer showing when the next api data update happens.
+ Price Website button. - hannibal2
    + Adds a button to the bazaar product inventory that will open the item page in skyblock.bz.
+ Craft Materials From Bazaar. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1104)
    + Show in a crafting view a shopping list of materials needed when buying from the Bazaar.

</details>
<details open><summary>

## Fishing

</summary>

+ **Trophy Counter**
    + After fishing a new trophy fish, showing you in chat how many more trophies you have collected in total.
    + **Trophy Counter Design** - Change the way trophy fish messages gets displayed in the chat. - appable0
    + **Hide Repeated Catches** - Delete past catches of the same trophy fish from chat. - appable0
    + Show total amount of all trophy fish rarities at the end of the chat message.
    + **Trophy Fish Info** - Hover over trophy fish caught chat message to see information and stats about the trophy
      fish. - appable0
    + **Fillet Tooltip** - Adding fillet amount and price to the tooltip of a trophy fish. Left shift to show stack
      value. - appable0
+ **Hide Bronze/Silver Duplicates** - Hiding chat message when catching a duplicate bronze/silver trophy fish.
+ **Shorten Fishing Message**
    + Replacing the green chat message when fishing a sea creature with a more clean format
    + Adds **Double Hook** to the **sea creature chat message** instead of in a previous line. - appable0
+ Highlight Thunder Sparks that spawn after killing a Thunder.
+ **Barn Timer**
    + Show the time and amount of sea creatures while fishing on the barn via hub.
    + Works in crystal hollows too (worm fishing)
    + Keybind to manually reset the barn/worm fishing timer. - CarsCupcake
    + Warning sound when the worm fishing cap of 60 is hit. - CarsCupcake
    + Has support for the game mode Stranded. - hannibal2
+ **Shark Fish Counter** - Counts how many sharks have been caught.
+ **Odger waypoint** - Show the Odger waypoint when trophy fishes are in the inventory and no lava rod in hand.
+ Showing fished item names
+ **Chum/Chumcap Bucket Hider**
    + Hide the name tags of Chum/Chumcap Bucket from other players.
    + Hide the Chum/Chumcap Bucket.
    + Hides your own Chum/Chumcap Bucket.
+ Highlight and outline feature for rare sea creatures. - Cad
+ Fishing Hook Display. - hannibal2
    + Display the Hypixel timer until the fishing hook can be pulled out of the water/lava, only bigger and on your
      screen.
+ Alerts when the player catches a Legendary Sea Creature. - Cad
+ **Fishing Bait Warnings.** - cimbraien
    + Option to warn when no bait is used.
    + Option to warn when used bait is changed.
+ Sea Creature Tracker. - hannibal2
    + Allows to only show single variants, e.g. water or lava or winter.
+ Totem Overlay. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1139)
    + Option to change the overlay distance.
    + Option to hide Totem Particles.
    + Option to show the effective area of a totem.
    + Option to get reminded when a totem is about to expire.
+ Trophy Fishing Display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1754)
    + Similar to Skytils, but with more options.
    + Option to sort by item rarity, total amount, rarity amount, the highest rarity, or name alphabetically.
    + Option to change what information is displayed (rarity, name, icon, total caught).
    + Option to show only missing trophies.
    + Option to highlight new catches in green.
    + Options for when to show the display: always in Crimson Isle, in inventory, with rod in hand, or on keybind press.
    + Option to show only when wearing a full hunter armor set.
    + Options to show checkmarks or crosses instead of numbers.
    + Hover over a trophy fish to see the percentage caught and how to gain progress.
+ Added Golden Fish Timer. - Empa (https://github.com/hannibal002/SkyHanni/pull/1941)
    + Includes an option to warn you when to throw your rod.
    + Shows how weak the golden fish is, as a nametag.
    + Also works on Stranded.
+ Added an alert for Gold or Diamond Trophy Fish catches. - ReyMaratov (https://github.com/hannibal002/SkyHanni/pull/2615)
    + Displays a popup with the trophy name, rarity, and amount of the catch.
    + Optionally, also plays a sound.
+ Added Lava Replacement. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1885)
    +  Replaces the lava texture with the water texture.
    +  Primarily used for lava fishing in the Crimson Isle, but can be used anywhere else if the option is enabled.

</details>
<details open><summary>

## Damage Indicator

</summary>

- Show the remaining health of selected bosses in the game in a bigger GUI.
- Send a chat message when the boss is healing himself.
- Option to hide or shorten the boss name above the health display.
- Specify for what bosses the damage indicator should be used.
- Option to hide the damage splash around the damage indicator (Supporting the Skytils damage splash)
- Show the collected damage over time (literally the DPS) for the last few seconds.
- Show the hits during the hit phase for Voidgloom Seraphs.
- Show the laser phase cooldown during the Voidgloom Seraph 4 fight.
- Option to show the health of Voidgloom Seraph 4 during the laser phase (useful when trying to phase skip)
- Show when Revenant Horror 5 is about to BOOM.
- Hide the vanilla nametag of damage indicator bosses.
- Garden Pests in Damage Indicator
- **Time to Kill**
    - Show the time it takes to kill the Slayer boss.
+ Added Broodmother support to the Damage Indicator. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2325)

+ **Vampire Slayer**
    + Show the amount of HP missing until the steak can be used on the vampire slayer on top of the boss.
    + Show a timer until the boss leaves the invincible Mania Circles state.
    + Show the percentage of HP next to the HP.

</details>
<details open><summary>

## Slayer

</summary>

+ Hide poor slayer drop chat messages.
+ Slayer **Mini Boss:**
    + Highlight the mob.
    + Show a line from player crosshair to the mob.
+ Enderman Slayer **Yang Glyph**:
    + Highlight the beacon in red color.
        + Supports beacon in hand and beacon flying.
    + Show timer till it explodes.
    + Show a line to the beacon. - hannibal2
    + Warning when enderman slayer beacon spawns - dragon99z
+ Highlight enderman slayer Nukekubi (Skulls) - dragon99z
+ Add a line to Nukekebi Skull. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2148)
+ Hide the name of the mobs you need to kill in order for the Slayer boss to spawn. Exclude mobs that are damaged,
  corrupted, runic or semi rare.
+ Cooldown when the Fire Pillar from the Blaze Slayer will kill you.
+ Custom countdown sound for the Fire Pillar timer for the Blaze Slayer.
+ Option to hide sound and entities when building the Fire Pillar for the Blaze Slayer.
+ Faster and permanent display for the Blaze Slayer daggers.
+ Mark the right dagger to use for blaze slayer in the dagger overlay.
+ Warning when the fire pit phase starts for the Blaze Slayer tier 3
+ Hide particles and fireballs near blaze slayer bosses and demons.
+ Option to remove the wrong dagger messages from chat.
+ Warning when wrong slayer quest is selected, or killing mobs for the wrong slayer.
+ **Item Profit Tracker**
    + Count items collected and how much you pay while doing slayer, calculates final profit
    + Shows the price of the item collected in chat (default disabled)
+ **Items on Ground**
    + Show item name and price over items laying on ground (only in slayer areas)
+ **Broken Hyperion Warning**
    + Warns when right-clicking with a Wither Impact weapon (e.g. Hyperion) no longer gains combat exp
    + Kill a mob with melee-hits to fix this hypixel bug
    + Only works while doing slayer
+ SOS/Alert/Warning Flare Display. - HiZe + hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1803)
    + Warn when the flare is about to despawn (chat, title or both).
    + Change the display type (as a GUI element, in the world, or both).
    + Show effective area (as a wireframe, filled or circle).
    + Show mana regeneration buff next to the GUI element.
    + Option to hide flare particles.
+ Title warning when picking up an expensive slayer item
+ **RNG Meter Display**
    + Display amount of bosses needed until the next RNG Meter item drops
    + Warn when no item is set in the RNG Meter
    + Hide the RNG Meter message from chat if the current item is selected
+ Vampire Slayer Features – HiZe
    + Highlight your own boss (color can be changed)
    + Change color when the boss is below 20% (can use steak) (can change color)
    + Highlight other players' boss only if you hit them (can be toggled)
    + Highlight Co-Op members' boss (you need to write their name in the config)
    + Highlight Killer Spring and Blood Ichor, TwinClaws warning
    + Sound for TwinClaws
    + Option to delay c notification and sound (in millis, configurable)
    + Draw line starting from the boss head to the Killer Spring/Blood Ichor (if the boss is highlighted)
    + Draw line starting from your crosshair to the boss head
    + Configurable to work only on your boss, on bosses hit, or on coop boss
+ Hide particles around enderman slayer bosses and mini bosses
+ Boss Spawn Warning - HiZe + hannibal2
    + Send a title when your slayer boss is about to spawn
    + Configurable percentage at which the title and sound should be sent

### Diana

+ Show burrows near you.
+ Uses Soopy's Guess Logic to find the next burrow. Does not require SoopyV2 or chat triggers to be installed.
+ Show the way from one burrow to another smoothly.
+ Warps to the nearest warp point on the hub, if closer to the next burrow.
+ **Griffin Pet Warning**
    + Warn when holding an Ancestral Spade while no Griffin pet is selected.
+ **Inquisitor Sharing**
    + Share waypoints for inquisitors you find with your party.
    + Show a timer until the inquisitor will despawn.
+ Diana Profit Tracker. - hannibal2
    + Same options as slayer and fishing trackers.
+ Highlight for the Minos Inquisitors to make them easier to see. - Cad
+ Mythological Mob Tracker. - hannibal2
    + Counts the different mythological mobs you have dug up.
    + Show percentage how often what mob spawned.
    + Hide the chat messages when digging up a mythological mob.
+ Diana Chat hider. - hannibal2
    + Hide chat messages around griffin burrow chains and griffin feather drops and coin drops.
+ Customizable Inquisitor Highlight color. - Empa (https://github.com/hannibal002/SkyHanni/pull/1323)
+ Mobs since last Inquisitor to Mythological Creature Tracker. - CuzImClicks (https://github.com/hannibal002/SkyHanni/pull/1346)
+ Blaze Slayer fire pillar display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1766)
    + Shows a large display with a timer when the Fire Pillar is about to explode.
    + Also displays for other players' bosses.

</details>
<details open><summary>

## Marked Players

</summary>

+ Adding or removing players as marked with command /shmarkplayer
+ Highlight marked players in the world.
+ Highlight marked player names in chat.
+ Mark the own player name.

</details>
<details open><summary>

## Bingo

</summary>

+ Shortens chat messages about skill level ups, collection gains and new area discoveries while on bingo.
+ Bingo Card
+ Show the duration until the next hidden bingo goal tip gets revealed. - hannibal2
+ Support for tips in hidden bingo card display. - hannibal2
+ Support for 'found by' info in bingo card. - hannibal2
+ Bingo Goal Rank as stack size in Bingo Card. - Erymanthus
+ Option to only show tier 1 Minion Crafts in the Helper display when their items needed are fully collected. -
  hannibal2
+ Option to click in the bingo card viewer on goals to mark them as highlighted. - hannibal2
    + If at least one goal is highlighted, non-highlighted goals will be hidden.
+ Send a chat message with the change of community goal percentages after opening the bingo card inventory. - hannibal2

</details>
<details open><summary>

## Mobs

</summary>

+ Arachne keeper highlighter.
+ Area boss highlighter.
+ Area boss spawn timer.
+ Corleone highlighter.
+ Zealots, Bruisers and Special Zealot highlighter.
+ Highlight corrupted mobs.
+ **Arachne Minis Hider** - Hides the nametag above arachne minis.
+ **Arachne Boss Highlighter** - Highlight the arachne boss in red and mini bosses and orange.
+ "Line to Arachne" setting, just like with slayer mini bosses. - azurejelly (https://github.com/hannibal002/SkyHanni/pull/1888)
+ Countdown for Arachne spawn. - Cad
    + Supports quick spawns.
+ Added Arachne Kill Timer. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/1993)
    + Shows how long it took to kill Arachne when the fight ends.
+ Option to hide the vanilla particles around enderman

</details>
<details open><summary>

## Garden Features

</summary>

+ **Copper Price** - Show copper to coin prices inside the Sky Mart inventory.
+ **Visitor Display** - Show all items needed for the visitors.
+ **Visitor Highlight** - Highlight visitor when the required items are in the inventory or the visitor is new and needs
  to checked what items it needs.
+ **Show Price** - Show the Bazaar price of the items required for the visitors.
+ **Crop Milestone** Number - Show the number of crop milestone in the inventory.
+ Show the progress bar until maxed crop milestone in the crop milestone inventory. - hannibal2
+ **Crop Upgrades** Number - Show the number of upgrades in the crop upgrades inventory.
+ **Visitor Timer** - Timer when the next visitor will appear, and a number how many visitors are already waiting.
+ **Visitor Notification** - Show as title and in chat when a new visitor is visiting your island.
+ **Plot Price** - Show the price of the plot in coins when inside the Configure Plots inventory.
+ **Garden Crop Milestone Display** - Shows the progress and ETA until the next crop milestone is reached and the
  current crops/minute value. (Requires a tool with either a counter or cultivating enchantment)
+ **Best Crop Display** - Lists all crops and their ETA till next milestone. Sorts for best crop for getting garden
  level or SkyBlock level.
+ **Copper Price** - Show the price for copper inside the visitor gui.
+ **Amount and Time** - Show the exact item amount and the remaining time when farmed manually. Especially useful for
  ironman.
+ **Custom Keybinds** - Use custom keybinds while having a farming tool or Daedalus Axe in the hand in the garden.
+ **Optimal Speed** - Show the optimal speed for your current tool in the hand. (Ty MelonKingDE for the values)
    + Also available to select directly in the rancher boots overlay (contributed by nea)
+ Desk shortcut in SkyBlock Menu.
+ **Garden Level Display** - Show the current garden level and progress to the next level.
+ **Fake garden level up** message: - hannibal2
    + In 10k garden exp steps after level 15.
    + Uses the overflow exp that hypixel still calculates (maybe official upgrade with more garden levels in the
      future?).
    + Click on the message to open the garden level display. - J10a1n15
+ **Farming Weight and Leaderboard**
    + provided by the Elite SkyBlock farmers.
    + next leaderboard position eta.
    + Instantly showing the next player in the lb when passing someone on the leaderboard. - Kaeso
    + Chat message how many places you dropped in the farming weight lb when joining garden.
+ **Dicer Counter** - Count RNG drops for Melon Dicer and Pumpkin Dicer.
+ **Warn When Close** - Warn with title and sound when the next crop milestone upgrade happens in 5 seconds. Useful for
  switching to a different pet for leveling.
+ **Money per Hour**
    + Displays the money per hour YOU get with YOUR crop/minute value when selling the items to bazaar.
    + Supports the dicer drops from melon and pumpkins as well. - CalMWolfs
    + Supports armor drops. - CalMWolfs
+ Farming contest timer.
+ Wrong fungi cutter mode warning.
+ Show the price per garden experience inside the visitor gui.
+ Support for mushroom cow pet perk. (Counting and updating mushroom collection when breaking crops with mushroom
  blocks, extra gui for time till crop milestones)
+ Blocks/Second display in crop milestone gui.
+ Farming armor drops counter
+ **Colored Name** - Show the visitor name in the color of the rarity.
+ **Visitor Item Preview** - Show the base type for the required items next to new visitors (Note that some visitors may
  require any crop)
+ **Money per Hour Advanced stats** - Show not only Sell Offer price but also Instant Sell price and NPC Sell price (
  Suggestion: Enable Compact Price as well for this)
+ **Anita Inventory**
    + **Medal Profit**
        + Helps to identify profitable items to buy at the Anita item shop and potential profit from
        + selling the item at the auction house.
    + **Extra Farming Fortune**
        + Show current tier and cost to max out in the item tooltip.
+ **Composter Compact Display** - Displays the compost data from the tab list in a compact form as gui element.
+ **Composter Upgrade Price** - Show the price for the composter upgrade in the lore
+ **Highlight Upgrade** - Highlight Upgrades that can be bought right now.
+ **Number Composter Upgrades** - Show the number of upgrades in the composter upgrades inventory.
+ **Composter Inventory Numbers** - Show the amount of Organic Matter, Fuel and Composts Available while inside the
  composter inventory.
+ **True Farming Fortune - Displays** current farming fortune, including crop-specific bonuses. (contributed by appable)
+ **Tooltip Tweaks Compact Descriptions** - Hides redundant parts of reforge descriptions, generic counter description,
  and Farmhand perk explanation. (contributed by appable)
+ **Tooltip Tweaks Breakdown Hotkey** - When the keybind is pressed, show a breakdown of all fortune sources on a
  tool. (contributed by appable)
+ **Tooltip Tweaks Tooltip Format** - Show crop-specific farming fortune in tooltip. (contributed by appable)
+ **Compost Low Notification** - Shows a notification as title when organic matter/fuel is low.
+ **Jacob's Contest Warning** - Show a warning shortly before a new jacob contest starts.
+ **Composter Overlay** - Show the cheapest items for organic matter and fuel, show profit per compost/hour/day and time
  per compost
+ **Composter Upgrades Overlay** - Show an overview of all composter stats, including time till organic matter and fuel
  is empty when fully filled and show a preview how these stats change when hovering over an upgrade
+ Hide crop money display, crop milestone display and garden visitor list while inside anita show, SkyMart or the
  composter inventory
+ Hide chat messages from the visitors in the garden. (Except Beth, Maeve and Spaceman)
+ Show the average crop milestone in the crop milestone inventory.
+ **FF for Contest** - Show the minimum needed Farming Fortune for reaching a medal in the Jacob's Farming Contest
  inventory.
+ **yaw and pitch display**
    + Shows yaw and pitch with customizable precision while holding a farm tool.
      Automatically fades out if no movement for a customizable duration (Contributed by Sefer)
+ Warning when 6th visitors is ready (Contributed by CalMWolfs)
+ **Contest Time Needed** - Show the time and missing FF for every crop inside Jacob's Farming Contest inventory.
+ **Garden Start Location**
    + Show the start waypoint for your farm with the currently holding tool.
    + Auto-detects the start of the farm when farming for the first time
    + Option to manually set the waypoint with /shcropstartlocation
+ Crop last farmed waypoint. - appable (https://github.com/hannibal002/SkyHanni/pull/1335)
    + Accessible in the Crop Start Locations section.
+ Jacob Contest Stats Summary
    + Showing Blocks per Second and total Blocks clicked after a farming contest in chat
+ **Contest Time Needed**
    + Show the time and missing FF for every crop inside Jacob's Farming Contest inventory
+ **Garden Crop Start Location**
    + Show the start waypoint for your farm with the currently holding tool.
    + Auto-detects the start of the farm when farming for the first time
    + Option to manually set the waypoint with `/shcropstartlocation`
+ **Farming Fortune Breakdown** for Armor and Equipment (Contributed by CalMWolfs)
    + Run /ff to open the menu
    + Works with: Base Stats, Reforge Bonus, Ability Fortune and Green Thumb
    + Breakdown for the true farming fortune from each crop
    + Ability to select a single piece of armor or equipment
+ **Garden Plot Icon** (Contributed by HiZe)
    + Select an item from the inventory to replace the icon in the Configure Plots inventory
    + Change the Edit mode in the bottom right corner in the Configure Plots inventory
+ Show a warning when finding a **visitor with a rare reward**
    + Show message in chat, over the visitor and prevents refusing
+ **Refusal Bypass Key** - HiZe
    + Hold a custom key to bypass the Prevent Refusing feature for visitors
+ **Farming Weight ETA Goal** - Kaeso
    + Override the Overtake ETA to show when you will reach the specified rank
    + If not there yet
    + Default: #10k
+ Sync Jacob Contests - Kaeso + CalMWolfs
    + No need to open the calendar every SkyBlock year again.
    + Grab Jacob Contest data from the elitebot.dev website.
    + Option to send local contest data to elitebot.dev at the start of the new SkyBlock year.
+ **Visual garden plot borders** - VixidDev
    + Press F3 + G to enable/disable the view.
+ /shmouselock command to lock mouse rotation for farming. - Cad
+ Highlight Visitors in SkyBlock. - nea
    + Highlights Visitors outside the Garden.
+ Block Interacting with Visitors. - nea
    + Blocks you from interacting with / unlocking Visitors to allow for Dedication Cycling.
+ Wrong crop milestone step detection. - hannibal2
    + When opening the crop milestone menu, a chat message is sent if Hypixel's crops per milestone level data is
      different from SkyHanni's.
    + You can use this to share your hypixel data with SkyHanni via the discord.
    + This will allow us to fix the crop milestone features quicker, as we currently do not have accurate data for this.
    + If you don't want to share anything, you can disable the chat message in the config with /sh copy milestone data.
+ Garden Vacuum Pests in Pest bag to item number as stack size. - hannibal2
    + Enable via /sh vacuum.
+ Show sack item amount to the visitor shopping list. - CalMWolfs

### Garden Pests

+ Garden Vacuum Pests in Pest bag to item number as stack size. - hannibal2
    + Enable via /sh vacuum.
+ Pests in Damage Indicator. - hannibal2
    + Enable Damage Indicator and select Garden Pests.
+ Change how the pest spawn chat message should be formatted. - hannibal2
    + Unchanged, compact or hide the message entirely.
+ Show a Title when a pest spawns. - hannibal2
+ Show the time since the last pest spawned in your garden. - hannibal2
    + Option to only show the time while holding vacuum in the hand.
+ Show the pests that are attracted when changing the selected material of the Sprayonator. - hannibal2
+ Garden only commands /home, /barn and /tp, and hotkeys. - hannibal2
+ Showing a better plot name in the scoreboard. Updates faster and doesn't hide when pests are spawned. - hannibal2
+ Show a display with all known pest locations. - hannibal2
    + Click to warp to the plot.
    + Option to only show the time while holding vacuum in the hand.
+ Mark the plots with pests on them in the world. - hannibal2
+ Press the key to warp to the nearest plot with pests on it. - hannibal2
+ Draw plot borders when holding the Sprayonator. - HiZe
+ Spray Display and Spray Expiration Notice. - appable
    + Show the active spray and duration for your current plot.
    + Show a notification in chat when a spray runs out in any plot. Only active in the Garden.
+ Atmospheric Filter Display. - Erymanthus
    + This display shows the currently active buff as a GUI element.
    + For an optimal experience, please have the Atmospheric Filter accessory active.
+ Sensitivity Reducer. - martimavocado
    + Lowers mouse sensitivity while in the garden.
    + Either when pressing a keybind or holding a farming tool in hand.
    + Changes by how much the sensitivity is lowered by.
    + Show a GUI element while the feature is enabled.
    + Option to only allow this feature while on ground and/or on barn plot.
+ Lane Switch Notification - ILike2WatchMemes
    + Sends a notification when approaching the end of a lane in Garden while farming.
    + Displays the distance until the end of a lane.
+ Made Rancher's Boots the stack size display account for the Cactus Knife now giving +100 speed cap while in the Garden. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1149)
    + Speed cap above 500 will now display as red because Hypixel now allows this for some reason, but it is practically unachievable. Also, the 1000 speed cap will now show up as 1k, so the text doesn't overflow into the slot to the left.
+ Plot Menu Highlighting - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/1181)
    + Plot highlighting based on plot statuses (pests, active spray, current plot, locked plot)
+ Pest Waypoint. - Empa + hannibal2 + Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1268)
    + Show a waypoint of the next pest when using a vacuum. Only points to the center of the plot the pest is in, if too far away.
    + Uses the particles and math to detect the location from everywhere in the garden.
    + Option to draw a line to waypoint.
    + Option to change the number of seconds until the waypoint will disappear.
+ Pest Profit Tracker. - Empa (https://github.com/hannibal002/SkyHanni/pull/1321)
+ Open On Elite. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1185)
    + Allow opening farming contest stats on elitebot.dev by pressing a keybind + mouse click onto a contest item.
    + Works inside the menus Jacob's Farming Contest, Your Contests, and SkyBlock Calendar.
+ Visitor's Logbook Stats. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1287)
    + Show all your visited/accepted/denied visitors stats in a display.
+ Stereo Harmony Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1324)
    + Options to show/hide boosted crop and pest icons.
+ Added Super Craft button to visitors for ease of access. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1173)
    + Checks if you have enough materials to craft the items and depending on that shows the button or not.
+ Overflow Garden crop milestones. - Luna & HiZe (https://github.com/hannibal002/SkyHanni/pull/997)
+ New "Craftable!" message when Visitor Items Needed are craftable. - Paloys (https://github.com/hannibal002/SkyHanni/pull/1891)
+ Added toggle for compacting Garden visitor summary messages. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2026)
+ Added Farming Personal Best FF Gain. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2332)
    + Displays in chat how much additional FF you earn from the farming contest personal best bonus after beating your previous record.
+ Added No Pests Chat Filter. - saga (https://github.com/hannibal002/SkyHanni/pull/1957)
    + Removed the chat message "There are no Pests on your Garden!".
+ Added No Pests Title. - saga (https://github.com/hannibal002/SkyHanni/pull/1957)
    + Shows a title when you use the Pest Tracker without any pests to clear.

</details>
<details open><summary>

## The Rift

</summary>

+ **Rift Timer**
    + Show the remaining rift time, max time, percentage, and extra time changes.
+ **Highlight Guide**
    + Highlight things to do in the Rift Guide.
+ **Shy Warning** (Contributed by CalMWolfs)
    + Shows a warning when a shy will steal your time.
    + Useful if you play without volume.
+ **Larvas Highlighter**
    + Highlight larvas on trees in Wyld Woods while holding a Larva Hook in the hand
    + Customize the color
+ **Odonatas Highlighter**
    + Highlight the small Odonatas flying around the trees while holding an Empty Odonata Bottle in the hand.
    + Customize the color
+ **Agaricus Cap** countdown
    + Counts down the time until Agaricus Cap (Mushroom) changes color from brown to red and is breakable.
+ **Volt Crux Warning** (Contributed by nea)
    + Shows a warning while a volt is discharging lightning
    + Shows the area in which a Volt might strike lightning
    + Change the color of the area
    + Change the color of the volt enemy depending on their mood (default disabled)
+ **Enigma Soul Waypoints** (Contributed by CalMWolfs)
    + Click on the soul name inside Rift Guide to show/hide
+ **Kloon Hacking** (Contributed by CalMWolfs)
    + Highlights the correct button to click in the hacking inventory
    + Tells you which color to pick
    + While wearing the helmet, waypoints will appear at each terminal location
    + Hide already completed kloon terminal waypoints
+ **Crux Talisman Progress** Display - HiZe
    + Show bonuses you get from the talisman
+ **Laser Parkour** Solver - CalMWolfs
    + Highlights the location of the invisible blocks in the Mirrorverse
+ Mirrorverse **Dance Room Helper** - HiZe
    + Helps to solve the dance room in the Mirrorverse by showing multiple tasks at once.
    + Change how many tasks you should see
    + Hide other players inside the dance room
    + Timer before next instruction
    + Option to hide default title (instructions, "Keep it up!" and "It's happening!")
    + Fully customize the description for now, next and later (with color)
+ **Upside Down** Parkour & **Lava Maze** - hannibal2
    + Helps to solve the Upside Down Parkour and Lava Maze in the Mirrorverse by showing the correct way
    + Change how many platforms should be shown in front of you
    + Rainbow color (optional) - nea
    + Hide other players while doing the parkour
    + Outlines the top edge of the platforms (for Upside Down Parkour only) - CalMWolfs
+ Show the Motes NPC price in the item lore
    + With Burgers multiplier - HiZe
+ Living Metal Suit Progress - HiZe
    + Display progress Living Metal Suit (Default disabled)
    + Option to show a compacted version of the overlay when the set is maxed
+ Highlight for Blobbercysts in Bacte fight in colosseum in rift - HiZe
+ Show a line between **Defense blocks** and the mob and highlight the blocks - hannibal2
    + Hide particles around Defense Blocks
+ Show a moving animation between **Living Metal** and the next block - hannibal2
    + Hide Living Metal particles
+ Highlight **flying Motes Orbs** - hannibal2
    + Hide normal motes orbs particles
+ Hide Not Rift-transferable items in Rift Transfer Chest as part of the hide not clickable items feature
+ Npc motes sell value for current opened chest - HiZe
+ Show locations of inactive **Blood Effigy**
    + Show effigies that are about to respawn
    + Show effigies without known time
+ **Wilted Berberis** Helper
    + Option to only show the helper while standing on Farmland blocks
    + Option to hide the wilted berberis particles
+ **Horsezooka Hider**
    + Hide horses while holding the Horsezooka in the hand.
+ Vermin Tracker. - walker
+ Vermin Highlight. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1457)
    + Highlights Vermin mobs in the West Village in the Rift.
    + Change Highlight color.
+ Punchcard Artifact Highlighter & Overlay. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1089)
    + Highlights unpunched players in the Rift.
+ Added Splatter Hearts Highlight for the Rift. - Empa (https://github.com/hannibal002/SkyHanni/pull/2318)
+ Added Motes per Session. - Empa (https://github.com/hannibal002/SkyHanni/pull/2323)
+ Added Crafting Room Helper. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2178)
    + Shows a holographic mob at the location where the mob is present in the real room inside the Mirrorverse in Rift.
+ Added Rift Time Real-Time Nametag Format. - Empa (https://github.com/hannibal002/SkyHanni/pull/2015)
+ Added a helper for tracking the Buttons Enigma Soul in the Rift. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2616)
    + Using pathfinding to guide the player to the nearest spot with unpressed buttons and highlights them.
+ Added a route helper for Gunther's Rift Race in the West Village. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2616)
+ Added the ability to mute Wilted Berberis sounds when not farming. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2616)

</details>
<details open><summary>

## Mining

</summary>

+ **Highlight Commission Mobs** - hannibal2
    + Highlight Mobs that are part of active commissions
+ Show the names of the **4 areas** while in the center of **crystal Hollows**.
+ **Powder Grinding Tracker** - HiZe
    + Shows the Mithril/Gemstone Powder gained, the number of chests opened, if Double Powder is active, and the items
      collected.
    + Change between current session and total (open the inventory and click on Display Mode).
    + Fully customizable: change what items or stats to show.
    + Has support for the maxed Great Explorer perk.
    + Option to hide while not grinding powder.
    + Added Compacted Hard Stone to Powder Tracker. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2138)
+ Deep Caverns Parkour. - hannibal2
    + Shows a parkour to the bottom of Deep Caverns and to Rhys.
+ Display upcoming mining events. - CalMWolfs
    + Show what mining events are currently occurring in both the Dwarven Mines and Crystal Hollows.
+ Area Walls. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1266)
    + Show walls between the main areas of the Crystal Hollows.
    + Option to show the walls also when inside the Nucleus.
+ Fossil Excavator Solver. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1427)
    + Shows where to next click for optimal chance of solving the fossil. If there is a fossil this will find it within 18 moves.
+ Hides tooltips of items inside the Fossil Excavator in Glacite Tunnels. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2539)
+ Excavation Profit Tracker. - hannibal2 + Empa (https://github.com/hannibal002/SkyHanni/pull/1432)
    + Count all drops you gain while excavating in the Fossil Research Center.
    + Track Glacite Powder gained as well (no profit, but progress).
    + Track Fossil Dust and use it for profit calculation.
+ Mining Notifications. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1429)
    + Mining events, including Mineshaft spawning, Suspicious Scrap drops, and Cold going above a threshold.
+ Configurable cold amount for the ascension rope message in Glacite Mines. - nopo (https://github.com/hannibal002/SkyHanni/pull/1905)
+ Profit Per Excavation. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1439)
    + Show profit/loss in chat after each excavation.
    + Also include breakdown information on hover.
+ Textured cold overlay to Glacite Tunnels. - j10a1n15, Empa (https://github.com/hannibal002/SkyHanni/pull/1438)
    + Change at what cold level the texture should appear.
+ Glacial Powder as stack size in the Fossil Excavator. - jani270 (https://github.com/hannibal002/SkyHanni/pull/1458)
+ Highlight own Golden/Diamond Goblin. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1466)
+ Click to get an Ascension Rope from sacks in the Mineshaft. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1542)
+ Tunnel Maps. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1546)
    + Provides a way to navigate inside the new Glacite Tunnels.
+ Commissions Blocks Color. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1701)
    + Highlights blocks required for commissions in a custom color.
    + Greys out other blocks for clarity.
    + Works in the Glacite Tunnels and Crystal Hollows.
+ Profit Per Corpse. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1734)
    + Displays profit/loss in chat after looting a corpse in the Mineshaft.
    + Includes a breakdown of information on hover.
+ HOTM perks level and Enable Display. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1059)
+ Mineshaft Corpse and Entrance/Ladder Locators. - nobaboy (https://github.com/hannibal002/SkyHanni/pull/1500)
    + Includes keybind functionality for both.
+ HotM Perks Powder features. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1460)
    + Added total powder cost to HotM perks.
    + Added powder cost for 10 levels.
+ Mineshaft Pity Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1655)
    + Displays information about Mineshaft spawns, your progress towards a Pity Mineshaft, and more.
+ Added a "Get from Sack" button in the forge recipe menu to retrieve ingredients. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2106)
+ Added Tracker for Glacite Corpses. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2306)
    + Tracks overall loot and loot per type.
+ Added Precision Mining Highlighter. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2614)
    +  Draws a box over the Precision Mining particles.
+ Added highlighting boxes to Crystal Nucleus crystals during Hoppity's Hunt. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2598)
+ Added Flowstate Helper. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2561)
    +  Displays stats for the Flowstate enchantment on mining tools.

</details>
<details open><summary>

## Events

</summary>

+ Highlight Jerries during the Jerrypocalypse. - Erymanthus
+ Show waypoints for Baskets of the Halloween Event in the main Hypixel lobby. - Erymanthus
    + Thanks Tobbbb for the coordinates!
    + Support for hiding basket waypoints once you have clicked on them. - hannibal2
    + Option to show only the closest basket. - hannibal2
+ Help with the 2023 Halloween visitor challenge (Ephemeral Gratitude) - nea
    + New Visitor Ping: Pings you when you are less than 10 seconds away from getting a new visitor.
    + Accept Hotkey: Accept a visitor when you press this keybind while in the visitor GUI.
+ Support for showing the primal fear data from tab list as GUI elements. - Erymanthus
+ Play warning sound when the next Primal Fear can spawn. - Thunderblade73
+ Unique Gifting Opportunities. - nea
    + Highlight players who you haven't given gifts to yet.
    + Only highlight ungifted players while holding a gift.
    + Make use of armor stands to stop highlighting players. This is a bit inaccurate, but it can help with people you
      gifted before this feature was used.
+ Unique Gifted users counter. - hannibal2
    + Show in a display how many unique players you have given gifts to in the winter 2023 event.
    + Run command /opengenerowmenu to sync up.
+ Waypoints for 2023 Lobby Presents. - walker
+ Jyrre Timer for Bottle of Jyrre. - walker
    + A timer showing the remaining duration of your intelligence boost.
    + Option to show the timer when inactive rather than removing it.
+ Option to show time held in lore for Jyrre Bottles and Cacao Truffles. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1916)
+ New Year Cake Reminder. - hannibal2
+ Easter Egg Hunt 2024 waypoints. - Erymanthus + walker (https://github.com/hannibal002/SkyHanni/pull/1193)

### Hoppity and Chocolate Factory

+ Hoppity rabbit collection stats summary. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1482)
+ Stuff for Chocolate Factory. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1434)
    + Show info about your chocolate factory.
    + Show which upgrades you can afford and which to buy.
    + Notification to click on rabbit in the inventory.
    + Notify you if you are close to having your rabbits crushed.
+ Chocolate Factory Menu Shortcut (Hoppity Menu Shortcut). - raven + martimavocado (https://github.com/hannibal002/SkyHanni/pull/1583)
+ Tooltip Move. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1581)
    + Moves the tooltip away from the item you hover over while inside the Chocolate Factory.
+ Chocolate Factory Compact On Click. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1579)
+ Factory Chat Filters. - RobotHanzo (https://github.com/hannibal002/SkyHanni/pull/1574)
    + Hide chocolate factory upgrade and employee promotion messages.
+ Copy Chocolate Factory Stats to clipboard. - seraid (https://github.com/hannibal002/SkyHanni/pull/1517)
+ Highlight non-purchased items in Hoppity shop. - seraid (https://github.com/hannibal002/SkyHanni/pull/1517)
+ Added time tower status to the chocolate factory stats. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1506)
    + Also can notify you when you get a new charge or your charges are full.
+ Extra tooltip stats about upgrades for the chocolate factory. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1594)
    + View these to know when to buy time tower or Coach Jackrabbit.
+ Chocolate Leaderboard Change. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1602)
    + Show the change of your chocolate leaderboard over time in chat.
    + This updates every time you first open the /cf menu on a new server.
+ Chocolate Shop Price. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1601)
    + Show chocolate to coin prices inside the Chocolate Shop inventory.
+ Keybinds for Chocolate Factory. - seraid (https://github.com/hannibal002/SkyHanni/pull/1644)
+ Warning when Chocolate Factory upgrade is available. - seraid (https://github.com/hannibal002/SkyHanni/pull/1642)
+ Amount of chocolate until next prestige to stats display. - seraid (https://github.com/hannibal002/SkyHanni/pull/1638)
+ Ability to adjust the opacity of players near shared and guessed egg waypoints. - RobotHanzo (https://github.com/hannibal002/SkyHanni/pull/1582)
+ Time until the next Hoppity event in chat message for egg locator. - seraid (https://github.com/hannibal002/SkyHanni/pull/1625)
+ Warning before the Time Tower in the Chocolate Factory ends. - seraid (https://github.com/hannibal002/SkyHanni/pull/1816)
+ Chat reminder to check Hoppity Shop each year. - appable (https://github.com/hannibal002/SkyHanni/pull/1843)
+ Highlight rabbits that have requirements in the Hoppity's Collection menu. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1874)
    + Green: Requirement met.
    + Red: Requirement not met.
+ Mark duplicate egg locations in red for unlocking some of the new rabbits. - appable (https://github.com/hannibal002/SkyHanni/pull/1929)
    + Option to always mark nearby duplicate egg locations.
+ Mark duplicate egg locations in red for unlocking some of the new rabbits. - appable (https://github.com/hannibal002/SkyHanni/pull/1929)
    + Option to always mark nearby duplicate egg locations.
+ Show total amount of chocolate spent in the Chocolate Shop. - sayomaki (https://github.com/hannibal002/SkyHanni/pull/1921)
+ Stray/golden chocolate rabbit production time. - sayomaki (https://github.com/hannibal002/SkyHanni/pull/1978)
+ Display missing egg collection locations. - nea (https://github.com/hannibal002/SkyHanni/pull/1997)
    + Tracks the unique egg location requirements for rabbits from your Hoppity collection.
    + This data is partially enriched using the duplicate egg location tracker.
+ Rabbit Pet Warning. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2087)
    + Warns when using the Egglocator without having a Mythic Rabbit Pet selected.
+ Flash your screen when a special stray rabbit appears. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2040)
    + Option to also flash on normal stray rabbits.
+ Added an option to block Chocolate Factory access without a mythic rabbit pet. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2124)
+ Add reminder that the Time Tower is expired and has extra charges available. - appable (https://github.com/hannibal002/SkyHanni/pull/2133)
    + Replaces the previous Time Tower Reminder feature.
+ Added a tracker for stray rabbits caught in the Chocolate Factory. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2210)
+ Added Hoppity Hunt event summary. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2311)
    + Use /shhoppitystats for live stats.
+ Added optional warning when Hoppity calls you with a rabbit to sell. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2272)
+ Added hotkey for picking up Abiphone calls from Hoppity. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2272)
+ Added a draggable list for Hoppity's Collection menu rabbit highlighting. - the1divider (https://github.com/hannibal002/SkyHanni/pull/2438)
    + Factory/Shop milestones = Yellow/Gold.
    + Stray rabbits = Dark Aqua.
    + Abi = Dark Green.
+ Added a toggle to highlight found rabbits in Hoppity's Collection menu. - the1divider (https://github.com/hannibal002/SkyHanni/pull/2438)
+ Added the ability to change the color of missing rabbit dyes in Hoppity's Collection to reflect rabbit rarity. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2522)
+ Added an easier way to check your unique Hoppity Eggs collected on each island. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2625)
    +  Shows your progress in the Warp Menu.
    +  Can be automatically hidden when an island is complete.
+ Added the ability to block opening the Chocolate Factory when Booster Cookie is inactive. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2713)
+ Added a feature to block opening Hoppity's trade menu from Abiphone calls if you do not have coins in your purse. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2664)
+ Added the ability to prevent closing Meal Eggs that have Rabbit the Fish inside. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2712)
+ Added the ability to display the Hoppity Event Card in real-time within a GUI element. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2749)
    +  This replaces the command `/shhoppitystats` (but it can be used to view past events).
    +  Can be configured to toggle visibility with a keybind.

### The Carnival

+ Added a Reminder to claim Carnival Tickets. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2498)
+ Added display for Carnival Goals. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2498)
+ Added a feature to double-click the Carnival NPC to start the game without clicking in the chat. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2498)
+ Added Zombie Shootout QoL improvements for the Carnival. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2497)
    + Colored hitboxes.
    + Lamp timer + line.

### The Great Spook

+ Added chat solvers for Primal Fears. - Helium9 (https://github.com/hannibal002/SkyHanni/pull/2771)
    +  Added solvers for Math and Public Speaking Primal Fears.

</details>
<details open><summary>

## Commands

</summary>

+ **/shcommands**
    + Show all commands in SkyHanni
+ **/wiki <search term>** - using hypixel-skyblock.fandom.com instead of Hypixel wiki.
+ **/shmarkplayer <player>** - marking a player with yellow color.
+ **/shtrackcollection <item>** - This tracks the number of items you collect, but it does not work with sacks.
+ **/shcropspeedmeter** - Helps calculate the real farming fortune with the formula crops broken per block.
+ **/shcroptime <amount> <item>** Displays the estimated time it will take to gather the requested quantity of a
  particular item based on the current crop speed.
+ **/shcropsin <time> <item>**. - DylanBruner
    + Shows the number of items you gain when farming in the garden for the given time.
+ `/pt <player>` as alias for `/party transfer <player>`
    + SkyBlock Command `/tp` to check the play time still works
+ **/shfarmingprofile [player name]**
    + Opens the elitebot.dev website in your web browser to show your Farming Weight profile.
+ Tab Complete support to sacks command /gfs and /getfromsacks. - J10a1n15
+ /shcalccrop. - CalMWolfs
    + Calculate how many crops need to be farmed between different crop milestones.
+ /shcalccroptime. - CalMWolfs
    + Calculate how long you need to farm crops between different crop milestones.
+ /shupdate command. - Empa (https://github.com/hannibal002/SkyHanni/pull/1578)
    + Can be used like `/shupdate <beta/full>` to download updates from a specific update stream.
+ /shignore. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1469)
    + This lets you block users from running party commands.
+ Added `/shcolors` command. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2216)
    + Prints a list of all Minecraft color and formatting codes in chat.
+ Added Remind command. - ThatGravyBoat & Zickles (https://github.com/hannibal002/SkyHanni/pull/1708)
    + Use `/shremind <time> <note>` to set reminders for your future self.
+ Added /shtps command. - saga (https://github.com/hannibal002/SkyHanni/pull/1961)
    + Informs in chat about the server's ticks per second (TPS).
+ Added command `/shedittracker <item name> <amount>`. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2448)
    + Changes the tracked item amount for Diana, Fishing, Pest, Excavator, and Slayer Item Trackers.
    + Use a negative amount to remove items.

</details>
<details open><summary>

## Stranded

</summary>

+ Highlights NPCs in the stranded menu that are placeable but haven't been placed. - walker

</details>
<details open><summary>

## Misc

</summary>

+ Allow to paste text from clipboard in signs
+ Pet Display (showing the currently selected pet as GUI element, without any fancy XP or level or percentage, but with
  auto-pet support)
+ Hiding exp Bottles lying on the ground.
+ **Real Time**
    + Display the current computer time, a handy feature when playing in full-screen mode.
+ Highlight the voidling extremist in pink color.
+ Highlight millenia aged blaze color in red
+ Option to hide all damage splashes, from anywhere in SkyBlock.
+ Hide armor or just helmet of other player or yourself
+ Display the active non-god potion effects.
+ Added warning when Non-God Pot effects are about to expire. - saga (https://github.com/hannibal002/SkyHanni/pull/2004)
    + Sends a title and plays a sound at a customizable time before the effect expires.
+ Wishing compass uses amount display.
+ Brewing Stand Overlay.
+ Crimson Isle Reputation Helper.
+ Quest Item Helper. (Crimson Isle) - NetheriteMiner
    + When you open the fetch item quest in the town board, it shows a clickable chat message that will grab the items
      needed from the sacks.
+ Crimson Isle **Pablo NPC Helper**. - NetheriteMiner
    + Similar to Quest Item Helper, shows a clickable message that grabs the flower needed from sacks.
+ Dojo Rank Display. - HiZe
    + Display your rank, score, actual belt and points needed for the next belt in the Challenges inventory on the
      Crimson Isles.
+ Volcano Explosivity in Crimson Isle. - Erymanthus
    + Show a HUD of the current volcano explosivity level.
+ Sulphur Skitter Box in Crimson Isle. - HiZe
    + Renders a box around the closest sulphur block.
+ Dojo Rank Display. - HiZe
    + Display your rank, score, actual belt and points needed for the next belt in the Challenges inventory on the
      Crimson Isles.
+ Added Crimson Isle Miniboss Respawn Timer. - Empa (https://github.com/hannibal002/SkyHanni/pull/2042)
+ Matriarch Helper. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1385)
    + Highlights the Heavy Pearls.
    + Draws a line to the Heavy Pearls.
+ Red Scoreboard Numbers - Hides the red numbers in the scoreboard sidebar on the right side of the screen.
+ **Tia Relay Waypoint** - Show the next Relay waypoint for Tia The Fairy, where maintenance for the abiphone network
  needs to be done.
+ **Tia Relay Helper** - Helps with solving the sound puzzle.
+ **Hide dead entities** - Similar to Skytils' feature for inside dungeon, but for everywhere.
+ **Tps Display** - Show the Tps of the current server.
+ **Particle Hider** - Hide blaze particles, fire block particles, fireball particles, near redstone particles, far
  particles or smoke particles.
+ Chicken Head Timer.
+ **rancher boots** speed display.
+ **CH Join** - Helps buy a Pass for accessing the Crystal Hollows if needed.
+ **Estimated Item Value**
    + Displays an estimated item value for the item you hover over.
    + Works with Attributes. - nea
    + Works with Gemstone Slot unlock costs. - Fix3dll
+ **Discord RPC** - NetheriteMiner
    + Showing stats like Location, Purse, Bits, Purse or Held Item at Discord Rich Presence.
    + Show dungeon information.
        + Show the current floor name.
        + Time since the dungeon started.
        + Number of boss collections of the current boss.
    + Dynamic Priority Box.
        + Change the order or disable dynamically rendered features (e.g. Slayer, Dungeon, Crop Milestone, Stacking
          Enchantment)
    + AFK time. - NetheriteMiner
    + SkyCrypt Button. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1526)
+ Server Restart Title
+ **City Project Features**
    + Show missing items to contribute inside the inventory
        + Click on the item name to open the bazaar
    + Highlight a component in the inventory that can be contributed
    + City Project Daily Reminder - Remind every 24 hours to participate
+ **Command Autocomplete**
    + Supports tab completing for warp points when typing /warp
    + Supports party members, friends (need to visit all friend list pages), player on the same server
    + Supports these commands: /p, /party, /pt (party transfer), /f, /friend /msg, /w, /tell, /boop, /visit, /invite,
      /ah, /pv (NEU's Profile Viewer), /shmarkplayer (SkyHanni's Mark Player feature)
    + Supports VIP /visit suggestions (e.g. PortalHub or Hubportal)
+ Piece of Wizard Portal show earned by player name (Contributed by HiZe)
+ Quick Mod Menu Switching (default disabled)
    + Allows for fast navigation between one Mod Config and another
    + Default disabled
    + Detects your SkyBlock Mod automatically
    + Does detect Chat Triggers and OneConfig itself, but no single mods that require these libraries
+ **Sack Item Display** (Contributed by HiZe)
    + price display next to sack items
    + Can be disabled
    + Sortable by price or items stored (both desc/asc)
    + Option to show prices from Bazaar or NPC
+ Option to highlight items that are full in the sack inventory.
+ **Ghost Counter** (Contributed by HiZe)
    + Shows number of ghosts killed in the Mist in Dwarven Mines
    + Shows kill combo, coins per scavenger, all item drops, bestiary, magic find and more
    + Each display line is highly customizable
+ **Frozen Treasure Tracker** (Contributed by CalMWolfs)
    + Show different items collected while breaking treasures in the Glacial Cave in Jerry's Workshop
    + Show Ice per hour
    + Customizable GUI
    + Option to hide the chat messages
+ While on the Winter Island, show a timer until Jerry's Workshop closes. - hannibal2
+ Added a message to warp to Winter Island spawn when a Reindrake spawns. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2569)
+ **Custom Text Box** - CalMWolfs
    + Write fancy text into a gui element to show on your screen at all time
    + Supports color codes
    + Supports line breaks `\n` - hannibal2
+ **/sendcoords** - dragon99z
    + Sending, detecting and rendering.
+ Dungeon Potion level as item stack size - HiZe
+ Dark Cacao Truffle hours held as stack size. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1916)
+ **Ender Node Tracker** - pretz
    + Tracks items and profit obtained from collecting ender nodes and killing normal endermen.
+ **Harp Keybinds** - NetheriteMiner
    + In Melody's Harp, press buttons with your number row on the keyboard instead of clicking.
+ **Teleport Pad Compact Name**
    + Hide the 'Warp to' and 'No Destination' texts over teleport pads.
    + Only on Private island.
+ **Inventory Numbers**
    + Show the number of the teleport pads inside the 'Change Destination' inventory as stack size.
    + Only on Private island.
+ Account upgrade complete reminder. - appable0
+ Pet Experience Tooltip
    + Show the full pet exp and the progress to level 100 (ignoring rarity) when hovering over a pet while pressing
      shift key.
    + Highlight the level 100 text in gold for pets below legendary. - hannibal2
      (This is to better indicate that the pet exp bar in the item tooltip is calculating with legendary.)
    + Option to only show level 100 for golden dragon in the pet experience tooltip. - hannibal2
+ **SkyHanni Installer** - NetheriteMiner
    + Double-clicking the mod jar file will open a window that asks you where to move the mod into.
+ **Default Option Settings:** - nea
    + Enables or disables all features at once, or per category.
    + Sends a chat message on first SkyHanni startup (starting with this feature, so this version everyone will see this
      message).
    + Shows new features after an update (starting with the next beta, not this one).
    + Allows to change those settings anytime again with /shdefaultoptions.
+ Show alert when reaching max super-pairs clicks. - pretz
    + Plays a beep sound and sends a message in chat when the player reaches the maximum number of clicks gained for
      super-pairs mini games.
+ Anniversary Event Active Player Ticket Timer. - nea
    + Option to play a sound as well.
+ **Trevor Trapper** Features in Farming Islands
    + Trapper Cooldown GUI. - NetheriteMiner
        + Show the cooldown on screen in an overlay (intended for abiphone users).
    + **Trevor the Trapper Tracker**. - CalMWolfs
        + Quests done
        + A breakdown of their rarity
        + Animals killed vs. animals that kill themselves
        + Pelts per hour
    + Press the hotkey to accept the next Trevor the Trapper quest. - CalMWolfs
+ **GUI Scale**: - nea
    + Scroll within the position editor to independently adjust the GUI scale for each SkyHanni element.
    + Change the global scale of all SkyHanni elements at once (in the config under /sh scale).
    + Change the scale with plus and minus keys. - CalMWolfs
+ **Compact Tab List**.
    + Compacts the tablist to make it look much nicer (old SBA feature, but fewer bugs). - CalMWolfs
    + Option to hide Hypixel advertisement banners. - CalMWolfs
    + **Advanced Player List**. - hannibal2
        + Customize the player list (inside the tab list) in various ways.
        + Change the sort order of players: Default, SkyBlock Level, alphabetical name, Iron Man first/bingo level,
          party/friends/guild
        + Option to hide different parts of the player list: Player skins/icons, Hypixel rank color, Emblems, SkyBlock
          level
+ Kick Duration. - hannibal2
    + Show in the Hypixel lobby since when you were last kicked from SkyBlock.
    + Useful if you get blocked because of 'You were kicked while joining that server!'.
    + Send a warning and sound after a custom amount of seconds.
+ Time In Limbo. - hannibal2
    + Show the time since you entered limbo.
    + Show a chat message for how long you were in limbo once you leave it.
+ Highlight Party Members. - Cad
    + Marking party members with a bright outline to better find them in the world.
+ Porting SkyBlockAddon's **chroma** into SkyHanni with many more options and chroma everything. - VixidDev
    + Options to change speed, size, saturation and direction.
+ Modify Visual Words (command /shwords). - CalMWolfs
    + Allows you to replace text on your screen with different text (like the SBE one, just less costly).
    + Supports all color codes, even chroma (use &&Z)
+ In-Game Date display. - Erymanthus
    + Show the in-game date of SkyBlock (like in Apec, but with mild delays).
    + Includes the SkyBlock year.
+ **Dungeon party finder** QOL improvements - Cad
    + Floor stack size.
    + Mark Paid Carries red.
    + Mark Low-Class levels orange.
    + Mark groups you can't join dark red.
    + Mark groups without your current classes green.
+ Shortcuts for **Party commands** and smart **tab complete**. - CalMWolfs
    + /pw -> party warp
    + /pk -> party kick
    + /pt -> party transfer
    + /pp -> party promote
    + /pko -> party kickoffline
+ Working **Livid Finder** (should work 100% of the time). - hannibal2
    + Option to hide other/wrong/fake Livids (try this out and see if you really want this, it can be counter-productive
      in some cases).
+ Option to change Hypixel Wiki to the fandom Wiki in more areas than just the /wiki command. - Erymanthus
    + E.g. inside the SkyBlock leveling guide.
+ Fixes Ghost Entities. - hannibal2 & nea & Thunderblade73
    + Removes ghost entities caused by a Hypixel bug. This included Diana, Dungeon and Crimson Isle mobs and nametags.
+ Party Kick with reason. - nea
    + Support for the Hypixel command /p kick /pk to add a reason. The reason will be sent in party chat before
      kicking the player.
+ Color the month names on the scoreboard. - J10a1n15
+ Blocks the mining ability when on a private island. - Thunderblade73
+ Crimson Isle Volcano Geyser features. - MrFast
    + Stops the white geyser smoke particles from rendering if your bobber is near the geyser.
    + Draws a box around the effective area of the geyser.
    + Change the color of the box around the geyser.
+ Skill Progress Display. - HiZe
    + ETA Display, exp, actions or percentage to next level, custom level goals, all skill display, chroma progress bar,
      and overflow levels for all those things.
    + A ton of settings.
+ Command `/shlimbo` for easier Limbo access. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/848)
    + Limbo time tracker also now works in the Slumber Hotel in the Bed Wars Lobby.
    + A new secret method to get more SkyHanni User Luck from Limbo.
+ Added SkyHanni User Luck to the stats breakdown. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1288)
    + This can be viewed in /sbmenu, /equipment, and the Misc Stats submenu.
+ Command `/shlimbostats` for a simple way to view your Limbo stats. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/848)
    + Support for `/playtime` and  `/pt` while in Limbo.
    + Added your playtime to Hypixel's `/playtimedetailed`.
+ Option to toggle limbo time in detailed /playtime. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1863)
+ Click on breakdown display in /playtimedetailed to copy the stats into the clipboard. - seraid (https://github.com/hannibal002/SkyHanni/pull/1807)
+ Custom Scoreboard - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
    + Customizable; New, never seen before lines like the current mayor with perks, your party, and more!
    + Custom Title and Footer, align them on different sides of the scoreboard.
    + Hide the Hypixel Scoreboard, add a custom Image as a background, rounded corners.
    + Supports colored month names & better garden plot names.
    + A ton of settings.
    + Added Chunked Stats as a draggable list to the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1341)
        + Select and prioritize the order of the stats you want to see.
    + Added Soulflow to the Custom Scoreboard. - Empa (https://github.com/hannibal002/SkyHanni/pull/1837)
      + Requires Soulflow to be enabled in Hypixel settings: /tab -> Profile Widget -> Show Soulflow.
  + Added the current minister to the calendar. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2342)
  + Allowed the use of the Custom Scoreboard outside of SkyBlock, but only on Hypixel. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1881)
  + Added an option to disable custom lines in the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1881)
+ No Bits Available Warning. - Empa (https://github.com/hannibal002/SkyHanni/pull/1286)
    + Warns when you run out of available bits to generate.
+ Added Display for Bits on Cookie buy. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2265)
    + Shows the Bits you would gain.
    + Can show the change for the available Bits.
    + Also shows the time more clearly (or adds it if not present).
+ Link from HUD elements to config options. - nea (https://github.com/hannibal002/SkyHanni/pull/1383)
    + Simply right-click a HUD element in the HUD editor to jump to its associated options.
    + Does not yet work on every GUI element. Wait for the missing elements in the following betas.
+ Quiver Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1190)
    + Only shows the type of arrow when wearing a Skeleton Master Chestplate.
+ Low Quiver Reminder at the end of Dungeon/Kuudra run. - Empa (https://github.com/hannibal002/SkyHanni/pull/1190)
+ party chat commands. - !nea (https://github.com/hannibal002/SkyHanni/pull/1433)
    + Added `!pt` (and aliases) as a command that allows others to transfer the party to themselves.
    + Added `!pw` (and aliases) as a command that allows others to request a warp.
+ Option to highlight Zealots holding Chests in a different color. - Luna (https://github.com/hannibal002/SkyHanni/pull/1347)
+ Allow party members to request allinvite to be turned on. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1464)
    + Say !allinv in party chat for them to enable all invite.
+ Added Reverse Party Leader. - Jordyrat (https://github.com/hannibal002/SkyHanni/pull/1712)
    + Reverse party transfer via clickable chat message or via the `/rpt` command.
    + Configurable party chat message after transferring.
+ Added Hide Far Entities. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1064)
    + Can perhaps increase FPS for some users by 5% to 150%.
    + Options to change the distance and number of mobs to always show.
    + Option to disable in garden.
+ Inventory background to GUI editor. - seraid (https://github.com/hannibal002/SkyHanni/pull/1622)
+  Added option to hide item tooltips inside the Harp. - raven (https://github.com/hannibal002/SkyHanni/pull/1700)
+ Option to Replace Roman Numerals. - Mikecraft1224 (https://github.com/hannibal002/SkyHanni/pull/1722)
+ Simple Ferocity Display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1765)
    + Shows the Ferocity stat as a single GUI element.
    + Requires the Tab List widget to be enabled and Ferocity to be selected to work.
+ Editable Hotbar. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1903)
    + Allows for moving and scaling in the SkyHanni GUI editor.
+ ULTRA RARE Book Notification when doing the Experiment Table. - raven (https://github.com/hannibal002/SkyHanni/pull/1738)
+ Added a warning when opening the Experimentation Table without a Guardian pet. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2127)
+ Added Enchanting Experience as Stack Size in Experimentation Table. - saga (https://github.com/hannibal002/SkyHanni/pull/1988)
    + Added to the Item Number list.
+ Added Compact Experimentation Table chat rewards. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2209)
    + Uses a compact chat message of rewards gained from Add-ons/Experiments.
+ Added Superpair Display. - ILike2WatchMemes  (https://github.com/hannibal002/SkyHanni/pull/2171)
    + Displays found and matched pairs, power-ups, and missing pairs/normals.
+ Added Experiments Dry-Streak Display. - ILike2WatchMemes  (https://github.com/hannibal002/SkyHanni/pull/2171)
    + Shows attempts and XP since the last ULTRA-RARE.
+ Added Experiments Profit Tracker. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2171)
    + Tracks profits in Coins and Enchanting XP.
+ In-Water Display. - Stella (https://github.com/hannibal002/SkyHanni/pull/1892)
    + Useful when using a Prismarine Blade in Stranded Mode.
+ Added Beacon Power Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1901)
+ Added highlighting for the active Beacon Effect. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2546)
+ Added Hide Useless Armor Stands. - Empa (https://github.com/hannibal002/SkyHanni/pull/1962)
    + Hides armor stands that briefly appear on Hypixel.
+ **Custom Wardrobe**, a new look for the wardrobe. - j10an15, Empa (https://github.com/hannibal002/SkyHanni/pull/2039)
    + Highly customizable: Colors, display sizes.
    + Estimated Price Integration.
    + Favorite slots; option to only display favorite slots.
+ Added a Chat Message if the Perkpocalypse Mayor is unknown. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2009)
+ Added a source download verification checker. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1914)
    + Warns you when the mod has been downloaded from an untrusted source (not the official GitHub or Modrinth).
+ Shows a list of items that can be crafted with the items in your inventory when inside the crafting menu. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1334)
    + Click on an item to open /recipe.
    + Option to include items from inside sacks.
    + Option to include vanilla items.
+ Add Armor Stack Display. - Xupie (https://github.com/hannibal002/SkyHanni/pull/1811)
    + Display the number of stacks on armor pieces like Crimson, Terror etc.
+ Added Option to crash your game when you die, for extra suffering. - Stella (https://github.com/hannibal002/SkyHanni/pull/2238)
    + Also punishes players who don't read patch notes.
+ Added Crown of Avarice counter. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2229)
+ Added a title notification for when the Thunder Bottle is fully charged. - raven (https://github.com/hannibal002/SkyHanni/pull/2234)
+ Added Editable XP Bar. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1944)
    + Enabled moving and scaling of the XP bar in the SkyHanni GUI Editor.
+ Show dye hex code as the actual color in the item lore. - nopo (https://github.com/hannibal002/SkyHanni/pull/2321)
+ Added a notification when you join a server you have joined previously. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2046)
+ Added Personal Compactor/Deletor Overlay. - Empa (https://github.com/hannibal002/SkyHanni/pull/1869)
    + Shows what items are currently inside the personal compactor/deletor, and whether the accessory is turned on or off.
+ Added Display Tab Widgets. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1276)
    + Allows the display of information from the Tab (e.g., Bestiary info).
+ Added Accessory magical power display as stack size. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2243)
    + Only works inside the Accessory Bag and Auction House.
+ Added Area Navigation. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2468)
    + Reused the logic from the Tunnel Maps Graphs (Glacite Tunnels), now applied to other islands.
    + Displays a list of all areas on your current island. Click on a name to find a path to that area via pathfinding.
    + Find a route to the Hoppity egg. Useful for islands with small, nested corridors.
    + Get a title warning when entering an area.
    + See area names in the world.
    + Does not yet work with Spider's Den and Dungeon Hub. Only partial support for Farming Islands and the Rift.
+ Added navigation command. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2575)
    + Use `/shnavigate <name>` to find interesting locations on your current SkyBlock island by searching for them.
    + Displays a pathfinder to the location.
    + Works with NPCs, points of interest, areas, slayer spots, emissaries, crimson mini-bosses, spots to mine ores, break crops, and kill mobs.
    + Does not yet work with Spider's Den and Dungeon Hub (still needs to be mapped out).
+ Added `/gfs` to fix a broken Piggy Bank. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2150)
+ Added Broodmother spawn alert, countdown and stage change messages. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2325)
    + Countdown will not show until a spawning stage change is observed, and may be off by a few seconds.
+ Added option to hide pet nametag text. - Empa + j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1880)
+ Added Transfer Cooldown Prevention. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/1751)
    + Wait for the transfer cooldown to complete before sending commands like warp/is/hub.
+ Added Carry Tracker. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2185)
    + Use `/shcarry` to add carries to a customer, and set a price for a slayer boss.
    + Automatically counts slayer bosses you carry.
    + Automatically tracks coins received from customers via `/trade`.
+ Added Draconic Sacrifice Tracker. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2077)
    +  Tracks items and profit while using the Draconic Altar in the End.

</details>
<details open><summary>

## Cosmetics

</summary>

+ **Following Line** - hannibal2
    + Draws a colored line behind the player.
    + Change the color, width, and duration of the line.
+ **Arrow Trail cosmetic** - Thunderblade73
    + Draw a colored line behind the arrows in the air.
    + Options to change the color of the line, to only show own arrows or every arrow, to have own arrows in a different
      color, to change the time alive, and the line width.

</details>
