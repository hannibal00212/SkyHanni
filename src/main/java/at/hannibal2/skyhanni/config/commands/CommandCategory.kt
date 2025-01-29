package at.hannibal2.skyhanni.config.commands

enum class CommandCategory(val color: String, val categoryName: String, val description: String) {
    MAIN(
        "§6",
        "Main Command",
        "Most useful commands of SkyHanni",
    ),
    USERS_ACTIVE(
        "§e",
        "Normal Command",
        "Normal Command for everyone to use",
    ),
    USERS_RESET(
        "§e",
        "Normal Reset Command",
        "Normal Command that resents some data",
    ),
    USERS_BUG_FIX(
        "§f",
        "User Bug Fix",
        "A Command to fix small bugs",
    ),
    DEVELOPER_TEST(
        "§5",
        "Developer Test Commands",
        "A Command to edit/test/change some features. §cIntended for developers only!",
    ),
    DEVELOPER_DEBUG(
        "§9",
        "Developer Debug Commands",
        "A Command to debug/read/copy/monitor features. §cIntended for developers only!",
    ),
    INTERNAL(
        "§8",
        "Internal Command",
        "A Command that should §cnever §7be called manually!",
    ),
    SHORTENED_COMMANDS(
        "§b",
        "Shortened Commands",
        "Commands that shorten or improve existing Hypixel commands!",
    )
}
