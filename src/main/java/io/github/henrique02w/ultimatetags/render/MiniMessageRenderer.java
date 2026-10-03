package io.github.henrique02w.ultimatetags.render;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class MiniMessageRenderer {
    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<RenderKey, CachedComponent> cache = new ConcurrentHashMap<>();
    private volatile Method placeholderApiSetPlaceholders;

    public MiniMessageRenderer(Plugin plugin) {
        this.plugin = plugin;
    }

    public Component render(String input, OfflinePlayer player, Map<String, Component> components, Map<String, String> strings) {
        if (input == null || input.isBlank()) {
            return Component.empty();
        }
        String withPapi = applyPlaceholderApi(input, player);
        TagResolver.Builder resolver = TagResolver.builder();
        components.forEach((key, value) -> resolver.resolver(Placeholder.component(key, value)));
        strings.forEach((key, value) -> resolver.resolver(Placeholder.parsed(key, value)));
        return miniMessage.deserialize(withPapi, resolver.build());
    }

    public Component cached(String namespace, String input, OfflinePlayer player, Map<String, Component> components, Map<String, String> strings, Duration ttl) {
        String playerKey = player == null ? "global" : String.valueOf(player.getUniqueId());
        RenderKey key = new RenderKey(namespace, input, playerKey, strings.hashCode(), components.hashCode());
        CachedComponent cached = cache.get(key);
        long now = System.currentTimeMillis();
        if (cached != null && cached.expiresAt > now) {
            return cached.component;
        }
        Component rendered = render(input, player, components, strings);
        cache.put(key, new CachedComponent(rendered, now + ttl.toMillis()));
        return rendered;
    }

    public String serialize(Component component) {
        return miniMessage.serialize(component);
    }

    public void clear() {
        cache.clear();
    }

    private String applyPlaceholderApi(String input, OfflinePlayer player) {
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                Method method = placeholderApiSetPlaceholders;
                if (method == null) {
                    Class<?> placeholderApi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                    method = placeholderApi.getMethod("setPlaceholders", OfflinePlayer.class, String.class);
                    placeholderApiSetPlaceholders = method;
                }
                return (String) method.invoke(null, player, input);
            } catch (Throwable throwable) {
                plugin.getLogger().fine("PlaceholderAPI parse skipped: " + throwable.getMessage());
            }
        }
        return input;
    }

    private record RenderKey(String namespace, String input, String player, int stringsHash, int componentHash) {
    }

    private static final class CachedComponent {
        private final Component component;
        private final long expiresAt;

        private CachedComponent(Component component, long expiresAt) {
            this.component = Objects.requireNonNull(component);
            this.expiresAt = expiresAt;
        }
    }
}
