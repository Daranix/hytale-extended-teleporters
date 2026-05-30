> **Fork of:** [No Teleporters Limit](https://www.curseforge.com/hytale/mods/no-teleporters-limit) by [katomaro](https://github.com/katomaro) — Updated to work with the latest Hytale server versions.

# Extended Teleport History

Hytale server mod that removes teleporter block placement limits and adds advanced teleporter management: private/restricted teleporters, trust system, custom destinations, server teleporters, single-use portals, self-destruct, bypass mode, and more.

## Features

- **Unlimited Placement**: Place up to 9,999 teleporter blocks (vs. ~8 default)
- **Private Teleporters**: Only the owner can use them
- **Restricted Mode**: Teleporter only activates when owner (or trusted) is nearby
- **Trust System**: Add subowners per teleporter
- **Custom Destinations**: Create named destinations and link teleporters to them
- **Server Teleporters**: Admins can designate teleporters as server-wide
- **Single-Use**: Teleporter self-destructs (warp only) after one use
- **Self-Destruct Timer**: Teleporter auto-deactivates after a configurable time
- **Bypass Mode**: Admins can toggle bypass to access all teleporters
- **Hidden World / Coordinates**: Per-teleporter visibility toggles
- **Settings GUI**: In-game management UI for all teleporter options
- **LuckPerms Integration**: Permission-based feature access
- **I18n**: Fully translatable via lang files

## Installation

1. Download the latest release JAR from the [Releases](../../releases) page
2. Place the `.jar` file into your Hytale server `mods/` directory
3. Restart the server

## Building

### Requirements
- Java 25+
- Maven 3.8+
- A local Hytale server installation (`HytaleServer.jar`)

### Setup

Edit `pom.xml` if your Hytale path differs:

```xml
<properties>
    <hytale.home>D:/Games/Hytale</hytale.home>
    <hytale.patchline>release</hytale.patchline>
</properties>
```

### Build

```bash
mvn clean package
```

The shaded JAR will be at `target/final/hytale-extended-teleports.jar`.

### CI / CD

On tag push (`v*`), GitHub Actions automatically builds and publishes a release with the JAR attached. Requires the `HYTALE_SERVER_JAR_URL` secret to be configured in repository settings.

## Project Structure

```
src/main/java/com/hytale/extendedteleport/
├── Main.java                              # Plugin entry point
├── TeleporterManager.java                 # Core manager (state, persistence, warps)
├── config/
│   └── ExtendedTeleportConfig.java        # Config codec & defaults
├── commands/
│   └── TeleporterCommand.java             # /teleporter command tree (~15 subcommands)
├── data/
│   ├── CustomDestination.java             # Named destination record
│   └── TeleporterInfo.java                # Teleporter state & flags
├── gui/
│   ├── SubownerManagementGui.java
│   ├── TeleporterSelectForSubownersGui.java
│   └── TeleporterSettingsGui.java
├── i18n/
│   └── Translations.java                 # I18n loader & helpers
├── interaction/
│   ├── ExtendedTeleporterInteraction.java # Overrides default teleporter interaction
│   └── UnlimitedPlacementConditionInteraction.java
└── system/
    ├── TeleporterPlaceBlockEventSystem.java
    ├── TeleporterBreakBlockEventSystem.java
    ├── TeleporterRestrictionTickingSystem.java
    ├── TeleporterSelfDestructTickingSystem.java
    └── TeleporterComponentRemovalSystem.java

src/main/resources/
├── manifest.json                          # Plugin metadata
└── lang/                                  # Translation files

src/main/asset-pack/                       # In-game assets (UI pages, icons)
```

## Permissions (LuckPerms)

| Node | Description |
|------|-------------|
| `extendedteleporters.feature.private` | Allow creating private teleporters |
| `extendedteleporters.feature.restricted` | Allow creating restricted teleporters |

## License

Original mod by [katomaro](https://github.com/katomaro). This fork is a from-scratch reimplementation.