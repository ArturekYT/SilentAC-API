package pl.niker.silentac.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.niker.silentac.api.model.SilentPlayer;

import java.util.Objects;

/**
 * Called whenever SilentAC flags a player for a check violation.
 *
 * <p>Cancelling this event suppresses downstream violation handling in the
 * SilentAC core (such as alert dispatch and violation recording).</p>
 */
public class PlayerFlagEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final SilentPlayer silentPlayer;
    private final String cheatName;
    private final int reportsCount;
    private boolean cancelled;

    /**
     * Creates a synchronous {@link PlayerFlagEvent}.
     *
     * @param player       Bukkit player who triggered the check
     * @param silentPlayer SilentAC player profile snapshot
     * @param cheatName    identifier of the triggered check (e.g. {@code "AutoTrapA"})
     * @param reportsCount updated violation/report count for this check
     */
    public PlayerFlagEvent(
            @NotNull Player player,
            @NotNull SilentPlayer silentPlayer,
            @NotNull String cheatName,
            int reportsCount
    ) {
        this(player, silentPlayer, cheatName, reportsCount, false);
    }

    /**
     * Creates a {@link PlayerFlagEvent} with explicit async flag.
     *
     * @param player       Bukkit player who triggered the check
     * @param silentPlayer SilentAC player profile snapshot
     * @param cheatName    identifier of the triggered check
     * @param reportsCount updated violation/report count for this check
     * @param async        {@code true} if fired asynchronously
     */
    public PlayerFlagEvent(
            @NotNull Player player,
            @NotNull SilentPlayer silentPlayer,
            @NotNull String cheatName,
            int reportsCount,
            boolean async
    ) {
        super(async);
        this.player = Objects.requireNonNull(player, "player cannot be null");
        this.silentPlayer = Objects.requireNonNull(silentPlayer, "silentPlayer cannot be null");
        this.cheatName = Objects.requireNonNull(cheatName, "cheatName cannot be null");
        this.reportsCount = Math.max(0, reportsCount);
    }

    /**
     * Returns the Bukkit {@link Player} associated with this flag.
     *
     * @return flagged player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the SilentAC profile of the flagged player.
     *
     * @return {@link SilentPlayer} profile
     */
    @NotNull
    public SilentPlayer getSilentPlayer() {
        return silentPlayer;
    }

    /**
     * Returns the name/identifier of the cheat check that fired.
     *
     * @return non-null cheat name
     */
    @NotNull
    public String getCheatName() {
        return cheatName;
    }

    /**
     * Returns the player's violation/report count for {@link #getCheatName()}.
     *
     * @return non-negative report count
     */
    public int getReportsCount() {
        return reportsCount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Returns the Bukkit {@link HandlerList} for this event type.
     *
     * @return static handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
