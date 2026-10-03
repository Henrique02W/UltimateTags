package io.github.henrique02w.ultimatetags.tag;

import java.nio.file.Path;
import java.util.Map;

public record Tag(
        String id,
        String name,
        String permission,
        String filePermission,
        int priority,
        int weight,
        TagDisplay display,
        ItemDefinition guiItem,
        UnlockItemDefinition unlockItem,
        AnimationDefinition animation,
        Map<String, String> messages,
        Map<String, SoundDefinition> sounds,
        String requirementPermission,
        TagOptions options,
        Path sourceFile
) {
    public String fileGroup() {
        return sourceFile == null ? "" : sourceFile.toString().replace('\\', '/');
    }

    public String rawDisplay(String context) {
        return display.byContext(context);
    }
}
