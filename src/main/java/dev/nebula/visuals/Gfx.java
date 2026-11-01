package dev.nebula.visuals;

import net.minecraft.client.gui.DrawContext;

final class Gfx {
	private Gfx() {}

	static String t(String ru, String en) {
		return "en".equals(NebulaConfig.get().lang) ? en : ru;
	}

	static boolean in(int mx, int my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	static int alpha(int color, int a) {
		return (a << 24) | (color & 0xFFFFFF);
	}

	static int lerp(int a, int b, float t) {
		int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
		int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
		int r = (int) (ar + (br - ar) * t);
		int g = (int) (ag + (bg - ag) * t);
		int bl = (int) (ab + (bb - ab) * t);
		return 0xFF000000 | (r << 16) | (g << 8) | bl;
	}

	static void rect(DrawContext ctx, int x, int y, int w, int h, int color) {
		ctx.fill(x, y, x + w, y + h, color);
	}

	static void round(DrawContext ctx, int x, int y, int w, int h, int color) {
		ctx.fill(x + 1, y, x + w - 1, y + h, color);
		ctx.fill(x, y + 1, x + w, y + h - 1, color);
	}

	static void outline(DrawContext ctx, int x, int y, int w, int h, int color) {
		ctx.fill(x, y, x + w, y + 1, color);
		ctx.fill(x, y + h - 1, x + w, y + h, color);
		ctx.fill(x, y, x + 1, y + h, color);
		ctx.fill(x + w - 1, y, x + w, y + h, color);
	}

	static void hGradient(DrawContext ctx, int x, int y, int w, int h, int c1, int c2) {
		for (int i = 0; i < w; i++) {
			ctx.fill(x + i, y, x + i + 1, y + h, lerp(c1, c2, i / (float) Math.max(1, w - 1)));
		}
	}

	static int hsv(float h, float s, float v) {
		h = h - (float) Math.floor(h);
		float c = v * s;
		float hh = h * 6f;
		float x = c * (1f - Math.abs(hh % 2f - 1f));
		float m = v - c;
		float r, g, b;
		switch (((int) hh) % 6) {
			case 0: r = c; g = x; b = 0; break;
			case 1: r = x; g = c; b = 0; break;
			case 2: r = 0; g = c; b = x; break;
			case 3: r = 0; g = x; b = c; break;
			case 4: r = x; g = 0; b = c; break;
			default: r = c; g = 0; b = x; break;
		}
		return 0xFF000000
			| (Math.round((r + m) * 255) << 16)
			| (Math.round((g + m) * 255) << 8)
			| Math.round((b + m) * 255);
	}
}
