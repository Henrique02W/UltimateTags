package dev.ultimatetags.animation;

import dev.ultimatetags.tag.Tag;
import dev.ultimatetags.tag.TagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimationService {
    private final JavaPlugin plugin;
    private final TagRegistry tags;
    private final Map<String, String> currentFrames = new ConcurrentHashMap<>();
    private BukkitTask task;
    private long tick;

    public AnimationService(JavaPlugin plugin, TagRegistry tags) {
        this.plugin = plugin;
        this.tags = tags;
    }

    public void start() {
        int interval = Math.max(1, plugin.getConfig().getInt("performance.animation-tick-interval", 10));
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::advance, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        currentFrames.clear();
    }

    public String frame(Tag tag, String fallback) {
        return currentFrames.getOrDefault(tag.id(), fallback);
    }

    private void advance() {
        tick++;
        for (Tag tag : tags.all()) {
            if (!tag.animation().enabled() || tag.animation().frames().isEmpty()) {
                continue;
            }
            int speed = Math.max(1, tag.animation().speed());
            int index = (int) ((tick / speed) % tag.animation().frames().size());
            currentFrames.put(tag.id(), tag.animation().frames().get(index));
        }
    }
}
