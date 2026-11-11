package pl.niker.silentac.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.niker.silentac.api.model.SilentPlayer;

import java.util.Objects;

/**
 * Called when SilentAC executes a punishment/ban against a player for a check violation.
 */
public class PlayerBanEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final SilentPlayer silentPlayer;
    private final String cheatName;

    /**
     * Creates a synchronous {@link PlayerBanEvent}.
     *
     * @param player       Bukkit player being banned
     * @param silentPlayer SilentAC player profile snapshot
     * @param cheatName    identifier of the cheat that triggered the ban
     */
    public PlayerBanEvent(
            @NotNull Player player,
            @NotNull SilentPlayer silentPlayer,
            @NotNull String cheatName
    ) {
        this(player, silentPlayer, cheatName, false);
    }

    /**
     * Creates a {@link PlayerBanEvent} with explicit async flag.
     *
     * @param player       Bukkit player being banned
     * @param silentPlayer SilentAC player profile snapshot
     * @param cheatName    identifier of the cheat that triggered the ban
     * @param async        {@code true} if fired asynchronously
     */
    public PlayerBanEvent(
            @NotNull Player player,
            @NotNull SilentPlayer silentPlayer,
            @NotNull String cheatName,
            boolean async
    ) {
        super(async);
        this.player = Objects.requireNonNull(player, "player cannot be null");
        this.silentPlayer = Objects.requireNonNull(silentPlayer, "silentPlayer cannot be null");
        this.cheatName = Objects.requireNonNull(cheatName, "cheatName cannot be null");
    }

    /**
     * Returns the Bukkit {@link Player} being punished.
     *
     * @return punished player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the SilentAC profile of the punished player.
     *
     * @return {@link SilentPlayer} profile
     */
    @NotNull
    public SilentPlayer getSilentPlayer() {
        return silentPlayer;
    }

    /**
     * Returns the cheat identifier associated with this ban.
     *
     * @return non-null cheat name
     */
    @NotNull
    public String getCheatName() {
        return cheatName;
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
