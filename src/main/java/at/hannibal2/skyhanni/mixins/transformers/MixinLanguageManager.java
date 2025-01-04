package at.hannibal2.skyhanni.mixins.transformers;

import at.hannibal2.skyhanni.events.minecraft.MinecraftLanguageChangeEvent;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageManager.class)
public class MixinLanguageManager {
    @Inject(method = "setCurrentLanguage", at = @At("TAIL"))
    private void onLanguageSet(Language currentLanguageIn, CallbackInfo ci) {
        new MinecraftLanguageChangeEvent(currentLanguageIn).post();
    }
}
