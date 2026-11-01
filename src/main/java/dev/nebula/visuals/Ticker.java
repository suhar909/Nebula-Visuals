package dev.nebula.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class Ticker {
	private Ticker() {}

	public record Popup(String text, long born, int color) {}

	public static final List<Popup> POPUPS = new ArrayList<>();
	private static final Deque<Long> LEFT = new ArrayDeque<>();
	private static final Deque<Long> RIGHT = new ArrayDeque<>();
	private static final Map<Integer, Float> HEALTH = new HashMap<>();
	private static final Set<Integer> GLOWED = new HashSet<>();
	private static boolean shiftWas, lmbWas, rmbWas, nightApplied;

	public static int leftCps() { return LEFT.size(); }
	public static int rightCps() { return RIGHT.size(); }

	/** Called every rendered frame so that fast clicks are not missed. */
	public static void pollMouse(MinecraftClient mc) {
		long h = mc.getWindow().getHandle();
		long now = System.currentTimeMillis();
		boolean free = mc.currentScreen == null;
		boolean l = free && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean r = free && GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		if (l && !lmbWas) LEFT.addLast(now);
		if (r && !rmbWas) RIGHT.addLast(now);
		lmbWas = l;
		rmbWas = r;
		while (!LEFT.isEmpty() && now - LEFT.peekFirst() > 1000) LEFT.pollFirst();
		while (!RIGHT.isEmpty() && now - RIGHT.peekFirst() > 1000) RIGHT.pollFirst();
	}

	public static void tick(MinecraftClient mc) {
		NebulaConfig c = NebulaConfig.get();
		long h = mc.getWindow().getHandle();

		// custom title screen replaces the vanilla one
		if (c.customTitle && mc.currentScreen instanceof TitleScreen) {
			mc.setScreen(new NebulaTitleScreen());
			return;
		}

		// Right Shift opens the menu
		boolean shift = GLFW.glfwGetKey(h, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		if (shift && !shiftWas && mc.currentScreen == null && mc.player != null) {
			mc.setScreen(new NebulaMenu(null));
		}
		shiftWas = shift;

		if (mc.player == null || mc.world == null) {
			HEALTH.clear();
			GLOWED.clear();
			nightApplied = false;
			return;
		}

		// toggle sprint
		if (c.toggleSprint && mc.currentScreen == null && mc.options.forwardKey.isPressed() && !mc.player.isSneaking()) {
			mc.options.sprintKey.setPressed(true);
		}

		// fullbright through a client-side night vision effect
		if (c.fullbright) {
			StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
			if (cur == null || cur.getDuration() < 600) {
				mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0, false, false, false));
			}
			nightApplied = true;
		} else if (nightApplied) {
			mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
			nightApplied = false;
		}

		// glow outline on other players
		if (c.glowEsp) {
			for (PlayerEntity p : mc.world.getPlayers()) {
				if (p != mc.player) {
					p.setGlowing(true);
					GLOWED.add(p.getId());
				}
			}
		} else if (!GLOWED.isEmpty()) {
			for (PlayerEntity p : mc.world.getPlayers()) {
				if (GLOWED.contains(p.getId())) p.setGlowing(false);
			}
			GLOWED.clear();
		}

		// damage numbers for the entity under the crosshair
		long now = System.currentTimeMillis();
		if (mc.targetedEntity instanceof LivingEntity le) {
			float hp = le.getHealth();
			Float prev = HEALTH.put(le.getId(), hp);
			if (c.damageNumbers && prev != null && hp < prev - 0.01f) {
				POPUPS.add(new Popup(String.format(Locale.ROOT, "-%.1f", prev - hp), now, 0xFFFF5555));
			}
		}
		if (HEALTH.size() > 64) HEALTH.clear();
		POPUPS.removeIf(p -> now - p.born() > c.popupMs);
	}
}
