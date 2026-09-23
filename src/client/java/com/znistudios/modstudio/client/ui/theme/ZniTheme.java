package com.znistudios.modstudio.client.ui.theme;

/**
 * The single source of truth for the ZniStudios look.
 *
 * <p>Every ZniStudios screen and component reads its colors and spacing from here.
 * Never hard-code a color in a screen: add a named value here instead, so the whole
 * ZniStudios theme can be retuned from one file.
 *
 * <p>All colors are ARGB ({@code 0xAARRGGBB}). Minecraft treats an alpha of {@code 0x00}
 * as fully transparent, so opaque colors must start with {@code 0xFF}.
 */
public final class ZniTheme {
	// Surfaces
	/** Full-screen backdrop drawn behind ZniStudios screens. Slightly translucent so the world blur shows through. */
	public static final int BACKGROUND = 0xE60A0A0C;
	/** Main content panels. */
	public static final int PANEL = 0xF2141416;
	/** Nested / inset areas inside a panel (lists, empty states). */
	public static final int PANEL_INSET = 0xFF0F0F11;
	/** Neutral hairline border for panels and idle components. */
	public static final int BORDER = 0xFF2A2A2F;

	// Text
	public static final int TEXT_PRIMARY = 0xFFF1EDE4;
	public static final int TEXT_SECONDARY = 0xFFA39E94;
	public static final int TEXT_MUTED = 0xFF6D6962;

	// Gold accents: a warm, muted gold rather than a bright yellow
	/** Resting gold used for headings, dividers and selected elements. */
	public static final int GOLD_ACCENT = 0xFFC9A24A;
	/** Brighter gold used when an element is hovered or focused. */
	public static final int GOLD_HOVER = 0xFFE6C36A;
	/** Low-alpha gold used for soft glows and subtle dividers. */
	public static final int GOLD_SOFT = 0x40C9A24A;

	// Interactive components
	public static final int BUTTON = 0xFF1A1A1D;
	/** Button fill while hovered/focused: a very slightly warm charcoal to sit under the gold border. */
	public static final int BUTTON_HIGHLIGHT = 0xFF231F17;
	public static final int DISABLED = 0xFF121214;
	public static final int DISABLED_TEXT = 0xFF4E4B46;

	// Spacing (GUI pixels)
	public static final int PADDING = 8;
	public static final int GAP = 6;
	public static final int BUTTON_HEIGHT = 20;
	/** Width of the gold border on hovered/focused/selected components. */
	public static final int ACCENT_BORDER = 1;

	private ZniTheme() {
	}
}
