package io.github.henrique02w.ultimatetags.tag;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;

public final class TagRegistry {
    private final Map<String, Tag> tags = new ConcurrentHashMap<>();
    private final List<String> errors = new CopyOnWriteArrayList<>();

    public void replace(Collection<Tag> newTags, Collection<String> newErrors) {
        tags.clear();
        errors.clear();
        newTags.forEach(tag -> tags.put(tag.id().toLowerCase(), tag));
        errors.addAll(newErrors);
    }

    public void replaceFile(String fileName, Collection<Tag> fileTags, Collection<String> newErrors) {
        String normalized = fileName.replace('\\', '/').toLowerCase();
        tags.entrySet().removeIf(entry -> entry.getValue().sourceFile().toString().replace('\\', '/').toLowerCase().endsWith(normalized));
        fileTags.forEach(tag -> tags.put(tag.id().toLowerCase(), tag));
        errors.clear();
        errors.addAll(newErrors);
    }

    public Optional<Tag> find(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        Tag tag = tags.get(id.toLowerCase());
        return Optional.ofNullable(tag == null ? tags.get(normalizeId(id)) : tag);
    }

    public List<Tag> all() {
        return tags.values().stream()
                .sorted(Comparator.comparingInt(Tag::priority).reversed().thenComparing(Tag::id))
                .toList();
    }

    public List<Tag> byFile(String file) {
        String normalized = file.replace('\\', '/').toLowerCase();
        return all().stream().filter(tag -> tag.fileGroup().toLowerCase().equals(normalized)).toList();
    }

    public List<String> errors() {
        return new ArrayList<>(errors);
    }

    public int size() {
        return tags.size();
    }

    private String normalizeId(String value) {
        String withoutTags = value.replaceAll("<[^>]+>", "");
        String normalized = Normalizer.normalize(withoutTags, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.isBlank() ? "tag" : normalized;
    }
}
