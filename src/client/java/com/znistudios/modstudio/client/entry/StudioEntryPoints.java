package com.znistudios.modstudio.client.entry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jspecify.annotations.Nullable;

import com.znistudios.modstudio.client.screen.ModStudioScreen;
import com.znistudios.modstudio.client.ui.render.ZniBranding;
import com.znistudios.modstudio.client.ui.theme.ZniTheme;
import com.znistudios.modstudio.client.ui.widget.ZniIconButton;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * The Zni's Mod Studio face button in vanilla menus. Both menus use the same button
 * ({@link #createButton}) and open the same screen ({@link #openModStudio}).
 *
 * <ul>
 *     <li><b>Pause menu</b>: {@code PauseScreenMixin} adds the button to vanilla's icon-button row.
 *     If that injection ever fails, {@link #addToPauseScreen} places it beside "Back to Game".</li>
 *     <li><b>Title screen</b>: {@link #addToTitleScreen} finds the row of small icon buttons
 *     (language, accessibility, Mod Menu's icon, ...) after the screen is built, appends the
 *     button and re-centers the row. No mixin is needed, and it re-runs on every resize or
 *     GUI-scale change because Fabric fires {@code AFTER_INIT} then too.</li>
 * </ul>
 */
public final class StudioEntryPoints {
	private static final Component OPEN_LABEL = Component.translatable("znis_mod_studio.button.open");
	private static final int SIZE = ZniTheme.BUTTON_HEIGHT;
	/** Spacing used when the surrounding row does not tell us its own spacing. */
	private static final int DEFAULT_GAP = 4;
	/** Widgets at most this big and square count as "small icon buttons". */
	private static final int MAX_ICON_BUTTON = 24;

	private StudioEntryPoints() {
	}

	public static void register() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof TitleScreen) {
				addToTitleScreen(screen);
			} else if (screen instanceof PauseScreen) {
				addToPauseScreen(screen);
			}
		});
	}

	/** Opens the Mod Studio home screen; {@code parent} is restored when it closes. */
	public static void openModStudio(@Nullable Screen parent) {
		Minecraft.getInstance().gui.setScreen(new ModStudioScreen(parent));
	}

	/** The shared entry button: a 20x20 ZniStudios icon button showing the Zni face, with gold hover/focus. */
	public static ZniIconButton createButton(Screen parent) {
		ZniBranding.refresh();
		return new EntryButton(parent);
	}

	/** Marker type so the entry button can be found without mistaking other ZniIconButtons for it. */
	private static final class EntryButton extends ZniIconButton {
		private EntryButton(Screen parent) {
			super(0, 0, SIZE, 16, OPEN_LABEL, ZniBranding::drawFace, button -> openModStudio(parent));
		}
	}

	// ---------------------------------------------------------------- pause menu

	private static void addToPauseScreen(Screen screen) {
		List<AbstractWidget> widgets = Screens.getWidgets(screen);
		if (containsStudioButton(widgets)) {
			return; // The mixin already placed it in the icon row.
		}

		AbstractWidget anchor = findByTranslationKey(widgets, "menu.returnToGame");
		if (anchor == null) {
			return; // Pause menu is hidden (F3+Esc); don't add anything.
		}

		ZniIconButton button = createButton(screen);
		button.setPosition(anchor.getX() + anchor.getWidth() + DEFAULT_GAP, anchor.getY());
		widgets.add(button);
	}

	// ---------------------------------------------------------------- title screen

	private static void addToTitleScreen(Screen screen) {
		List<AbstractWidget> widgets = Screens.getWidgets(screen);
		if (containsStudioButton(widgets)) {
			return;
		}

		ZniIconButton button = createButton(screen);
		List<AbstractWidget> row = findIconRow(widgets);

		if (!row.isEmpty() && (tryAppendAndRecenter(screen, widgets, row, button) || tryBesideRow(screen, widgets, row, button))) {
			widgets.add(button);
			return;
		}

		// No usable icon row: sit beside the main button column, next to "Options...".
		AbstractWidget anchor = findByTranslationKey(widgets, "menu.options");
		if (anchor == null) {
			anchor = findByTranslationKey(widgets, "menu.singleplayer");
		}
		if (anchor != null) {
			button.setPosition(anchor.getX() - DEFAULT_GAP - SIZE, anchor.getY());
			if (fits(screen, widgets, button)) {
				widgets.add(button);
				return;
			}
		}

		// Last resort: top-left corner, which vanilla leaves empty.
		button.setPosition(DEFAULT_GAP, DEFAULT_GAP);
		widgets.add(button);
	}

	/** The largest group of visible small square buttons that share one row (same Y). */
	private static List<AbstractWidget> findIconRow(List<AbstractWidget> widgets) {
		List<AbstractWidget> best = List.of();
		for (AbstractWidget candidate : widgets) {
			if (!isIconButton(candidate)) {
				continue;
			}
			List<AbstractWidget> row = new ArrayList<>();
			for (AbstractWidget other : widgets) {
				if (isIconButton(other) && other.getY() == candidate.getY() && other.getHeight() == candidate.getHeight()) {
					row.add(other);
				}
			}
			if (row.size() > best.size()) {
				best = row;
			}
		}
		List<AbstractWidget> sorted = new ArrayList<>(best);
		sorted.sort(Comparator.comparingInt(AbstractWidget::getX));
		return sorted;
	}

	/**
	 * For an evenly spaced row (vanilla's centered icon row): add the button to the end and
	 * re-center the whole row around its original center, keeping the row's own spacing.
	 */
	private static boolean tryAppendAndRecenter(Screen screen, List<AbstractWidget> widgets, List<AbstractWidget> row, ZniIconButton button) {
		if (row.size() < 2) {
			return false;
		}

		int gap = -1;
		for (int i = 1; i < row.size(); i++) {
			int between = row.get(i).getX() - (row.get(i - 1).getX() + row.get(i - 1).getWidth());
			if (between < 0 || (gap >= 0 && between != gap)) {
				return false; // Overlapping or irregular: not a simple row, don't touch it.
			}
			gap = between;
		}
		if (gap > 3 * SIZE) {
			return false; // Buttons flanking something else, not one row.
		}

		AbstractWidget first = row.get(0);
		AbstractWidget last = row.get(row.size() - 1);
		int center = (first.getX() + last.getX() + last.getWidth()) / 2;
		int total = gap + SIZE;
		for (AbstractWidget widget : row) {
			total += widget.getWidth() + gap;
		}
		total -= gap;

		int[] originalX = new int[row.size()];
		int x = center - total / 2;
		for (int i = 0; i < row.size(); i++) {
			originalX[i] = row.get(i).getX();
			row.get(i).setX(x);
			x += row.get(i).getWidth() + gap;
		}
		button.setPosition(x, first.getY() + (first.getHeight() - SIZE) / 2);

		List<AbstractWidget> moved = new ArrayList<>(row);
		moved.add(button);
		boolean ok = true;
		for (AbstractWidget widget : moved) {
			if (!fits(screen, widgets, widget)) {
				ok = false;
				break;
			}
		}
		if (!ok) {
			for (int i = 0; i < row.size(); i++) {
				row.get(i).setX(originalX[i]);
			}
		}
		return ok;
	}

	/** For a single or irregular row: put the button just right of it, or else just left of it. */
	private static boolean tryBesideRow(Screen screen, List<AbstractWidget> widgets, List<AbstractWidget> row, ZniIconButton button) {
		AbstractWidget first = row.get(0);
		AbstractWidget last = row.get(row.size() - 1);
		int y = last.getY() + (last.getHeight() - SIZE) / 2;

		button.setPosition(last.getX() + last.getWidth() + DEFAULT_GAP, y);
		if (fits(screen, widgets, button)) {
			return true;
		}
		button.setPosition(first.getX() - DEFAULT_GAP - SIZE, y);
		return fits(screen, widgets, button);
	}

	/** On screen and not overlapping any other visible widget. */
	private static boolean fits(Screen screen, List<AbstractWidget> widgets, AbstractWidget widget) {
		if (widget.getX() < 0 || widget.getY() < 0
				|| widget.getX() + widget.getWidth() > screen.width
				|| widget.getY() + widget.getHeight() > screen.height) {
			return false;
		}
		for (AbstractWidget other : widgets) {
			if (other != widget && other.visible && overlaps(other, widget)) {
				return false;
			}
		}
		return true;
	}

	private static boolean overlaps(AbstractWidget a, AbstractWidget b) {
		return a.getX() < b.getX() + b.getWidth() && b.getX() < a.getX() + a.getWidth()
				&& a.getY() < b.getY() + b.getHeight() && b.getY() < a.getY() + a.getHeight();
	}

	private static boolean isIconButton(AbstractWidget widget) {
		return widget.visible && widget.getWidth() == widget.getHeight() && widget.getWidth() <= MAX_ICON_BUTTON
				&& !(widget instanceof EntryButton);
	}

	// ---------------------------------------------------------------- helpers

	private static boolean containsStudioButton(List<AbstractWidget> widgets) {
		for (AbstractWidget widget : widgets) {
			if (widget instanceof EntryButton) {
				return true;
			}
		}
		return false;
	}

	private static @Nullable AbstractWidget findByTranslationKey(List<AbstractWidget> widgets, String key) {
		for (AbstractWidget widget : widgets) {
			if (widget.getMessage().getContents() instanceof TranslatableContents contents && contents.getKey().equals(key)) {
				return widget;
			}
		}
		return null;
	}
}
