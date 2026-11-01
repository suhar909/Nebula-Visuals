package dev.nebula.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

/** Drag HUD elements with the mouse; buttons at the bottom show/hide each element. */
public class HudEditorScreen extends Screen {
	private final Screen parent;
	private String dragKey;
	private int offX, offY;
	private boolean pressedBefore = true;

	public HudEditorScreen(Screen parent) {
		super(Text.literal("HUD"));
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
	protected void init() {
		NebulaConfig c = NebulaConfig.get();
		int bw = 80, gap = 4;
		for (int i = 0; i < NebulaConfig.KEYS.length; i++) {
			String key = NebulaConfig.KEYS[i];
			int col = i % 4, row = i / 4;
			int x = width / 2 - (4 * bw + 3 * gap) / 2 + col * (bw + gap);
			int y = height - 48 + row * 22;
			NebulaConfig.Pos p = c.hud.get(key);
			addDrawableChild(ButtonWidget.builder(label(key, p), btn -> {
				p.enabled = !p.enabled;
				c.save();
				btn.setMessage(label(key, p));
			}).dimensions(x, y, bw, 20).build());
		}
	}

	private Text label(String key, NebulaConfig.Pos p) {
		return Text.literal(key + (p.enabled ? " §aON" : " §cOFF"));
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		NebulaConfig c = NebulaConfig.get();
		super.render(ctx, mouseX, mouseY, delta);
		HudRenderer.drawAll(ctx, mc, true);

		long h = mc.getWindow().getHandle();
		boolean down = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		if (down) {
			if (dragKey == null && !pressedBefore && mouseY < height - 52) {
				for (Map.Entry<String, int[]> e : HudRenderer.BOUNDS.entrySet()) {
					int[] b = e.getValue();
					if (Gfx.in(mouseX, mouseY, b[0], b[1], b[2], b[3])) {
						dragKey = e.getKey();
						offX = mouseX - b[0];
						offY = mouseY - b[1];
					}
				}
			}
			if (dragKey != null) {
				NebulaConfig.Pos p = c.hud.get(dragKey);
				p.x = mouseX - offX;
				p.y = mouseY - offY;
			}
		} else if (dragKey != null) {
			dragKey = null;
			c.save();
		}
		pressedBefore = down;

		ctx.drawCenteredTextWithShadow(textRenderer,
			Text.literal(Gfx.t("Перетаскивайте элементы мышью. Esc — выход", "Drag elements with the mouse. Esc to exit")),
			width / 2, 10, 0xFFFFFFFF);
	}
}
