package io.github.henrique02w.ultimatetags.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class TagGuiHolder implements InventoryHolder {
    private final int page;
    private final String file;

    public TagGuiHolder(int page, String file) {
        this.page = page;
        this.file = file;
    }

    public int page() {
        return page;
    }

    public String file() {
        return file;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
