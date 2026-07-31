package dev.ultimatetags.tag;

public record UnlockItemDefinition(
        boolean enabled,
        ItemDefinition item,
        boolean consume,
        boolean givePermission,
        boolean autoSelect
) {
}
