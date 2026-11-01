package dev.nebula.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.function.Consumer;

/** Right Shift menu: sidebar tabs, panels, toggles, sliders, two accent colors, language switch. */
public class NebulaMenu extends Screen {
	private static final Identifier LOGO = Identifier.of(NebulaVisuals.ID, "textures/gui/logo_small.png");
	private static final int HUE_ID = 9000;
	private static int tab = 0;

	private final Screen parent;
	private MinecraftClient mc;
	private NebulaConfig c;

	private boolean wasDown = true, shiftWas = true, down, clicked;
	private int mx, my, uid, dragId = -1, colorTarget = 1;
	private Runnable pendingRelease;
	private int cx, cy, cw;
	private float renderDist = -1f;

	public NebulaMenu(Screen parent) {
		super(Text.literal("Nebula Visuals"));
		this.parent = parent;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		NebulaConfig.get().save();
		MinecraftClient.getInstance().setScreen(parent);
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		mc = MinecraftClient.getInstance();
		c = NebulaConfig.get();
		mx = mouseX;
		my = mouseY;
		uid = 0;

		long h = mc.getWindow().getHandle();
		down = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		clicked = down && !wasDown;
		boolean shift = GLFW.glfwGetKey(h, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		if (shift && !shiftWas) {
			close();
			return;
		}
		shiftWas = shift;

		ctx.fill(0, 0, width, height, 0xAA000000);

		int W = Math.min(width - 16, 440);
		int H = Math.min(height - 16, 262);
		int x0 = (width - W) / 2;
		int y0 = (height - H) / 2;

		// header: logo
		box(ctx, x0, y0, 36, 36);
		ctx.drawTexture(RenderPipelines.GUI_TEXTURED, LOGO, x0 + 2, y0 + 2, 0f, 0f, 32, 32, 32, 32);

		// header: language
		box(ctx, x0 + 40, y0, 120, 36);
		langButton(ctx, x0 + 46, y0 + 10, 52, 16, "Русский", "ru");
		langButton(ctx, x0 + 102, y0 + 10, 52, 16, "English", "en");

		// header: colors
		int bx = x0 + 164;
		int bw = W - 164;
		box(ctx, bx, y0, bw, 36);
		colorButton(ctx, bx + 6, y0 + 4, 56, 12, "Color 1", 1);
		colorButton(ctx, bx + 66, y0 + 4, 56, 12, "Color 2", 2);
		hueBar(ctx, bx + 6, y0 + 21, bw - 12, 9);

		// sidebar
		box(ctx, x0, y0 + 40, 36, H - 40);
		ItemStack[] icons = {
			new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.CLOCK),
			new ItemStack(Items.REDSTONE), new ItemStack(Items.COMPASS)
		};
		for (int i = 0; i < icons.length; i++) {
			int ix = x0 + 4, iy = y0 + 44 + i * 30;
			boolean hov = Gfx.in(mx, my, ix, iy, 28, 26);
			if (tab == i) {
				Gfx.round(ctx, ix, iy, 28, 26, Gfx.alpha(c.color1(), 0x70));
				Gfx.outline(ctx, ix, iy, 28, 26, c.color1());
			} else if (hov) {
				Gfx.round(ctx, ix, iy, 28, 26, 0x33FFFFFF);
			}
			ctx.drawItem(icons[i], ix + 6, iy + 5);
			if (clicked && hov) tab = i;
		}

		// content
		int ax = x0 + 40;
		int ay = y0 + 40;
		int aw = W - 40;
		int ah = H - 40 - 14;
		int pw = (aw - 4) / 2;
		renderTab(ctx, ax, ay, pw, ah);

		// footer
		ctx.drawTextWithShadow(mc.textRenderer, "Nebula Visuals  |  RShift", ax + 2, y0 + H - 10, 0xFF8888AA);

		wasDown = down;
		if (!down) {
			if (pendingRelease != null) {
				pendingRelease.run();
				pendingRelease = null;
			}
			dragId = -1;
		}
	}

	// ---------------------------------------------------------------- tabs

	private void renderTab(DrawContext ctx, int ax, int ay, int pw, int ah) {
		int rx = ax + pw + 4;
		switch (tab) {
			case 0 -> {
				panel(ctx, ax, ay, pw, ah, Gfx.t("Бой", "Combat visuals"));
				toggle(ctx, Gfx.t("Тоггл спринт", "Toggle sprint"), c.toggleSprint, v -> c.toggleSprint = v);
				toggle(ctx, Gfx.t("Цифры урона", "Damage numbers"), c.damageNumbers, v -> c.damageNumbers = v);
				toggle(ctx, Gfx.t("Маркер попадания", "Hit marker"), c.hitMarker, v -> c.hitMarker = v);
				slider(ctx, Gfx.t("Время цифр, мс", "Popup time, ms"), 400, 3000, c.popupMs, 0, v -> c.popupMs = Math.round(v), null);
				panel(ctx, rx, ay, pw, ah, Gfx.t("Инфо", "Info"));
				hudToggle(ctx, "target", Gfx.t("Инфо о цели", "Target info"));
				hudToggle(ctx, "cooldown", Gfx.t("Кулдаун атаки", "Attack cooldown"));
				hudToggle(ctx, "keystrokes", Gfx.t("Нажатия клавиш", "Keystrokes"));
				hudToggle(ctx, "cps", "CPS");
			}
			case 1 -> {
				panel(ctx, ax, ay, pw, ah, Gfx.t("Видимость", "Visibility"));
				toggle(ctx, "Fullbright", c.fullbright, v -> c.fullbright = v);
				toggle(ctx, Gfx.t("Свечение игроков", "Player glow"), c.glowEsp, v -> c.glowEsp = v);
				panel(ctx, rx, ay, pw, ah, Gfx.t("Важно", "Note"));
				note(ctx, Gfx.t("Свечение видно сквозь стены.", "Glow is visible through walls."));
				note(ctx, Gfx.t("На многих серверах это", "Many servers treat this"));
				note(ctx, Gfx.t("запрещено. Проверьте", "as cheating. Check the"));
				note(ctx, Gfx.t("правила своего сервера.", "rules of your server."));
			}
			case 2 -> {
				panel(ctx, ax, ay, pw, ah, Gfx.t("Элементы HUD", "HUD elements"));
				toggle(ctx, "HUD", c.hudEnabled, v -> c.hudEnabled = v);
				hudToggle(ctx, "fps", "FPS");
				hudToggle(ctx, "ping", Gfx.t("Пинг", "Ping"));
				hudToggle(ctx, "coords", Gfx.t("Координаты", "Coordinates"));
				hudToggle(ctx, "armor", Gfx.t("Броня", "Armor"));
				panel(ctx, rx, ay, pw, ah, Gfx.t("Редактор", "Editor"));
				button(ctx, Gfx.t("Перемещать элементы", "Move elements"), () -> mc.setScreen(new HudEditorScreen(this)));
				button(ctx, Gfx.t("Сбросить позиции", "Reset positions"), () -> { c.resetPositions(); c.save(); });
			}
			case 3 -> {
				panel(ctx, ax, ay, pw, ah, Gfx.t("Производительность", "Performance"));
				button(ctx, Gfx.t("Режим макс. FPS", "Max FPS mode"), () -> PerformanceMode.apply(mc));
				if (renderDist < 0f) renderDist = PerformanceMode.viewDistance(mc);
				slider(ctx, Gfx.t("Дальность", "Render dist."), 2, 32, renderDist, 0,
					v -> renderDist = v, () -> PerformanceMode.setViewDistance(mc, Math.round(renderDist)));
				note(ctx, Gfx.t("Для большего FPS: Sodium,", "For more FPS: Sodium,"));
				note(ctx, "Lithium, FerriteCore");
				panel(ctx, rx, ay, pw, ah, Gfx.t("Опции", "Options"));
				toggle(ctx, Gfx.t("Тени сущностей", "Entity shadows"), PerformanceMode.shadows(mc), v -> PerformanceMode.setShadows(mc, v));
				toggle(ctx, Gfx.t("Мин. частицы", "Minimal particles"), PerformanceMode.minimalParticles(mc), v -> PerformanceMode.setMinimalParticles(mc, v));
				toggle(ctx, "VSync", PerformanceMode.vsync(mc), v -> PerformanceMode.setVsync(mc, v));
			}
			default -> {
				panel(ctx, ax, ay, pw, ah, Gfx.t("Общее", "General"));
				toggle(ctx, Gfx.t("Свой экран входа", "Custom title screen"), c.customTitle, v -> c.customTitle = v);
				toggle(ctx, Gfx.t("Фильтр чата", "Chat filter"), c.chatFilter, v -> c.chatFilter = v);
				note(ctx, Gfx.t("Слова для фильтра: файл", "Filter words: file"));
				note(ctx, "config/nebula_visuals.json");
				panel(ctx, rx, ay, pw, ah, Gfx.t("Язык", "Language"));
				button(ctx, "Русский", () -> c.lang = "ru");
				button(ctx, "English", () -> c.lang = "en");
			}
		}
	}

	private void hudToggle(DrawContext ctx, String key, String label) {
		NebulaConfig.Pos p = c.hud.get(key);
		if (p == null) return;
		toggle(ctx, label, p.enabled, v -> p.enabled = v);
	}

	// ---------------------------------------------------------------- widgets

	private void box(DrawContext ctx, int x, int y, int w, int h) {
		ctx.fill(x, y, x + w, y + h, 0xD0101018);
		Gfx.outline(ctx, x, y, w, h, Gfx.alpha(c.color1(), 0x50));
	}

	private void panel(DrawContext ctx, int x, int y, int w, int h, String title) {
		box(ctx, x, y, w, h);
		ctx.drawTextWithShadow(mc.textRenderer, title, x + 6, y + 5, c.color1());
		Gfx.hGradient(ctx, x + 4, y + 15, w - 8, 1, c.color1(), c.color2());
		cx = x + 6;
		cy = y + 20;
		cw = w - 12;
	}

	private void note(DrawContext ctx, String s) {
		ctx.drawTextWithShadow(mc.textRenderer, s, cx + 3, cy + 1, 0xFF8888AA);
		cy += 11;
	}

	private void toggle(DrawContext ctx, String label, boolean val, Consumer<Boolean> set) {
		int x = cx, y = cy, w = cw, h = 14;
		boolean hov = Gfx.in(mx, my, x, y, w, h);
		if (hov) ctx.fill(x, y, x + w, y + h, 0x22FFFFFF);
		ctx.drawTextWithShadow(mc.textRenderer, label, x + 3, y + 3, 0xFFDDDDDD);
		int bx = x + w - 14;
		Gfx.round(ctx, bx, y + 2, 11, 10, val ? c.color1() : 0xFF2A2A34);
		if (val) Gfx.rect(ctx, bx + 3, y + 5, 5, 4, 0xFFFFFFFF);
		if (clicked && hov) {
			set.accept(!val);
			c.save();
		}
		cy += 15;
	}

	private void button(DrawContext ctx, String label, Runnable action) {
		int x = cx, y = cy, w = cw, h = 14;
		boolean hov = Gfx.in(mx, my, x, y, w, h);
		Gfx.round(ctx, x, y, w, h, hov ? Gfx.alpha(c.color1(), 0x90) : 0xFF22222C);
		ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), x + w / 2, y + 3, 0xFFFFFFFF);
		if (clicked && hov) {
			action.run();
			c.save();
		}
		cy += 17;
	}

	private void slider(DrawContext ctx, String label, float min, float max, float val, int dec,
	                    Consumer<Float> set, Runnable onRelease) {
		int id = ++uid;
		int x = cx, y = cy, w = cw;
		ctx.drawTextWithShadow(mc.textRenderer, label, x + 3, y + 3, 0xFFDDDDDD);
		int sw = 44;
		int sx = x + w - sw - 32;
		boolean hov = Gfx.in(mx, my, sx - 2, y, sw + 4, 14);
		if (clicked && hov) {
			dragId = id;
			pendingRelease = onRelease;
		}
		if (dragId == id && down) {
			float t = Math.max(0f, Math.min(1f, (mx - sx) / (float) sw));
			float v = min + (max - min) * t;
			if (dec == 0) v = Math.round(v);
			set.accept(v);
			val = v;
		}
		float t = Math.max(0f, Math.min(1f, (val - min) / (max - min)));
		Gfx.rect(ctx, sx, y + 6, sw, 3, 0xFF2A2A34);
		Gfx.rect(ctx, sx, y + 6, (int) (sw * t), 3, c.color1());
		Gfx.rect(ctx, sx + (int) (sw * t) - 1, y + 3, 3, 9, 0xFFFFFFFF);
		ctx.drawTextWithShadow(mc.textRenderer, String.format(Locale.ROOT, "%." + dec + "f", val), sx + sw + 5, y + 3, 0xFFAAAAAA);
		cy += 15;
	}

	private void langButton(DrawContext ctx, int x, int y, int w, int h, String label, String code) {
		boolean hov = Gfx.in(mx, my, x, y, w, h);
		boolean active = code.equals(c.lang);
		ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), x + w / 2, y + 4, active ? 0xFFFFFFFF : 0xFF777788);
		if (active) Gfx.hGradient(ctx, x + 4, y + h - 1, w - 8, 1, c.color1(), c.color2());
		else if (hov) Gfx.rect(ctx, x + 4, y + h - 1, w - 8, 1, 0x55FFFFFF);
		if (clicked && hov) {
			c.lang = code;
			c.save();
		}
	}

	private void colorButton(DrawContext ctx, int x, int y, int w, int h, String label, int target) {
		int col = target == 1 ? c.color1() : c.color2();
		boolean hov = Gfx.in(mx, my, x, y, w, h);
		Gfx.round(ctx, x, y, w, h, Gfx.alpha(col, 0xB0));
		if (colorTarget == target) Gfx.outline(ctx, x, y, w, h, 0xFFFFFFFF);
		else if (hov) Gfx.outline(ctx, x, y, w, h, 0x88FFFFFF);
		ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), x + w / 2, y + 2, 0xFFFFFFFF);
		if (clicked && hov) colorTarget = target;
	}

	private void hueBar(DrawContext ctx, int x, int y, int w, int h) {
		for (int i = 0; i < w; i++) {
			ctx.fill(x + i, y, x + i + 1, y + h, Gfx.hsv(i / (float) w, 0.72f, 1f));
		}
		boolean hov = Gfx.in(mx, my, x, y - 2, w, h + 4);
		if (clicked && hov) dragId = HUE_ID;
		if (dragId == HUE_ID && down) {
			float t = Math.max(0f, Math.min(1f, (mx - x) / (float) w));
			if (colorTarget == 1) c.hue1 = t;
			else c.hue2 = t;
		}
		float cur = colorTarget == 1 ? c.hue1 : c.hue2;
		int mxp = x + (int) (cur * (w - 1));
		Gfx.rect(ctx, mxp - 1, y - 2, 3, h + 4, 0xFFFFFFFF);
	}
}
