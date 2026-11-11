## Zawartość

- **`pl.niker.silentac.api.model.SilentPlayer`** - interfejs profilu gracza udostępniający:
  - `getName()` (`String`)
  - `getUuid()` / `getUniqueId()` (`UUID`)
  - `getReports()` (`Map<String, Integer>` - check, liczba zgłoszeń)
  - metody pomocnicze: `getReportCount(String)`, `hasReports(String)`, `getTotalReports()`
  - fabryki niemodyfikowalnych snapshotów: `SilentPlayer.of(name, uuid, reports)` oraz `SilentPlayerSnapshot`
- **`pl.niker.silentac.api.event.PlayerFlagEvent`** - zdarzenie Bukkit (`Cancellable`) wywoływane przy wykryciu naruszenia:
  - `getPlayer()` (`Player`)
  - `getSilentPlayer()` (`SilentPlayer`)
  - `getCheatName()` (`String`)
  - `getReportsCount()` (`int`)
  - `isCancelled()` / `setCancelled(boolean)`
- **`pl.niker.silentac.api.event.PlayerBanEvent`** - zdarzenie Bukkit wywoływane przy wykonaniu bana przez SilentAC:
  - `getPlayer()` (`Player`)
  - `getSilentPlayer()` (`SilentPlayer`)
  - `getCheatName()` (`String`)
- **`pl.niker.silentac.api.SilentACAPI` & `SilentACProvider`** - punkt dostępowy do pobierania profilu gracza z poziomu zewnętrznego pluginu.

---

## Instalacja (JitPack)

### Gradle (Groovy DSL - `build.gradle`)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.ArturekYT:SilentAC-API:main-SNAPSHOT'
}
```

### Gradle (Kotlin DSL - `build.gradle.kts`)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.ArturekYT:SilentAC-API:main-SNAPSHOT")
}
```

### Maven (`pom.xml`)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.ArturekYT</groupId>
        <artifactId>SilentAC-API</artifactId>
        <version>main-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## Examplesy

### 1. `PlayerFlagEvent`

```java

    @EventHandler
    public void onFlag(PlayerFlagEvent event) {
        Player player = event.getPlayer();
        SilentPlayer profile = event.getSilentPlayer();
        String cheat = event.getCheatName();
        int count = event.getReportsCount();

        if ("lobby".equalsIgnoreCase(player.getWorld().getName())) {
            event.setCancelled(true);
            return;
        }

        int total = profile.getTotalReports();
        player.getServer().getLogger().info(
                profile.getName() + " (" + profile.getUuid() + ") -> " + cheat
                        + " x" + count + " (łącznie: " + total + ")"
        );
    }
```

### 2. `PlayerBanEvent`

```java

    @EventHandler
    public void onBan(PlayerBanEvent event) {
        SilentPlayer profile = event.getSilentPlayer();
        event.getPlayer().getServer().getLogger().info(
                "Zbanowano gracza " + profile.getName() + " za " + event.getCheatName()
        );
    }
```

### 3. Pobieranie profilu `SilentPlayer` przez `SilentACProvider`

```java
SilentACProvider.getOptional().ifPresent(api ->
        api.getSilentPlayer(player).ifPresent(profile -> {
            int aimReports = profile.getReportCount("AimML");
            Map<String, Integer> allReports = profile.getReports();
        })
);
```

### 4. Przykład `AimML` — 3 obroty gracza + 0 obrażeń przez minutę

Pełny gotowy projekt przykładowego pluginu znajduje się w katalogu [`examples/aimml-rotate-example`](examples/aimml-rotate-example) ([`AimMLRotateListener.java`](examples/aimml-rotate-example/src/main/java/pl/niker/silentac/example/AimMLRotateListener.java)):

```java
private final Map<UUID, Long> zeroDamageUntil = new ConcurrentHashMap<>();
private static final float[][] ROTATION_STEPS = {
        {115.0f, -28.0f},
        {-120.0f, 32.0f},
        {180.0f, -12.0f}
};

@EventHandler
public void onAimMLFlag(PlayerFlagEvent event) {
    if (event.isCancelled() || !"AimML".equalsIgnoreCase(event.getCheatName())) {
        return;
    }

    Player player = event.getPlayer();
    zeroDamageUntil.put(player.getUniqueId(), System.currentTimeMillis() + 60_000L);

    for (int i = 0; i < ROTATION_STEPS.length; i++) {
        float deltaYaw = ROTATION_STEPS[i][0];
        float deltaPitch = ROTATION_STEPS[i][1];

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            Location loc = player.getLocation().clone();
            loc.setYaw(loc.getYaw() + deltaYaw);
            loc.setPitch(Math.max(-89.0f, Math.min(89.0f, loc.getPitch() + deltaPitch)));
            player.teleport(loc);
        }, i * 4L);
    }
}

@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
public void onHit(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player attacker)) return;
    Long until = zeroDamageUntil.get(attacker.getUniqueId());
    if (until != null && System.currentTimeMillis() < until) {
        event.setDamage(0.0); // nie blokuje uderzenia, zeruje tylko obrażenia
    }
}
```
