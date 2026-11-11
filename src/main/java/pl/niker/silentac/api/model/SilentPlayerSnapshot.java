package pl.niker.silentac.api.model;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable value model implementing {@link SilentPlayer}.
 *
 * <p>Safe to share across threads, store in caches, or pass into Bukkit events.</p>
 */
public final class SilentPlayerSnapshot implements SilentPlayer {

    private final String name;
    private final UUID uuid;
    private final Map<String, Integer> reports;

    /**
     * Constructs an immutable player profile snapshot.
     *
     * @param name    player username
     * @param uuid    player unique identifier
     * @param reports report counts keyed by cheat name
     */
    public SilentPlayerSnapshot(@NotNull String name, @NotNull UUID uuid, @NotNull Map<String, Integer> reports) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.uuid = Objects.requireNonNull(uuid, "uuid cannot be null");
        Objects.requireNonNull(reports, "reports cannot be null");

        Map<String, Integer> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : reports.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                copy.put(entry.getKey(), entry.getValue());
            }
        }
        this.reports = Collections.unmodifiableMap(copy);
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @Override
    @NotNull
    public Map<String, Integer> getReports() {
        return reports;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SilentPlayerSnapshot that)) return false;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && Objects.equals(reports, that.reports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid, reports);
    }

    @Override
    public String toString() {
        return "SilentPlayerSnapshot{"
                + "name='" + name + '\''
                + ", uuid=" + uuid
                + ", reports=" + reports
                + '}';
    }
}
