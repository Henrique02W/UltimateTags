package io.github.henrique02w.ultimatetags.command;

import io.github.henrique02w.ultimatetags.UltimateTagsPlugin;
import io.github.henrique02w.ultimatetags.config.MessageService;
import io.github.henrique02w.ultimatetags.gui.TagGuiService;
import io.github.henrique02w.ultimatetags.item.UnlockItemService;
import io.github.henrique02w.ultimatetags.permission.PermissionService;
import io.github.henrique02w.ultimatetags.render.MiniMessageRenderer;
import io.github.henrique02w.ultimatetags.storage.PlayerTagRepository;
import io.github.henrique02w.ultimatetags.tag.Tag;
import io.github.henrique02w.ultimatetags.tag.TagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TagCommand implements CommandExecutor, TabCompleter {
    private final UltimateTagsPlugin plugin;
    private final TagRegistry tags;
    private final PlayerTagRepository repository;
    private final PermissionService permissions;
    private final UnlockItemService unlockItems;
    private final TagGuiService gui;
    private final MessageService messages;
    private final MiniMessageRenderer renderer;

    public TagCommand(UltimateTagsPlugin plugin, TagRegistry tags, PlayerTagRepository repository, PermissionService permissions, UnlockItemService unlockItems, TagGuiService gui, MessageService messages, MiniMessageRenderer renderer) {
        this.plugin = plugin;
        this.tags = tags;
        this.repository = repository;
        this.permissions = permissions;
        this.unlockItems = unlockItems;
        this.gui = gui;
        this.messages = messages;
        this.renderer = renderer;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "player-only");
                return true;
            }
            gui.open(player, 0, "");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "select" -> select(sender, args);
            case "remove" -> remove(sender);
            case "preview" -> preview(sender, args);
            case "reload" -> reload(sender, args);
            case "editor" -> editor(sender);
            case "give" -> give(sender, args);
            case "giveitem" -> giveItem(sender, args);
            case "removeperm" -> removePermission(sender, args);
            default -> {
                if (sender instanceof Player player) {
                    gui.open(player, 0, args[0]);
                } else {
                    messages.send(sender, "tag-not-found", Map.of(), Map.of("id", args[0]));
                }
            }
        }
        return true;
    }

    private void select(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }
        if (args.length < 2) {
            gui.open(player, 0, "");
            return;
        }
        Tag tag = tags.find(args[1]).orElse(null);
        if (tag == null) {
            messages.send(player, "tag-not-found", Map.of(), Map.of("id", args[1]));
            return;
        }
        repository.load(player.getUniqueId()).thenAccept(data -> {
            boolean unlocked = data.unlocked().contains(tag.id()) || permissions.has(player, tag.permission()) || permissions.has(player, tag.filePermission());
            if (!unlocked) {
                Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "tag-locked"));
                return;
            }
            repository.select(player.getUniqueId(), tag.id()).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () ->
                    messages.send(player, "tag-selected", Map.of("tag", renderer.render(tag.rawDisplay("chat"), player, Map.of(), Map.of())), Map.of())));
        });
    }

    private void remove(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }
        repository.select(player.getUniqueId(), "").thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "tag-removed")));
    }

    private void preview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }
        if (args.length < 2) {
            gui.open(player, 0, "");
            return;
        }
        Tag tag = tags.find(args[1]).orElse(null);
        if (tag == null) {
            messages.send(player, "tag-not-found", Map.of(), Map.of("id", args[1]));
            return;
        }
        messages.send(player, "tag-preview", Map.of("tag", renderer.render(tag.rawDisplay("chat"), player, Map.of(), Map.of())), Map.of());
    }

    private void reload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimatetags.admin")) {
            messages.send(sender, "no-permission");
            return;
        }
        messages.send(sender, "reload-start");
        long started = System.currentTimeMillis();
        var future = args.length > 1 ? plugin.reloadFile(args[1]) : plugin.reloadAll();
        future.whenComplete((ignored, error) -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (error != null) {
                messages.send(sender, "reload-error", Map.of(), Map.of("error", error.getMessage()));
            } else if (args.length > 1) {
                messages.send(sender, "reload-file-done", Map.of(), Map.of("file", args[1]));
            } else {
                messages.send(sender, "reload-done", Map.of(), Map.of("tags", String.valueOf(tags.size()), "millis", String.valueOf(System.currentTimeMillis() - started)));
            }
        }));
    }

    private void editor(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "player-only");
            return;
        }
        if (!sender.hasPermission("ultimatetags.admin")) {
            messages.send(sender, "no-permission");
            return;
        }
        gui.open(player, 0, "");
        messages.send(sender, "editor-open");
    }

    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimatetags.admin")) {
            messages.send(sender, "no-permission");
            return;
        }
        if (args.length < 3) {
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Tag tag = tags.find(args[2]).orElse(null);
        if (tag == null) {
            messages.send(sender, "tag-not-found", Map.of(), Map.of("id", args[2]));
            return;
        }
        permissions.grant(target, tag.permission()).thenCompose(ignored -> repository.unlock(target.getUniqueId(), tag.id()))
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> messages.send(sender, "permission-granted")));
    }

    private void giveItem(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimatetags.admin")) {
            messages.send(sender, "no-permission");
            return;
        }
        if (args.length < 3) {
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        Tag tag = tags.find(args[2]).orElse(null);
        if (target == null || tag == null) {
            messages.send(sender, "tag-not-found", Map.of(), Map.of("id", args[2]));
            return;
        }
        ItemStack item = unlockItems.create(tag, 1);
        target.getInventory().addItem(item).values().forEach(drop -> target.getWorld().dropItemNaturally(target.getLocation(), drop));
        messages.send(sender, "item-given", Map.of("tag", renderer.render(tag.rawDisplay("chat"), target, Map.of(), Map.of())), Map.of("player", target.getName()));
    }

    private void removePermission(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimatetags.admin")) {
            messages.send(sender, "no-permission");
            return;
        }
        if (args.length < 3) {
            return;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        Tag tag = tags.find(args[2]).orElse(null);
        if (tag == null) {
            messages.send(sender, "tag-not-found", Map.of(), Map.of("id", args[2]));
            return;
        }
        permissions.remove(target, tag.permission()).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> messages.send(sender, "permission-removed")));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("select", "remove", "preview", "reload", "editor", "give", "giveitem", "removeperm"), args[0]);
        }
        if (args.length == 2 && List.of("select", "preview").contains(args[0].toLowerCase())) {
            return filter(tags.all().stream().map(Tag::id).toList(), args[1]);
        }
        if (args.length == 3 && List.of("give", "giveitem", "removeperm").contains(args[0].toLowerCase())) {
            return filter(tags.all().stream().map(Tag::id).toList(), args[2]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reload")) {
            return filter(tags.all().stream().map(tag -> tag.sourceFile().toString().replace('\\', '/')).distinct().toList(), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String token) {
        String lower = token.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(lower)) {
                result.add(value);
            }
        }
        return result;
    }
}
