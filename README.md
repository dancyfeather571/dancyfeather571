# Zni's Mod Studio

**Mod ID:** `znis_mod_studio` · **Version:** 1.0.0 · **Minecraft Java 26.2** · **Fabric** (Loader 0.19.5+, Fabric API 0.161.0+26.2, Java 25)

Zni's Mod Studio is the home and shared UI library for ZniStudios Minecraft mods. It will
eventually list every installed ZniStudios mod (icon, name, version, description, status,
settings button) and give all ZniStudios mods the same dark-and-gold components.

## What it does now

- **Mod Studio home screen**: branding header (face + title), intro panel, and an
  *Installed Mods* list of every registered ZniStudios mod (or an honest empty state).
- **Opening it**: the Zni face button in the pause menu's icon row **and** on the title
  screen's icon row. You can also bind a key under
  *Options > Controls > Miscellaneous > Open Zni's Mod Studio* (unbound by default).
- **Automatic mod registration** through `fabric.mod.json` (see below).
- **Design system**: centralized theme colors plus reusable `ZniButton` / `ZniIconButton`
  with a gold accent on mouse hover **and** keyboard/controller focus.
- **Responsive layout**: everything is computed from the scaled window size, so it adapts to
  any GUI scale.

## Branding assets

The source skin is kept at `art/zni_skin_source.png` (not shipped in the jar). Every branding
texture is derived from it by `art/BakeBranding.java`, so nothing is cropped or stretched at
runtime. Re-run the tool after replacing the skin:

```
java art/BakeBranding.java
```

| Derived file (`assets/znis_mod_studio/...`) | Size | Used for |
|---|---|---|
| `textures/gui/zni_face_8.png` / `_16` / `_32` | 8, 16, 32 px | Head (face + hair layer): pause-menu button (16), Mod Studio header (32 or 16) |
| `textures/gui/zni_character.png` / `_2x` | 16x32, 32x64 | Full front-view character: Mod Studio header on tall screens |
| `icon.png` | 128x128 | Mod list icon (face) |

The bake step composites the overlay layers onto their base layers and only uses whole-number
nearest-neighbour enlargement, so transparency and pixel edges are kept exactly. In game the
textures are drawn 1:1 (or at whole-number multiples). Minecraft's default GUI texture sampling
is nearest-neighbour and GUI scales are always whole numbers, so the art stays sharp at every scale.

## Project structure

```
art/                                source skin + BakeBranding.java (asset derivation tool)
src/main/java/com/znistudios/modstudio/
  ZnisModStudio.java                  common entrypoint, MOD_ID, id() helper
  api/ZniModRegistry.java             discovers registered mods from fabric.mod.json
  api/ZniRegisteredMod.java           one registered mod's metadata
src/client/java/com/znistudios/modstudio/client/
  ZnisModStudioClient.java            client entrypoint: key mapping
  api/ZniSettingsScreens.java         optional per-mod settings screen hook
  entry/StudioEntryPoints.java        shared face button for pause menu + title screen
  mixin/PauseScreenMixin.java         adds the icon button to the pause menu icon row
  screen/ModStudioScreen.java         the home screen
  screen/InstalledModsSection.java    scrollable Installed Mods list / empty state
  screen/ModCard.java                 one registered mod (icon, name, version, authors, description)
  ui/theme/ZniTheme.java              ALL ZniStudios colors and spacing
  ui/render/ZniDraw.java              panels, outlines, gold divider, text wrapping
  ui/render/ZniBranding.java          face/character drawing (baked textures, monogram fallback)
  ui/widget/ZniButton.java            standard gold-hover/focus button
  ui/widget/ZniIconButton.java        square icon variant
```

## Registering a ZniStudios mod

A mod appears in Zni's Mod Studio by adding this to its own `fabric.mod.json`. Nothing else is
needed:

```json
"custom": {
  "zni_mod_studio": {
    "registered": true
  }
}
```

The Studio discovers it through `FabricLoader.getInstance().getAllMods()` and shows the mod's
existing **name, version, description, authors and icon**. Mods without the block, or with a
malformed one, are ignored (a warning is logged) and never crash the game. Icons following the
usual `assets/<modid>/icon.png` convention are shown; otherwise a lettered tile is used.

Other code can read the same list with `ZniModRegistry.getMods()`.

### Settings pages (extension point)

A registered mod can optionally give the Studio its own settings screen from its client entrypoint:

```java
ZniSettingsScreens.register("my_mod", parent -> new MySettingsScreen(parent));
```

Its card then shows a gold *Settings* tag and opens that screen when pressed. There is no config
framework here on purpose: each mod builds its own screen.

## Planned

- **Shared library**: other ZniStudios mods depend on this mod and reuse `ZniTheme`,
  `ZniButton`, panels and headings, so every mod shares one look.

## Building

`./gradlew build` → `build/libs/znis-mod-studio-1.0.0.jar`
