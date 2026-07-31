package dev.ultimatetags.tag;

import java.util.List;

public record TagLoadResult(List<Tag> tags, List<String> errors) {
}
