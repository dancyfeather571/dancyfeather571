package com.znistudios.modstudio.client.ui.render;

import java.util.ArrayList;
import java.util.List;

import com.znistudios.modstudio.client.ui.theme.ZniTheme;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * Small drawing helpers shared by every ZniStudios screen and component.
 */
public final class ZniDraw {
	private static final FormattedText ELLIPSIS = FormattedText.of("...");

	private ZniDraw() {
	}

	/** Draws a 1px outline just inside the given rectangle. */
	public static void outline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y + 1, x + 1, y + height - 1, color);
		graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
	}

	/** Draws a standard ZniStudios panel: dark fill with a neutral hairline border. */
	public static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		panel(graphics, x, y, width, height, ZniTheme.PANEL, ZniTheme.BORDER);
	}

	public static void panel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int fill, int border) {
		graphics.fill(x, y, x + width, y + height, fill);
		outline(graphics, x, y, width, height, border);
	}

	/** A thin gold rule with a softer glow line beneath it, used under headers. */
	public static void goldDivider(GuiGraphicsExtractor graphics, int x, int y, int width) {
		graphics.fill(x, y, x + width, y + 1, ZniTheme.GOLD_ACCENT);
		graphics.fill(x, y + 1, x + width, y + 2, ZniTheme.GOLD_SOFT);
	}

	/**
	 * Word-wraps text to the given width. If {@code maxLines} would be exceeded, the last
	 * visible line is ellipsized so text never spills outside its area.
	 */
	public static List<FormattedCharSequence> wrap(Font font, Component text, int width, int maxLines) {
		List<FormattedCharSequence> result = new ArrayList<>();
		if (width <= 0 || maxLines <= 0) {
			return result;
		}

		List<FormattedText> lines = font.getSplitter().splitLines(text, width, Style.EMPTY);
		for (int i = 0; i < lines.size() && i < maxLines; i++) {
			FormattedText line = lines.get(i);
			if (i == maxLines - 1 && lines.size() > maxLines) {
				line = ellipsize(font, line, width);
			}
			result.add(Language.getInstance().getVisualOrder(line));
		}
		return result;
	}

	/** Returns the text as a single line, cut with "..." if it is wider than {@code width}. */
	public static FormattedCharSequence singleLine(Font font, Component text, int width) {
		if (font.width(text) <= width) {
			return text.getVisualOrderText();
		}
		return Language.getInstance().getVisualOrder(ellipsize(font, text, width));
	}

	private static FormattedText ellipsize(Font font, FormattedText text, int width) {
		int available = Math.max(0, width - font.width(ELLIPSIS));
		List<FormattedText> parts = font.getSplitter().splitLines(text, Math.max(1, available), Style.EMPTY);
		FormattedText head = parts.isEmpty() ? FormattedText.of("") : parts.get(0);
		return FormattedText.composite(head, ELLIPSIS);
	}
}
