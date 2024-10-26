package at.hannibal2.skyhanni.data.jsonobjects.repo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RepoErrorJson(
    @Expose @SerializedName("changed_error_messages") val changedErrorMessages: List<RepoErrorData>,
)

data class RepoErrorData(
    @Expose @SerializedName("message_exact") private var rawMessageExact: List<String>?,
    @Expose @SerializedName("message_starts_with") private var rawMessageStartsWith: List<String>?,
    @Expose @SerializedName("replace_message") var replaceMessage: String?,
    @Expose @SerializedName("custom_message") var customMessage: String?,
    @Expose @SerializedName("affected_versions") var affectedVersions: List<String> = listOf(),
) {
    val messageExact get() = rawMessageExact.orEmpty()
    val messageStartsWith get() = rawMessageStartsWith.orEmpty()
}
