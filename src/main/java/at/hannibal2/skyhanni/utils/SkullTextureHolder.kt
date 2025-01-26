package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.events.RepositoryReloadEvent
import at.hannibal2.skyhanni.features.mining.eventtracker.MiningEventType
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule

@SkyHanniModule
object SkullTextureHolder {

    private var skullTextures = mutableMapOf<String, String>()
    // I just took this from the Skulls.json
    @Suppress("MaxLineLength")
    private val ALEX_SKIN_TEXTURE = "fRBfVNlIWW6cL478st/8NsNEHVxjvwQDp4+MbKbFj1tPZvxXgpIXRaQsLeDl/0+E4tipPKNANAbmqj9EKAVx3b3gDqLLrTTk/NfuH2RD3I5ppzio8w5oYk1022SopaayGBP4+kuwktDHzlR8IgAUb1RiavldKp+TGRdCbqw8vHHBm9pnuOePzTOOADQgdanRj98bOcfIXe69tSS/VHxDe9tkpYFPkQR8zsJcjUxf+nS83iFU9CW9lKtQlyoU6/BPbHFILvcR1KDR5Imj7GJe2OJefghI6OqtHNZP2tzkia2IDU0Yc4ikwC+7yN3i6I3Do4G3gTtCZVfNXiSdFyU9nCMyBxggTaG9zaljZpN0BynG4FzYMujIVgeNa6FLqwoaFT0iELW2w9JgJFgyVlaDKEqMSGyxgqtcQMPBuvCwMFFjeFd2EhtfTjQ4hcpva+NXXoYPP7yfTk/0DErNZV2dUTasekar8lH6U58B7ECNxDUwcon4z7sSO5mdlPJoiT7zllgpwQn5NUPaxZxaKkGdUIFEGzjmBfnCmk6MOqzi05Rr18wnkdic9hz/fIzzTMhn9mbMG6VF9eBkE4mNu1K5jai6II5Mz9BV49U0ZcA874N1VHpJpQE6762TYv+u7ICTRIOf2LD9wEgu3py/nX+IHma5j22ClUtXH3hYdZmHg+s=\",Value:\"ewogICJ0aW1lc3RhbXAiIDogMTcxMTY1OTI2NDg1NSwKICAicHJvZmlsZUlkIiA6ICI2YWI0MzE3ODg5ZmQ0OTA1OTdmNjBmNjdkOWQ3NmZkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQWxleCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84M2NlZTVjYTZhZmNkYjE3MTI4NWFhMDBlODA0OWMyOTdiMmRiZWJhMGVmYjhmZjk3MGE1Njc3YTFiNjQ0MDMyIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0="

    @HandleEvent
    fun onRepoReload(event: RepositoryReloadEvent) {
        skullTextures = event.getConstant<Map<String, String>>("Skulls").toMutableMap()
        fixLateInits()
    }

    fun getTexture(name: String): String {
        return skullTextures[name] ?: ALEX_SKIN_TEXTURE
    }

    // Any classes that rely on textures that cannot make use of by lazy or other late initializers
    private fun fixLateInits() {
        MiningEventType.fixGoblinItemStack()
    }
}
