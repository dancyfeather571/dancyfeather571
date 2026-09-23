package com.znistudios.modstudio.client;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;
import com.znistudios.modstudio.client.screen.ModStudioScreen;
import com.znistudios.modstudio.client.ui.render.ZniFace;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;
import com.znistudios.modstudio.client.ui.widget.ZniIconButton;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * Client entrypoint: wires up the ways to open Zni's Mod Studio.
 *
 * <ol>
 *     <li>A small face icon button in the pause menu's icon row (see {@code PauseScreenMixin}).
 *     If that injection ever fails to apply, a fallback places the same button beside
 *     "Back to Game" instead, so the entry point never silently disappears.</li>
 *     <li>An optional key mapping, unbound by default, under Options &gt; Controls.</li>
 * </ol>
 */
public final class ZnisModStudioClient implements ClientModInitializer {
	private static final Component OPEN_LABEL = Component.translatable("znis_mod_studio.button.open");

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

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof PauseScreen) {
				addPauseButtonFallback(screen);
			}
		});
	}

	/** Opens the Mod Studio home screen; {@code parent} is restored when it closes. */
	public static void openModStudio(@Nullable Screen parent) {
		Minecraft.getInstance().gui.setScreen(new ModStudioScreen(parent));
	}

	/** The pause-menu entry: a 20x20 ZniStudios icon button showing the face. */
	public static ZniIconButton createPauseMenuButton(Screen pauseScreen) {
		ZniFace.refresh();
		return ZniIconButton.create(OPEN_LABEL, ZniFace::draw, button -> openModStudio(pauseScreen));
	}

	private static void addPauseButtonFallback(Screen screen) {
		List<AbstractWidget> widgets = Screens.getWidgets(screen);
		AbstractWidget anchor = null;
		for (AbstractWidget widget : widgets) {
			if (widget instanceof ZniIconButton) {
				return; // The mixin already placed it in the icon row.
			}
			if (anchor == null && hasTranslationKey(widget, "menu.returnToGame")) {
				anchor = widget;
			}
		}

		if (anchor == null) {
			return; // Pause menu is hidden (F3+Esc); don't add anything.
		}

		ZniIconButton button = createPauseMenuButton(screen);
		button.setPosition(anchor.getX() + anchor.getWidth() + ZniTheme.GAP / 2 + 1, anchor.getY());
		widgets.add(button);
	}

	private static boolean hasTranslationKey(AbstractWidget widget, String key) {
		return widget.getMessage().getContents() instanceof TranslatableContents contents && contents.getKey().equals(key);
	}
}
