# Neighborhood

A native port of the [Neighbor](https://github.com/sinender/Neighbor) ChatTriggers module: the
texture picker for the Neighborhood Pack in Hypixel Housing. Built for four targets from one
source tree using [Stonecutter](https://stonecutter.kikugie.dev/).

## What it does

The pack keys textures off three legacy colour codes prefixed to an item's display name, so
texture `094` rides on the item as `§0§9§4`. Applying a texture means rewriting the held
item's name and pushing it back with a creative inventory action, which is why creative mode
and a held item are required.

- `/settexture` (`/st`) with no argument opens the picker
- `/settexture <id>` applies a texture id directly
- `/settexture <text>` opens the picker with that search prefilled

The picker is a real double chest - `ContainerScreen` on Fabric, `GuiChest` on 1.8.9 - with a
category rail, search box and pagination. The ChatTriggers module had to build a client-side
chest and then reflect into its slot and container internals to find out what had been
clicked, because it could not subclass anything. Subclassing gets the vanilla background,
slot highlighting and item tooltips for free, and reduces click handling to one override that
consumes every click, so nothing is ever picked up and no window-click packet is sent.

The catalogue ships in the jar at `neighborhood/textures.txt` and is refreshed from the
upstream repo on a daemon thread at startup, so a slow or missing network never blocks launch.

## Textures

Textures live in `src/main/resources/assets/neighborhood/textures/item/`, named after the item
rather than the id - `Archeology Brush` is `archeology_brush.png`. Nothing else is needed: the
`generateItemModels` Gradle task reads the catalogue and writes the two model files Minecraft
wants for each id, so adding a texture is a PNG plus a catalogue line.

An entry with no PNG is skipped everywhere and renders as the plain item - pointing at a model
that does not exist looks worse than leaving it alone. 278 of the 324 catalogue entries
currently ship a texture; the 46 without are the armour pieces and the `Icon *` entries. The
reverse is also true: 133 of the 399 PNGs have no catalogue entry, so they cannot be reached
from the picker until `textures.txt` lists them.

How they are drawn differs by target, because 1.8.9 has no item-model component:

- **Fabric** sets `DataComponents.ITEM_MODEL` on the stack. Vanilla resolves it everywhere an
  item is drawn, so the texture shows in the picker, in inventories, in hand and in the world
  with no per-surface hook. A mixin on `ItemModelResolver.appendItemLayers` attaches the
  component to items that arrive from the server without it.
- **Forge 1.8.9** has no item-model component, so it substitutes the baked model instead. Each
  shipped PNG is stitched into the block atlas as a sprite loaded off the classpath, baked into
  a flat item model, and swapped in by a mixin on `ItemModelMesher.getItemModel` - the one
  place every render path resolves its model, so the GUI, the hand, dropped entities and item
  frames are all covered by a single hook. The substituted model keeps the original item's
  camera transforms, so a retextured sword is still held like a sword.

### Shared assets

The textures and the catalogue live with the common project, which is a plain library rather
than a mod - its resources are on the classpath but never registered as a resource pack. Both
loader branches therefore add `src/main/resources` as a resource root of their own, so the
assets end up in a pack the game actually searches. Skipping that leaves the textures present
in the jar and invisible in game, with nothing logged.

The 1.8.9 branch goes further and reads the PNGs straight off the classpath into dynamic
textures rather than through the resource manager, so a dev run and a release jar resolve them
identically no matter how the pack is assembled.

### Mixin on 1.8.9

Forge 1.8.9 ships no Mixin, and getting one running takes three pieces that are easy to miss:

- `gg.essential:mixin` on the dev runtime, not only shaded into the release jar. Without it
  Mixin is simply absent in dev and every mixin silently never applies.
- **ASM 9.** Mixin 0.8.7 needs `ClassRemapper`, which arrived in ASM 6; Forge 1.8.9 only ships
  `asm-debug-all:5.0.3`, which still has `RemappingClassAdapter`, and `gg.essential:mixin`
  bundles no ASM. Missing this fails bootstrap with `ClassNotFoundException` in dev *and* in a
  release jar.
- `NeighborhoodTweaker`, which calls `MixinBootstrap.init()` and `Mixins.addConfiguration`.
  The stock `MixinTweaker` starts the subsystem but never learns which configs to load, and
  Loom's `--mixin <config>` argument is a Fabric Loader convention that nothing on legacy Forge
  parses - hence the `Completely ignored arguments: [--mixin, ...]` line, which is harmless.

The dev run sets `-Dmixin.debug.verbose=true`, so `Mixing RenderItemMixin ... into RenderItem`
appears in the log and a mixin that fails to match is visible rather than silent.

| Target | Loader | Loom | Mappings | Java |
| --- | --- | --- | --- | --- |
| `1.8.9` | Forge 11.15.1.2318 | `gg.essential.loom` (architectury-loom fork) | MCP `mcp_stable:22-1.8.9` | 8 |
| `1.21.11` | Fabric | `net.fabricmc.fabric-loom-remap` | Mojang | 21 |
| `26.1.2` | Fabric | `net.fabricmc.fabric-loom` | none (unobfuscated) | 25 |
| `26.2` | Fabric | `net.fabricmc.fabric-loom` | none (unobfuscated) | 25 |

Minecraft has been shipped unobfuscated since 26.1, so Loom split into two plugin ids.
[`loom-back-compat`](https://codeberg.org/KikuGie/loom-back-compat) picks the right one per
node, which is why all three Fabric versions share a single build script.

## Requirements

- JDK 25 for the Gradle daemon (26.1+ will not build on anything older)
- Gradle 9.7.1 via the wrapper

JDK 8 and 21 are provisioned automatically by the foojay toolchain resolver.

## Layout

```
src/                Minecraft-FREE Kotlin, compiled at Java 8 and merged into every jar
fabric/src/         all three Fabric versions, //? conditions for the API deltas
forge/src/          1.8.9 only
versions/<v>/       per-version dependency properties, read by both loader branches
```

`src/` holds the catalogue, the texture-id encoding, the picker layout and every slot action -
all of it Minecraft-free and identical on all four targets. The `Platform` interface is the
only seam: creative check, held item name, name rewrite plus packet, chat, open picker.

Each loader branch implements that against its own Minecraft. 1.8.9 and 26.2 share no relevant
API - MCP names and NBT display names on one side, Mojang names and data components on the
other - so no attempt is made to bridge them. The Stonecutter conditions live almost entirely
in `fabric/src`, where 1.21.11 -> 26.2 genuinely does share code. Five things differ across
those three versions, all of them one-liners: the chat sink (twice over, since 26.2 moved the
HUD out of `Gui`), the client-command helper (`ClientCommandManager` -> `ClientCommands`), the
black glass pane item (`ColorCollection` in 26.2) and the slot-click parameter type
(`ClickType` -> `ContainerInput`).

Mixins are written in **Java**, not Kotlin, in both branches: the Mixin annotation processor
never sees Kotlin sources.

## Commands

Build every target into `build/libs/<mod.version>/<loader>`:

```bash
./gradlew chiseledBuild
```

Build one loader:

```bash
./gradlew chiseledBuildFabric
```

Switch the active version - this rewrites the sources in place so the IDE resolves against
that version:

```bash
./gradlew "Set active project to 1.8.9"
```

Run the active version's client:

```bash
./gradlew runActiveClientFabric
```

Before committing, put the tree back on the version control version (26.2):

```bash
./gradlew "Reset active project"
```

## Working in the IDE

Only the **active version's** node owns its branch sources. With `26.2` active, `src/` and
`fabric/src/` are attached to the 26.2 modules and `forge/src/` is attached to nothing - it
will show as unresolved until you switch. The two branches share no version number, so no
single active version can light up both; switching is how you cross the 1.8.9 <-> modern
boundary.

Switch with the Stonecutter Dev version selector (`Ctrl`+`Shift`+`S`), which re-syncs Gradle
for you. Switching from the terminal needs a manual Gradle re-sync afterwards, otherwise the
sources are rewritten but the IDE modules still point at the old version.

The project SDK is `1.8`, and the Java 8 modules (forge, and every common-branch node) rely
on inheriting it rather than having an explicit module SDK. Raising the project SDK will
break them unless you pin their module SDK first.

## Adding a version

1. Add it to `versions(...)` and the relevant `branch(...)` in `settings.gradle.kts`.
2. Create `versions/<version>/gradle.properties` with its dependency versions.
3. Build it and fix what breaks with `//? if` conditions in `fabric/src`.
