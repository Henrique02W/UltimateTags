package dev.ultimatetags.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerTagRepository extends AutoCloseable {
    CompletableFuture<PlayerTagData> load(UUID uuid);

    CompletableFuture<Void> save(PlayerTagData data);

    CompletableFuture<Void> select(UUID uuid, String tagId);

    CompletableFuture<Void> unlock(UUID uuid, String tagId);

    void invalidate(UUID uuid);

    @Override
    void close();
}
