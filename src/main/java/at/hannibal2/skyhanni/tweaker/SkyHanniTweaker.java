package at.hannibal2.skyhanni.tweaker;

import net.hypixel.modapi.tweaker.HypixelModAPITweaker;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.spongepowered.asm.launch.MixinTweaker;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
public class SkyHanniTweaker implements ITweaker {

    public SkyHanniTweaker() {
        DownloadSourceChecker.init();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        List<String> tweakClasses = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClasses.add(MixinTweaker.class.getName());
        tweakClasses.add(ModLoadingTweaker.class.getName());
        tweakClasses.add(HypixelModAPITweaker.class.getName());
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
