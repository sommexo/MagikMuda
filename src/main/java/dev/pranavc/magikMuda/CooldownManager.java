package dev.pranavc.magikMuda;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Map<String, Map<UUID, Long>> cooldowns = new HashMap<>();

    public boolean isOnCooldown(UUID playerId, String type) {
        long now = System.currentTimeMillis();
        cooldowns.putIfAbsent(type, new HashMap<>());
        return cooldowns.get(type).getOrDefault(playerId, 0L) > now;
    }

    public long getTimeLeft(UUID playerId, String type) {
        cooldowns.putIfAbsent(type, new HashMap<>());
        long left = cooldowns.get(type).getOrDefault(playerId, 0L) - System.currentTimeMillis();
        return Math.max(left / 1000, 0);
    }

    public void setCooldown(UUID playerId, String type, int seconds) {
        cooldowns.putIfAbsent(type, new HashMap<>());
        cooldowns.get(type).put(playerId, System.currentTimeMillis() + (seconds * 1000L));
    }
}
