package io.github.henrique02w.ultimatetags.permission;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class PermissionService {
    private final JavaPlugin plugin;
    private final LuckPermsHook luckPerms;

    public PermissionService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.luckPerms = new LuckPermsHook(plugin);
    }

    public boolean has(Player player, String permission) {
        return permission == null || permission.isBlank() || player.hasPermission(permission);
    }

    public CompletableFuture<Void> grant(OfflinePlayer player, String permission) {
        if (permission == null || permission.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        if (usesLuckPerms() && luckPerms.available()) {
            return luckPerms.grant(player, permission)
                    .exceptionallyCompose(error -> runCommand("permissions.fallback-grant-command", player, permission));
        }
        return runCommand("permissions.fallback-grant-command", player, permission);
    }

    public CompletableFuture<Void> remove(OfflinePlayer player, String permission) {
        if (permission == null || permission.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }
        if (usesLuckPerms() && luckPerms.available()) {
            return luckPerms.remove(player, permission)
                    .exceptionallyCompose(error -> runCommand("permissions.fallback-remove-command", player, permission));
        }
        return runCommand("permissions.fallback-remove-command", player, permission);
    }

    /** "LUCKPERMS" (padrão) usa a API do LuckPerms quando ele está presente; "COMMAND" sempre usa o comando fallback. */
    private boolean usesLuckPerms() {
        String mode = plugin.getConfig().getString("permissions.grant-mode", "LUCKPERMS").toUpperCase(Locale.ROOT);
        return !mode.equals("COMMAND");
    }

    private CompletableFuture<Void> runCommand(String path, OfflinePlayer player, String permission) {
        String command = plugin.getConfig().getString(path, "")
                .replace("{player}", player.getName() == null ? player.getUniqueId().toString() : player.getName())
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{permission}", permission);
        if (command.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                future.complete(null);
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }
}
