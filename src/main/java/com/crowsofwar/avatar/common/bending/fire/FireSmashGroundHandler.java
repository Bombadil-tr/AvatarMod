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
package com.crowsofwar.avatar.common.bending.fire;


import com.crowsofwar.avatar.common.bending.air.SmashGroundHandler;
import com.crowsofwar.avatar.common.data.TickHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

/**
 * @author CrowsOfWar
 */
public class FireSmashGroundHandler extends SmashGroundHandler {

	public static TickHandler SMASH_GROUND_FIRE = new FireSmashGroundHandler();

	@Override
	protected double getRange() {
		return 3;
	}

	@Override
	protected SoundEvent getSound() {
		return SoundEvents.ITEM_FIRECHARGE_USE;
	}

	@Override
	protected int getNumberOfParticles() {
		return 15;
	}

	@Override
	protected EnumParticleTypes getParticle() {
		return EnumParticleTypes.FLAME;
	}

	@Override
	protected float getParticleSpeed() {
		return 0;
	}

	@Override
	protected float getKnockbackHeight() {
		return 0.2F;
	}

	@Override
	protected double getSpeed() {
		return 2;
	}

	@Override
	protected boolean isFire() {
		return true;
	}

	@Override
	protected int fireTime() {
		return 5;
	}
}
