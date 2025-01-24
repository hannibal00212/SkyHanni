package at.hannibal2.skyhanni.config.features.dev.minecraftconsole;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class ConsoleFiltersConfig {
    @Expose
    @ConfigOption(name = "Filter Chat", desc = "Filter chat messages.")
    @ConfigEditorBoolean
    public boolean filterChat = false;

    @Expose
    @ConfigOption(name = "Filter Grow Buffer", desc = "Filter 'Needed to grow BufferBuilder buffer:'")
    @ConfigEditorBoolean
    public boolean filterGrowBuffer = true;

    @Expose
    @ConfigOption(name = "Filter Sound Error", desc = "Filter 'Unable to play unknown soundEvent'.")
    @ConfigEditorBoolean
    public boolean filterUnknownSound = true;

    @Expose
    @ConfigOption(name = "Filter Scoreboard Errors", desc = "Filter error messages with Scoreboard: removeTeam, createTeam, " +
        "removeObjective and 'scoreboard team already exists'.")
    @ConfigEditorBoolean
    public boolean filterScoreboardErrors = true;

    @Expose
    @ConfigOption(name = "Filter Particle", desc = "Filter message 'Could not spawn particle effect VILLAGER_HAPPY'.")
    @ConfigEditorBoolean
    public boolean filterParticleVillagerHappy = true;

    @Expose
    @ConfigOption(name = "Filter OptiFine", desc = "Filter OptiFine messages CustomItems and ConnectedTextures during loading.")
    @ConfigEditorBoolean
    public boolean filterOptiFine = true;

    @Expose
    @ConfigOption(name = "Filter AsmHelper Transformer", desc = "Filter messages when AsmHelper is Transforming a class during loading.")
    @ConfigEditorBoolean
    public boolean filterAmsHelperTransformer = true;

    @Expose
    @ConfigOption(name = "Filter Applying AsmWriter", desc = "Filter messages when AsmHelper is applying AsmWriter ModifyWriter.")
    @ConfigEditorBoolean
    public boolean filterAsmHelperApplying = true;

    @Expose
    @ConfigOption(name = "Filter Biome ID Bounds", desc = "Filter message 'Biome ID is out of bounds'.")
    @ConfigEditorBoolean
    public boolean filterBiomeIdBounds = true;
}
