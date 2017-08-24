package com.crowsofwar.avatar.common.bending.lightning;

import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.entity.EntityLightningArc;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.UUID;

import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;

/**
 * @author CrowsOfWar
 */
public class LightningChargeHandler extends TickHandler {

	private static final UUID MOVEMENT_MODIFIER_ID = UUID.fromString
			("dfb6235c-82b6-407e-beaf-a48045735a82");

	@Override
	public boolean tick(BendingContext ctx) {

		World world = ctx.getWorld();
		EntityLivingBase entity = ctx.getBenderEntity();
		BendingData data = ctx.getData();

		if (world.isRemote) {
			return false;
		}

		int duration = data.getTickHandlerDuration(this);

		float movementMultiplier = 0.6f - 0.7f * MathHelper.sqrt(duration / 40f);
		applyMovementModifier(entity, MathHelper.clamp(movementMultiplier, 0.1f, 1));

		if (duration >= 40) {

			AbilityData abilityData = data.getAbilityData(AbilityLightningArc.ID);
			float damage = abilityData.getLevel() >= 1 ? 8 : 6;
			double speed = abilityData.getLevel() >= 0 ? 20 : 30;
			float size = abilityData.getLevel() >= 2 ? 1.5f : 1;

			fireLightning(world, entity, damage, speed, size);
			entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_MODIFIER_ID);

			abilityData.addXp(SKILLS_CONFIG.madeLightning);

			return true;

		}

		return false;

	}

	private void fireLightning(World world, EntityLivingBase entity, float damage, double speed,
							   float size) {
		float[] turbulenceValues = {0.6f, 1.2f};

		for (float turbulence : turbulenceValues) {

			EntityLightningArc lightning = new EntityLightningArc(world);
			lightning.setOwner(entity);
			lightning.setTurbulence(turbulence);
			lightning.setDamage(damage);
			lightning.setSizeMultiplier(size);

			lightning.setPosition(Vector.getEyePos(entity));
			lightning.setEndPos(Vector.getEyePos(entity));

			Vector velocity = Vector.getLookRectangular(entity);
			velocity = velocity.normalize().times(speed);
			lightning.setVelocity(velocity);

			world.spawnEntity(lightning);

		}
	}

	private void applyMovementModifier(EntityLivingBase entity, float multiplier) {

		IAttributeInstance moveSpeed = entity.getEntityAttribute(SharedMonsterAttributes
				.MOVEMENT_SPEED);

		moveSpeed.removeModifier(MOVEMENT_MODIFIER_ID);

		moveSpeed.applyModifier(new AttributeModifier(MOVEMENT_MODIFIER_ID,
				"Lightning charge modifier", multiplier - 1, 1));

	}

}
