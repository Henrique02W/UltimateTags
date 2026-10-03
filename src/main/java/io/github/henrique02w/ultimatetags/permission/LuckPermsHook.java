package io.github.henrique02w.ultimatetags.permission;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

/**
 * Concede e remove permissões diretamente via API do LuckPerms, sem passar por um comando.
 *
 * <p>Isolado em uma classe própria para que nenhuma outra parte do plugin precise referenciar
 * {@code net.luckperms.api.*} diretamente: se o LuckPerms não estiver instalado, esta classe
 * simplesmente não é usada (ver {@link #available()}), e nada tenta carregar essas classes.</p>
 */
public final class LuckPermsHook {
    private final JavaPlugin plugin;

    public LuckPermsHook(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean available() {
        return Bukkit.getPluginManager().isPluginEnabled("LuckPerms")
                && Bukkit.getServicesManager().isProvidedFor(LuckPerms.class);
    }

    public CompletableFuture<Void> grant(OfflinePlayer player, String permission) {
        return withApi(api -> api.getUserManager().modifyUser(player.getUniqueId(),
                user -> user.data().add(Node.builder(permission).build())));
    }

    public CompletableFuture<Void> remove(OfflinePlayer player, String permission) {
        return withApi(api -> api.getUserManager().modifyUser(player.getUniqueId(),
                user -> user.data().remove(Node.builder(permission).build())));
    }

    private CompletableFuture<Void> withApi(java.util.function.Function<LuckPerms, CompletableFuture<Void>> action) {
        ServicesManager services = Bukkit.getServicesManager();
        LuckPerms api = services.load(LuckPerms.class);
        if (api == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("LuckPerms não está disponível."));
        }
        return action.apply(api);
    }
}
