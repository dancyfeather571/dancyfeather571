package com.znistudios.modstudio.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.znistudios.modstudio.ZnisModStudio;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

/**
 * Discovers ZniStudios mods through Fabric Loader. No mod is hard-coded here.
 *
 * <p>A mod opts in with nothing but its {@code fabric.mod.json}:
 * <pre>{@code
 * "custom": {
 *   "zni_mod_studio": {
 *     "registered": true
 *   }
 * }
 * }</pre>
 * Mods without that block, or with a missing/malformed one, are ignored. A problem reading one
 * mod's metadata never affects the others and never crashes the game.
 *
 * <p>The loaded mod set cannot change while the game runs, so the scan happens once, lazily.
 */
public final class ZniModRegistry {
	/** The key under {@code custom} in {@code fabric.mod.json}. */
	public static final String METADATA_KEY = "zni_mod_studio";
	public static final String REGISTERED_KEY = "registered";

	private static final int ICON_SIZE = 64;

	private static List<ZniRegisteredMod> mods;

	private ZniModRegistry() {
	}

	/** All registered ZniStudios mods, sorted by display name. */
	public static synchronized List<ZniRegisteredMod> getMods() {
		if (mods == null) {
			mods = scan();
		}
		return mods;
	}

	public static Optional<ZniRegisteredMod> get(String modId) {
		for (ZniRegisteredMod mod : getMods()) {
			if (mod.modId().equals(modId)) {
				return Optional.of(mod);
			}
		}
		return Optional.empty();
	}

	public static boolean isRegistered(String modId) {
		return get(modId).isPresent();
	}

	private static List<ZniRegisteredMod> scan() {
		List<ZniRegisteredMod> found = new ArrayList<>();
		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			try {
				ModMetadata metadata = container.getMetadata();
				if (isOptedIn(metadata)) {
					found.add(read(container, metadata));
				}
			} catch (RuntimeException e) {
				ZnisModStudio.LOGGER.warn("Skipping a mod with unreadable metadata", e);
			}
		}
		found.sort(Comparator.comparing((ZniRegisteredMod mod) -> mod.name().toLowerCase(Locale.ROOT))
				.thenComparing(ZniRegisteredMod::modId));
		ZnisModStudio.LOGGER.info("Found {} registered ZniStudios mod(s)", found.size());
		return List.copyOf(found);
	}

	private static boolean isOptedIn(ModMetadata metadata) {
		CustomValue block = metadata.getCustomValue(METADATA_KEY);
		if (block == null) {
			return false;
		}
		if (block.getType() != CustomValue.CvType.OBJECT) {
			ZnisModStudio.LOGGER.warn("Mod '{}': custom.{} must be an object; ignoring it", metadata.getId(), METADATA_KEY);
			return false;
		}
		CustomValue registered = block.getAsObject().get(REGISTERED_KEY);
		if (registered == null) {
			return false;
		}
		if (registered.getType() != CustomValue.CvType.BOOLEAN) {
			ZnisModStudio.LOGGER.warn("Mod '{}': custom.{}.{} must be true or false; ignoring it",
					metadata.getId(), METADATA_KEY, REGISTERED_KEY);
			return false;
		}
		return registered.getAsBoolean();
	}

	private static ZniRegisteredMod read(ModContainer container, ModMetadata metadata) {
		String modId = metadata.getId();
		String name = orEmpty(metadata.getName()).isBlank() ? modId : metadata.getName().strip();
		String version = metadata.getVersion() == null ? "" : orEmpty(metadata.getVersion().getFriendlyString());
		String description = orEmpty(metadata.getDescription()).replaceAll("\\s*[\\r\\n]+\\s*", " ").strip();

		List<String> authors = new ArrayList<>();
		if (metadata.getAuthors() != null) {
			for (Person person : metadata.getAuthors()) {
				if (person != null && person.getName() != null && !person.getName().isBlank()) {
					authors.add(person.getName().strip());
				}
			}
		}

		Optional<String> icon;
		try {
			icon = Objects.requireNonNullElse(metadata.getIconPath(ICON_SIZE), Optional.<String>empty());
		} catch (RuntimeException e) {
			icon = Optional.empty();
		}

		return new ZniRegisteredMod(modId, name, version, description, List.copyOf(authors), icon, container);
	}

	private static String orEmpty(String value) {
		return value == null ? "" : value;
	}
}
