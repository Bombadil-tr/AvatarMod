/* 
  This file is part of AvatarMod.
    
  AvatarMod is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  AvatarMod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with AvatarMod. If not, see <http://www.gnu.org/licenses/>.
*/

package com.crowsofwar.avatar.common.bending.air;

import com.crowsofwar.avatar.common.entity.EntityShockwave;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

import com.crowsofwar.avatar.common.AvatarParticles;
import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.controls.AvatarControl;
import com.crowsofwar.avatar.common.data.*;
import com.crowsofwar.avatar.common.data.AbilityData.AbilityTreePath;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.particle.*;
import com.crowsofwar.gorecore.util.Vector;

import java.util.List;

import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;
import static com.crowsofwar.avatar.common.data.TickHandlerController.*;

/**
 * @author CrowsOfWar
 */
public class StatCtrlAirJump extends StatusControl {

	public StatCtrlAirJump() {
		super(0, AvatarControl.CONTROL_JUMP, CrosshairPosition.BELOW_CROSSHAIR);
	}

	@Override
	public boolean execute(BendingContext ctx) {

		Bender bender = ctx.getBender();
		EntityLivingBase entity = ctx.getBenderEntity();
		BendingData data = ctx.getData();
		World world = ctx.getWorld();

		AbilityData abilityData = data.getAbilityData("air_jump");
		boolean allowDoubleJump = abilityData.getLevel() == 3 && abilityData.getPath() == AbilityTreePath.FIRST;

		// Figure out whether entity is on ground by finding collisions with
		// ground - if found a collision box, then is not on ground
		List<AxisAlignedBB> collideWithGround = world.getCollisionBoxes(entity, entity.getEntityBoundingBox().grow(0.4, 1, 0.4));
		boolean onGround = !collideWithGround.isEmpty() || entity.collidedVertically || world.getBlockState(entity.getPosition()).getBlock() == Blocks.WEB;

		if (onGround || (allowDoubleJump && bender.consumeChi(STATS_CONFIG.chiAirJump))) {

			int lvl = abilityData.getLevel();
			double multiplier = 0.65;
			double powerModifier = 10;
			double powerDuration = 3;
			int numberOfParticles = 40;
			double particleSpeed = 0.2;
			if (lvl >= 1) {
				multiplier = 1;
				powerModifier = 15;
				powerDuration = 4;
				numberOfParticles = 50;
				particleSpeed = 0.3;
			}
			if (lvl >= 2) {
				multiplier = 1.2;
				powerModifier = 20;
				powerDuration = 5;
				numberOfParticles = 60;
				particleSpeed = 0.35;
			}
			if (lvl >= 3) {
				multiplier = 1.4;
				powerModifier = 25;
				powerDuration = 6;
			}
			if (abilityData.isMasterPath(AbilityTreePath.SECOND)) {
				numberOfParticles = 70;
				particleSpeed = 0.5;
			}

			if (world instanceof WorldServer) {
				WorldServer World = (WorldServer) world;
				World.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, entity.posX, entity.getEntityBoundingBox().minY + 0.1, entity.posZ,
									numberOfParticles, 0, 0, 0, particleSpeed);
			}

			Vector rotations = new Vector(Math.toRadians((entity.rotationPitch) / 1), Math.toRadians(entity.rotationYaw), 0);

			Vector velocity = rotations.toRectangular();
			velocity = velocity.withY(Math.pow(velocity.y(), .1));
			velocity = velocity.times(multiplier);
			if (!onGround) {
				velocity = velocity.times(0.6);
				entity.motionX = 0;
				entity.motionY = 0;
				entity.motionZ = 0;
			}
			entity.addVelocity(velocity.x(), velocity.y(), velocity.z());
			if (entity instanceof EntityPlayerMP) {
				((EntityPlayerMP) entity).connection.sendPacket(new SPacketEntityVelocity(entity));
			}

			ParticleSpawner spawner = new NetworkParticleSpawner();
			spawner.spawnParticles(entity.world, AvatarParticles.getParticleAir(), 2, 6, new Vector(entity), new Vector(1, 0, 1));

			float fallAbsorption = 0;
			if (lvl <= 0) {
				fallAbsorption = 8;
			} else if (lvl == 1) {
				fallAbsorption = 13;
			} else if (lvl == 2) {
				fallAbsorption = 16;
			} else if (lvl == 3) {
				fallAbsorption = 19;
			}

			data.getMiscData().setFallAbsorption(fallAbsorption);

			data.addTickHandler(AIR_PARTICLE_SPAWNER);
			if (abilityData.getLevel() == 3 && abilityData.getPath() == AbilityTreePath.SECOND) {
				data.addTickHandler(SMASH_GROUND);
			}
			abilityData.addXp(SKILLS_CONFIG.airJump);

			entity.world.playSound(null, new BlockPos(entity), SoundEvents.ENTITY_FIREWORK_LAUNCH, SoundCategory.PLAYERS, 1, .7f);

			PowerRatingModifier powerRatingModifier = new AirJumpPowerModifier(powerModifier);
			powerRatingModifier.setTicks((int) (powerDuration * 20));
			//noinspection ConstantConditions
			data.getPowerRatingManager(Airbending.ID).addModifier(powerRatingModifier, ctx);
			EntityShockwave wave = new EntityShockwave(world);
			wave.setDamage(0);
			wave.setFireTime(0);
			wave.setRange(lvl > 0 ? 3 + lvl : 3);
			wave.setSpeed(lvl > 0 ? 0.6F + lvl / 10F: 0.6f);
			wave.setKnockbackHeight(lvl > 0 ? 0.2F + lvl /  20F : 0.2F);
			wave.setParticleName(EnumParticleTypes.EXPLOSION_NORMAL.getParticleName());
			wave.setPerformanceAmount(2);
			wave.setAbility(new AbilityAirJump());
			wave.setElement(new Airbending());
			wave.setParticleSpeed(lvl > 0 ? 0.1F + lvl / 10F : 0.1F);
			wave.setPosition(entity.getPositionVector());
			wave.setOwner(entity);
			world.spawnEntity(wave);


			return true;

		}

		return false;

	}

}
