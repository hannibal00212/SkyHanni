package at.hannibal2.skyhanni.features.misc.update

import at.hannibal2.skyhanni.utils.system.PlatformUtils
import com.google.gson.JsonPrimitive
import moe.nea.libautoupdate.GithubReleaseUpdateData
import moe.nea.libautoupdate.GithubReleaseUpdateSource
import moe.nea.libautoupdate.UpdateData

/**
 * This class is a custom implementation of the [GithubReleaseUpdateSource] that filters assets based on the mod's version.
 */
class CustomGithubReleaseUpdateSource(owner: String, repository: String) : GithubReleaseUpdateSource(owner, repository) {

    override fun findAsset(release: GithubRelease?): UpdateData? {
        release ?: return null

        return release.assets.stream()
            .filter { it.filterAsset() }
            .map { it.createReleaseData(release) }
            .findFirst().orElse(null)
    }

    private fun GithubRelease.Download.filterAsset(): Boolean {
        name ?: return false
        browserDownloadUrl ?: return false
        if (!name.endsWith(".jar")) return false
        return name.contains(PlatformUtils.MC_VERSION)
    }

    private fun GithubRelease.Download.createReleaseData(release: GithubRelease): GithubReleaseUpdateData {
        return GithubReleaseUpdateData(
            if (release.name == null) release.tagName else release.name,
            JsonPrimitive(release.tagName),
            null,
            browserDownloadUrl,
            release.body,
            release.targetCommitish,
            release.created_at,
            release.publishedAt,
            release.htmlUrl,
        )
    }
}
