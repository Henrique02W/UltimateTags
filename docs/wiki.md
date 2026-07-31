# Wiki Do UltimateTags

## Estrutura Do Projeto

```text
plugins/UltimateTags/
  config.yml
  messages/
    pt_br.yml
    en_us.yml
  tags/
    vip.yml
    eventos.yml
    especiais/
      halloween.yml
      natal.yml
      pascoa.yml
```

## Arquivos De Tags

As tags ficam em arquivos separados dentro da pasta `tags/`. O carregamento e recursivo, entao subpastas funcionam normalmente.

Um arquivo pode conter uma tag na raiz ou varias tags dentro de `tags:`. No formato simples, a chave e o nome da tag, e o valor e o display MiniMessage usado em todos os lugares.

Exemplo:

```yaml
tags:
  Nozes: "<gradient:#000000:#B654A2>Nozes</gradient>"
```

O plugin gera sozinho:

- id interno: `nozes`
- permissao: `tags.nozes`
- display de chat, TAB, nametag, GUI e PlaceholderAPI
- item de desbloqueio com o mesmo nome/display

Durante o reload, IDs duplicados gerados pelo nome sao rejeitados e reportados no console. Configs antigas com `id`, `name`, `permission`, `display.chat`, `item`, `unlock-item`, animacoes e sons continuam funcionando quando voce precisar de controle fino.

## MiniMessage

O UltimateTags guarda a string MiniMessage original e usa a implementacao oficial do Kyori `MiniMessage` para renderizar componentes. O plugin nao achata, reordena, simplifica ou reescreve tags antes do parser.

O formato abaixo e suportado em chat, TAB, nametag, placeholders, GUI, preview, itens e mensagens:

```text
<gradient:#000000:#B654A2><shadow:#00D9FF:1>N</shadow><shadow:#59A589:1>o</shadow><shadow:#B27112:1>z</shadow><shadow:#89B786:1>e</shadow><shadow:#5FFDFA:1>s</shadow></gradient>
```

## Permissoes

A permissao individual e derivada do nome da tag. Exemplo: `Nozes` vira `tags.nozes`; `Tag Especial` vira `tags.tag_especial`.

Quando instalado, o LuckPerms e usado para conceder ou remover permissoes permanentes. Sem LuckPerms, o plugin executa os comandos fallback definidos em `config.yml`.

## Comandos

- `/tag`
- `/tags`
- `/tag select <nome>`
- `/tag remove`
- `/tag preview <nome>`
- `/tag reload`
- `/tag reload <arquivo>`
- `/tag editor`
- `/tag give <player> <nome>`
- `/tag giveitem <player> <nome>`
- `/tag removeperm <player> <nome>`

## Banco De Dados

Modos disponiveis:

- `YAML`
- `SQLITE`
- `MYSQL`

Os dados do jogador sao mantidos em cache local e gravados de forma async.

## TAB, nChat E PlaceholderAPI

TAB e nChat podem consumir as tags pelo PlaceholderAPI. Isso evita recalculo constante de scoreboard/tablist e deixa a integracao leve para servidores grandes.

## ExcellentCrates

Use:

```yaml
reward:
  type: command
  commands:
    - "tag giveitem %player_name% Nozes"
```

O item entregue e identificado por PDC e desbloqueia a tag ao ser usado.
