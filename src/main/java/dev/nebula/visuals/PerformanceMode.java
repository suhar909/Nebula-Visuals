package dev.nebula.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;

public final class PerformanceMode {
	private PerformanceMode() {}

	public static void apply(MinecraftClient mc) {
		GameOptions o = mc.options;
		o.getViewDistance().setValue(6);
		o.getSimulationDistance().setValue(5);
		setMinimalParticles(mc, true);
		o.getEntityShadows().setValue(false);
		o.getBiomeBlendRadius().setValue(0);
		o.getEntityDistanceScaling().setValue(0.75);
		o.getMaxFps().setValue(260);
		o.getEnableVsync().setValue(false);
		o.getCloudRenderMode().setValue(CloudRenderMode.OFF);
		o.write();
		if (mc.world != null) mc.worldRenderer.reload();
	}

	public static void setViewDistance(MinecraftClient mc, int v) {
		mc.options.getViewDistance().setValue(v);
		mc.options.write();
	}

	public static void setShadows(MinecraftClient mc, boolean on) {
		mc.options.getEntityShadows().setValue(on);
		mc.options.write();
	}

	public static boolean shadows(MinecraftClient mc) {
		return mc.options.getEntityShadows().getValue();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void setMinimalParticles(MinecraftClient mc, boolean minimal) {
		SimpleOption opt = mc.options.getParticles();
		Object cur = opt.getValue();
		if (cur instanceof Enum<?> e) {
			Object[] all = e.getDeclaringClass().getEnumConstants();
			opt.setValue(minimal ? all[all.length - 1] : all[0]);
		}
		mc.options.write();
	}

	public static boolean minimalParticles(MinecraftClient mc) {
		Object cur = mc.options.getParticles().getValue();
		if (cur instanceof Enum<?> e) {
			Object[] all = e.getDeclaringClass().getEnumConstants();
			return e == all[all.length - 1];
		}
		return false;
	}

	public static void setVsync(MinecraftClient mc, boolean on) {
		mc.options.getEnableVsync().setValue(on);
		mc.options.write();
	}

	public static boolean vsync(MinecraftClient mc) {
		return mc.options.getEnableVsync().getValue();
	}

	public static int viewDistance(MinecraftClient mc) {
		return mc.options.getViewDistance().getValue();
	}
}
