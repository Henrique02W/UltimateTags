package dev.ultimatetags.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerTagData {
    private final UUID uuid;
    private String selectedTag;
    private final Set<String> favorites;
    private final Set<String> unlocked;

    public PlayerTagData(UUID uuid, String selectedTag, Set<String> favorites, Set<String> unlocked) {
        this.uuid = uuid;
        this.selectedTag = selectedTag == null ? "" : selectedTag;
        this.favorites = new HashSet<>(favorites);
        this.unlocked = new HashSet<>(unlocked);
    }

    public UUID uuid() {
        return uuid;
    }

    public String selectedTag() {
        return selectedTag;
    }

    public void selectedTag(String selectedTag) {
        this.selectedTag = selectedTag == null ? "" : selectedTag;
    }

    public Set<String> favorites() {
        return favorites;
    }

    public Set<String> unlocked() {
        return unlocked;
    }
}
