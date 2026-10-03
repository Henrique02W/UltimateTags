package io.github.henrique02w.ultimatetags.tag;

public record TagDisplay(String chat, String tab, String nametag) {
    public String byContext(String context) {
        return switch (context.toLowerCase()) {
            case "tab" -> tab;
            case "name", "nametag" -> nametag;
            default -> chat;
        };
    }
}
