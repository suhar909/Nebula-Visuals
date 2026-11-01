package dev.nebula.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HudRenderer {
	private HudRenderer() {}

	/** Last drawn bounds of every element: {x, y, w, h}. Used by the HUD editor. */
	public static final Map<String, int[]> BOUNDS = new LinkedHashMap<>();

	public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden) return;
		Ticker.pollMouse(mc);
		NebulaConfig c = NebulaConfig.get();
		if (!c.hudEnabled) return;
		drawAll(ctx, mc, false);
		drawCombat(ctx, mc, c);
	}

	public static void drawAll(DrawContext ctx, MinecraftClient mc, boolean editing) {
		NebulaConfig c = NebulaConfig.get();
		BOUNDS.clear();
		int sw = ctx.getScaledWindowWidth();
		int sh = ctx.getScaledWindowHeight();
		for (String key : NebulaConfig.KEYS) {
			NebulaConfig.Pos p = c.hud.get(key);
			if (p == null || !p.enabled) continue;
			switch (key) {
				case "fps" -> textBox(ctx, mc, c, key, p, sw, sh, List.of("FPS: " + mc.getCurrentFps()));
				case "ping" -> textBox(ctx, mc, c, key, p, sw, sh, List.of(Gfx.t("Пинг: ", "Ping: ") + ping(mc) + " ms"));
				case "cps" -> textBox(ctx, mc, c, key, p, sw, sh, List.of("CPS: " + Ticker.leftCps() + " | " + Ticker.rightCps()));
				case "coords" -> textBox(ctx, mc, c, key, p, sw, sh, List.of(
					"XYZ: " + mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ()));
				case "armor" -> armor(ctx, mc, c, key, p, sw, sh);
				case "keystrokes" -> keystrokes(ctx, mc, c, key, p, sw, sh);
				case "target" -> target(ctx, mc, c, key, p, sw, sh, editing);
				case "cooldown" -> cooldown(ctx, mc, c, key, p, sw, sh);
				default -> {}
			}
		}
	}

	private static int ping(MinecraftClient mc) {
		if (mc.getNetworkHandler() == null) return 0;
		PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
		return e == null ? 0 : e.getLatency();
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}

	private static void textBox(DrawContext ctx, MinecraftClient mc, NebulaConfig c, String key, NebulaConfig.Pos p,
	                            int sw, int sh, List<String> lines) {
		int w = 0;
		for (String s : lines) w = Math.max(w, mc.textRenderer.getWidth(s));
		w += 10;
		int h = lines.size() * 10 + 4;
		int x = clamp(p.x, 0, Math.max(0, sw - w));
		int y = clamp(p.y, 0, Math.max(0, sh - h));
		ctx.fill(x, y, x + w, y + h, 0x99000000);
		ctx.fill(x, y, x + 2, y + h, c.color1());
		for (int i = 0; i < lines.size(); i++) {
			ctx.drawTextWithShadow(mc.textRenderer, lines.get(i), x + 6, y + 3 + i * 10, 0xFFFFFFFF);
		}
		BOUNDS.put(key, new int[]{x, y, w, h});
	}

	private static void armor(DrawContext ctx, MinecraftClient mc, NebulaConfig c, String key, NebulaConfig.Pos p, int sw, int sh) {
		EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
		int w = 62, h = 4 * 18 + 4;
		int x = clamp(p.x, 0, Math.max(0, sw - w));
		int y = clamp(p.y, 0, Math.max(0, sh - h));
		ctx.fill(x, y, x + w, y + h, 0x99000000);
		ctx.fill(x, y, x + 2, y + h, c.color1());
		for (int i = 0; i < 4; i++) {
			ItemStack st = mc.player.getEquippedStack(slots[i]);
			if (st.isEmpty()) continue;
			ctx.drawItem(st, x + 5, y + 2 + i * 18);
			if (st.isDamageable()) {
				int max = st.getMaxDamage();
				int left = max - st.getDamage();
				float ratio = left / (float) Math.max(1, max);
				int col = Gfx.lerp(0xFFFF4444, 0xFF55FF55, ratio);
				ctx.drawTextWithShadow(mc.textRenderer, String.valueOf(left), x + 24, y + 6 + i * 18, col);
			}
		}
		BOUNDS.put(key, new int[]{x, y, w, h});
	}

	private static void keyBox(DrawContext ctx, MinecraftClient mc, NebulaConfig c, int x, int y, int w, int h,
	                           String label, boolean pressed) {
		ctx.fill(x, y, x + w, y + h, pressed ? Gfx.alpha(c.color1(), 0xC0) : 0x99000000);
		Gfx.outline(ctx, x, y, w, h, Gfx.alpha(c.color1(), 0x80));
		ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), x + w / 2, y + h / 2 - 4, 0xFFFFFFFF);
	}

	private static void keystrokes(DrawContext ctx, MinecraftClient mc, NebulaConfig c, String key, NebulaConfig.Pos p, int sw, int sh) {
		int w = 68, h = 68;
		int x = clamp(p.x, 0, Math.max(0, sw - w));
		int y = clamp(p.y, 0, Math.max(0, sh - h));
		keyBox(ctx, mc, c, x + 23, y, 22, 22, "W", mc.options.forwardKey.isPressed());
		keyBox(ctx, mc, c, x, y + 23, 22, 22, "A", mc.options.leftKey.isPressed());
		keyBox(ctx, mc, c, x + 23, y + 23, 22, 22, "S", mc.options.backKey.isPressed());
		keyBox(ctx, mc, c, x + 46, y + 23, 22, 22, "D", mc.options.rightKey.isPressed());
		keyBox(ctx, mc, c, x, y + 46, 34, 22, "L " + Ticker.leftCps(), mc.options.attackKey.isPressed());
		keyBox(ctx, mc, c, x + 34, y + 46, 34, 22, "R " + Ticker.rightCps(), mc.options.useKey.isPressed());
		BOUNDS.put(key, new int[]{x, y, w, h});
	}

	private static void target(DrawContext ctx, MinecraftClient mc, NebulaConfig c, String key, NebulaConfig.Pos p,
	                           int sw, int sh, boolean editing) {
		String name;
		float hp, max;
		if (mc.targetedEntity instanceof LivingEntity le) {
			name = le.getName().getString();
			hp = le.getHealth();
			max = Math.max(1f, le.getMaxHealth());
		} else if (editing) {
			name = Gfx.t("Цель", "Target");
			hp = 14f;
			max = 20f;
		} else {
			return;
		}
		int w = 120, h = 26;
		int x = clamp(p.x, 0, Math.max(0, sw - w));
		int y = clamp(p.y, 0, Math.max(0, sh - h));
		ctx.fill(x, y, x + w, y + h, 0x99000000);
		ctx.fill(x, y, x + 2, y + h, c.color1());
		ctx.drawTextWithShadow(mc.textRenderer, name, x + 6, y + 3, 0xFFFFFFFF);
		float ratio = Math.max(0f, Math.min(1f, hp / max));
		ctx.fill(x + 6, y + 15, x + w - 6, y + 22, 0xFF222230);
		int barW = (int) ((w - 12) * ratio);
		ctx.fill(x + 6, y + 15, x + 6 + barW, y + 22, Gfx.lerp(0xFFFF4444, 0xFF55FF55, ratio));
		String txt = String.format(Locale.ROOT, "%.1f", hp);
		ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(txt), x + w / 2, y + 14, 0xFFFFFFFF);
		BOUNDS.put(key, new int[]{x, y, w, h});
	}

	private static void cooldown(DrawContext ctx, MinecraftClient mc, NebulaConfig c, String key, NebulaConfig.Pos p, int sw, int sh) {
		int w = 80, h = 8;
		int x = clamp(p.x, 0, Math.max(0, sw - w));
		int y = clamp(p.y, 0, Math.max(0, sh - h));
		float pr = Math.max(0f, Math.min(1f, mc.player.getAttackCooldownProgress(0f)));
		ctx.fill(x, y, x + w, y + h, 0x99000000);
		ctx.fill(x + 1, y + 1, x + 1 + (int) ((w - 2) * pr), y + h - 1, pr >= 1f ? c.color2() : c.color1());
		BOUNDS.put(key, new int[]{x, y, w, h});
	}

	private static void drawCombat(DrawContext ctx, MinecraftClient mc, NebulaConfig c) {
		int sw = ctx.getScaledWindowWidth();
		int sh = ctx.getScaledWindowHeight();
		int cx = sw / 2;
		int cy = sh / 2;
		if (c.hitMarker && mc.targetedEntity instanceof LivingEntity) {
			int col = c.color2();
			ctx.fill(cx - 10, cy - 1, cx - 6, cy, col);
			ctx.fill(cx + 6, cy - 1, cx + 10, cy, col);
			ctx.fill(cx - 1, cy - 10, cx, cy - 6, col);
			ctx.fill(cx - 1, cy + 6, cx, cy + 10, col);
		}
		if (c.damageNumbers) {
			long now = System.currentTimeMillis();
			for (Ticker.Popup pop : new ArrayList<>(Ticker.POPUPS)) {
				float t = (now - pop.born()) / (float) Math.max(1, c.popupMs);
				if (t < 0f || t > 1f) continue;
				int a = Math.max(12, (int) (255 * (1f - t)));
				int y = cy - 16 - (int) (t * 22);
				ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(pop.text()), cx + 24, y, (a << 24) | (pop.color() & 0xFFFFFF));
			}
		}
	}
}
