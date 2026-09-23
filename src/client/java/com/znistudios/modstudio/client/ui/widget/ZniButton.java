package com.znistudios.modstudio.client.ui.widget;

import java.util.function.Consumer;

import com.znistudios.modstudio.client.ui.render.ZniDraw;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * The standard ZniStudios button: dark and neutral at rest, gold when highlighted.
 *
 * <p>"Highlighted" means hovered by the mouse <em>or</em> focused through keyboard/controller
 * navigation, so the gold accent looks identical whichever input is used. The button is a
 * normal vanilla {@link AbstractButton}, so Tab/arrow-key focus, Enter/Space activation,
 * narration, click sounds and controller mods such as Controllable all work unchanged.
 *
 * <p>Subclasses change what is drawn inside the frame by overriding {@link #extractLabel}.
 */
public class ZniButton extends AbstractButton {
	private final Consumer<ZniButton> onPress;
	private boolean selected;

	public ZniButton(int x, int y, int width, int height, Component message, Consumer<ZniButton> onPress) {
		super(x, y, width, height, message);
		this.onPress = onPress;
	}

	public static ZniButton create(Component message, Consumer<ZniButton> onPress) {
		return new ZniButton(0, 0, 150, ZniTheme.BUTTON_HEIGHT, message, onPress);
	}

	public ZniButton withTooltip(Component tooltip) {
		this.setTooltip(Tooltip.create(tooltip));
		return this;
	}

	/** Selected buttons (e.g. the active tab) keep a resting gold border even when not highlighted. */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return this.selected;
	}

	/** True while hovered by the mouse or focused by keyboard/controller navigation. */
	public boolean isHighlighted() {
		return this.active && (this.isHovered() || this.isFocused());
	}

	@Override
	public void onPress(InputWithModifiers input) {
		this.onPress.accept(this);
	}

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		this.extractFrame(graphics);
		this.extractLabel(graphics, this.labelColor());
	}

	/** Background and border; this is where the gold hover/focus accent comes from. */
	protected void extractFrame(GuiGraphicsExtractor graphics) {
		int x = this.getX();
		int y = this.getY();
		int w = this.getWidth();
		int h = this.getHeight();

		if (!this.active) {
			ZniDraw.panel(graphics, x, y, w, h, ZniTheme.DISABLED, ZniTheme.BORDER);
			return;
		}

		if (this.isHighlighted()) {
			ZniDraw.panel(graphics, x, y, w, h, ZniTheme.BUTTON_HIGHLIGHT, ZniTheme.GOLD_HOVER);
			// Soft inner glow line along the bottom edge.
			graphics.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, ZniTheme.GOLD_SOFT);
		} else if (this.selected) {
			ZniDraw.panel(graphics, x, y, w, h, ZniTheme.BUTTON, ZniTheme.GOLD_ACCENT);
		} else {
			ZniDraw.panel(graphics, x, y, w, h, ZniTheme.BUTTON, ZniTheme.BORDER);
		}
	}

	protected void extractLabel(GuiGraphicsExtractor graphics, int color) {
		Font font = Minecraft.getInstance().font;
		int maxWidth = this.getWidth() - 2 * ZniTheme.GAP;
		FormattedCharSequence label = ZniDraw.singleLine(font, this.getMessage(), maxWidth);
		int textX = this.getX() + (this.getWidth() - font.width(label)) / 2;
		int textY = this.getY() + (this.getHeight() - font.lineHeight) / 2 + 1;
		graphics.text(font, label, textX, textY, color, false);
	}

	protected int labelColor() {
		if (!this.active) {
			return ZniTheme.DISABLED_TEXT;
		}
		if (this.isHighlighted()) {
			return ZniTheme.GOLD_HOVER;
		}
		return this.selected ? ZniTheme.GOLD_ACCENT : ZniTheme.TEXT_PRIMARY;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		output.add(NarratedElementType.TITLE, this.getMessage());
	}
}
