package at.hannibal2.skyhanni.utils.compat;

import net.minecraft.client.Minecraft;

public class Pattterns {

    @Pattern
    private static boolean isOnMainThread(Minecraft mc) {
        //#if MC>=11400
        //$$ return mc.isOnThread();
        //#else
        return mc.isCallingFromMinecraftThread();
        //#endif
    }
}
