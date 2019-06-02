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

import com.crowsofwar.avatar.common.bending.BendingStyle;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.world.World;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.data.*;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.entity.EntityShockwave;

/**
 * @author CrowsOfWar
 */
public class SmashGroundHandler extends TickHandler {
	public SmashGroundHandler(int id) {
		super(id);
	}

	@Override
	public boolean tick(BendingContext ctx) {

		EntityLivingBase entity = ctx.getBenderEntity();
		Bender bender = ctx.getBender();
		World world = ctx.getWorld();

		if (entity.isInWater() || entity.onGround || bender.isFlying()) {

			if (entity.onGround) {
				smashEntity(entity);
				world.playSound(null, entity.posX, entity.posY, entity.posZ, getSound(), getSoundCategory(), 4F, 0.5F);

			}

			return true;
		}

		return false;
	}

	protected void smashEntity(EntityLivingBase entity) {
		World world = entity.world;
		EntityShockwave shockwave = new EntityShockwave(world);
		shockwave.setDamage(getDamage());
		shockwave.setOwner(entity);
		shockwave.setPosition(entity.posX, entity.getEntityBoundingBox().minY, entity.posZ);
		shockwave.setKnockbackHeight(getKnockbackHeight());
		shockwave.setSpeed((float) getSpeed() / 5);
		shockwave.setRange((float) getRange());
		shockwave.setElement(getElement());
		shockwave.setParticleName(getParticle().getParticleName());
		shockwave.setParticleAmount(getParticleAmount());
		shockwave.setParticleSpeed((float) getParticleSpeed());
		shockwave.setFireTime(fireTime());
		shockwave.setAbility(getAbility());
		shockwave.setPerformanceAmount(getPerformanceAmount());
		world.spawnEntity(shockwave);
	}

	protected int fireTime() {
		return 0;
	}

	protected double getRange() {
		return 4;
	}

	protected EnumParticleTypes getParticle() {
		return EnumParticleTypes.EXPLOSION_NORMAL;
	}

	protected int getParticleAmount() {
		return 2;
	}

	protected Ability getAbility() {
		return new AbilityAirJump();
	}

	protected double getParticleSpeed() {
		return 0.1F;
	}

	protected double getSpeed() {
		return 4;
	}

	protected float getKnockbackHeight() {
		return 0.1F;
	}

	protected SoundEvent getSound() {
		return SoundEvents.BLOCK_FIRE_EXTINGUISH;
	}

	protected SoundCategory getSoundCategory() {
		return SoundCategory.BLOCKS;
	}

	protected int getPerformanceAmount() {
		return 10;
	}

	protected float getDamage() {
		return 6F;
	}

	protected BendingStyle getElement() {
		return new Airbending();
	}
}

