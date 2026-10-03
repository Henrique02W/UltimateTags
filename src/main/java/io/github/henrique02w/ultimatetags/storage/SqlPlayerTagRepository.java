package io.github.henrique02w.ultimatetags.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class SqlPlayerTagRepository implements PlayerTagRepository {
    private final JavaPlugin plugin;
    private final HikariDataSource dataSource;
    private final Map<UUID, PlayerTagData> cache = new ConcurrentHashMap<>();

    public SqlPlayerTagRepository(JavaPlugin plugin, String type) throws Exception {
        this.plugin = plugin;
        HikariConfig config = new HikariConfig();
        if (type.equals("SQLITE")) {
            File file = new File(plugin.getDataFolder(), "data/ultimatetags.db");
            file.getParentFile().mkdirs();
            config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
            config.setMaximumPoolSize(1);
        } else {
            config.setJdbcUrl(plugin.getConfig().getString("storage.mysql.jdbc-url"));
            config.setUsername(plugin.getConfig().getString("storage.mysql.username"));
            config.setPassword(plugin.getConfig().getString("storage.mysql.password"));
            config.setMaximumPoolSize(plugin.getConfig().getInt("storage.mysql.pool-size", 6));
        }
        this.dataSource = new HikariDataSource(config);
        migrate();
    }

    @Override
    public CompletableFuture<PlayerTagData> load(UUID uuid) {
        PlayerTagData cached = cache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT selected,favorites,unlocked FROM ultimatetags_players WHERE uuid=?")) {
                statement.setString(1, uuid.toString());
                var rs = statement.executeQuery();
                if (rs.next()) {
                    PlayerTagData data = new PlayerTagData(uuid, rs.getString("selected"), split(rs.getString("favorites")), split(rs.getString("unlocked")));
                    cache.put(uuid, data);
                    return data;
                }
            } catch (Exception exception) {
                plugin.getLogger().warning("Could not load SQL tag data: " + exception.getMessage());
            }
            PlayerTagData data = new PlayerTagData(uuid, "", Set.of(), Set.of());
            cache.put(uuid, data);
            return data;
        });
    }

    @Override
    public CompletableFuture<Void> save(PlayerTagData data) {
        cache.put(data.uuid(), data);
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("REPLACE INTO ultimatetags_players(uuid,selected,favorites,unlocked) VALUES(?,?,?,?)")) {
                statement.setString(1, data.uuid().toString());
                statement.setString(2, data.selectedTag());
                statement.setString(3, String.join(",", data.favorites()));
                statement.setString(4, String.join(",", data.unlocked()));
                statement.executeUpdate();
            } catch (Exception exception) {
                plugin.getLogger().warning("Could not save SQL tag data: " + exception.getMessage());
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
    public PlayerTagData peek(UUID uuid) {
        return cache.get(uuid);
    }

    @Override
    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public void close() {
        cache.values().forEach(data -> save(data).join());
        dataSource.close();
    }

    private void migrate() throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ultimatetags_players(uuid VARCHAR(36) PRIMARY KEY, selected VARCHAR(128), favorites TEXT, unlocked TEXT)")) {
            statement.executeUpdate();
        }
    }

    private Set<String> split(String value) {
        if (value == null || value.isBlank()) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(value.split(",")));
    }
}
