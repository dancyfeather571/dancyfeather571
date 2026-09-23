package com.znistudios.modstudio.client.ui.widget;

import java.util.function.Consumer;

import com.znistudios.modstudio.client.ui.theme.ZniTheme;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * A square {@link ZniButton} that shows an icon instead of text. It keeps the full button
 * behaviour (focus, gold highlight, narration of {@code message}) and shows the message as
 * a tooltip, since the text itself is not drawn.
 */
public class ZniIconButton extends ZniButton {
	/** Draws the icon inside the given square. */
	@FunctionalInterface
	public interface Icon {
		void draw(GuiGraphicsExtractor graphics, int x, int y, int size);
	}

	private final Icon icon;
	private final int iconSize;

	public ZniIconButton(int x, int y, int size, int iconSize, Component message, Icon icon, Consumer<ZniButton> onPress) {
		super(x, y, size, size, message, onPress);
		this.icon = icon;
		this.iconSize = iconSize;
		this.withTooltip(message);
	}

	public static ZniIconButton create(Component message, Icon icon, Consumer<ZniButton> onPress) {
		return new ZniIconButton(0, 0, ZniTheme.BUTTON_HEIGHT, 16, message, icon, onPress);
	}

	@Override
	protected void extractLabel(GuiGraphicsExtractor graphics, int color) {
		int offset = (this.getWidth() - this.iconSize) / 2;
		this.icon.draw(graphics, this.getX() + offset, this.getY() + offset, this.iconSize);
	}
}
