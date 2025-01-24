package at.hannibal2.skyhanni.tweaker;


import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * The mod loading tweaker makes sure that we are recognized as a Forge Mod, despite having a Tweaker.
 * We also add ourselves as a mixin container for integration with other mixin loaders.
 *
 * Taken from https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/20821e63057add096e314310ea8fa8e0c411e964/src/main/java/io/github/moulberry/notenoughupdates/loader/ModLoadingTweaker.java
 */
public class ModLoadingTweaker implements ITweaker {
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        URL location = ModLoadingTweaker.class.getProtectionDomain().getCodeSource().getLocation();
        if (location == null) return;
        if (!"file".equals(location.getProtocol())) return;
        try {
            MixinBootstrap.getPlatform().addContainer(location.toURI());
            String file = new File(location.toURI()).getName();
            CoreModManager.getIgnoredMods().remove(file);
            CoreModManager.getReparseableCoremods().add(file);
        } catch (URISyntaxException e) {
            System.err.println("SkyHanni-@MOD_VERSION@ could not re-add itself as mod.");
            e.printStackTrace();
        }
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

    }

    @Override
    public String getLaunchTarget() {
        return null;
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}
