package com.crowsofwar.avatar.common.bending.water;

import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.entity.EntityWaterCannon;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

public class WaterChargeHandler extends TickHandler {
	private static final UUID MOVEMENT_MODIFIER_ID = UUID.fromString("87a0458a-38ea-4d7a-be3b-0fee10217aa6");

	public WaterChargeHandler(int id) {
		super(id);
	}

	@Override
	public boolean tick(BendingContext ctx) {

		AbilityData abilityData = ctx.getData().getAbilityData("water_cannon");
		World world = ctx.getWorld();
		EntityLivingBase entity = ctx.getBenderEntity();
		BendingData data = ctx.getData();
		Bender bender = ctx.getBender();

		double powerRating = ctx.getBender().calcPowerRating(Waterbending.ID);
		int duration = data.getTickHandlerDuration(this);
		double speed = abilityData.isMasterPath(AbilityData.AbilityTreePath.SECOND) ? 40 : 0;
		float damage;
		float maxRange = abilityData.getLevel() >= 1 ? 40 : 60;
		Vec3d knockback = entity.getLookVec().scale(maxRange / 50).scale(STATS_CONFIG.waterCannonSettings.waterCannonKnockbackMult);
		float movementMultiplier = 0.6f - 0.7f * MathHelper.sqrt(duration / 40f);
		float size;
		//Multiply by 1.5 to get water cannon size
		float ticks = 50;
		int durationToFire = 40;
		if (abilityData.isMasterPath(AbilityData.AbilityTreePath.FIRST)) {
			durationToFire = 60;
		}


		if (world.isRemote) {
			return false;
		}
		applyMovementModifier(entity, MathHelper.clamp(movementMultiplier, 0.1f, 1));

		if (abilityData.isMasterPath(AbilityData.AbilityTreePath.SECOND)) {

			size = 0.1F;
			ticks = 50;
			damage = (float) (STATS_CONFIG.waterCannonSettings.waterCannonDamage * 0.5 * bender.getDamageMult(Waterbending.ID));
			knockback.scale(damage);

			// Fire once every 10 ticks, until we get to 100 ticks
			// So at fire at 60, 70, 80, 90, 100
			if (duration >= 40 && duration % 10 == 0) {

				fireCannon(world, entity, damage, speed, size, ticks, maxRange, knockback);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.BLOCK_WATER_AMBIENT, SoundCategory.PLAYERS, 1, 2);
				entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_MODIFIER_ID);

				return duration >= 100;

			}

		} else if (duration >= durationToFire) {

			speed = abilityData.getLevel() >= 1 ? 20 : 30;
			speed += powerRating / 15;
			damage = (float) (STATS_CONFIG.waterCannonSettings.waterCannonDamage * bender.getDamageMult(Waterbending.ID));
			//Default damage is 1
			size = 0.25F;

			if (abilityData.getLevel() >= 1) {
				damage = (float) (STATS_CONFIG.waterCannonSettings.waterCannonDamage * 1.25 * bender.getDamageMult(Waterbending.ID));
				size = 0.4f;
				ticks = 75;
			}
			if (abilityData.getLevel() >= 2) {
				damage = (float) (STATS_CONFIG.waterCannonSettings.waterCannonDamage * 1.5 * bender.getDamageMult(Waterbending.ID));
				size = 0.55f;
				ticks = 100;
			}
			if (abilityData.isMasterPath(AbilityData.AbilityTreePath.FIRST)) {
				damage = (float) (STATS_CONFIG.waterCannonSettings.waterCannonDamage * 2.5 * bender.getDamageMult(Waterbending.ID));
				ticks = 125;
			}

			damage *= bender.getDamageMult(Waterbending.ID);
			knockback.scale(damage);
			fireCannon(world, entity, damage, speed, size, ticks, maxRange, knockback);
			world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 1, 2);
			entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_MODIFIER_ID);


			return true;
		}

		return false;

	}

	private void fireCannon(World world, EntityLivingBase entity, float damage, double speed, float size, float ticks, float maxRange, Vec3d knockBack) {

		EntityWaterCannon cannon = new EntityWaterCannon(world);

		cannon.setOwner(entity);
		cannon.setDamage(damage);
		cannon.setSizeMultiplier(size);
		cannon.setPosition(Vector.getEyePos(entity));
		cannon.setLifeTime(ticks);
		cannon.setKnockBack(knockBack);
		cannon.setMaxRange(maxRange);
		cannon.setAbility(new AbilityWaterCannon());

		Vector velocity = Vector.getLookRectangular(entity);
		velocity = velocity.normalize().times(speed);
		cannon.setVelocity(velocity);
		world.spawnEntity(cannon);

	}

	private void applyMovementModifier(EntityLivingBase entity, float multiplier) {

		IAttributeInstance moveSpeed = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

		moveSpeed.removeModifier(MOVEMENT_MODIFIER_ID);

		moveSpeed.applyModifier(new AttributeModifier(MOVEMENT_MODIFIER_ID, "Water charge modifier", multiplier - 1, 1));

	}

}


