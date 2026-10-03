package io.github.henrique02w.ultimatetags.tag;

import java.util.List;

public record AnimationDefinition(
        boolean enabled,
        String type,
        int speed,
        List<String> frames
) {
    public static AnimationDefinition disabled() {
        return new AnimationDefinition(false, "none", 20, List.of());
    }
}
