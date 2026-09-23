package com.znistudios.modstudio.client.api;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.screens.Screen;

/**
 * Extension point for mod-specific settings pages inside Zni's Mod Studio.
 *
 * <p>Registration itself only needs {@code fabric.mod.json} (see {@code ZniModRegistry}). A
 * registered mod that also has a settings screen can hand it to the Studio from its client
 * entrypoint:
 * <pre>{@code
 * ZniSettingsScreens.register("my_mod", parent -> new MySettingsScreen(parent));
 * }</pre>
 * The Studio then opens that screen when the mod's card is pressed. This is deliberately just a
 * screen factory: how a mod builds its settings screen is up to the mod.
 */
public final class ZniSettingsScreens {
	/** Creates a mod's settings screen; {@code parent} is the screen to return to when it closes. */
	@FunctionalInterface
	public interface Factory {
		Screen create(@Nullable Screen parent);
	}

	private static final Map<String, Factory> FACTORIES = new ConcurrentHashMap<>();

	private ZniSettingsScreens() {
	}

	public static void register(String modId, Factory factory) {
		FACTORIES.put(modId, factory);
	}

	public static Optional<Factory> get(String modId) {
		return Optional.ofNullable(FACTORIES.get(modId));
	}
}
