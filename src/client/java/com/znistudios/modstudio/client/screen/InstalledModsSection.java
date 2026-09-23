package com.znistudios.modstudio.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.znistudios.modstudio.api.ZniRegisteredMod;
import com.znistudios.modstudio.client.ui.render.ZniDraw;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * The "Installed Mods" area of the Mod Studio home screen.
 *
 * <p>Shows one {@link ModCard} per mod discovered by {@code ZniModRegistry}, or an honest empty
 * state when none are registered. The list scrolls with the mouse wheel, and keyboard/controller
 * focus moving onto a card scrolls it into view. Cards are drawn clipped to the list area.
 */
final class InstalledModsSection {
	private static final Component HEADING = Component.translatable("znis_mod_studio.installed_mods");
	private static final Component EMPTY = Component.translatable("znis_mod_studio.installed_mods.empty");
	private static final Component EMPTY_HINT = Component.translatable("znis_mod_studio.installed_mods.empty_hint");

	private static final int CARD_GAP = 4;
	private static final int SCROLLBAR_WIDTH = 3;
	private static final int SCROLL_STEP = ModCard.HEIGHT / 2;

	private final List<ModCard> cards = new ArrayList<>();

	private int x;
	private int y;
	private int width;
	private int height;
	private int listTop;
	private int listBottom;
	private int scroll;
	private int mouseX = -1;
	private int mouseY = -1;

	InstalledModsSection(List<ZniRegisteredMod> mods, Screen studio) {
		for (ZniRegisteredMod mod : mods) {
			this.cards.add(new ModCard(mod, this, studio));
		}
	}

	/** Smallest height at which the heading plus one line of the empty state still fits. */
	static int minHeight(Font font) {
		return headerHeight(font) + font.lineHeight + 2 * ZniTheme.PADDING;
	}

	/** Height the screen should try to keep free for this section: one full card, or the empty state. */
	static int preferredMinHeight(Font font, boolean hasMods) {
		return hasMods ? headerHeight(font) + ModCard.HEIGHT + ZniTheme.PADDING : minHeight(font);
	}

	/** Positions the section and hands its cards to the screen (as input-only widgets; the section draws them). */
	void init(Font font, int x, int y, int width, int height, Consumer<AbstractWidget> addWidget) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.listTop = y + headerHeight(font);
		this.listBottom = y + height - ZniTheme.PADDING;
		this.scroll = Math.clamp(this.scroll, 0, this.maxScroll());

		boolean visible = height >= minHeight(font);
		for (ModCard card : this.cards) {
			card.visible = visible;
			addWidget.accept(card);
		}
		this.layoutCards();
	}

	boolean viewportContains(double mouseX, double mouseY) {
		return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.listTop && mouseY < this.listBottom;
	}

	int mouseX() {
		return this.mouseX;
	}

	int mouseY() {
		return this.mouseY;
	}

	/** Mouse-wheel scrolling; returns true when the wheel was over the list and it consumed it. */
	boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (this.cards.isEmpty() || !this.viewportContains(mouseX, mouseY) || this.maxScroll() == 0) {
			return false;
		}
		this.setScroll(this.scroll - (int) Math.round(amount * SCROLL_STEP));
		return true;
	}

	void extract(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY, float delta) {
		if (this.height < minHeight(font)) {
			return;
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;

		ZniDraw.panel(graphics, this.x, this.y, this.width, this.height);

		int inner = this.width - 2 * ZniTheme.PADDING;
		int textX = this.x + ZniTheme.PADDING;
		int textY = this.y + ZniTheme.PADDING;
		graphics.text(font, ZniDraw.singleLine(font, HEADING, inner), textX, textY, ZniTheme.GOLD_ACCENT, false);

		if (this.cards.isEmpty()) {
			extractEmptyState(graphics, font, textX, this.listTop, inner, this.listBottom - this.listTop);
			return;
		}

		this.followFocus();

		graphics.enableScissor(this.x + 1, this.listTop, this.x + this.width - 1, this.listBottom);
		for (ModCard card : this.cards) {
			if (card.getY() + card.getHeight() > this.listTop && card.getY() < this.listBottom) {
				card.extractRenderState(graphics, mouseX, mouseY, delta);
			}
		}
		graphics.disableScissor();

		this.extractScrollbar(graphics);
	}

	private void extractScrollbar(GuiGraphicsExtractor graphics) {
		int max = this.maxScroll();
		if (max == 0) {
			return;
		}
		int trackX = this.x + this.width - ZniTheme.PADDING + (ZniTheme.PADDING - SCROLLBAR_WIDTH) / 2;
		int trackHeight = this.listBottom - this.listTop;
		int thumbHeight = Math.max(12, trackHeight * trackHeight / (trackHeight + max));
		int thumbY = this.listTop + (trackHeight - thumbHeight) * this.scroll / max;
		graphics.fill(trackX, this.listTop, trackX + SCROLLBAR_WIDTH, this.listBottom, ZniTheme.PANEL_INSET);
		graphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, ZniTheme.GOLD_ACCENT);
	}

	/** Keeps a keyboard/controller-focused card fully inside the visible list area. */
	private void followFocus() {
		for (ModCard card : this.cards) {
			if (card.isFocused()) {
				int top = card.getY();
				int bottom = top + card.getHeight();
				if (top < this.listTop) {
					this.setScroll(this.scroll - (this.listTop - top));
				} else if (bottom > this.listBottom) {
					this.setScroll(this.scroll + (bottom - this.listBottom));
				}
				return;
			}
		}
	}

	private void setScroll(int scroll) {
		this.scroll = Math.clamp(scroll, 0, this.maxScroll());
		this.layoutCards();
	}

	private void layoutCards() {
		int cardWidth = this.width - 2 * ZniTheme.PADDING;
		int cardY = this.listTop - this.scroll;
		for (ModCard card : this.cards) {
			card.setPosition(this.x + ZniTheme.PADDING, cardY);
			card.setWidth(cardWidth);
			cardY += ModCard.HEIGHT + CARD_GAP;
		}
	}

	private int contentHeight() {
		return this.cards.isEmpty() ? 0 : this.cards.size() * (ModCard.HEIGHT + CARD_GAP) - CARD_GAP;
	}

	private int maxScroll() {
		return Math.max(0, this.contentHeight() - (this.listBottom - this.listTop));
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
