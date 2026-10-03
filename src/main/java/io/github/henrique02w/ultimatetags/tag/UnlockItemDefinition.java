package io.github.henrique02w.ultimatetags.tag;

public record UnlockItemDefinition(
        boolean enabled,
        ItemDefinition item,
        boolean consume,
        boolean givePermission,
        boolean autoSelect
) {
}
