package dev.ultimatetags.storage;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class YamlPlayerTagRepository implements PlayerTagRepository {
    private final JavaPlugin plugin;
    private final File folder;
    private final Map<UUID, PlayerTagData> cache = new ConcurrentHashMap<>();

    public YamlPlayerTagRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "data/players");
        this.folder.mkdirs();
    }

    @Override
    public CompletableFuture<PlayerTagData> load(UUID uuid) {
        PlayerTagData cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() -> {
            File file = file(uuid);
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            PlayerTagData data = new PlayerTagData(
                    uuid,
                    yaml.getString("selected", ""),
                    new HashSet<>(yaml.getStringList("favorites")),
                    new HashSet<>(yaml.getStringList("unlocked"))
            );
            cache.put(uuid, data);
            return data;
        });
    }

    @Override
    public CompletableFuture<Void> save(PlayerTagData data) {
        cache.put(data.uuid(), data);
        return CompletableFuture.runAsync(() -> {
            try {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.set("selected", data.selectedTag());
                yaml.set("favorites", data.favorites().stream().sorted().toList());
                yaml.set("unlocked", data.unlocked().stream().sorted().toList());
                yaml.save(file(data.uuid()));
            } catch (Exception exception) {
                plugin.getLogger().warning("Could not save player tag data: " + exception.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> select(UUID uuid, String tagId) {
        return load(uuid).thenCompose(data -> {
            data.selectedTag(tagId);
            return save(data);
        });
    }

    @Override
    public CompletableFuture<Void> unlock(UUID uuid, String tagId) {
        return load(uuid).thenCompose(data -> {
            data.unlocked().add(tagId.toLowerCase());
            return save(data);
        });
    }

    @Override
    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public void close() {
        cache.values().forEach(data -> save(data).join());
        cache.clear();
    }

    private File file(UUID uuid) {
        return new File(folder, uuid + ".yml");
    }
}
