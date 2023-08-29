package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.fishing.trophy.TrophyRarity
import at.hannibal2.skyhanni.features.garden.CropType
import at.hannibal2.skyhanni.features.misc.update.UpdateManager
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzVec
import at.hannibal2.skyhanni.utils.NEUInternalName
import at.hannibal2.skyhanni.utils.NEUInternalName.Companion.asInternalName
import at.hannibal2.skyhanni.utils.NEUItems
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.moulberry.moulconfig.observer.PropertyTypeAdapterFactory
import io.github.moulberry.moulconfig.processor.BuiltinMoulConfigGuis
import io.github.moulberry.moulconfig.processor.ConfigProcessorDriver
import io.github.moulberry.moulconfig.processor.MoulConfigProcessor
import net.minecraft.item.ItemStack
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.concurrent.fixedRateTimer

class ConfigManager {
    companion object {
        val gson = GsonBuilder().setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeSpecialFloatingPointValues()
            .registerTypeAdapterFactory(PropertyTypeAdapterFactory())
            .registerTypeAdapter(UUID::class.java, object : TypeAdapter<UUID>() {
                override fun write(out: JsonWriter, value: UUID) {
                    out.value(value.toString())
                }

                override fun read(reader: JsonReader): UUID {
                    return UUID.fromString(reader.nextString())
                }
            }.nullSafe())
            .registerTypeAdapter(LorenzVec::class.java, object : TypeAdapter<LorenzVec>() {
                override fun write(out: JsonWriter, value: LorenzVec) {
                    value.run { out.value("$x:$y:$z") }
                }

                override fun read(reader: JsonReader): LorenzVec {
                    val (x, y, z) = reader.nextString().split(":").map { it.toDouble() }
                    return LorenzVec(x, y, z)
                }
            }.nullSafe())
            .registerTypeAdapter(TrophyRarity::class.java, object : TypeAdapter<TrophyRarity>() {
                override fun write(out: JsonWriter, value: TrophyRarity) {
                    value.run { out.value(value.name) }
                }

                override fun read(reader: JsonReader): TrophyRarity {
                    val text = reader.nextString()
                    return TrophyRarity.getByName(text) ?: error("Could not parse TrophyRarity from '$text'")
                }
            }.nullSafe())
            .registerTypeAdapter(ItemStack::class.java, object : TypeAdapter<ItemStack>() {
                override fun write(out: JsonWriter, value: ItemStack) {
                    out.value(NEUItems.saveNBTData(value))
                }

                override fun read(reader: JsonReader): ItemStack {
                    return NEUItems.loadNBTData(reader.nextString())
                }
            }.nullSafe())
            .registerTypeAdapter(NEUInternalName::class.java, object : TypeAdapter<NEUInternalName>() {
                override fun write(out: JsonWriter, value: NEUInternalName) {
                    out.value(value.asString())
                }

                override fun read(reader: JsonReader): NEUInternalName {
                    return reader.nextString().asInternalName()
                }
            }.nullSafe())
            .enableComplexMapKeySerialization()
            .create()
    }

    lateinit var features: Features
        private set
    private val logger = LorenzLogger("config_manager")

    var configDirectory = File("config/skyhanni")
    private var configFile: File? = null
    lateinit var processor: MoulConfigProcessor<Features>

    fun firstLoad() {
        if (::features.isInitialized) {
            logger.log("Loading config despite config being already loaded?")
        }
        configDirectory.mkdir()

        configFile = File(configDirectory, "config.json")

        logger.log("Trying to load config from $configFile")

        if (configFile!!.exists()) {
            try {
                val inputStreamReader = InputStreamReader(FileInputStream(configFile!!), StandardCharsets.UTF_8)
                val bufferedReader = BufferedReader(inputStreamReader)
                val builder = StringBuilder()
                for (line in bufferedReader.lines()) {
                    val result = fixConfig(line)
                    builder.append(result)
                    builder.append("\n")
                }


                logger.log("load-config-now")
                val jsonObject = gson.fromJson(builder.toString(), JsonObject::class.java)
                val newJsonObject = ConfigUpdaterMigrator.fixConfig(jsonObject)
                features = gson.fromJson(
                    newJsonObject,
                    Features::class.java
                )
                logger.log("Loaded config from file")
            } catch (error: Exception) {
                error.printStackTrace()
                val backupFile = configFile!!.resolveSibling("config-${System.currentTimeMillis()}-backup.json")
                logger.log("Exception while reading $configFile. Will load blank config and save backup to $backupFile")
                logger.log("Exception was $error")
                try {
                    configFile!!.copyTo(backupFile)
                } catch (e: Exception) {
                    logger.log("Could not create backup for config file")
                    e.printStackTrace()
                }
            }
        }

        if (!::features.isInitialized) {
            logger.log("Creating blank config and saving to file")
            features = Features()
            saveConfig("blank config")
        }

        fixedRateTimer(name = "skyhanni-config-auto-save", period = 60_000L, initialDelay = 60_000L) {
            saveConfig("auto-save-60s")
        }

        val features = SkyHanniMod.feature
        processor = MoulConfigProcessor(SkyHanniMod.feature)
        BuiltinMoulConfigGuis.addProcessors(processor)
        UpdateManager.injectConfigProcessor(processor)
        ConfigProcessorDriver.processConfig(
            features.javaClass,
            features,
            processor
        )
    }

    private fun fixConfig(line: String): String {
        var result = line
        for (type in CropType.entries) {
            val normal = "\"${type.cropName}\""
            val enumName = "\"${type.name}\""
            while (result.contains(normal)) {
                result = result.replace(normal, enumName)
            }
        }
        return result
    }

    fun saveConfig(reason: String) {
        logger.log("saveConfig: $reason")
        val file = configFile ?: throw Error("Can not save config, configFile is null!")
        try {
            logger.log("Saving config file")
            file.parentFile.mkdirs()
            val unit = file.parentFile.resolve("config.json.write")
            unit.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream(unit), StandardCharsets.UTF_8)).use { writer ->
                // TODO remove old "hidden" area
                writer.write(gson.toJson(SkyHanniMod.feature))
            }
            // Perform move — which is atomic, unlike writing — after writing is done.
            unit.renameTo(file)
        } catch (e: IOException) {
            logger.log("Could not save config file to $file")
            e.printStackTrace()
        }
    }
}