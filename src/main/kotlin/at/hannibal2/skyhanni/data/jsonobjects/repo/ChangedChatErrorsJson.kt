package at.hannibal2.skyhanni.data.jsonobjects.repo

import at.hannibal2.skyhanni.utils.system.ModVersion
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ChangedChatErrorsJson(
    @Expose @SerializedName("changed_error_messages") val changedErrorMessages: List<RepoErrorData>,
)

data class RepoErrorData(
    @Expose @SerializedName("message_exact") private val rawMessageExact: List<String>?,
    @Expose @SerializedName("message_starts_with") private val rawMessageStartsWith: List<String>?,
    @Expose @SerializedName("replace_message") val replaceMessage: String?,
    @Expose @SerializedName("custom_message") val customMessage: String?,
    @Expose @SerializedName("fixed_in") val fixedIn: ModVersion?,
) {
    val messageExact get() = rawMessageExact.orEmpty()
    val messageStartsWith get() = rawMessageStartsWith.orEmpty()
}
