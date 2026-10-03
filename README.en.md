# 🏷️ UltimateTags

[![Build](https://github.com/Henrique02W/UltimateTags/actions/workflows/build.yml/badge.svg)](https://github.com/Henrique02W/UltimateTags/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Henrique02W/UltimateTags?display_name=tag)](https://github.com/Henrique02W/UltimateTags/releases/latest)
![Minecraft](https://img.shields.io/badge/minecraft-26.2-brightgreen)
![Paper](https://img.shields.io/badge/paper-26.2-blue)
![Java](https://img.shields.io/badge/java-25-orange)
![Adventure](https://img.shields.io/badge/adventure-MiniMessage-9146FF)
![License](https://img.shields.io/badge/license-Non--Commercial-blue)

[🇧🇷 Português](README.md) · 🇺🇸 English

> 🏷️ A modern tag plugin for Paper/Purpur/Pufferfish, with native Adventure Components and MiniMessage.

---

## 📖 About

**UltimateTags** is a modern alternative to DeluxeTags, built on an up-to-date base: **Adventure Components**, the **official MiniMessage** parser (no flattening or rewriting of tags), modular and recursive tag files, async storage with local caching, a paginated GUI, native unlock items, and friendly integration with PlaceholderAPI, LuckPerms, TAB and nChat.

Design goals:

* Modern (Adventure/MiniMessage, no legacy `&` formatting)
* Modular (tags organised freely in files and subfolders)
* Performant (local cache, async operations)

> Default messages are in Brazilian Portuguese (`pt_br`). English messages (`en_us`) ship with the plugin; switch the `locale` setting in `config.yml`.

---

## 🛠️ Features

* 🎨 Native rendering with **Kyori Adventure** and the **official MiniMessage** parser — the original string is kept and rendered as-is
* 📁 Modular, recursive tags in `plugins/UltimateTags/tags/**/*.yml`
* ⚡ **Minimal config**: just set the tag's name/display — id, permission and unlock item are generated automatically
* 🔄 Full reload or per-file reload
* 🚨 Duplicate ID detection with a detailed console report
* 🔓 Unlock items backed by **PersistentDataContainer**, validated against tampering
* 🧩 **PlaceholderAPI** expansion (`%tagplugin_tag%`)
* 🖼️ **GUI** with pagination, per-file filtering, favourites, locked/unlocked tags, preview and quick selection
* 💾 Storage in **YAML, SQLite or MariaDB/MySQL**, with a local cache and async queries
* ✨ Basic **animation** engine: frames, rainbow and gradient-shift
* ⌨️ Admin commands with tab completion
* 🔗 **LuckPerms** integration (grant/remove permissions through the API), falling back to a console command when LuckPerms is absent
* 🏆 Integrates with **ExcellentCrates** via command rewards

---

## 🚀 Getting started

### Requirements

* Paper, Purpur or Pufferfish server for Minecraft **26.2**
* **Java 25** or newer
* (Optional) PlaceholderAPI, LuckPerms, TAB and/or nChat

> Starting with **2.0.0** the plugin only supports Minecraft 26.2 (Paper). The 1.x line, built for 1.21.8, is no longer supported.

### Upgrading from 1.x (1.21.8)

* Back up `plugins/UltimateTags/` (`config.yml`, `messages/`, `tags/`, and the `data/` folder if you use YAML or SQLite storage).
* The plugin name and the storage format (YAML, SQLite, MariaDB/MySQL) are unchanged, so player data stays compatible.
* If you use `permissions.grant-mode: LUCKPERMS`: in 1.x this setting had no effect and the plugin always ran the fallback command under the hood; as of 2.0.0 it actually uses the LuckPerms API when present. The player-facing behaviour is the same, but it's worth checking groups/permissions after upgrading.
* Update the server to Paper 26.2 on Java 25 and replace the jar with the 2.x version.

### Installation

```bash
git clone https://github.com/Henrique02W/UltimateTags.git
cd UltimateTags

# Build (requires JDK 25)
mvn package
```

The jar is written to `target/UltimateTags-<version>.jar`. Drop it into your server's `/plugins` folder.

### Configuration

**`config.yml`** — locale, storage, permissions and GUI:

```yaml
locale: "pt_br"

storage:
  type: "YAML" # YAML, SQLITE, MYSQL

permissions:
  grant-mode: "LUCKPERMS" # LUCKPERMS, COMMAND

gui:
  title: "<gradient:#48D1CC:#B654A2>Tags</gradient>"
  rows: 6
```

**Tags** live in `plugins/UltimateTags/tags/**/*.yml` (loaded recursively). Minimal format:

```yaml
tags:
  Nozes: "<gradient:#000000:#B654A2>Nozes</gradient>"
```

From this the plugin generates an internal id, a `tags.<id>` permission, and the chat/TAB/nametag/GUI/placeholder display. The old, more detailed format (`id`, `name`, `permission`, `display.chat`, `item`, `unlock-item`, animations, sounds) still works for fine-grained control.

### Usage

* `/tag` or `/tags` — open the tag GUI
* `/tag select <name>` — select a tag
* `/tag remove` — remove your active tag
* `/tag preview <name>` — preview a tag
* `/tag reload` / `/tag reload <file>` — reload everything, or just one file
* `/tag editor` — open the editor
* `/tag give <player> <name>` — grant a tag's permission to a player
* `/tag giveitem <player> <name>` — give a player that tag's unlock item
* `/tag removeperm <player> <name>` — remove a tag's permission from a player

Main placeholder: `%tagplugin_tag%`.

---

## 📚 Documentation

See [CHANGELOG.md](CHANGELOG.md) for the version history and [docs/wiki.md](docs/wiki.md) for full details on file structure, MiniMessage, permissions, storage and integrations.

## 🔒 License

This project is under the **Custom Non-Commercial Software License v1.0** (see [`LICENSE.md`](LICENSE.md); a Portuguese version is in [`LICENSE_pt.md`](LICENSE_pt.md), and the English version prevails in case of conflict).

⚠️ **Commercial use is strictly prohibited.** You may use it personally or for education, and fork and modify it. You may not sell the plugin or monetize any part of the project. For commercial use, contact the author.

## 🤝 Contributing & issues

See [`CONTRIBUTING.md`](CONTRIBUTING.md). Found a bug or have an idea? Open an [issue](https://github.com/Henrique02W/UltimateTags/issues) with your Paper and plugin versions and logs if possible.

## 📬 Contact

👤 **Henrique02W** — GitHub: <https://github.com/Henrique02W> · Discord: henrique02#7075
