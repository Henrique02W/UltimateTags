# 🏷️ UltimateTags

![Licença](https://img.shields.io/badge/licença-Non--Commercial-blue)
![Status](https://img.shields.io/badge/status-ativo-success)
[![Build](https://github.com/Henrique02W/UltimateTags/actions/workflows/build.yml/badge.svg)](https://github.com/Henrique02W/UltimateTags/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Henrique02W/UltimateTags?display_name=tag)](https://github.com/Henrique02W/UltimateTags/releases/latest)
![Minecraft](https://img.shields.io/badge/minecraft-26.2-brightgreen)
![Paper](https://img.shields.io/badge/paper-26.2-blue)
![Java](https://img.shields.io/badge/java-25-orange)
![Adventure](https://img.shields.io/badge/adventure-MiniMessage-9146FF)

> 🏷️ Um plugin de tags moderno para Paper/Purpur/Pufferfish, com Adventure Components e MiniMessage nativo.

🇧🇷 Português · [🇺🇸 English](README.en.md)

---

## 📖 Sobre o Projeto

O **UltimateTags** foi desenvolvido como uma alternativa moderna ao DeluxeTags, usando uma base atualizada: **Adventure Components**, **MiniMessage oficial** (sem achatamento ou reescrita das tags), arquivos de tags modulares e recursivos, storage assíncrono com cache inteligente, GUI paginada, itens nativos de desbloqueio e integração amigável com PlaceholderAPI, LuckPerms, TAB e nChat.

O projeto foi pensado para ser:

* Moderno (Adventure/MiniMessage, sem legado de formatação `&`)
* Modular (tags organizadas livremente em arquivos e subpastas)
* Performático (cache local, operações async, debounce de atualização)

---

## 🛠️ Funcionalidades

* 🎨 Renderização nativa com **Kyori Adventure** e **MiniMessage oficial** — a string original é preservada e renderizada sem reescrita
* 📁 Tags modulares e recursivas em `plugins/UltimateTags/tags/**/*.yml`
* ⚡ **Config simplificada**: basta definir o nome/display da tag — id, permissão e item de desbloqueio são gerados automaticamente
* 🔄 Reload completo ou por arquivo individual
* 🚨 Detecção de IDs duplicados com relatório detalhado de erros no console
* 🔓 Itens de desbloqueio via **PersistentDataContainer**, com validação contra falsificação
* 🧩 Expansão de **PlaceholderAPI** (`%tagplugin_tag%`)
* 🖼️ **GUI** com paginação, filtro por arquivo, favoritos, tags bloqueadas/desbloqueadas, preview e seleção rápida
* 💾 Storage em **YAML, SQLite ou MariaDB/MySQL**, com cache local e consultas assíncronas
* ✨ Motor inicial de **animações**: frames, rainbow e gradient-shift
* ⌨️ Comandos administrativos com autocomplete
* 🔗 Integração com **LuckPerms** (concessão/remoção de permissões) e fallback via comandos quando ausente
* 🏆 Integração com **ExcellentCrates** via recompensa por comando

---

## 🧰 Tecnologias Utilizadas

* ☕ Java 25
* 📦 Maven
* ✨ Kyori Adventure + MiniMessage
* 🗺️ Paper 26.2 (ou forks compatíveis)
* 🧩 PlaceholderAPI, LuckPerms, TAB, nChat (integrações opcionais)

---

## 📂 Estrutura do Projeto

```bash
UltimateTags/
├── src/
│   └── main/
│       ├── java/dev/ultimatetags/
│       │   ├── UltimateTagsPlugin.java
│       │   ├── animation/
│       │   │   └── AnimationService.java
│       │   ├── command/
│       │   │   └── TagCommand.java
│       │   ├── config/
│       │   │   ├── MessageService.java
│       │   │   └── TagLoader.java
│       │   ├── gui/
│       │   │   ├── TagGuiHolder.java
│       │   │   ├── TagGuiListener.java
│       │   │   └── TagGuiService.java
│       │   ├── hook/
│       │   │   └── PlaceholderApiExpansion.java
│       │   ├── item/
│       │   │   ├── UnlockItemListener.java
│       │   │   └── UnlockItemService.java
│       │   ├── permission/
│       │   │   ├── LuckPermsHook.java
│       │   │   └── PermissionService.java
│       │   ├── render/
│       │   │   └── MiniMessageRenderer.java
│       │   ├── storage/
│       │   │   ├── PlayerDataLifecycleListener.java
│       │   │   ├── PlayerTagData.java
│       │   │   ├── PlayerTagRepository.java
│       │   │   ├── SqlPlayerTagRepository.java
│       │   │   └── YamlPlayerTagRepository.java
│       │   └── tag/
│       │       ├── AnimationDefinition.java
│       │       ├── ItemDefinition.java
│       │       ├── SoundDefinition.java
│       │       ├── Tag.java
│       │       ├── TagDisplay.java
│       │       ├── TagLoadResult.java
│       │       ├── TagOptions.java
│       │       ├── TagRegistry.java
│       │       └── UnlockItemDefinition.java
│       └── resources/
│           ├── config.yml
│           ├── paper-plugin.yml
│           ├── messages/
│           │   ├── en_us.yml
│           │   └── pt_br.yml
│           └── tags/
│               └── vip.yml
├── docs/
│   └── wiki.md
├── .github/
│   ├── workflows/            # CI (build) e release por tag
│   └── dependabot.yml
├── pom.xml
├── CHANGELOG.md
├── CONTRIBUTING.md
├── LICENSE.md
├── LICENSE_pt.md
└── README.md / README.en.md
```

---

## 🚀 Começando

### 📦 Requisitos

* Servidor Paper, Purpur ou Pufferfish para Minecraft 26.2
* Java 25 ou superior
* (Opcional) PlaceholderAPI, LuckPerms, TAB e/ou nChat instalados

> ℹ️ A partir da versão **2.0.0** o plugin suporta apenas o Minecraft 26.2 (Paper). A linha 1.x, feita para o 1.21.8, não recebe mais suporte.

### 🔄 Atualizando da versão 1.x (1.21.8)

* Faça backup da pasta `plugins/UltimateTags/` (`config.yml`, `messages/`, `tags/` e, se usar storage YAML ou SQLite, a pasta `data/`).
* O nome do plugin e o formato de armazenamento (YAML, SQLite, MariaDB/MySQL) não mudaram, então os dados dos jogadores continuam compatíveis.
* Se você usa `permissions.grant-mode: LUCKPERMS`: na 1.x essa opção não tinha efeito algum e o plugin sempre executava o comando fallback por baixo dos panos; a partir da 2.0.0 ele realmente usa a API do LuckPerms quando ela está presente. O comportamento visível para o jogador é o mesmo, mas vale conferir se os grupos/permissões continuam corretos após a atualização.
* Atualize o servidor para Paper 26.2 com Java 25 e troque o `.jar` pela versão 2.x.

---

### ⚙️ Instalação

```bash
# Clonar o repositório
git clone https://github.com/Henrique02W/UltimateTags.git

# Entrar na pasta
cd UltimateTags

# Compilar (requer JDK 25)
mvn package
```

O jar final fica em `target/UltimateTags-<versão>.jar`. Coloque o arquivo gerado na pasta `/plugins` do seu servidor.

---

### 🔑 Configuração

**`config.yml`** — idioma, storage, performance, permissões e GUI:

```yaml
locale: "pt_br"

storage:
  type: "YAML" # YAML, SQLITE, MYSQL

performance:
  async-load: true
  cache-expire-minutes: 30

permissions:
  grant-mode: "LUCKPERMS" # LUCKPERMS, COMMAND

gui:
  title: "<gradient:#48D1CC:#B654A2>Tags</gradient>"
  rows: 6
  sounds: true
  show-locked: true
```

**Tags** — arquivos em `plugins/UltimateTags/tags/**/*.yml` (carregamento recursivo). Formato recomendado:

```yaml
tags:
  Nozes: "<gradient:#000000:#B654A2>Nozes</gradient>"
```

Com isso o plugin gera automaticamente:

* id interno: `nozes`
* permissão: `tags.nozes`
* display de chat, TAB, nametag, GUI e PlaceholderAPI
* item de desbloqueio com o mesmo nome/display

Configs antigas com `id`, `name`, `permission`, `display.chat`, `item`, `unlock-item`, animações e sons continuam funcionando para quem precisa de controle fino.

---

### ▶️ Executando

1. Inicie o servidor com o UltimateTags instalado
2. As tags de `tags/*.yml` são carregadas automaticamente (recursivamente)
3. Use `/tag reload` para recarregar tudo, ou `/tag reload <arquivo>` para recarregar apenas um arquivo

---

## 🧠 Uso

* `/tag` ou `/tags` — abre a GUI de tags
* `/tag select <nome>` — seleciona uma tag
* `/tag remove` — remove a tag ativa
* `/tag preview <nome>` — visualiza uma tag
* `/tag reload` / `/tag reload <arquivo>` — recarrega as configurações
* `/tag editor` — abre o editor de tags
* `/tag give <player> <nome>` — concede a permissão de uma tag a um jogador
* `/tag giveitem <player> <nome>` — entrega o item de desbloqueio de uma tag
* `/tag removeperm <player> <nome>` — remove a permissão de uma tag de um jogador

### Placeholder Principal

* `%tagplugin_tag%`

### Integração com ExcellentCrates

```yaml
reward:
  type: command
  commands:
    - "tag giveitem %player_name% Nozes"
```

O jogador recebe um item real de desbloqueio. Ao usá-lo, o UltimateTags valida os dados do PDC, concede a permissão via LuckPerms (quando disponível), consome um item da stack, atualiza o cache e pode selecionar a tag automaticamente.

---

## 📚 Documentação

Veja o [CHANGELOG.md](CHANGELOG.md) para o histórico de versões e [docs/wiki.md](docs/wiki.md) para detalhes completos sobre estrutura de arquivos, MiniMessage, permissões, banco de dados e integrações, além dos exemplos em `src/main/resources`.

---

## 🔒 Licença

Este projeto está sob uma **Licença Personalizada Não Comercial**.

⚠️ **Uso comercial é estritamente proibido.**

Você pode:

* Usar para fins pessoais
* Usar para fins educacionais
* Fazer forks e modificar

Você NÃO pode:

* Vender o plugin
* Monetizar qualquer parte do projeto

📩 Para uso comercial, entre em contato com o autor.

---

## 🤝 Contribuindo

Contribuições são bem-vindas!

## 🐛 Problemas (Issues)

Encontrou um bug ou tem uma sugestão?

* Abra uma issue
* Descreva o problema claramente
* Envie logs ou prints, se possível

---

## 📬 Contato

👤 **Henrique02W**

* GitHub: https://github.com/Henrique02W
* Discord: henrique02#7075

---

## ⭐ Apoie o Projeto

Se você gostou:

* ⭐ Dê uma estrela no repositório
* 🍴 Faça um fork
* 📢 Compartilhe com outras pessoas

---

> “Tags modernas, renderização de verdade, controle total.”
