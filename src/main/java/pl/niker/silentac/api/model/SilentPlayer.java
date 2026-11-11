package pl.niker.silentac.api.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Public read-only profile representation of a player monitored by SilentAC.
 *
 * <p>Exposes basic player identity and per-check violation statistics without
 * revealing internal detection logic or mutable state.</p>
 */
public interface SilentPlayer {

    /**
     * Returns the player's last known username.
     *
     * @return non-null player username
     */
    @NotNull
    String getName();

    /**
     * Returns the player's unique identifier.
     *
     * @return non-null player {@link UUID}
     */
    @NotNull
    UUID getUuid();

    /**
     * Convenience alias for {@link #getUuid()}.
     *
     * @return non-null player {@link UUID}
     */
    @NotNull
    default UUID getUniqueId() {
        return getUuid();
    }

    /**
     * Returns an unmodifiable map of active report counts keyed by cheat name.
     *
     * <p>Map entries follow {@code Map<CHEAT_NAME, REPORT_COUNT>}.</p>
     *
     * @return non-null, unmodifiable map of cheat names to report counts
     */
    @NotNull
    Map<String, Integer> getReports();

    /**
     * Returns the report count for the given cheat name.
     *
     * @param cheatName cheat/check identifier (for example {@code "AimML"} or {@code "ThroughWalls"})
     * @return current report count for that check, or {@code 0} if absent
     */
    default int getReportCount(@NotNull String cheatName) {
        Integer count = getReports().get(cheatName);
        return count != null ? count : 0;
    }

    /**
     * Checks whether the player has at least one recorded report for the specified cheat.
     *
     * @param cheatName cheat/check identifier
     * @return {@code true} if {@link #getReportCount(String)} is greater than zero
     */
    default boolean hasReports(@NotNull String cheatName) {
        return getReportCount(cheatName) > 0;
    }

    /**
     * Returns the sum of all active reports across every check for this player.
     *
     * @return total report count
     */
    default int getTotalReports() {
        int total = 0;
        for (Integer count : getReports().values()) {
            if (count != null && count > 0) {
                total += count;
            }
        }
        return total;
    }

    /**
     * Creates an immutable {@link SilentPlayer} snapshot from raw profile data.
     *
     * @param name    player username
     * @param uuid    player UUID
     * @param reports per-cheat report statistics map (copied defensively)
     * @return immutable {@link SilentPlayer} instance
     */
    @NotNull
    static SilentPlayer of(@NotNull String name, @NotNull UUID uuid, @NotNull Map<String, Integer> reports) {
        return new SilentPlayerSnapshot(name, uuid, reports);
    }

    /**
     * Creates an immutable {@link SilentPlayer} snapshot with an empty reports map.
     *
     * @param name player username
     * @param uuid player UUID
     * @return immutable {@link SilentPlayer} instance
     */
    @NotNull
    static SilentPlayer of(@NotNull String name, @NotNull UUID uuid) {
        return new SilentPlayerSnapshot(name, uuid, Collections.emptyMap());
    }
}
