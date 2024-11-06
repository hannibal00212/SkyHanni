package at.hannibal2.skyhanni.events

import at.hannibal2.skyhanni.data.jsonobjects.other.HypixelApiTrophyFish
import at.hannibal2.skyhanni.data.jsonobjects.other.HypixelPlayerApiJson
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.NumberUtil.isInt
import at.hannibal2.skyhanni.utils.json.BaseGsonBuilder
import at.hannibal2.skyhanni.utils.json.fromJson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import io.github.moulberry.notenoughupdates.events.ProfileDataLoadedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule(neuRequired = true)
object NeuEventWrappers {

    private val hypixelApiGson by lazy {
        BaseGsonBuilder.gson()
            .registerTypeAdapter(
                HypixelApiTrophyFish::class.java,
                object : TypeAdapter<HypixelApiTrophyFish>() {
                    @Suppress("EmptyFunctionBlock")
                    override fun write(out: JsonWriter, value: HypixelApiTrophyFish) {}

                    override fun read(reader: JsonReader): HypixelApiTrophyFish {
                        val trophyFish = mutableMapOf<String, Int>()
                        var totalCaught = 0
                        reader.beginObject()
                        while (reader.hasNext()) {
                            val key = reader.nextName()
                            if (key == "total_caught") {
                                totalCaught = reader.nextInt()
                                continue
                            }
                            if (reader.peek() == JsonToken.NUMBER) {
                                val valueAsString = reader.nextString()
                                if (valueAsString.isInt()) {
                                    trophyFish[key] = valueAsString.toInt()
                                    continue
                                }
                            }
                            reader.skipValue()
                        }
                        reader.endObject()
                        return HypixelApiTrophyFish(totalCaught, trophyFish)
                    }
                }.nullSafe(),
            )
            .create()
    }


    @SubscribeEvent
    fun onProfileDataLoaded(event: ProfileDataLoadedEvent) {
        val apiData = event.data ?: return
        try {
            val playerData = hypixelApiGson.fromJson<HypixelPlayerApiJson>(apiData)
            NeuProfileDataLoadedEvent(playerData).postAndCatch()

        } catch (e: Exception) {
            ErrorManager.logErrorWithData(
                e, "Error reading hypixel player api data",
                "data" to apiData,
            )
        }
    }

    @SubscribeEvent
    fun onNeuRepoReload(event: RepositoryReloadEvent) {
        NeuRepositoryReloadEvent().postAndCatch()
    }
}
