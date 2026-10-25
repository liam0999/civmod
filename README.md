# CivMod

A single Fabric client mod for Civ-style servers: radar, nametags, snitch tracking,
macros and item tooling, behind one settings screen and one `/civmod` command.

Fabric only. Minecraft 26.3, Java 25.

## Features

- **Radar** — entity and player radar with configurable range, icons, scaling and alerts
- **Nametags** — distance-aware player nametag rendering
- **Nearby players** — on-screen list of nearby players with status reporting
- **Snitches** — snitch tracking, per-server storage, range and placement overlays
- **Macros** — hold-left, hold-right, attack and ice-road macros
- **Items** — compacted item display, repair cost, uncompacted counts
- **Chat** — clickable coordinate links

## Building

Minecraft 26.3 requires Java 25.

```sh
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```

The jar is written to `dist/`.

## Credits

CivMod consolidates work from CivModern, CombatRadar and SnitchMod.
See [CREDITS.md](CREDITS.md) for authors, sources and licences.
