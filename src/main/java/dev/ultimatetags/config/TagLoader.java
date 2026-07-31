package dev.ultimatetags.config;

import dev.ultimatetags.tag.AnimationDefinition;
import dev.ultimatetags.tag.ItemDefinition;
import dev.ultimatetags.tag.SoundDefinition;
import dev.ultimatetags.tag.Tag;
import dev.ultimatetags.tag.TagDisplay;
import dev.ultimatetags.tag.TagLoadResult;
import dev.ultimatetags.tag.TagOptions;
import dev.ultimatetags.tag.UnlockItemDefinition;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.Normalizer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class TagLoader {
    private final JavaPlugin plugin;

    public TagLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public TagLoadResult loadAll() {
        List<Tag> tags = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Path root = plugin.getDataFolder().toPath().resolve("tags");
        try (Stream<Path> files = Files.walk(root)) {
            files.filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .forEach(path -> {
                        TagLoadResult result = loadAbsoluteFile(path);
                        for (Tag tag : result.tags()) {
                            if (!ids.add(tag.id().toLowerCase(Locale.ROOT))) {
                                errors.add("Duplicate tag id '" + tag.id() + "' in " + root.relativize(path));
                            } else {
                                tags.add(tag);
                            }
                        }
                        errors.addAll(result.errors());
                    });
        } catch (Exception exception) {
            errors.add("Could not scan tags folder: " + exception.getMessage());
        }
        return new TagLoadResult(tags, errors);
    }

    public TagLoadResult loadFile(Path fileName) {
        Path root = plugin.getDataFolder().toPath().resolve("tags");
        return loadAbsoluteFile(root.resolve(fileName).normalize());
    }

    private TagLoadResult loadAbsoluteFile(Path file) {
        List<Tag> tags = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        if (!Files.exists(file)) {
            return new TagLoadResult(tags, List.of("Tag file does not exist: " + file));
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        String filePermission = yaml.getString("file-permission", "");
        ConfigurationSection tagsSection = yaml.getConfigurationSection("tags");
        if (tagsSection != null) {
            for (String key : tagsSection.getKeys(false)) {
                ConfigurationSection section = tagsSection.getConfigurationSection(key);
                if (section != null) {
                    readTag(section, key, filePermission, file, tags, errors);
                } else {
                    readSimpleTag(key, tagsSection.getString(key, key), filePermission, file, tags, errors);
                }
            }
        } else {
            readTag(yaml, yaml.getString("id", stripExtension(file.getFileName().toString())), filePermission, file, tags, errors);
        }
        return new TagLoadResult(tags, errors);
    }

    private void readSimpleTag(String name, String display, String filePermission, Path file, List<Tag> tags, List<String> errors) {
        try {
            String id = normalizeId(name);
            tags.add(tag(id, name, display == null || display.isBlank() ? name : display, filePermission, file, null));
        } catch (Exception exception) {
            errors.add("Invalid tag in " + file + ": " + exception.getMessage());
        }
    }

    private void readTag(ConfigurationSection section, String fallbackId, String filePermission, Path file, List<Tag> tags, List<String> errors) {
        try {
            String name = section.getString("name", fallbackId);
            String id = normalizeId(section.getString("id", name));
            String display = section.getString("display", name);
            tags.add(tag(id, name, display, filePermission, file, section));
        } catch (Exception exception) {
            errors.add("Invalid tag in " + file + ": " + exception.getMessage());
        }
    }

    private Tag tag(String id, String name, String display, String filePermission, Path file, ConfigurationSection section) {
        return new Tag(
                id,
                name,
                section == null ? "tags." + id : section.getString("permission", "tags." + id),
                filePermission,
                section == null ? 0 : section.getInt("priority", 0),
                section == null ? 0 : section.getInt("weight", 0),
                new TagDisplay(
                        section == null ? display : section.getString("display.chat", display),
                        section == null ? display : section.getString("display.tab", section.getString("display.chat", display)),
                        section == null ? display : section.getString("display.nametag", section.getString("display.tab", display))
                ),
                item(section == null ? null : section.getConfigurationSection("item"), Material.NAME_TAG, display),
                unlock(section == null ? null : section.getConfigurationSection("unlock-item"), display),
                animation(section == null ? null : section.getConfigurationSection("animations")),
                stringMap(section == null ? null : section.getConfigurationSection("messages")),
                sounds(section == null ? null : section.getConfigurationSection("sounds")),
                section == null ? "" : section.getString("requirements.permission", ""),
                new TagOptions(
                        section != null && section.getBoolean("options.hidden", false),
                        section != null && section.getBoolean("options.default", false),
                        section == null || section.getBoolean("options.removable", true)
                ),
                plugin.getDataFolder().toPath().resolve("tags").relativize(file)
        );
    }

    private ItemDefinition item(ConfigurationSection section, Material fallback, String fallbackName) {
        if (section == null) {
            return new ItemDefinition(fallback, null, false, fallbackName, List.of());
        }
        return new ItemDefinition(
                material(section.getString("material", fallback.name()), fallback),
                section.isSet("custom-model-data") ? section.getInt("custom-model-data") : null,
                section.getBoolean("glow", false),
                section.getString("name", fallbackName),
                section.getStringList("lore")
        );
    }

    private UnlockItemDefinition unlock(ConfigurationSection section, String display) {
        if (section == null) {
            return new UnlockItemDefinition(true, new ItemDefinition(Material.PAPER, null, false, display, List.of()), true, true, false);
        }
        ConfigurationSection actions = section.getConfigurationSection("actions");
        return new UnlockItemDefinition(
                section.getBoolean("enabled", true),
                item(section, Material.PAPER, display),
                actions == null || actions.getBoolean("consume", true),
                actions == null || actions.getBoolean("give-permission", true),
                actions != null && actions.getBoolean("auto-select", false)
        );
    }

    private AnimationDefinition animation(ConfigurationSection section) {
        if (section == null || !section.getBoolean("enabled", false)) {
            return AnimationDefinition.disabled();
        }
        return new AnimationDefinition(section.getBoolean("enabled"), section.getString("type", "frames"), section.getInt("speed", 20), section.getStringList("frames"));
    }

    private Map<String, String> stringMap(ConfigurationSection section) {
        Map<String, String> map = new HashMap<>();
        if (section != null) {
            section.getKeys(false).forEach(key -> map.put(key, section.getString(key, "")));
        }
        return map;
    }

    private Map<String, SoundDefinition> sounds(ConfigurationSection section) {
        Map<String, SoundDefinition> map = new HashMap<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Sound sound = sound(section.getString(key + ".sound", "UI_BUTTON_CLICK"));
                map.put(key, new SoundDefinition(sound, (float) section.getDouble(key + ".volume", 1), (float) section.getDouble(key + ".pitch", 1)));
            }
        }
        return map;
    }

    private Material material(String value, Material fallback) {
        Material material = Material.matchMaterial(value == null ? "" : value);
        return material == null ? fallback : material;
    }

    private Sound sound(String value) {
        try {
            return Sound.valueOf(value);
        } catch (Exception ignored) {
            return Sound.UI_BUTTON_CLICK;
        }
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot == -1 ? name : name.substring(0, dot);
    }

    private String normalizeId(String value) {
        String withoutTags = value == null ? "" : value.replaceAll("<[^>]+>", "");
        String normalized = Normalizer.normalize(withoutTags, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.isBlank() ? "tag" : normalized;
    }
}
