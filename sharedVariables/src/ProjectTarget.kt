package at.skyhanni.sharedvariables

private fun yarn(version: String): String = "net.fabricmc:yarn:${version}:v2"

enum class ProjectTarget(
    val projectName: String,
    val minecraftVersion: MinecraftVersion,
    val mappingDependency: String,
    val mappingStyle: MappingStyle,
    val forgeDep: String?,
    linkTo: String?,
) {
    MAIN(
        "1.8.9",
        MinecraftVersion.MC189,
        "de.oceanlabs.mcp:mcp_stable:22-1.8.9@zip",
        MappingStyle.SEARGE,
        "net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9",
        "BRIDGE112",
    ),
    BRIDGE112(
        "1.12.2",
        MinecraftVersion.MC11202,
        "de.oceanlabs.mcp:mcp_stable:39-1.12@zip",
        MappingStyle.SEARGE,
        "net.minecraftforge:forge:1.12.2-14.23.5.2847",
        "BRIDGE116FORGE",
    ),
    BRIDGE116FORGE(
        "1.16.5-forge",
        MinecraftVersion.MC11605,
        "official",
        MappingStyle.SEARGE,
        "net.minecraftforge:forge:1.16.5-36.2.39",
        "BRIDGE116FABRIC",
    ),
    BRIDGE116FABRIC(
        "1.16.5",
        MinecraftVersion.MC11605,
        yarn("1.16.5+build.10"),
        MappingStyle.YARN,
        null,
        "MODERN",
    ),
    MODERN(
        "1.21",
        MinecraftVersion.MC121,
        yarn("1.21+build.9"),
        MappingStyle.YARN,
        null,
        null,
    )
    ;

    val isBridge get() = name.contains("bridge")

    val linkTo by lazy {
        if (linkTo == null) null
        else {
            ProjectTarget.values().find { it.name == linkTo }!!
        }
    }
    val parent by lazy {
        values().find { it.linkTo == this }
    }
    val isForge get() = forgeDep != null
    val isFabric get() = forgeDep == null

    val projectPath get() = ":$projectName"

    companion object {
        fun activeVersions() = values().filter { MultiVersionStage.activeState.shouldCreateProject(it) }
    }
}
