package dev.pranavc.magikMuda;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

public class StrikeCommand implements CommandExecutor {

    private final Plugin plugin;
    private final CooldownManager cooldownManager;

    public StrikeCommand(Plugin plugin, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("magikmuda.strike")) {
            player.sendMessage("§cYou don’t have permission to use this command.");
            return true;
        }

        if (cooldownManager.isOnCooldown(player.getUniqueId(), "strike")) {
            player.sendMessage("§cStrike is on cooldown! (" +
                    cooldownManager.getTimeLeft(player.getUniqueId(), "strike") + "s left)");
            return true;
        }

        cooldownManager.setCooldown(player.getUniqueId(), "strike", 30);

        RayTraceResult result = player.rayTraceBlocks(50);
        Entity targetEntity = player.getTargetEntity(50);

        if (targetEntity != null && targetEntity != player) {
            player.getWorld().strikeLightningEffect(targetEntity.getLocation());
            if (targetEntity instanceof LivingEntity le) {
                le.damage(8.0, player);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
            player.sendMessage("§bLightning strikes your target!");
        } else {
            Block lookingBlock = (result != null) ? result.getHitBlock() : player.getLocation().getBlock();

            if (lookingBlock != null && lookingBlock.getLocation().equals(player.getLocation().getBlock().getLocation())) {
                player.getWorld().strikeLightningEffect(player.getLocation());
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 200, 1));
                player.sendMessage("§aYou are empowered by the lightning!");
            } else if (lookingBlock != null) {
                Location loc = lookingBlock.getLocation().add(0.5, 1, 0.5);
                player.getWorld().strikeLightningEffect(loc);
                player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                player.sendMessage("§bLightning strikes the block!");
            }
        }

        return true;
    }
}
