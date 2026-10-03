package io.github.henrique02w.ultimatetags.gui;

import io.github.henrique02w.ultimatetags.config.MessageService;
import io.github.henrique02w.ultimatetags.render.MiniMessageRenderer;
import io.github.henrique02w.ultimatetags.storage.PlayerTagRepository;
import io.github.henrique02w.ultimatetags.tag.Tag;
import io.github.henrique02w.ultimatetags.tag.TagRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TagGuiService {
    private final JavaPlugin plugin;
    private final MiniMessageRenderer renderer;
    private final MessageService messages;
    private final TagRegistry tags;
    private final PlayerTagRepository repository;
    private final Map<Integer, Tag> slotCache = new HashMap<>();

    public TagGuiService(JavaPlugin plugin, MiniMessageRenderer renderer, MessageService messages, TagRegistry tags, PlayerTagRepository repository) {
        this.plugin = plugin;
        this.renderer = renderer;
        this.messages = messages;
        this.tags = tags;
        this.repository = repository;
    }

    public void open(Player player, int page, String file) {
        repository.load(player.getUniqueId()).thenAccept(data -> Bukkit.getScheduler().runTask(plugin, () -> {
            String title = plugin.getConfig().getString("gui.title", "<aqua>Tags</aqua>");
            int rows = Math.max(3, Math.min(6, plugin.getConfig().getInt("gui.rows", 6)));
            Inventory inventory = Bukkit.createInventory(new TagGuiHolder(page, file), rows * 9, renderer.render(title, player, Map.of(), Map.of()));
            List<Tag> visible = tags.all().stream()
                    .filter(tag -> file == null || file.isBlank() || tag.fileGroup().equalsIgnoreCase(file))
                    .filter(tag -> !tag.options().hidden() || player.hasPermission("ultimatetags.admin"))
                    .sorted(Comparator.comparingInt(Tag::priority).reversed())
                    .toList();
            int perPage = (rows - 1) * 9;
            int start = page * perPage;
            slotCache.clear();
            for (int slot = 0; slot < perPage && start + slot < visible.size(); slot++) {
                Tag tag = visible.get(start + slot);
                boolean unlocked = data.unlocked().contains(tag.id()) || player.hasPermission(tag.permission()) || player.hasPermission(tag.filePermission());
                inventory.setItem(slot, icon(player, tag, unlocked, data.favorites().contains(tag.id())));
                slotCache.put(slot, tag);
            }
            inventory.setItem(rows * 9 - 9, control(Material.ARROW, "<yellow>Anterior"));
            inventory.setItem(rows * 9 - 5, control(Material.COMPASS, "<aqua>Arquivos"));
            inventory.setItem(rows * 9 - 1, control(Material.ARROW, "<yellow>Próxima"));
            player.openInventory(inventory);
        }));
    }

    public void handleClick(Player player, int slot, TagGuiHolder holder, boolean rightClick) {
        Tag tag = slotCache.get(slot);
        if (tag == null) {
            if (slot == player.getOpenInventory().getTopInventory().getSize() - 9 && holder.page() > 0) {
                open(player, holder.page() - 1, holder.file());
            } else if (slot == player.getOpenInventory().getTopInventory().getSize() - 1) {
                open(player, holder.page() + 1, holder.file());
            }
            return;
        }
        repository.load(player.getUniqueId()).thenAccept(data -> {
            if (rightClick) {
                if (data.favorites().contains(tag.id())) {
                    data.favorites().remove(tag.id());
                } else {
                    data.favorites().add(tag.id());
                }
                repository.save(data).thenRun(() -> open(player, holder.page(), holder.file()));
                return;
            }
            boolean unlocked = data.unlocked().contains(tag.id()) || player.hasPermission(tag.permission()) || player.hasPermission(tag.filePermission());
            if (!unlocked) {
                Bukkit.getScheduler().runTask(plugin, () -> messages.send(player, "tag-locked"));
                return;
            }
            repository.select(player.getUniqueId(), tag.id()).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                messages.send(player, "tag-selected", Map.of("tag", renderer.render(tag.rawDisplay("chat"), player, Map.of(), Map.of())), Map.of());
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1.2f);
                player.closeInventory();
            }));
        });
    }

    public void clear() {
        slotCache.clear();
    }

    private ItemStack icon(Player player, Tag tag, boolean unlocked, boolean favorite) {
        var definition = unlocked ? tag.guiItem() : tag.guiItem();
        ItemStack item = new ItemStack(unlocked ? definition.material() : Material.matchMaterial(plugin.getConfig().getString("gui.locked-material", "BARRIER")));
        ItemMeta meta = item.getItemMeta();
        if (definition.customModelData() != null && unlocked) {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            component.setFloats(List.of((float) definition.customModelData()));
            meta.setCustomModelDataComponent(component);
        }
        meta.displayName(renderer.render((favorite ? "<yellow>★</yellow> " : "") + tag.rawDisplay("chat"), player, Map.of(), Map.of()));
        meta.lore(List.of(
                renderer.render(unlocked ? "<green>Desbloqueada" : "<red>Bloqueada", player, Map.of(), Map.of()),
                renderer.render("<gray>Arquivo: <white>" + tag.fileGroup(), player, Map.of(), Map.of()),
                renderer.render("<dark_gray>Botão direito: favorito", player, Map.of(), Map.of())
        ));
        if (definition.glow() || favorite) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack control(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(renderer.render(name, null, Map.of(), Map.of()));
        item.setItemMeta(meta);
        return item;
    }
}
