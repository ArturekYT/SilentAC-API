package pl.niker.silentac.example;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Przykładowy plugin korzystający z SilentAC-API.
 */
public final class SilentACExamplePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new AimMLRotateListener(this), this);
        getLogger().info("SilentAC-Example zostal uruchomiony.");
    }
}
