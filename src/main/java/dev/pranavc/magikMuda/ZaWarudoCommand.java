package dev.pranavc.magikMuda;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class ZaWarudoCommand implements CommandExecutor {

    private final Plugin plugin;
    private final CooldownManager cooldownManager;

    public ZaWarudoCommand(Plugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("magikmuda.zawarudo")) {
            player.sendMessage("§cYou don’t have permission to use this command.");
            return true;
        }

        if (cooldownManager.isOnCooldown(player.getUniqueId(), "zawarudo")) {
            player.sendMessage("§cZa Warudo is on cooldown! (" +
                    cooldownManager.getTimeLeft(player.getUniqueId(), "zawarudo") + "s left)");
            return true;
        }

        cooldownManager.setCooldown(player.getUniqueId(), "zawarudo", 30);

        int duration = 5; // seconds
        List<Entity> frozenEntities = new ArrayList<>();
        Map<Entity, Vector> velocities = new HashMap<>();
        Map<Projectile, Vector> projectileVelocities = new HashMap<>();
        Map<Projectile, Vector> projectileLocations = new HashMap<>();

        for (Entity e : player.getWorld().getEntities()) {
            if (e.equals(player)) continue;

            if (e instanceof Projectile projectile) {
                projectileVelocities.put(projectile, projectile.getVelocity());
                projectileLocations.put(projectile, projectile.getLocation().toVector());
                projectile.setVelocity(new Vector(0, 0, 0));
                continue;
            }

            velocities.put(e, e.getVelocity());
            e.setVelocity(new Vector(0, 0, 0));

            if (e instanceof LivingEntity le) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration * 20, 255));
                le.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration * 20, 250));
                le.setAI(false);
            }

            if (e instanceof Player target && !target.equals(player)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration * 20, 250));
                target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, duration * 20, 250));
                target.sendMessage("§7Time has stopped... you cannot move!");
            }

            frozenEntities.add(e);
        }

        player.sendTitle("§6ZA WARUDO!", "§eTime is stopped!", 10, 40, 10);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);

        BukkitTask freezeProjectilesTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Projectile p : projectileVelocities.keySet()) {
                if (p.isValid() && projectileLocations.containsKey(p)) {
                    Vector loc = projectileLocations.get(p);
                    p.teleport(p.getWorld().getBlockAt(loc.toLocation(p.getWorld())).getLocation().add(0.5, 0.5, 0.5));
                    p.setVelocity(new Vector(0, 0, 0));
                }
            }
        }, 1L, 1L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            freezeProjectilesTask.cancel();

            for (Entity e : frozenEntities) {
                if (velocities.containsKey(e)) e.setVelocity(velocities.get(e));
                if (e instanceof LivingEntity le) {
                    le.removePotionEffect(PotionEffectType.SLOWNESS);
                    le.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    le.setAI(true);
                }
                if (e instanceof Player target && !target.equals(player)) {
                    target.removePotionEffect(PotionEffectType.SLOWNESS);
                    target.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    target.sendMessage("§aTime resumes, you can move again!");
                }
            }

            for (Projectile p : projectileVelocities.keySet()) {
                if (p.isValid()) {
                    p.setVelocity(projectileVelocities.get(p));
                }
            }

            player.sendTitle("§6Time resumes!", "", 10, 40, 10);
        }, duration * 20L);

        return true;
    }
}
