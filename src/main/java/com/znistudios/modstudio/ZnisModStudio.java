package com.znistudios.modstudio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

/**
 * Common entrypoint and shared constants for Zni's Mod Studio.
 *
 * <p>Everything visual lives in the client source set. This class stays tiny so that
 * future ZniStudios mods can depend on the library from either side.
 */
public final class ZnisModStudio implements ModInitializer {
	public static final String MOD_ID = "znis_mod_studio";
	public static final Logger LOGGER = LoggerFactory.getLogger("Zni's Mod Studio");

	@Override
	public void onInitialize() {
		LOGGER.info("Zni's Mod Studio initialized");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
