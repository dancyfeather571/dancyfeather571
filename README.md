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

| File | Purpose |
|---|---|
| `src/main/resources/assets/znis_mod_studio/textures/gui/zni_face.png` | A pre-cropped face/head icon (any size, drawn whole, aspect ratio kept). |
| `src/main/resources/assets/znis_mod_studio/textures/gui/zni_skin.png` | *Or* a full 64x64 / 64x32 skin: only the head front + hat layer are cropped out. Used when `zni_face.png` is absent. |
| `src/main/resources/assets/znis_mod_studio/icon.png` | Mod list icon (currently a placeholder "Z" monogram). Replace with your own art. |

If neither face file exists, a gold "Z" monogram is drawn instead. Pixel art is always drawn
with nearest-neighbour sampling at whole-number scales, so it stays crisp.

## Project structure

```
src/main/java/com/znistudios/modstudio/
  ZnisModStudio.java                  common entrypoint, MOD_ID, id() helper
src/client/java/com/znistudios/modstudio/client/
  ZnisModStudioClient.java            client entrypoint: key mapping, pause-menu entry, openModStudio()
  mixin/PauseScreenMixin.java         adds the icon button to the pause menu icon row
  screen/ModStudioScreen.java         the home screen
  screen/InstalledModsSection.java    Installed Mods area (empty state for now)
  ui/theme/ZniTheme.java              ALL ZniStudios colors and spacing
  ui/render/ZniDraw.java              panels, outlines, gold divider, text wrapping
  ui/render/ZniFace.java              face/skin drawing with crisp pixels + fallback
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
