package com.znistudios.modstudio.client;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import com.znistudios.modstudio.client.entry.StudioEntryPoints;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;

/**
 * Client entrypoint: wires up the ways to open Zni's Mod Studio.
 *
 * <ol>
 *     <li>The Zni face button in the pause menu and on the title screen (see {@link StudioEntryPoints}).</li>
 *     <li>An optional key mapping, unbound by default, under Options &gt; Controls.</li>
 * </ol>
 */
public final class ZnisModStudioClient implements ClientModInitializer {
	private static KeyMapping openKey;

	@Override
	public void onInitializeClient() {
		openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.znis_mod_studio.open",
				InputConstants.Type.KEYSYM,
				InputConstants.UNKNOWN.getValue(),
				KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openKey.consumeClick()) {
				openModStudio(client.gui.screen());
			}
		});

		StudioEntryPoints.register();
	}

	/** Opens the Mod Studio home screen; {@code parent} is restored when it closes. */
	public static void openModStudio(@Nullable Screen parent) {
		StudioEntryPoints.openModStudio(parent);
	}
}
