package pl.niker.silentac.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Static service locator holding the active {@link SilentACAPI} implementation
 * registered by the SilentAC core plugin.
 */
public final class SilentACProvider {

    private static volatile SilentACAPI instance;

    private SilentACProvider() {
    }

    /**
     * Returns the registered {@link SilentACAPI} instance.
     *
     * @return active {@link SilentACAPI}
     * @throws IllegalStateException if SilentAC has not registered an API implementation yet
     */
    @NotNull
    public static SilentACAPI get() {
        SilentACAPI current = instance;
        if (current == null) {
            throw new IllegalStateException("SilentAC API is not loaded. Make sure SilentAC is enabled and listed in depend/softdepend.");
        }
        return current;
    }

    /**
     * Returns the active {@link SilentACAPI} instance if available.
     *
     * @return {@link Optional} containing the active API, or empty if unloaded
     */
    @NotNull
    public static Optional<SilentACAPI> getOptional() {
        return Optional.ofNullable(instance);
    }

    /**
     * Checks whether a {@link SilentACAPI} implementation is currently registered.
     *
     * @return {@code true} if {@link #get()} will succeed
     */
    public static boolean isLoaded() {
        return instance != null;
    }

    /**
     * Registers the active {@link SilentACAPI} implementation.
     *
     * <p>Intended to be invoked exclusively by the SilentAC core plugin during startup.</p>
     *
     * @param api non-null implementation instance
     */
    public static void register(@NotNull SilentACAPI api) {
        instance = Objects.requireNonNull(api, "api cannot be null");
    }

    /**
     * Unregisters the active {@link SilentACAPI} implementation during plugin shutdown.
     */
    public static void unregister() {
        instance = null;
    }
}
