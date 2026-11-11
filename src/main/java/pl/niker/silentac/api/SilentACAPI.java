package pl.niker.silentac.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.niker.silentac.api.model.SilentPlayer;

import java.util.Optional;
import java.util.UUID;

/**
 * Public entry point for interacting with SilentAC from external plugins.
 *
 * <p>Obtain an active instance through {@link SilentACProvider#get()}.</p>
 */
public interface SilentACAPI {

    /**
     * Looks up a player's {@link SilentPlayer} profile by their UUID.
     *
     * @param uuid player unique identifier
     * @return {@link Optional} containing the profile if tracked by SilentAC, otherwise empty
     */
    @NotNull
    Optional<SilentPlayer> getSilentPlayer(@NotNull UUID uuid);

    /**
     * Looks up a player's {@link SilentPlayer} profile for an online Bukkit {@link Player}.
     *
     * @param player online Bukkit player
     * @return {@link Optional} containing the profile if tracked by SilentAC, otherwise empty
     */
    @NotNull
    default Optional<SilentPlayer> getSilentPlayer(@NotNull Player player) {
        return getSilentPlayer(player.getUniqueId());
    }

    /**
     * Returns the SilentAC API version implemented by the loaded provider.
     *
     * @return non-null semantic version string (for example {@code "1.0.0"})
     */
    @NotNull
    String getApiVersion();
}
