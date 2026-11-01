package dev.nebula.visuals;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Client-side movement and combat helpers. Everything here is ticked from Ticker. */
public final class Cheats {
	private Cheats() {}

	public static void tick(MinecraftClient mc) {
		ClientPlayerEntity p = mc.player;
		NebulaConfig c = NebulaConfig.get();
		if (p == null || mc.world == null) return;

		reach(p, c);
		if (c.fly) fly(mc, p, c);
		if (c.speed && !c.fly) speed(mc, p, c);
		if (c.noSlow) noSlow(mc, p);
		if (c.jesus) jesus(p);
		if (c.noFall) noFall(mc, p);
		if (c.autoTool) autoTool(mc, p);
		if (c.killAura) killAura(mc, p, c);
	}

	// ------------------------------------------------------------- movement

	/** Writes horizontal velocity along the current input direction. Returns false if no keys held. */
	private static boolean movementInput(MinecraftClient mc, ClientPlayerEntity p, double target) {
		float fwd = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
		float str = (mc.options.leftKey.isPressed() ? 1 : 0) - (mc.options.rightKey.isPressed() ? 1 : 0);
		if (fwd == 0f && str == 0f) return false;
		float yaw = p.getYaw() * MathHelper.RADIANS_PER_DEGREE;
		double sin = MathHelper.sin(yaw);
		double cos = MathHelper.cos(yaw);
		double x = -sin * fwd + cos * str;
		double z = cos * fwd + sin * str;
		double len = Math.sqrt(x * x + z * z);
		if (len < 1.0E-4) return false;
		p.setVelocity(x / len * target, p.getVelocity().y, z / len * target);
		return true;
	}

	private static void fly(MinecraftClient mc, ClientPlayerEntity p, NebulaConfig c) {
		if (p.getAbilities().allowFlying && p.getAbilities().flying) return;
		double y = 0.0;
		if (mc.options.jumpKey.isPressed()) y = c.flySpeed * 0.32;
		else if (mc.options.sneakKey.isPressed()) y = -c.flySpeed * 0.32;
		Vec3d v = p.getVelocity();
		p.setVelocity(v.x, y, v.z);
		movementInput(mc, p, c.flySpeed * 0.32);
		p.setOnGround(true);
		p.fallDistance = 0f;
	}

	private static void speed(MinecraftClient mc, ClientPlayerEntity p, NebulaConfig c) {
		if (p.getAbilities().flying || p.isTouchingWater() || p.isClimbing()) return;
		double target = 0.2873 * c.speedValue;
		Vec3d v = p.getVelocity();
		if (Math.hypot(v.x, v.z) > target + 0.02) return;
		movementInput(mc, p, target);
	}

	private static void noSlow(MinecraftClient mc, ClientPlayerEntity p) {
		if (!p.isUsingItem() && !p.isBlocking()) return;
		if (p.getAbilities().flying || p.isTouchingWater()) return;
		movementInput(mc, p, 0.26);
	}

	private static void jesus(ClientPlayerEntity p) {
		if (!p.isTouchingWater()) return;
		Vec3d v = p.getVelocity();
		if (v.y < 0.1) p.setVelocity(v.x, 0.08, v.z);
		p.setOnGround(true);
		p.fallDistance = 0f;
	}

	private static void noFall(MinecraftClient mc, ClientPlayerEntity p) {
		if (p.isOnGround() || p.getAbilities().flying) return;
		if (p.getVelocity().y >= 0) return;
		if (mc.getNetworkHandler() == null) return;
		mc.getNetworkHandler().sendPacket(
			new PlayerMoveC2SPacket.OnGroundOnly(true, p.horizontalCollision));
		p.fallDistance = 0f;
	}

	// --------------------------------------------------------------- combat

	private static void killAura(MinecraftClient mc, ClientPlayerEntity p, NebulaConfig c) {
		if (mc.interactionManager == null || mc.world == null) return;
		LivingEntity target = null;
		double best = Double.MAX_VALUE;
		for (Entity e : mc.world.getEntities()) {
			if (!(e instanceof LivingEntity le) || le == p || le instanceof ArmorStandEntity) continue;
			if (!le.isAlive() || le.isInvulnerable() || le.isSpectator()) continue;
			double d = p.distanceTo(le);
			if (d > c.auraRange || d >= best) continue;
			best = d;
			target = le;
		}
		if (target == null) return;

		Vec3d aim = target.getPos().add(0.0, target.getHeight() * 0.75, 0.0);
		Vec3d eye = p.getEyePos();
		double dx = aim.x - eye.x;
		double dy = aim.y - eye.y;
		double dz = aim.z - eye.z;
		double flat = Math.sqrt(dx * dx + dz * dz);
		p.setYaw((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
		p.setPitch((float) (-Math.toDegrees(Math.atan2(dy, flat))));

		if (p.getAttackCooldownProgress(0.5f) >= 1f) {
			mc.interactionManager.attackEntity(p, target);
			p.swingHand(Hand.MAIN_HAND);
		}
	}

	// ---------------------------------------------------------------- misc

	private static void reach(ClientPlayerEntity p, NebulaConfig c) {
		setBase(p, EntityAttributes.BLOCK_INTERACTION_RANGE, c.reach);
		setBase(p, EntityAttributes.ENTITY_INTERACTION_RANGE, c.reach);
	}

	private static void setBase(ClientPlayerEntity p, RegistryEntry<EntityAttribute> attr, double value) {
		EntityAttributeInstance a = p.getAttributeInstance(attr);
		if (a != null && a.getBaseValue() != value) a.setBaseValue(value);
	}

	private static void autoTool(MinecraftClient mc, ClientPlayerEntity p) {
		if (mc.interactionManager == null || mc.world == null) return;
		if (!mc.options.attackKey.isPressed()) return;
		if (!(mc.crosshairTarget instanceof BlockHitResult hit)) return;
		BlockState state = mc.world.getBlockState(hit.getBlockPos());
		if (state.isAir()) return;
		int bestSlot = -1;
		float bestSpeed = 1.0f;
		for (int i = 0; i < 9; i++) {
			ItemStack stack = p.getInventory().getStack(i);
			float s = stack.getMiningSpeedMultiplier(state);
			if (s > bestSpeed) {
				bestSpeed = s;
				bestSlot = i;
			}
		}
		if (bestSlot != -1 && p.getInventory().selectedSlot != bestSlot) {
			p.getInventory().selectedSlot = bestSlot;
		}
	}
}