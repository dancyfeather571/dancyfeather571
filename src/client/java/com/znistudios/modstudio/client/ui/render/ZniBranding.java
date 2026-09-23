package com.znistudios.modstudio.client.ui.render;

import com.znistudios.modstudio.ZnisModStudio;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

/**
 * Draws the ZniStudios character branding.
 *
 * <p>All textures are pre-baked from the source skin ({@code art/zni_skin_source.png}) by
 * {@code art/BakeBranding.java}, with overlay layers composited and whole-number
 * nearest-neighbour enlargement. The skin is never cropped or stretched at runtime:
 * <ul>
 *     <li>{@code zni_face_8/16/32.png}: the head (face + hair overlay) for icons and headers;</li>
 *     <li>{@code zni_character.png} (16x32) and {@code zni_character_2x.png} (32x64): the full
 *     front-view character for larger branding areas.</li>
 * </ul>
 * Each draw picks the baked size that matches the requested size, so the common sizes are
 * drawn 1:1; other multiples use a whole-number scale. Minecraft samples GUI textures with
 * nearest-neighbour filtering (no {@code blur} metadata is set), and GUI scale is always a whole
 * number, so every pixel edge stays sharp at every GUI scale.
 */
public final class ZniBranding {
	public static final Identifier FACE_8 = ZnisModStudio.id("textures/gui/zni_face_8.png");
	public static final Identifier FACE_16 = ZnisModStudio.id("textures/gui/zni_face_16.png");
	public static final Identifier FACE_32 = ZnisModStudio.id("textures/gui/zni_face_32.png");
	public static final Identifier CHARACTER = ZnisModStudio.id("textures/gui/zni_character.png");
	public static final Identifier CHARACTER_2X = ZnisModStudio.id("textures/gui/zni_character_2x.png");

	/** Size of the character art at 1x, in GUI pixels. */
	public static final int CHARACTER_WIDTH = 16;
	public static final int CHARACTER_HEIGHT = 32;

	private static final int[] FACE_SIZES = {32, 16, 8};
	private static final Identifier[] FACE_TEXTURES = {FACE_32, FACE_16, FACE_8};
	private static final Component MONOGRAM = Component.literal("Z");

	private static Boolean available;

	private ZniBranding() {
	}

	/**
	 * Re-checks that the branding textures exist. Called when a ZniStudios screen is built, so a
	 * resource pack that removes them falls back to the monogram instead of the missing texture.
	 */
	public static void refresh() {
		var resources = Minecraft.getInstance().getResourceManager();
		available = resources.getResource(FACE_8).isPresent()
				&& resources.getResource(FACE_16).isPresent()
				&& resources.getResource(FACE_32).isPresent()
				&& resources.getResource(CHARACTER).isPresent()
				&& resources.getResource(CHARACTER_2X).isPresent();
	}

	/** Draws the head/face into a {@code size} x {@code size} square. Use multiples of 8 for exact pixels. */
	public static void drawFace(GuiGraphicsExtractor graphics, int x, int y, int size) {
		if (!isAvailable()) {
			drawMonogram(graphics, x, y, size);
			return;
		}

		// Prefer the largest baked size that divides the target evenly (1:1 or a whole-number scale).
		for (int i = 0; i < FACE_SIZES.length; i++) {
			if (size % FACE_SIZES[i] == 0) {
				drawScaled(graphics, FACE_TEXTURES[i], x, y, FACE_SIZES[i], FACE_SIZES[i], size / FACE_SIZES[i]);
				return;
			}
		}

		// Odd sizes: the largest baked face that fits, drawn 1:1 and centered.
		for (int i = 0; i < FACE_SIZES.length; i++) {
			if (FACE_SIZES[i] <= size || i == FACE_SIZES.length - 1) {
				int offset = (size - FACE_SIZES[i]) / 2;
				drawScaled(graphics, FACE_TEXTURES[i], x + offset, y + offset, FACE_SIZES[i], FACE_SIZES[i], 1);
				return;
			}
		}
	}

	/**
	 * Draws the full front-view character at a whole-number {@code scale}, occupying
	 * {@code CHARACTER_WIDTH * scale} x {@code CHARACTER_HEIGHT * scale} GUI pixels.
	 */
	public static void drawCharacter(GuiGraphicsExtractor graphics, int x, int y, int scale) {
		scale = Math.max(1, scale);
		if (!isAvailable()) {
			int size = CHARACTER_WIDTH * scale;
			drawMonogram(graphics, x, y + (CHARACTER_HEIGHT * scale - size) / 2, size);
			return;
		}

		if (scale % 2 == 0) {
			drawScaled(graphics, CHARACTER_2X, x, y, CHARACTER_WIDTH * 2, CHARACTER_HEIGHT * 2, scale / 2);
		} else {
			drawScaled(graphics, CHARACTER, x, y, CHARACTER_WIDTH, CHARACTER_HEIGHT, scale);
		}
	}

	/** Draws a whole texture of the given size, enlarged by a whole-number factor. */
	private static void drawScaled(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width, int height, int factor) {
		int w = width * factor;
		int h = height * factor;
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, w, h, w, h);
	}

	private static boolean isAvailable() {
		if (available == null) {
			refresh();
		}
		return available;
	}

	private static void drawMonogram(GuiGraphicsExtractor graphics, int x, int y, int size) {
		ZniDraw.panel(graphics, x, y, size, size, ZniTheme.PANEL_INSET, ZniTheme.GOLD_ACCENT);
		Font font = Minecraft.getInstance().font;
		FormattedCharSequence letter = MONOGRAM.getVisualOrderText();
		int textX = x + (size - font.width(letter)) / 2 + 1;
		int textY = y + (size - font.lineHeight) / 2 + 1;
		graphics.text(font, letter, textX, textY, ZniTheme.GOLD_ACCENT, false);
	}
}
