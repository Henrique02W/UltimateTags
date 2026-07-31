package dev.ultimatetags.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public final class TagGuiListener implements Listener {
    private final TagGuiService gui;

    public TagGuiListener(TagGuiService gui) {
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TagGuiHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() != event.getView().getTopInventory() || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        gui.handleClick(player, event.getSlot(), holder, event.isRightClick());
    }
}
