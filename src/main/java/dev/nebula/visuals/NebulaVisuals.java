package dev.nebula.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NebulaVisuals implements ClientModInitializer {
	public static final String ID = "nebulavisuals";

	@Override
	public void onInitializeClient() {
		NebulaConfig.get();
		HudElementRegistry.addLast(Identifier.of(ID, "hud"), HudRenderer::render);
		ClientTickEvents.END_CLIENT_TICK.register(Ticker::tick);
		ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
			NebulaConfig c = NebulaConfig.get();
			if (!c.chatFilter) return true;
			String low = message.toLowerCase();
			for (String w : c.bannedWords) {
				if (w != null && !w.isBlank() && low.contains(w.toLowerCase())) {
					MinecraftClient mc = MinecraftClient.getInstance();
					if (mc.player != null) {
						mc.player.sendMessage(Text.literal("§d[Nebula] §c" + Gfx.t("Заблокировано фильтром: ", "Blocked by filter: ") + w), false);
					}
					return false;
				}
			}
			return true;
		});
	}
}
