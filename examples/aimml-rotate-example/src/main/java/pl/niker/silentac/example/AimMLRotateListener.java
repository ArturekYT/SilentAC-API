package pl.niker.silentac.example;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import pl.niker.silentac.api.event.PlayerFlagEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener reagujący na flagę {@code AimML} z SilentAC-API:
 * <ul>
 *     <li>obraca gracza 3 razy w 3 różne kierunki (yaw i pitch),</li>
 *     <li>przez 60 sekund nie blokuje samego ciosu, lecz ustawia jego obrażenia na {@code 0.0}.</li>
 * </ul>
 */
public final class AimMLRotateListener implements Listener {

    private static final String TARGET_CHECK = "AimML";
    private static final long ROTATION_COOLDOWN_MS = 3_000L;
    private static final long ZERO_DAMAGE_DURATION_MS = 60_000L;
    private static final long STEP_INTERVAL_TICKS = 4L;

    /**
     * 3 różne przesunięcia kątowe: {deltaYaw, deltaPitch}.
     */
    private static final float[][] ROTATION_STEPS = {
            {115.0f, -28.0f},
            {-120.0f, 32.0f},
            {180.0f, -12.0f}
    };

    private final Plugin plugin;
    private final Set<UUID> activeRotations = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastTriggerTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> zeroDamageUntil = new ConcurrentHashMap<>();

    public AimMLRotateListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFlag(PlayerFlagEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!TARGET_CHECK.equalsIgnoreCase(event.getCheatName())) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Ustaw 1 minutę obrażeń równych 0 przy każdym wykryciu AimML
        zeroDamageUntil.put(uuid, now + ZERO_DAMAGE_DURATION_MS);

        Long previous = lastTriggerTime.get(uuid);
        if (previous != null && now - previous < ROTATION_COOLDOWN_MS) {
            return;
        }
        if (!activeRotations.add(uuid)) {
            return;
        }
        lastTriggerTime.put(uuid, now);

        for (int i = 0; i < ROTATION_STEPS.length; i++) {
            final float deltaYaw = ROTATION_STEPS[i][0];
            final float deltaPitch = ROTATION_STEPS[i][1];
            final boolean isLast = (i == ROTATION_STEPS.length - 1);
            final long delayTicks = i * STEP_INTERVAL_TICKS;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                try {
                    if (!player.isOnline()) {
                        return;
                    }
                    rotatePlayer(player, deltaYaw, deltaPitch);
                } finally {
                    if (isLast) {
                        activeRotations.remove(uuid);
                    }
                }
            }, delayTicks);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) {
            return;
        }

        UUID uuid = attacker.getUniqueId();
        Long until = zeroDamageUntil.get(uuid);
        if (until == null) {
            return;
        }

        if (System.currentTimeMillis() >= until) {
            zeroDamageUntil.remove(uuid);
            return;
        }

        // Nie anulujemy zdarzenia — gracz może bić, ale zadaje 0 obrażeń
        event.setDamage(0.0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        activeRotations.remove(uuid);
        lastTriggerTime.remove(uuid);
        zeroDamageUntil.remove(uuid);
    }

    private static Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }

    private static void rotatePlayer(Player player, float deltaYaw, float deltaPitch) {
        Location target = player.getLocation().clone();
        float nextYaw = normalizeYaw(target.getYaw() + deltaYaw);
        float nextPitch = clampPitch(target.getPitch() + deltaPitch);
        target.setYaw(nextYaw);
        target.setPitch(nextPitch);
        player.teleport(target);
    }

    private static float normalizeYaw(float yaw) {
        float wrapped = yaw % 360.0f;
        if (wrapped >= 180.0f) wrapped -= 360.0f;
        if (wrapped < -180.0f) wrapped += 360.0f;
        return wrapped;
    }

    private static float clampPitch(float pitch) {
        return Math.max(-89.0f, Math.min(89.0f, pitch));
    }
}
