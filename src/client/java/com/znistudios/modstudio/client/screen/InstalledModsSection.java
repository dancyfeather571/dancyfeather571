package com.znistudios.modstudio.client.screen;

import java.util.List;

import com.znistudios.modstudio.client.ui.render.ZniDraw;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * The "Installed Mods" area of the Mod Studio home screen.
 *
 * <p>Step 1 has no registration API yet, so this only ever shows an honest empty state:
 * no mods are faked. In Step 2 the registry will hand this section the list of registered
 * ZniStudios mods, and the empty-state branch becomes the fallback for an empty list.
 * The area it is given is already sized by the screen, so a list or grid can fill it.
 */
final class InstalledModsSection {
	private static final Component HEADING = Component.translatable("znis_mod_studio.installed_mods");
	private static final Component EMPTY = Component.translatable("znis_mod_studio.installed_mods.empty");
	private static final Component EMPTY_HINT = Component.translatable("znis_mod_studio.installed_mods.empty_hint");

	private InstalledModsSection() {
	}

	/** Smallest height at which the heading plus one line of the empty state still fits. */
	static int minHeight(Font font) {
		return headerHeight(font) + font.lineHeight + 2 * ZniTheme.PADDING;
	}

	static void extract(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height) {
		ZniDraw.panel(graphics, x, y, width, height);

		int inner = width - 2 * ZniTheme.PADDING;
		int textX = x + ZniTheme.PADDING;
		int textY = y + ZniTheme.PADDING;
		graphics.text(font, ZniDraw.singleLine(font, HEADING, inner), textX, textY, ZniTheme.GOLD_ACCENT, false);

		int bodyTop = y + headerHeight(font);
		int bodyHeight = y + height - ZniTheme.PADDING - bodyTop;
		extractEmptyState(graphics, font, textX, bodyTop, inner, bodyHeight);
	}

	private static int headerHeight(Font font) {
		return ZniTheme.PADDING + font.lineHeight + ZniTheme.GAP;
	}

	private static void extractEmptyState(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height) {
		if (height < font.lineHeight) {
			return;
		}
		graphics.fill(x, y, x + width, y + height, ZniTheme.PANEL_INSET);

		int lineGap = 2;
		List<FormattedCharSequence> primary = ZniDraw.wrap(font, EMPTY, width - 2 * ZniTheme.GAP, 2);
		List<FormattedCharSequence> hint = ZniDraw.wrap(font, EMPTY_HINT, width - 2 * ZniTheme.GAP, 2);

		int lineStep = font.lineHeight + lineGap;
		int maxLines = Math.max(1, (height + lineGap) / lineStep);
		int shown = Math.min(maxLines, primary.size() + hint.size());

		int lineY = y + (height - (shown * lineStep - lineGap)) / 2;
		for (int i = 0; i < shown; i++) {
			boolean isPrimary = i < primary.size();
			FormattedCharSequence line = isPrimary ? primary.get(i) : hint.get(i - primary.size());
			int color = isPrimary ? ZniTheme.TEXT_SECONDARY : ZniTheme.TEXT_MUTED;
			graphics.text(font, line, x + (width - font.width(line)) / 2, lineY, color, false);
			lineY += lineStep;
		}
	}
}
