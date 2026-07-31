package dev.ultimatetags.item;

import dev.ultimatetags.render.MiniMessageRenderer;
import dev.ultimatetags.tag.ItemDefinition;
import dev.ultimatetags.tag.Tag;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class UnlockItemService {
    private final JavaPlugin plugin;
    private final MiniMessageRenderer renderer;
    private final NamespacedKey tagIdKey;
    private final NamespacedKey signatureKey;

    public UnlockItemService(JavaPlugin plugin, MiniMessageRenderer renderer) {
        this.plugin = plugin;
        this.renderer = renderer;
        this.tagIdKey = new NamespacedKey(plugin, "unlock_tag_id");
        this.signatureKey = new NamespacedKey(plugin, "unlock_signature");
    }

    public ItemStack create(Tag tag, int amount) {
        ItemDefinition definition = tag.unlockItem().item();
        ItemStack item = new ItemStack(definition.material(), Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (definition.customModelData() != null) {
            meta.setCustomModelData(definition.customModelData());
        }
        if (definition.name() != null && !definition.name().isBlank()) {
            meta.displayName(renderer.render(definition.name(), null, Map.of("tag", renderer.render(tag.rawDisplay("chat"), null, Map.of(), Map.of())), Map.of()));
        }
        meta.lore(definition.lore().stream()
                .map(line -> renderer.render(line, null, Map.of("tag", renderer.render(tag.rawDisplay("chat"), null, Map.of(), Map.of())), Map.of()))
                .toList());
        if (definition.glow()) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        meta.getPersistentDataContainer().set(tagIdKey, PersistentDataType.STRING, tag.id());
        meta.getPersistentDataContainer().set(signatureKey, PersistentDataType.STRING, signature(tag));
        item.setItemMeta(meta);
        return item;
    }

    public String readTagId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return "";
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().getOrDefault(tagIdKey, PersistentDataType.STRING, "");
    }

    public boolean isValid(ItemStack item, Tag tag) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(tagIdKey, PersistentDataType.STRING);
        String signature = meta.getPersistentDataContainer().get(signatureKey, PersistentDataType.STRING);
        return tag.id().equalsIgnoreCase(id) && signature(tag).equals(signature);
    }

    public void consumeOne(ItemStack item) {
        if (item.getAmount() <= 1) {
            item.setAmount(0);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
    }

    private String signature(Tag tag) {
        return Integer.toHexString((tag.id() + "|" + tag.permission() + "|" + tag.unlockItem().item().customModelData()).hashCode());
    }
}
