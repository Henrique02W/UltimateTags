# UltimateTags

UltimateTags e um plugin moderno de tags para Paper, Purpur e Pufferfish 1.21.8+, inspirado no DeluxeTags, mas com uma base mais atual: Adventure Components, MiniMessage oficial, tags modulares por arquivo, storage async, cache inteligente, GUI, itens nativos de desbloqueio, PlaceholderAPI e integracao amigavel com LuckPerms.

## Destaques

- Renderizacao nativa com Kyori Adventure e MiniMessage oficial.
- Tags modulares e recursivas em `plugins/UltimateTags/tags/**/*.yml`.
- Config simples: o nome da tag vira permissao, item e identificador automaticamente.
- Reload completo e reload individual por arquivo.
- Deteccao de IDs duplicados e relatorio detalhado de erros.
- Itens de desbloqueio usando PersistentDataContainer, com validacao contra falsificacao simples.
- Expansao PlaceholderAPI.
- GUI com paginacao, filtro por arquivo, favoritos, tags bloqueadas/desbloqueadas, preview e selecao rapida.
- Storage YAML, SQLite e MariaDB/MySQL com cache local e consultas async.
- Motor inicial de animacoes por frames, rainbow e gradient-shift.
- Comandos administrativos com autocomplete.

## Build

Com Maven:

```bash
mvn -DskipTests package
```

Com Gradle:

```bash
gradle build
```

O jar final fica em `target/UltimateTags-1.0.0.jar` quando compilado com Maven.

## Config De Tags

O formato recomendado deixa apenas o nome/display da tag:

```yaml
tags:
  Nozes: "<gradient:#000000:#B654A2>Nozes</gradient>"
```

Com isso o plugin gera sozinho o id `nozes`, a permissao `tags.nozes`, o display de chat/TAB/nametag e o item de desbloqueio com o mesmo nome. Configs antigas com `id`, `name`, `permission`, `display.chat`, `item` e `unlock-item` continuam funcionando.

## Integracao Com ExcellentCrates

Use recompensa por comando:

```yaml
reward:
  type: command
  commands:
    - "tag giveitem %player_name% Nozes"
```

O jogador recebe um item real de desbloqueio. Ao usar o item, o UltimateTags valida os dados PDC, concede a permissao configurada pelo LuckPerms quando disponivel, consome um item da stack, atualiza o cache e pode selecionar a tag automaticamente.

## Placeholder Principal

- `%tagplugin_tag%`

## Documentacao

Veja [docs/wiki.md](docs/wiki.md) e os exemplos em `src/main/resources`.
