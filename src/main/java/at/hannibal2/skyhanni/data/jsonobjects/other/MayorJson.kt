package at.hannibal2.skyhanni.data.jsonobjects.other

import com.google.gson.annotations.Expose

data class MayorJson(
    @Expose val mayor: MayorInfo,
    @Expose val current: MayorElection?,
)

data class MayorInfo(
    @Expose val key: String,
    @Expose val name: String,
    @Expose val perks: List<MayorPerk>,
    // Ministers won't exist,
    // when the current mayor is a special mayor
    @Expose val minister: Minister?,
    @Expose val election: MayorElection,
)

data class MayorElection(
    @Expose val year: Int,
    @Expose val candidates: List<MayorCandidate>,
)

data class MayorCandidate(
    @Expose val key: String,
    @Expose val name: String,
    @Expose val perks: List<MayorPerk>,
    @Expose val votes: Int,
)

data class Minister(
    @Expose val key: String,
    @Expose val name: String,
    @Expose val perk: MayorPerk,
)

data class MayorPerk(
    @Expose val name: String,
    @Expose val description: String,
    @Expose val minister: Boolean = false,
)
