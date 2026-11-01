package dev.nebula.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

/** Replacement for the vanilla title screen: dark background, nebula ring, centered menu. */
public class NebulaTitleScreen extends Screen {
	private static final Identifier LOGO = Identifier.of(NebulaVisuals.ID, "textures/gui/logo.png");
	private static final int PARTICLES = 70;

	private final long start = System.currentTimeMillis();
	private final float[] px = new float[PARTICLES];
	private final float[] py = new float[PARTICLES];
	private final float[] ps = new float[PARTICLES];
	private boolean wasDown = true;

	public NebulaTitleScreen() {
		super(Text.literal("Nebula Visuals"));
		Random r = new Random(7);
		for (int i = 0; i < PARTICLES; i++) {
			px[i] = r.nextFloat();
			py[i] = r.nextFloat();
			ps[i] = 0.01f + r.nextFloat() * 0.03f;
		}
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		NebulaConfig c = NebulaConfig.get();
		float t = (System.currentTimeMillis() - start) / 1000f;

		long h = mc.getWindow().getHandle();
		boolean down = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean clicked = down && !wasDown;
		wasDown = down;

		ctx.fill(0, 0, width, height, 0xFF05050A);

		// drifting pixels
		for (int i = 0; i < PARTICLES; i++) {
			float fx = (px[i] + t * ps[i] * 0.4f) % 1f;
			float fy = (py[i] - t * ps[i]) % 1f;
			if (fy < 0f) fy += 1f;
			int x = (int) (fx * width);
			int y = (int) (fy * height);
			int a = 40 + (int) (80 * (0.5f + 0.5f * Math.sin(t * 2f + i)));
			int col = Gfx.alpha(i % 2 == 0 ? c.color1() : c.color2(), a);
			int s = i % 5 == 0 ? 3 : 2;
			ctx.fill(x, y, x + s, y + s, col);
		}

		int midX = width / 2;
		int logoY = Math.max(6, height / 2 - 100);
		int ringX = midX;
		int ringY = logoY + 32;

		// rotating nebula ring
		for (int ring = 0; ring < 2; ring++) {
			float rot = t * (ring == 0 ? 0.25f : -0.18f) + ring * 1.2f;
			float a = 70 + ring * 14;
			float b = 24 + ring * 6;
			for (int i = 0; i < 260; i++) {
				float ang = i / 260f * (float) (Math.PI * 2);
				float ex = (float) Math.cos(ang) * a;
				float ey = (float) Math.sin(ang) * b;
				int x = ringX + (int) (ex * Math.cos(rot) - ey * Math.sin(rot));
				int y = ringY + (int) (ex * Math.sin(rot) + ey * Math.cos(rot));
				int al = 20 + (int) (90 * (0.5f + 0.5f * Math.sin(ang * 2f + t)));
				int col = Gfx.alpha(Gfx.lerp(c.color1(), c.color2(), i / 260f), al);
				ctx.fill(x, y, x + 2, y + 2, col);
			}
		}

		ctx.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO, midX - 32, logoY, 0f, 0f, 64, 64, 64, 64);

		// title with soft glow
		int ty = logoY + 70;
		Text title = Text.literal("NEBULA VISUALS");
		int glow = Gfx.alpha(c.color1(), 70);
		ctx.drawCenteredTextWithShadow(textRenderer, title, midX - 1, ty, glow);
		ctx.drawCenteredTextWithShadow(textRenderer, title, midX + 1, ty, glow);
		ctx.drawCenteredTextWithShadow(textRenderer, title, midX, ty - 1, glow);
		ctx.drawCenteredTextWithShadow(textRenderer, title, midX, ty + 1, glow);
		ctx.drawCenteredTextWithShadow(textRenderer, title, midX, ty, 0xFFFFFFFF);

		// buttons
		int bw = 150, bh = 16, gap = 3;
		int bx = midX - bw / 2;
		int by = ty + 16;
		if (btn(ctx, c, bx, by, bw, bh, Gfx.t("Одиночная игра", "Singleplayer"), mouseX, mouseY, clicked)) {
			mc.execute(() -> mc.setScreen(new SelectWorldScreen(this)));
		}
		by += bh + gap;
		if (btn(ctx, c, bx, by, bw, bh, Gfx.t("Сетевая игра", "Multiplayer"), mouseX, mouseY, clicked)) {
			mc.execute(() -> mc.setScreen(new MultiplayerScreen(this)));
		}
		by += bh + gap;
		if (btn(ctx, c, bx, by, bw, bh, Gfx.t("Настройки Nebula", "Nebula settings"), mouseX, mouseY, clicked)) {
			mc.execute(() -> mc.setScreen(new NebulaMenu(this)));
		}
		by += bh + gap;
		int half = (bw - gap) / 2;
		if (btn(ctx, c, bx, by, half, bh, Gfx.t("Настройки", "Options"), mouseX, mouseY, clicked)) {
			mc.execute(() -> mc.setScreen(new OptionsScreen(this, mc.options)));
		}
		if (btn(ctx, c, bx + half + gap, by, half, bh, Gfx.t("Выход", "Quit"), mouseX, mouseY, clicked)) {
			mc.execute(mc::scheduleStop);
		}

		ctx.drawTextWithShadow(textRenderer, "Nebula Visuals 1.0  |  Minecraft 1.21.11", 4, height - 11, 0xFF555566);
	}

	private boolean btn(DrawContext ctx, NebulaConfig c, int x, int y, int w, int h, String label,
	                    int mx, int my, boolean clicked) {
		boolean hov = Gfx.in(mx, my, x, y, w, h);
		ctx.fill(x, y, x + w, y + h, hov ? 0xC0242438 : 0xB0121220);
		if (hov) {
			ctx.fill(x, y, x + 2, y + h, c.color1());
			ctx.fill(x + w - 2, y, x + w, y + h, c.color2());
		}
		ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(label), x + w / 2, y + h / 2 - 4, hov ? 0xFFFFFFFF : 0xFFBBBBCC);
		return clicked && hov;
	}
}
