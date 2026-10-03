package io.github.henrique02w.ultimatetags.tag;

import java.util.List;

public record TagLoadResult(List<Tag> tags, List<String> errors) {
}
