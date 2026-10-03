package io.github.henrique02w.ultimatetags.storage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Mantém o cache de {@link PlayerTagRepository} alinhado com quem está online.
 *
 * <p>Sem isto, os dados de cada jogador que já se conectou ficavam em cache para sempre
 * (nunca eram removidos), crescendo indefinidamente em um servidor de longa duração com
 * muitos jogadores distintos. Também carrega os dados no login, em vez de só na primeira
 * vez que o jogador abre a GUI ou é consultado por um placeholder.</p>
 */
public final class PlayerDataLifecycleListener implements Listener {
    private final PlayerTagRepository repository;

    public PlayerDataLifecycleListener(PlayerTagRepository repository) {
        this.repository = repository;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        repository.load(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        var data = repository.peek(uuid);
        if (data != null) {
            repository.save(data).thenRun(() -> repository.invalidate(uuid));
        } else {
            repository.invalidate(uuid);
        }
    }
}
