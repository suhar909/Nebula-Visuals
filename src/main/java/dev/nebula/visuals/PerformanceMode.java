package dev.nebula.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

public final class PerformanceMode {
	private PerformanceMode() {}

	public static void apply(MinecraftClient mc) {
		GameOptions o = mc.options;
		o.getViewDistance().setValue(6);
		o.getSimulationDistance().setValue(5);
		o.getParticles().setValue(ParticlesMode.MINIMAL);
		o.getEntityShadows().setValue(false);
		o.getBiomeBlendRadius().setValue(0);
		o.getEntityDistanceScaling().setValue(0.75);
		o.getMaxFps().setValue(260);
		o.getEnableVsync().setValue(false);
		o.getGraphicsMode().setValue(GraphicsMode.FAST);
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

	public static void setMinimalParticles(MinecraftClient mc, boolean on) {
		mc.options.getParticles().setValue(on ? ParticlesMode.MINIMAL : ParticlesMode.ALL);
		mc.options.write();
	}

	public static boolean minimalParticles(MinecraftClient mc) {
		return mc.options.getParticles().getValue() == ParticlesMode.MINIMAL;
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
