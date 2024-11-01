# SkyHanni - Change Log

## Version 0.28 (in Beta)

### New Features

#### Hoppity Features

+ Added an easier way to check your unique Hoppity Eggs collected on each island. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2625)
    +  Shows your progress in the Warp Menu.
    +  Can be automatically hidden when an island is complete.
+ Added the ability to block opening the Chocolate Factory when Booster Cookie is inactive. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2713)
+ Added a feature to block opening Hoppity's trade menu from Abiphone calls if you do not have coins in your purse. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2664)
+ Added the ability to prevent closing Meal Eggs that have Rabbit the Fish inside. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2712)

#### Inventory Features

+ Added Focus Mode. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2694)
    +  In Focus Mode, only the name of the item is displayed instead of the full description.
+ Added WASD keybinds to the Abiphone Snake Game. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1968)
+ Added Attribute Overlay. - Empa (https://github.com/hannibal002/SkyHanni/pull/2001)
    +  Options to highlight good rolls in different colors, show only certain attributes, etc.
+ Added helpers for Essence Shops and Carnival Event Upgrade Shops. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2423)
    +  Assists with maxing upgrades.

#### Fishing Features

+ Added Lava Replacement. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1885)
    +  Replaces the lava texture with the water texture.
    +  Primarily used for lava fishing in the Crimson Isle, but can be used anywhere else if the option is enabled.

#### Mining Features

+ Added Precision Mining Highlighter. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2614)
    +  Draws a box over the Precision Mining particles.
+ Added highlighting boxes to Crystal Nucleus crystals during Hoppity's Hunt. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2598)
+ Added Flowstate Helper. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2561)
    +  Displays stats for the Flowstate enchantment on mining tools.

#### Dungeon Features

+ Added Terminal Waypoints. - Stella (https://github.com/hannibal002/SkyHanni/pull/2719)
    +  Displays waypoints during the F7/M7 Goldor Phase.

#### Chat Features

+ Added chat compacting for 'items in stash' warnings. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2639)

#### Combat Features

+ Added Draconic Sacrifice Tracker. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2077)
    +  Tracks items and profit while using the Draconic Altar in the End.

#### Event Features

+ Added chat solvers for Primal Fears. - Helium9 (https://github.com/hannibal002/SkyHanni/pull/2771)
    +  Added solvers for Math and Public Speaking Primal Fears.

### Improvements

#### Inventory Improvements

+ Added Pocket Sack-in-a-Sack support to Estimated Item Value. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2698)
+ Added more options to Focus Mode to avoid hiding the item lore unintentionally. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2844)
    +  Even when enabled in config, focus mode is now inactive on game start and needs to get enabled via toggle mode.
    +  Show a hint in the item lore how to enable/disable focus mode (with a config option to hide this hint).
    +  Option to enable focus mode all the time, ignoring the keybind.

#### Chat and Command Improvements

+ Replaced repeated SkyHanni messages with the previous message. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2700)
+ Added support for Guilds in player-related tab completions. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2637)
+ Added support for all Guild and Friend commands in tab completions. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2637)
+ Reordered commands in categories. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2642)
+ Renamed some commands for clarity. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2642)

#### Combat Improvements

+ Added Totem of Corruption and Enrager to the Ability Cooldown feature. - DungeonHub (https://github.com/hannibal002/SkyHanni/pull/2706)
+ Improved Flare Display. - DungeonHub (https://github.com/hannibal002/SkyHanni/pull/2705)
    +  Added Flare Expiration Sound.
    +  Added Flare Expiration Flash Warning.
    +  Added a setting to adjust the expiration warning time.
+ Improved Slayer Miniboss features. - Empa (https://github.com/hannibal002/SkyHanni/pull/2081)

#### Mining Improvements

+ Made the "You need a stronger tool to mine ..." chat filter hide every such message, not just Crystal Hollows gemstones. - Luna (https://github.com/hannibal002/SkyHanni/pull/2724)
+ Added an option to draw a line to your golden or diamond goblin. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2717)

#### Diana Improvements

+ Added support for detecting and handling Inquisitor spawn messages from other mods from chat. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2720)

#### Fishing Improvements

+ Added an option to always display the Barn Fishing Timer anywhere. - NeoNyaa (https://github.com/hannibal002/SkyHanni/pull/2735)

#### Hoppity Improvements

+ Improved the Time Tower Usage Warning so it doesn't spam messages. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2730)

#### Great Spook Improvements

+ Highlight Great Spook's answer in blue. - not_a_cow (https://github.com/hannibal002/SkyHanni/pull/2798)

#### Custom Scoreboard Improvements

+ Added the date to the Custom Scoreboard Lobby code. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2802)
+ Added an option to display the profile type instead of the name in the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2810)
+ Improved performance when checking the Scoreboard. - Empa (https://github.com/hannibal002/SkyHanni/pull/2765)

#### Misc Improvements

+ Added distance display to waypoints created by Patcher's Send Coords feature. - jani (https://github.com/hannibal002/SkyHanni/pull/2704)
+ Made multiple improvements to the Custom Scoreboard. - Empa, j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2162)
    +  Added an option to align the text.
    +  Improved overall performance.
    +  Added the Party Leader to the Party Element.
    +  Separated title and footer alignment.
    +  Added a custom alpha footer.

### Fixes

#### Dungeon Fixes

+ Fixed Magical Power resetting to 0 when opening "Your Bags" in the Catacombs. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2710)
+ Fixed a rare case where invisible Fels were highlighted even though they shouldn't. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2746)

#### Fishing Fixes

+ Fixed fishing displays showing in dungeons. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2697)

#### Inventory Fixes

+ Fixed "No Guardian Pet warning" not supporting Pet Rule "On open Experimentation Table". - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2699)
+ Fixed an error with compact experiment rewards chat messages. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2702)
+ Fixed Craft Materials Bazaar not working with long item names. - Fazfoxy (https://github.com/hannibal002/SkyHanni/pull/2703)
+ Fixed the debug feature that allows you to add/remove stars being enabled by default. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2715)
+ Fixed displaying the Guardian warning text in the Experimentation Table even when using a Guardian Pet. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2718)
+ Fixed locked Ultimate enchantments being hidden by Enchant Parsing. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2732)
+ Fixed Chest Value Display on Carpentry Chests. - fazfoxy (https://github.com/hannibal002/SkyHanni/pull/2743)
+ Fixed Compact Item Stars. - Empa, Fazfoxy (https://github.com/hannibal002/SkyHanni/pull/2741)
+ Fixed an Estimated Item Value issue where +10 stars were accidentally added to certain items and unstarred items were not showing stars. - Fazfoxy and Empa (https://github.com/hannibal002/SkyHanni/pull/2758)
+ Fixed an issue with Estimated Item Value erroring when multiple mods affect the same item. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2787)
+ Fixed "Dungeon Potion level as stack size" not working in shop menus. - phoebe (https://github.com/hannibal002/SkyHanni/pull/2825)
+ Fixed being unable to use the "Close" button when "Change all clicks to shift clicks in brewing stands" is enabled. - phoebe (https://github.com/hannibal002/SkyHanni/pull/2824)
+ Fixed a rare error message when using Experimentation Table features. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2833)

#### Combat Fixes

+ Fixed Ghost Counter display appearing while in incorrect areas on the map. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2696)
+ Fixed Ashfang Blazes sometimes being highlighted with the wrong color. - Empa (https://github.com/hannibal002/SkyHanni/pull/2112)
+ Fixed Ashfang Reset Cooldown counting in the wrong direction. - Empa (https://github.com/hannibal002/SkyHanni/pull/2112)
+ Fixed Millennia-Aged Blaze not being highlighted by the Area Boss Highlight feature. - jani (https://github.com/hannibal002/SkyHanni/pull/2707)
+ Fixed a small typo in Bestiary Display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2748)
+ Fixed `Line to Slayer Miniboss` not checking for walls. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2788)

#### Custom Scoreboard Fixes

+ Fixed Custom Scoreboard not showing the Second Barbarian Quest. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2709)
+ Fixed Custom Scoreboard duplicating the Party Leader. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2780)
+ Fixed some Custom Scoreboard errors. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2779)
+ Fixed party leader not displaying correctly in the Custom Scoreboard. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2783)
+ Fixed the Broodmother line in the Custom Scoreboard having a leading space. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2810)

#### Hoppity Fixes

+ Fixed the chocolate egg share message sometimes displaying the wrong location name. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2711)
+ Fixed El Dorado not receiving a compacted chat message. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2742)
+ Fixed issues with El Dorado stray detection. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2786)
+ Fixed an error when Rabbit the Fish was found in Meal Eggs. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2785)
+ Fixed dyes being incorrectly modified in Hoppity's Collection after disabling "Re-Color Missing Rabbit Dyes". - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2803)

#### Garden Fixes

+ Fixed farming weight not disappearing when the config option is off. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2731)
+ Fixed New Visitor Ping triggering too late if the player is actively farming. - Luna (https://github.com/hannibal002/SkyHanni/pull/2767)
+ Fixed the mouse not unlocking when teleporting to the Barn. - not_a_cow (https://github.com/hannibal002/SkyHanni/pull/2799)
+ Fixed API error when sending Jacob Contests. - Ke5o (https://github.com/hannibal002/SkyHanni/pull/2819)

#### Crimson Isle Fixes

+ Fixed Replace Lava not working with OptiFine. - CalMWolfs + nopo (https://github.com/hannibal002/SkyHanni/pull/2727)

#### Diana Fixes

+ Fixed Griffin Pet Warning not supporting Diana Autopet rules. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2722)

#### Commands Fixes

+ Fixed /shtranslate not working in most cases. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/2693)
+ Fixed tab completions for /p [IGN]. - appable (https://github.com/hannibal002/SkyHanni/pull/2769)

#### Mining Fixes

+ Fixed a crash when attempting to edit the Flowstate Helper config. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2740)

#### Chocolate Factory Fixes

+ Fixed an issue where the Time Tower Usage Warning would notify you after expiration or when you have 0 charges. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2751)
+ Fixed Choc Factory screen flashing not disappearing in a timely manner after clicking some strays. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2782)

#### Event Fixes

+ Fixed Fear Stat Display. - Fazfoxy (https://github.com/hannibal002/SkyHanni/pull/2766)
+ Fixed Carnival Goal display rarely showing outside the Hub Island. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2772)
+ Fixed the Great Spook features. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2804)
+ Fixed Primal Fear Notify incorrectly notifying when a Primal Fear is not ready to spawn. - Luna (https://github.com/hannibal002/SkyHanni/pull/2831)
+ Fixed typos in Primal Fear Solver. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/2834)
+ Fixed issues where the Carnival Ticket claim feature would trigger even when a carnival isn't active. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2838)

#### Rift Fixes

+ Fixed Mirrorverse features sometimes not working. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2773)
+ Fixed incorrect health format for holographic mobs in Rift's Crafting Room. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2846)

#### Chat Fixes

+ Fixed item stash messages not being compacted correctly. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2781)
+ Fixed own player messages not being reformatted by chat formatting. - !nea (https://github.com/hannibal002/SkyHanni/pull/2806)
    +  Also fixed ranks losing their "+" colors.
+ Fixed compact stash messages. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/2821)
    +  Fixed compact item stash messages not being detected correctly.
    +  Fixed the color of material messages.

#### Misc Fixes

+ Fixed SkyHanni messages being sent twice. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2736)
+ Fixed the formatting of negative durations. - Empa (https://github.com/hannibal002/SkyHanni/pull/2726)
+ Fixed debug messages not sending when debug mode is enabled. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2793)
+ Fixed TPS display not working outside Skyblock. - Empa (https://github.com/hannibal002/SkyHanni/pull/2791)
+ Fixed the "Colored Class Level" tab list displaying "null". - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2814)
+ Fixed kick duration not showing with some kick messages. - Luna (https://github.com/hannibal002/SkyHanni/pull/2837)

### Technical Details

+ Assigned 'backend' label to PRs with 'backend' in the title. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2690)
    +  This does not change the functionality of the current bug fix label.
+ Added SuggestionProvider. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2637)
    +  This new class simplifies building suggestion trees.
+ Added Stats API. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2253)
+ Cleaned up LorenzVec and added drawLineToEye. - Empa (https://github.com/hannibal002/SkyHanni/pull/2056)
+ Added SkyHanni event inheritance. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2047)
+ Added /shtranslateadvanced command. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/2693)
    +  Allows specifying both the source and target language.
+ Added changelog verification. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2692)
    +  This action ensures your PR is in the correct format so that it can be used by the release notes tool.
+ Added dungeon phase detection. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1865)
+ Added EntityLeaveWorldEvent. - Empa (https://github.com/hannibal002/SkyHanni/pull/2112)
+ Made command registration event-based. - j10a1n15, ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2642)
+ Added "line to the mob" handler. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2717)
+ No longer use NEU GUI elements for the auto-update button. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2725)
+ Added SkyHanni notifications. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2630)
+ Moved Hoppity Warp Menu config to `HoppityWarpMenuConfig`. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2712)
+ Migrated some LorenzEvents without parameters to SkyHanniEvents. - Empa (https://github.com/hannibal002/SkyHanni/pull/2744)
+ Changed SkyHanniEvents without parameters to be objects. - Empa (https://github.com/hannibal002/SkyHanni/pull/2744)
+ Added a custom import ordering Detekt rule. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2775)
+ Added a method to retrieve a readable dump of an item's NBT tag(s). - Daveed (https://github.com/hannibal002/SkyHanni/pull/2787)
+ Added MultiMC to the list of known launchers. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2784)
+ Added informative comments on PRs when failures are detected. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2790)
+ Added in-file annotations when 'Detekt' failures occur. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2790)
+ Added some preprocessing mappings. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2776)
+ Added Shot support to the multi-version build. - !nea (https://github.com/hannibal002/SkyHanni/pull/2800)
    +  This allows adding nullability annotations (and other simple annotations) to vanilla code.
+ Added an event handler check to SkyHanni Events. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2755)
+ Refactored and optimized the `NEUInternalName` class. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2841)
    +  Added caching for `fromItemNameOrNull` by introducing `itemNameCache` to improve efficiency on repeated calls.
    +  Renamed `asInternalName` to `toInternalName` for readability and consistency with Kotlin naming, marking `asInternalName` as deprecated for backward compatibility.
    +  Simplified coin name checks by replacing the `isCoins` method with a predefined set `coinNames`, enhancing readability and potentially reducing overhead.
    +  Renamed `map` to `internalNameMap` to better reflect its purpose.
+ Improved enchant detection and enchant parser error logging. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2816)
+ Changed "Kick Duration" to use `RepoPatterns`. - Luna (https://github.com/hannibal002/SkyHanni/pull/2837)
+ Used pre-processed methods for GUI scaling operations. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2648)
+ Fixed a typo in the key of Carnival Repo Patterns. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2842)
+ Improved the handling of active mayor perks, making it less annoying to work with. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2838)

## Version 0.27

### New Features

#### Garden Features

+ Added No Pests Chat Filter. - saga (https://github.com/hannibal002/SkyHanni/pull/1957)
    + Removed the chat message "There are no Pests on your Garden!".
+ Added No Pests Title. - saga (https://github.com/hannibal002/SkyHanni/pull/1957)
    + Shows a title when you use the Pest Tracker without any pests to clear.

#### Mining Features

+ Added a "Get from Sack" button in the forge recipe menu to retrieve ingredients. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2106)
+ Added Tracker for Glacite Corpses. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2306)
    + Tracks overall loot and loot per type.
+ Hides tooltips of items inside the Fossil Excavator in Glacite Tunnels. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2539)

#### Rift Features

+ Added Motes per Session. - Empa (https://github.com/hannibal002/SkyHanni/pull/2323)
+ Added Crafting Room Helper. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2178)
    + Shows a holographic mob at the location where the mob is present in the real room inside the Mirrorverse in Rift.
+ Added Rift Time Real-Time Nametag Format. - Empa (https://github.com/hannibal002/SkyHanni/pull/2015)
+ Added a helper for tracking the Buttons Enigma Soul in the Rift. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2616)
    + Using pathfinding to guide the player to the nearest spot with unpressed buttons and highlights them.
+ Added a route helper for Gunther's Rift Race in the West Village. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2616)
+ Added the ability to mute Wilted Berberis sounds when not farming. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2616)

#### Dungeon Features

+ Added highlight for starred dungeon mobs. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1558)
+ Added highlight for Fel skulls. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1558)
    + Optionally draws a line to them as well.
+ Added a Secret Chime for Dungeons with adjustable pitch and sound. - Ovi_1 (https://github.com/hannibal002/SkyHanni/pull/2478)
    + The sound and pitch of chimes in dungeons are customizable.

#### Scoreboard Features

+ Added Soulflow to the Custom Scoreboard. - Empa (https://github.com/hannibal002/SkyHanni/pull/1837)
    + Requires Soulflow to be enabled in Hypixel settings: /tab -> Profile Widget -> Show Soulflow.
+ Added the current minister to the calendar. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2342)
+ Allowed the use of the Custom Scoreboard outside of SkyBlock, but only on Hypixel. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1881)
+ Added an option to disable custom lines in the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1881)

#### Hoppity Features

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

#### The Carnival Features

+ Added a Reminder to claim Carnival Tickets. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2498)
+ Added display for Carnival Goals. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2498)
+ Added a feature to double-click the Carnival NPC to start the game without clicking in the chat. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2498)
+ Added Zombie Shootout QoL improvements for the Carnival. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2497)
    + Colored hitboxes.
    + Lamp timer + line.

#### GUI Features

+ Added Editable XP Bar. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1944)
    + Enabled moving and scaling of the XP bar in the SkyHanni GUI Editor.
+ Added Display Tab Widgets. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1276)
    + Allows the display of information from the Tab (e.g., Bestiary info).

#### Inventory Features

+ Added a warning when opening the Experimentation Table without a Guardian pet. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2127)
+ Added Enchanting Experience as Stack Size in Experimentation Table. - saga (https://github.com/hannibal002/SkyHanni/pull/1988)
    + Added to the Item Number list.
+ Show dye hex code as the actual color in the item lore. - nopo (https://github.com/hannibal002/SkyHanni/pull/2321)
+ Added Display for Bits on Cookie buy. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2265)
    + Shows the Bits you would gain.
    + Can show the change for the available Bits.
    + Also shows the time more clearly (or adds it if not present).
+ Added Compact Experimentation Table chat rewards. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2209)
    + Uses a compact chat message of rewards gained from Add-ons/Experiments.
+ Added Personal Compactor/Deletor Overlay. - Empa (https://github.com/hannibal002/SkyHanni/pull/1869)
    + Shows what items are currently inside the personal compactor/deletor, and whether the accessory is turned on or off.
+ Added Accessory magical power display as stack size. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2243)
    + Only works inside the Accessory Bag and Auction House.
+ Added `/gfs` to fix a broken Piggy Bank. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2150)
+ Added Superpair Display. - ILike2WatchMemes  (https://github.com/hannibal002/SkyHanni/pull/2171)
    + Displays found and matched pairs, power-ups, and missing pairs/normals.
+ Added Experiments Dry-Streak Display. - ILike2WatchMemes  (https://github.com/hannibal002/SkyHanni/pull/2171)
    + Shows attempts and XP since the last ULTRA-RARE.
+ Added Experiments Profit Tracker. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2171)
    + Tracks profits in Coins and Enchanting XP.
+ Added Ultimate Enchant Star. - Empa (https://github.com/hannibal002/SkyHanni/pull/2612)
    + Shows a star on Enchanted Books with an Ultimate Enchant.

#### Chat Features

+ Added `/shcolors` command. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2216)
    + Prints a list of all Minecraft color and formatting codes in chat.
+ Added Remind command. - ThatGravyBoat & Zickles (https://github.com/hannibal002/SkyHanni/pull/1708)
    + Use `/shremind <time> <note>` to set reminders for your future self.

#### Command Features

+ Added Reverse Party Leader. - Jordyrat (https://github.com/hannibal002/SkyHanni/pull/1712)
    + Reverse party transfer via clickable chat message or via the `/rpt` command.
    + Configurable party chat message after transferring.
+ Added /shtps command. - saga (https://github.com/hannibal002/SkyHanni/pull/1961)
    + Informs in chat about the server's ticks per second (TPS).
+ Added command `/shedittracker <item name> <amount>`. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2448)
    + Changes the tracked item amount for Diana, Fishing, Pest, Excavator, and Slayer Item Trackers.
    + Use a negative amount to remove items.

#### Crimson Isle Features

+ Added Crimson Isle Miniboss Respawn Timer. - Empa (https://github.com/hannibal002/SkyHanni/pull/2042)

#### Combat Features

+ Added Arachne Kill Timer. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/1993)
    + Shows how long it took to kill Arachne when the fight ends.
+ Added Broodmother spawn alert, countdown and stage change messages. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2325)
    + Countdown will not show until a spawning stage change is observed, and may be off by a few seconds.
+ Added Broodmother support to the Damage Indicator. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2325)
+ Added Carry Tracker. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2185)
    + Use `/shcarry` to add carries to a customer, and set a price for a slayer boss.
    + Automatically counts slayer bosses you carry.
    + Automatically tracks coins received from customers via `/trade`.

#### Fishing Features

+ Added Golden Fish Timer. - Empa (https://github.com/hannibal002/SkyHanni/pull/1941)
    + Includes an option to warn you when to throw your rod.
    + Shows how weak the golden fish is, as a nametag.
    + Also works on Stranded.
+ Added an alert for Gold or Diamond Trophy Fish catches. - ReyMaratov (https://github.com/hannibal002/SkyHanni/pull/2615)
    + Displays a popup with the trophy name, rarity, and amount of the catch.
    + Optionally, also plays a sound.+ Added an alert for Gold or Diamond Trophy Fish catches. - ReyMaratov (https://github.com/hannibal002/SkyHanni/pull/2615)
  + Displays a popup with the trophy name, rarity, and amount of the catch.
  + Optionally, also plays a sound.

#### Misc Features

+ Added a notification when you join a server you have joined previously. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2046)
+ Added warning when Non-God Pot effects are about to expire. - saga (https://github.com/hannibal002/SkyHanni/pull/2004)
    + Sends a title and plays a sound at a customizable time before the effect expires.
+ Added Area Navigation. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2468)
    + Reused the logic from the Tunnel Maps Graphs (Glacite Tunnels), now applied to other islands.
    + Displays a list of all areas on your current island. Click on a name to find a path to that area via pathfinding.
    + Find a route to the Hoppity egg. Useful for islands with small, nested corridors.
    + Get a title warning when entering an area.
    + See area names in the world.
    + Does not yet work with Spider's Den and Dungeon Hub. Only partial support for Farming Islands and the Rift.
+ Added option to hide pet nametag text. - Empa + j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1880)
+ Added Transfer Cooldown Prevention. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/1751)
    + Wait for the transfer cooldown to complete before sending commands like warp/is/hub.
+ Added highlighting for the active Beacon Effect. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2546)
+ Added navigation command. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2575)
    + Use `/shnavigate <name>` to find interesting locations on your current SkyBlock island by searching for them.
    + Displays a pathfinder to the location.
    + Works with NPCs, points of interest, areas, slayer spots, emissaries, crimson mini-bosses, spots to mine ores, break crops, and kill mobs.
    + Does not yet work with Spider's Den and Dungeon Hub (still needs to be mapped out).
+ Added a message to warp to Winter Island spawn when a Reindrake spawns. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2569)

### Improvements

#### Mining Improvements

+ Now allows modifying the Mineshaft spawn message without requiring the Pity Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/2308)
+ Added reset command for Fossil Excavator Profit Tracker. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2408)
+ Added current session and total Mineshaft count to the Mineshaft Pity Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/2310)
+ Some icons in the Mining Event Display now use the texture of the item they represent. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2425)
    + E.g., Raffle Event -> Raffle Ticket.
+ Changed how Powder Tracker works to account for powder buffs. - Jordyrat + Empa (https://github.com/hannibal002/SkyHanni/pull/2394)
+ Added an option to hide all tooltips inside the Fossil Excavator. - Cuz_Im_Clicks (https://github.com/hannibal002/SkyHanni/pull/2579)
+ Added support for using the Pickobulus ability for the Mineshaft Pity Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/2540)
+ Added Area Navigation support to Glacite Tunnels. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2544)
+ Updated wording in mineshaft pity breakdown ("efficient miner" -> "spread"). - Luna (https://github.com/hannibal002/SkyHanni/pull/2633)
+ Improved the wording of the Mining Event data error chat message. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2671)
+ Added Mining Effects (Powder Pumpkin, Filet O' Fortune, and Chilled Pristine Potato) to the Non-God Effect Display. - jani (https://github.com/hannibal002/SkyHanni/pull/2677)

#### Diana Improvements

+ Added /warp stonks to the Burrow Warp Helper. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2399)
+ Added per-election season Diana Trackers. - nea (https://github.com/hannibal002/SkyHanni/pull/2487)
    + This includes the Diana Profit Tracker and the Mythological Creature Tracker.
    + The data is stored moving forward, but there is currently no way to view anything other than the current year.
+ Changed the Diana Profit Tracker and Mythological Creature Tracker to always be visible when holding the Ancestral Spade in hand, even if Diana is not the mayor. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2496)

#### Scoreboard Improvements

+ Added an option to display the SkyBlock time in 24-hour format in the Custom Scoreboard. - Empa (https://github.com/hannibal002/SkyHanni/pull/1837)
+ Added an option to hide coins earned/lost in the Custom Scoreboard. - Empa (https://github.com/hannibal002/SkyHanni/pull/1837)
+ Added margin customization to the Custom Scoreboard. - Empa (https://github.com/hannibal002/SkyHanni/pull/1837)
+ Added an opacity slider for the Custom Background Image in the Custom Scoreboard. - Empa (https://github.com/hannibal002/SkyHanni/pull/1837)
+ Added an Undo message when auto alignment inCustom Scoreboard gets disabled. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2402)
+ Added an option to show SkyBlock time on the custom scoreboard rounded to 10 minutes, similar to the vanilla scoreboard. - Luna (https://github.com/hannibal002/SkyHanni/pull/2443)
+ Reduced the frequency of scoreboard errors being displayed. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2672)

#### Inventory Improvements

+ Added a toggle for displaying SkyHanni User Luck in Skyblock Stats. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2357)
+ Improved the clicked block highlight feature. - Vahvl & j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2166)
    + Added extensive customization options for colors, locked chest detection, and more.
+ Improved Enchant Parsing compatibility with other mods. - Vixid (https://github.com/hannibal002/SkyHanni/pull/1717)
    + Now works with SBA's Convert Roman Numerals feature.
    + Now works with NEU's inventories, such as `/pv` and storage overlay.
+ Added exceptions to the enchant parser. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2254)
    + Stonk and non-mining tools with Efficiency 5 use the maximum enchant color.
+ Added a short cooldown between Experimentation Table Guardian Pet chat warnings. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2459)
+ Prevented the SB Menu from replacing items from Dungeons and Kuudra that are placed in the item pickup log. - Fazfoxy (https://github.com/hannibal002/SkyHanni/pull/2599)

#### Chat Improvements

+ Added support for changing the translation language when clicking on a chat message. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2403)
+ Added support for using the "/gfs <item>" command without specifying an amount. - saga (https://github.com/hannibal002/SkyHanni/pull/1927)
    + The default value can be modified in the config.
+ Reworked the chat formatting for reminders and warnings across multiple features. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2428)
    + They now all allow performing an action on click (e.g., warping you somewhere or opening a menu) and can be disabled in the config via shift-click or control-click on the chat message.
    + Changed chat messages: Account/Profile Upgrade reminder, Hoppity Cake reminder, New Year Hoppity NPC reminder, New Year Cake reminder, City Project reminder, Garden Composter empty warning, CF Custom Goal reached message, Time Tower warning, CF upgrade warning.
+ Added more options to the Chat Hider feature. - not_a_cow (https://github.com/hannibal002/SkyHanni/pull/2348)
    + Option to block parkour messages and teleport pad messages.
+ Added more options to block messages. - not_a_cow (https://github.com/hannibal002/SkyHanni/pull/2490)
    + Chat Filter settings now include blocking parkour messages and teleport pad messages.

#### Hoppity Improvements

+ Added config option to disable rarity in Compact Hoppity messages. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2383)
+ Chocolate Factory: Shows "time to next milestone" instead of "max milestone" when you are at Prestige 6. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2503)
+ Added the required chocolate amount to the tooltip of milestone rabbits in Hoppity's Collection. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2523)
+ Improved the responsiveness of Hoppity Chat Compacting. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2488)
+ Added the ability to see how many duplicates you have previously found in Hoppity messages. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2556)
+ Improved the formatting of the Hoppity Event Stats card. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2638)

#### Combat Improvements

+ Made the "Line to Arachne" width configurable. - azurejelly (https://github.com/hannibal002/SkyHanni/pull/2406)
+ Made the "Line to Miniboss" width configurable. - azurejelly (https://github.com/hannibal002/SkyHanni/pull/2406)
+ Added item cooldown support for Blaze Flares. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2601)+ Added item cooldown support for Blaze Flares. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2601)

#### Config Improvements

+ Improved the description of several config options. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2397)
+ Enabled more Dev Options to be changed via /shdefaultoptions. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2397)

#### Garden Improvements

+ Improved incorrect Rancher Boots speed warning. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2413)
    + Also allows clicking on the message to open the edit sign directly.
    + Now warns only when actually farming and wearing Rancher Boots.
+ Changed suggested cactus speed to 464 again. - DarkDash (https://github.com/hannibal002/SkyHanni/pull/2424)
    + Since the cactus knife now allows 500 max speed.
+ Made the waypoint to the middle of the plot for Pest Waypoint optional (off by default). - Luna (https://github.com/hannibal002/SkyHanni/pull/2469)
+ Reduced one click for supercrafting visitor materials by using `/viewrecipe` instead of `/recipe` when using the Shopping List button. - Miestiek (https://github.com/hannibal002/SkyHanni/pull/2505)
+ Added ignored crops for the Farming Lane feature. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2622)
+ Made the Visitor Timer GUI clickable to teleport to the Barn. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2658)

#### Rift Improvements

+ Updated the description of the config for Enigma Soul Waypoints to help find the Rift Guide in the game. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2433)
+ Added Path Finder to Enigma Soul waypoints in the Rift. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2515)
+ Added the option to choose the color of the Living Metal Helper highlight. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2616)
+ Added an option to hide players while in Gunther's Rift Race. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2655)

#### Dungeon Improvements

+ Added dungeon stars and crimson stars to estimated item value. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2492)

#### The Carnival Improvements

+ Updated the Zombie Shootout Diamond color to be a deeper blue. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2511)
+ Added a cooldown to the Carnival NPC Quickstart feature. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2628)

#### Misc Improvements

+ Updated Last Server to also alert for servers you have just left. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2494)
+ Added search functionality to some SkyHanni GUIs. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2477)
    + Search works in all SkyHanni Trackers and the Sack Display.
    + In your inventory, hover over the GUI with your mouse, then start typing to search.
+ Improved the performance of pathfinding logic in Area Navigation. - nea (https://github.com/hannibal002/SkyHanni/pull/2537)
+ Added a toggle to force the `en_US` locale for number formatting. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2563)
+ Queued GfS will now always fall back to a default amount if one is not specified. - Luna (https://github.com/hannibal002/SkyHanni/pull/2584)
+ Improved pathfinding. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2597)
    + The line to the target no longer jumps around as much.
    + Progress to the target now shows up in chat, displaying blocks remaining and percentage completed. Clicking on the chat message cancels the pathfinding.
+ Added a warning in chat when the computer's time is inaccurate, along with a tutorial on how to fix it. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2623)
+ Improved the way pathfinding lines are rendered. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2634)
+ Automatically starts pathfinding after clicking on SkyHanni reminder chat messages about Carnival Reminder, Hoppity NPC, City Project, and account/profile upgrades. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2635)
+ Greatly improved performance by optimizing SkyHanni Chroma support for Patcher Font Renderer. - nopo (https://github.com/hannibal002/SkyHanni/pull/2666)
+ "Could not find server id" errors are now less common. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2680)
+ Clarified the wording of Bazaar Data Loading error messages. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2681)
+ Indicate in the CF menu when the chocolate amount has reached the cap and is therefore halted. - hannibal2  (https://github.com/hannibal002/SkyHanni/pull/2688)

### Fixes

#### Diana Fixes

+ Added a chat option to disable a Patcher setting that breaks SkyHanni's line from the center of the player to something else. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2387)
    + E.g. Diana guesses, Slayer Mini Bosses, etc.
+ Fixed an error when pressing the inquisitor share button. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2484)

#### Bingo Fixes

+ Fixed "show bingo rank number" feature toggle not functioning. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2445)
+ Fixed Custom Scoreboard not showing Carnival Lines on Bingo. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2631)

#### Inventory Fixes

+ Fixed ItemCategory errors being sent even though we already have a fix. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2392)
+ Fixed AH item movements being detected by Item Trackers as new drops. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2380)
+ Fixed Pickup Log Display Entries not expiring while in GUIs. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2401)
+ Fixed the price of mana enchantments tiers 1-4 in Estimated Chest value. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2395)
+ Fixed Guardian Reminder Experimentation Table not working with autopet rules. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2430)
+ Fixed SkyHanni Global Scale affecting Custom Wardrobe. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2420)
+ Fixed /show displaying an incorrect number of enchants. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2435)
+ Fixed Armor Hider hiding armor inside the Custom Wardrobe. - Empa (https://github.com/hannibal002/SkyHanni/pull/2449)
+ Fixed detection of El Dorado and Stray Hordes in the Chocolate Factory. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2450)
+ Fixed merged enchant books tooltip being overwritten. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2454)
+ Fixed skull sizes inside the Personal Compactor Overlay. - Empa (https://github.com/hannibal002/SkyHanni/pull/2486)
+ Fixed the Auction House Price Comparison appearing in your own inventory as well. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2483)
+ Fixed "click to show tunnel map" from the Glacite Mines commission menu appearing in the inventory. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2485)
+ Fixed Mouse Keys not working as Wardrobe Slot Hotkeys. - j10a1n15, ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2506)
+ Fixed Estimated Item Value incorrectly showing all Kuudra armor as starred. - Luna (https://github.com/hannibal002/SkyHanni/pull/2550)
+ Fixed Experiments Profit Tracker & Superpair Data. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2560)
+ Fixed price per unit for Stonk of Stonks auction not working for the bracket you are in. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2572)
+ Fixed bugs in the Experiments Profit Tracker. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2594)
    + Corrected the count for bottles applied while being inside the experimentation table.
    + Fixed `/shedittracker` support not functioning properly.
+ Fixed an issue where the item stack size on Diamond/Golden Heads and Master Skulls could be incorrect. - Fazfoxy (https://github.com/hannibal002/SkyHanni/pull/2611)
+ Fixed item category detection for recombobulated items. - minhperry (https://github.com/hannibal002/SkyHanni/pull/2608)
+ Fixed bugs in Superpair Data. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2566)
    + Fixed pair detection.
    + Fixed enchanting XP detection.
+ Fixed Minister in Calendar Perk description sometimes not using the gray color. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2632)
+ Fixed highlighting items in one's own inventory for some Bazaar features, e.g., in the "click to re-order" feature. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2640)
+ Automatically remove incorrect Account/Profile Upgrade warnings by talking to Elizabeth. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2674)

#### Mining Fixes

+ Added new Pure Ores support. - Empa (https://github.com/hannibal002/SkyHanni/pull/2388)
+ Fixed a typo in corpse detection. - Maratons4 (https://github.com/hannibal002/SkyHanni/pull/2386)
+ Fixed a compatibility issue in Fossil Solver with resource packs like Hypixel+. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2409)
+ Fixed Powder for 10 Levels overflowing over the max level. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2422)
+ Fixed Glacite Tunnel Maps repeatedly showing the last commission goal even when there is no new commission. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2432)
+ Fixed Mineshaft Pity Counter not working with some ore types. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2436)
+ Fixed Tungsten Ore and Hardstone detection after the mining update. - Empa (https://github.com/hannibal002/SkyHanni/pull/2437)
+ Fixed item category errors after the new mining update. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2529)
+ Fixed blocks mined by Efficient Miner not being detected. - Empa (https://github.com/hannibal002/SkyHanni/pull/2526)
+ Fixed Corpse Tracker resetting items between restarts. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2528)
+ Fixed the Corpse Tracker not displaying while in the Mineshaft. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2516)
+ Fixed Hardstone and Tungsten not being properly detected in Mineshafts. - Empa (https://github.com/hannibal002/SkyHanni/pull/2536)
+ Fixed Heart of the Mountain showing outdated perks and powder information. - Tryp0xd (https://github.com/hannibal002/SkyHanni/pull/2543)
+ Fixed Sky Mall perks not being read correctly after the Hypixel update. - Luna (https://github.com/hannibal002/SkyHanni/pull/2551)
+ Fixed Mining Commission Blocks Color not working properly with Connected Textures. - Empa (https://github.com/hannibal002/SkyHanni/pull/2553)
+ Fixed Gemstone Gauntlet item category. - Luna (https://github.com/hannibal002/SkyHanni/pull/2534)
+ Fixed Glacite powder being tracked as Glacite in Corpse Tracker. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2557)
+ Fixed "Mining Commissions Block Color" disabling OptiFine connected textures. - nopo (https://github.com/hannibal002/SkyHanni/pull/2559)
+ Fixed some blocks in the Crystal Hollows being detected as Mithril instead of Hard Stone. - Luna (https://github.com/hannibal002/SkyHanni/pull/2580)
+ Fixed "Mining Commissions Block Color" causing OptiFine connected textures not to connect properly. - nopo (https://github.com/hannibal002/SkyHanni/pull/2577)
+ Fixed the Mineshaft Pity Counter not working in the Great Glacite Lake. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2565)
+ Fixed Powder Tracker inaccuracies. - Empa (https://github.com/hannibal002/SkyHanni/pull/2591)
+ Fixed Jasper gemstones not being addressed in the Powder Mining filter. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2618)
+ Fixed the King Talisman Tracker sometimes not displaying the correct data. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2663)
+ Fixed the Powder Tracker not tracking items correctly. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2645)
+ Fixed an error in Powder Tracker. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2678)

#### Scoreboard Fixes

+ Fixed a Scoreboard Error during M7 Dragons. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2382)
+ Fixed Custom Scoreboard Background position being updated 1 frame after the scoreboard text. - Empa (https://github.com/hannibal002/SkyHanni/pull/1837)
+ Fixed various Custom Scoreboard issues. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2402)
    + Bank showing "null" during Island Switch.
    + Alignment not being affected by outline thickness.
    + Alignment with scales that are not 1.
    + Line Spacing not working.
+ Fixed Custom Scoreboard Error when no repository exists. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2407)
+ Fixed rounded rectangles not working with a scale less than 1. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2404)
    + E.g. Custom Scoreboard Background.
+ Fixed rare Custom Scoreboard errors. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2411)
+ Fixed Minister not resetting after the election closed. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2467)
+ Fixed Custom Scoreboard errors while doing Carnival activities. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2455)
+ Fixed Custom Scoreboard error while in a Dungeon Queue. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2441)
+ Fixed Custom Scoreboard Error during the Barry Protestors Quest. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2480)
+ Fixed a Custom Scoreboard error during the Magma Boss Fight and Goblin Raid. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2531)
+ Fixed a Custom Scoreboard error while in the Dojo. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2519)
+ Fixed a Custom Scoreboard error during M7 Dragons. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2510)
+ Fixed Custom Scoreboard error during the Raffle Event. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2545)
+ Fixed both the Custom Scoreboard Active and Starting Soon Tab List events being active simultaneously in the Garden. - Empa, j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2592)
+ Fixed Custom Scoreboard not showing the Perkpocalypse Mayor. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2683)
+ Fixed some scoreboard error messages appearing in chat. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2672)
+ Fixed the custom scoreboard not updating when the Hypixel scoreboard is not updating. - hannibal2  (https://github.com/hannibal002/SkyHanni/pull/2685)

#### Hoppity Fixes

+ Fixed "Side Dish" and Milestone rabbits not sending a message when Compact Hoppity is enabled. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2311)
+ Fixed rabbit rarity not appearing on compacted Duplicate rabbit messages. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2383)
+ Fixed "Highlight Requirement Rabbits" requiring "Hoppity Collection Stats" to function. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2398)
+ Fixed minor formatting issues with Hoppity Event Summary. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2416)
+ Fixed Hoppity Event stats resetting. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2489)
+ Fixed an issue where Fish the Rabbit and El Dorado did not have compacted chat messages. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2488)
+ Fixed inconsistencies in Hoppity Duplicate Number counts. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2595)
+ Fixed Fish the Rabbit and El Dorado incorrectly counting as unique rabbits during Hoppity's Hunt. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2627)
+ Fixed Hoppity Collection Display not updating the rabbit count in some cases, e.g., when collecting rabbits on alpha or manually changing the config values. - hannibal2  (https://github.com/hannibal002/SkyHanni/pull/2686)
+ Fixed the chocolate count increasing in other menus even when production is halted. - hannibal2  (https://github.com/hannibal002/SkyHanni/pull/2688)

#### Chat Fixes

+ Fixed an error with Patcher Coordinate Detection from chat. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2410)
+ Fixed ultimate enchants not showing in `/show`. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2465)
+ Fixed being unable to shift-click on chat messages. - nopo (https://github.com/hannibal002/SkyHanni/pull/2650)

####  Commands Fixes

+ Fixed the command `/shtps` printing nothing if the TPS display was currently showing nothing. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2446)
+ Fixed "/viewrecipe command replace" breaking pages. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2679)

#### Dungeon Fixes

+ Fixed disabled SkyHanni features causing compatibility issues with other Dungeon Party Finder mods. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2470)
+ Fixed dungeon completion detection. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2456)
    + This also fixes the Croesus reroll detection breaking.

#### Crimson Isle Fixes

+ Fixed the reputation helper not updating after a sack update. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2439)
+ Fixed Heavy Pearls sometimes not being highlighted. - Luna (https://github.com/hannibal002/SkyHanni/pull/2479)
+ Added a delay to the Pablo helper message so it appears after he finishes his dialogue. - Luna (https://github.com/hannibal002/SkyHanni/pull/2662)

#### Garden Fixes

+ Fixed outdated Dicer gemstone fortune in FF guide. - not_a_cow (https://github.com/hannibal002/SkyHanni/pull/2499)
+ Fixed some garden messages not being blocked. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2532)
+ Fixed active sprays not being removed from the GUI when using a washer. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2532)

#### Combat Fixes

+ Updated Ghost Counter bestiary tiers to reflect Hypixel reducing the max kills needed from 250K to 100K. - Luna (https://github.com/hannibal002/SkyHanni/pull/2533)
+ Fixed a case where the Broodmother countdown could be wildly inaccurate. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2513)
+ Fixed Flare Display spamming chat with negative values when the server is lagging. - Stella (https://github.com/hannibal002/SkyHanni/pull/2567)
+ Fixed a case where the Imminent stage did not update the Broodmother countdown. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2602)v
+ Fixed some Islands Dummy mob types not being detected by the Damage Indicator. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2646)
+ Fixed the Ender Node Tracker not detecting some items. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2644)
+ Fixed Flare Expiration warnings not appearing in chat or as a title. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2643)

#### The Carnival Fixes

+ Fixed two "Catch a Fish" carnival goals not being properly detected from chat. - Luna (https://github.com/hannibal002/SkyHanni/pull/2517)
+ Fixed the bounding box of baby zombies in Zombie Shootout. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2512)

#### Fishing Fixes

+ Fixed error messages while using the Chum Bucket Hider feature. - nea (https://github.com/hannibal002/SkyHanni/pull/2587)
+ Fixed the Trophy Fish Title displaying an undefined character in the format. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2670)

#### Rift Fixes

+ Fixed the Rift Timer pausing while in Gunther's Rift Race. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2616)

#### Misc Fixes

+ Fixed Mayor Detection failing when Special Mayors are in office. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2389)
+ Updated Derpy's extra tax perk to use the correct name. - Luna (https://github.com/hannibal002/SkyHanni/pull/2393)
+ Fixed Christmas presents in the Hypixel lobby being visible outside of December. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2396)
+ Fixed the mod crashing on startup when the game directory path contains a "!". - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2427)
+ Fixed chat formatting for coin drops in profit trackers (Diana, Slayer, Fishing, Pest). - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2444)
+ Fixed Last Server Time Slider not functioning. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2440)
+ Fixed warning sounds being stuttered and cutting out. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2482)
+ Fixed an internal bug with `getItemName` that caused the `NEUInternalName.NONE has no name!` error messages to appear. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2471)
+ Fixed a typo in mob detection config. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2472)
+ Fixed performance issues with a high number of dungeon runs in one session. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2504)
+ Fixed `/shedittracker duplex` not working. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2501)
+ Fixed "Last Server Joined" spamming the chat. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2495)
+ Fixed lag spikes in Path Finder. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2525)
+ Fixed old mayor perks not being marked as inactive when the mayor changes. - Luna (https://github.com/hannibal002/SkyHanni/pull/2524)
+ Fixed direct GitHub downloads triggering the source checker. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2548)
+ Fixed reforge display not working with new mining stats. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2555)
+ Fixed Totem of Corruption expiry warning not working in some cases. - Luna (https://github.com/hannibal002/SkyHanni/pull/2554)
+ Fixed a few typos in the config. - rdbt (https://github.com/hannibal002/SkyHanni/pull/2585)
+ Fixed Area Navigation distances to player being incorrect. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2634)
+ Fixed item trackers displaying removed item names incorrectly. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2620)
+ Fixed some messages from Pablo NPC not being detected. - Luna (https://github.com/hannibal002/SkyHanni/pull/2636)
+ Fixed SkyHanni re-downloading the repository on every launch. - nopo (https://github.com/hannibal002/SkyHanni/pull/2669)
+ Fixed unknown Perkpocalypse Mayor chat spam. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2683)
+ Fixed the wiki keybind searching for "null" on items with no internal name. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2682)

### Technical Details

+ Cleaned up IslandExceptions to improve readability. - walker (https://github.com/hannibal002/SkyHanni/pull/1560)
+ Added drawInsideRoundedRectOutline, drawInsideImage, and image Renderables. - Empa (https://github.com/hannibal002/SkyHanni/pull/1837)
+ Added `HoppityAPI`, which should house event-specific features rather than `ChocolateFactoryAPI`. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2311)
    + Added an `Always Hoppity` dev config option, which will override `isHoppityEvent`.
+ Added `RabbitFoundEvent`, which will be fired when a chocolate rabbit is found from an egg, bought from Hoppity, or claimed from a milestone. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2311)
+ Added `SIDE_DISH`, `CHOCOLATE_SHOP_MILESTONE`, `CHOCOLATE_FACTORY_MILESTONE`, and `BOUGHT` as `HoppityEggType` options. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2311)
+ Added version to download source checker popup message. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2400)
+ Added "hex string to color int" method in ColorUtils. - nopo (https://github.com/hannibal002/SkyHanni/pull/2321)
+ Fixed Sound Errors in the console. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2431)
+ Added /shdebugscoreboard to monitor scoreboard changes. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2412)
    + Prints the raw scoreboard lines in the console after each update, with the time since the last update.
+ Added split and dissolve features for the graph editor. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2466)
+ Added ghost position feature to the graph editor, useful for editing nodes that are in the air. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2461)
+ Added tags to the graph editor. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2464)
+ Changed graph node editor behavior. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2451)
    + WASD node movement now reflects the player's look direction.
    + Nodes are only deleted when they are selected/active.
    + Allows creating nodes much closer to each other.
+ Replaced `List<List<Any>>` with `List<Renderable>` in all SkyHanni trackers. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2474)
+ Rebased Hoppity config to be in its own package. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2272)
+ Use on DelayedRun to add scheduled tasks. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2462)
+ Created a `ClickBlockType` enum and a `DungeonBlockClickEvent` class. - Ovi_1 (https://github.com/hannibal002/SkyHanni/pull/2478)
    + Added a new enum for block types and a class to handle click events on dungeon blocks.
+ Updated `DungeonHighlightClickedBlocks` to use the new `DungeonBlockClickEvent` class. - Ovi_1 (https://github.com/hannibal002/SkyHanni/pull/2478)
+ Added fakeInventory, paddingContainer and drawInsideFixedSizedImage. - Empa (https://github.com/hannibal002/SkyHanni/pull/1869)
+ Changed RenderableTooltips to be more consistent with the vanilla ones. - Empa (https://github.com/hannibal002/SkyHanni/pull/1869)
+ Improved error logging for enchant parser. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2454)
+ Added renderable search box. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2475)
+ Added the ability to add more optional display modes to trackers. - nea (https://github.com/hannibal002/SkyHanni/pull/2487)
    + These extra trackers still need to provide their own storage.
+ Added perk descriptions to mayor perks. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2342)
    + These are loaded when checking the active mayor/minister from the API, so most of the perks will initially have no description.
+ Added a method to override an ItemStack's lore. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2342)
+ Updated HandleEvent onlyOnIslands to handle multiple islands. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2502)
+ Added an abstract "bucketed" tracker that can store where items came from. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2306)
    + Takes an enum in instantiation and has built-in logic for swapping display types.
+ Graph Editor improvements. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2515)
    + Code cleanup in Graph Editor, moving node lists into their own class.
    + Fixed searchable tag list.
    + Added more graph node tags; only display certain tags on specific islands in the editor.
    + Added an option to limit the maximum render range of nodes and edges in the Graph Editor for better performance.
+ Hoppity's Collection highlighting now updates on event triggers instead of every frame. - the1divider (https://github.com/hannibal002/SkyHanni/pull/2438)
+ Added `select near look` keybind in Graph Editor. - nea (https://github.com/hannibal002/SkyHanni/pull/2537)
+ Added checks to avoid creating bugs with the area structure in Graph Editor. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2537)
+ Added options to the graph node list to hide nodes with certain tags. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2537)
+ Removed the use of `LorenzChatEvent()` for 'manually' adding data to Hoppity Chat Compact. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2488)
    + Now uses `EggFoundEvent()`.
+ The Renderable Wrapped String now has an option to align text inside the wrapped string box. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2372)
+ Improved handling of unknown stats for ReforgeAPI. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2562)
+ Added `AreaChangeEvent`. - Empa (https://github.com/hannibal002/SkyHanni/pull/2535)
+ Updated `HandleEvent` to allow `onlyOnIsland` usage for a single island without brackets. - Empa (https://github.com/hannibal002/SkyHanni/pull/2535)
+ Created a Text method to display a paginated list. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2583)
    + Moved existing paged lists in `/shcommands` and `/shremind` to use this.
+ Node Tags filter list in Graph Editor now sorts by nodes per tag. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2575)
+ Additional code cleanup around ingredients. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2576)
+ Removed duplicate rounding functions. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1931)
+ Added HTTP Request Patching. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2578)
+ Removed references to NEU code, especially code from the Utils class in NEU. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2419)
+ Banned importing `io.github.moulberry.notenoughupdates.util.Utils`. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2419)
+ Added common live templates. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2463)
    + These can be automatically imported via the plugin "Live Templates Sharing".
+ Created and used primitive recipe and ingredient classes. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2460)
    + These are added as a layer between SkyHanni and NEU so that we are not reliant on NEU functions.
+ Added Blossom Gradle plugin for token replacement in code. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2558)
    + `@MOD_VERSION@` is now a token that will be replaced.
+ Added `detekt` runner to the build process. - Daveed & nea (https://github.com/hannibal002/SkyHanni/pull/2547)
+ Added Clock Offset Millis using an NTP server. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2623)
+ Added `EntityMovementData.onNextTeleport()` logic, which triggers a runnable after the world has changed or the player has teleported. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2635)
+ Added Bazaar Fetch information to `/shdebug`. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2675)
+ Removed the password from the download source check. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2649)
+ Added a missing SkyBlock check for debug commands `/shtrackparticles` and `/shtracksounds`. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2641)
+ Prepared for later migration of correct config name of Plhlegblast. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2684)
+ Added the ability to disable SkyHanni errors via repo configuration. - nopo (https://github.com/hannibal002/SkyHanni/pull/2668)
+ Added current server ID and fetch data to the `/shdebug` command. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2680)
+ Added more `/shdebug` checks for aspects like OS, Java version, and launcher. - hannibal2  (https://github.com/hannibal002/SkyHanni/pull/2691)

### Removed Features

+ Removed Wild Strawberry Dye notification. - MTOnline (https://github.com/hannibal002/SkyHanni/pull/2520)
    + Artist's Abode now sends a lobby-wide message for dye drops, making the notification obsolete.
+ Removed `Only Requirement Not Met` and `Highlight Requirement Rabbits` config options. - the1divider (https://github.com/hannibal002/SkyHanni/pull/2438)
    + Replaced then with a draggable list of different Hoppity collection highlight options.
+ Removed "Forge GfS". - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2564)
    + Hypixel now pulls directly from sacks when using the forge.
+ Removed Mithril Powder from the Powder Tracker. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2656)

## Version 0.26

### New Features

#### Combat Features

+ SOS/Alert/Warning Flare Display. - HiZe + hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1803)
    + Warn when the flare is about to despawn (chat, title or both).
    + Change the display type (as a GUI element, in the world, or both).
    + Show effective area (as a wireframe, filled or circle).
    + Show mana regeneration buff next to the GUI element.
    + Option to hide flare particles.
+ Added a "Line to Arachne" setting, just like with slayer mini bosses. -
  azurejelly (https://github.com/hannibal002/SkyHanni/pull/1888)
+ Added In-Water Display. - Stella (https://github.com/hannibal002/SkyHanni/pull/1892)
    + Useful when using a Prismarine Blade in Stranded Mode.
+ Added toggle for compacting Garden visitor summary messages. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2026)
+ Add Armor Stack Display. - Xupie (https://github.com/hannibal002/SkyHanni/pull/1811)
    + Display the number of stacks on armor pieces like Crimson, Terror etc.
+ Add a line to Nukekebi Skull. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2148)

#### Garden Features

+ New "Craftable!" message when Visitor Items Needed are craftable. -
  Paloys (https://github.com/hannibal002/SkyHanni/pull/1891)
+ Added crop last farmed waypoint. - appable (https://github.com/hannibal002/SkyHanni/pull/1335)
    + Accessible in the Crop Start Locations section.
+ Added Farming Personal Best FF Gain. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2332)
    + Displays in chat how much additional FF you earn from the farming contest personal best bonus after beating your previous record.

#### Dungeon Features

+ Added Croesus Limit Warning. - saga (https://github.com/hannibal002/SkyHanni/pull/1908)
    + Sends a warning when you are close to the Croesus Chest Limit.

#### Command Features

+ Option to toggle limbo time in detailed /playtime. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1863)

#### Chat Features

+ Added new chat filters. - Mikecraft1224 (https://github.com/hannibal002/SkyHanni/pull/1750)
    + Filters for rare dungeon chest rewards and sacrifice messages from other players.
+ Add config option to hide achievement spam on Alpha. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2076)

#### Event Features

+ Chat reminder to check Hoppity Shop each year. - appable (https://github.com/hannibal002/SkyHanni/pull/1843)
+ Highlight rabbits that have requirements in the Hoppity's Collection menu. -
  HiZe (https://github.com/hannibal002/SkyHanni/pull/1874)
    + Green: Requirement met.
    + Red: Requirement not met.
+ Mark duplicate egg locations in red for unlocking some of the new rabbits. -
  appable (https://github.com/hannibal002/SkyHanni/pull/1929)
    + Option to always mark nearby duplicate egg locations.
+ Added "Collected Locations" line to the Unclaimed Eggs Display. -
  appable (https://github.com/hannibal002/SkyHanni/pull/1929)
    + This may be inaccurate if you've already collected eggs on an island.
+ Show total amount of chocolate spent in the Chocolate Shop. -
  sayomaki (https://github.com/hannibal002/SkyHanni/pull/1921)
+ Added stray/golden chocolate rabbit production time. - sayomaki (https://github.com/hannibal002/SkyHanni/pull/1978)
+ Display missing egg collection locations. - nea (https://github.com/hannibal002/SkyHanni/pull/1997)
    + Tracks the unique egg location requirements for rabbits from your Hoppity collection.
    + This data is partially enriched using the duplicate egg location tracker.
+ Added Rabbit Pet Warning. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2087)
    + Warns when using the Egglocator without having a Mythic Rabbit Pet selected.
+ Flash your screen when a special stray rabbit appears. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2040)
    + Option to also flash on normal stray rabbits.
+ Added a tracker for stray rabbits caught in the Chocolate Factory. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2210)

#### Mining Features

+ Configurable cold amount for the ascension rope message in Glacite Mines. -
  nopo (https://github.com/hannibal002/SkyHanni/pull/1905)
+ Added HOTM perks level and Enable Display. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1059)
+ Added Mineshaft Corpse and Entrance/Ladder Locators. - nobaboy (https://github.com/hannibal002/SkyHanni/pull/1500)
    + Includes keybind functionality for both.
+ Added HotM Perks Powder features. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1460)
    + Added total powder cost to HotM perks.
    + Added powder cost for 10 levels.
+ Added Mineshaft Pity Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1655)
    + Displays information about Mineshaft spawns, your progress towards a Pity Mineshaft, and more.
+ Added Compacted Hard Stone to Powder Tracker. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2138)

#### Inventory Features

+ Warning when outbid on an auction. - seraid (https://github.com/hannibal002/SkyHanni/pull/1818)
+ Added option to show time held in lore for Jyrre Bottles and Cacao Truffles. -
  Obsidian (https://github.com/hannibal002/SkyHanni/pull/1916)
+ Added Dark Cacao Truffle hours held as stack size. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1916)
+ Added ULTRA RARE Book Notification when doing the Experiment Table. -
  raven (https://github.com/hannibal002/SkyHanni/pull/1738)
+ Wand of Strength cooldown is now displayed. - saga (https://github.com/hannibal002/SkyHanni/pull/1948)
    + The cooldown displayed is for the buff, not the item usage.
+ Added Favorite Power Stones. - saga (https://github.com/hannibal002/SkyHanni/pull/2002)
    + Highlighted in the Thaumaturgy inventory.
    + Shift-click to add/remove them.
+ Added an option to block Chocolate Factory access without a mythic rabbit pet. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2124)
+ Shows a list of items that can be crafted with the items in your inventory when inside the crafting menu. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1334)
    + Click on an item to open /recipe.
    + Option to include items from inside sacks.
    + Option to include vanilla items.
+ Add Page Scrolling. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2164)
    + Allows scrolling in any inventory with multiple pages.
+ Add Reforge Helper. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1437)
    + Add reforge locking similar to SkyblockAddons.
    + Work with Hex.
    + Show a list of all possible reforges for the item, with stats.
    + The reforge list can be sorted by a specific stat.
    + Optionally show the different stat values compared to the current reforge.
+ Add reminder that the Time Tower is expired and has extra charges available. - appable (https://github.com/hannibal002/SkyHanni/pull/2133)
    + Replaces the previous Time Tower Reminder feature.
+ Show Bestiary Level as Stack Size. - saga (https://github.com/hannibal002/SkyHanni/pull/1945)
    + Can be enabled/disabled on the Item Number list.
+ Added Item Pickup Log. - catgirlseraid (https://github.com/hannibal002/SkyHanni/pull/1937)
+ Added Tactical Insertion Ability Cooldown feature. - DungeonHub (https://github.com/hannibal002/SkyHanni/pull/2278)
+ Display the price per Stonk when taking the minimum bid in the Stonks Auction (Richard Menu). - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2195)

#### Fishing Features

+ Added Trophy Fishing Display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1754)
    + Similar to Skytils, but with more options.
    + Option to sort by item rarity, total amount, rarity amount, the highest rarity, or name alphabetically.
    + Option to change what information is displayed (rarity, name, icon, total caught).
    + Option to show only missing trophies.
    + Option to highlight new catches in green.
    + Options for when to show the display: always in Crimson Isle, in inventory, with rod in hand, or on keybind press.
    + Option to show only when wearing a full hunter armor set.
    + Options to show checkmarks or crosses instead of numbers.
    + Hover over a trophy fish to see the percentage caught and how to gain progress.

#### Rift Features

+ Punchcard Artifact Highlighter & Overlay. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1089)
    + Highlights unpunched players in the Rift.
+ Added Splatter Hearts Highlight for the Rift. - Empa (https://github.com/hannibal002/SkyHanni/pull/2318)

#### Misc Features

+ Click on breakdown display in /playtimedetailed to copy the stats into the clipboard. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1807)
+ Added Editable Hotbar. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1903)
    + Allows for moving and scaling in the SkyHanni GUI editor.
+ Added Beacon Power Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1901)
+ Added Hide Useless Armor Stands. - Empa (https://github.com/hannibal002/SkyHanni/pull/1962)
    + Hides armor stands that briefly appear on Hypixel.
+ Added Custom Wardrobe, a new look for the wardrobe. - j10a1n15, Empa (https://github.com/hannibal002/SkyHanni/pull/2039)
    + Highly customizable: Colors, display sizes.
    + Estimated Price Integration.
    + Favorite slots; option to only display favorite slots.
+ Added toggle for compacting Garden visitor summary messages. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2026)
+ Added SkyHanni User Luck to the stats breakdown. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1288)
    + This can be viewed in /sbmenu, /equipment, and the Misc Stats submenu.
+ Added a source download verification checker. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1914)
    + Warns you when the mod has been downloaded from an untrusted source (not the official GitHub or Modrinth).
+ Added Chunked Stats as a draggable list to the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1341)
    + Select and prioritize the order of the stats you want to see.
+ Added Option to crash your game when you die, for extra suffering. - Stella (https://github.com/hannibal002/SkyHanni/pull/2238)
    + Also punishes players who don't read patch notes.
+ Added Crown of Avarice counter. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2229)
+ Added a title notification for when the Thunder Bottle is fully charged. - raven (https://github.com/hannibal002/SkyHanni/pull/2234)

### Improvements

#### Farming Island Improvements

+ Trevor Mob Detection now works with Oasis mobs. - Awes (https://github.com/hannibal002/SkyHanni/pull/1812)

#### Garden Improvements

+ Added `/shtpinfested` command to teleport to nearest infested plot. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1763)
+ Warp to Infested Plot warps to garden home when no pests are found. -
  saga (https://github.com/hannibal002/SkyHanni/pull/1910)
    + Affects both hotkey and command.
+ Added an option for a compacted Dicer RNG Drop Tracker Display. -
  Jordyrat (https://github.com/hannibal002/SkyHanni/pull/1735)
    + Also shortened the default display.
+ Minor GUI improvements in /ff. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/873)
+ Displays in /ff the fortune from bestiary and armor stats for enchantments and gemstones. - maxime-bodifee (https://github.com/hannibal002/SkyHanni/pull/1861)
+ Allow clicking on the Craftable! text in the shopping list to open the "view recipe" menu. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2075)
+ Rename Mushroom Tier to Mushroom Milestone in Mooshroom Cow Perk. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2159)
+ Add Fine Flour to the /ff menu. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2194)
+ Added a "Compact" option to the Rancher Boots Optimal Speed GUI. - Empa (https://github.com/hannibal002/SkyHanni/pull/2137)
+ The Rancher Boots Optimal Speed GUI now highlights the crop corresponding to the last held tool. - Empa (https://github.com/hannibal002/SkyHanni/pull/2137)
+ Added Slug pet to the FF Guide. - maxime-bodifee (https://github.com/hannibal002/SkyHanni/pull/2249)
+ Added Copper dye to the list of blocked visitor rewards. - not-a-cow (https://github.com/hannibal002/SkyHanni/pull/2329)
+ Added Copper Dye to Visitor Drop Statistics. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2346)

#### Hoppity Event Improvements

+ Renamed "Unfound Eggs" to "Unclaimed Eggs" for the Hoppity event. -
  Luna (https://github.com/hannibal002/SkyHanni/pull/1876)
+ Added Compact Hoppity Chat option. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1748)
+ Hoppity Collection Stats are now persistent. - appable (https://github.com/hannibal002/SkyHanni/pull/1836)
    + No longer reset on profile swap or game restart.
+ Merged duplicate times in compacted Hoppity's messages. -
  Daveed (https://github.com/hannibal002/SkyHanni/pull/1887)
+ Option to show Hoppity Eggs timer outside of SkyBlock. -
  maxime-bodifee (https://github.com/hannibal002/SkyHanni/pull/1926)
+ Adjusted and added Rabbit Uncle & Dog Keybinds. - raven (https://github.com/hannibal002/SkyHanni/pull/1907)
+ Added a distinct sound for the special stray rabbit. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1913)
+ Added collected egg location API import. - appable (https://github.com/hannibal002/SkyHanni/pull/1972)
    + Open your NEU profile viewer and click the chat message to load data.
+ Added Carnival lines to the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1995)
+ Made the unclaimed eggs GUI clickable. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1934)
+ Improve punctuation in the rabbit barn full/almost full messages. - Luna (https://github.com/hannibal002/SkyHanni/pull/2146)
+ Separate option for Unclaimed Eggs warning while "Busy". - Obsidian (https://github.com/hannibal002/SkyHanni/pull/2091)
    + In the Rift, Kuudra, Dungeons, etc.
+ Hoppity and Chocolate reminders/warnings now appear less frequently and are less intrusive. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2196)
+ Add a configuration option to allow Time Tower to appear in the Chocolate Factory next upgrade list. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2142)
+ Changed click-to-warp toggle to also work for unclaimed eggs display. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2211)
+ When Chocolate Factory prestige is maxed, the display shows time until the next max milestone instead. - seraid (https://github.com/hannibal002/SkyHanni/pull/2226)
+ Added an option to choose the color of Hoppity Egg Waypoints. - jani (https://github.com/hannibal002/SkyHanni/pull/2259)
+ Added rabbit rarity to Compact Hoppity messages. - not_a_cow (https://github.com/hannibal002/SkyHanni/pull/2364)

#### Mining Improvements

+ Highlight Treasure Hoarders during Treasure Hoarder Puncher commissions. -
  Luna (https://github.com/hannibal002/SkyHanni/pull/1852)
+ Added Mining Event Icons. Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1305)
+ Moved all SkyMall chat message hiders under the main SkyMall toggle. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1971)
+ Powder Tracker: Auto-detect whether Great Explorer is maxed. - Luna (https://github.com/hannibal002/SkyHanni/pull/2032)
    + The configuration option to manually specify this has been removed.
+ Added different display options for mining powder in the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1442)
+ Added Left Click on Royal Pigeon for next spot in Tunnel Maps. - Empa (https://github.com/hannibal002/SkyHanni/pull/2181)
+ Overhauled the Powder Mining chat filter to be much more customizable. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2152)

#### Commands Improvements

+ Updated /shcommands. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1720)
    + Now has pages for better navigation.
    + Improved visual appearance.

#### Dungeon Improvements

+ Changed reminders to show at the end of a dungeon run. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1917)
    + E.g. Composter empty, Hoppity Eggs ready, New Year Cake, etc.

#### Chat Improvements

+ Added Pet Rarity to Oringo Abiphone messages. - Empa (https://github.com/hannibal002/SkyHanni/pull/1862)
+ Improved many chat message hover descriptions. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1919)

#### Inventory Improvements

+ Improved Bazaar re-buy order helper to also search in the Bazaar upon chat message click. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1946)
+ Clarified the maximum clicks message for experiments. - Luna (https://github.com/hannibal002/SkyHanni/pull/1963)
+ Improved Attribute Prices in Estimated Item Value. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2020)
    + Ignoring irrelevant attributes (Resistance, Speed, Experience, etc).
    + No longer counting attribute combos or single attribute prices when cheaper than the base item price.
+ Added option to choose Bazaar price for Copper price. - Trîggered (https://github.com/hannibal002/SkyHanni/pull/2030)
+ Added Custom Wardrobe Slot Keybinds. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2105)
    + These function independently of NEU. NEU's keybinds will not work in the Custom Wardrobe.
+ Added an option to show item tooltips in the Custom Wardrobe only when holding a keybind. - Empa (https://github.com/hannibal002/SkyHanni/pull/2078)
+ Add support for Estimated Item Value to Custom Wardrobe. - Empa (https://github.com/hannibal002/SkyHanni/pull/2080)
+ Update Estimated Item Value calculation. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2180)
    + Change attribute prices from composited to the lowest bin, and added an option to use composited prices again.
    + Add Toxophilite.
    + Add option to choose whether to use Bazaar buy order or instant buy prices.
    + Calculating with tier 5 prices for Ferocious Mana, Hardened Mana, Mana Vampire and Strong Mana.
+ Added reset buttons for colors and spacing in the Custom Wardrobe config. - raven (https://github.com/hannibal002/SkyHanni/pull/2120)
+ Added an option for the Estimated Chest Value display to also work in your own inventory. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2218)
+ Added SkyBlock Level number in SB Menu to Item Number config. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/2172)
+ Added Divan's Powder Coating to the Estimated Item Value feature. - jani (https://github.com/hannibal002/SkyHanni/pull/2317)
+ Added support for Chocolate Milestones in Custom Reminder. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2334)
+ Clicking on the chat message when you try to open /cf without a rabbit pet will now open /pets. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2333)
+ Improved Wardrobe Keybinds when using favorite mode. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2331)

#### Fishing Improvements

+ Added option to show the name of the sea creature in the title notification. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1858)
+ Clarified chat prompt to load trophy fishing data from NEU PV. - Luna (https://github.com/hannibal002/SkyHanni/pull/2139)
+ Improved Barn Fishing Timer. - Empa (https://github.com/hannibal002/SkyHanni/pull/1960)
    + Modified the warning thresholds for Crimson Isle and Crystal Hollows.
    + The timer now only takes into account the user's own sea creatures.
    + Added a warning when reaching a certain number of sea creatures.

#### Diana Improvements

+ Reduced the frequency of Diana sound guess hints while the feature is working correctly. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1954)

#### Crimson Isle

+ Crimson Isle Reputation Helper now warns when the Faction Quest Widget is disabled in the tab list. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1977)

#### Combat Improvements

+ Improved Summoning Mob features. - Empa (https://github.com/hannibal002/SkyHanni/pull/2092)
    + Added a separate toggle to enable/disable summoning chat messages.

#### Misc Improvements

+ Added a toggle for 24-hour time. - seraid (https://github.com/hannibal002/SkyHanni/pull/1804)
+ Improved the NEU missing/outdated warning. - Luna (https://github.com/hannibal002/SkyHanni/pull/1847)
    + Link to the latest GitHub release instead of 2.2.0.
    + Also link to Modrinth.
+ Improved performance with Slayer Items on Ground and Fished Item Names. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1875)
+ Updated /shclearminiondata to remove only bugged minions nearby, not all minions. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1951)
+ Added information about the current Perkpocalypse Mayor's perks to the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2003)
+ Added support for Fancy Contributors in nametags. - Empa (https://github.com/hannibal002/SkyHanni/pull/1687)
+ Improved overall performance slightly by about ~1%. - nea (https://github.com/hannibal002/SkyHanni/pull/2033)
+ Improved Hypixel server detection. - Luna (https://github.com/hannibal002/SkyHanni/pull/2064)
+ Added Carnival lines to the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1995)
+ Added an exclude dungeon toggle to hide far entities. - Iceshadow (https://github.com/hannibal002/SkyHanni/pull/2036)
+ Improved Custom Wardrobe outline color. - Empa (https://github.com/hannibal002/SkyHanni/pull/2079)
+ Improved phrasing of config descriptions, enhanced overall readability, fixed typos, newlines, etc. - zapteryx (https://github.com/hannibal002/SkyHanni/pull/2067)
+ Add some OneConfig mods to the quick mod switcher. - nea (https://github.com/hannibal002/SkyHanni/pull/2155)
+ Add config option to maintain "current" sound settings levels while warning sounds are played. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2129)
+ Added a reset button in the config for the appearance and event priority of the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2141)
+ Reverted item source names to shorter format. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2233)
+ Rounded corner smoothness now applies to custom Scoreboard textures. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2228)
+ Reduced the frequency of "tab list not found" error messages. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2266)
+ Hide Beacon Power display while on a Bingo profile. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2286)
+ Added the current Minister's name and perks to the Mayor Display on the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2313)
+ Hide repo errors in chat. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2338)
    + Errors will be displayed directly in the GUI when necessary, making them less intrusive.
+ Added a "Mods Folder" button to the NEU error message. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2362)

### Fixes

#### Mining Fixes

+ Fixed Tunnel Maps GUI position not saving. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1882)
+ Fixed Glacite Walkers not being highlighted during commissions. -
  Luna (https://github.com/hannibal002/SkyHanni/pull/1850)
+ Fixed warping to Base in Glacite Tunnels when pressing the keybind while inside any GUI. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1911)
+ Fixed Custom Scoreboard error in Mineshaft. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1909)
+ Fixed HotM error with Blue Cheese Goblin Omelette. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1953)
+ Fixed mining events not showing up in the Mineshaft. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1952)
+ Fixed Custom Scoreboard error in the mines. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1947)
+ Fixed a typo in Tunnels Maps. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1883)
+ Fixed error with SkyMall with Titanium buff. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1967)
+ Fixed swapped mining event display icons. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1958)
+ Fixed Area Walls being broken in Nucleus. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1994)
+ Consider Y=64 part of Magma Fields. - Luna (https://github.com/hannibal002/SkyHanni/pull/2029)
    + Hypixel quietly changed this in or around SkyBlock 0.20.1.
+ Fixed the order of Goblin Eggs in the Powder Tracker. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2138)
+ Fixed ore event sometimes getting stuck. - Empa (https://github.com/hannibal002/SkyHanni/pull/2115)
+ Fix incorrect powder costs and powder spent amounts being displayed for some HOTM perks. - Luna (https://github.com/hannibal002/SkyHanni/pull/2145)
+ Fix Powder Percentage Compatibility Issue with ColeWeight. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2186)
+ Fixed Powder detection from chat messages. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2207)
+ Ignore Jasper/Ruby Crystal price. - Luna (https://github.com/hannibal002/SkyHanni/pull/2235)
    + These crystals used to have an item form and may be sold on the Auction House for overinflated prices (e.g., over 900M for a Jasper Crystal), which can inflate values like Corpse Profit.
+ Fixed PowderTracker Pattern. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2240)
+ Fixed total powder amounts resetting when opening the SkyBlock Menu on a mining island. - Luna (https://github.com/hannibal002/SkyHanni/pull/2263)
+ Fixed detection of 2x Powder Event for Powder Tracker. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2157)
    + Now uses the Tab Widget.
+ Fixed TunnelMaps not resetting on lobby swap. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2275)
+ Fixed a rare error when detecting corpses in the Mineshaft. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2268)
+ Fixed incorrect maximum Powder value on HotM perks when disabled. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2269)
+ Fixed HOTM levels being saved incorrectly if the menu was opened with a Blue Egg Cheese Drill in hand. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2295)
+ Fixed Flawless Gemstones not being formatted correctly when the Powder Mining filter is enabled. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2281)
+ Fixed Peak of the Mountain always being disabled. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2282)
+ Fixed an issue where TunnelMaps could stop functioning. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2277)
+ Fixed blocks mined by Efficient Miner not being detected in the Pity Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/2307)
+ Fixed empty lines not being blocked by the Powder Mining filter. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2309)
+ Fixed a rare crash occurring on a mining island. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2354)

#### Garden Fixes

+ Fixed farming contests showing in the election GUI. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1871)
+ Fixed wrong icon for the rat crop type in Stereo Harmony display. -
  raven (https://github.com/hannibal002/SkyHanni/pull/1849)
+ Fixed /shcropgoal not working with double-name crops (wart, cocoa, cane). -
  L3Cache (https://github.com/hannibal002/SkyHanni/pull/1970)
+ Fixed Garden Visitor Drop Statistics not tracking new data. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1976)
    + This may also fix the display not showing up.
+ Fixed Non-Craftable Items breaking the Visitor Shopping List. - jani/hannibal2/nea (https://github.com/hannibal002/SkyHanni/pull/2019)
+ Fixed stats in visitor inventory not showing under certain circumstances. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2018)
+ Fixed calculation of universal farming fortune in /ff. - maxime-bodifee (https://github.com/hannibal002/SkyHanni/pull/1861)
    + Removed 100 base ff and added garden bonus from bestiary.
+ Fixed cake buff in /ff. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2034)
+ Fix an issue where the farming overtake ETA was being displayed as 60 times longer than it actually was. - Evhan_ (https://github.com/hannibal002/SkyHanni/pull/2158)
+ Fix overflow level up message being sent upon reaching crop milestone 46. - Luna (https://github.com/hannibal002/SkyHanni/pull/2161)
+ Fix Craftable! text not opening the recipe. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2183)
+ Fixed an error in the Farming ETA Duration calculation. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2217)
+ Fixed a bug where the fragged Daedalus Axe was not being detected as a farming tool. - Luna (https://github.com/hannibal002/SkyHanni/pull/2212)
+ Fixed Fine Flour exportation not getting detected from chat messages for use in FF Guide. - Luna (https://github.com/hannibal002/SkyHanni/pull/2215)
+ Fixed "Craftable!" text not opening the recipe. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2219)
+ Fixed favorite pets not being detected by FF Guide. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2202)
+ Fixed farming weight estimate being much too large. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2230)
+ Fixed Jacob's Contest FF needed display not showing. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2262)
+ Fixed Dicer Tracker Reset Button misalignment. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2248)
+ Fixed errors when selecting Fine Flour with the Sprayonator. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2251)
+ Fixed typos in the FF guide. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2273)
+ Fixed command sent in chat for Farming Weight Display. - not-a-cow (https://github.com/hannibal002/SkyHanni/pull/2303)
+ Fixed visitor shopping list with AH items. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2297)
+ Fixed Zorro's Cape not working with Green Thumb in the FF stats breakdown in item lore. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2299)
+ Fixed Composter Overlay calculating incorrectly while Composter Display is disabled. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2294)
+ Fixed own garden not being detected when swapping islands from a guesting server. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2355)
+ Fixed a typo in the Farming Weight Display error message. - aphased (https://github.com/hannibal002/SkyHanni/pull/2376)
+ Fixed Finnegan still having the Farming Simulator perk and missing the Pest Eradicator perk. - Luna (https://github.com/hannibal002/SkyHanni/pull/2359)

#### Chocolate Factory & Hoppity Hunt Fixes

+ Fixed some Chocolate Factory issues caused by a Hypixel update. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1860)
+ Fixed a bug where the chocolate factory leaderboard position was not showing when the position had many digits. -
  sayomaki (https://github.com/hannibal002/SkyHanni/pull/1918)
+ Fixed compact chat sometimes breaking when obtaining legendary or higher tier rabbits. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1923)
+ Fixed Sigma Rabbit bonuses not counting in collection stats display. -
  appable (https://github.com/hannibal002/SkyHanni/pull/1925)
+ Fixed incorrect API name conversion for Fish the Rabbit. - appable (https://github.com/hannibal002/SkyHanni/pull/1975)
+ Fixed stray rabbit sound not playing for the Golden Rabbit. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1913)
+ Fixed compact chat sometimes breaking when obtaining legendary or higher tier rabbits. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2041)
+ Fixed highlight missing requirements even when Hoppity Rabbit is already found. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2101)
+ Fixed Compact Hoppity Chat not working with rabbits purchased from the Hoppity NPC. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2100)
+ Fixed a rare case where golden strays caused incorrect duplicate detection during Compact Chat for Hoppity's. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2090)
+ Fixed the custom reminder in the Chocolate Factory activating when using the Time Tower charge. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2126)
+ Fixed compact Hoppity chat breaking with special mythic rabbits. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2093)
+ Fix opening Chocolate Factory without Mythic Rabbit Pet not being blocked when using the `/factory` command. - Luna (https://github.com/hannibal002/SkyHanni/pull/2154)
+ Fix Time Tower "One charge will give" calculation not accounting for the player having the Mu Rabbit in their collection. - Luna (https://github.com/hannibal002/SkyHanni/pull/2165)
+ Fix typo in Chocolate Factory custom reminder config. - aphased (https://github.com/hannibal002/SkyHanni/pull/2169)
+ Fix the warning messages for the rabbit barn being full/almost full being reversed. - Luna (https://github.com/hannibal002/SkyHanni/pull/2146)
+ Allow seeing the unclaimed eggs GUI while "Busy". - Obsidian (https://github.com/hannibal002/SkyHanni/pull/2091)
    + In the Rift, Kuudra, Dungeons, etc.
+ Fix detection for supreme chocolate bar completion. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2194)
+ Fixed the detection of production time from Fish the Rabbit. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2225)
+ Fixed buying items sometimes triggering compact Hoppity messages. - sayomaki (https://github.com/hannibal002/SkyHanni/pull/2201)
+ Fixed Hoppity Chat Compact not working outside of Hoppity's event for Abiphone users who have Hoppity as a contact. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2250)
+ Fixed stray rabbit production time tooltip display. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2239)
+ Fixed Hoppity custom goals being off by one level. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2280)
+ Fixed Custom Reminder not working in the Chocolate Shop. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2334)
+ Fixed a warning about space in the Chocolate Factory appearing even when the barn was already large enough to store all rabbits. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2356)
+ Fixed the Hoppity Menu not being detected when it only had one page. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2368)

#### Custom Scoreboard Fixes

+ Fixed scoreboard player count not displaying in dungeons. -
  ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1894)
+ Fixed showing "No Magical Power detected" message multiple times. - Empa (https://github.com/hannibal002/SkyHanni/pull/2065)
+ Fixed unknown lines not appearing instantly in the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2063)
+ Fixed Anniversary Line not being detected in Custom Scoreboard. - Empa, j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2058)
+ Fixed Perkpocalypse Mayor's time being longer than Jerry's time in office. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2045)
+ Fixed Unknown lines in the Custom Scoreboard. - Empa (https://github.com/hannibal002/SkyHanni/pull/2119)
+ Fixed broken event start time for upcoming events in the Custom Scoreboard. - Luna (https://github.com/hannibal002/SkyHanni/pull/2113)
+ Fixed the New Year Event missing in the Custom Scoreboard.. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2122)
+ Fix Custom Scoreboard Error while voting. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2184)
+ Fix Custom Scoreboard Error during the Winter Event. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2192)
+ Fix Custom Scoreboard Error in Kuudra. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2176)
+ Fix New Mayor perks not showing up on the Scoreboard. - Stella (https://github.com/hannibal002/SkyHanni/pull/2187)
+ Fixed Custom Scoreboard Error during Carnival. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2245)
+ Fixed 2 Custom Scoreboard Errors while in Kuudra. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2236)
+ Fixed Scoreboard Reset not updating the draggable list. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2252)
+ Fixed a scoreboard error message when a Hypixel bug shows a negative number of magma cubes remaining in the scoreboard. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2267)
+ Fixed Custom Scoreboard Error when having more than 1000 medals. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2319)
+ Fixed a Custom Scoreboard error during the Winter Event. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2345)
+ Fixed getting mayor/minister information for Custom Scoreboard. - Luna (https://github.com/hannibal002/SkyHanni/pull/2343)

#### Dungeon Fixes

+ Fixed healer orb pickup hider. - Mikecraft1224 (https://github.com/hannibal002/SkyHanni/pull/1750)
+ Fixed Dungeon Rank Tab list color not showing the names of players with YouTube/Admin rank. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1878)
+ Fixed Dungeons in DiscordRPC not showing the elapsed time. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2089)
+ Fixed multiple empty lines appearing in Custom Scoreboard while in Dungeons. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2082)
+ Fixed Livid Finder not working with Magenta Livid. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2086)
+ Fixed the dungeon finder item lore displaying on other items in the inventory. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2298)

#### Commands Fixes

+ Fixed typo in /shclearkismet command. - fahr-plan (https://github.com/hannibal002/SkyHanni/pull/1912)
+ Fixed 'viewrecipe' lowercase not working. - Obsidian + hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1939)
+ Fixed rare cases where queued /gfs didn't work. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1999)
+ Fixed an additional empty /shcommands page from appearing. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2287)

#### Fishing Fixes

+ Fixed Thunder and Jawbus not being highlighted by Highlight Rare Sea Creatures. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1858)
+ Fixed Trophy Fish Display not working for stranded players. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1966)
+ Fixed Sea Creature highlight not working when the damage indicator was enabled. - Empa (https://github.com/hannibal002/SkyHanni/pull/1985)
+ Fixed Fishing Bait warnings not working. - Empa (https://github.com/hannibal002/SkyHanni/pull/2135)
+ Fixed a rare double hook detection issue. - appable (https://github.com/hannibal002/SkyHanni/pull/2125)
+ Fixed Commission Mob Highlighter incorrectly highlighting Deep Sea Protectors as Automatons. - Luna (https://github.com/hannibal002/SkyHanni/pull/2130)
+ Fixed Squid Pet counting towards the Fishing Timer. - Empa (https://github.com/hannibal002/SkyHanni/pull/1960)
+ Fixed an error during fishing. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2350)
+ Fixed error messages appearing while fishing. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2370)
+ Fixed obfuscated trophy fish names being unreadable in the Fishing Profit Tracker. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2363)
+ Fixed item names appearing in incorrect spots while holding a fishing rod. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2371)
+ Fixed the fishing timer being incorrect for some sea creatures. - Empa (https://github.com/hannibal002/SkyHanni/pull/2365)

#### Performance Fixes

+ Fixed small memory leaks when staying in one island for extended periods. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1890)

#### Diana Fixes

+ Fixed Inquisitor detection for sharing. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1949)
+ Fixed Diana errors being shown when the feature is not enabled. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1942)

#### Rift Fixes

+ Fixed rift slayer warning showing while hitting Oubliette Guard. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1950)
+ Fixed an error when killing Splatter Cruxes in the Rift. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2349)

#### Inventory Fixes

+ Fixed estimated item value not showing in NEU PV. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1955)
+ Fixed showing the favorite wardrobe slot background color while in "Only Favorites" mode. - Empa (https://github.com/hannibal002/SkyHanni/pull/2072)
+ Fixed incorrect stripping of some prefixes from non-Kuudra armor items in the estimated item value calculation. - Luna (https://github.com/hannibal002/SkyHanni/pull/2057)
    + This broke other items, like Hot Bait.
+ Fixed a Custom Wardrobe error when retrieving an item price. - j10a1n15, Empa (https://github.com/hannibal002/SkyHanni/pull/2123)
+ Double-Bit now works in reforge helper . - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2182)
+ Fix favorited pets not being detected. - Empa (https://github.com/hannibal002/SkyHanni/pull/2188)
+ Fix wrong item prices in Estimated Item Value when the craft price is higher than the NPC price and the item is soulbound. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2173)
    + E.g. for Gillsplash (fishing equipment).
+ Fix Custom Wardrobe Keybinds not working. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2198)
+ Fixed price calculation for Kuudra keys. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2205)
+ Page scrolling now works correctly in the museum. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2208)
+ Fixed detecting Hot Potato Books inside the Community Project as potatoes. - Empa (https://github.com/hannibal002/SkyHanni/pull/2214)
+ Fixed price source inconsistencies (Bazaar sell/buy and NPC prices). - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2221)
+ Fixed Carnival Masks not being detected by Reforge Helper. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2244)
+ Fixed a typo in the Item Pickup Log config. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2271)
+ Fixed a bug in SkyHanni's Time Held in Lore with Coca Truffle when using Odin. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2300)
+ Fixed sack changes appearing with a delay in the Item Pickup Log. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2292)
+ Fixed Item Pickup Log error with Wisp Potion. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2291)
+ Fixed Carnival Masks not being clickable in `/equipment`. - Luna (https://github.com/hannibal002/SkyHanni/pull/2293)
+ Fixed Bobbin' Time not being detected correctly. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2301)
+ Fixed lore not showing on enchanted books with compressed format. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2301)
+ Fixed the incorrect "This item has no attributes" message appearing in the Attribute Fusion menu for some items. - Luna (https://github.com/hannibal002/SkyHanni/pull/2304)
+ Fixed shift-clicking items in GUIs triggering the Item Pickup Log. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2305)
+ Fixed the Master Mode Dungeon stack size display in the party finder menu. - fazfoxy (https://github.com/hannibal002/SkyHanni/pull/2316)
+ Fixed the Item Pickup Log detecting "Magical Map" in dungeons when using Skytils. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2351)
+ Fixed Attribute Shards not displaying their names in the Chest Value feature. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2363)
+ Fixed Bazaar item movement being counted towards Item Trackers. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2366)
+ Fixed the Item Pickup Log showing incorrect icons for some items when collected via sacks. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2373)

#### Combat Fixes

+ Fixed summons not being removed from Summoning Mob Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1964)
+ Fixed area mini boss spawn durations below 1 second not displaying. - Empa (https://github.com/hannibal002/SkyHanni/pull/2110)
+ Fixed summons death messages getting blocked even when the feature is disabled. - Daveed (https://github.com/hannibal002/SkyHanni/pull/2132)
+ Fixed slayer quest warning sometimes showing incorrectly. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2232)
+ Fixed the "wrong slayer" alert being shown when attacking Bladesoul. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2367)

#### Chat Fixes

+ Fixed stash getting detected as a private message. - sayomaki (https://github.com/hannibal002/SkyHanni/pull/2060)
+ Fix Guild Rank formatting having an extra space. - Empa (https://github.com/hannibal002/SkyHanni/pull/2191)

#### Crimson Fixes

+ Fixed Pablo Helper not functioning when called via Abiphone. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2288)
+ Fixed reputation helper keybind activating while inside GUIs. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2284)

#### Misc Fixes

+ Fixed LorenzToolTipEvent triggering when not actually hovering over an item. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1920)
+ Fixed the GUI editor opening unintentionally. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1973)
+ Fixed movable hotbar conflicts with some mods. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1965)
+ Fixed crashes caused by tooltips. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1986)
+ Fixed config reset on encountering unknown enum values in the config. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1990)
+ Config no longer resets when an incorrect value is entered. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1979)
+ Config no longer resets when downgrading versions. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1979)
+ Fixed accidental hiding of boss bars. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1980)
+ Fixed Account Upgrade Reminder feature with simultaneous account and profile upgrades. - appable (https://github.com/hannibal002/SkyHanni/pull/2007)
+ Fixed crash if a key code is set to a non-standard key. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2066)
+ Fixed corrupted config files not being reset to the default state. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2055)
+ Fixed an error with the chum bucket hider. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2043)
+ Fixed stranded highlighted placeable NPCs. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2071)
+ Fixed visual words import from SBE with special characters. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2085)
+ Fixed waypoint text being hidden when more than 80 blocks away. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2103)
+ Fixed durations displaying with milliseconds. - Empa (https://github.com/hannibal002/SkyHanni/pull/2095)
+ Fixed getting security error when download from GitHub. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2111)
+ Fixed Trevor Tracker not working when Trevor Solver is disabled. - appable (https://github.com/hannibal002/SkyHanni/pull/2116)
+ Fixed Hypixel detection on non-Hypixel servers. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/2131)
+ Fix the skill progress ETA XP/hour being doubled. - Evhan_ (https://github.com/hannibal002/SkyHanni/pull/2163)
+ Fix launchers showing SkyHanni as LibNinePatch. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2147)
+ Fix "/worldedit right" setting the left position. - HiZe (https://github.com/hannibal002/SkyHanni/pull/2179)
+ Fix a bug in the Hypixel detection. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2193)
+ Fix random Stack Overflow Errors. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2199)
+ Fixed a typo in /shcommands. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/2223)
+ Fixed multiple features not showing a duration when it was less than one second. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2261)
+ Fixed some incorrect boxes in the GUI editor. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2260)
+ Fixed chroma preview always being white. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2302)
+ Fixed Contributor name tag flickering when using Patcher. - nea (https://github.com/hannibal002/SkyHanni/pull/2296)
+ Fixed Discord Rich Presence dynamic fallback. - NetheriteMiner (https://github.com/hannibal002/SkyHanni/pull/2279)
    + Placeholder messages that should not be visible will no longer appear.
+ Fixed pet detection with newer NEU versions. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2358)
+ Fixed Item Trackers displaying the incorrect NPC price. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2352)
+ Fixed players with similar names to friends being incorrectly marked as friends in the tab list. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2353)
+ Fixed color codes in Discord RPC pet status. - NetheriteMiner (https://github.com/hannibal002/SkyHanni/pull/2344)
+ Disabled the SkyHanni GUI's hover function while in other mods' screens. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2374)
+ Fixed the Trapper Helper checking for the wrong Finnegan perk. - Luna (https://github.com/hannibal002/SkyHanni/pull/2359)
+ Fixed items with no NPC sell price being counted as -1 coins instead of 0 coins. - Luna (https://github.com/hannibal002/SkyHanni/pull/2360)

### Technical Details

+ Use more simple time mark. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1777)
+ Removed all uses of ChatUtils.sendCommandToServer, replacing with HypixelCommands.. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1769)
+ Use fewer deprecated LorenzVec functions. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1556)
+ Cleanup of duplicate code in EntityUtils. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1556)
+ Move regex and pattern operations to RegexUtils. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1854)
+ Added Track Particles command. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1787)
+ Fixed stacked mob highlights. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1856)
+ SkillExpGainEvent now uses SkillAPI data. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1788)
+ Added InquisitorFoundEvent event. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1949)
+ Added annotation to automatically load modules. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1904)
    + Just add the @SkyHanniModule annotation to an object to auto load it.
+ Modified removeColor for visual formatting consistency. - appable (https://github.com/hannibal002/SkyHanni/pull/1824)
    + Keeps formatting consistency in the presence of color codes acting as implicit reset codes.
+ Added RepoPattern.list. - nea (https://github.com/hannibal002/SkyHanni/pull/1733)
    + A way to store x amount of patterns.
+ Added unit test for repo pattern lists. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1733)
+ Added RepoPattern.exclusiveGroup. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1733)
    + Reserves a key namespace, that is only accessible from this group.
+ Increased usage of seconds passed. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1899)
+ Refactored /ff. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/873)
+ Added Perkpocalypse Mayor to MayorAPI. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2003)
+ Created FmlEventApi to hold Forge events before dispatching them as SkyHanniEvents. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1986)
+ Ensured correct config version when downgrading the mod. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1990)
+ Changed CachedItemData to use SimpleTimeMark. - nea (https://github.com/hannibal002/SkyHanni/pull/2006)
    + This is done via an in-Kotlin delegate constructor, since calling functions with default arguments that have an inline class type from Java is not stable.
+ Added SimpleStringTypeAdapter. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1979)
+ Added SkippingTypeAdapterFactory. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1979)
+ Cleaned up ConfigManager. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1979)
+ Moved json package inside the utils package. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1979)
+ Added Tab Widget abstraction. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1150)
+ Added EntityDisplayNameEvent. - Empa (https://github.com/hannibal002/SkyHanni/pull/1687)
+ Make build fail when event functions have wrong annotation. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2024)
+ Begin transitioning to the new event system. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2023)
+ Added custom event bus. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2008)
    + Added live plugin to show when an event method is missing its annotation.
+ Changed Java to Kotlin for repository files. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1543)
+ Made in-game date display use a pattern instead of repository. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1543)
+ All /gfs calls go through gfs API now. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1999)
+ Made ItemHoverEvent be called earlier. - Vixid (https://github.com/hannibal002/SkyHanni/pull/2018)
+ Removed deprecated bazaar variables. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1987)
+ Removed another deprecated function. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1956)
+ Used event.cancel() over event.isCanceled = true for LorenzEvents. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1915)
+ Converted classes to objects, then used annotation. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1982)
+ Added annotations to objects. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1974)
+ Added module plugin. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1974)
+ Moved the drawWaypointFilled function to RenderUtils. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1930)
+ Added mixin search scope. - nea (https://github.com/hannibal002/SkyHanni/pull/2033)
+ Changed `.gitignore` so that subdirectories of the `.idea` folder can be manually unignored. - nea (https://github.com/hannibal002/SkyHanni/pull/2033)
+ Fixed infinite scrolling for scrollables. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2053)
+ DevTool Graph Editor. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1454)
+ Added player, drawInsideRoundedRecWithOutline & doubleLayered Renderable. - j10a1n15, Empa, Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2039)
+ Added render Renderables method, which only takes in one renderable. - Empa (https://github.com/hannibal002/SkyHanni/pull/2039)
+ Added method to remove enchants from any item. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2039)
+ Added method to click on any slot in any window. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2039)
+ Added "addToGUIEditor" param to render Renderables method. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2039)
+ Added the ability to specify a SkyHanniModule to only load in dev. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/2011)
+ Migrated long to SimpleTimeMark in Storage if they are time marks. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2038)
+ Added `HotmData.isMaxLevel`, which also accounts for Blue Cheese. - Luna (https://github.com/hannibal002/SkyHanni/pull/2032)
+ Code cleanups for forEach loops. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1725)
+ Removed the deprecated TimeUtils function and cleaned up the newer one. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1898)
+ Fully utilized the module annotation for feature objects. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1983)
+ Added getPerkByName(name) and calculateTotalCost(level) to HotmAPI. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1460)
+ New Scoreboard lines are added automatically from remote. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2068)
    + The pattern still needs to be added to the list in development.
+ Improved code around Kuudra armor sets in estimated item value checking. - Luna (https://github.com/hannibal002/SkyHanni/pull/2057)
+ Cleaned up some scoreboard event handling. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1940)
+ Refactored Summoning Mob Manager to use Mob Detection. - Empa (https://github.com/hannibal002/SkyHanni/pull/2092)
+ Added true color support to text rendering. - nea (https://github.com/hannibal002/SkyHanni/pull/1397)
    + This is separate from chroma.
    + This is achieved by using §#§0§0§0§f§f§f§/.
    + Simply use the ExtendedChatColor class.
+ Refactored Area Mini boss Features to use mob detection. - Empa (https://github.com/hannibal002/SkyHanni/pull/2095)
+ Added OreBlock and OreType. - Empa (https://github.com/hannibal002/SkyHanni/pull/1655)
+ Added OreMinedEvent. - Empa (https://github.com/hannibal002/SkyHanni/pull/1655)
    + Provides information on the original block mined, and any other blocks mined by sources like Efficient Miner, Mole, etc.
+ Improved performance with Commission Color Blocks. - Empa (https://github.com/hannibal002/SkyHanni/pull/1655)
+ Fixed HotmAPI saving incorrect level values when using Blue Egg. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2097)
+ Fixed the interest widget and added a scrap widget. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2094)
+ Made SkyHanni into a Mod. - hannibal2 + nea (https://github.com/hannibal002/SkyHanni/pull/2099)
+ Fixed illegal mob detection with rideable pets. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2010)
+ Removed yet another deprecated function. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/2021)
+ Created and implemented PlayerDeathEvent. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1544)
+ Added a list of named nodes for the graph editor. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2059)
+ Added debug command /shtestisland. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2107)
    + Simulate being on a SkyBlock island.
+ Fixed a ConcurrentModificationException in RepoPatternManager during tests. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2098)
+ Updated the shader enabling system to work with enums instead of shader names. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2035)
+ Add DragNDrop. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1864)
    + Handler for drag and drop behavior for renderables.
+ Add SkyblockStats. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1437)
+ Add ReforgeAPI. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1437)
+ Switch some stuff from TabListUpdate to TabWidgetUpdate. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1240)
+ Add carnival mask to item category. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2189)
+ Fix pattern for Dragon Widget. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2190)
+ Changed the readme.md file. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2174)
+ Add Mob Detection First Seen Event. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2096)
+ Mob Detection now runs every tick instead of every other tick. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2096)
+ RepoPattern locale loading now works inversely for the development environment. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2069)
+ Move Hoppity Rabbit chat patterns to the eggs manager. - sayomaki (https://github.com/hannibal002/SkyHanni/pull/2144)
+ Removed redundant `lowName` property from HotmAPI.Powder. - Luna (https://github.com/hannibal002/SkyHanni/pull/2128)
+ Made RecalculatingValue a ReadOnlyProperty. - Empa (https://github.com/hannibal002/SkyHanni/pull/2151)
+ Visitor rarities from the repository are now exposed to the code in a cleaner way. - Luna (https://github.com/hannibal002/SkyHanni/pull/2220)
+ Added automatic PR labeling for bug fixes. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2203)
+ Deprecated some methods in RegexUtils, and added some others. - Empa (https://github.com/hannibal002/SkyHanni/pull/2160)
+ Fixed ScoreboardData Line formatting with doubled color codes. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2264)
+ Changed "colour" to "color" internally, everywhere. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2258)
+ Added renderRenderableDouble. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2260)
    + A direct replacement for renderStringsAndItems.
+ Changed detecting active mayor to no longer use the number of votes. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2227)
+ Fixed wrong color for Jasper gemstone in OreBlock API. - Luna (https://github.com/hannibal002/SkyHanni/pull/2257)
+ Added DYE to Item Category. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/2315)
+ Added /shdebugprice command. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2289)
+ Cache rewardPatterns in the powder chat filter. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2276)
+ Added Minister data to the MayorAPI. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/2313)
+ Added ItemPriceUtils and moved all item price functions into it. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2290)
+ Added a preprocessor to prepare for 1.21. - nea (https://github.com/hannibal002/SkyHanni/pull/2283)
    + This will require a minor adjustment in how the Minecraft client is launched.
+ Deleted an unnecessary function in Garden Crop Milestone. - notafrogo (https://github.com/hannibal002/SkyHanni/pull/2339)
+ Removed repository item errors. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2338)
    + Instead of stack traces for "no item name for internal name", "no item stack for internal name", and "no internal name for item stack", we now return the internal name in red, the internal name `MISSING_ITEM`, or a barrier item instead.
    + Command /shdebug lists all failed item names.
    + Debug chat still logs the errors.
    + Shows a popup (currently disabled).

### Removed Features

+ Removed Mineshaft from mining event display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/2083)
+ Remove the original Time Tower Reminder feature. - appable (https://github.com/hannibal002/SkyHanni/pull/2133)
    + Hypixel now correctly calculates Time Tower offline.
+ Removed the /shlimbo command. - nopo (https://github.com/hannibal002/SkyHanni/pull/2231)
    + Hypixel added /limbo.

## Version 0.25

### New Features

#### Garden Features

+ Added Pest Profit Tracker. - Empa (https://github.com/hannibal002/SkyHanni/pull/1321)
+ Added Open On Elite. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1185)
    + Allow opening farming contest stats on elitebot.dev by pressing a keybind + mouse click onto a contest item.
    + Works inside the menus Jacob's Farming Contest, Your Contests, and SkyBlock Calendar.
+ Added Visitor's Logbook Stats. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1287)
    + Show all your visited/accepted/denied visitors stats in a display.
+ Added Stereo Harmony Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1324)
    + Options to show/hide boosted crop and pest icons.
+ Added Super Craft button to visitors for ease of access. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1173)
    + Checks if you have enough materials to craft the items and depending on that shows the button or not.
+ Added overflow Garden crop milestones. - Alexia Luna & HiZe (https://github.com/hannibal002/SkyHanni/pull/997)

#### Diana Features

+ Added customizable Inquisitor Highlight color. - Empa (https://github.com/hannibal002/SkyHanni/pull/1323)
+ Added mobs since last Inquisitor to Mythological Creature Tracker. -
  CuzImClicks (https://github.com/hannibal002/SkyHanni/pull/1346)

#### Mining Features

+ Added Area Walls. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1266)
    + Show walls between the main areas of the Crystal Hollows.
    + Option to show the walls also when inside the Nucleus.
+ Fossil Excavator Solver. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1427)
    + Shows where to next click for optimal chance of solving the fossil. If there is a fossil this will find it within
      18 moves.
+ Added Excavation Profit Tracker. - hannibal2 + Empa (https://github.com/hannibal002/SkyHanni/pull/1432)
    + Count all drops you gain while excavating in the Fossil Research Center.
    + Track Glacite Powder gained as well (no profit, but progress).
    + Track Fossil Dust and use it for profit calculation.
+ Added Mining Notifications. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1429)
    + Mining events, including Mineshaft spawning, Suspicious Scrap drops, and Cold going above a threshold.
+ Added Profit Per Excavation. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1439)
    + Show profit/loss in chat after each excavation.
    + Also include breakdown information on hover.
+ Added a textured cold overlay to Glacite Tunnels. - j10a1n15, Empa (https://github.com/hannibal002/SkyHanni/pull/1438)
    + Change at what cold level the texture should appear.
+ Added option to highlight own Golden/Diamond Goblin. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1466)
+ Added Glacial Powder as stack size in the Fossil Excavator. -
  jani (https://github.com/hannibal002/SkyHanni/pull/1458)
+ Click to get an Ascension Rope from sacks in the Mineshaft. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1542)
+ Added Tunnel Maps. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1546)
    + Provides a way to navigate inside the new Glacite Tunnels.
+ Added Commissions Blocks Color. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1701)
    + Highlights blocks required for commissions in a custom color.
    + Greys out other blocks for clarity.
    + Works in the Glacite Tunnels and Crystal Hollows.
+ Profit Per Corpse. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1734)
    + Displays profit/loss in chat after looting a corpse in the Mineshaft.
    + Includes a breakdown of information on hover.

#### Dungeon Features

+ Added SA Jump Notification. - CarsCupcake (https://github.com/hannibal002/SkyHanni/pull/852)
    + Warn shorty before a Shadow Assassin jumps to you in dungeons.
+ Added Low Quiver Reminder at the end of Dungeon/Kuudra run. - Empa (https://github.com/hannibal002/SkyHanni/pull/1190)
+ Added notifications for architect on puzzle fail. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1197)
    + Shows Title.
    + Shows button in chat to retrieve from sack.
    + Only works when having enough Architect First Drafts in the sack.
+ Added dungeon hub race waypoints. - seraid (https://github.com/hannibal002/SkyHanni/pull/1471)
    + Only works for Nothing; No return races.
+ Added the ability to hide solo class, solo class stats and fairy dialogue chat messages in Dungeons. -
  raven (https://github.com/hannibal002/SkyHanni/pull/1702)

#### Rift Features

+ Added Vermin Highlight. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1457)
    + Highlights Vermin mobs in the West Village in the Rift.
    + Change Highlight color.

#### Config Features

+ Added a link from HUD elements to config options. - nea (https://github.com/hannibal002/SkyHanni/pull/1383)
    + Simply right-click a HUD element in the HUD editor to jump to its associated options.
    + Does not yet work on every GUI element. Wait for the missing elements in the following betas.

#### Combat Features

+ Added Quiver Display. - Empa (https://github.com/hannibal002/SkyHanni/pull/1190)
    + Only shows the type of arrow when wearing a Skeleton Master Chestplate.
+ Added an option to highlight Zealots holding Chests in a different color. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1347)

#### Chat Features

+ Added party chat commands. - nea (https://github.com/hannibal002/SkyHanni/pull/1433)
    + Added `!pt` (and aliases) as a command that allows others to transfer the party to themselves.
    + Added `!pw` (and aliases) as a command that allows others to request a warp.
+ Added Option to reorder or hide every part of a player chat message. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + Parts to move around: SkyBlock Level, Emblem, player name, guild rank, private island rank, crimson faction, iron
      man mode, bingo level and Private Island Guest.
    + Player messages impacted by this: all chat, party, guild, private chat, /show.
    + This might break hover/click on chat actions (Will be fixed later).
+ Added Hide Level Brackets. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + Hide the gray brackets in front of and behind the level numbers.
+ Added Level Color As Name. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + Use the color of the SkyBlock level for the player color.
+ Allow party members to request allinvite to be turned on. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1464)
    + Say !allinv in party chat for them to enable all invite.

#### Event Features

+ Added stuff for Chocolate Factory. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1434)
    + Show info about your Chocolate Factory.
    + Show which upgrades you can afford and which to buy.
    + Notification to click on rabbit in the inventory.
    + Notify you if you are close to having your rabbits crushed.
+ Added solver for the Egg Locator. - CalMWolfs & gravy (https://github.com/hannibal002/SkyHanni/pull/1434)
+ Added other stuff for Hoppity Event. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1434)
    + Display which eggs you have not gotten this SkyBlock day.
    + Waypoints for every egg on all islands.
    + Shared waypoints for found eggs.
+ Added Bits Gained Chat Message. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1487)
+ Added Hoppity rabbit collection stats summary. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1482)
+ Added Chocolate Factory Menu Shortcut (Hoppity Menu Shortcut). - raven +
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1583)
+ Added Tooltip Move. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1581)
    + Moves the tooltip away from the item you hover over while inside the Chocolate Factory.
+ Added Chocolate Factory Compact On Click. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1579)
+ Added Factory Chat Filters. - RobotHanzo (https://github.com/hannibal002/SkyHanni/pull/1574)
    + Hide chocolate factory upgrade and employee promotion messages.
+ Copy Chocolate Factory Stats to clipboard. - seraid (https://github.com/hannibal002/SkyHanni/pull/1517)
+ Highlight non-purchased items in Hoppity shop. - seraid (https://github.com/hannibal002/SkyHanni/pull/1517)
+ Added time tower status to the chocolate factory stats. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1506)
    + Also can notify you when you get a new charge or your charges are full.
+ Extra tooltip stats about upgrades for the chocolate factory. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1594)
    + View these to know when to buy time tower or Coach Jackrabbit.
+ Added Chocolate Leaderboard Change. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1602)
    + Show the change of your chocolate leaderboard over time in chat.
    + This updates every time you first open the /cf menu on a new server.
+ Added Chocolate Shop Price. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1601)
    + Show chocolate to coin prices inside the Chocolate Shop inventory.
+ Added keybinds for Chocolate Factory. - seraid (https://github.com/hannibal002/SkyHanni/pull/1644)
+ Added warning when Chocolate Factory upgrade is available. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1642)
+ Added amount of chocolate until next prestige to stats display. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1638)
+ Ability to adjust the opacity of players near shared and guessed egg waypoints. -
  RobotHanzo (https://github.com/hannibal002/SkyHanni/pull/1582)
+ Added time until the next Hoppity event in chat message for egg locator. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1625)
+ Added a warning before the Time Tower in the Chocolate Factory ends. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1816)

#### Inventory Features

+ Added SBA style Enchant Parsing. - Vixid (https://github.com/hannibal002/SkyHanni/pull/654)
    + Option to remove vanilla enchants in tooltip.
    + Option to remove enchant descriptions.
    + Option to change enchant formatting.
    + Also parses tooltips from /show.
+ Added option to hide item tooltips inside the Harp. - raven (https://github.com/hannibal002/SkyHanni/pull/1700)
+ Added an option in the Auction House search browser to search for the item on coflnet.com. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1743)

#### Crimson Features

+ Added Matriarch Helper. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1385)
    + Highlights the Heavy Pearls.
    + Draws a line to the Heavy Pearls.

#### Slayer Features

+ Added Blaze Slayer fire pillar display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1766)
    + Shows a large display with a timer when the Fire Pillar is about to explode.
    + Also displays for other players' bosses.

#### Misc Features

+ Added No Bits Available Warning. - Empa (https://github.com/hannibal002/SkyHanni/pull/1286)
    + Warns when you run out of available bits to generate.
+ Added Hide Far Entities. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1064)
    + Can perhaps increase FPS for some users by 5% to 150%.
    + Options to change the distance and number of mobs to always show.
    + Option to disable in garden.
+ SkyCrypt button to Discord RPC. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1526)
+ Added `/shupdate` command. - Empa (https://github.com/hannibal002/SkyHanni/pull/1578)
    + Can be used like `/shupdate <beta/full>` to download updates from a specific update stream.
+ Added Inventory background to GUI editor. - seraid (https://github.com/hannibal002/SkyHanni/pull/1622)
+ Added /shignore. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1469)
    + This lets you block users from running party commands.
+ Option to Replace Roman Numerals. - Mikecraft1224 (https://github.com/hannibal002/SkyHanni/pull/1722)
+ Added a simple Ferocity Display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1765)
    + Shows the Ferocity stat as a single GUI element.
    + Requires the Tab List widget to be enabled and Ferocity to be selected to work.

### Improvements

#### Garden Improvements

+ Added warnings when Farming Fortune/Crop Fortune aren't found in tab list. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1330)
+ Added highlight slot while hovering over line for Anita medal profit and SkyMart copper price. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1118)
+ SkyMart and Anita Medal prices are now perfectly aligned in the displays. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1376)
+ Clear plot sprays when Portable Washer is used. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1178)
+ Added option to show atmospheric filter display outside SB. -
  Obsidian (https://github.com/hannibal002/SkyHanni/pull/1131)
+ Improved visitor reward blocking. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1417)
    + Added option to block accepting/refusing expensive/cheap copper.
    + Added option to prevent blocking never accepted visitors.
    + Reworked visitor blocking logic to make it more like not clickable items.
+ Only warn that crop fortune is missing in tab list while actively farming. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1451)
+ Added Plot Visibility Type for garden. - Empa (https://github.com/hannibal002/SkyHanni/pull/1369)
    + Choose how to show infested plots in the world: Border, Name or both.
+ Added support for showing overflow garden levels outside the Garden Level Display. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1325)
+ Added options to toggle overflow garden levels and overflow level up messages. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1325)
+ Added an option to disable custom garden key binds while on the barn plot. -
  jani (https://github.com/hannibal002/SkyHanni/pull/1559)
+ Added option to block accepting/refusing visitors depending on coin loss. -
  Albin (https://github.com/hannibal002/SkyHanni/pull/1502)
+ Changed broken Pest Particle Waypoint to just show existing particles better, no guess. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1263)
    + This is just a workaround until the pest location detection is working again.
+ Armor drop tracker now displays only when holding a farming tool. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1650)
+ Made the Composter chat message clickable to warp to the Garden. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1723)
+ Improved Farming Timer. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1737)
    + The secondary visitor timer now accurately checks your BPS (Blocks Per Second) instead of assuming a value of 20.
+ Do Not Hide Maeve's Garden Chat Dialogue. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1742)
    + Maeve's Garden visitor dialogue is no longer hidden, preventing confusion about potential bugs.
+ Displays the middle of the plot while the pest line is showing. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1647)
    + This makes it clearer when the guess points to the middle of the plot rather than to a pest.
+ Improved pest count accuracy in plot detection. - Empa (https://github.com/hannibal002/SkyHanni/pull/1764)
+ Added options to show crops per second and crops per hour in the Crop Milestone Display. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1536)
+ Added tool gemstone information to /ff crop pages. -
  maxime-bodifee (https://github.com/hannibal002/SkyHanni/pull/1817)
+ Added supreme chocolate bars to the Cocoa Beans menu in /ff. -
  maxime-bodifee (https://github.com/hannibal002/SkyHanni/pull/1817)
    + Toggle this information with `/shcocoabeans`.

#### Diana Improvements

+ Now detecting inquisitor share chat messages from CT module "InquisitorChecker". -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1315)
+ Added kills since last inquis to chat message when digging one up. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1363)
+ Inquisitor sharing can now detect coordinates from all chat. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1316)
    + Disabled per default.
+ Now detecting inquisitor share messages sent from CT module "InquisitorChecker". -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1359)
+ Added a Diana chat helper. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1808)
    + This feature helps to enable /particlequality extreme and /togglemusic when needed.

#### GUI Improvements

+ Made Cookie Buff timer in Custom Scoreboard work without tab list. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1292)
+ Update Magical Power from Maxwell as well. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1327)
+ Improved SkyBlock Area in Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1350)
+ Added Cold Resistance 4 to Non God Pot Effects. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1440)
+ Added queue to Scoreboard Events in Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1420)
+ New Images for Discord Rich Presence. - NetheriteMiner (https://github.com/hannibal002/SkyHanni/pull/1422)
    + Updated important areas to have new images.
+ Added Fortunate Freezing Bonus to Mining Events in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1453)
+ Added line breaks functionality for Custom Scoreboard Title/Footer. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1373)
+ Added Dungeon Room ID to Lobby Code. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1428)
+ Made all rift event entries into one big rift entry in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1480)
+ Added outline to custom scoreboard. - Vixid (https://github.com/hannibal002/SkyHanni/pull/1461)
    + Customize border thickness.
    + Customize border blur.
    + Customize top and bottom color (gradient).
+ Moved Unknown Lines warning option back into Custom Scoreboard Category. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1561)
+ Added more Alignment Options to Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1566)
+ Allowed Custom Scoreboard Outline to also work on Background Images. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1566)
+ Chocolate Shop now updates chocolate availability every second. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1652)
+ Chocolate Shop now also shows time until affordable. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1652)
+ Changed the color of time to blue in Chocolate Factory displays. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1652)
+ Star now shows on time tower and coach jackrabbit. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1641)
+ Maxed upgrades in Chocolate Factory now show a checkmark. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1641)
+ Hide many garden GUIs while inside any Chocolate Factory menu. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1648)
+ Changed Chocolate Factory shortcut icon. - seraid (https://github.com/hannibal002/SkyHanni/pull/1640)
+ Added incoming tab list event to Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1666)
+ Changed the default design of the quiver line in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1752)
+ Show infinite Arrows in Custom Scoreboard when wearing Skeleton Master Chestplate. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1795)

#### Commands Improvements

+ Add /trade to tab completable commands. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1354)
+ Improved gfs to have support for calculations in amount. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1493)
+ Cleanup the format for the /shcommand hover description. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1531)
+ Added autocomplete for party commands. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1598)

#### Config Improvements

+ Added right click support for many garden GUI elements. - hannibal2 +
  nea (https://github.com/hannibal002/SkyHanni/pull/1395)
+ Added config links to most features. - jani (https://github.com/hannibal002/SkyHanni/pull/1404)
+ Added all missing config links, or added a comment if no related GUI element exists. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1412)
+ Moved No Bits Warning category into Bits Features category. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1487)
+ Changed Reset Config Command. - seraid (https://github.com/hannibal002/SkyHanni/pull/1524)

#### Inventory Improvements

+ Items that cannot be salvaged because you have donated them to the Museum are now greyed out in the Salvage menu. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1384)
+ Added support for Glacial Cave gemstones in Estimated Item Value. -
  Fix3dll (https://github.com/hannibal002/SkyHanni/pull/1416)
+ Added Fossil Excavator and Research Center to Hide Non-Clickable Items feature. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1430)
+ Sack Display update. - hannibal2 + Thunderblade73 + CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1381)
    + Sorting for gemstone sack and rune sack.
    + Now has options for horizontal alignment.
    + Highlight the item in inventory while hovering over the item name in the display.
+ Enchant books always show descriptions regardless of if 'Hide enchant description' is enabled. -
  Vixid (https://github.com/hannibal002/SkyHanni/pull/1552)
+ Added Item Ability Cooldown support for Royal Pigeon. - Jordyrat (https://github.com/hannibal002/SkyHanni/pull/1705)
+ Use raw craft cost of an item if no Auction House price is available. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1682)
+ Added Custom Chocolate Factory Reminder. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1740)
    + Click an item that can be upgraded/purchased with chocolate to set it as a reminder goal.
    + Works with Rabbits, Barn, Time Tower, etc.
    + Option to display this timer universally, even outside the inventory.
+ Added a display for the time remaining until Tower Charges are full. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1760)
+ Include item prices for upgrading in the Chocolate Shop calculation. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1805)
+ Added display of prestige level in chocolate factory statistics. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1827)

#### Slayer Improvements

+ Added Dragon's Nest as a valid Enderman Slayer location. - Alexia Luna
  (https://github.com/hannibal002/SkyHanni/pull/1399)
+ RNG meter item will now be detected when opening slayer menu. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1479)
+ Clicking the slayer RNG meter now opens the selection inventory. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1694)

#### Chat Improvements

+ Added more messages to Pet Drop Rarity. - Empa (https://github.com/hannibal002/SkyHanni/pull/1213)
+ Added option Ignore YouTube. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + Do not remove the rank for YouTubers in chat.
+ Added option Same Chat Color. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + All players, also those with ranks, write with the same, white chat color.
+ Enchant Parsing support for other mods' chat tooltips. - Vixid (https://github.com/hannibal002/SkyHanni/pull/1589)
+ Added hover events back into the custom player message formatter. -
  nea (https://github.com/hannibal002/SkyHanni/pull/1516)

#### Combat Improvements

+ Added customizable amount at which to get a Low Quiver Notification / Quiver Reminder. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1190)

#### Mining Improvements

+ Renamed Deep Caverns parkour to Deep Caverns Guide. - seraid (https://github.com/hannibal002/SkyHanni/pull/1443)
+ Make Fossil Excavator a category rather than accordion. - walker (https://github.com/hannibal002/SkyHanni/pull/1441)
+ Disabled colored blocks in the Mineshaft. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1749)
+ Disabled mining commissions block color in Dwarven Mines. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1790)

#### Event Improvements

+ Compressed the no power up line in the Jerry winter event. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1355)
+ Improved description of Hoppity highlight updates. - rnghatesme (https://github.com/hannibal002/SkyHanni/pull/1580)
+ Added a way to disable the rabbit crush warning by setting the threshold to zero. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1575)
+ Improved various Hoppity features. - seraid (https://github.com/hannibal002/SkyHanni/pull/1517)
+ Show an estimation of when you will prestige based on your chocolate/second. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1514)
+ Removed the Hoppity Menu Shortcut in Rift, Kuudra, Catacombs and Mineshaft. -
  raven (https://github.com/hannibal002/SkyHanni/pull/1585)
    + You cannot use the chocolate factory in these areas, resulting in the button being removed.
+ Live update chocolate amounts in other inventories related to the chocolate factory. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1594)
+ Added ranking percentage to Hoppity Display. - seraid (https://github.com/hannibal002/SkyHanni/pull/1501)
+ Split up Chocolate Factory and Hoppity in the config. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1599)
    + Factory is now under category inventory.
    + Hoppity is now alone.
+ Update saved chocolate count when getting a duplicate rabbit. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1665)
+ Improved upgrade recommendations in the Chocolate Factory. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1659)
    + No longer recommends the time tower upgrade, as it can lead to inefficiencies.
+ Added time until the next Hoppity egg. - seraid (https://github.com/hannibal002/SkyHanni/pull/1625)
+ Displays time until the best chocolate upgrade is available. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1654)
+ Show a line to Hoppity guess point. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1669)
+ Sharing the name of the rabbit hunt egg note in chat. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1675)
+ Added option to show the Hoppity Unclaimed Eggs display during a farming contest. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1670)
+ Added Duplicate Rabbit Time. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1671)
    + Show the production time of chocolate gained from duplicate rabbits.
+ Improved the description of the Time in Chat feature (Hoppity Event). -
  Obsidian (https://github.com/hannibal002/SkyHanni/pull/1679)
+ Expanded options for Hoppity Waypoint rendering. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1739)
+ Improved wording for Chocolate Factory spam filter and upgrade notification config options. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1729)
+ Added an option to only receive Rabbit Crush warnings during the Hoppity event. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1762)
+ Warn when all three Hoppity eggs are ready to be found. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1747)
+ Improved Hoppity Egg warning. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1802)
    + Added option to warp to an island on click.
    + Fixed a typo.
+ Extended warning sound duration when all 3 Hoppity Eggs are found. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1793)

#### Rift Improvements

+ Made the vermins in the Vermin Tracker match the order shown in Kat's menu. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1459)

#### Dungeon Improvements

+ Added a command to clear the kismet state in Croesus. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1539)
+ Added a way to disable marking low-level parties in dungeon finder by setting it to 0. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1626)

#### Fishing Improvements

+ Fixed a typo in the fishing profit tracker. - raven (https://github.com/hannibal002/SkyHanni/pull/1678)

#### Misc Improvements

+ Improved mod performance. - CalMWolfs, ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1342)
+ Added option to toggle No Bits Warning sound. - Empa (https://github.com/hannibal002/SkyHanni/pull/1425)
+ Show enchantment in book bundle name in profit tracker. - Empa (https://github.com/hannibal002/SkyHanni/pull/1391)
+ Added the option to choose the duration and color of Patcher Coords Waypoints. -
  jani (https://github.com/hannibal002/SkyHanni/pull/1476)
+ Added support to right-click in GUI editor to open config for all remaining GUI elements. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1455)
+ Fixed another memory leak. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1491)
+ Don't send some reminders while in dark auction. - seraid (https://github.com/hannibal002/SkyHanni/pull/1533)
+ Added descriptions for missing config categories. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1600)
+ Architect Draft messages can now be copied and pasted. - raven (https://github.com/hannibal002/SkyHanni/pull/1732)
+ Updated setting description for clarity. - seraid (https://github.com/hannibal002/SkyHanni/pull/1736)
+ The GUI position editor can now edit all GUIs that were visible within the last 20 seconds, including those inside
  inventories. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1768)
+ Improved the performance of scoreboard and tab list reading. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1779)
+ Improved performance of minion nametag feature and removed unnecessary checks between blocks. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1780)
+ Re-added different arrow types in Quiver Reminder after a run. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1832)

### Fixes

#### Fishing Fixes

+ Fixed not checking if in an area that thunder can spawn before checking for thunder sparks. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1345)

#### Slayer Fixes

+ Made the fire pit warning actually make a sound. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1342)
+ Fixed resetting the RNG item after dropping it for slayer. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1477)
+ Fixed Slayer failed warnings not showing up in Rift. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1627)
+ Fixed vampire slayer features not working while nicked. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1607)
+ Fixed error message when starting slayer quest in mining islands. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1619)
+ Fixed multiple slayer warning bugs. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1814)
    + Fixed slayer warning showing up at the wrong time.
    + Fixed Slayer Warning showing up when others are damaging mobs in an incorrect area.
    + Fixed showing warnings while doing Diana.
    + Fixed Slayer warning being off when joining/leaving the Rift.

#### Garden Fixes

+ Fixed Farming Fortune Display not showing when no Crop Fortune in tab. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1330)
+ Fixed impossible pest counts. - Empa (https://github.com/hannibal002/SkyHanni/pull/1362)
+ Fixed crop fortune message spam. - Empa (https://github.com/hannibal002/SkyHanni/pull/1364)
+ Fixed coins from leveling up farming counting to Pest Profit Tracker. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1358)
+ Fixed not being able to teleport to other infested plots immediately after killing a pest. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1357)
+ Fixed Jacob's contest detection. - HiZe (https://github.com/hannibal002/SkyHanni/pull/1375)
+ Fixed updating pests before pest count got updated. - Empa (https://github.com/hannibal002/SkyHanni/pull/1386)
+ Fixed being able to have duplicate custom key binds. - Empa (https://github.com/hannibal002/SkyHanni/pull/1387)
+ Fixed typo in Show outside list for Jacobs contest. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1131)
+ Next contest overlay now looks like before. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1403)
+ Fixed item sizes in the Crop Milestone display. - martimavocado &
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1436)
+ Fixed crash with garden visitor hotkey names. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1478)
+ Fixed issues with Pest Count. - Empa (https://github.com/hannibal002/SkyHanni/pull/1406)
+ Fixed some items being tracked incorrectly in the Pest Profit Tracker. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1388)
+ Fixed shift click on accept/refusal no longer changes the visitor highlight. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1475)
+ Fixed rare error with visitor reward warning. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1481)
+ Fixed bug with Garden Visitor Supercraft. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1550)
+ Fixed an error message with pest particles. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1551)
+ Fixed error message with GFS and super crafting pets. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1553)
+ Fixed pest count error being shown. - Empa (https://github.com/hannibal002/SkyHanni/pull/1563)
+ Fixed pest count logic not having necessary delay. - Empa (https://github.com/hannibal002/SkyHanni/pull/1591)
+ Fixed visitor reward warning only blocking normal clicks. -
  Obsidian (https://github.com/hannibal002/SkyHanni/pull/1595)
+ Fixed Visitor's logbook stats display showing while guesting. -
  HiZe (https://github.com/hannibal002/SkyHanni/pull/1635)
+ Fixed Farming Lane auto-activation when clicking on crops without farming tool. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1645)
+ Fixed visitor highlight color sometimes being wrong. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1639)
+ Fixed pet drop rarity issue in Pest Profit Tracker. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1624)
+ Fixed pest plot border showing over pest guess line. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1618)
+ Fixed crop milestone detection via tab list. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1693)
+ Fixed farming tool detection sometimes not working after closing an inventory. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1692)
+ Fixed the secondary visitor timer constantly disappearing while farming. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1737)
+ Fixed /ff not displaying armor fortune correctly. - maxime-bodifee (https://github.com/hannibal002/SkyHanni/pull/1817)
    + Gemstones and the Pesterminator are now included in the calculations.
    + The total value for each armor piece is now displayed correctly.
    + Updated total universal farming values to reflect these changes.
+ Fixed Farming Contest Stats taking too long to display in chat. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1829)
+ Fixed Farming Contest blocking other GUIs even while outside the Garden. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1829)
+ Fixed showing the wrong visitor time when info is not in the tab list. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1831)
+ Fixed an error in /ff when you don't have a gemstone. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1823)
+ Fixed plot border rendering in the Garden. - seraid (https://github.com/hannibal002/SkyHanni/pull/1819)

#### Chat Fixes

+ Added names of lobby players to /p tab complete. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1340)
+ Fixed friend detection from chat. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1336)
+ Fixed the Supercraft /gfs prompt not showing when you crafted 1,000 or more items. - Alexia Luna
  (https://github.com/hannibal002/SkyHanni/pull/1351)
+ Make fire sale hider in tab list work again. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1355)
+ Queued gfs now works with gemstone names. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1468)
+ Fixed pet rarity drop messages modifying unrelated messages. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1507)
+ Fixed "chat rank hider" changing rank formatting for level 400+. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
+ Fixed /gfs error with 0 at the end. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1528)
+ Fixed Transfer Party Chat command. - nobaboy (https://github.com/hannibal002/SkyHanni/pull/1505)
+ Fixed aquamarine gemstones in queued gfs. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1545)
+ Fixed some guild chat messages having wrong format. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1568)
+ Fixed chat click and hover functionality. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1680)
+ Fixed a rare crash related to complex chat events. - nea (https://github.com/hannibal002/SkyHanni/pull/1707)
+ Fixed stash getting detected as private message. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1716)
+ Fixed spaces in chat formatting. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1704)
+ Fixed player chat spacing issue with the crimson faction icon. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1770)
+ Fixed extra space in chat formatting. - Jordyrat (https://github.com/hannibal002/SkyHanni/pull/1785)
+ Fixed incorrect chat format when using the Hypixel command /show. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1781)
+ Fixed replacing words in dialogue with numbers. - Mikecraft1224 (https://github.com/hannibal002/SkyHanni/pull/1755)
+ Minor co-op chat capitalization fix. - appable (https://github.com/hannibal002/SkyHanni/pull/1825)

#### GUI Fixes

+ Fixed "No Power" being displayed as "No" in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1332)
+ Fixed coop banks not being affected by "hide empty numbers" in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1328)
+ Fixed bits available being subtracted in an infinite loop. - Empa (https://github.com/hannibal002/SkyHanni/pull/1292)
+ Fixed not setting No Power Active. - Empa (https://github.com/hannibal002/SkyHanni/pull/1366)
+ Fixed Objective Lines never being shown in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1377)
+ Fixed Crystal Hollows Island size being 26 instead of 24 in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1389)
+ Fixed "unknown lines" error with an objective line in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1394)
+ Fixed Powder not being shown in the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1414)
+ Fixed the skill tab list pattern sometimes not matching. - Alexia Luna
  (https://github.com/hannibal002/SkyHanni/pull/1419)
+ Fixed multiple mining v3 Unknown Lines errors in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1420)
+ Fixed Fame rank-up not updating the rank. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1450)
+ Made compact tab list not be invisible when turning it on. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1463)
+ Fixed Bits Available being out of sync after leaving the Rift. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1467)
+ Fixed pet error with Discord Rich Presence. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1488)
+ Fixed Custom Scoreboard missing Ävaeìkx sitting time. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1480)
+ Fixed Maxwell Power not updating when unlocking a new power. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1520)
+ Fixed Custom Scoreboard not detection M7 dragons during Derpy. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1561)
+ Fixed space in visitor shopping list. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1549)
+ Fixed missing Fossil Dust line in Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1586)
+ Fixed SkyHanni showing the Vanilla Scoreboard when using Apec. - hannibal2,
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1592)
+ Fixed Magical Power not setting to zero when the mod can't find the magical power line. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1632)
+ Fixed crash on Unix with Discord Rich Presence. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1611)
+ Made some lines in Discord Rich Presence shorter. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1613)
    + This should help avoid overflowing the SkyCrypt profiles button.
+ Fixed wrong max lobby size in Custom Scoreboard size while in Mineshaft. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1651)
+ Fixed Magical Power always being 0. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1646)
+ Fixed chroma not being affected by alpha. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1660)
+ Fixed an issue where Magical Power in Maxwell was sometimes not detected. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1674)
+ Fixed the Vanilla Scoreboard not reappearing after leaving SkyBlock. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1673)
+ Fixed an undetected objective in Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1697)
+ Fixed text transparency issues with Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1730)
+ Fixed more double objectives in Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1731)
+ Fixed missing Hay Eaten line in Custom Scoreboard in Rift. - Empa (https://github.com/hannibal002/SkyHanni/pull/1721)
+ Fixed Custom Scoreboard showing the Spooky Festival Time twice. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1757)
+ Fixed unknown scoreboard lines chat error message when Hypixel sends incomplete line data. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1761)
+ Fixed Custom Scoreboard error during Winter Event. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1789)
+ Fixed max island size display in Crimson Isle and Catacombs in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1822)
+ Fixed some issues where skyhanni guis would have click and hover events when they shouldn't. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1846)

#### Dungeon Fixes

+ Dungeon Copilot no longer tells you to enter the nonexistent boss room in The Catacombs - Entrance. - Alexia Luna
  (https://github.com/hannibal002/SkyHanni/pull/1374)
+ Fixed some area walls overlapping with blocks. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1418)
+ Fixed Dungeon Copilot not working on the Entrance Floor. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1656)

#### Bingo Fixes

+ Fixes rare error when hovering over a row item in Bingo Card. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1370)
+ Fixed bingo card reward line not found error. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1400)

#### Inventory Fixes

+ Fixed bazaar order cancel save to clipboard error again. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1474)
+ Fixed Bazaar Cancelled Buy Order Clipboard one last time. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1529)
+ Fixed tooltips disappearing with the Enchant Parsing feature. -
  Vixid (https://github.com/hannibal002/SkyHanni/pull/1552)
+ Fixed tooltip on certain items when using Enchant Parsing. - Vixid (https://github.com/hannibal002/SkyHanni/pull/1569)
+ Fixed slot highlight showing over item tooltip. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1603)
+ Fixed middle-clicking not working on time tower. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1596)
    + You can still right click to activate it.
+ Fixed harp keybinds. - seraid (https://github.com/hannibal002/SkyHanni/pull/1644)
+ Fixed Bazaar buy order error when nothing to cancel. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1606)
+ Fixed Estimated Item Value not visible in PV. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1620)
+ Fixed total rabbits not resetting. - seraid (https://github.com/hannibal002/SkyHanni/pull/1637)
+ Fixed error message in the Chocolate Factory. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1661)
+ Fixed checkmark incorrectly showing on prestige item. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1659)
+ Fixed inventory highlights sometimes remaining after exiting. -
  Mikecraft1224 (https://github.com/hannibal002/SkyHanni/pull/1699)
+ Fixed unintended clicks while using keybind feature in Melody's Harp or Chocolate Factory menu. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1690)
+ Fixed an error in estimated item value calculation. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1728)
+ Fixed upgrade time display when Chocolate Factory Upgrades are maxed. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1727)
+ Fixed incorrect time to prestige after using the Time Tower. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1726)
+ Fixed the Chocolate Factory upgrade warning incorrectly displaying when no upgrade was possible. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1741)
+ Fixed a case where the chocolate factory upgrade warning did not work. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1758)
+ Fixed Chocolate Factory time remaining calculations while the Time Tower is active. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1774)
+ Fixed enchantment colors showing as white when SkyHanni chroma is not enabled. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1798)
+ Fixed Chocolate Factory Shop. - seraid (https://github.com/hannibal002/SkyHanni/pull/1815)
    + Profit calculations now show in sub-menus.
    + Total chocolate value now updates when buying something from the Chocolate Factory Shop.
+ Fixed displaying Chest Value in minion inventories. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1834)
+ Fixed displaying Chest Value in some bazaar pages. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1833)
+ Fixed the selling of SkyBlock Menu. - seraid (https://github.com/hannibal002/SkyHanni/pull/1820)
+ Fixed Dark Candy not appearing in Candy Bag possible items. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1820)
+ Fixed the ability to hide Chocolate Factory upgrade messages. -
  seraid (https://github.com/hannibal002/SkyHanni/pull/1820)
+ Fixed Time Until Next Charge display. - seraid (https://github.com/hannibal002/SkyHanni/pull/1806)
+ Fixed typos and formatting in Craft Materials From Bazaar. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1840)
+ Fixed invalid bazaar items being highlighted in the clickable items feature. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1841)
+ Fixed Bazaar price features not working for Ultimate Jerry. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1842)

#### Rift Fixes

+ Fixed Rift NPC shops being treated as Overworld ones for selling items to them. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1494)
+ Fixed Blood Effigies timers in the Rift. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1649)
    + Fixed timers showing even when above the set respawn time.
    + Fixed display flickering due to milliseconds.
+ Fixed duplicate word "soul" in an Enigma Waypoint. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1776)

#### Mining Fixes

+ Fixed Paleontologist book not getting detected when excavate fossils. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1490)
+ Fixed cold overlay not resetting. - Empa (https://github.com/hannibal002/SkyHanni/pull/1540)
+ Fixed Profit per Excavation including Glacite Powder. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1628)
+ Fixed Fossil Dust and Glacite Powder saving in Excavation Profit tracker. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1628)
+ Fixed Mining event error spam in chat when the API got connection problems. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1746)
+ Fixed Bal being incorrectly highlighted when a Yog Slayer commission is active. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1801)

#### Command Fixes

+ Fixed party invite tab completion not working for friends. - nea (https://github.com/hannibal002/SkyHanni/pull/1571)
+ Fixed error when running /gfs without amount. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1567)
+ Fixed out of item messages in Composter GfS. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1398)

#### Config Fixes

+ Fixed show unclaimed eggs config option. - nopo (https://github.com/hannibal002/SkyHanni/pull/1572)

#### Event Fixes

+ Fixed a typo in the Barn Warning message. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1584)
+ Fixed not sharing location when barn full. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1608)
+ Fixed Chocolate Factory Move Tooltip flickering when clicking quickly. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1604)
+ Fixed swapped colors for Breakfast and Dinner eggs. - yhtez (https://github.com/hannibal002/SkyHanni/pull/1664)
+ Fixed typo in Hoppity's Hunt "not active" message. - walker (https://github.com/hannibal002/SkyHanni/pull/1711)
+ Fixed some cases where egg locator solver would trigger incorrectly. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1689)
+ Fixed incorrect Hoppity Waypoint rendering. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1739)
+ Fixed Hoppity Egg warnings appearing at the wrong time. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1796)
    + Fixed warning showing while the event is already over.
    + Fixed warning showing while you are busy.

#### Crimson Isle Fixes

+ Fixed incorrect mini boss amount displayed by Crimson Isle Reputation Helper. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1633)
+ Fixed Volcano Explosivity Display. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1821)
    + Broken when Hypixel introduced the new tab list.

#### Diana Fixes

+ Fixed an error in the All Burrows List. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1809)

#### Combat Fixes

+ Fixed Low Quiver Warning incorrectly appearing when switching to no arrows. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1832)
+ Fixed bestiary display always being disabled in search mode. -
  appable (https://github.com/hannibal002/SkyHanni/pull/1828)

#### Misc Fixes

+ Fixed tick event being called twice a tick. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1342)
+ Fixes random error in mob detection. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1361)
+ Fixed a spelling mistake in the Deep Caverns Parkour config. -
  jani (https://github.com/hannibal002/SkyHanni/pull/1349)
+ Rendered Items in SkyHanni GUIs no longer have partially broken textures (on default size). -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1390)
+ Fixes skill progress item scale being bigger than intended. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1413)
+ Fixed modid of SkyHanni classes being wrong, causing various issues, like the gfs queue not working. -
  nea (https://github.com/hannibal002/SkyHanni/pull/1411)
+ Fixed some typos. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1408)
+ Fixed No Bits Warning triggering when spending bits. - Empa (https://github.com/hannibal002/SkyHanni/pull/1425)
+ Clear SkyBlock area on world change. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1421)
    + This fixed the wrong area showing in some cases, such as when guesting on someone's Private Island.
+ Fixed a small issue that caused some features to not work as intended. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1402)
+ Fixed Bazaar copy not working and showing null instead. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1452)
+ Fixed Totem of Corruption Overlay not getting cleared on disable. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1449)
+ Fixed detecting being in calendar when you aren't. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1445)
+ Fixed delayed run crashes. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1445)
+ Fixed error from console filter on Minecraft shutdown. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1445)
+ Fixed skill issue. - nopo (https://github.com/hannibal002/SkyHanni/pull/1446)
+ Removed weird edge case which increased memory use. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1473)
+ Fixed catch incorrect error type when on alpha 27. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1462)
+ Fixed rendering an empty string on every single item. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1465)
    + This change improves performance.
+ Fixed Bits Gained Chat Messages being sent randomly. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1503)
+ Fixed discord profile level error. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1548)
+ Fixed Profit Trackers showing no more than 2.1b coins. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1554)
+ Fixed error with other mods that include DiscordIPC and do not relocate. -
  ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1573)
+ Fixed updater downloading the wrong version. - Empa (https://github.com/hannibal002/SkyHanni/pull/1590)
+ Fixed some mob features not working with Skytils' ping display. - Thunderblade73 &
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1605)
+ Fixed overly frequent bazaar price error messages. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1597)
+ Fixed overly long description for Patcher send coordinates waypoints. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1631)
+ Fixed the low quiver warning. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1629)
+ Fixed Custom Scoreboard occasionally displaying an outdated mayor after an election switch. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1698)
+ Fixed Active Effects in Compact Tab List always showing 0. - Alexia
  Luna (https://github.com/hannibal002/SkyHanni/pull/1706)
+ Fixed bugged minion name tags on your private island when opening a minion. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1630)
+ Fixed super-crafted items being incorrectly added to profit trackers. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1784)
+ Fixed typo in the Mythological Creature Tracker reset command. -
  Jordyrat (https://github.com/hannibal002/SkyHanni/pull/1800)
+ Fixed Inquisitor chat sharing sometimes not working. - yhtez (https://github.com/hannibal002/SkyHanni/pull/1799)
+ Fixed some cases of incorrect height for Griffin burrow waypoints. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1797)
+ Fixed profit tracker enchanted book name. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1775)
+ Fixed removal of incorrect minion name tags. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1791)
    + No longer resets all minion nametags when clicking the wheat minion in the Hub.
    + No longer resets minion nametags that are far away from the clicked minion.
+ Fixed the armor hider also hiding items in the inventory. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1813)
+ Fixed an error in the Outside SkyBlock setting. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1810)
+ Fixed memory leaks. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1839)
+ Fixed a small Memory Leak in MobData. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1844)

### Technical Details

+ Added KuudraAPI. - Empa (https://github.com/hannibal002/SkyHanni/pull/1209)
+ Added KuudraEnterEvent and KuudraCompleteEvent. - Empa (https://github.com/hannibal002/SkyHanni/pull/1209)
+ Added intrinsic scalability of strings. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/888)
+ Added Renderable.wrappedString. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/888)
    + Wraps a string n times if it exceeds the specified length
+ Added Mob Detection. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/712)
+ Make all event function names uniform. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1290)
+ Added offset to tick event. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1342)
+ Made dungeon milestone use repo instead of local patterns. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1342)
+ Removed private modifier from LorenzVec.toCleanString. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1344)
+ Removed Old TimeMark Class. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1322)
+ Remove a lot of usage of fixed rate timers and replace with a new event. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1264)
    + This makes these events that need to repeat about every second execute on the main thread.
+ Added Booster Cookie expiry time and hasCookieBuff to BitsAPI. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1292)
+ Added SimpleTimeMark.isInFuture(). - Empa (https://github.com/hannibal002/SkyHanni/pull/1292)
+ Added pest data to /shdebug. - Empa (https://github.com/hannibal002/SkyHanni/pull/1362)
+ Improved Scoreboard Line joining with special formatting codes. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1333)
+ Use InventoryCloseEvent over GuiContainerEvent.CloseWindowEvent when able. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1184)
+ Added an option to copy the raw scoreboard. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1356)
    + This is accessed with `/shcopyscoreboard -raw`.
    + `/shcopyscoreboard true` has been changed to `/shcopyscoreboard -nocolor`.
+ Added HypixelData.skyblockAreaWithSymbol which includes the symbol and color of the SkyBlock area. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1350)
+ Created `List<String>.matchFirst(pattern)`. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1353)
+ Error Manager now has a parameter to only show for beta users. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1299)
    + Less spam for full version users, still enough reports from beat users.
+ Added HighlightOnHoverSlot. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1118)
+ Added BitsUpdateEvent. - Empa (https://github.com/hannibal002/SkyHanni/pull/1286)
+ Fixed default fame rank being null. - Empa (https://github.com/hannibal002/SkyHanni/pull/1286)
+ Mob Detection logEvents to file. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1379)
+ Split sack display render code into multiple functions. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1380)
+ Added Renderable.verticalContainer. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1318)
+ Remove a lot of deprecated methods. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1371)
+ Added Renderable.table. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/822)
+ Add API for rendering fake ghostly/holographic entities in the world. -
  nea (https://github.com/hannibal002/SkyHanni/pull/731)
+ Added a `removeSpray` method to `Plot`. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1178)
+ Changed stuff around with chat messages. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1227)
+ Added config option to overwrite current mayor. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1302)
    + This replaces all "Always <mayor>" options.
+ Add isEnabled to BasketWaypoints and remove unused code line. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1207)
+ Added feature toggle adapter to automatically turn on features. -
  nea (https://github.com/hannibal002/SkyHanni/pull/581)
+ Fixed mob detection: decoy, m7 dragon, husks. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1367)
+ Added clickableUserError. - Empa (https://github.com/hannibal002/SkyHanni/pull/1387)
+ Fixed itemScale for Renderable.itemStack. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1390)
+ Bump MoulConfig to 3.0.0 betas. - nea (https://github.com/hannibal002/SkyHanni/pull/1382)
    + Adds jumps, links and portals.
    + Changes the package from a mix of `io.github.moulberry.moulconfig` and `io.github.notenoughupdates.moulconfig` to
      just `io.github.notenoughupdates.moulconfig`
+ Added Renderable.renderInsideRoundedRect. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/890)
+ Switch from LorenzUtils.inDungeons to DungeonAPI.inDungeon(). -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1378)
+ Bumped MoulConfig. - nea (https://github.com/hannibal002/SkyHanni/pull/1411)
+ Deleted two empty files. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1401)
+ Added BitsGain, BitsSpent and BitsAvailableGained to BitsUpdateEvent. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1425)
+ Renamed bitsToClaim to bitsAvailable. - Empa (https://github.com/hannibal002/SkyHanni/pull/1425)
+ Made `HypixelData.skyBlockArea[withSymbol]` and `LorenzUtils.skyBlockArea` nullable. - Alexia Luna
  (https://github.com/hannibal002/SkyHanni/pull/1421)
    + This replaces the previous `"?"` magic value.
+ Fixed colors in debug messages. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1423)
+ Code cleanup. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1402)
+ Added function ChatUtils.chatAndOpenConfig(). - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1396)
    + Allows sending a chat message that will open a given config element in the config editor.
+ Added slot to OwnInventoryItemUpdateEvent. - Empa (https://github.com/hannibal002/SkyHanni/pull/1190)
+ Added QuiverUpdateEvent. - Empa (https://github.com/hannibal002/SkyHanni/pull/1190)
+ Use Quiver item in 9th hotbar slot for QuiverAPI. - Empa (https://github.com/hannibal002/SkyHanni/pull/1190)
+ Added party leader to `PartyAPI`. - nea (https://github.com/hannibal002/SkyHanni/pull/1433)
+ Added party chat event. - nea (https://github.com/hannibal002/SkyHanni/pull/1433)
+ Removed use of notenoughupdates.util.Constants. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1191)
+ Made a generic gson builder containing common type adapters. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1308)
    + Moved specific gson types (Elite api data and Hypixel api data) to their own Gson objects.
+ Elitebot api now uses CropType and PestType instead of strings. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1308)
+ Added Renderable.multiClickable. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1407)
+ Added isKeyClicked. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1405)
    + This cleans up the Renderable.clickable.
+ Adding Hypixel mod API. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1426)
+ Added expiry time parameter to ClickableAction. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1445)
+ Added some LorenzVec and LocationUtils functions. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1445)
+ Deprecated all old LorenzVec math functions and made new operator functions for them. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1445)
+ Changed neu version in dev env. - nopo (https://github.com/hannibal002/SkyHanni/pull/1446)
+ Changed config name. - seraid (https://github.com/hannibal002/SkyHanni/pull/1443)
+ Fixed internalizing config moves introducing a self reference inside the JSON tree. -
  nea (https://github.com/hannibal002/SkyHanni/pull/1447)
+ Added MiningAPI which includes all GlaciteTunnels related stuff. - j10a1n15,
  Empa (https://github.com/hannibal002/SkyHanni/pull/1438)
+ Added ColdUpdateEvent. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1438)
+ Cleanup getting tablist header and footer. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1355)
+ Create TablistFooterUpdateEvent . - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1355)
+ Added direct support of setting the color of a mob. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1466)
+ Crash in dev env with missing @ConfigLink. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1455)
+ Formatted the whole code base to fit new editor config. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1485)
+ Force wrapping of enums. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1484)
+ Fixed a bug in mobDetection. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1495)
+ Show sound locations in-world for `/shtracksounds`. -
  ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1489)
+ Changed auto mixins to be gathered at compile time, rather than runtime. -
  nea (https://github.com/hannibal002/SkyHanni/pull/1456)
+ A ton. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
+ LorenzTooltipEvent no longer uses forge events. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1331)
+ Fixed all regexes in DungeonCopilot.kt. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1197)
    + Now uses `RepoPattern` with good key naming conventions.
+ Fixed f7 bosses not detecting as boss. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1496)
+ Move contributors to its own class manager. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1519)
    + Allow tab list suffix to be changed per contributor.
+ Added chat component spans. - nea (https://github.com/hannibal002/SkyHanni/pull/1512)
    + Chat component spans represent a substring of a component, preserving chat style information.
    + You can further slice it, as well as sampling the style in various places.
    + Allows transforming back into an equivalent chat component (excluding hierarchy information).
+ Added component matcher. - nea (https://github.com/hannibal002/SkyHanni/pull/1512)
    + Allows matching regexes against chat component spans (and by extension, chat components).
+ Removed `ItemRenderBackground`. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1497)
    + Added helpers in `RenderUtils` for drawing borders.
    + Added helpers in `RenderUtils` for drawing borders and highlights on `RenderGuiItemOverlayEvent`.
+ Changed `RenderRealOverlayEvent` to `RenderGuiItemOverlayEvent` to be more representative of what the event is for. -
  ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1497)
+ Added more queue draining functions. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1182)
+ Unit Test for RepoPatterns. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1410)
+ Created and used HypixelCommands. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1525)
+ Replace many internal commands with onClick() feature. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1522)
+ Using PrimitiveItemStack at getMultiplier. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1165)
+ Simplified the code checking when you can steak your Vampire Slayer boss. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1547)
+ Added blessings to dungeonAPI. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1326)
+ Moved drawGradientRect to RenderUtils. - Vixid (https://github.com/hannibal002/SkyHanni/pull/1461)
+ Moved Discord RPC to ThatGravyBoat version on TeamResourceful maven. -
  ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1526)
+ Fixed a few small errors related to RenderLivingEvent. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1555)
+ Improved mixins for custom tab completion. - nea (https://github.com/hannibal002/SkyHanni/pull/1571)
    + This should make SkyHanni more compatible with other mods doing the exact same mixin for tab completion.
    + Also changed the mixin to use an event, allowing more decentralized tab completion changes.
+ Changes how some of the chocolate factory data is stored and accessed. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1514)
+ Renderable tooltips are now deferred in end of RenderTick. -
  ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1603)
    + Tooltips should only be set in between RenderTickEvent start and RenderTickEvent end. i.e. screen render events.
+ Made DelayedRun return its SimpleTimeMark. - ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1570)
    + This is useful as to not over extraneously run an operation when another is already queued.
+ Added scroll able logic for Renderables. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/886)
    + Added elements: scrollList and scrollTable.
+ Cleanup Custom Scoreboard code. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1486)
+ Cleanup HoppityCollectionStats to improve readability. - walker (https://github.com/hannibal002/SkyHanni/pull/1562)
+ Added NamedParkourJson. - seraid (https://github.com/hannibal002/SkyHanni/pull/1471)
+ Added a `/shtestsackapi` debug command. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1398)
+ All upgrades in chocolate factory are now stored as ChocolateFactoryUpgrade. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1641)
+ Fixed ClickTypeEnum ids starting at 1. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1639)
+ Added pet rarity support to backend. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1623)
+ Created new event ItemHoverEvent. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1620)
+ Use Renderable in GardenCropMilestoneDisplay. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1534)
+ Allows use of `/shtestmessage -complex` to test components in JSON format. -
  nea (https://github.com/hannibal002/SkyHanni/pull/1663)
    + Similar to those obtained from shift-clicking `/shchathistory`.
+ Made `CropType.getByNameOrNull()` case-insensitive. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/997)
+ Added GuiContainerEvent.BeforeDraw. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1510)
+ Created a new bazaar price data fetcher, independent of NEU. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1597)
+ Added Text object that provides various helpers, DSL, and utilities for interaction with chat components. -
  ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1718)
+ Added EntityRenderLayersEvent for enabling and disabling an entities layers. -
  ThatGravyBoat (https://github.com/hannibal002/SkyHanni/pull/1696)
    + Useful for hiding extra layers of a player such as armor, capes, Items and so on.
+ Removed code duplication in AxisAlignedBB.getCorners. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1756)
+ Added a RemovalListener to TimeLimitedCache and TimeLimitedSet. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1761)
+ Added an optional custom hover to clickableChat. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1802)
+ Added test command /shtestgriffinspots. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1797)
    + This command tests all surrounding locations for possible Griffin burrow spots.
+ No longer creating a new MiscFeatures instance on each lobby command. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1792)
+ Fixed up some other patterns. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1792)

### Removed Features

+ Removed the option to change the highlight color in chest value. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1118)
+ Removed Bow Sound distance setting. - Empa (https://github.com/hannibal002/SkyHanni/pull/1190)
+ Removing Player Chat Symbols. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1483)
    + This is now merged in "Part Order".
+ Removed Twinclaws Sound. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1527)
    + Implemented by Hypixel.

## Version 0.24

### New Features

#### Inventory Features

+ Added Max Items With Purse. - NetheriteMiner
    + Calculates the maximum number of items that can be purchased from the Bazaar with the number of coins in your
      purse.
+ Added Copy Underbid Keybind. - Obsidian
    + Copies the price of the hovered item in Auction House minus 1 coin into the clipboard for easier under-bidding.
+ Added Gfs message after super crafting. — Zickles
    + Adding a clickable message to pick up the super crafted items from sacks.
+ Added Craft Materials From Bazaar. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1104)
    + Show in a crafting view a shopping list of materials needed when buying from the Bazaar.
+ Added AH Show Price Comparison. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/339)
    + Highlight auctions based on the difference between their estimated value and the value they are listed for.
    + Options to change the colors
+ Added Highlight options in /tab. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1175)
    + Green for enabled
    + Red for disabled

#### Minion Features

+ Added Inferno Minion Fuel pickup prevention. - Zickles (https://github.com/hannibal002/SkyHanni/pull/1103)
    + Blocks picking up the Inferno Minion or replacing the fuel inside when expensive minion fuels are in use.

#### Chat Features

+ Hide chat message about bank interest when the received interest is zero coins. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1117)
+ Rarity text to pet drop messages. - Empa (https://github.com/hannibal002/SkyHanni/pull/1136)

#### Dungeon Features

+ Added available classes in the tooltip. - Conutik
    + Shows in the dungeon party finder when hovering over a group.
    + Highlights your selected class in green if it's available.
+ Kismet tracking for dungeon chests. - Thunderblade73
    + Highlight chests which have been rerolled inside Croesus
    + Shows kismet amount at the re-roll button

#### Garden Features

+ Lane Switch Notification - ILike2WatchMemes
    + Sends a notification when approaching the end of a lane in Garden while farming.
    + Displays the distance until the end of a lane.
+ Made Rancher's Boots the stack size display account for the Cactus Knife now giving +100 speed cap while in the
  Garden. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1149)
    + Speed cap above 500 will now display as red because Hypixel now allows this for some reason, but it is practically
      unachievable. Also, the 1000 speed cap will now show up as 1k, so the text doesn't overflow into the slot to the
      left.
+ Added Plot Menu Highlighting - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/1181)
    + Plot highlighting based on plot statuses (pests, active spray, current plot, locked plot)
+ Added Pest Waypoint. - Empa + hannibal2 + Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1268)
    + Show a waypoint of the next pest when using a vacuum. Only points to the center of the plot the pest is in, if too
      far away.
    + Uses the particles and math to detect the location from everywhere in the garden.
    + Option to draw a line to waypoint.
    + Option to change the number of seconds until the waypoint will disappear.

#### Mining Features

+ Display upcoming mining events. - CalMWolfs
    + Show what mining events are currently occurring in both the Dwarven Mines and Crystal Hollows.

#### Gui Features

+ Added Custom Scoreboard - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
    + Customizable; New, never seen before lines like the current mayor with perks, your party, and more!
    + Custom Title and Footer, align them on different sides of the scoreboard.
    + Hide the Hypixel Scoreboard, add a custom Image as a background, rounded corners.
    + Supports colored month names & better garden plot names.
    + A ton of settings.
+ /shwords now saves to a new file so that you can find and share them more easily. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1234)

#### Event Features

+ Easter Egg Hunt 2024 waypoints. - Erymanthus + walker (https://github.com/hannibal002/SkyHanni/pull/1193)

#### Fishing Features

+ Added Totem Overlay. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1139)
    + Option to change the overlay distance.
    + Option to hide Totem Particles.
    + Option to show the effective area of a totem.
    + Option to get reminded when a totem is about to expire.

#### Misc Features

+ Added command `/shlimbo` for easier Limbo access. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/848)
    + Limbo time tracker also now works in the Slumber Hotel in the Bed Wars Lobby.
    + A new secret method to get more SkyHanni User Luck from Limbo.
+ Added command `/shlimbostats` for a simple way to view your Limbo stats. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/848)
    + Support for `/playtime` and  `/pt` while in Limbo.
    + Added your playtime to Hypixel's `/playtimedetailed`.
+ Added full auto-update for SkyHanni. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1122)

### Improvements

#### Chat Improvements

+ Added more messages to chat filter category annoying. - martimavocado
    + Not enough breaking power for gems.
    + Useless messages for sky mall and Jacob's event artifact.
+ Adding a toggle to hide sky mall perk messages outside mining islands. - martimavocado
+ Added a toggle to hide Jacob's event artifact message outside the garden. - martimavocado
+ Added Booster Cookie purchase reminder to chat filter category others. - Alexia Luna
+ Hide the TNT Run Tournament advertisement message in the main lobby. - Alexia Luna
+ Add event level up message to chat filter. - Zickles (https://github.com/hannibal002/SkyHanni/pull/1214)

#### Inventory Improvements

+ Changed Max Items with Purse display format. - hannibal2
+ Various additions to `/playtimedetailed`'s Limbo to be more consistent with Hypixel. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1252)
    + The item is now hidden when playtime is lower than 1 minute.
    + The tooltip is now hidden when playtime is lower than 2 minutes.
    + Fixes 1 minute being pluralized.

#### GUI Improvements

+ Added option to only show Custom Text Box while an inventory is open. - martimavocado
+ Added option to hide Profit Trackers while not inside an inventory. - hannibal2
+ Custom Scoreboard improvements. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1156)
    + Added a warning to config about other mods.
    + Added three empty lines.
    + Added option to hide empty lines at the top/bottom.
+ Option to change line spacing in Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1174)
+ Improved delete word/line functionality for text boxes/signs. -
  Obsidian (https://github.com/hannibal002/SkyHanni/pull/1090)
    + It's now consistent with Discord's.
+ Added Scoreboard Improvements. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1183)
    + Now only showing the two biggest units in time until next mayor.
    + Now only showing an active Slayer Quest while in the correct area.
    + Added current server player count as an element with an option to show max player count.
    + Added option to show the magical power.
+ Show Thaumaturgy Tuning in Custom Scoreboard. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1201)
    + Option to show in compact mode.
    + Supports "Values First" option.
    + Change the number of tunings shown.
+ Improved the stats tuning message when Hypixel auto-adjusts your tuning points. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1212)
+ Updating "bits to claim" in the Custom Scoreboard when opening the GUI /boostercookiemenu. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1230)
+ Added "Curse of Greed" to non-God Potion effect display. - Alexia
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1225)
+ Added cold as a Scoreboard Element. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1238)
+ Add 4 more empty lines into custom scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1259)
+ Added customisable Events Priority in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1221)
    + Using a draggable list, you can fully customise, what events will be shown which what priority.
+ Updated default Scoreboard Elements config option. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1221)
+ Added feature to showcase Foxy' extra event in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1270)

##### Garden Improvements

+ Show calculation breakdowns when hovering over Anita Medal Profit or SkyMart Coins per copper displays. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1116)
+ Get garden level from SkyBlock Menu and Desk. - Empa (https://github.com/hannibal002/SkyHanni/pull/1164)
+ Partially rewrote the Lane Switch Warning feature. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1208)
    + Works with every lane size.
    + Option to show the corner of the current lane.
    + Removed the need to open the plot inventory.
    + More precise timer/distance calculation.
    + Reordered Farming Lane config.
+ Added support for using the tab list pest widget to know where pests are. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1237)
+ Use more information about pests from the scoreboard when possible. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1237)
+ Added support for offline pest messages. - Empa (https://github.com/hannibal002/SkyHanni/pull/1237)
+ Lane detection works faster now. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1239)
+ Added option to change how often the Lane Switch sound should play. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1239)
+ Allow using the teleport hotkey when in an infested plot. -
  Obsidian (https://github.com/hannibal002/SkyHanni/pull/1140)
+ Added the command `/shresetvisitordrops` to reset your Garden Visitor Drops Statistics. -
  HiZe (https://github.com/hannibal002/SkyHanni/pull/1135)
+ Improved Plot Menu Highlighting. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/1254)
    + Added 'Pasting' status.
    + Changed Event used for checking for statuses.
+ Hide the Sensitivity Reducer overlay when the mouse is locked. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1253)
+ Hide visitor Jacob messages without hiding real Jacob ones. - Alexia
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1267)
    + Previously, we made the Jacob visitor messages not hidden to avoid hiding real Jacob messages.
+ Added the ability to get your current speed on the Garden even if it isn't shown in the tab list. - Alexia
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1277)
    + This also means the speed now updates faster.
+ Change the Pest Waypoint color depending on distance. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1296)
    + It is the one from the Hypixel particles.
+ Better pest tracker waypoint detection for pest or center of plot. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1284)
+ Immediately hide waypoints when there are no pests left. - Empa (https://github.com/hannibal002/SkyHanni/pull/1284)

#### Crimson Improvements

+ Show Town Board waypoint when there is an accepted Rescue Mission quest. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1157)

#### Command Improvements

+ Allow using translate commands without the feature turned on. -
  Obsidian (https://github.com/hannibal002/SkyHanni/pull/1141)
    + Changed /shsendtranslation to /shtranslate and moved it from internal commands.
+ Adds a few keywords to `/shlimbostats` for better discoverability. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1252)

#### Fishing Improvements

+ Fishing Hook Alert text changes. - Empa (https://github.com/hannibal002/SkyHanni/pull/1169)
    + Added custom text when ready to pull.
    + The text is now aligned to the center of the GUI element.
+ Added a delay after tool swap before warning about wrong farming speed. - Alexia
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/840)
+ Loading Trophy Fish information from NEU PV. - hannibal2 &
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1123)
+ Added wireframe as a valid Totem of Corruption outline. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1248)

#### Dungeon Improvements

+ Add new class level colors in the Party Finder. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1219)

#### Mining Improvements

+ Add the ability to see Mineshaft mining events when that releases to main. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1236)

##### Config Improvements

+ Changed a lot in the config. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1300)
    + Main categories are reordered.
    + Moved into GUI: Compact Tab List, Cosmetic, Discord Rich Presence, Chroma and Marked Players
    + Moved into Inventory: Estimated Item Value, Pocket Sack in a Sack, Auction House and Item Abilities
    + Moved into Misc: Commands, Stranded and Minions
+ Reordered custom scoreboard config options. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1294)
+ Changed SkyBlock Level Guide Highlighting Collections to no longer being default enabled. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1298)

#### Misc Improvements

+ Added option to show some features outside SkyBlock. - Obsidian
+ Added goal to collection tracker. - Thunderblade73
    + /shtrackcollection \<item name> [goal amount]
    + Shows a chat message once the goal has been reached.
+ Added SkyHanni update download finished chat message. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1122)
+ Better chat error when profile name is disabled via Hypixel widgets. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1153)
+ Single line Hover messages are now closer to the cursor -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/821)

### Fixes

#### Bingo Fixes

+ Fixed Bingo Card Tips using wrong slot sometimes. - Thunderblade73
+ Fixed Bingo Minion Craft Helper sometimes causing performance problems. - CalMWolfs

#### Mining Fixes

+ Fixed a small typo in the Deep Caverns Parkour message. - Alexia Luna
+ Stopped Dwarven Mines events from showing in Crystal Hollows in the mining event tracker. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1142)
+ Further fixed Showing Dwarven specific events in Crystal Hollows/Mineshaft section of mining event display. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1289)

#### Inventory Fixes

+ Fixed copy underbid keybind not working. - Obsidian
+ Fixed SkyBlock guide highlight missing tasks not working properly for minions and collections. - Thunderblade73
+ Fixed Harp Quick Restart not working when Harp GUI Scale is disabled. - Zickles
+ Fixed "Mark Missing Class" highlighting every party. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1211)
+ Fix a typo in Not Clickable Items in the /equipment menu. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1242)
+ Fixed `/playtimedetailed`'s Limbo displaying incorrectly for x.0 play times. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1252)

#### Chat Fixes

+ Fixed Fire Sale chat hider not working when multiple fire sales are ending. - Zickles
+ Fixed Auto Tip chat filter. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1105)
+ Fixed Hide Anita Accessories' fortune bonus chat message not getting hidden in the chat filter. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1106)
+ Fixed some chat symbols showing twice in chat due to a Hypixel change. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1119)
+ Fixed Anita's Accessory chat filter not working with multi-word crops (Cocoa Beans, Nether Wart, Sugar Cane). -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1115)
+ Fixed fire sale chat hider. - Empa (https://github.com/hannibal002/SkyHanni/pull/1147)
+ Fix event level up chat filter. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1250)

#### Garden Fixes

+ Stop SkyHanni from saying that Next Visitor is visiting your garden. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1125)
+ Fixed typos/grammar. - Obsidian
    + Typos in pest features.
    + Grammar in Farming Weight Display
+ Fixed the Lane Switch Calculation - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/1138)
+ Fixed coins per copper display not working with new sub categories of SkyMart. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1154)
+ Fixed tab list visitor name detection breaking when tab list said "new" -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1151)
+ Fixed garden features appearing while visiting other player's garden. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1188)
+ Fixed rare crash in /ff display. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1217)
+ Fixed NEUInternalName is null for item name 'Mushroom'. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1226)
+ Fixed visitor status not updating to Ready when you have enough items in sacks after first talking to the visitor. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1224)
+ Fixed visitor status not updating to Waiting when you no longer have enough items when talking to them. - Alexia
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1224)
+ Fixed pest overlays not showing if the amount in each one was unknown. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1237)
+ Fixed pests spawning in the Barn plot not being detected. - Empa (https://github.com/hannibal002/SkyHanni/pull/1237)
+ Fix lane corners showing while not farming. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1239)
+ Fix Movement Speed display while on soulsand. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1239)
+ Fix Farming Lane time remaining display while on soulsand. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1239)
+ Lane Switch warning and remaining time ETA now supports soul sand farming and shows the current state of movement. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1245)
+ Made drops from pests get added to crop milestones. - Empa (https://github.com/hannibal002/SkyHanni/pull/1243)
+ Fixed Lane Detection warning and time remaining not working when movement speed feature is disabled. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1241)
+ Show warning in Composter Overlay when composter upgrades are not found. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1256)
+ Fixed incorrect pest amount in plots under certain conditions. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1260)
+ Fixed pests in uncleared plots not getting detected. - Empa (https://github.com/hannibal002/SkyHanni/pull/1260)
+ Fixed another Sensitivity Reducer + Mouse Lock incompatibility. -
  martimavocado (https://github.com/hannibal002/SkyHanni/pull/1253)
+ Fixed mouse locking not always working. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1272)
+ Fixed a bug that the seconds per copper is incorrect in garden visitors with multiple items. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1295)
+ Fixed some particles triggering the pest tracker waypoint. - Empa (https://github.com/hannibal002/SkyHanni/pull/1284)
+ Fixed not detecting infected plots from tab list. - Empa (https://github.com/hannibal002/SkyHanni/pull/1291)
+ Fixed visitor tooltip breaking when visitor data is disabled in tab list. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1251)

#### Command Fixes

+ Fixed duplicate commands in /shcommands. - CalMWolfs
+ Fixed inconsistent coloring in `/shlimbostats`. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1252)
+ Fixed rare bug with gfs from bz. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1283)

#### Slayer Fixes

+ Added Burningsoul Demon (75M HP mini boss) to line to mini boss and highlight slayer mini bosses. -
  Empa (https://github.com/hannibal002/SkyHanni/pull/1144)
+ Fixed Damage Indicator not hiding vanilla names. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1179)
+ Fixed Slayer Profit Tracker not detecting the slayer spawn cost when taking money from the bank. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1218)
+ Fixed slayer cost from bank counting plus instead of minus profit. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1220)
+ Fixed Server Player Amount not being affected by "display numbers first". -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1285)

#### Gui Fixes

+ Fixed voting line, dojo, server-restart & plot pasting errors in custom scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1152)
+ Fixed Mithril Powder showing Gemstone Powder instead. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1152)
+ Custom Scoreboard fixes. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1156)
    + Fixed Jacob's Contest, Trevor the Trapper & M7 Dragons.
    + Fixed the bottom border not working when using a custom background.
    + Fixed Hypixel scoreboard being shown while using Apec.
+ Fixed skill detection from tab list again. - HiZe_ (https://github.com/hannibal002/SkyHanni/pull/1160)
+ Fixed other GUIs rendering over Compact Tab List. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1161)
+ Tab list fixes. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1121)
    + Fixed garden spam because of wrong tab list visitor format.
    + Fixed the next visitor timer.
    + Allowed you to use extended visitor info and still have visitor display work.
+ Scoreboard Fixed. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1174)
    + Fixed "Hide empty lines at top/bottom" sometimes not actually hiding them .
    + Fixed Server ID sometimes not showing.
    + Fixed instance shutdown error while in Kuudra.
+ Fixed Hypixel scoreboard never reappearing. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1170)
+ Fixed pet name in tab list getting detected as player name with widget settings. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1166)
+ Scoreboard Fixes. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1189)
    + Fixed current power not working when the player doesn't have the accessory bag unlocked.
    + Fixed bits to claim not setting to zero when the booster cookie item doesn't exist in SkyBlock Menu.
+ Scoreboard Fixes. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1183)
    + Fixed Hypixel URL sometimes showing during a Jacob's contest.
    + Fixed Unknown Lines error during wind compass.
+ Fixed unknown line issues in Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1205)
    + Fixed unknown line error in magma chamber.
    + Fixed unknown line issues when Hypixel doesn't send the whole line
+ Fixed Custom Scoreboard errors inside the dungeon. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1199)
    + Fixed dragon's line not being properly removed.
    + Fixed a line randomly showing "0" sometimes.
    + Fixed "Cleared..." line sometimes being black.
+ Custom Scoreboard Fixes. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1212)
    + Fixed IndexOutOfBoundException.
    + Fixed Instance Shutdown Line not being hidden.
    + Fixed a broken Hypixel Scoreboard Line.
    + Fixed New Year Line appearing twice.
+ Fixed Scoreboard Unknown Lines error during a dojo and a floor 3 run. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1229)
+ Fixed all new custom scoreboard errors. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1238)
+ Fixed time in Custom Scoreboard displaying 0 instead of 12. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1249)
+ Fixed custom scoreboard issue with cold line. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1244)
+ Fixed server ID error appearing in chat. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1262)
+ Fixed two Scoreboard Errors. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1255)
    + During the F3/M3 boss fight.
    + During a garden cleanup
+ Fixed Mining Events Priority in Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1278)
+ Fixed powder display always displaying the color first in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1275)
+ Fixed Hot Dog Contest error (Rift) in Custom Scoreboard. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1273)
+ Fixed Cookie time with in custom scoreboard when effects widget is enabled. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1261)
+ Fixed some more cases which would incorrectly show server ID error. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1271)

#### Dungeon Fixes

+ Fixed error in Dungeon Finder Features. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1162)
+ Fix getting current class in Party Finder. - martimavocado (https://github.com/hannibal002/SkyHanni/pull/1219)
+ Fixed showing the Dungeon Floor numbers in your inventory as well while inside the Catacombs Gate menu. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1297)

#### Crimson Isle Fixes

+ Fixed wrong crimson isle quests detection after tab list widget update. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1167)

#### Dungeon Fixes

+ Fixed Dungeon Complete on Entrance. - Empa (https://github.com/hannibal002/SkyHanni/pull/1202)

#### Config Fixes

+ Removed a second "Pet Candy Used" config option that did nothing. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1198)
+ Fixed game crash when open browser button in config fails. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1195)
+ Remove the removed stack size option from config. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1274)

#### Fishing Fixes

+ Renamed "fishing contest" to "fishing festival". - Empa (https://github.com/hannibal002/SkyHanni/pull/1222)
+ Fixed duplicate chat prefix when updating trophy fishing data from NEU PV. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1246)

#### Misc Fixes

+ Fixed skill level up message when below level 60. - HiZe
+ Fixed SkyHanni not working on Alpha. - hannibal2 & CalMWolfs
+ Fixed getting profile name from tab list on alpha for special profiles. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1120)
    + Fixes for any Ironman, Stranded and maybe Bingo players.
+ Fixed minion hopper profit display feature not getting changed by /shdefaultoptions. - hannibal2
+ Disable action bar hider if Skill Progress feature is disabled. -
  ooffyy (https://github.com/hannibal002/SkyHanni/pull/1137)
+ Improved performance of custom colored mobs. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1127)
+ Fixed Skill Progress Pattern. - HiZe_ (https://github.com/hannibal002/SkyHanni/pull/1148)
+ Fixed selecting arrows with different color codes. - Empa (https://github.com/hannibal002/SkyHanni/pull/1194)
+ Fixed wrong pattern causing trapper mob area to not get a waypoint. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1186)
+ Fixed Superboom TNT not working with Queued GFS. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1196)
+ Fixed detection of Party Leader when their username ends with an `s`. - Alexia
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1203)
+ Hover messages can't go off-screen anymore - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/821)
+ Fix "Ghost Entities" feature breaking outside SkyBlock game modes on Hypixel. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1301)

### Technical Details

+ Auto-enable + configure DevAuth. - nea
+ Improve performance of removeColor. - nea
+ Removed wildcard imports in VisualWorldGui. - CalMWolfs
+ printStackTrace -> ErrorManager. - hannibal2
+ Added command /shconfig toggle <path> <value 1> <value 2>. - Obsidian
+ Removes usage of logError everywhere in the code and replaces it with logErrorWithData. - CalMWolfs
+ Removed ErrorManager.logErrorState(). - CalMWolfs
+ Creating number utils functions for string -> int, long, double, with either error throwing or wrong usage to user
  message.
+ Removed RenderWorldLastEvent usage in WorldEdit feature. - hannibal2
+ Using GardenAPI.storage everywhere. - hannibal2
+ Added stacking enchants to the repo. - CalMWolfs
+ Moved bazaar config into inventory category. - hannibal2
+ Optimize IntelliJ icon. - nea
+ Used the isInIsland function more. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1107)
+ Optimized item.isFishingRod logic. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1056)
+ Code cleanup in multiple files. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1113)
+ ItemStack.name no longer nullable. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1101)
+ Better error handling (more user facing errors - less hidden errors). -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1101)
+ Replaced/fixed deprecated function calls. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1101)
+ Added item category FISHING_BAIT. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1101)
+ Use internal name more in ItemDisplayOverlayFeatures - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1114)
+ Reformatted the code in a lot of files. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1109)
+ Changed line endings to be normalized over the whole project and prevents further breaks. -
  nea (https://github.com/hannibal002/SkyHanni/pull/1112)
+ Added more error logging to getting farming weight. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1120)
+ Removed unnecessary capturing groups from the Anita's Accessory chat filter regex. -
  Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1115)
+ Add maven publication details, allowing other mods to integrate more easily with Skyhanni. -
  nea (https://github.com/hannibal002/SkyHanni/pull/935)
+ Created Pull Request template. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1108)
    + For better maintainability and automatic beta changelog creation.
+ Use less forge events throughout the mod to reduce possible crashes. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1085)
+ Fix entity click not being cancelled - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1072)
+ Cleanup ItemClickData. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1072)
+ Fixes minecraft bug where text that is bold can render weirdly. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1126)
+ Added "unlocked" parameter for the GardenPlotAPI -
  ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/1075)
+ Updated MovementSpeedDisplay to use new calculations -
  ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/1075)
+ Added Track Sounds Command - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/855)
+ Removed unused Sound Test - martimavocado (https://github.com/hannibal002/SkyHanni/pull/855)
+ Gson serialize the mining event data sent and received. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1040)
+ Changed fillTable to use a list of DisplayTableEntry. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1116)
+ Removed the RenderMobColoredEvent & ResetEntityHurtEvent. -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1127)
+ Added BitsAPI with current bits, bits to claim & current Fame Rank. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Added FameRanks, which includes all ranks with important data. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Added QuiverAPI with current arrow type and amount, and a hashmap of the arrows of the player. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Added MaxwellAPI with current power and magical power. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Added Mayor & Perk Enum. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Added method to render a string on element width. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Updated MayorAPI with new debug data, better PerkActive method & a method to give a mayor a color. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Updated ScoreboardData to only remove the second color code when it's the same as the old one. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Updated TablistData with a new getPlayerTabOverlay() and a fullyLoaded var. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Added LorenzUtils.inAnyIsland(). - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Added List.removeNextAfter() to CollectionUtils. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Added noStackTrace param to .logErrorWithData() in ErrorManager. -
  j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Updated some patterns to RepoPatterns. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/893)
+ Created GuiPositionMovedEvent. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/893)
+ More features for /shtestmessage - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1145)
    + Supports -clipboard parameter, uses the clipboard as text.
    + Supports blocked or modified chat events.
+ Code cleanup in LaneSwitchUtils.isBoundaryPlot. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1143)
+ Creating and using ContainerChest.getAllItems(), getUpperItems() and getLowerItems(). -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1047)
+ Fixed unknown power errors in the Custom Scoreboard. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1156)
+ Use a repo pattern for getting the visitor count. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1121)
+ Calling server ID getter method now in HypixelData. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1174)
+ Revert shader version for Mac compatibility. - Vixid (https://github.com/hannibal002/SkyHanni/pull/1176)
+ Added ItemStack.toPrimitiveStackOrNull(). - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1104)
+ Added InventoryOpenEvent.inventoryItemsPrimitive. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1104)
+ Added custom scoreboard lines to /shdebug. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1183)
+ Added support to search the chat history via /shchathistory <search term>. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1187)
+ Added more repo patterns to the trapper feature and improved enum names. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1186)
+ Extracted player and profile specific storage into their own classes in a new packet. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1171)
+ Moved Thaumaturgy Tuning Points detection into MaxwellAPI, and saving it in the profile-specific config. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1201)
+ Add "open in regex101.com" IntelliJ intention. - nea (https://github.com/hannibal002/SkyHanni/pull/1210)
    + Press ALT+ENTER while hovering over a RepoPattern.pattern call with your text cursor to select the "Open
      regex101.com" option
    + Add a Kotlin doc comment with `REGEX-TEST: someString` lines to add test cases
+ Creating and using FarmingLaneSwitchEvent. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1208)
+ Refactored all of `DungeonFinderFeatures.kt`. - Conutik (https://github.com/hannibal002/SkyHanni/pull/1180)
    + Less laggy and resource intensive.
+ Code cleanup in RenderLineTooltips. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1232)
+ Added JSON objects for Hypixel player API data, which can be used for other stuff later. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1146)
+ Renderable.hoverTips now supports renderables as tips and content -
  Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/821)
+ Added more debug when internal name from item name is null. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1226)
+ Refactored visitor handling code slightly. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1224)
    + Removed unneeded inSacks property.
+ Added "unknownAmount" to PestSpawnEvent. - Empa (https://github.com/hannibal002/SkyHanni/pull/1237)
+ Added StringUtils.generateRandomId(). - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1128)
+ Added ChatUtils.clickableChat support for runnable action. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1128)
+ Creating and using NeuInternalName.getAmountInInventory() and NeuInternalName.getAmountInSacksOrNull(). -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1231)
+ Added NeuProfileDataLoadedEvent. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1123)
+ Add mineshaft islandType. - CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1235)
+ Removed distanceFromPreviousTick as it is not accurate while on soulsand. -
  hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1239)
+ Added bindCamera() method and started using it. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1248)
+ Added drawSphereInWorld() method. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1139)
+ Removal of NEU's SlotClickEvent. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1206)
+ Fixed usage of checkCurrentServerId. - j10a1n15 (https://github.com/hannibal002/SkyHanni/pull/1177)
+ Made LorenzUtils.lastWorldSwitch use SimpleTimeMark instead of currentTimeMillis. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1262)
+ Repo Pattens now work inside Unit Tests. - Thunderblade73 (https://github.com/hannibal002/SkyHanni/pull/1258)
+ Added isBeingPasted variable for plots. - ILike2WatchMemes (https://github.com/hannibal002/SkyHanni/pull/1254)
+ Always use local repo patterns when in dev env. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1282)
+ Removed VisitorToolTipEvent. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1251)

### Removed Features

+ Removed Advanced Stats in SkyMart Copper price. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1116)
    + The same information is now always visible via hovering.
+ Removed desk in the SB menu in favour of Hypixel's version. -
  CalMWolfs (https://github.com/hannibal002/SkyHanni/pull/1158)
+ Removed Plot Name in Scoreboard. - hannibal2 (https://github.com/hannibal002/SkyHanni/pull/1168)
    + Hypixel added their own compact format now.
+ Removed max pet XP tooltip because Hypixel added it. - Obsidian (https://github.com/hannibal002/SkyHanni/pull/1132)
+ Removed colored name tag option for Garden visitors. - Alexia Luna (https://github.com/hannibal002/SkyHanni/pull/1228)
    + Hypixel has added this feature themselves.

## Version 0.23

### New Features

#### Garden Features

+ Added command /shcropsin <time> <item>. - DylanBruner
    + Shows the number of items you gain when farming in the garden for the given time.
+ Show sack item amount to the visitor shopping list. - CalMWolfs
+ Added Atmospheric Filter Display. - Erymanthus
    + This display shows the currently active buff as a GUI element.
    + For an optimal experience, please have the Atmospheric Filter accessory active.
+ Added Sensitivity Reducer. - martimavocado
    + Lowers mouse sensitivity while in the garden.
    + Either when pressing a keybind or holding a farming tool in hand.
    + Changes by how much the sensitivity is lowered by.
    + Show a GUI element while the feature is enabled.
    + Option to only allow this feature while on ground and/or on barn plot.

#### Fishing Features

+ Added Sea Creature Tracker. - hannibal2
    + Allows to only show single variants, e.g. water or lava or winter.

#### Mining Features

+ Added Deep Caverns Parkour. - hannibal2
    + Shows a parkour to the bottom of Deep Caverns and to Rhys.
+ Blocks the mining ability when on a private island. - Thunderblade73

#### Dungeon Features

+ Hide particles and damage splashes during the terracotta phase in dungeons F6 and M6. - hannibal2

#### Crimson Isle Features

+ Added Volcano Explosivity in Crimson Isle. - Erymanthus
    + Show a HUD of the current volcano explosivity level.
+ Added Sulphur Skitter Box in Crimson Isle. - HiZe
    + Renders a box around the closest sulphur block.
+ Added Dojo Rank Display. - HiZe
    + Display your rank, score, actual belt and points needed for the next belt in the Challenges inventory on the
      Crimson Isles.
+ Crimson Isle Volcano Geyser features. - MrFast
    + Stops the white geyser smoke particles from rendering if your bobber is near the geyser.
    + Draws a box around the effective area of the geyser.
    + Change the color of the box around the geyser.

#### Inventory Features

+ Added Power Stone Guide features. - hannibal2
    + Highlight missing power stones, show their total bazaar price, and allows to open the bazaar when clicking on the
      items in the Power Stone Guide.
+ Option to make normal clicks to shift clicks in npc sell inventory. - Thunderblade73
+ Show pet items XP Share and Tier Boost as small icons next to the pet in an inventory. - Thunderblade73
+ Added Shift Click Brewing. - Thunderblade73
    + Makes normal clicks to shift clicks in Brewing Stand inventory.
+ Low Quiver Notification. - CarsCupcake
    + This will notify you via title if your quiver is low on arrows according to chat message.
+ Added not fully completed tasks in Way to gain SkyBlock XP menus. - Thunderblade73
    + Works in the subcategories.
    + It does not work with infinite tasks.
+ Added Harp GUI Scale. - Thunderblade73
    + Automatically sets the GUI scale to AUTO when entering the Harp.
+ Added Harp Quick Restart. - Thunderblade73
    + Once you've launched the harp, quickly hit the close button in the harp menu to initiate the selected song.

#### Item Features

+ Lesser Orb of Healing Hider. - jani

#### Chat Features

+ Add tab list fire sale advertisement hider. - nea
+ Added SkyBlock XP Chat. - Thunderblade73
    + Sends the SkyBlock XP message from the action bar into the chat.

#### Command Features

+ Party Kick with reason. - nea
    + Added support for the Hypixel command /p kick /pk to add a reason. The reason will be sent in party chat before
      kicking the player.
+ Added /shcalccrop. - CalMWolfs
    + Calculate how many crops need to be farmed between different crop milestones.
+ Added /shcalccroptime. - CalMWolfs
    + Calculate how long you need to farm crops between different crop milestones.

#### Diana Features

+ Saving every Diana Burrow Location and option to render them. - hannibal2
    + Saves the burrow locations you find into a list in the local config file. (default enabled, option to opt out)
    + Option to render all saved locations in the world. (default disabled)
    + Commands to save the full list to the clipboard, and load a list from the clipboard (to share between users)

#### GUI Features

+ Added Skill Progress Display. - HiZe
    + ETA Display, exp, actions or percentage to next level, custom level goals, all skill display, chroma progress bar,
      and overflow levels for all those things.
    + A ton of settings.
+ Color the month names on the scoreboard. - J10a1n15

### Changes

#### Garden Changes

+ Renamed Visitor Items Needed to Visitor Shopping List. - hannibal2
+ Added biofuel support to the Composter Overlay get from sack logic. - obsidian
+ Updated max reforge fortune for Fungi Cutter. - Alexia Luna
    + Hypixel has increased the base rarity to Epic.
+ Don't count Bountiful as a max reforge fortune on tools. - Alexia Luna
+ /shcroptime now supports k, m and b numbers. - DylanBruner
+ Only show the Garden Vacuum Bag on your own vacuums. - Alexia Luna
+ Jacob Contest feature now has clickable calendar open command when Elite API is not ready yet. - Alexia Luna
+ Mark carrot/pumpkin fortune as completed when giving to Carrolyn after already done. - Alexia Luna
+ Added ability to get unique visitors served without Green Thumb. - Alexia Luna
+ Check if you are sneaking for optimal speed in the Garden and show current speed when wrong. - Alexia Luna
+ Added option to show visitor shopping list in Farming Islands - Erymanthus
    + Show the Visitor Shopping List while on the Farming Islands or inside the Farm in the Hub.
    + This helps to farm the correct amount, especially when in the early game.
+ Made blocks per second calculation more accurate immediately after starting to farm. - hannibal2
+ Warn to open visitor inventory again after partially serving Spaceman. - Alexia Luna
+ Updating the Garden Optimal Speed Display and Warning immediately when changing the setting. - hannibal2
+ Added auto mouse-unlocking on plot teleport in garden. - martimavocado
+ Improve items in sacks display in Visitor Shopping List wording and color. - Alexia Luna
+ Hide Garden Composter reminder while participating in a Farming Contest. - hannibal2
+ Improve Exportable Carrots/Expired Pumpkin tooltip in /ff. - Alexia Luna

#### Rift Changes

+ Rift time now updates correctly in wizard tower and instantly updates the format when toggling max time or
  percentage. - hannibal2
+ Added options to only show the Rift Vermin Tracker with vacuum on inventory and only in West Village. - Empa
    + This now hides the Vermin Tracker by default when not in the surrounding area.
+ Show the Vermin Tracker immediately after joining the rift. - hannibal2

#### Fishing Changes

+ Odger highlight feature tells in description that it is only useful for users without abiphone. - hannibal2
+ Added toggle to count double hook catches as two catches in Sea Creature Tracker. - hannibal2
+ Smarter check if the player is fishing for all fishing related displays. - hannibal2
+ Hide Sea Creature Tracker when wearing full Trophy Hunter armor. - martimavocado
+ Disabled Fishing Profit/SC Tracker in Kuudra. - CalMWolfs
+ Moved Sulphur Skitter Box from Crimson Isle to Fishing -> Trophy Fishing. - hannibal2
+ Fishing Profit Tracker now has better categories. - hannibal2 & Empa
+ No longer showing fishing trackers when holding a fishing rod in the hand. Only showing the trackers when truly
  fishing. - hannibal2
+ Added option to only show the Geyser Box while holding a lava rod. - Obsidian

#### Mining Changes

+ Show Powder Tracker immediately after joining the Crystal Hollows. - hannibal2

#### Bingo Changes

+ Show the guide text when hovering over the missing bingo goal list. - hannibal2

#### Crimson Isle Changes

+ Added option to hide tasks after they've been completed in Crimson Isle Reputation Helper. - martimavocado

#### Inventory Changes

+ Removed flawless gemstones from sack display. - CalMWolfs
    + Hypixel removed them from sacks.
+ Show a warning in Bestiary Display when Overall Progress is not enabled. - HiZe
+ Added option to hide pet candy count on maxed out pets. - CalMWolfs
+ Added option to change the size of the Pet Item Display Icon in inventories. - Thunderblade73
+ Allow putting Water Bottles into Potion Bag. - Alexia Luna
+ Skip unavailable experiments for number of clicks in Super Pair Clicks Alert in the experimentation table. - Alexia
  Alexia Luna
+ Hide non-clickable items in Basket of Seeds, Nether Wart Pouch & Trick or Treat Bag. - Alexia Luna
+ Added support for blocking quick crafting for the new quick craft UI. - hannibal2

#### Dungeon Changes

+ Changed the description of the Dungeon Chat Filter feature to be more descriptive. - Wambo
+ Added options to change exactly what part of the Dungeon Chat Filter should be used. - Wambo
+ Hide F3/M3 guardian Damage Indicators once the professor spawns. - hannibal2
+ Added exact names for the floor 6 giants in the Damage Indicator. - hannibal2
+ Show the laser phase timer even during the hit phase after a successful damage phase skip. - hannibal2

#### Diana Changes

+ Changed the Griffin Burrow Guess text to a centered title instead of an odd line that goes down. - hannibal2
+ Removed unnecessary error messages in Diana Burrow detection. - hannibal2
+ Fixed and improved the Inquisitor Waypoint Share feature. - hannibal2
    + Now it correctly hides burrow waypoints when the "focus" mode is enabled.
    + Shows a pink line to the shared location.
    + Support for the nearest warp feature.
+ Changed the Diana Guess Waypoint word color to only be in blue when actually warping to the nearest warp. - hannibal2

#### Chat Changes

+ Hide new Fire Sale message format. - Thunderblade73
+ Added Hypixel Lobby chat messages "SMP" and "Snow Particles" to the Spam Filter. - walker
+ Added more messages to Hypixel Lobby spam hider.
    + SMP and Snow Particles. - walker
    + Earned mystery dust. - Alexia Luna
+ Added the fire sale ended message to the Fire Sale Chat Hider. - hannibal2
+ Hide pet consumables chat messages on Hypixel main lobby. - Alexia Luna
+ Fire sale chat message hider now also hides the "and x more" part. - hannibal2

#### Command Changes

+ Added /pd short command for party disband. - Empa
+ Added support for number abbreviations in /shskills. - hannibal2
    + "/shskills levelwithxp 750m"
+ Renamed currentXP to xp in /shskills description. - hannibal2

#### Config Changes

+ Using a better title for Area Boss features. - hannibal2

#### Slayer Changes

+ Adding Soul Of The Alpha support to the Area Mini Boss features respawn timer and highlight. - martimavocado

#### GUI Changes

+ The Compact Tab List now has toggle tab support. - hannibal2
+ The tab list now toggles on key press, not key release. This should feel faster now. - hannibal2

#### Misc Changes

+ Tia Relay Helper: Suggest /togglemusic. - Alexia Luna
+ Added option to ignore Everything Chroma in the chat. - VixidDev
+ Added Item Ability Cooldown support for Talbot's Theodolite (the Farming Island Trapper tracker thing). - Erymanthus
+ Fewer election API updates, fewer election API errors. - CalMWolfs
+ SkyHanni's own /wiki command logic now works better in SkyBlock Level guide and allows changing weather to use
  official Hypixel Wiki or the older Fandom Wiki. - Obsidian
+ Added option to hide the already existing F3 SkyBlock Area Debug Feature. - Obsidian
    + This feature shows the current area in SkyBlock while f3 is open.
+ Hovering on cheap items in an Item Tracker now shows the hidden items. - Mikecraft1224
+ Added shader reload capabilities for chroma resource packs. - nea
+ Added option to only show Ender Node tracker while holding a pickaxe. - Thunderblade73
+ Matched XP/h timer with the session timer in the skill ETA display. - HiZe
+ Talbot's Theodolite: Support exact height message. - Alexia Luna

### Fixes

#### Garden Fixes

+ Fixed mushrooms being counted with Common/Uncommon Mooshroom Cow Pet. - Alexia Luna
+ Fixed progress to maxed milestone appearing twice in the crop milestone menu when having milestone 20. - Empa
+ Fixed max crop milestone display being too long in the crop milestone menu. - obsidian
+ Fixed Mooshroom Cow Perk counter when farming sugar cane/cactus with Mooshroom Cow. - Alexia Luna
+ Show an error message for the commands /shcropsin and /shcroptime if show money per hour display is not loaded. -
  hannibal2
+ Auto-fixing plots marked as pests when killing all pests without SkyHanni earlier. - hannibal2
+ Fixed error message that nearest pests cannot get removed properly. - hannibal2
+ Fixed grammar in Jacob Contest chat messages. - Alexia Luna
+ Fixed rarity error for items thrown around when using Sprayonator. - hannibal2
+ Added cooldown to Garden Warp Commands. - Empa
+ Fixed the detection of Anita and Jacob visitors. - hannibal2
+ Fixed the pets menu detection for /ff. - martimavocado
+ Fixed Anita and Jacob workaround working outside of garden. - CalMWolfs
+ Fixed opening visitor Milestones menu not loading visitor amount for /ff. - martimavocado
+ Fixed Sensitivity Reducer still working when switching from the garden to the hub. - martimavocado
+ Fixed a rare farming weight API error. - CalMWolfs
+ Fixed mouse rotation unlocks after doing /warp garden with Sensitivity Reducer on. - martimavocado
+ Fixed wording of composter fuel warning. - Alexia Luna
+ Fixed Garden plot menu icon edit mode copies stack size. - hannibal2
+ Fixed the wrong color code on the visitor shopping list sacks number line. - hannibal2
+ Fixed Anita Medal Profit Display using wrong items. - hannibal2
+ Fixed Box of Seeds not working with /shcroptime. - Alexia Luna
+ Fixed Farming Weight Display sometimes not showing when joining the Garden. - CalMWolfs

#### Combat Fixes

+ Fixed corrupted Kada Knight getting detected as Revenant Slayer mini boss. - hannibal2
+ Fixed Daily Kuudra part of Crimson Isle Reputation Helper not detecting completed runs. - hannibal2
+ Fixed wrong calculation when zero bosses killed in slayer profit trackers. - hannibal2
+ Hide No Fishing Bait warning during Kuudra fight. - hannibal2
+ Fixed Dungeon and Kuudra party finder join message not detecting in party member tab complete. - CalMWolfs
+ Fixed Fire Veil Wand circle shows on left-clicking. - hannibal2
+ Fixed Ashfang Freeze Cooldown being off by one second. Fixed Fire Veil line gets shown even while frozen. - hannibal2
+ Fixed time until the next area mini boss spawns being off by one second. - hannibal2
+ Fixed Reputation Helper in Crimson Isle showing incorrect sack amount. - CalMWolfs
+ Fixed the ender slayer laser phase timer being inaccurate in the Damage Indicator. - hannibal2

#### Dungeon Fixes

+ Fixed the M3 reinforced guardian not getting detected in the Damage Indicator. - hannibal2
+ Gave Damage Indicator mobs correct names on floor 1, 2, 3 and 6. (Removed "Generic Dungeon Boss") - hannibal2
+ Fixed kill time of slayer in Damage Indicator to be off by one second. - hannibal2
+ Fixed all Damage Indicator boss timers in Dungeons being off by a second. - hannibal2
+ Fixed Bonzo phase 1 does not get detected properly for Damage Indicator. - hannibal2
+ Fixed Dungeon Clean End sometimes not deactivating when chest spawned. - hannibal2
+ Fixed F3/M3 guardian spawn timer in Damage Indicator. - hannibal2
+ Fixed Highlight Clicked Chest in water puzzle room not showing. - hannibal2

#### Diana Fixes

+ Fixed close griffin burrow detection with particles failing sometimes. - hannibal2
+ Fixed Diana mobs being invisible because of Fix Ghost Entities and four season pet skin. - hannibal2
+ Fixed a rare bug that leaves ghost burrows around. - hannibal2
+ Properly deleting Diana target markers when manually clearing griffin burrows by talking to Diana NPC. - hannibal2
+ Properly resetting internal Diana data on clearing waypoints via NPC. - hannibal2
+ Fixed the missing color code in the inquisitor deletion message. - jani

#### Mining Fixes

+ Fixed an error when showing all elements in Powder Tracker. - hannibal2
+ Fixed powder tracker detecting gemstone chat messages. - CalMWolfs
+ Fixed Mining Chat Filter not hiding gemstone messages. - CalMWolfs
+ Fixed names for the Crystal Hollows Mining Areas feature. - Alexia Luna
+ Fixed detection of gold and diamond essence gain chat message when powder mining. - CalMWolfs
+ Fixed powder mining start/end detection in Powder Tracker. - CalMWolfs
+ Fixed Ruby Gemstone detection in powder chest reward. - J10a1n15

#### Rift Fixes

+ Fixed vampire slayer damage indicator not working during Derpy. - hannibal2

#### Fishing Fixes

+ Fixed Reindrake mob, Frosty NPC and frosty the snow blaster shop counting as sea creatures in the barn fishing
  timer. - hannibal2
+ Fixed trophy fish chat message detection. - Empa
+ Fixed Sheep pet triggering Geyser Box and fixed particles being permanently hidden after throwing bobber at it once. -
  Empa
+ Fixed fishing profit tracker stops working when trophy fishing for 10 minutes. - hannibal2
+ Fixed adding drops to Fishing Profit Tracker while not actually fishing. - hannibal2
    + This fixes red mushroom picking up with Mooshroom Cow pet while farming.
    + This does not fix wrongfully adding drops while moving items in inventory.
+ Fixed fishing trackers appearing when rod swapping. - hannibal2
+ Fixed fishing bait change spam. - hannibal2
+ Fixed no bait warning appearing at the wrong moment or not appearing at all. - hannibal2
+ Fixed Crimson Isle Fishing Geyser Box showing even when very far away. - Obsidian

#### Inventory Fixes

+ Fixed hide non-clickable items not working in some bazaar pages. - hannibal2
+ Fixed rogue sword ability taking into account mage cooldown reduction. - Empa
+ Reset item ability cooldowns on the world switch. - hannibal2
+ Fixed Crap Hat of Celebration not getting detected as accessory in Hide Not Clickable Items. - Empa
+ Fixed rune price calculation in Chest Value. - hannibal2
+ Fixed Power Stone Guide Highlight shows in other inventories when exiting via command. - hannibal2
+ Added options to hide Helmet Skins, Armor Dyes or Runes from Estimated Item Value Calculation. - hannibal2
+ Fixed Divine Gift and Flash enchants showing the wrong/no price in Estimated Item Value. - jani
+ Fixed showing the Piece of Wizard Portal earned duplicate. - Thunderblade73
+ Fixed shift-click NPC sell not working for menus with different sizes and full inventories. - Thunderblade73
+ Fixed an error with the shift-click NPC sell feature. - Thunderblade73
+ Ignore Shift-Click NPC Sell when right-clicking a sack. - Thunderblade73
+ Fixed pet level stack size - Thunderblade73
+ Fixed enchantment names and pet names in the chest value feature. - hannibal2
+ Fixed pet names in item profit trackers. - hannibal2
+ Fixed Book Bundle showing the wrong number of books in Estimated Item Value. - Empa

#### Bingo Fixes

+ Fixed detecting bingo profile while visiting other players bingo island. - hannibal2
+ Fixed performance issues with Bingo Minion Craft Helper. - hannibal2
+ Fixed Bingo Minion Craft Helper not detecting crafted tier one minion. - hannibal2
+ Fixed rare error reading and rendering Bingo Card Tips in Bingo inventory. - Thunderblade73

#### Chat Fixes

+ Fixed poisoned candy potion chat message not getting compacted. - Alexia Luna
+ Fixed the fire sale chat message hider again. - CalMWolfs

#### GUI Fixes

+ Fixed items in SkyHanni GUI elements rendering over minecraft menus. - Thunderblade73
+ Fixed GUI Editor hotkey working while inside a NEU PV text box. - Thunderblade73
+ Fixed render overlapping problem with chat, SkyHanni GUIs and title. - Thunderblade73
+ Fixed GUI positions moving into the bottom-right corner when leaving the GUI position editor while pressing the mouse
  button on next reopen. - hannibal2
+ Fixed parts of Compact Tab List being uncolored. - CalMWolfs
+ Fixed Compact Tab List' Toggle Tab not working when using Patcher. - hannibal2
+ Fixed Skill progress display size too small when not using the progress bar. - Thunderblade73
+ Fixed the skill progress bar trying to get out of the screen. - HiZe
+ Fixed the negative time remaining in the skill ETA display. - HiZe
+ Fixed skill timer. - hannibal2
+ Fixed overflow level goal in skills tooltips. - HiZe

#### Winter Fixes

+ Fixed Unique Gifting Opportunities working with Golden Gift. - CalMWolfs
+ Fixed Frozen Treasure Tracker showing wrong Compact Procs number. - CalMWolfs

#### Command Fixes

+ Fixed /gfs not working. - Thunderblade73
+ Fixed /sendcoords command not working. - CalMWolfs
+ Fixed open bazaar command sending color code as well. - Thunderblade73
+ Fixed /gfs not working with spaces in item name. - Thunderblade73
+ Fixed multiple edge cases with /gfs. - Thunderblade73
+ Fixed commands /shskill levelwithxp/xpforlevel. - HiZe

#### Config Fixes

+ Fixed rare profile detection bugs. - Alexia Luna

#### Misc Fixes

+ Maybe fixed Tia Relay Helper. - Thunderblade73
+ Fixed wording in trackers when the item is newly obtained. - hannibal2
+ Fixed titles not showing above other SkyHanni GUI elements all the time. - Thunderblade73
+ Fixed Daily City Project Reminder is still working on already-released projects. - Alexia Luna
+ Fixed a typo in Odger Waypoint config. - Empa
+ Fixed NPC typos in config. - absterge
+ Fixed rare error in Harp Features. - Thunderblade73
+ Fixed some getItemStack errors. - CalMWolfs
+ Fixed Minion XP calculation not working when having different mouse settings. - Thunderblade73
+ Fixed /gfs tab complete. - martimavocado
+ Fixed /warp is command replace. - hannibal2
+ Fixed Queued Gfs description. - Thunderblade73
+ Fixed ender bow ability time (30s -> 5s). - hannibal2
+ Reputation Helper now shows Kuudra Runs for barbarian faction as well. - hannibal2
    + Daily Kuudra reputation also works for barbarian faction, not only mage.
+ Fixed item trackers duplicating items when taking items out of storage or chest. - hannibal2
+ Fixed current mayor is taking 20 minutes to get loaded. - Empa
    + This fixes active Diana detection not working, making the workaround (/sh always diana) unnecessary.
+ Fixes Ghost Entities. - hannibal2 & nea & Thunderblade73
    + Removes ghost entities caused by a Hypixel bug. This included Diana, Dungeon and Crimson Isle mobs and nametags.

### Technical Changes

+ Migrate Hypixel API to v2. - hannibal2
+ Added SackDataUpdateEvent. - CalMWolfs
+ Fixing a mac crash in dev environment automatically. - CalMWolfs
+ Bingo repo change: Make note of an alternative title, and create a guide field for the actual guide text. - hannibal2
+ Moved Tia Relay Helper chat messages into repo patterns. - Thunderblade73
+ Added Dark Auction as IslandType and fixed IslandType detection for dungeons. - j10a1n15
+ Modify instead of blocking trophy fishing and sea creature chat messages. - appable
+ Changed regex in case Hypixel changes color codes for island names in the tab list. - Empa
+ Extract FirstMinionTier logic from the Bingo Minion Craft Helper to better analyze the performance problems some users
  have. - hannibal2
+ Moving minion craft helper fully over to neu internal names. - hannibal2
+ Added information about trackers to the Discord FAQ. - j10a1n15
+ Defined the way how dependent PRs should be written in contributing.md. - Thunderblade73
+ Added debug command /shtestburrow. - hannibal2
+ Using SkyHanniMod.coroutineScope instead of CoroutineScope(Dispatchers.Default). - CalMWolfs
+ Creating function addTotalProfit for item trackers. - hannibal2
+ Tell people how to name a pattern variable. - CalMWolfs
+ Typo fixes in contributing md. - CalMWolfs
+ Make Repo Pattern keys more consistent for Farming Gear. - CalMWolfs
+ Added options to ban specific imports in some packages. - nea
+ Don't allow uppercase Repo Pattern keys and added a more descriptive error. - CalMWolfs
+ Moved party API chat messages to Repo Pattern. - CalMWolfs
+ Allowing nullable parameters for regex functions matches() and find(). - hannibal2
+ Cleanup calculate() in Estimated Item Value. - walker
+ Added DebugDataCollectEvent. - hannibal2
+ Added Diana Burrow Nearest Warp to /shdebugdata - hannibal2
+ Changed debug commands: - hannibal2
    + /shdebugwaypoint -> /shtestwaypoint
    + /shdebugtablist -> /shtesttablist
    + /shdebugdata -> /shdebug
+ Moving dungeons and slayer related debug data into DebugDataCollectEvent. - hannibal2
    + Allowing to hide/search for specific debug data with /shdebug <search> - hannibal2
+ Per default only active/relevant data is shown.
+ Added visitor status to /shdebug - hannibal2
+ Added hotswap detection and reloading all listeners on hotswap. - nea
+ Categorized every item using the rarity lore line. - Thunderblade73
+ Used better way of getting the item rarity. - Thunderblade73
+ Added function SlotClickEvent.makeShiftClick(). - Thunderblade73
+ Used a better bug fix for DelayedRun sync issues. - Thunderblade73
+ Creating and using TimeLimitedCache and TimeLimitedSet instead of guava cache. - hannibal2
+ Bring back the deleted item modifier test and fix the issue causing it. - CalMWolfs
+ Better error handling when an unknown crimson isle quest is detected. - hannibal2
+ Marked old number formatting code as deprecated. - hannibal2
+ Added the SkyHanni icon to the IntelliJ profile view. - Erymanthus
+ Fixed key name in utils patterns. - CalMWolfs
+ Using NEUInternalName in the Reputation Helper Quest. - CalMWolfs
+ Limit RAM to 4 GB in the development environment. - CalMWolfs
    + This is just the default and can be changed if needed.
+ Made /shupdaterepo better. - CalMWolfs
+ Added alignment support to Renderable. - Thunderblade73
+ Added support for dynamic Y size in Renderables. - Thunderblade73
+ Added outgoing chat log to /shchathistory. - nea
+ Added sending mining events to Soopy's API to test for new Mining Event feature. - CalMWolfs
+ Added /shcopybossbar to copy bossbar - Erymanthus
+ Splitting many utils functions from LorenzUtils up into other classes: ChatUtils, CollectionUtils, ConditionalUtils. -
  Thunderblade73
+ A ton of code cleanup, mainly on imports. - Thunderblade73 & hannibal2
+ Added mod identification for outgoing mod calls and show this data in /shchathistory. - nea
+ Sensitivity Reducer changes: clearer error message, using ChatUtils and wrong values in debug data. - martimavocado
+ Bumped NEU to version 2.1.1 pre-5. - CalMWolfs
+ Fixed an NPE in ReflectionUtils.shPackageName. - Thunderblade73
+ Cleaned up string pluralization methods. - Alexia Luna
+ Moved many regex patterns in the repo and code cleanup. - CalMWolfs
+ Improved purse pattern. - j10a1n15
+ Added cache to item stack → neu internal name. - hannibal2
+ Added cache to internal name → item name. - hannibal2
+ Added debug option to show SkyHanni item name in item lore. - hannibal2
+ Created ActionBarUpdateEvent and used it. - CalMWolfs
+ Added Rounded Rectangles Renderable. - VixidDev
+ Added progress Bar Renderable. - Thunderblade73
+ Added Horizontal Container Renderable. - Thunderblade73
+ Added GetFromSackAPI. - Thunderblade73
+ Improved Packet Test. - Thunderblade73
+ Increases the feature set of the packet test and improves usability.
+ Fishing Tracker using SH Repo over NEU recipes. - hannibal2 & Empa
+ Deprecate LorenzUtils.sendCommandToServer. - hannibal2
+ Adds a chroma shader to be used on non-textured GUI elements. - VixidDev
+ Added /shdebug Garden Next Jacob Contest. - hannibal2
+ Make future NPC price fetch errors better debuggable. - CalMWolfs
+ Removed duplicate pet-level detection logic. - hannibal2
+ Changed PreProfileSwitchEvent to ProfileJoinEvent. - Alexia Luna
+ Cleanup some repo pattern formatting. - CalMWolfs
+ Make Hypixel items API response a proper JSON object. - CalMWolfs
+ Created utils function String.formatDouble(): Double. - hannibal2
+ Fixed ReplaceWith auto-replace feature from IDEs for deprecated functions. - hannibal2
+ Fixed the BuildList name in part of the stack trace. - hannibal2
+ Cleanup error manager code. - hannibal2
+ Fixed /shdebug without parameter showing everything instead of only important data. - hannibal2
+ Fixed internal item name resolving problems. - hannibal2
+ Extracted item name resolving logic into own class. - hannibal2
+ Added debug command /shtestitem. - hannibal2
+ Made String.formatDouble() and formatLong()return nullable. - hannibal2
+ /gfs tab complete now uses NEU's Repo instead of SkyHanni Repo. - CalMWolfs
+ Creating NeuRepositoryReloadEvent as wrapper for less confusion. - CalMWolfs
+ Made ErrorManager compact stack trace even more compacter. - hannibal2
+ Added holdingLavaRod and holdingWaterRod in FishingAPI. - Obsidian
+ Fixed HighlightMissingRepoItems. - CalMWolfs
+ Added String.formatLongOrUserError(). - hannibal2
+ Use duration for time in ServerRestartTitle. - hannibal2
+ Added error handling for ServerRestartTitle problems. - hannibal2

## Version 0.22

### New Features

#### Garden Features

+ Added Garden Vacuum Pests in Pest bag to item number as stack size. - hannibal2
    + Enable via /sh vacuum.
+ Added Pests to Damage Indicator. - hannibal2
    + Enable Damage Indicator and select Garden Pests.
+ Change how the pest spawn chat message should be formatted. - hannibal2
    + Unchanged, compact or hide the message entirely.
+ Show a Title when a pest spawns. - hannibal2
+ Show the time since the last pest spawned in your garden. - hannibal2
    + Option to only show the time while holding vacuum in the hand.
+ Show the pests that are attracted when changing the selected material of the Sprayonator. - hannibal2
+ Added Garden only commands /home, /barn and /tp, and hotkeys. - hannibal2
+ Showing a better plot name in the scoreboard. Updates faster and doesn't hide when pests are spawned. - hannibal2
+ Show a display with all known pest locations. - hannibal2
    + Click to warp to the plot.
    + Option to only show the time while holding vacuum in the hand.
+ Mark the plots with pests on them in the world. - hannibal2
+ Press the key to warp to the nearest plot with pests on it. - hannibal2
+ Draw plot borders when holding the Sprayonator. - HiZe
+ Added Spray Display and Spray Expiration Notice. - appable
    + Show the active spray and duration for your current plot.
    + Show a notification in chat when a spray runs out in any plot. Only active in the Garden.

#### Fishing Features

+ Added Barn Fishing Timer to Jerry's Workshop and Crimson Isle. - martimavocado
+ Added Fishing Tracker and changed trackers in general. - hannibal2
    + This tracker GUI behaves the same way as the Slayer Tracker: Allows for single item remove or hide
    + Counts coin drops from chat.
    + Mark the amount in green when recently gained the item.
    + Option to hide the Fishing Tracker while moving.
    + Option to hide all Trackers while Estimated Item Value is visible.
    + Option to change the default display mode for all trackers.
    + The hidden flag for items in Item Trackers is now shared between total view and session view.
    + Option to exclude hidden items in the total price calculation.
    + Option to change the display mode that gets shown on default: Total, Current or remember last.

#### Winter Features

+ Added Unique Gifting Opportunities. - nea
    + Highlight players who you haven't given gifts to yet.
    + Only highlight ungifted players while holding a gift.
    + Make use of armor stands to stop highlighting players. This is a bit inaccurate, but it can help with people you
      gifted before this feature was used.
+ Added Unique Gifted users counter. - hannibal2
    + Show in a display how many unique players you have given gifts to in the winter 2023 event.
    + Run command /opengenerowmenu to sync up.
+ Jyrre Timer for Bottle of Jyrre. - walker
    + A timer showing the remaining duration of your intelligence boost.
    + Option to show the timer when inactive rather than removing it.

#### Bingo Features

+ Show the duration until the next hidden bingo goal tip gets revealed. - hannibal2
+ Added support for tips in hidden bingo card display. - hannibal2
+ Added support for 'found by' info in bingo card. - hannibal2
+ Added Bingo Goal Rank as stack size in Bingo Card. - Erymanthus
+ Added the option to only show tier 1 Minion Crafts in the Helper display when their items needed are fully
  collected. - hannibal2
+ Added the option to click in the bingo card viewer on goals to mark them as highlighted. - hannibal2
    + If at least one goal is highlighted, non-highlighted goals will be hidden.
+ Send a chat message with the change of community goal percentages after opening the bingo card inventory. - hannibal2

#### Diana Features

+ Added Diana Profit Tracker. - hannibal2
    + Same options as slayer and fishing trackers.
+ Added highlight for the Minos Inquisitors to make them easier to see. - Cad
+ Added Mythological Mob Tracker. - hannibal2
    + Counts the different mythological mobs you have dug up.
    + Show percentage how often what mob spawned.
    + Hide the chat messages when digging up a mythological mob.
+ Added Diana Chat hider. - hannibal2
    + Hide chat messages around griffin burrow chains and griffin feather drops and coin drops.

#### Inventory Features

+ Added bottle of Jyrre time overlay in stack size feature. - HiZe
+ Added show special edition number as stack size when below 1k. - hannibal2
+ Added Copy Underbid Price. - hannibal2
    + Copies the price of an item in the "Create BIN Auction" minus 1 coin into the clipboard for faster under-bidding.
+ Highlight your own lowest BIN auctions that are outbid. - hannibal2

#### Minion Features

+ Shows how much skill experience you will get when picking up items from the minion storage. - Thunderblade73

#### Chat Features

+ Hide the repeating fire sale reminder chat messages. - hannibal2

#### Event Features

+ Added Waypoints for 2023 Lobby Presents. - walker
+ Added New Year Cake Reminder. - hannibal2

#### Stranded Features

+ Highlights NPCs in the stranded menu that are placeable but haven't been placed. - walker

#### Rift Features

+ Added Vermin Tracker. - walker

### Changes

#### Garden Changes

+ Added option to enable/disable the vacuum bag item number being capped to 40. - hannibal2
+ Automatic unlocking /shmouselock when teleporting in the garden. - hannibal2
+ Don't hide messages from Jacob. - Alexia Luna
    + This is a workaround for wrongly hidden Jakob messages.
+ Show the hint to open Configure Plot only if the pest display is incorrect. - hannibal2
+ Added the "plot" word to the sidebar again (only if there are no pests in garden). - hannibal2
+ Hide the Composter Overlay in composter inventory while the Estimated Item Value is visible. - hannibal2
+ Made the wording of "no pest spawned yet" message more clear. - hannibal2
+ Not only show the waypoint for infested plots, also show their waypoints in the world. - hannibal2
+ Use different colors in the tab list depending on the pest count. - Alexia Luna
+ Highlight the boosted crop contest in all Jacob's Contest displays. - Alexia Luna
+ Added Delicate 5 to visitor drop counter and visitor block refuse and highlighter. - hannibal2
+ Block visitor interaction for dedication cycling is now disabled by default. - hannibal2
+ Added an option to only warn for specific crop contests. - Obsidian
+ Added an option to show plot borders for a given number of seconds after holding a vacuum. - HiZe
+ Added command /shclearcontestdata to Reset Jacob's Contest Data. - martimavocado
+ Display Farming Fortune reduction from pests on the HUD. - Alexia Luna
+ Allow showing optimal speed warning without HUD enabled. - Alexia Luna
+ Highlight Rancher Boots speed in green when a Racing Helmet is equipped. — walker
+ Show the closest plot border when outside a garden plot. - hannibal2
    + Especially useful when building farms.
+ Show the garden build limit with F3+G on. - hannibal2
+ Added an option to show if the plot is not sprayed. - Alexia Luna
+ Changed pest spawn message format. - hannibal2
+ Improve the precision of the compost empty timer. - appable
+ Added Harvest Harbinger and Pest Repellent non-god effects. - Alexia Luna
+ Draw Plot Border change: Pest Finder now always renders over the Spray Selector or Garden Plot Border. - hannibal2
+ Make the vacuum bag show 40+ instead of 40 when capped. - Alexia Luna
+ Default disabled Anita Shop, SkyMart and Sacks display. - hannibal2
+ Default disabled Farming Weight feature: Show LB Change. - hannibal2
+ Default disabled many garden features: - hannibal2
    + Composter Display
    + Best Crop Time
    + Optimal Speed Display
    + True Farming Fortune
    + Garden Level Display
    + Armor Drop Tracker
    + Dicer Drop Tracker
    + Money Per Hour
+ Renamed Farming Armor Drops to Armor Drop Tracker. - hannibal2
+ Renamed RNG Drop Counter to Dicer RNG Drop Tracker. - hannibal2

#### Fishing Changes

+ Show the fishing tracker for a couple of seconds after catching something even while moving. - hannibal2
+ Show breakdown of different shark types caught during festivals. - Cad

#### Winter Changes

+ Hiding Unique Gifted Players Highlighting for ironman and bingo while not on those modes. - Thunderblade73
+ Make the Unique Gift Counter and Unique Gifting Opportunities highlight only active during December. - hannibal2
+ Added Frozen Bait and Einary's Red Hoodie to Frozen Treasure Tracker. - hannibal2

#### Chat Changes

+ Added fire sale messages in the hub to the chat message filter. - hannibal2
+ Added compact potion message support for splash messages and for Poisoned Candy I. - walker
+ Added "fire sale starting soon" message to fire sale chat hider. - hannibal2

#### Bingo Changes

+ Option to remove the background difficulty color in the bingo card inventory when the goal is done. - hannibal2
+ Mark the background difficulty gray for unknown goals. - hannibal2
    + This is no longer needed as all 20 hidden goals are known now, but we now have this support for the next extreme
      bingo with hidden goals.
+ Added the community goal percentage to the bingo card display. - hannibal2
+ Saving minion craft helper crafted tier 1 minions and bingo card goals per bingo session/month. - hannibal2

#### Diana Changes

+ Resetting the guess and burrow locations when clearing the burrows at Diana NPC. - hannibal2
+ Removed Diana "Smooth Transition" and replaced it with "Line to Next". - hannibal2

#### Inventory Changes

+ Copy Underbid Price now supports stack sizes of more than one. - hannibal2

#### Misc Changes

+ Titles sent by SkyHanni look better now. - Cad
+ Added support for show XP gained from wheat minion in hub. - Thunderblade73
+ Option to change the color of Marked Players in chat and in the world. - walker & hannibal2
+ Added support for the new item rarity "Ultimate Cosmetic". - hannibal2
+ Hide "Winter Island Close" timer during the month of December. - hannibal2
+ Changes Ctrl+V in signs to better sign editing. - Obsidian
    + Allows pasting (Ctrl+V), copying (Ctrl+C), and deleting whole words/lines (Ctrl+Backspace/Ctrl+Shift+Backspace) in
      signs.
+ Improve accuracy of movement speed display. - Alexia Luna
+ Added toggle to hide autopet messages. - CalMWolfs
+ Not only Slayer, also Fishing and Diana item drops will now show in chat & title when over a custom defined price. -
  hannibal2
+ Added Support to read BadLion sendcoords format. - Cad
+ Added an option to not show cooldown when ability is ready. - Obsidian
+ Added an option to highlight dungeon perm/vc parties. - Cad
+ Added Glowing Mush Mixin support to the Non-God Pod display. - jani
+ Added options to hide cheap items in item profit trackers. - hannibal2
+ Bazaar Best Sell Method now warns if an unknown bazaar item is detected. - hannibal2
+ SkyHanni position editor hotkey now work outside SkyBlock as well. - hannibal2
+ Dungeon party finder highlight features now works outside Dungeon Hub as well. - hannibal2
+ Added Rogue Sword item ability support. - hannibal2

### Fixes

#### Garden Fixes

+ Fixed pest damage indicator not working for some pests. - hannibal2
+ Fixed pest kill detection. - hannibal2
+ Fixed /tp <plot name> not working with uppercase characters. - hannibal2
+ Fixed total equipment fortune in /ff. - Alexia Luna
+ Fixed Locust pest not getting detected in damage indicator. - hannibal2
+ Fixed Pest Spray Display showing outside the garden. - hannibal2
+ Fixed pest detection when more than 3 pests are spawned at once. - hannibal2
+ Fixed showing on the scoreboard  "garden outside" immediately after teleporting to a plot. - hannibal2
+ Fixed visitor timer counting down too fast sometimes. - hannibal2
+ Fixed Mooshroom cow Perk display not showing when maxed. - hannibal2
+ Show a text around the new year that the calendar is not loaded for the next Jacob Contest. - hannibal2
+ Fixed visitor reward item refuse inconsistencies. - hannibal2
+ Fixed wrong base 100ff calculations in the farming fortune needed display. - Alexia Luna
+ Fixed showing Sprayonator plot grid overlay outside garden. - HiZe
+ Fixed an error message in the composter inventory when hovering over some items. - hannibal2
+ Correctly load the plot names of locked plots from inventory. - hannibal2
+ Fixed the boosted crop not being highlighted during contest participation. - Alexia Luna
+ Fixed farming weight leaderboard showing new position as -1 sometimes. - Alexia Luna
+ Fix typo with Not Clickable Items in Composter. - absterge
+ Added missing preview number from Visitor Drop Statistics. - absterge
+ Fixed error message with newly bought fungi cutter. - hannibal2
+ Ignoring custom NEU items like copper that causes wrong coins per copper price. - hannibal2
+ Fixed rancher boots speed stack size. - walker
+ Fixed overflow garden level detection. - hannibal2
+ Fixed pet level up check in /ff. - Alexia Luna
+ Fixed next visitor time while farming. - Alexia Luna
+ Fixed sixth visitor warning title showing when disabled. - Alexia Luna
+ Show not revealed brackets in the Jacob Contest time needed display. - hannibal2
+ Fixed wrong Rancher Boots item stack size color in combination with Black Cat or Racing Helmet. - hannibal2
+ Fixed showing medal icons in Jacob inventory. - hannibal2
+ Fixed SkyMart items showing the wrong profit when having other items as costs. - hannibal2
+ Hide "Not sprayed!" text while in the barn or outside the garden area. - hannibal2
+ Fixed armor drop tracker not instantly visible. - hannibal2
+ Fixed Jacob Contest Warning. - hannibal2

#### Bingo Fixes

+ Hide the long hint line in the Bingo Goal Display. - hannibal2
+ Show community goals in the Bingo Display correctly. - hannibal2
+ Hide enchanted tools in Minion Craft Helper. - hannibal2
+ Opening the bingo card will no longer crash the game sometimes. Showing an error message instead. - hannibal2

#### Minion Fixes

+ Fixed Minion XP display not showing sometimes. - Thunderblade73
+ Updating the Minion XP display when the minion picks up a new item while inside the inventory. - hannibal2
+ Fixed minion features disappear inside the minion inventory when picking up an item. - hannibal2

#### Fishing Fixes

+ Fixed Water Hydra warning showing up multiple times. - Cad
+ Fixed Shark Message missing a color code. - jani
+ Fixed /shresetfishingtracker description - absterge
+ Fixed barn fishing reset hotkey triggering while inside a GUI. - hannibal2
+ Fixed Rare Sea Creature Warning/Highlight during Derpy. - hannibal2

#### Diana Fixes

+ Fixed the closest burrow warp point being off, especially for the castle. - hannibal2
+ Fixed show inquisitor waypoints outside the hub. - Cad
+ Fixed Minos Inquisitor highlighting not working while the mob is getting damage. - hannibal2

#### Chat Fixes

+ Fixed an error message on /pt. - nea
+ Fixed the fire sale filter when a rune is selling. - j10a1n15
+ Fixed bestiary compact message. - hannibal2
+ Fixed a typo in the Player Chat Symbol config. - walker

#### Dungeon Fixes

+ Fixed Hide Healer Fairy. - hannibal2

#### Misc Fixes

+ Fixed Item Tracker not ignoring manual sack movements. - hannibal2
+ Fixed showing yourself green with Unique Gifting Opportunities. - hannibal2
+ Fixed NPC messages getting detected as player messages. - CalMWolfs
+ Hide Scavenger 5 on an Ice Spray Wand and Replenish on an Advanced Gardening Hoe/Axe for the Estimated Item Value. -
  hannibal2
+ Fixed an error when the king talisman helper does not find the king in range. - hannibal2
+ Fixed control/modifier key logic on Apple devices. - walker
+ Fixed lag spikes when downloading updates. - nea
+ Fixed showing the Minion XP display in the Bazaar. - Thunderblade73
+ Fixed the city project time remaining "soon!" error. - hannibal2
+ Fixed Slayer Profit Tracker display and price problems with Wisp's Ice Flavored Water Potion. - hannibal2
+ Fixed an error message when closing the wheat minion in the Hub. - Thunderblade73
+ Fixed /locraw sending outside Hypixel. - walker
+ Fixed finished city project still reminding and suggests buying items. - hannibal2
    + Open the city project inventory once again to fix warnings correctly.
+ Fixed kick alert triggering instantly. - Alexia Luna
+ Fixed daily boss kill detection in Crimson Isle Reputation Helper. - hannibal2
+ Fixed Estimated Item Value error in Jerry's Island Deliveries menu. - hannibal2
+ Fixed the /gfs command counting in the Slayer Profit Tracker. - hannibal2
+ Fixed the Fire Veil effect and item ability cooldown not working when clicking in the air. - hannibal2
+ Fixed broken area mini boss highlight during Derpy. - hannibal2
+ Fixed Special Zealot Highlight during Derpy. - hannibal2
+ Fixed the left-click ability detection of the Gyro Wand. - hannibal2
+ Fixed non-god pot effects display staying after profile switch. - hannibal2
+ Fixed broken percent to tier icon in bestiary display when bestiary is maxed. - hannibal2
+ Fixed config migration errors with Chroma, Crop Milestones, Chest Value and Item Trackers. - hannibal2

#### Config Fixes

+ Fixed a typo in config. - walker

### Technical Details

+ Code cleanup in many files. - walker & hannibal2
+ Moved the JSON object files into another package. - walker
+ Replaced SkyHanniMod.feature.garden with GardenAPI.config. - hannibal2
+ Added MessageSendToServerEvent. - hannibal2
+ Added GardenPlotAPI, support for detecting the current slot of the player. - hannibal2
+ Updated .editorconfig file to better support imports. - Thunderblade73
+ Migrate Integer to Enums in Config. - walker
+ Using a broken config no longer resets the config in dev env. - hannibal2
+ Auto-removing all labels of PRs on merging/closing. - hannibal2
+ Changed OwnInventoryItemUpdateEvent to be called synced to the main thread. - hannibal2
+ romanToDecimalIfNeeded -> romanToDecimalIfNecessary. - hannibal2
    + For more context: https://chat.openai.com/share/502571b5-8851-4047-b343-3b1475ca8a88
+ Added the debug feature SkyHanni Event Counter. - hannibal2
+ Fix Consecutive Spaces in RegEx. - walker
+ No longer creating new regex pattern elements each time in DungeonDeathCounter. - walker
+ Changed DungeonChatFilter to use lists of patterns. - walker
+ Code cleanup in DungeonMilestoneDisplay. - walker
+ Code cleanup and removed .matchRegex() - walker
+ Misc pattern optimizations. - walker
+ Moving the bingo goal list into BingoAPI. - hannibal2
+ Created BingoGoalReachedEvent. - hannibal2
+ Created Matcher.groupOrNull. - walker
+ cleanPlayerName respects playerRankHider option now. - hannibal2
+ Replaced ItemWarnEntry with VisitorReward. This should fix some errors. - hannibal2
+ GardenNextJacobContest now uses SimpleTimeMark. SimpleTimeMark can now be saved in the config directly and compared - hannibal2
+ No longer sending contest data to elite close to new year. - hannibal2
+ Added RepoPatterns. - nea
+ Use LorenzToolTipEvent over ItemTooltipEvent if possible. - hannibal2
+ Added an abstract error message on LorenzToolTipEvent error. - hannibal2
+ Added test command /shsendtitle - Cad
+ Saving bingo goal data into the config. - hannibal2
+ Added WorldEdit region selection preview support. - nea
    + Command /shworldedit and right/left clicking with a wood axe work.
+ Fixed error message in the "/shconfig set" command. - Thunderblade73
+ Add a check for the SkyHanni repository ID in publish. - walker
+ Cleanup getItemsInOpenChest. - walker
+ Changed MinionCraftHelper to use NeuInternalName. - walker
+ Added a separate debug hotkey for Bypass Advanced Tab List. - hannibal2
+ Added Config Transform function. - walker
+ Migrate Deprecated Config Values to Enums. - walker
+ Migrate Config Value in EliteFarmingWeight. - walker
+ Add limbo time PB. - martimavocado
+ Added helper functions for armor. - walker
+ Created PetAPI.isCurrentPet. - hannibal2
+ Refactored Duplicate Code in SkyHanniInstallerFrame. - walker
+ Removed unnecessary non-capturing groups. - walker
+ ComposterOverlay now uses NEUInternalName. - walker
+ Added the debug command /shfindnullconfig. - hannibal2

## Version 0.21.1

### New Features

+ Organised the config into sub categories. - nea & walker
+ Wrong crop milestone step detection. - hannibal2
    + When opening the crop milestone menu, a chat message is sent if Hypixel's crops per milestone level data is
      different from SkyHanni's.
    + You can use this to share your hypixel data with SkyHanni via the discord.
    + This will allow us to fix the crop milestone features quicker, as we currently do not have accurate data for this.
    + If you don't want to share anything, you can disable the chat message in the config with /sh copy milestone data.

### Changes

+ /shtrackcollection now supports sack messages. - hannibal2
+ Changed formatting of coin value to be more consistent over multiple features. - hannibal2
+ Made skill level as item number no longer default enabled. - hannibal2

### Fixes

+ Fixed the wrong coloring of hidden items in Slayer Profit Tracker. - hannibal2
+ Added support for NEU Heavy Pearl TO-DO fix working without nether sacks as well. - hannibal2
+ Fixed Estimated Item Value getting shown in pet rule creation wardrobe slot pick menu. - hannibal2

### Technical Details

+ Added /shwhereami command to show the current island. - martimavocado
+ Tons of code clean-ups over the whole project. - walker & hannibal2
    + Added ItemAddEvent. - hannibal2
+ Gets called when the user collects an item into inventory or sacks.
+ Created SkyHanniItemTracker. - hannibal2
    + This is a Special variant of SkyHanniTracker, that has item specific functions (hide or remove) and different
      price variants.
+ Migrated slayer profit data into SkyHanniTracker format. - hannibal2

#### Garden Changes

+ Added mythic/Maeve visitor support. - walker & hannibal2
+ Added option to use custom Blocks per Second value in some Garden GUIs instead of the real one. - hannibal2
+ Added option to change the item scale of SkyMart Coins per Copper list. - hannibal2
+ Added support for Sunder 6 in /ff upgrades. - hannibal2
+ Added support for mythic in Visitor Drop Statistics. - hannibal2
+ Use the crop fortune from tab in Farming Fortune HUD. - Alexia Luna
+ Shows the last saved ff value in gray while switching tools instead of the question mark. - hannibal2
+ Removed chat message that your crop milestone data is correct. - hannibal2
+ Removed the message when crop milestones look different in the menu than stored SkyHanni data. - hannibal2
    + We already have the correct data now, and Hypixel rounds the numbers in the menu poorly.
    + Only show the Total Crop Milestone info in crop milestone inventory when below tier 20. - hannibal2
    + Hypixel now has their own line for the same information for tier 20+
+ Make the FF Display only visible while holding a farming tool in hand. - hannibal2
+ Hide in crop milestone display the line with time remaining entirely when the milestone is maxed. - hannibal2

#### Other Changes

+ Added guess seconds to the Visitor Timer when the tab list doesn't show seconds. - hannibal2
+ Add option to hide the chat message when toggling /shmouselock. - hannibal2
+ Reminds to use the GUI Position Editor hotkey. - hannibal2
    + Reminds every 30 minutes after using /sh gui or clicking the GUI edit button.
+ Added Bookworm Book to the Estimated Item Value feature. - jani

### Fixes

#### Garden Fixes

+ Fixed new visitor alerts triggering wrongly and constantly. - Cad
+ Fixed visitor timer. - hannibal2
+ Fixed wrong Fungi Cutter mode warning not working. - walker
+ Fixed Maximum FF Needed display not showing in Jacob NPC menu. - hannibal2
+ Fixed calendar contest detection failing. - hannibal2
+ Fixed plot borders flickering and consistency errors when pressing the keybind - hannibal2
+ Fixed wrong ff needed values in Time Needed for Gold Medal GUI. - hannibal2
+ Fixed Farming Contest Medal Icons in Inventory not showing. - hannibal2
+ Fixed /ff not detecting collection analyst fortune. - hannibal2
+ Fixed Mushroom Cow Perk display not working. - hannibal2
+ Fixed visitor timer error if the visitors aren't unlocked yet. - hannibal2
+ Fixed farming weight no longer updating on block breaking. - hannibal2
+ Added cooldown to prevent spam clicking on farming weight buttons to open many web pages. - hannibal2
+ Fixed clickable farming weight GUI no longer opens #1000 in lb website. - hannibal2
+ Fixed /ff upgrade suggests updating bustling reforge even when no farming armor is found. - hannibal2
+ Fixed maxed sunder fortune in the /ff stats breakdown. - Alexia Luna
+ Fixed the farming contest summary not showing when the crop is buffed by Anita Talisman/Ring/Artifact. - hannibal2
+ Fixed Farming Fortune Display not showing for non crop-specific tools. - hannibal2
+ Fixed green thumb fortune in /ff to include Maeve. - hannibal2
+ Fixed crops per second and time remaining not using the 100 base ff in their formula. - Alexia Luna

#### Other Fixes

+ Fixed showing "slayer boss spawn soon" message outside the correct slayer area. - hannibal2
+ Fixed blocking clicks on bazaar with player name "wiki". - hannibal2
+ Fixed highlighting some mobs in the dungeon wrongly as area mini bosses. - hannibal2
+ Fixed opening the Pet menu no longer updating the current pet display. - hannibal2
+ Fixed Archfiend Dice and High Class Archfiend Dice counting as slayer drops when rolled. - hannibal2
+ Fixed dice roll profit counting as Mob Kill Coins in Slayer Tracker. - hannibal2
+ Fixed Sack Display sometimes not formatting a million correctly. - HiZe
+ Fixed Estimated Item Value getting shown in stats breakdown menu. - hannibal2
+ Fixed a bug with the ender chest and SkyHanni GUI editor. - hannibal2
+ Fixed crimson isle faction icon in tab list showing twice and not going away fully when enabling the "hide faction"
  option of advanced player list. - hannibal2

### Technical Details

+ Updated to a newer version of MoulConfig. - nea & walker
    + This includes support for the new sub category part in the config.
+ Added TimeUtils.getDuration and deprecated TimeUtils.getMillis. - hannibal2
+ Created PetAPI and deprecated String.matchRegex(). - hannibal2
+ Extracted sacks, friends, known features and Jacob contests in to their separate files. - CalMWolfs
+ Add log clearing. - CalMWolfs
+ Add auto-prefix to chat message methods. - walker
+ Added support for extra data in error manager. - hannibal2
+ Added /shreadcropmilestonefromclipboard. - hannibal2
    + This command reads the clipboard content, in the format of users sending crop milestone step data.
    + The new data gets compared to the currently saved data, differences are getting replaced and the result gets put
      into the clipboard. The clipboard context can be used to update the repo content.

### Removed Features

+ Removed 100 Farming Fortune from "Show As Drop Multiplier" from all displays (also known as "base ff"). - hannibal2
    + This can cause some numbers to show 100 FF too much. Simply update the values to fix it.
    + Those "base FF" values were never really part of your farming fortune stats. They are just a result of looking at
      the crop drop formula. SkyHanni used those values to be more comparable with other Discord Bots and spreadsheets.
      This also caused confusion, so we have removed it entirely now.

## Version 0.21

### New Features

#### Inventory

+ Added Quick Craft Confirmation. - Cad
    + Require Ctrl+Click to craft items that aren't often quick crafted (e.g. armor, weapons, accessories).
    + Sack items can be crafted normally.
+ Added Shift Click Equipment. - Thunderblade73
    + This removes the need to shift-click to swap the equipment items, without the annoying "pick up animation".
+ Added option to highlight items that are full in the sack inventory.

#### GUI

+ Added **Compact Tab List**.
    + Compacts the tablist to make it look much nicer (old SBA feature, but fewer bugs). - CalMWolfs
    + Option to hide Hypixel advertisement banners. - CalMWolfs
    + Added **Advanced Player List**. - hannibal2
        + Customize the player list (inside the tab list) in various ways.
        + Change the sort order of players: Default, SkyBlock Level, alphabetical name, Iron Man first/bingo level,
          party/friends/guild
        + Option to hide different parts of the player list: Player skins/icons, Hypixel rank color, Emblems, SkyBlock
          level
+ Added AFK time to Discord RPC. - NetheriteMiner

#### Chat

+ Adds chat symbols such as iron man/bingo/nether faction like SBA had/has. - CalMWolfs
    + Will not break with emblems.
    + Optional if left or right side of name.
    + Should not break with other mods.
+ Added Chat **Translator** - NetheriteMiner
    + After enabling, click on any chat message sent by another player to translate it to English.

#### Rendering

+ Added Highlight Party Members. - Cad
    + Marking party members with a bright outline to better find them in the world.
+ Porting SkyBlockAddon's **chroma** into SkyHanni with many more options and chroma everything. - VixidDev
    + Options to change speed, size, saturation and direction.
+ Added Modify Visual Words (command /shwords). - CalMWolfs
    + Allows you to replace text on your screen with different text (like the SBE one, just less costly).
    + Supports all color codes, even chroma (use &&Z)
+ Added In-Game Date display. - Erymanthus
    + Show the in-game date of SkyBlock (like in Apec, but with mild delays).
    + Includes the SkyBlock year.
+ Added **Arrow Trail Cosmetic** - Thunderblade73
    + Draw a colored line behind the arrows in the air.
    + Options to change the color of the line, to only show own arrows or every arrow, to have own arrows in a different
      color, to change the time alive, and the line width.

#### Crimson Isle

+ Added Quest Item Helper. - NetheriteMiner
    + When you open the fetch item quest in the town board, it shows a clickable chat message that will grab the items
      needed from the sacks.
+ Added Crimson Isle **Pablo NPC Helper**. - NetheriteMiner
    + Similar to Quest Item Helper, shows a clickable message that grabs the flower needed from sacks.

##### Fishing

+ Added alerts when the player catches a Legendary Sea Creature. - Cad
+ Added **Fishing Bait Warnings.** - cimbraien
    + Option to warn when no bait is used.
    + Option to warn when used bait is changed.

#### Dungeon

+ Added Soulweaver Skull Hider to the Dungeon Object Hider. - nea
    + Hide the annoying soulweaver skulls that float around you if you have the soulweaver gloves equipped.
+ Added **Dungeon party finder** QOL improvements - Cad
    + Floor stack size.
    + Mark Paid Carries red.
    + Mark Low-Class levels orange.
    + Mark groups you can't join dark red.
    + Mark groups without your current classes green.
+ Added working **Livid Finder** (should work 100% of the time). - hannibal2
    + Option to hide other/wrong/fake Livids (try this out and see if you really want this, it can be counter-productive
      in some cases).

#### Garden

+ Added /shmouselock command to lock mouse rotation for farming. - Cad
+ Added Highlight Visitors in SkyBlock. - nea
    + Highlights Visitors outside the Garden.
+ Block Interacting with Visitors. - nea
    + Blocks you from interacting with / unlocking Visitors to allow for Dedication Cycling.
+ Added Auto-Detection of Expired Pumpkin farming fortune. - CalMWolfs

#### Events

+ Highlight Jerries during the Jerrypocalypse. - Erymanthus
+ Show waypoints for Baskets of the Halloween Event in the main Hypixel lobby. - Erymanthus
    + Thanks Tobbbb for the coordinates!
    + Support for hiding basket waypoints once you have clicked on them. - hannibal2
    + Option to show only the closest basket. - hannibal2
+ Help with the 2023 Halloween visitor challenge (Ephemeral Gratitude) - nea
    + New Visitor Ping: Pings you when you are less than 10 seconds away from getting a new visitor.
    + Accept Hotkey: Accept a visitor when you press this keybind while in the visitor GUI.
+ Added support for showing the Primal Fear data from tab list as GUI elements. - Erymanthus
+ Play warning sound when the next Primal Fear can spawn. - Thunderblade73

#### Commands

+ Added shortcuts for **Party commands** and smart **tab complete**. - CalMWolfs
    + /pw -> party warp
    + /pk -> party kick
    + /pt -> party transfer
    + /pp -> party promote
    + /pko -> party kickoffline
+ Added the option to change Hypixel Wiki to the fandom Wiki in more areas than just the /wiki command. - Erymanthus
    + E.g. inside the SkyBlock leveling guide.
+ Added command **/shpumpkin** to toggle include/exclude Expired Pumpkin farming fortune in the /ff GUI and in the true
  ff
  display. - CalMWolfs

#### Misc

+ Added Kick Duration. - hannibal2
    + Show in the Hypixel lobby since when you were last kicked from SkyBlock.
    + Useful if you get blocked because of 'You were kicked while joining that server!'.
    + Send a warning and sound after a custom amount of seconds.
+ Added Time In Limbo. - hannibal2
    + Show the time since you entered limbo.
    + Show a chat message for how long you were in limbo once you leave it.

### Changes

+ Allowing clicks on the farming weight overlay to open the Farming Profile of you or the next person in the
  leaderboard, or manual reloading the farming weight. - hannibal2
+ Added "Burning Desert" as a tarantula slayer area. - hannibal2
+ Slayer features now work only when the slayer type matches the current SkyBlock area. - hannibal2
+ Made Fatal Tempo same as Chimera in Estimated Item Value. - jani
+ Added debug options for fishing hook display. - hannibal2
    + This should help find values that the fishing hook display works 100% with.
+ Changed the color for the tab list Special Persons Mark. - hannibal2
+ Mark SkyHanni Devs in the tab list special. - hannibal2
+ Added buttons to change the format of the price and the number in the sack display. - HiZe
+ Made Smoldering same as Fatal Tempo and Chimera in Estimated Item Value. - jani
+ Added an option to change where to get the items from in the composter overlay: from the bazaar or from sacks. - HiZe
+ Added mouse button support for key binds. - CalMWolfs
+ Added 'spooked into the lobby' chat message to the Outside Hypixel filter. - CalMWolfs
+ Changed the Yaw/Pitch Display to not show scientific notations at very small numbers. - Obsidian
+ Added an option to the Advanced Player List to hide Crimson Isle faction icons. - hannibal2
+ Added Enrichment to Estimated Item Value. - jani
+ Added Plhlegblast to the Rare Sea Creature Warning and Highlight. - hannibal2
+ Disabling all Diana features if no Diana mayor is active, allowing to overwrite the Diana mayor check if the election
  API check failed. - CalMWolfs
+ Hiding a bunch of garden GUIs in a bunch of garden inventories. - hannibal2
+ More shwords support. - CalMWolfs
    + Added the option to change between case-sensitive and case-insensitive.
    + Allow the use of Ctrl + C to copy the current textbox.
    + The effect of visual words is now visible while editing text.
+ Added Diamond/Gold essence to chat filter powder mining. - HiZe
+ Added fillet count and coins to Bronze/Silver Trophy Sack. - HiZe
+ Chest value is now default disabled in the dungeon, and added an option to enable it. - HiZe
+ Added support for lower case item IDs to the Hypixel command /viewrecipe. - walker
+ Added support for tab complete item IDs in the Hypixel command /viewrecipe. - hannibal2
    + Only items with recipes are tab completed.
+ Added option to set the size of highlighted motes orbs in rift and make them smaller by default. - cimbraien
+ Disabled clicks on SkyHanni GUIs while inside NEU's or Skytils profile viewer. - hannibal2
+ Removed armor stand checks for Trevor Solver. This fixes or de-buffs the feature to not highlight mobs behind blocks
  sometimes. - hannibal2
+ Added diamond and gold essence support to PowderTracker. - walker
+ Change the fandom wiki search engine (under the /wiki command) from Google to the fandom wiki's built-in search
  engine - Erymanthus
+ Added option to hide Chest Value while the Estimated Item Value display is showing. - hannibal2
+ No longer merging same items with different prices in Chest Value together. - hannibal2
+ Adding Great Spook support for Non God Pot Effect display. - hannibal2
+ Added a title warning to the Worm Cap Alert ping sound. - Vahvl
+ Added support for detecting refreshed farming fortune century cake effect. - Alexia Luna
+ Show key to press below burrow warp. - hannibal2
+ Makes the Compact Potion message open the Potion effects menu on click. - jani
+ Added option to show King Talisman Helper outside Dwarven Mines. - hannibal2
+ In-Game Date: Adds support for reading the in-game scoreboard, and also allow sun/moon symbol customization. -
  Erymanthus
+ Added Estimated Item Value support to NEU Profile Viewer - hannibal2
+ Added support to import SBE Visual Words into SkyHanni. - HiZe
+ Add custom keybinds for Harp Helper. - Thunderblade73
+ Show the custom hotkey name in the Harp inventory. - hannibal2
+ Added a GUI element to remind you while /shmouselock is enabled. - CalMWolfs
+ Make Crimson Isle Quest Item Helper only get amount needed. - NetheriteMiner
+ Change config order to alphabetical. - walker
+ Added commands /shresetpowdertracker and /shresetdicertracker to reset the Powder Tracker and Dicer Drop Tracker -
  hannibal2
+ Added current session/total session switch for Dicer Drop Tracker. - hannibal2
+ Added a button to reset the local session for Dicer Drop Tracker and for Powder Tracker. - hannibal2
+ Added more features for Ender Node Tracker and Armor Drop Tracker. - hannibal2
    + Added session/display mode support, added a button to reset the current session, and added the commands
      /shresetendernodetracker and /shresetarmordroptracker to reset the full data.
+ Added support for different current sessions per profile for all new trackers: Ender Node, Armor Drop, Dicer Drop,
  Powder and Slayer Profit Tracker
+ Added the option to change the Blaze Slayer Dagger GUI positions. - hannibal2
+ Added more features to the Frozen Treasure Tracker. - hannibal2
    + Added session/display mode support, added a button to reset the current session, and added the commands
      /shresetfrozentreasuretracker to reset the full data.
+ Added Slayer Profit Tracker support for loot from area mini-bosses. - hannibal2
+ No longer opening the empty /shdefaultoptions GUI. - walker
+ Added the SkyHanni icon and a link to the GitHub page for MC launchers like Prism to display. - hannibal2

### Bug Fixes

+ The yaw/pitch display does no longer show scientific notations at small values. - hannibal2
+ Fixed slayer RNG Meter problem to detect the selected item. - hannibal2
+ Fixed capitalization errors. - J10a1n15
+ Fixed a bug that hides the contest inventory menu. - CalMWolfs
+ Fixed an error with the tab list. - hannibal2
+ Fixed fishing hook display data not properly resetting on a world change. - hannibal2
+ Fixed an error in Quick Craft Confirmation. - hannibal2
+ Fixed a crash with future NEU versions because of renamed code. - hannibal2
+ Fixed double rendering of item tooltips with chat triggers. - nea
+ Fixed Sacks Display integer limit error. - HiZe & hannibal2
+ Fixed the vitality attribute is wrongly labeled as "mending" in Estimated Item Value. - walker
+ Fixed lever clicks getting highlighted in the water room. - hannibal2
    + This solution might not work for iron man in full parties.
+ Fixed slayer profit tracker showing Bazaar/AH price as NPC price. - hannibal2
+ Fixed Hyper reforge/End Stone Geode detection. - hannibal2
+ Better limbo leave detection. - hannibal2
+ Fixed rare crash when trying to read neu config. - hannibal2
+ Fixed rare case where the visitor description is empty. - hannibal2
+ Fixed a typo in the config. - hannibal2
+ Fixed the chat filter not applying immediately after joining the Hypixel lobby. - hannibal2
+ Fixed the selected drop in the catacombs RNG meter inventory not getting highlighted. - hannibal2
+ Fixed Fishing Hook Display showing wrong damage numbers. - hannibal2
+ Fixed Abiphone ring message not getting filtered correctly. - hannibal2
+ Fixed the bug that faction icons in Crimson Isle are always hidden in the tab list. - hannibal2
+ Fixed SkyHanni GUI Edit Button not working inside storage even if neu storage is not disabled. - hannibal2
+ Hide "click" texts with the Fishing Hook Display. - hannibal2
+ Fixed Highlight Showcase Items not working in some areas. - Obsidian
+ Fixed the Blaze Slayer Damage Indicator not working and no longer causing FPS drops. - hannibal2
+ Fixed clean end working in f5/m5. - hannibal2
+ Fixed visitor drop statistics preview. - Obsidian
+ Fixed a bracket at the wrong spot in Discord RPC. - hannibal2
+ Fixed fishing hook display triggering on wrong texts. - hannibal2
+ Fixed crop milestone inventory showing wrong level as stack size when maxed out. - hannibal2
+ Fixed various bugs with the /shwords GUI. - CalMWolfs
+ Bring back the command /shwords - CalMWolfs
+ It got deleted accidentally during code optimisations.
+ Highlight Commission Mobs: The "Golden Goblin Slayer" Commission should not trigger the "Goblin Slayer" Commission
  anymore. - Thunderblade73
+ Fixed potential crash with future neu version. - CalMWolfs
+ Added missing beep sound to rejoin SkyBlock after kick warning. - hannibal2
+ Fixed Fire Veil particle hider not working. - cimbraien
+ Fixed a bug with the Trophy Sack Display reading number. - HiZe
+ Fixed Ghost Counter item stack detection. - HiZe
+ Fixed Farming Weight Display shows 0 weight wrongly sometimes. - Kaeso
+ Fixed damage indicator errors during Enderman Slayer. - hannibal2
+ Fixed weird error messages in the Damage Indicator. - hannibal2
+ Fixed Enderman Slayer errors again. - hannibal2
+ Fixed the Sack Display error again. - HiZe
+ Fixed the attribute Vitality getting wrongly labeled as Mending on attribute shards. - hannibal2
+ Made Livid Solver great again. - hannibal2
+ Fixed Damage Indicator in Dungeons for some floors. - hannibal2
+ Fixed Damage Indicator "hide nametag" feature works even for disabled mobs. - hannibal2
+ Fixed item rarity detection for pets. - hannibal2
+ Fixed rare error message while disconnecting. - Thunderblade73
+ Disabled the Daily Quest part of the Reputation Helper during the great spook. - hannibal2
+ Fixed Diana warp key not working. - hannibal2
+ SkyHanni Keybinds no longer work inside SkyHanni config. - hannibal2
+ Fixed Great Spook potion not working in Non God Pot Effect feature. - jani
+ Fixed wrong Rhys (Deep Caverns NPC) items needed for Dwarven Mines unlock in Bingo Step Helper. - ReyMaratov
+ Fixed King Talisman Helper once again. - hannibal2
+ Made the ESC -> Mod Options -> SkyHanni -> Config button not crash you. - hannibal2
+ Disabled Diana Warp key and Inquis Share key while inside any GUI. - hannibal2
+ Removed Diana warp data on world switch. - hannibal2
+ Reset mouse sensitivity back to 100% if you log off with lock mouse look enabled. - hannibal2
+ Fixed mouse sensitivity stuck after restarting by storing old sensitivity. - CalMWolfs
+ Fixed tool fortune. - CalMWolfs
+ Fixed Item Ability Cooldown display not activating for Sword of Bad Health. - hannibal2
+ Fixed the crop name gets replaced to internal name in /shwords. - hannibal2
+ Show obfuscated fish as bait instead of caught item. - cimbraien
+ Fixed Estimated Item Value that renders twice inside NEU PV, by not rendering anything when the cursor is exactly in
  between two items. - hannibal2
+ fixed more error messages with The Great Spook data getting stored in the Reputation Helper quest config
  accidentally. - hannibal2
+ Hopefully fixed resets of Visitor Drops stats. - hannibal2
+ Fixed typo in The Art Of Peace. - walker
+ Fixed compatibility problems with ChatTriggers that caused flickering in the Estimated Item Value while inside the NEU
  Profile Viewer. - hannibal2
+ Fixed Quest Item Helper showing negative numbers. - hannibal2
+ Fixed YouTuber and Admin ranks getting lost in the tab list. - walker
+ Added a cooldown to the current session tracker reset button to fix the chat spam. - hannibal2
+ Changed the color of the "Slayer boss soon!" warning from red to yellow. - hannibal2
+ Fixed a bug where some items were counted twice in the Slayer Profit Tracker. - hannibal2
+ Fixed item rarity errors in the museum. - hannibal2
+ Fixed mob highlighting problems with Blaze Slayer and Skytils. - hannibal2
+ Pablo Helper: Fixed some messages not showing the "get from sack" clickable message. - hannibal2
+ Fixed scoreboard date number suffixes are missing sometimes. - Erymanthus
+ Fixed the leftStronghold area not getting detected. - hannibal2
+ Fixed error message with Ashfang Blazes. - hannibal2
+ Fixed crash with pet exp tooltip. - hannibal2
+ Fixed Dungeoneering showing as 0 in the skill menu. - hannibal2
+ Fixed showing minion level as 101 in some menus. - hannibal2

#### Config

+ Fixed two typos in the config description. - Absterge
+ Fixed small typos in config. - Absterge

#### Removed Features

+ Removed **Duplicate Hider**.
    + Hypixel now fixed the bug themselves and hides duplicate farming contests in the Jacob inventory.

#### Technical Details

+ Add Repo TODOs to regex patterns. - walker
+ Moved many patterns from function scope to members. - hannibal2
+ Avoid hardcoded dispatcher. - walker
+ Created and used Entity.canBeSeen and LorenzVec.canBeSeen. - hannibal2
+ Reducing cognitive complexity in StatsTuning.kt - walker
+ Reducing indentations and line counts in StatsTuning.kt - hannibal2
+ Mark functions around item utils and neu items as deprecated. - walker
+ Added debug command /shconfigmanagerreset. - hannibal2
+ Reloads the config manager and rendering processors of MoulConfig. This WILL RESET your config, but also update the
  java config files (names, description, orderings and stuff).
+ Adding 100 lines to MobFinder.kt and making it better readable in the process. - walker
+ Making ChatFiler.kt way better, storing regex objects for reuse and preparing future repo support. - walker
+ Added command /shkingfix to reset the internal King Talisman Helper offset. - hannibal2
+ Updated dependency version of junixsocket in DiscordIPC so that antivirus websites no longer show false positives. -
  NetheriteMiner
+ Changed wrong/missing neu version message to show NEU version 2.1.1-Pre-4 instead of beta versions. - CalMWolfs
+ Deleting the old "hidden" part of the config. - hannibal2
+ This will reset parts of the config for users with 7-month-old SkyHanni versions that want to migrate into the
  present.
+ Added a workaround for the NEU Profile Viewer bug where the ItemTooltipEvent gets called for two items when hovering
  over the border between two items. - hannibal2
+ Using visitorDrops.visitorRarities directly from the config instead of accessing the local field. Hopefully this will
  prevent partial config resets in the future. - hannibal2
+ Added a tracker API that can be used for all features in SkyHanni that currently track stuff that the user collects. -
  hannibal2
+ Added the slayer profit tracker logic (command to reset, toggle between total view and session view, and button to
  delete session) to powder tracker and Dicer Drop Tracker. - hannibal2
+ Added support for migrating parts of the player or session storage. - nea
+ Changed the config format for dicerRngDrops/dicerDropsTracker. - hannibal2
+ Created SkyHanniTracker, the core API for working with tracker stuff. This should be used everywhere someday in the
  future. - hannibal2
+ Used SkyHanniTracker in FrozenTreasureTracker. - hannibal2
+ Added /shdebugwaypoint as a test/debug command. - hannibal2
+ Added debug messages to detect hot swaps. - hannibal2
+ Added /shdebugtablist
+ Set your clipboard as a fake tab list. - hannibal2
+ /shversion now copies the SkyHanni version into the clipboard as well. - hannibal2
+ Moved location fixes to the repo. - hannibal2
+ Added debug information for PetExpTooltip crash. - hannibal2

## Version 0.20

### New Features

+ Replacing command `/warp is` with `/is`.
+ Added command `/shbingotoggle` to toggle the bingo card.
+ Added option to disable quick bingo card toggle with sneaking.
+ **King Talisman Helper**
    + Show kings you have not talked to yet, and when the next missing king will appear.
+ **Harp Keybinds** - NetheriteMiner
    + In Melody's Harp, press buttons with your number row on the keyboard instead of clicking.
+ **Ender Node Tracker** - pretz
    + Tracks items and profit obtained from mining ender nodes and killing normal endermen.
+ **Fishing timer** now works in **Crystal Hollows** as well. (Worm fishing)
+ Added keybind to manually reset the barn/worm fishing timer. - CarsCupcake
+ Added barn fishing timer support for stranded. - hannibal2
+ Option to shorten the **bestiary level-up message**.
+ **Bestiary overlay** - HiZe
    + Options for change number format, display time, number type and hide maxed.
    + Highlight maxed bestiaries.
+ Show the names of the **4 areas** while in the center of **crystal Hollows**.
+ Chest Value - HiZe
    + Shows a list of all items and their price when inside a chest on your private island.
+ In Melody's Harp, show buttons as stack size. - NetheriteMiner
    + Intended to be used with Harp Keybinds
+ Added timer till shared inquisitors will despawn. - hannibal2
+ Account upgrade complete reminder. - appable0
+ Chat message how many places you dropped in the farming weight lb when joining garden.
+ Added command /shfarmingprofile [player name]
    + Opens the elitebot.dev website in your web browser to show your Farming Weight profile.
+ Pet Experience Tooltip
    + Show the full pet exp and the progress to level 100 (ignoring rarity) when hovering over a pet while pressing
      shift key.
    + Highlight the level 100 text in gold for pets below legendary. - hannibal2
      (This is to better indicate that the pet exp bar in the item tooltip is calculating with legendary.)
    + Option to only show level 100 for golden dragon in the pet experience tooltip. - hannibal2
+ Anita Extra Farming Fortune:
    + Show current tier and cost to max out in the item tooltip.
+ Sync Jacob Contests - Kaeso + CalMWolfs
    + No need to open the calendar every SkyBlock year again.
    + Grab Jacob Contest data from the elitebot.dev website.
    + Option to send local contest data to elitebot.dev at the start of the new SkyBlock year.
+ Added SkyHanni **Installer** - NetheriteMiner
    + Double-clicking the mod jar file will open a window that asks you where to move the mod into.
+ Show the progress bar until maxed crop milestone in the crop milestone inventory. - hannibal2
+ Show a line to the enderman slayer beacon. - hannibal2
+ Added **Default Option Settings:** - nea
    + Enables or disables all features at once, or per category.
    + Sends a chat message on first SkyHanni startup (starting with this feature, so this version everyone will see this
      message).
    + Shows new features after an update (starting with the next beta, not this one).
    + Allows to change those settings anytime again with /shdefaultoptions.
+ While on the Winter Island, show a timer until Jerry's Workshop closes. - hannibal2
+ Added Reindrake support to the Damage Indicator. - hannibal2
+ Added visual garden plot borders. - VixidDev
    + Press F3 + G to enable/disable the view.
+ Added **Following Line** Cosmetic - hannibal2
    + Draws a colored line behind the player.
    + Change the color, width, and duration of the line.
+ Show alert when reaching max super-pairs clicks. - pretz
    + Plays a beep sound and sends a message in chat when the player reaches the maximum number of clicks gained for
      super-pair mini games.
+ Added gemstone slot unlock costs to the Estimated Item Value. - Fix3dll
+ **Powder Grinding Tracker** - HiZe
    + Shows the Mithril/Gemstone Powder gained, the number of chests opened, if Double Powder is active, and the items
      collected.
    + Change between current session and total (open the inventory and click on Display Mode).
    + Fully customizable: change what items or stats to show.
    + Has support for the maxed Great Explorer perk.
    + Option to hide while not grinding powder.
+ Added Anniversary Event Active Player Ticket Timer. - nea
    + Option to play a sound as well.
+ Added highlight and outline feature for rare sea creatures. - Cad
+ Add feature to outline dropped items. - Cad
+ Add Outline Dungeon Teammates. - Cad
+ Added Price Website button. - hannibal2
    + Adds a button to the bazaar product inventory that will open the item page in skyblock.bz.
+ Added icons for the medals received in a contest. - CalMWolfs
    + Different symbol for when it was a Finnegan contest.
+ Added Tab Complete support to sacks command /gfs and /getfromsacks. - J10a1n15
+ Added GUI Scale. - nea
    + Scroll within the position editor to independently adjust the GUI scale for each SkyHanni element.
    + Change the global scale of all SkyHanni elements at once (in the config under /sh scale).
+ Added Fishing Hook Display. - hannibal2
    + Display the Hypixel timer until the fishing hook can be pulled out of the water/lava, only bigger and on your
      screen.
+ Added **Trevor the Trapper Tracker**. - CalMWolfs
    + Quests done
    + A breakdown of their rarity
    + Animals killed vs. animals that kill themselves
    + Pelts per hour
+ Press the hotkey to accept the next Trevor the Trapper quest. - CalMWolfs
+ Added a countdown for Arachne spawn. - Cad
    + Supports quick spawns.
+ Added **Sack Change** chat message hider. - hannibal2
    + Enable this option instead of Hypixel's own setting to hide the chat message while enabling mods to utilize sack
      data for future features.
+ Added dungeon information to the Discord RPC. - NetheriteMiner
    + Show the current floor name.
    + Time since the dungeon started.
    + Number of boss collections of the current boss.
+ Added a Dynamic Priority Box to Discord RPC. - NetheriteMiner
    + Change the order or disable dynamically rendered features (e.g. Slayer, Dungeon, Crop Milestone, Stacking
      Enchantment)
+ Dungeon Colored Class Level. - hannibal2
    + Color class levels in the tab list. (Also hide rank colors and emblems because who needs that in dungeons anyway?)

### Changes

+ Added option to disable quick bingo card toggle with sneaking.
+ Made damage indicator more performant. - nea
+ Ghost bestiary update. - HiZe
+ Api error messages are now formatted more helpful.
+ Added option to only show the reputation helper while pressing a hotkey.
+ Garden **Money per Hour** now uses the **dicer drops** from melon and pumpkins as well. - CalMWolfs
+ Adds **Double Hook** to the **sea creature chat message** instead of in a previous line. - appable0
+ Rune display now shows always in sack display.
+ Shark fish counter now counts twice for Double hook. - appable0
+ Ghost counter check for Mist now ignores y coordinates - HiZe
+ Telling the user about the bypass hotkey when the visitor drop warning blocks a visitor refusal. - CalMWolfs
+ Added warning sound when the worm fishing cap of 60 is hit. - CarsCupcake
+ Shared inquisitor waypoints will now get removed after 75 seconds. - hannibal2
+ Chest Value now works with Backpack and Ender Chest. - HiZe
    + Only works if NEU storage is not active.
+ Removed distance checks when detecting sea creatures for fishing timer. - hannibal2
+ Added Enchantments Cap to EstimatedItemValue. - hannibal2
    + Only show the top # most expensive enchantments on an item.
+ Count sea emperor and rider of the deep twice against sea creature cap.
+ Clicking on the chat message from farming weight will run the /shfarmingprofile command.
+ Changed Bestiary Display number format - HiZe
+ Changed ff buffs for Elephant and Anita.
+ Changed chicken head timer from 20s to 5s.
+ Added option to show reputation locations only when pressing the hotkey.
+ Delay the custom garden level up message by a few milliseconds to not cut into the garden milestone message.
+ Added runic support for Zealots/Bruiser.
+ Added cultivating and replenish to the visitor rewards. - ReyMaratov
+ Added Bee pet support to /ff - derholzmann12321
+ Added exportable carrot to /ff. - CalMWolfs
    + If SkyHanni doesn't know you have it, run /shcarrot
+ The damage indicator now includes a toggle option for the Ender Slayer Laser Phase timer. - hannibal2
+ Added ender slayer beacon explode timer. - hannibal2
+ Show the name over Nukekubi Skulls for in ender slayer (deadly eyes). - hannibal2
+ Changed the enderman slayer laser timer to be more exact. - hannibal2
+ Added support for the new fixed inquisitor spawn message from Hypixel for the detection. - hannibal2
+ Added option to hide the off-screen drop chat message. - hannibal2
+ Now hides the farming weight display by default when below 200 weight (can be disabled). - hannibal2
+ Added option to change the Enderman Slayer beacon color. - hannibal2
+ Added option to show a line to every slayer mini boss around you. - hannibal2
+ Added options to ignore the wizard and the crypt warp for Diana.
+ Loading farming weight values directly from elitebot.dev. - CalMWolfs
    + This has no effect right now but allows Kaeso to change the values easier in the future.
+ Slightly changed the FF guide tab colors to make more sense. - Obsidian
+ Changed the bingo tips description to show the actual data source.
+ Added Fire Fury Staff support to item ability cooldown feature. - Cad
+ Estimated item value now shows the reforge stone apply cost as well. - hannibal2
+ Added Bits, Mithril Powder, and Gemstone Powder to visitor drop statistics. - Obsidian
+ Added support for slayer profit trackers on stranded. - hannibal2
+ Added Trapper Cooldown GUI. - NetheriteMiner
    + Show the cooldown on screen in an overlay (intended for abiphone users).
+ Made the fake garden level up message (after level 15) clickable. - J10a1n15
+ Moving many GUI elements in the config around. - hannibal2
    + This will not reset anything.
    + This includes the creation of new categories and regrouping.
+ Better Trevor the Trapper detection. - CalMWolfs
+ Show Trevor the Trapper's mob name next to the waypoint. - CalMWolfs
+ Small performance improvements when working with color codes. - brainage04
+ Added dungeon mage cooldown reduction support for item ability cooldown. - Cad
+ Improved the wording and fixed typos of the config category descriptions. - zapteryx
+ Changed the option in the pet experience tooltip to show progress to level 100 for golden dragon eggs (can be
  disabled). - hannibal2
+ Replaces the word Tps to TPS. - Erymanthus
+ Improved performance of item ability cooldown and farming fortune display data. - Cad
    + The item flickering for ability cooldown background should be less/gone completely.
+ Added the ability to bypass not clickable items when holding the control key. - CalMWolfs
+ Croesus Chest Tracker can now determine chest that only can be open with a dungeon key - Thunderblade73
+ Added armor drops to the Money per Hour display. - CalMWolfs
+ Croesus chest highlight now uses different colors for Dungeon Chest Key-only chests. - Thunderblade73
+ Added support to change the scale of GUI elements with plus and minus keys. - CalMWolfs
+ Ignoring non-slayer drops in the slayer profit tracker. - hannibal2
+ Added support for slayer drops that go directly into the sack. - hannibal2
    + This still does not work for items that land directly in your inventory. (e.g., Netherrack-Looking Sunshade,
      Summoning Eye, etc.)
+ Added toggle for 12hr/24hr in real-time HUD. - Thunderblade73
+ Removed Flowering Bouquet from and added Cultivating to the default enabled Visitor Reward Warnings list. - hannibal2
+ Improved performance in the Garden Visitor inventory. - hannibal2

### Fixes

+ Removed `Simple Carrot Candy` from composter overlay.
+ Fixed croesus highlight unopened chest not working anymore. (ty hypixel)
+ Should not crash anymore if a file in repo is missing.
+ Fixed Killer Spring tower still highlighting even with the feature turned off. - HiZe
+ Fixed weird tuba ability cooldown not working in rift.
+ Fixed holy ice cooldown sometimes not working.
+ Fixed a rare startup crash.
+ Fixed Ghost stats after bestiary update. - HiZe
+ Watchdog hider now correctly hides empty lines as well, even when empty line hider is disabled. - appable0
+ Fixed `saw mob` title for trevor trapper solver still showing even when feature is disabled.
+ Fixed chicken from raider of the sea get detected as trevor trapper mob.
+ Fixed master star detection in estimate item value broken for some times.
+ Fixed description in golden trophy fish info tooltip. - appable0
+ Fixed End Node Tracker not updating when changing the text format.
+ Fixed neu repo error messages with runes.
+ Fixed rare crashes with scoreboard.
+ Fixed feature that replaces the sack stitched lore message. - hannibal2
+ Fixed some typos in config descriptions and correctly rounding down in composter overlay. - CalMWolfs
+ Fixed a typo in an error message. - Obsidian
+ Fixed Chest Value fails to detect chests when using a resource pack. - HiZe
+ Fixed Sea Creature Timer not working on barn. - hannibal2
+ Arachne boss highlighter no longer requires damage indicator to be enabled. - hannibal2
+ /shtrackcollection Cactus now detects cactus green. hannibal2
+ Fixed arachne minis falsely show in damage indicator.
+ Fixed rare cases where special laptop keys trigger behavior unintended. - hannibal2
+ Fixed rendering problems with stack background and custom text lore. - nea
+ Hopefully fixed Derpy problems with Trevor Trapper, Arachne, Arachne Keeper and Zealots.
+ Fixed Anita upgrade detection from the Anita Shop inventory.
+ Fixed error message when clicking a finished upgrade in the community shop.
+ Fixed everything **crop milestone** related. - CalMWolfs
+ Fixed estimated item value detects master stars on non-dungeon items. - hannibal2
+ Fixed wrong progress bar for pet exp display. - hannibal2
+ Fixed compatibility problem with NEU for pet exp number format. - hannibal2
+ Various **/ff fixes** - CalMWolfs
+ Numbers, rounding and pets being reset.
+ Fixed inventory item number offset being wrong. - CalMWolfs
+ Fixed slayer quest detection after death. - hannibal2
+ Fixed rounding errors with yaw and pitch display. - hannibal2
+ Fixed ender slayer beacon don't disappear sometimes. - hannibal2
+ Fixed multiple bugs with garden visitor: - hannibal2
    + Visitors with multiple different crops now calculate the copper price and the visitor drop stats correctly.
    + Reward items no longer impact the visitor drop coins spent data negatively.
    + Copper per coin price now respects the reward item profit as well.
    + Now showing the NPC price for items without ah/bazaar data (looking at pet candy).
+ Fixed highlight in the main bestiary menu. - HiZe
+ Fixed maxed farming fortune possible in /ff - CalMWolfs
+ Fixed negative coins per copper price in visitor inventory. - hannibal2
+ Fixed rare error message while farming. - hannibal2
+ Fixed a rare error message while showing the lore of a farming tool. - hannibal2
+ Fixed estimated item value doesn't detect master stars in Auction House. - hannibal2
+ Fixed enderman slayer beacon warning only visible for a very short amount of time. - hannibal2
+ Fixed enderman slayer line to beacon only shows when beacon highlight is enabled. - hannibal2
+ Fixed major composter profit calculation with multi-drop. - pretz
+ Garden Money Per Hour: Fixed the wrong NPC price when Merge Seeds is enabled. - hannibal2
+ Hopefully fixed error messages in bingo around collection values. - hannibal2
+ Hopefully fixed a rare chat error. - hannibal2
+ Fixed duration format in non-god-potion display. - appable0
+ Fixed various typos. - pretz
+ Fixed minion nametag not getting removed after picking it up. - Cad
+ Fixed 3k hp spiders being falsely highlighted as Arachne's Keepers. - oofy
+ Fixed a bug in GhostCounter for the 48th time. - HiZe
+ Fixed typo of effigy in the rift blood effigies display. - Vahvl
+ Fixed potential bug in salvage inventory with "hide not clickable items" on the alpha. - hannibal2
+ Fixed small typos in config. - hannibal2
+ Fixed maxed pet exp progress bar is wrong for bingo pet. - hannibal2
+ Hopefully fixed bug that opening a visitor too quickly causes detection problems. - hannibal2
+ Added nametags for minions as soon as they are placed. - Cad
+ Fixed wrong display offset in Powder Tracker. - HiZe
+ Fixed some features not working correctly because of broken location detection for wrong Hypixel area names. - Cad
+ This includes:
+ Wilted Berberis Helper (in the Rift, Dreadfarm)
+ Slayer Profit Tracker (in the Park, Howling Cave)
+ End Node profit Tracker (in the End)
+ Fixed crash when item rarity cannot be detected in Auction House. - hannibal2
+ Fixed Replenish and Cultivating being bugged in visitor drop statistics. - Obsidian
+ Summoning souls should no longer be counted as sea creatures for the fishing timer. - hannibal2
+ Fixed Jacob Contests times needed display showing impossible FF values. - ReyMaratov
+ Fixed item cooldown issues with fire fury staff and jinxed voodoo doll. - Cad
+ Fixed the percentage going above 100% for bestiary progress. - HiZe
+ Fixed max kills from Ghost Counter using old bestiary data when resetting the config. - HiZe
+ Fixed part of Zealot Hideout area showing as The End. - Cad
+ Fixed many typos in the config. - schlaumeyer, oofy, CalMWolfs & hannibal2
+ Fixed a missing bracket in the reforge apply cost display from Estimated Item Value. - jani
+ Fixed a rare crash while doing enderman slayer. - hannibal2
+ Fixed pet exp tooltip doesn't show in pet inventory or Hypixel profile viewer (right-click a player). - hannibal2
+ Fixed turbo books price not getting detected in visitor rewards. - hannibal2
+ Fixed that the Paste Into Sign feature only pastes into the first line. - hannibal2 + nea
+ Hopefully fixed rare config reset cases. - nea
+ This should also fix problems with false positive detections in the crimson isle.
+ Fixed item rarity problems. - hannibal2
+ Fixed a rare error when opening minion inventory. - hannibal2
+ Fixed stuff in the **Frozen Treasure Tracker**. - CalMWolfs
    + The ice/hour calculation
    + Rate Timer
    + Typos
+ Fixed a small typo in the config. - hannibal2
+ Fixed inconsistencies with Arachne brood highlighting. - Cad
+ Fixed Crimson Reputation Helper doesn't count trophy fish when sacks exist. - Fix3dll
+ Fixed a rare error when switching to a dicer farming tool too quickly. - Cad
+ Added workaround for new fire sale cosmetics (pet skin, helmet skin, rune) in estimated item value. - hannibal2
+ Fixed garden visitors not highlighting on status "new". - hannibal2
+ Fixed wrongly highlighting enchanted sacks for reputation helper fetch quests. - hannibal2
+ Fixed Fragmented Spirit Mask not showing a cooldown after being triggered. - Cad
+ Fixed item rarity problems with very special. - hannibal2
+ Fixed party member detection issues for the tab complete feature. - CalMWolfs
+ Hide item rarity error message in /ff. - hannibal2
+ Fixed an issue with the Wheat Profit Display not showing the correct value when combined with seeds. - Thunderblade73
+ Tab complete party members now also detects if the party leader leaves. - CalMWolfs
+ Fixed typos in many config settings. - Absterge
+ Fixed NEU Heavy Pearl detection. - hannibal2
+ Fixed showing Wrong Slayer Warning sometimes at the wrong time. - Cad
+ Fixed farming contest summary time being off sometimes. - hannibal2
+ Fixed wrong order at Visitor Drop Statistics. - hannibal2
+ Fixed bits and powder not saving in Visitor Drop Statistics. - hannibal2
+ Fixed not being able to disable show Dungeon Head Floor Number as stack size. - hannibal2
+ Fixed green bandana not getting detected for visitor reward warning. - hannibal2
+ Fixed zealots and zealot bruisers not getting detected when being runic. - hannibal2
+ Fixed mixins not getting detected correctly from effects inventory for Non God Pot Effect display. - hannibal2
+ Fixed an error when detecting Jacob Contest times. - hannibal2
+ Fixed Mushroom Goal breaking the Bingo Card detection. - hannibal2
+ Fixed Diana's Griffin Pet Warning not working during the Jerry Mayor. - hannibal2
+ Fixed /ff not detecting changes in the /pets inventory. - CalMWolfs

### Removed Features

- Removed **Broken Wither Impact** detection. - hannibal2
    - (Hypixel fixed their bug, finally)
- Removed remaining **Hypixel API** support for still existing legacy api keys. - hannibal2
    - This should not affect much
- Removed wishing compass in item number.
    - Every Wishing Compass now only has one use instead of three.

## Version 0.19

### Rift Features

+ Added **Rift Timer**
    + Show the remaining rift time, max time, percentage, and extra time changes.
+ **Highlight Guide**
    + Highlight things to do in the Rift Guide.
+ Added **Shy Warning** (Contributed by CalMWolfs)
    + Shows a warning when a shy crux will steal your time.
    + Useful if you play without volume.
+ Added **Larvas Highlighter**
    + Highlight larvas on trees in Wyld Woods while holding a Larva Hook in the hand
    + Customize the color
+ Added **Odonatas Highlighter**
    + Highlight the small Odonatas flying around the trees while holding an Empty Odonata Bottle in the hand.
    + Customize the color.
+ Added **Agaricus Cap** countdown
    + Counts down the time until Agaricus Cap (Mushroom) changes color from brown to red and is breakable.
+ Added **Leech Supreme** to Damage Indicator
+ Added **Volt Crux Warning** (Contributed by nea)
    + Shows a warning while a volt is discharging lightning
    + Shows the area in which a Volt might strike lightning
    + Change the color of the area
    + Change the color of the volt enemy depending on their mood (default disabled)
+ Added **Enigma Soul Waypoints** (Contributed by CalMWolfs)
    + Click on the soul name inside Rift Guide to show/hide
+ Added **Kloon Hacking** (Contributed by CalMWolfs)
    + Highlights the correct button to click in the hacking inventory
    + Tells you which color to pick
    + While wearing the helmet, waypoints will appear at each terminal location
+ Added **Crux Talisman Progress** Display - HiZe
    + Show bonuses you get from the talisman
+ Added existing slayer feature support for **Vampire Slayer** - HiZe
    + This contains RNG Meter, Slayer quest warning and Items on ground
+ Added item ability cooldown support for **Weirder Tuba** and **Holy Ice** - HiZe
+ Added **Laser Parkour** Solver - CalMWolfs
    + Highlights the location of the invisible blocks in the Mirrorverse
+ Added Mirrorverse **Dance Room Helper** - HiZe
    + Helps to solve the dance room in the Mirrorverse by showing multiple tasks at once.
    + Change how many tasks you should see
    + Hide other players inside the dance room
    + Added timer before next instruction
    + Option to hide default title (instructions, "Keep it up!" and "It's happening!")
    + Fully customize the description for now, next and later (with color)
+ Added **Upside Down** Parkour & **Lava Maze** - hannibal2
    + Helps to solve the Upside Down Parkour and Lava Maze in the Mirrorverse by showing the correct way
    + Change how many platforms should be shown in front of you
    + Rainbow color (optional) - nea
    + Hide other players while doing the parkour
    + Outlines the top edge of the platforms (for Upside Down Parkour only) - CalMWolfs
+ Added Jinxed voodoo doll ability cooldown support - HiZe
+ Added Polarvoid Books to estimated item value - jani
+ Added **Motes NPC** price in the item lore
    + With Burgers multiplier - HiZe
+ Added Motes Grubber to Not Clickable Items feature
+ **Living Metal Suit** Progress - HiZe
    + Display progress Living Metal Suit (Default disabled)
    + Option to show a compacted version of the overlay when the set is maxed
+ Added Highlight for Blobbercysts in Bacte fight in colosseum in rift - HiZe
+ Show a line between **Defense blocks** and the mob and highlight the blocks - hannibal2
    + Hide particles around Defense Blocks
+ Show a moving animation between **Living Metal** and the next block - hannibal2
    + Hide Living Metal particles
+ Highlight **flying Motes Orbs** - hannibal2
    + Hide normal motes orbs particles
+ Added bloodfiend (vampire slayer) to Damage Indicator - HiZe
+ Add Bacte to Damage Indicator
+ Hide Not Rift-transferable items in Rift Transfer Chest as part of the hide not clickable items feature
+ Add npc motes sell value for current opened chest - HiZe
+ Vampire Slayer Feature - HiZe
    + Sound for twinclaws
    + Option to delay twinclaws notification and sound (in millis, configurable)
    + Draw line starting from the boss head to the Killer Spring/Blood Ichor (if the boss is highlighted)
    + Draw line starting from your crosshair to the boss head
    + Configurable to work only on your boss, on bosses hit, or on coop boss
+ Show locations of inactive Blood Effigy
    + Show effigies that are about to respawn
    + Show effigies without known time
+ Added **Wilted Berberis Helper**
    + Option to only show the helper while standing on Farmland blocks
    + Option to hide the wilted berberis particles
+ Added **Vampire Slayer** features in Damage Indicator
    + Show the amount of HP missing until the steak can be used on the vampire slayer on top of the boss.
    + Show a timer until the boss leaves the invincible Mania Circles state.
    + Show the percentage of HP next to the HP.
+ Added **Horsezooka Hider**
    + Hide horses while holding the Horsezooka in the hand.

### Other New Features

+ Added **Frozen Treasure Tracker** (Contributed by CalMWolfs)
    + Show different items collected while breaking treasures in the Glacial Cave in Jerry's Workshop
    + Show Ice per hour
    + Customizable GUI
    + Option to hide the chat messages
+ Added **Custom Text Box** - CalMWolfs
    + Write fancy text into a gui element to show on your screen at all time
    + Supports color codes
    + Supports line breaks `\n` - hannibal2
+ Added **Highlight Commission Mobs** - hannibal2
    + Highlight Mobs that are part of active commissions
+ Added command **/shcommands**
    + Show all commands in SkyHanni
+ Added Attribute price support to Estimated Item Value - nea
+ Added warning when enderman slayer beacon spawns - dragon99z
+ Added Highlight enderman slayer Nukekubi (Skulls) - dragon99z
+ Added option to hide the vanilla particles around enderman
+ Hide particles around enderman slayer bosses and mini bosses
+ Added support for Shadow Fury ability cooldown - HiZe
+ Added /sendcoords sending, detecting and rendering - dragon99z
+ Added **Boss Spawn Warning** - HiZe + hannibal2
    + Send a title when your slayer boss is about to spawn
    + Configurable percentage at which the title and sound should be sent
+ Added **Refusal Bypass Key** - HiZe
    + Hold a custom key to bypass the Prevent Refusing feature for visitors
+ Added **Farming Weight ETA Goal** - Kaeso
    + Override the Overtake ETA to show when you will reach the specified rank.
    + If not there yet
    + Default: #10k
+ Added **Dungeon Potion level as item stack size - HiZe
+ Added **Griffin Pet Warning**
    + Warn when holding an Ancestral Spade while no Griffin pet is selected.
+ More **Trophy Fish** features - appable0
    + **Trophy Fish Info** - Hover over trophy fish caught chat message to see information and stats about the trophy
      fish.
    + **Fillet Tooltip** - Adding fillet amount and price to the tooltip of a trophy fish. Left shift to show stack
      value.
+ Added Chumcap support for Chum Bucket Hider - jani

### Changes

+ SkyHanni no longer requires Patcher to start! (Big thanks nea)
+ Option to show the yaw and pitch display outside garden or while not holding a farming tool (Contributed by CalMWolfs)
+ Added wizard warp as diana waypoint
+ Added option to show exact prices in estimated item value
+ Better error handling with missing neu repo items - CalMWolfs
+ Changes to **Discord RCP** - NetheriteMiner
    + More images for different locations
    + Fixed names unnecessarily getting cut off when visiting an island in Location
    + Fewer crashes because of NEU Item Repo
+ Changed time for Trapper from 30 to 15 seconds while Finnegan is mayor – CalMWolfs
+ Added warning to not disable repo auto-update
+ Wither impact ability cooldown now only shows when all 3 scrolls are attached
+ Show total amount of all trophy fish rarities at the end of the chat message
+ Changed **Elite farming weight** display - Kaeso
    + Instantly showing the next player in the lb when passing someone on the leaderboard
+ Show in chat the total amount of sharks fished after the fishing contest
+ Custom text over Visitor name moves more smoothly now
+ Discord Rich Presence now supports Motes in the rift and the garden plot number - NetheriteMiner
+ Crop money display: Using npc price for mushroom cow pet calculation when on ironman, stranded or bingo
+ Sacks no longer get blocked from moving in storage (not clickable items feature)
+ Using 19.9 bps if bps is too low for jacob contest ff needed display
+ Better default positions for some guis

### Fixes

+ Fixed Pocket Sack-In-A-Sack Replace in lore
+ Fixed possible crash with broken neu repo when opening the desk inventory in the garden (Contributed by CalMWolfs)
+ Fixed frozen treasures per hour display being inaccurate (Contributed by CalMWolfs)
+ Fixed bug with ghost counter sometimes not detecting new kills (Contributed by CalMWolfs)
+ Fixed **Ghost Counter** - HiZe & ksipli
    + Should no longer have compatibility issues with other mods
    + It work even if the action bar show percent +xxx (xx%) instead of +xxx (xxx/xxx)
    + Added command /shresetghostcounter to reset everything (not the bestiary data)
    + Added time format in ETA formatting (can show days, hours, minutes and seconds remaining)
+ Fixed **Dungeon Master Stars** calculation not working correctly sometimes – hannibal2 & Fix3dll
+ Fixed Discord Rich Presence detecting absorption hearts not correctly – NetheriteMiner
+ Fixed Reputation Helper **Kuudra Completions** not getting detected and adding T4 and T5 support - Cinshay
    + Fixed Item Ability cooldown not working for Astraea - hannibal2
    + Fixed a typo in the config - hannibal2
    + Added a workaround for a crash when refusing a visitor - hannibal2
+ Added support for new counter drops to dicer rng counter - ReyMaratov
+ Fixed composter inventory numbers after Hypixel changes - hannibal2
+ RNG dicer chat hider now works without enabling drop counter
+ Server restart timer no longer shows all the time if over 2 minutes
+ Fixed crazy rare drops not counting properly - ReyMaratov
+ Fixed individual attribute prices in estimated item value - nea
+ Fixed sack display detection - HiZe
+ Fixed rare Ghost Counter bugs - HiZe
+ Fixed a bug that farming weight display does not show up if you only have one profile
+ Fixed broken thorn damage indicator detection in dungeon F4/M4
+ Fixed "toggle sneak mod" breaking escape menu open detection for quick mod menu switch
+ Fixed error with detecting hit phase during eman slayer in damage indicator
+ No longer double counting mob kill coins in slayer item profit tracker
+ Fixed jacob contest time chat message chat shows one second too long
+ Fixed farming contest calendar reading going above 100% - Contrabass26
+ Changed multiple descriptions in the misc category, fixed grammar and punctuation problems - Absterge
+ Fixed rare bug with Damage Indicator
+ Fixed showing skill level as item stack being broken for level 0. - Erymanthus
+ Fixed ability cooldown not working for some items. - Cad
+ Fixed Bazaar item category "Ink Sack" gets wrongly detected as an item sack. - Erymanthus
+ Fixed reforge stone "Pitchin' Koi" not detected in the Estimated Item Value. - Fix3dll
+ Minion Craft Helper now ignores Inferno and Vampire minions as well. - walker
+ Better wording in the auto updater. - zapteryx

## Version 0.18 (2023-06-19)

### New Features

+ Added **Visitor Drop Counter** (Contributed by CalMWolfs)
    + Counts up all the drops that you get from visitors
    + Count each rarity of visitor Accepted
    + Count copper, farming exp and coins spent
    + Setting to show number or drop first
    + Setting to show as the icon instead of the name
    + Setting to show only on the barn plot
+ Added **Contest Time Needed**
    + Show the time and missing FF for every crop inside Jacob's Farming Contest inventory.
+ Added **Garden Start Location**
    + Show the start waypoint for your farm with the currently holding tool.
    + Auto-detects the start of the farm when farming for the first time
    + Option to manually set the waypoint with /shcropstartlocation
+ Added **price display next to sack items** (Contributed by HiZe)
    + Can be disabled
    + Sortable by price or items stored (both desc/asc)
    + Option to show prices from Bazaar or NPC
+ Added Farming Fortune Breakdown for Armor and Equipment (Contributed by CalMWolfs)
    + Run /ff to open the menu
    + Works with: Base Stats, Reforge Bonus, Ability Fortune and Green Thumb
    + Breakdown for the true farming fortune from each crop
    + Ability to select a single piece of armor or equipment
+ Added Server Restart Title
+ Added Jacob Contest Stats Summary
    + Showing Blocks per Second and total Blocks clicked after a farming contest in chat
+ Added City Project Features
    + Show missing items to contribute inside the inventory
        + Click on the item name to open the bazaar
    + Highlight a component in the inventory that can be contributed
+ Added `/pt <player>` alias for `/party transfer <player>`
    + SkyBlock Command `/tp` to check the play time still works
+ Added **Command Autocomplete**
    + Supports tab completing for warp points when typing /warp
    + Supports party members, friends (need to visit all friend list pages), player on the same server
    + Supports these commands: /p, /party, /pt (party transfer), /f, /friend /msg, /w, /tell, /boop, /visit, /invite,
      /ah, /pv (NEU's Profile Viewer), /shmarkplayer (SkyHanni's Mark Player feature)
    + Supports VIP /visit suggestions (e.g. PortalHub or Hubportal)
+ Added Item Profit Tracker (Slayer only)
    + Count items collected and how much you pay while doing slayer, calculates final profit
    + Shows the price of the item collected in chat (default disabled)
+ Added Items on Ground (Slayer only)
    + Show item name and price over items laying on ground (only in slayer areas)
+ Added Broken Hyperion Warning (Slayer only)
    + Warns when right-clicking with a Wither Impact weapon (e.g. Hyperion) no longer gains combat exp
    + Kill a mob with melee-hits to fix this hypixel bug
    + Only while doing slayer
+ Added Piece of Wizard Portal show earned by player name (Contributed by HiZe)
+ City Project Daily Reminder
    + Remind every 24 hours to participate
+ Added Quick Mod Menu Switching (default disabled)
    + Allows for fast navigation between one Mod Config and another
    + Default disabled
    + Detects your SkyBlock Mod automatically
    + Does detect Chat Triggers and OneConfig itself, but no single mods that require these libraries
+ Added **Arachne Chat Hider**
    + Hide chat messages about the Arachne Fight while outside of Arachne's Sanctuary
+ Added **Contest Time Needed**
    + Show the time and missing FF for every crop inside Jacob's Farming Contest inventory
+ Added **Sack Item Display** (Contributed by HiZe)
    + Added price display next to sack items
    + Can be disabled
    + Sortable by price or items stored (both desc/asc)
    + Option to show prices from Bazaar or NPC
+ Added profile id chat hider
+ Added Garden Crop Start Location
    + Show the start waypoint for your farm with the currently holding tool.
    + Auto-detects the start of the farm when farming for the first time
    + Option to manually set the waypoint with `/shcropstartlocation`
+ Added Pet Candies Used number
    + Works even after Hypixel removed the `10 pet candies applied` line
+ Added Estimated Armor Value display
    + Shows the price of all 4 armor pieces combined inside the wardrobe
+ Added Garden Plot Icon (Contributed by HiZe)
    + Select an item from the inventory to replace the icon in the Configure Plots inventory
    + Change the Edit mode in the bottom right corner in the Configure Plots inventory
+ Showing fished item names
+ Show numbers of Pocket Sack-In-A-Sack applied on a sack (Default disabled, contributed by HiZe)
+ Added a warning when finding a visitor with a rare reward
    + Show message in chat, over the visitor and prevents refusing
+ Added composter empty timer for outside garden
+ Added title warning when picking up an expensive slayer item
+ Added **RNG Meter Display**
    + Display number of bosses needed until the next RNG Meter item drops
    + Warn when no item is set in the RNG Meter
    + Hide the RNG Meter message from chat if the current item is selected
+ Added **Ghost Counter** (Contributed by HiZe)
    + Shows number of ghosts killed in the Mist in Dwarven Mines
    + Shows kill combo, coins per scavenger, all item drops, bestiary, magic find and more
    + Each display line is highly customizable

### Changes

+ Added Options for displays Crop Milestone and Best Crop Time.
    + Change the time format/the highest time unit to show. (1h30m vs. 90 min)
    + Maxed Milestone:  Calculate the progress and ETA until maxed milestone (46) instead of the next milestone.
+ Changed 'Warp to Trapper Hotkey' to only work while in the Farming Islands
+ Changed Trevor Trapper find mods logic (Contributed by CalMWolfs)
    + Fixed Detection of Oasis Mobs and Horse
    + Improved detection speed
    + Derpy double health support
    + More fair detection logic
+ Added extra setting to allow/block clicks for the 'hide not clickable' feature
+ Disabled hide far particles feature in M7 boss fight. This will fix M7 boss fight features from other mods
+ Added support for multiple players/profiles:
    + SkyHanni saves all profile specific data now separately (e.g., garden data, crimson isle reputation progress,
      minion display text on the island)
    + Config toggles and GUI elements stay synced between all profiles
+ Reworked Hide Armor
    + More options
        + Fixed Movement bugs with Depth Strider enchantment
        + Fixed compatibility issues with other mods
        + Note: The hide armor config got reset, if you want to use it, you have to enable it again)
+ Added support for maxed out crop milestones
+ Showing total price for visitor items needed
+ Warning when BPS in Jacob contest ff needed display is below 1
+ More Discord Rich Presence features (Contributed by NetheriteMiner)
    + Option "Profile" not only shows the profile name but also the profile type and SkyBlock level
    + Option "Dynamic" now also supports stacking enchantments
+ Highlight the item in bazaar search result inventory when clicking on an item list (Contributed by CalMWolfs)
+ Resetting Ragnarock ability cooldown when being hit during channeling
+ Hide Non God Pot Effect Display while inside the Rift

### Fixes

+ Fixed typos in Trevor Trapper texts (Contributed by CalMWolfs)
+ Fixed Hypixel bug that the equipment lore talks about "kills" instead of "visitors" (Contributed by CalMWolfs)
+ Fixed reforge stone 'warped' not detected in item price display
+ Hotkey to open SkyHanni Position Editor no longer works inside signs
+ Fixed non god pot effects display being wrong sometimes
+ Fixed duplex not working in Bazaar features
+ Fixed crashes with repo errors
+ If you touch water, the King Scent form Non God Pot Timer will now be correctly removed

### Removals

- Removed Garden Recent Teleport Pads display

## Version 0.17 (2023-05-11)

### Features

+ Added **Time to Kill**
    + Show the time it takes to kill the Slayer boss.
+ Added skill and collection level as item stack.
+ Added **Auction Highlighter**
    + Highlight own items that are sold in green and that are expired in red.
+ Added support for tier 1 minions and title send for the minion craft helper.
+ Added Chicken head Timer.
+ Added **rancher boots** speed display.
+ Added **Unclaimed Rewards**
    + Highlight contests with unclaimed rewards in the jacob inventory.
+ Added **Duplicate Hider**
    + Hides duplicate farming contests in the inventory.
+ Added **Contest Time**
    + Adds the real time format to the farming contest description.
+ Added **Hide Repeated Catches** (contributed by appable)
    + Delete past catches of the same trophy fish from chat
+ Added **Trophy Counter Design** (contributed by appable)
    + Change the way trophy fish messages gets displayed in the chat
+ Added **CH Join**
    + Helps buy a Pass for accessing the Crystal Hollows if needed
+ Added **Estimated Item Value**
    + Displays an estimated item value for the item you hover over
+ Added Arachne to damage indicator.
+ Added **Arachne Minis Hider**
    + Hides the nametag above arachne minis
+ Added **Arachne Boss Highlighter**
    + Highlight the arachne boss in red and mini bosses and orange.
+ Added **Discord RPC** (contributed by NetheriteMiner)
    + Showing stats like Location, Purse, Bits, Purse or Held Item at Discord Rich Presence

### Garden Features

+ Added **Copper Price**
    + Show copper to coin prices inside the Sky Mart inventory.
+ Added **Visitor Display**
    + Show all items needed for the visitors.
+ Added **Visitor Highlight**
    + Highlight visitor when the required items are in the inventory or the visitor is new and needs to checked what
      items it needs
+ Added **Show Price**
    + Show the Bazaar price of the items required for the visitors.
+ Added **Crop Milestone** Number
    + Show the number of the crop milestone in the inventory.
+ Added **Crop Upgrades** Number
    + Show the number of upgrades in the crop upgrades inventory.
+ Added **Visitor Timer**
    + Timer when the next visitor will appear, and a number how many visitors are already waiting.
+ Added **Visitor Notification**
    + Show as title and in chat when a new visitor is visiting your island.
+ Added **Plot Price**
    + Show the price of the plot in coins when inside the Configure Plots inventory.
+ Added **Garden Crop Milestone Display**
    + Shows the progress and ETA until the next crop milestone is reached and the current crops/minute value. (Requires
      a tool with either a counter or cultivating enchantment)
+ Added **Best Crop Display**
    + Lists all crops and their ETA till next milestone. Sorts for best crop for getting garden level or SkyBlock level.
+ Added **Copper Price**
    + Show the price for copper inside the visitor gui.
+ Added **Amount and Time**
    + Show the exact item amount and the remaining time when farmed manually. Especially useful for ironman.
+ Added **Custom Keybinds**
    + Use custom keybinds while having a farming tool or Daedalus Axe in the hand in the garden.
+ Added Desk shortcut in SkyBlock Menu.
+ Added **Garden Level Display**
    + Show the current garden level and progress to the next level.
+ Added **Farming Weight and Leaderboard**, provided by the Elite SkyBlock farmers.
+ Added farming weight next leaderboard position eta.
+ Added **Dicer Counter**
    + Count RNG drops for Melon Dicer and Pumpkin Dicer.
+ Added **Optimal Speed**
    + Show the optimal speed for your current tool in the hand. (Ty MelonKingDE for the values)
    + Also available to select directly in the rancher boots overlay (contributed by nea)
+ Added **Warn When Close**
    + Warn with title and sound when the next crop milestone upgrade happens in 5 seconds
    + Useful for switching to a different pet for leveling
+ Added **Money per Hour**
    + Displays the money per hour YOU get with YOUR crop/minute value when selling the items to bazaar.
+ Added farming contest timer.
+ Added wrong fungi cutter mode warning.
+ Added show the price per garden experience inside the visitor gui.
+ Added support for mushroom cow pet perk. (Counting and updating mushroom collection when breaking crops with mushroom
  blocks, added extra gui for time till crop milestones)
+ Added blocks/second display to crop milestone gui and made all crop milestone gui elements customizable/toggleable.
+ Added farming armor drops counter.
+ Added **Colored Name**
    + Show the visitor name in the color of the rarity.
+ Added **Visitor Item Preview**
    + Show the base type for the required items next to new visitors (Note that some visitors may require any crop)
+ Added **Teleport Pad Compact Name**
    + Hide the 'Warp to' and 'No Destination' texts over teleport pads.
+ Added **Money per Hour Advanced stats**
    + Show not only Sell Offer price but also Instant Sell price and NPC Sell price
    + Suggestion: Enable Compact Price as well for this
+ Added **Anita Medal Profit**
    + Helps to identify profitable items to buy at the Anita item shop and potential profit from selling the item at the
      auction house.
+ Added **Composter Compact Display**
    + Displays the compost data from the tab list in a compact form as gui element.
+ Added **Composter Upgrade Price**
    + Show the price for the composter upgrade in the lore
+ Added **Highlight Upgrade**
    + Highlight Upgrades that can be bought right now.
+ Added **Number Composter Upgrades**
    + Show the number of upgrades in the composter upgrades inventory.
+ Added **Composter Inventory Numbers**
    + Show the amount of Organic Matter, Fuel and Composts Available while inside the composter inventory.
+ Added **True Farming Fortune
    + Displays** current farming fortune, including crop-specific bonuses. (contributed by appable)
+ Added **Tooltip Tweaks Compact Descriptions**
    + Hides redundant parts of reforge descriptions, generic counter description, and Farmhand perk explanation. (
      contributed by appable)
+ Added **Tooltip Tweaks Breakdown Hotkey**
    + When the keybind is pressed, show a breakdown of all fortune sources on a tool. (contributed by appable)
+ Added **Tooltip Tweaks Tooltip Format**
    + Show crop-specific farming fortune in tooltip. (contributed by appable)
+ Added command **/shcropspeedmeter**
    + Helps calculate the real farming fortune with the formula crops broken per block.
+ Added **Compost Low Notification**
    + Shows a notification as title when organic matter/fuel is low.
+ Added **Jacob's Contest Warning**
    + Show a warning shortly before a new jacob contest starts.
+ Added **Inventory Numbers**
    + Show the number of the teleport pads inside the 'Change Destination' inventory as stack size.
+ Added **Composter Overlay**
    + Show the cheapest items for organic matter and fuel, show profit per compost/hour/day and time per compost
+ Added **Composter Upgrades Overlay**
    + Show an overview of all composter stats, including time till organic matter and fuel is empty when fully filled
      and show a preview how these stats change when hovering over an upgrade
+ Hide crop money display, crop milestone display and garden visitor list while inside anita show, SkyMart or the
  composter inventory
+ Hide chat messages from the visitors in the garden. (Except Beth and Spaceman)
+ Introduced a new command '/shcroptime <amount> <item>' that displays the estimated time it will take to gather the
  requested quantity of a particular item based on the current crop speed.
+ Show the average crop milestone in the crop milestone inventory.
+ Added **FF for Contest**
    + Show the minimum needed Farming Fortune for reaching a medal in the Jacob's Farming Contest inventory.
+ Added **yaw and pitch display**
    + Shows yaw and pitch with customizable precision while holding a farm tool.
    + Automatically fades out if no movement for a customizable duration (Contributed by Sefer)
+ Added warning when 6th visitors is ready (Contributed by CalMWolfs)

### Features from other Mods

> *The following features are only there because I want them when testing SkyHanni features without other mods present.*

+ Added Hide explosions.
+ Added **Enderman Teleportation Hider**
    + Stops the enderman teleportation animation (Like in SBA)
+ Added **Fire Overlay Hider**
    + Hide the fire overlay (Like in Skytils)

### Changes

+ Reworked reputation helper design in the crimson isle.
+ Moved the key setting for diana `warp to nearest burrow waypoint` from vanilla mc (esc -> config -> controls -> scroll
  all the way down to skyhanni category) to just `/sh diana`

### Fixed

+ Barbarian Duke Damage Indicator now only starts displaying after the player is getting close to him. (30 blocks)
+ Fixed a bug that caused fire veil particle `hider/redline drawer` to not always detect the right click correctly.
+ Removed `Fixing Skytils custom Damage Splash` (Skytils has already fixed this bug. Additionally, this option enabled
  and skytils' damage splash disabled caused the hypixel damage splash to not show the tailing commas at all)
+ Fixed bug with particles that blocks NotEnoughUpdates' Fishing features.
+ Hopefully fixed incompatibility with skytils `hide cheap coins` feature.
+ Fixed dungeon milestone messages getting wrongfully formatted.
+ Fixed bazaar features not working for items with an `-` (turbo farming books and triple strike)
+ Fixed Crab Hat of Celebration not being detected as an accessory correctly.
+ Added support for soopy's \[hand] feature

## Version 0.16 (2023-02-11)

## Features

+ Added highlight for stuff that is missing in the SkyBlock level guide inventory.
+ Added Bingo Card display.
+ **Minion Craft Helper** - Show how many more items you need to upgrade the minion in your inventory. Especially useful
  for bingo.
+ Hide dead entities - Similar to Skytils' feature for inside dungeon, but for everywhere.
+ Hide Fireball particles and hide Fire Block particles.
+ Made **blaze slayer clear view** work with more particles.
+ Added colors for the missing slayer area bosses (Golden Ghoul, Old Wolf and Spider Keeper)
+ Added Arachne keeper highlighter.
+ Added area boss highlighter.
+ Added area boss spawn timer.
+ Added Corleone highlighter.
+ Added **Tps Display** - Show the Tps of the current server.
+ Added Kuudra Key Number overlay.
+ Added colored highlight support for zealots, bruisers and special zealots.
+ Added more particle hiders. (Far particles, near redstone particles and smoke particles)

## Removals

- Removed Blaze slayer Pillar warning + timer (The Feature caused lags and Soopy's approach is better)

## Version 0.15.1 (2023-01-25)

## Features

+ Adding green line around items that are clickable. (Inside the **Not Clickable Items Feature**)
+ Added **Odger waypoint** - Show the Odger waypoint when trophy fishes are in the inventory and no lava rod in hand.
+ Added the option to hide the red numbers in the scoreboard sidebar on the right side of the screen.
+ Added **Tia Relay Waypoint** - Show the next Relay waypoint for Tia The Fairy, where maintenance for the abiphone
  network needs to be done.
+ Added **Tia Relay Helper** - Helps with solving the sound puzzle.

## Changes

+ Hide reputation boss waypoint when damage indicator is present.

## Version 0.15 (2023-01-22)

### Features

+ Added Bazaar Update Timer - Forrick.
+ Added Crimson Isle Reputation Helper.
+ Added **Barn Timer** - Show the time and amount of sea creatures while fishing on the barn via hub.
+ Added **Shark Fish Counter** - Counts how many sharks have been caught.
+ Added **Hide Silver Duplicates** - Hiding chat message when catching a duplicate silver trophy fish.

### Changes

+ **Damage Indicator** text size scale now changes dynamically.
+ Enderman slayer beacon now renders behind the damage indicator text, for better readability.

### Fixes

+ Fixed **Steaming Hot Flounder** in custom trophy fish counter.
+ Fixed hide powder gain message from chests in crystal hollows. (thanks hypixel)
+ Fixed damage indicator for M3 Professor bug. (Thanks hypixel)

## Version 0.14

### Features

+ Added /shtrackcollection <item> - This tracks the number of items you collect, but it does not work with sacks.
+ Added Compact Bingo Chat Messages.
+ Added Compact Potion Effect Chat Messages.
+ Added Brewing Stand Overlay.
+ Added minion name display with minion tier.

### Changes

+ Don't render overlays when tab list key is pressed.
+ Do no longer prevent the selling of bazaar items to NPC when on ironman, stranded or bingo mode.

### Fixes

+ Fixed wrong dungeon level colors. (in Party Finder)
+ Fixed error in Sea Creature Guide inventory.

## Version 0.13

### Features

+ Player Rank Hider works now with all messages.
+ Add two more chat filter categories: Powder Mining and Winter Gifts.
+ Add catacombs class level color to party finder.
+ Add wishing compass uses amount display.
+ Saves missing items from cancelled buy orders to clipboard for faster re-entry.
+ Adds a visual highlight to the Croesus inventory that show what chests have not yet been opened.

### Removals

- Removed additional settings for the chat design, like channel prefix and SkyBlock level

### Fixes

+ Fixed void slayer mini bosses not being detected and colored.

## Version 0.12.2

### Changes

+ Add Bonzo's Mask highlight timer
+ Made the config button in the forge mod list work.
+ Blaze slayer pillar warning text is now much bigger.
+ Hides the new 'you earn x event exp' message.
+ Added Peek Chat feature.

### Fixes

+ Fixed a bug that caused the right blaze slayer dagger highlight to show wrong values.
+ Fixed kill combo message filter format

## Version 0.12.1

### Fixes

+ Fixed a crash when checking if the hotkey is pressed.

## Version 0.12

### Diana

+ Show burrows near you.
+ Uses Soopy's Guess Logic to find the next burrow. Does not require SoopyV2 or chat triggers to be installed.
+ Let Soopy guess the next burrow location for you.
+ Warps to the nearest warp point on the hub, if closer to the next burrow.

### Crimson Isle

+ Option to remove the wrong blaze slayer dagger messages from chat.
+ Hide particles and fireballs near blaze slayer bosses and demons.
+ Added Option to hide blaze particles.
+ Highlight millenia aged blaze color in red

### Changes

+ Removed more blaze slayer item drop message spam.
+ Showing number next to custom hellion shield from damage indicator.
+ All particles next to ashfang are now hidden when clear ashfang view is enabled.
+ All particles around blaze slayer are hidden when blaze particles is enabled.
+ Hide blocks circling around ashfang.
+ Hide the vanilla nametag of damage indicator bosses.

## Version 0.11

### Slayer

+ Added toggle to ender slayer phase display
+ Added blaze slayer phase display
+ Added toggle for blaze slayer colored mobs
+ Mark the right dagger to use for blaze slayer in the dagger overlay.
+ Added option to select the first, left sided dagger for the display.

### Stats Tuning

+ Show the tuning stats in the Thaumaturgy inventory.
+ Show the amount of selected tuning points in the stats tuning inventory.
+ Highlight the selected template in the stats tuning inventory.
+ Show the type of stats for the tuning point templates.

### Misc

+ Added Lord Jawbus to damage indicator
+ Display the active non-god potion effects.

### Changes

+ Showing Thunder Sparks while in lava
+ Damage indicator switches into cleaner design when close to boss and not in f5 view

### Fixes

+ Reduced lags after world switch

## Version 0.10 - Slayer

### Blaze Slayer

+ Added a cooldown when the Fire Pillars from the Blaze Slayer will kill you.
+ Added a faster and permanent display for the Blaze Slayer daggers.
+ Added custom countdown sound for the Fire Pillar timer for the Blaze Slayer.
+ Added hide sound and entities when building the Fire Pillar for the Blaze Slayer.
+ Added warning when the fire pit phase starts for the Blaze Slayer tier 3.

### Slayer

+ Added warning when wrong slayer quest is selected, or killing mobs for the wrong slayer.
+ Added hide more poor slayer drop chat messages.

### Misc

+ Added option to hide armor or just helmet of other player or yourself.

### Fixes

+ Fixed overload damage gets not detected as damage splash.

## Version 0.9 - Chat + Dungeon

### Chat

+ Added option to enable that clicking on a player name in chat opens the profile viewer of NotEnoughUpdates (to fix
  SkyHanni breaking the default NEU feature).
+ Added support for new SBA chat icon feature (show profile type and faction in chat)
+ Highlight marked player names in chat.
+ Scan messages sent by players in all-chat for blacklisted words and greys out the message
+ Links in player chat are clickable again

### Dungeon

+ Added hide the damage, ability damage and defense orbs that spawn when the healer is killing mobs in dungeon
+ Added hide the golden fairy that follows the healer in dungeon.
+ Added hidden music for the clean end of the dungeon

### Misc

+ Added hide the name of the mobs you need to kill in order for the Slayer boss to spawn. Exclude mobs that are damaged,
  corrupted, runic or semi rare.
+ Added option to hide all damage splashes, from anywhere in SkyBlock.
+ Added highlight Thunder Sparks after killing a Thunder
+ Added Thunder to damage indicator
+ Mark the own player name (for Chat messages)

### Fixed

- Fixed vanilla items not being sellable to npc

## Version 0.8

### Misc

- Added hiding the flame particles when using the Fire Veil Wand ability
- Added circle around the player when having the Fire Veil Wand ability is active
- The config GUI how has a search function (top right corner)
- Added more player chat format options (show all channel prefix, hide player rank, hide colon after player name,
  hide/change elite position format, channel prefix design)

### Small Changes

- Changed the ashfang gravity orb highlight from square to cylinder
- Added msg support to fancy chat format
- Added damage indicator for diana mobs

### Fixes

- Fixed enchanted books in bazaar
- Fixed LorenzLogger printing output into console
- Fixed best sell method not working for some bazaar items
- Fixed summoning mobs display don't track mobs when moving too fast while spawning summoning mobs

## Version 0.7 - Dungeon and Colored Highlight

### New Dungeon Features

- Added highlight deathmites in dungeon in red color
- Added hide Superboom TNT lying around in dungeon
- Added hide Blessings lying around in dungeon
- Added hide Revive Stones lying around in dungeon
- Added hide Premium Flesh lying around in dungeon
- Added Dungeon Copilot (Suggests to you what to do next in dungeon)
- Added separate option to hide dungeon key pickup and door open messages
- Added hide Journal Entry pages lying around in dungeon.
- Added hide Skeleton Skulls lying around in dungeon.
- Added highlight Skeleton Skulls in dungeon when combining into a skeleton in orange color (not useful combined with
  feature Hide Skeleton Skull)

### Other Misc Features

- Added option to hide the SkyBlock Level from the chat messages (alpha only atm)
- Added option to change the way the SkyBlock Level gets displayed in the chat (only working when SkyBlock level and
  fancy player message format are enabled)
- Added highlight the voidling extremist in pink color
- Added highlight corrupted mobs in purple color
- Added command /shmarkplayer <player> (marking a player with yellow color)
- Added highlight slayer mini boss in blue color
- Added option to hide the death messages of other players, except for players who are close to the player, inside
  dungeon or during a Kuudra fight.
- Added highlight the enderman slayer Yang Glyph (Beacon) in red color (supports beacon in hand and beacon flying)

### Fixes

- Fixed message filter for small bazaar messages

## Version 0.6 - Ashfang and Summoning Mobs

### New Features

- Added /wiki command (using hypixel-skyblock.fandom.com instead of Hypixel wiki)
- Added hiding damage splashes while inside the boss room (replacing a broken feature from Skytils)
- Added Summoning Mob Display (Show the health of your spawned summoning mobs listed in an extra GUI element and hiding
  the corresponding spawning/de-spawning chat messages)
- Added option to hide the nametag of your spawned summoning mobs
- Added option to mark the own summoning mobs in green
- Added Ashfang Blazing Souls display
- Added highlight for the different ashfang blazes in their respective color
- Added option to hide all the particles around the ashfang boss
- Added option to hide the name of full health blazes around ashfang (only useful when highlight blazes is enabled)
- Added option to hide damage splashes around ashfang

### Minor Changes

- Optimizing the highlight block size for minions, blazing souls and gravity orbs
- Added option to change the gray-out opacity for 'Not Clickable Items'
- Added option to show the health of Voidgloom Seraph 4 during the laser phase (useful when trying to phase skip)
- Fixed that items with stars don't gray out properly when hidden by the 'hide not clickable' feature
- Fixed 'hiding the nametag of mobs close to minions' not working when minion nearby was never collected

## Version 0.5 - Minions and RNG Meter

### New Features

- Added a display that show the last time the hopper inside a minion has been emptied
- Added a marker to the last opened minion for a couple of seconds (Seen through walls)
- Added option to hide mob nametags close to minions
- Added showing stars on all items (Not only dungeon stars and master stars but also on crimson armors, cloaks and
  fishing rods)
- Added **Real Time** - Display the current computer time, a handy feature when playing in full-screen mode.
- Added overlay features to the RNG meter inventory (Highlight selected drop and floors without a drop and show floor)
- Added minion hopper coins per day display (Using the held coins in the hopper and the last time the hopper was
  collected to calculate the coins a hopper collects in a day)

### Minor Changes

- Summoning souls display is rendering better close to corners
- Ashfang gravity orbs are now rendering better close to corners
- Showing the name of ashfang gravity orbs as a nametag above the orb
- Bazaar now knows books and essences (Thanks again, Hypixel)

### Bug Fixes

- Fixed damage Indicator damage over time display order swapping sometimes

## Version 0.4.2 - Repair what Hypixel broke

### New Features

- Added grabbing the API key from other mods. First time using SkyHanni should not require you to set the API key
  manually (Thanks efefury)

### Fixes

- Fixing ender slayer health for damage indicator (Hypixel broke it)
- Fixing format to hide all blessing messages in dungeon again (Hypixel broke it)
- Fixing 'damage splash hiding' when near damage indicator (Hypixel broke it)
- Fixed Skytils custom damage splash (Hypixel broke it)

## Hotfix 0.4.1 - Removing red bazaar errors

- This is no support for the new enchanted books in the bazaar. It’s just removing the red error messages from the chat.

## Version 0.4

### Damage Indicator stuff

- Added damage indicator healing chat messages
- Added damage indicator showing boss name (not working in dungeon yet)
- Added damage indicator option to hide or only show short name
- Added option to enable/disable damage indicator for specific bosses
- Added enderman slayer hits display to damage indicator
- Added that damage indicator is showing when the boss is dead
- Added enderman slayer laser phase cooldown to damage indicator
- Added all slayers to damage indicator (except blaze above tier 1)
- Added revenant slayer 5 boom display to damage indicator
- Fixed damage indicator f4 and m4 thorn support for Derpy
- Added option to hide vanilla (or Skytils) damage splashes next to damage indicator
- Added damage/healing over time display to damage indicator
- Added Training Dummy (on personal island) to damage indicator

### Other stuff

+ Added ashfang reset cooldown
+ Added fire veil wand ability cooldown
+ Added custom player chat for dead players in dungeon and for visiting players on own islands
+ Added ashfang gravity orbs display

## Version 0.3

- Added damage indicator for magma boss and headless horseman
- Added summoning souls display
- Added Derpy support for damage indicator

## Version 0.1

- Added damage indicator for some bosses who are outside dungeon (4 nether bosses: Ashfang, barbarian duke, mage outlaw
  and Bladesoul, slayers: Enderman 1-4, revenant 5, and untested support for Vanquisher in Crimson Isle, Ender Dragon and
  Endstone Protector in end)
- Added item ability cooldown background display (over the slot, work in progress)
- Added Ashfang freeze cooldown (when you get hit by “anti ability” and slowness effect)
- Changed “hot clickable items” to show items again, but only with dark gray overlay. Looks nicer
- Made the GitHub repository public
