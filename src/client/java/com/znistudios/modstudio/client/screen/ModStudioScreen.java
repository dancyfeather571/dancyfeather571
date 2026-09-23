package com.znistudios.modstudio.client.screen;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.znistudios.modstudio.api.ZniModRegistry;
import com.znistudios.modstudio.api.ZniRegisteredMod;
import com.znistudios.modstudio.client.ui.render.ZniBranding;
import com.znistudios.modstudio.client.ui.render.ZniDraw;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;
import com.znistudios.modstudio.client.ui.widget.ZniButton;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * The Zni's Mod Studio home screen.
 *
 * <p>Layout, top to bottom: branding header (character or face, title, subtitle), gold divider, intro
 * panel, "Installed Mods" section (fills the remaining height, lists registered mods), Done button. Every position
 * is derived from the current GUI-scaled width and height in {@link #init()}, which vanilla
 * calls again on every resize or GUI-scale change.
 */
public class ModStudioScreen extends Screen {
	private static final Component TITLE = Component.translatable("znis_mod_studio.title");
	private static final Component SUBTITLE = Component.translatable("znis_mod_studio.subtitle");
	private static final Component INTRO = Component.translatable("znis_mod_studio.intro");

	private static final int MAX_CONTENT_WIDTH = 360;
	private static final int MAX_BUTTON_WIDTH = 200;
	private static final int LINE_GAP = 2;

	private final @Nullable Screen parent;

	// Layout, recomputed in init()
	private int contentX;
	private int contentWidth;
	private int portraitX;
	private int portraitY;
	private int portraitWidth;
	private int portraitHeight;
	/** Full character at 2x on tall screens, otherwise the face only. */
	private boolean showCharacter;
	private int titleX;
	private int titleY;
	private int titleScale;
	private int subtitleY;
	private int dividerY;
	private int introY;
	private int introHeight;
	private List<FormattedCharSequence> introLines = List.of();
	private InstalledModsSection modsSection;

	public ModStudioScreen(@Nullable Screen parent) {
		super(TITLE);
		this.parent = parent;
	}

	@Override
	protected void init() {
		ZniBranding.refresh();

		int sideMargin = Math.max(ZniTheme.PADDING, this.width / 20);
		this.contentWidth = Math.min(this.width - 2 * sideMargin, MAX_CONTENT_WIDTH);
		this.contentX = (this.width - this.contentWidth) / 2;

		int top = clamp(this.height / 14, 6, 24);
		int bottomMargin = clamp(this.height / 20, 6, 20);

		// Header: [portrait] [title / subtitle], centered as a group.
		// Tall screens get the full character, medium the 32px face, small the 16px face.
		boolean roomy = this.height >= 240;
		this.showCharacter = this.height >= 320;
		if (this.showCharacter) {
			this.portraitWidth = ZniBranding.CHARACTER_WIDTH * 2;
			this.portraitHeight = ZniBranding.CHARACTER_HEIGHT * 2;
		} else {
			this.portraitWidth = roomy ? 32 : 16;
			this.portraitHeight = this.portraitWidth;
		}
		int textStart = this.portraitWidth + ZniTheme.GAP + 2;
		int titleWidth = this.font.width(TITLE);
		this.titleScale = roomy && textStart + titleWidth * 2 <= this.contentWidth ? 2 : 1;
		int textBlockWidth = Math.max(titleWidth * this.titleScale, this.font.width(SUBTITLE));
		int textBlockHeight = this.font.lineHeight * this.titleScale + LINE_GAP + this.font.lineHeight;
		int headerHeight = Math.max(this.portraitHeight, textBlockHeight);
		int groupWidth = Math.min(this.contentWidth, textStart + textBlockWidth);
		int groupX = this.contentX + (this.contentWidth - groupWidth) / 2;

		this.portraitX = groupX;
		this.portraitY = top + (headerHeight - this.portraitHeight) / 2;
		this.titleX = groupX + textStart;
		this.titleY = top + (headerHeight - textBlockHeight) / 2;
		this.subtitleY = this.titleY + this.font.lineHeight * this.titleScale + LINE_GAP;
		this.dividerY = top + headerHeight + ZniTheme.GAP;

		// Footer
		int buttonWidth = Math.min(MAX_BUTTON_WIDTH, this.contentWidth);
		int buttonY = this.height - bottomMargin - ZniTheme.BUTTON_HEIGHT;

		// Body: intro gets what it needs, as long as the mods section keeps its minimum height.
		int bodyTop = this.dividerY + 2 + ZniTheme.GAP + 2;
		int bodyBottom = buttonY - ZniTheme.GAP - 2;
		int bodyHeight = Math.max(0, bodyBottom - bodyTop);
		int lineStep = this.font.lineHeight + LINE_GAP;
		List<ZniRegisteredMod> mods = ZniModRegistry.getMods();
		int modsMin = InstalledModsSection.preferredMinHeight(this.font, !mods.isEmpty());

		int introSpace = bodyHeight - modsMin - ZniTheme.GAP - 2 * ZniTheme.PADDING;
		int maxIntroLines = Math.max(0, (introSpace + LINE_GAP) / lineStep);
		this.introLines = ZniDraw.wrap(this.font, INTRO, this.contentWidth - 2 * ZniTheme.PADDING, maxIntroLines);
		this.introY = bodyTop;
		this.introHeight = this.introLines.isEmpty() ? 0 : this.introLines.size() * lineStep - LINE_GAP + 2 * ZniTheme.PADDING;

		int modsY = this.introHeight > 0 ? this.introY + this.introHeight + ZniTheme.GAP : bodyTop;
		int modsHeight = bodyBottom - modsY;

		// Mod cards are added before Done so Tab/controller focus runs top to bottom.
		this.modsSection = new InstalledModsSection(mods, this);
		this.modsSection.init(this.font, this.contentX, modsY, this.contentWidth, modsHeight, this::addWidget);

		ZniButton done = new ZniButton((this.width - buttonWidth) / 2, buttonY, buttonWidth, ZniTheme.BUTTON_HEIGHT,
				CommonComponents.GUI_DONE, button -> this.onClose());
		this.addRenderableWidget(done);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Keep vanilla's blur/panorama, then lay the ZniStudios backdrop on top of it.
		super.extractBackground(graphics, mouseX, mouseY, delta);
		graphics.fill(0, 0, this.width, this.height, ZniTheme.BACKGROUND);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		this.extractHeader(graphics);
		ZniDraw.goldDivider(graphics, this.contentX, this.dividerY, this.contentWidth);

		if (this.introHeight > 0) {
			ZniDraw.panel(graphics, this.contentX, this.introY, this.contentWidth, this.introHeight);
			int lineY = this.introY + ZniTheme.PADDING;
			for (FormattedCharSequence line : this.introLines) {
				graphics.text(this.font, line, this.contentX + ZniTheme.PADDING, lineY, ZniTheme.TEXT_PRIMARY, false);
				lineY += this.font.lineHeight + LINE_GAP;
			}
		}

		this.modsSection.extract(graphics, this.font, mouseX, mouseY, delta);

		// Widgets (the Done button) are drawn last so they sit above the panels.
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private void extractHeader(GuiGraphicsExtractor graphics) {
		if (this.showCharacter) {
			ZniBranding.drawCharacter(graphics, this.portraitX, this.portraitY, 2);
		} else {
			ZniBranding.drawFace(graphics, this.portraitX, this.portraitY, this.portraitWidth);
		}

		int textWidth = this.contentX + this.contentWidth - this.titleX;
		FormattedCharSequence title = ZniDraw.singleLine(this.font, TITLE, textWidth / this.titleScale);
		if (this.titleScale == 1) {
			graphics.text(this.font, title, this.titleX, this.titleY, ZniTheme.GOLD_ACCENT, true);
		} else {
			// Whole-number scaling keeps the pixel font crisp.
			graphics.pose().pushMatrix();
			graphics.pose().translate(this.titleX, this.titleY);
			graphics.pose().scale(this.titleScale, this.titleScale);
			graphics.text(this.font, title, 0, 0, ZniTheme.GOLD_ACCENT, true);
			graphics.pose().popMatrix();
		}

		FormattedCharSequence subtitle = ZniDraw.singleLine(this.font, SUBTITLE, textWidth);
		graphics.text(this.font, subtitle, this.titleX, this.subtitleY, ZniTheme.TEXT_SECONDARY, false);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return this.modsSection.mouseScrolled(mouseX, mouseY, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.parent);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
