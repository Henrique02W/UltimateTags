package io.github.henrique02w.ultimatetags.item;

import io.github.henrique02w.ultimatetags.config.MessageService;
import io.github.henrique02w.ultimatetags.permission.PermissionService;
import io.github.henrique02w.ultimatetags.storage.PlayerTagRepository;
import io.github.henrique02w.ultimatetags.tag.Tag;
import io.github.henrique02w.ultimatetags.tag.TagRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class UnlockItemListener implements Listener {
    private final JavaPlugin plugin;
    private final TagRegistry tags;
    private final PlayerTagRepository repository;
    private final PermissionService permissions;
    private final UnlockItemService unlockItems;
    private final MessageService messages;

    public UnlockItemListener(JavaPlugin plugin, TagRegistry tags, PlayerTagRepository repository, PermissionService permissions, UnlockItemService unlockItems, MessageService messages) {
        this.plugin = plugin;
        this.tags = tags;
        this.repository = repository;
        this.permissions = permissions;
        this.unlockItems = unlockItems;
        this.messages = messages;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) {
            return;
        }
        ItemStack item = event.getItem();
        String tagId = unlockItems.readTagId(item);
        if (tagId.isBlank()) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        Tag tag = tags.find(tagId).orElse(null);
        if (tag == null || !unlockItems.isValid(item, tag)) {
            messages.send(player, "tag-not-found", Map.of(), Map.of("id", tagId));
            return;
        }
        repository.load(player.getUniqueId()).thenCompose(data -> {
            if (data.unlocked().contains(tag.id()) || player.hasPermission(tag.permission()) || player.hasPermission(tag.filePermission())) {
                Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "already-unlocked"));
                return java.util.concurrent.CompletableFuture.completedFuture(null);
            }
            var grant = tag.unlockItem().givePermission() ? permissions.grant(player, tag.permission()) : java.util.concurrent.CompletableFuture.<Void>completedFuture(null);
            return grant.thenCompose(ignored -> repository.unlock(player.getUniqueId(), tag.id()))
                    .thenCompose(ignored -> tag.unlockItem().autoSelect() ? repository.select(player.getUniqueId(), tag.id()) : java.util.concurrent.CompletableFuture.completedFuture(null))
                    .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> finishUnlock(player, item, tag)));
        });
    }

    private void finishUnlock(Player player, ItemStack item, Tag tag) {
        if (tag.unlockItem().consume()) {
            unlockItems.consumeOne(item);
        }
        Component tagComponent = plugin instanceof io.github.henrique02w.ultimatetags.UltimateTagsPlugin ultimateTags
                ? ultimateTags.renderer().render(tag.rawDisplay("chat"), player, Map.of(), Map.of())
                : Component.text(tag.name());
        messages.send(player, "tag-unlocked", Map.of("tag", tagComponent), Map.of());
        var sound = tag.sounds().get("select");
        if (sound != null) {
            player.playSound(player.getLocation(), sound.sound(), SoundCategory.MASTER, sound.volume(), sound.pitch());
        }
    }
}
