import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Derives the ZniStudios branding textures from the source skin (art/zni_skin_source.png).
 *
 * Run from the project root with:  java art/BakeBranding.java
 *
 * Pure pixel copies only: parts are cropped from the standard 64x64 skin layout, overlay
 * layers are alpha-composited on top of their base layer, and enlargements use whole-number
 * nearest-neighbour scaling. No smoothing, no resampling, transparency preserved.
 */
public class BakeBranding {
	static final String OUT = "src/main/resources/assets/znis_mod_studio/";

	public static void main(String[] args) throws IOException {
		BufferedImage skin = ImageIO.read(new File("art/zni_skin_source.png"));
		if (skin.getWidth() != 64 || skin.getHeight() != 64) {
			throw new IllegalStateException("Expected a 64x64 skin, got " + skin.getWidth() + "x" + skin.getHeight());
		}

		// Head: front face (8,8) + hat overlay front (40,8).
		BufferedImage face = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
		paste(skin, face, 8, 8, 8, 8, 0, 0);
		paste(skin, face, 40, 8, 8, 8, 0, 0);

		// Full body, front view, classic (4px) arms: 16x32.
		// Character's right side appears on the viewer's left.
		BufferedImage body = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
		int[][] parts = {
				// srcX, srcY, w, h, dstX, dstY  (base layer, then its overlay)
				{8, 8, 8, 8, 4, 0}, {40, 8, 8, 8, 4, 0},       // head + hat
				{20, 20, 8, 12, 4, 8}, {20, 36, 8, 12, 4, 8},  // body + jacket
				{44, 20, 4, 12, 0, 8}, {44, 36, 4, 12, 0, 8},  // right arm + sleeve
				{36, 52, 4, 12, 12, 8}, {52, 52, 4, 12, 12, 8}, // left arm + sleeve
				{4, 20, 4, 12, 4, 20}, {4, 36, 4, 12, 4, 20},  // right leg + pants
				{20, 52, 4, 12, 8, 20}, {4, 52, 4, 12, 8, 20}, // left leg + pants
		};
		for (int[] p : parts) {
			paste(skin, body, p[0], p[1], p[2], p[3], p[4], p[5]);
		}

		write(scale(face, 1), "textures/gui/zni_face_8.png");
		write(scale(face, 2), "textures/gui/zni_face_16.png");
		write(scale(face, 4), "textures/gui/zni_face_32.png");
		write(scale(body, 1), "textures/gui/zni_character.png");
		write(scale(body, 2), "textures/gui/zni_character_2x.png");
		write(scale(face, 16), "icon.png");
	}

	/** Alpha-composites a region of src onto dst ("source over"), pixel for pixel. */
	static void paste(BufferedImage src, BufferedImage dst, int sx, int sy, int w, int h, int dx, int dy) {
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int top = src.getRGB(sx + x, sy + y);
				int ta = top >>> 24;
				if (ta == 0) {
					continue;
				}
				if (ta == 255) {
					dst.setRGB(dx + x, dy + y, top);
					continue;
				}
				int bottom = dst.getRGB(dx + x, dy + y);
				int ba = bottom >>> 24;
				float a = ta / 255f;
				float outA = a + ba / 255f * (1 - a);
				int out = Math.round(outA * 255) << 24;
				for (int shift = 0; shift <= 16; shift += 8) {
					float tc = (top >> shift & 0xFF) * a;
					float bc = (bottom >> shift & 0xFF) * (ba / 255f) * (1 - a);
					int c = outA == 0 ? 0 : Math.round((tc + bc) / outA);
					out |= Math.min(255, c) << shift;
				}
				dst.setRGB(dx + x, dy + y, out);
			}
		}
	}

	/** Whole-number nearest-neighbour enlargement: every source pixel becomes a factor x factor block. */
	static BufferedImage scale(BufferedImage src, int factor) {
		BufferedImage out = new BufferedImage(src.getWidth() * factor, src.getHeight() * factor, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < out.getHeight(); y++) {
			for (int x = 0; x < out.getWidth(); x++) {
				out.setRGB(x, y, src.getRGB(x / factor, y / factor));
			}
		}
		return out;
	}

	static void write(BufferedImage image, String path) throws IOException {
		File file = new File(OUT + path);
		file.getParentFile().mkdirs();
		ImageIO.write(image, "png", file);
		System.out.println("wrote " + file + " (" + image.getWidth() + "x" + image.getHeight() + ")");
	}
}
