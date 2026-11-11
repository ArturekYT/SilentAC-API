package pl.niker.silentac.api;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pl.niker.silentac.api.event.PlayerBanEvent;
import pl.niker.silentac.api.event.PlayerFlagEvent;
import pl.niker.silentac.api.model.SilentPlayer;
import pl.niker.silentac.api.model.SilentPlayerSnapshot;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SilentACApiTest {

    @AfterEach
    void cleanupProvider() {
        SilentACProvider.unregister();
    }

    @Test
    void silentPlayerSnapshotExposesImmutableStats() {
        UUID uuid = UUID.randomUUID();
        Map<String, Integer> mutable = new HashMap<>();
        mutable.put("AimML", 3);
        mutable.put("ThroughWalls", 2);

        SilentPlayer profile = SilentPlayer.of("Niker", uuid, mutable);
        mutable.put("AutoTrapA", 99);

        assertEquals("Niker", profile.getName());
        assertEquals(uuid, profile.getUuid());
        assertEquals(uuid, profile.getUniqueId());
        assertEquals(3, profile.getReportCount("AimML"));
        assertEquals(0, profile.getReportCount("AutoTrapA"));
        assertEquals(5, profile.getTotalReports());
        assertTrue(profile.hasReports("ThroughWalls"));
        assertFalse(profile.hasReports("Unknown"));
        assertInstanceOf(SilentPlayerSnapshot.class, profile);
        assertThrows(UnsupportedOperationException.class, () -> profile.getReports().put("Test", 1));
    }

    @Test
    void playerFlagEventSupportsCancellationAndFields() {
        UUID uuid = UUID.randomUUID();
        Player mockPlayer = fakePlayer("Cheater", uuid);
        SilentPlayer silentPlayer = SilentPlayer.of("Cheater", uuid, Map.of("AimML", 4));

        PlayerFlagEvent event = new PlayerFlagEvent(mockPlayer, silentPlayer, "AimML", 4);

        assertSame(mockPlayer, event.getPlayer());
        assertSame(silentPlayer, event.getSilentPlayer());
        assertEquals("AimML", event.getCheatName());
        assertEquals(4, event.getReportsCount());
        assertFalse(event.isCancelled());
        assertSame(PlayerFlagEvent.getHandlerList(), event.getHandlers());

        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @Test
    void playerBanEventExposesExpectedFields() {
        UUID uuid = UUID.randomUUID();
        Player mockPlayer = fakePlayer("Cheater", uuid);
        SilentPlayer silentPlayer = SilentPlayer.of("Cheater", uuid, Map.of("ThroughWalls", 3));

        PlayerBanEvent event = new PlayerBanEvent(mockPlayer, silentPlayer, "ThroughWalls");

        assertSame(mockPlayer, event.getPlayer());
        assertSame(silentPlayer, event.getSilentPlayer());
        assertEquals("ThroughWalls", event.getCheatName());
        assertSame(PlayerBanEvent.getHandlerList(), event.getHandlers());
    }

    @Test
    void providerRegistersAndResolvesPlayers() {
        assertFalse(SilentACProvider.isLoaded());
        assertThrows(IllegalStateException.class, SilentACProvider::get);

        UUID uuid = UUID.randomUUID();
        SilentPlayer expected = SilentPlayer.of("Tester", uuid, Map.of("NukerB", 1));
        SilentACProvider.register(new SilentACAPI() {
            @Override
            public Optional<SilentPlayer> getSilentPlayer(UUID query) {
                return uuid.equals(query) ? Optional.of(expected) : Optional.empty();
            }

            @Override
            public String getApiVersion() {
                return "1.0.0";
            }
        });

        assertTrue(SilentACProvider.isLoaded());
        assertEquals("1.0.0", SilentACProvider.get().getApiVersion());
        assertEquals(Optional.of(expected), SilentACProvider.get().getSilentPlayer(fakePlayer("Tester", uuid)));
    }

    private static Player fakePlayer(String name, UUID uuid) {
        return (Player) Proxy.newProxyInstance(
                Player.class.getClassLoader(),
                new Class<?>[]{Player.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getName" -> name;
                    case "getUniqueId" -> uuid;
                    case "equals" -> args != null && args.length == 1 && proxy == args[0];
                    case "hashCode" -> uuid.hashCode();
                    case "toString" -> "FakePlayer(" + name + ")";
                    default -> null;
                }
        );
    }
}
