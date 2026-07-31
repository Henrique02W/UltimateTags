package dev.ultimatetags.hook;

import dev.ultimatetags.UltimateTagsPlugin;
import dev.ultimatetags.render.MiniMessageRenderer;
import dev.ultimatetags.storage.PlayerTagRepository;
import dev.ultimatetags.tag.Tag;
import dev.ultimatetags.tag.TagRegistry;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlaceholderApiExpansion extends PlaceholderExpansion {
    private final UltimateTagsPlugin plugin;
    private final TagRegistry tags;
    private final PlayerTagRepository repository;
    private final MiniMessageRenderer renderer;

    public PlaceholderApiExpansion(UltimateTagsPlugin plugin, TagRegistry tags, PlayerTagRepository repository, MiniMessageRenderer renderer) {
        this.plugin = plugin;
        this.tags = tags;
        this.repository = repository;
        this.renderer = renderer;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tagplugin";
    }

    @Override
    public @NotNull String getAuthor() {
        return "UltimateTags";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        var data = repository.load(player.getUniqueId()).join();
        Tag selected = tags.find(data.selectedTag()).orElse(null);
        if (params.equalsIgnoreCase("selected")) {
            return data.selectedTag();
        }
        if (params.equalsIgnoreCase("available")) {
            return String.valueOf(tags.all().size());
        }
        if (params.equalsIgnoreCase("unlocked")) {
            return String.valueOf(data.unlocked().size());
        }
        if (params.startsWith("has_tag_")) {
            return String.valueOf(data.unlocked().contains(params.substring("has_tag_".length()).toLowerCase()));
        }
        if (selected == null) {
            return "";
        }
        return switch (params.toLowerCase()) {
            case "tag", "tag_chat" -> selected.rawDisplay("chat");
            case "tag_raw" -> selected.rawDisplay("chat");
            case "tag_tab" -> selected.rawDisplay("tab");
            case "tag_name" -> selected.rawDisplay("nametag");
            case "file" -> selected.fileGroup();
            default -> "";
        };
    }
}
