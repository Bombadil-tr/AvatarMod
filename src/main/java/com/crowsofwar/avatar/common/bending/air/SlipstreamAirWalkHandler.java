package com.crowsofwar.avatar.common.bending.air;

import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.Chi;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.particle.NetworkParticleSpawner;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Objects;

public class SlipstreamAirWalkHandler extends TickHandler {
	private NetworkParticleSpawner p;

	public SlipstreamAirWalkHandler(int id) {
		super(id);
		this.p = new NetworkParticleSpawner();
	}

	@Override
	public boolean tick(BendingContext ctx) {
		EntityLivingBase entity = ctx.getBenderEntity();
		BendingData bD = ctx.getData();
		Chi chi = bD.chi();
		World world = ctx.getWorld();
		boolean hasModifier = Objects.requireNonNull(bD.getPowerRatingManager(Airbending.ID)).hasModifier(SlipstreamPowerModifier.class);
		if (hasModifier) {
			boolean hasChi = chi.getTotalChi() > 0 && chi.getAvailableChi() > 0;
			boolean isCreative = (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative());
			if (hasChi || isCreative) {
				Vec3d targetVel = entity.getLookVec().add(new Vec3d(entity.motionX, entity.motionY, entity.motionZ));
				Vec3d currentVel = new Vec3d(entity.motionX, entity.motionY, entity.motionZ);
				entity.setNoGravity((targetVel.y <= 0 || currentVel.y <= 0) && entity.motionY <= 0);
				if (entity.getActivePotionEffect(MobEffects.INVISIBILITY) == null)
					p.spawnParticles(world, EnumParticleTypes.EXPLOSION_NORMAL, 1, 2, entity.posX, entity.getEntityBoundingBox().minY - 0.05, entity.posZ,
							0, 0, 0);
				if (entity.ticksExisted % 5 == 0 && !isCreative) {
					chi.setAvailableChi(chi.getAvailableChi() - 1);
				}
			}

		}
		return !hasModifier;
	}
}
