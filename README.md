# Zni's Mod Studio

**Mod ID:** `znis_mod_studio` · **Version:** 1.0.0 · **Minecraft Java 26.2** · **Fabric** (Loader 0.19.5+, Fabric API 0.161.0+26.2, Java 25)

Zni's Mod Studio is the home and shared UI library for ZniStudios Minecraft mods. It will
eventually list every installed ZniStudios mod (icon, name, version, description, status,
settings button) and give all ZniStudios mods the same dark-and-gold components.

## Step 1: what it does now

- **Mod Studio home screen**: branding header (face + title), intro panel, and an
  *Installed Mods* section that shows an honest empty state (no mods are faked).
- **Opening it**: a small face icon button in the pause menu's icon row. You can also bind
  a key under *Options > Controls > Miscellaneous > Open Zni's Mod Studio* (unbound by default).
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
src/client/java/com/znistudios/modstudio/client/
  ZnisModStudioClient.java            client entrypoint: key mapping, pause-menu entry, openModStudio()
  mixin/PauseScreenMixin.java         adds the icon button to the pause menu icon row
  screen/ModStudioScreen.java         the home screen
  screen/InstalledModsSection.java    Installed Mods area (empty state for now)
  ui/theme/ZniTheme.java              ALL ZniStudios colors and spacing
  ui/render/ZniDraw.java              panels, outlines, gold divider, text wrapping
  ui/render/ZniBranding.java          face/character drawing (baked textures, monogram fallback)
  ui/widget/ZniButton.java            standard gold-hover/focus button
  ui/widget/ZniIconButton.java        square icon variant
```

## Planned (not built yet)

- **Registration API (Step 2)**: ZniStudios mods register a mod ID, display name, version,
  icon, description and a settings-screen callback; the Installed Mods section lists them.
- **Shared library**: other ZniStudios mods depend on this mod and reuse `ZniTheme`,
  `ZniButton`, panels and headings, so every mod shares one look.

## Building

`./gradlew build` → `build/libs/znis-mod-studio-1.0.0.jar`
