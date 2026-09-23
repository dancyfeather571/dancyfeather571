package com.znistudios.modstudio.client.ui.render;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.znistudios.modstudio.ZnisModStudio;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FormattedCharSequence;

/**
 * Draws the ZniStudios face used for branding.
 *
 * <p>Two artwork slots are checked, in this order:
 * <ol>
 *     <li>{@code textures/gui/zni_face.png}: a pre-cropped face/head icon of any size, drawn
 *     whole with its aspect ratio preserved.</li>
 *     <li>{@code textures/gui/zni_skin.png}: a full Minecraft skin (64x64, or legacy 64x32).
 *     Only the 8x8 head front plus its hat overlay are cropped out, so the skin is never
 *     stretched into the icon.</li>
 * </ol>
 * Pixel art stays crisp: Minecraft samples GUI textures with nearest-neighbour filtering, and
 * skins are only drawn at whole-number multiples of 8px. If neither file exists, a gold "Z"
 * monogram is drawn so the purple missing-texture checker never appears.
 */
public final class ZniFace {
	public static final Identifier FACE_TEXTURE = ZnisModStudio.id("textures/gui/zni_face.png");
	public static final Identifier SKIN_TEXTURE = ZnisModStudio.id("textures/gui/zni_skin.png");

	private static final int SKIN_FACE_U = 8;
	private static final int SKIN_FACE_V = 8;
	private static final int SKIN_HAT_U = 40;
	private static final int SKIN_FACE_SIZE = 8;
	private static final Component MONOGRAM = Component.literal("Z");

	private enum Kind { MISSING, SKIN, IMAGE }

	private record Info(Kind kind, Identifier texture, int width, int height) {
		static final Info MISSING_INFO = new Info(Kind.MISSING, FACE_TEXTURE, 0, 0);
	}

	private static Info info;

	private ZniFace() {
	}

	/**
	 * Re-reads the face texture's dimensions. Called when a ZniStudios screen is built, so a
	 * resource pack override or replaced file is picked up without restarting.
	 */
	public static void refresh() {
		info = readInfo();
	}

	public static void draw(GuiGraphicsExtractor graphics, int x, int y, int size) {
		if (info == null) {
			refresh();
		}

		switch (info.kind()) {
			case SKIN -> drawSkinFace(graphics, info.texture(), x, y, size, info.height());
			case IMAGE -> drawImage(graphics, info.texture(), x, y, size, info.width(), info.height());
			case MISSING -> drawMonogram(graphics, x, y, size);
		}
	}

	private static void drawSkinFace(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int size, int skinHeight) {
		// Only whole-number scales keep skin pixels perfectly square.
		int scale = Math.max(1, size / SKIN_FACE_SIZE);
		int drawn = scale * SKIN_FACE_SIZE;
		int dx = x + (size - drawn) / 2;
		int dy = y + (size - drawn) / 2;

		// Sampling a region of a "virtually scaled" texture draws that region enlarged.
		int texWidth = 64 * scale;
		int texHeight = skinHeight * scale;
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, dx, dy,
				SKIN_FACE_U * scale, SKIN_FACE_V * scale, drawn, drawn, texWidth, texHeight);
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, dx, dy,
				SKIN_HAT_U * scale, SKIN_FACE_V * scale, drawn, drawn, texWidth, texHeight);
	}

	private static void drawImage(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int size, int width, int height) {
		int longest = Math.max(width, height);
		int drawWidth = Math.max(1, size * width / longest);
		int drawHeight = Math.max(1, size * height / longest);
		int dx = x + (size - drawWidth) / 2;
		int dy = y + (size - drawHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, dx, dy, 0.0F, 0.0F, drawWidth, drawHeight, drawWidth, drawHeight);
	}

	private static void drawMonogram(GuiGraphicsExtractor graphics, int x, int y, int size) {
		ZniDraw.panel(graphics, x, y, size, size, ZniTheme.PANEL_INSET, ZniTheme.GOLD_ACCENT);
		Font font = Minecraft.getInstance().font;
		FormattedCharSequence letter = MONOGRAM.getVisualOrderText();
		int textX = x + (size - font.width(letter)) / 2 + 1;
		int textY = y + (size - font.lineHeight) / 2 + 1;
		graphics.text(font, letter, textX, textY, ZniTheme.GOLD_ACCENT, false);
	}

	private static Info readInfo() {
		Info face = readInfo(FACE_TEXTURE, Kind.IMAGE);
		if (face.kind() != Kind.MISSING) {
			return face;
		}

		Info skin = readInfo(SKIN_TEXTURE, Kind.SKIN);
		if (skin.kind() == Kind.SKIN && (skin.width() != 64 || (skin.height() != 64 && skin.height() != 32))) {
			ZnisModStudio.LOGGER.warn("{} is {}x{}, not a 64x64 or 64x32 skin; drawing it whole instead",
					SKIN_TEXTURE, skin.width(), skin.height());
			return new Info(Kind.IMAGE, SKIN_TEXTURE, skin.width(), skin.height());
		}
		return skin;
	}

	private static Info readInfo(Identifier texture, Kind kind) {
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);
		if (resource.isEmpty()) {
			return Info.MISSING_INFO;
		}

		// Read the PNG IHDR header directly: width and height are the two ints after the 16-byte prefix.
		try (InputStream stream = resource.get().open(); DataInputStream in = new DataInputStream(stream)) {
			in.skipNBytes(16);
			int width = in.readInt();
			int height = in.readInt();
			if (width <= 0 || height <= 0) {
				return Info.MISSING_INFO;
			}
			return new Info(kind, texture, width, height);
		} catch (IOException e) {
			ZnisModStudio.LOGGER.warn("Could not read {}; using the monogram fallback", texture, e);
			return Info.MISSING_INFO;
		}
	}
}
