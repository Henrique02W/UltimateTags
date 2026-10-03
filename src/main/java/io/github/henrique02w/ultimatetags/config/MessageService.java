package io.github.henrique02w.ultimatetags.config;

import io.github.henrique02w.ultimatetags.render.MiniMessageRenderer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class MessageService {
    private final JavaPlugin plugin;
    private final MiniMessageRenderer renderer;
    private YamlConfiguration messages;
    private String prefix;

    public MessageService(JavaPlugin plugin, MiniMessageRenderer renderer) {
        this.plugin = plugin;
        this.renderer = renderer;
        reload();
    }

    public void reload() {
        String locale = plugin.getConfig().getString("locale", "pt_br");
        File file = new File(plugin.getDataFolder(), "messages/" + locale + ".yml");
        this.messages = YamlConfiguration.loadConfiguration(file);
        this.prefix = plugin.getConfig().getString("messages.prefix", "<aqua>UltimateTags</aqua> ");
    }

    public Component component(String key, Map<String, Component> components, Map<String, String> strings) {
        String raw = messages.getString(key, "<red>Missing message: " + key);
        Map<String, String> merged = new HashMap<>(strings);
        merged.put("prefix", prefix);
        return renderer.render(raw, null, components, merged);
    }

    public void send(CommandSender sender, String key, Map<String, Component> components, Map<String, String> strings) {
        Component component = component(key, components, strings);
        if (sender instanceof Player player) {
            player.sendMessage(component);
        } else {
            sender.sendMessage(renderer.serialize(component));
        }
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of(), Map.of());
    }
}
