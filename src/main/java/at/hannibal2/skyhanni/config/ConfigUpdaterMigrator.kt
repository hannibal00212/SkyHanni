package at.hannibal2.skyhanni.config

import at.hannibal2.skyhanni.events.LorenzEvent
import at.hannibal2.skyhanni.features.misc.limbo.LimboTimeTracker
import at.hannibal2.skyhanni.utils.LorenzLogger
import at.hannibal2.skyhanni.utils.LorenzUtils.asIntOrNull
import at.hannibal2.skyhanni.utils.json.shDeepCopy
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

object ConfigUpdaterMigrator {

    val logger = LorenzLogger("ConfigMigration")
    const val CONFIG_VERSION = 57
    fun JsonElement.at(chain: List<String>, init: Boolean): JsonElement? {
        if (chain.isEmpty()) return this
        if (this !is JsonObject) return null
        var obj = this[chain.first()]
        if (obj == null && init) {
            obj = JsonObject()
            this.add(chain.first(), obj)
        }
        return obj?.at(chain.drop(1), init)
    }

    data class ConfigFixEvent(
        val old: JsonObject,
        val new: JsonObject,
        val oldVersion: Int,
        var movesPerformed: Int,
        val dynamicPrefix: Map<String, List<String>>,
    ) : LorenzEvent() {

        init {
            dynamicPrefix.entries
                .filter { it.value.isEmpty() }
                .forEach {
                    logger.log("Dynamic prefix ${it.key} does not resolve to anything.")
                }
        }

        fun transform(since: Int, path: String, transform: (JsonElement) -> JsonElement = { it }) {
            move(since, path, path, transform)
        }

        fun move(since: Int, oldPath: String, newPath: String, transform: (JsonElement) -> JsonElement = { it }) {
            if (since <= oldVersion) {
                logger.log("Skipping move from $oldPath to $newPath ($since <= $oldVersion)")
                return
            }
            if (since > CONFIG_VERSION) {
                error("Illegally new version $since > $CONFIG_VERSION")
            }
            if (since > oldVersion + 1) {
                logger.log("Skipping move from $oldPath to $newPath (will be done in another pass)")
                return
            }
            val op = oldPath.split(".")
            val np = newPath.split(".")
            if (op.first().startsWith("#")) {
                require(np.first() == op.first())
                val realPrefixes = dynamicPrefix[op.first()]
                if (realPrefixes == null) {
                    logger.log("Could not resolve dynamic prefix $oldPath")
                    return
                }
                for (realPrefix in realPrefixes) {
                    move(
                        since,
                        "$realPrefix.${oldPath.substringAfter('.')}",
                        "$realPrefix.${newPath.substringAfter('.')}", transform
                    )
                    return
                }
            }
            val oldElem = old.at(op, false)
            if (oldElem == null) {
                logger.log("Skipping move from $oldPath to $newPath ($oldPath not present)")
                return
            }
            val newParentElement = new.at(np.dropLast(1), true)
            if (newParentElement !is JsonObject) {
                logger.log("Catastrophic: element at path $old could not be relocated to $new, since another element already inhabits that path")
                return
            }
            movesPerformed++
            if (np == listOf("#player", "personalBest")) LimboTimeTracker.workaroundMigration(oldElem.asInt)
            newParentElement.add(np.last(), transform(oldElem.shDeepCopy()))
            logger.log("Moved element from $oldPath to $newPath")
        }
    }

    private fun merge(originalObject: JsonObject, overrideObject: JsonObject): Int {
        var count = 0
        for ((key, newElement) in overrideObject.entrySet()) {
            val element = originalObject[key]
            if (element is JsonObject && newElement is JsonObject) {
                count += merge(element, newElement)
            } else {
                if (element != null) {
                    logger.log("Encountered destructive merge. Erasing $element in favour of $newElement.")
                    count++
                }
                originalObject.add(key, newElement)
            }
        }
        return count
    }

    fun fixConfig(config: JsonObject): JsonObject {
        val lastVersion = (config["lastVersion"] as? JsonPrimitive)?.asIntOrNull ?: -1
        if (lastVersion > CONFIG_VERSION) {
            logger.log("Attempted to downgrade config version")
            config.add("lastVersion", JsonPrimitive(CONFIG_VERSION))
            return config
        }
        if (lastVersion == CONFIG_VERSION) return config
        return (lastVersion until CONFIG_VERSION).fold(config) { accumulator, i ->
            logger.log("Starting config transformation from $i to ${i + 1}")
            val storage = accumulator["storage"]?.asJsonObject
            val dynamicPrefix: Map<String, List<String>> = mapOf(
                "#profile" to
                    (storage?.get("players")?.asJsonObject?.entrySet()
                        ?.flatMap { player ->
                            player.value.asJsonObject["profiles"]?.asJsonObject?.entrySet()?.map {
                                "storage.players.${player.key}.profiles.${it.key}"
                            } ?: listOf()
                        }
                        ?: listOf()),
                "#player" to
                    (storage?.get("players")?.asJsonObject?.entrySet()?.map { "storage.players.${it.key}" }
                        ?: listOf()),
            )
            val migration = ConfigFixEvent(accumulator, JsonObject().also {
                it.add("lastVersion", JsonPrimitive(i + 1))
            }, i, 0, dynamicPrefix).also { it.postAndCatch() }
            logger.log("Transformations scheduled: ${migration.new}")
            val mergesPerformed = merge(migration.old, migration.new)
            logger.log("Migration done with $mergesPerformed merges and ${migration.movesPerformed} moves performed")
            migration.old
        }.also {
            logger.log("Final config: $it")
        }
    }
}
