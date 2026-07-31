package dev.ultimatetags.tag;

import org.bukkit.Material;

import java.util.List;

public record ItemDefinition(
        Material material,
        Integer customModelData,
        boolean glow,
        String name,
        List<String> lore
) {
    public static ItemDefinition empty(Material material) {
        return new ItemDefinition(material, null, false, "", List.of());
    }
}
