package dev.pranavc.magikMuda;

import org.bukkit.plugin.java.JavaPlugin;

public class MagikMuda extends JavaPlugin {

    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        cooldownManager = new CooldownManager();

        // Register Minecraft commands
        getCommand("zawarudo").setExecutor(new ZaWarudoCommand(this, cooldownManager));
        getCommand("strike").setExecutor(new StrikeCommand(this, cooldownManager));

        getLogger().info("MagikMuda enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MagikMuda disabled!");
    }
}
