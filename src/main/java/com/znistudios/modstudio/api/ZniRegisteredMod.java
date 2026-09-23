package com.znistudios.modstudio.api;

import java.util.List;
import java.util.Optional;

import net.fabricmc.loader.api.ModContainer;

/**
 * A ZniStudios mod that opted into Zni's Mod Studio.
 *
 * <p>Everything here is read from the mod's own {@code fabric.mod.json}; mods never have to
 * repeat their name, version, description, authors or icon for the Studio.
 *
 * @param modId       Fabric mod ID
 * @param name        display name (falls back to the mod ID)
 * @param version     friendly version string (may be empty)
 * @param description description with line breaks flattened (may be empty)
 * @param authors     author names (may be empty)
 * @param iconPath    icon path inside the mod jar, e.g. {@code assets/example/icon.png}
 * @param container   the Fabric container, for anything the Studio needs later
 */
public record ZniRegisteredMod(
		String modId,
		String name,
		String version,
		String description,
		List<String> authors,
		Optional<String> iconPath,
		ModContainer container
) {
}
