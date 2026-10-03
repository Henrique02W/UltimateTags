# Changelog

Todas as mudanças relevantes deste projeto são documentadas aqui.
O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o projeto usa [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [2.0.0] - Não lançado

### ⚠️ Mudanças que quebram compatibilidade
- O plugin agora suporta **apenas Paper 26.2** e exige **Java 25**. A linha 1.x (Paper 1.21.8, Java 21) não recebe mais suporte.
- O pacote Java foi renomeado de `dev.ultimatetags` para `io.github.henrique02w.ultimatetags`. Isso só afeta quem dependia das classes do plugin como API; o nome do plugin e o formato de armazenamento (YAML/SQLite/MariaDB) não mudaram.

### Corrigido
- **PlaceholderAPI travando a thread principal:** `onRequest()` chamava `repository.load(uuid).join()`, bloqueando quem consultasse o placeholder (TAB, scoreboards etc. fazem isso a cada tick, geralmente na thread principal). Agora o expansion só lê o cache (`peek()`, novo método) e dispara um carregamento em segundo plano se ainda não houver dados, sem nunca bloquear.
- **Cache de jogadores sem limite:** os dados de todo jogador que já se conectou ficavam guardados para sempre (`invalidate()` existia, mas nada o chamava). Um novo `PlayerDataLifecycleListener` carrega os dados no login e salva + libera no logout.
- **`grant-mode: LUCKPERMS` nunca fazia nada:** apesar do config e do README dizerem que o LuckPerms era usado quando presente, o código sempre caía no comando fallback; a dependência da API também estava com `join-classpath: false` no `paper-plugin.yml`, o que teria impedido carregar as classes do LuckPerms em runtime mesmo se o código tentasse. Agora um `LuckPermsHook` usa a API de verdade (`UserManager#modifyUser`) para conceder/remover a permissão quando o LuckPerms está presente e `grant-mode` não é `COMMAND`, com o comando fallback como reserva se a chamada falhar.
- `item.setCustomModelData(int)`, legado, trocado por `CustomModelDataComponent` (API atual) em `TagGuiService` e `UnlockItemService`.

### Alterado
- `paper-api` atualizado para `26.2.build.121-stable`; `api-version` do `paper-plugin.yml` passou para `26.2`.
- `bstats-bukkit`, `HikariCP` e `sqlite-jdbc` atualizados para as versões mais recentes.
- Autor e site corretos no `paper-plugin.yml`.
- `README.md` reescrito (requisitos, guia de atualização a partir da 1.x, estrutura do projeto) e `README.en.md` adicionado.

### Removido
- `build.gradle.kts`, `settings.gradle.kts` e `gradle.properties`: o projeto passa a usar somente Maven.

### Infraestrutura
- GitHub Actions: build a cada push/PR e release automática ao enviar uma tag `vX.Y.Z`, com o JAR anexado.
- Dependabot para dependências Maven e Actions; `maven-enforcer-plugin` garantindo JDK 25+.
- `.gitignore` e `CONTRIBUTING.md`.

## [1.0.0]

- Versão inicial para Paper 1.21.8: tags com Adventure/MiniMessage, arquivos modulares, GUI paginada, itens de desbloqueio, animações, storage em YAML/SQLite/MariaDB e integrações com PlaceholderAPI, LuckPerms, TAB e nChat.
