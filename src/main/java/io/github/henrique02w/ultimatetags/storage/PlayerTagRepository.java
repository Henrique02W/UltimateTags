package io.github.henrique02w.ultimatetags.storage;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerTagRepository extends AutoCloseable {
    CompletableFuture<PlayerTagData> load(UUID uuid);

    /**
     * Devolve os dados já em cache, sem tocar em disco ou banco, ou {@code null} se o
     * jogador ainda não foi carregado. Para uso em locais que não podem bloquear a thread
     * principal (ex.: PlaceholderAPI) — nunca chame {@link java.util.concurrent.CompletableFuture#join()}
     * de {@link #load(UUID)} nesses locais.
     */
    PlayerTagData peek(UUID uuid);

    CompletableFuture<Void> save(PlayerTagData data);

    CompletableFuture<Void> select(UUID uuid, String tagId);

    CompletableFuture<Void> unlock(UUID uuid, String tagId);

    void invalidate(UUID uuid);

    @Override
    void close();
}
