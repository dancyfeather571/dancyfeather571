package com.znistudios.modstudio.client.screen;

import java.util.Locale;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.znistudios.modstudio.api.ZniRegisteredMod;
import com.znistudios.modstudio.client.api.ZniSettingsScreens;
import com.znistudios.modstudio.client.ui.render.ZniDraw;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;
import com.znistudios.modstudio.client.ui.widget.ZniButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

/**
 * One registered ZniStudios mod in the Installed Mods list: icon, name, version, authors and
 * description. It is a {@link ZniButton}, so it gets the standard gold hover/focus accent and
 * works with mouse, keyboard and controller navigation.
 *
 * <p>Pressing it opens the mod's settings screen when the mod has supplied one through
 * {@link ZniSettingsScreens}; otherwise pressing does nothing.
 */
final class ModCard extends ZniButton {
	static final int HEIGHT = 44;
	private static final int ICON_SIZE = 32;
	private static final Component SETTINGS = Component.translatable("znis_mod_studio.installed_mods.settings");

	private final ZniRegisteredMod mod;
	private final InstalledModsSection section;
	private final @Nullable Identifier icon;

	ModCard(ZniRegisteredMod mod, InstalledModsSection section, Screen studio) {
		super(0, 0, 100, HEIGHT, Component.literal(mod.name()), button -> openSettings(mod, studio));
		this.mod = mod;
		this.section = section;
		this.icon = resolveIcon(mod);
	}

	private static void openSettings(ZniRegisteredMod mod, Screen studio) {
		Optional<ZniSettingsScreens.Factory> factory = ZniSettingsScreens.get(mod.modId());
		factory.ifPresent(f -> Minecraft.getInstance().gui.setScreen(f.create(studio)));
	}

	private boolean hasSettings() {
		return ZniSettingsScreens.get(this.mod.modId()).isPresent();
	}

	// Clicks and hover only count inside the list's visible (clipped) area.

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.section.viewportContains(mouseX, mouseY) && super.isMouseOver(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return this.section.viewportContains(event.x(), event.y()) && super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean isHighlighted() {
		return this.active && ((this.isHovered() && this.section.viewportContains(this.section.mouseX(), this.section.mouseY())) || this.isFocused());
	}

	@Override
	protected void extractLabel(GuiGraphicsExtractor graphics, int color) {
		Font font = Minecraft.getInstance().font;
		int x = this.getX();
		int y = this.getY();
		int pad = ZniTheme.GAP;

		int iconX = x + pad;
		int iconY = y + (this.getHeight() - ICON_SIZE) / 2;
		this.extractIcon(graphics, font, iconX, iconY);

		int textX = iconX + ICON_SIZE + pad + 2;
		int right = x + this.getWidth() - pad;

		// Optional "Settings" tag on the right.
		if (this.hasSettings()) {
			int tagWidth = font.width(SETTINGS);
			int tagX = right - tagWidth;
			graphics.text(font, SETTINGS.getVisualOrderText(), tagX, y + pad, this.isHighlighted() ? ZniTheme.GOLD_HOVER : ZniTheme.GOLD_ACCENT, false);
			right = tagX - pad;
		}
		int textWidth = Math.max(0, right - textX);

		// Line 1: name + version
		int lineY = y + pad;
		FormattedCharSequence name = ZniDraw.singleLine(font, Component.literal(this.mod.name()), textWidth);
		int nameColor = this.isHighlighted() ? ZniTheme.GOLD_HOVER : ZniTheme.TEXT_PRIMARY;
		graphics.text(font, name, textX, lineY, nameColor, false);
		int nameWidth = font.width(name);
		if (!this.mod.version().isEmpty() && nameWidth + 8 < textWidth) {
			FormattedCharSequence version = ZniDraw.singleLine(font, Component.literal(this.mod.version()), textWidth - nameWidth - 6);
			graphics.text(font, version, textX + nameWidth + 6, lineY, ZniTheme.TEXT_MUTED, false);
		}

		// Line 2: authors
		lineY += font.lineHeight + 2;
		if (!this.mod.authors().isEmpty()) {
			Component authors = Component.translatable("znis_mod_studio.installed_mods.by", String.join(", ", this.mod.authors()));
			graphics.text(font, ZniDraw.singleLine(font, authors, textWidth), textX, lineY, ZniTheme.TEXT_SECONDARY, false);
			lineY += font.lineHeight + 2;
		}

		// Line 3: description
		if (!this.mod.description().isEmpty() && lineY + font.lineHeight <= y + this.getHeight() - 2) {
			graphics.text(font, ZniDraw.singleLine(font, Component.literal(this.mod.description()), textWidth), textX, lineY, ZniTheme.TEXT_MUTED, false);
		}
	}

	private void extractIcon(GuiGraphicsExtractor graphics, Font font, int x, int y) {
		if (this.icon != null) {
			// Whole texture into a 32x32 square; GUI textures use nearest-neighbour sampling.
			graphics.blit(RenderPipelines.GUI_TEXTURED, this.icon, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
			return;
		}

		// No usable icon: a neutral tile with the mod's initial.
		ZniDraw.panel(graphics, x, y, ICON_SIZE, ICON_SIZE, ZniTheme.PANEL_INSET, ZniTheme.BORDER);
		String initial = this.mod.name().isEmpty() ? "?" : this.mod.name().substring(0, 1).toUpperCase(Locale.ROOT);
		FormattedCharSequence letter = Component.literal(initial).getVisualOrderText();
		graphics.text(font, letter, x + (ICON_SIZE - font.width(letter)) / 2 + 1, y + (ICON_SIZE - font.lineHeight) / 2 + 1, ZniTheme.GOLD_ACCENT, false);
	}

	/**
	 * Mod icons follow the Fabric convention {@code assets/<namespace>/<path>}, which makes them
	 * normal resources. Anything else, or a file that is not actually there, gets the fallback tile.
	 */
	private static @Nullable Identifier resolveIcon(ZniRegisteredMod mod) {
		if (mod.iconPath().isEmpty()) {
			return null;
		}
		String path = mod.iconPath().get().replace('\\', '/');
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (!path.startsWith("assets/")) {
			return null;
		}
		int slash = path.indexOf('/', "assets/".length());
		if (slash < 0) {
			return null;
		}

		try {
			Identifier id = Identifier.fromNamespaceAndPath(path.substring("assets/".length(), slash), path.substring(slash + 1));
			return Minecraft.getInstance().getResourceManager().getResource(id).isPresent() ? id : null;
		} catch (RuntimeException e) {
			return null; // Invalid characters in the path.
		}
	}
}
