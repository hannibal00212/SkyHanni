package at.hannibal2.skyhanni.config;

/**
 * The interface HasLegacyId.
 * To be used for config elements that are being migrated from ArrayLists to Enums.
 * A legacyId is not needed for new elements.
 */
public interface HasLegacyId {

    /**
     * Gets display string.
     *
     * @return the display string
     */
    String toString();

    /**
     * Gets legacy id. This is used for legacy configs that are being migrated to enums.
     * New elements do not need a legacyId, and should return -1
     *
     * @return the legacy id
     */
    default int getLegacyId() {
        return -1;
    }
}
