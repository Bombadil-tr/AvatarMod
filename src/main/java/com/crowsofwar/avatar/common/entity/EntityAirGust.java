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

package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.bending.BendingStyle;
import com.crowsofwar.avatar.common.bending.air.Airbending;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.util.AvatarUtils.afterVelocityAdded;

public class EntityAirGust extends EntityArc<EntityAirGust.AirGustControlPoint> {

	public static final Vector ZERO = new Vector(0, 0, 0);
	private static final DataParameter<Float> SYNC_SIZE = EntityDataManager.createKey(EntityAirGust.class, DataSerializers.FLOAT);
	private boolean airGrab, destroyProjectiles, pushStone, pushIronTrapDoor, pushIronDoor;

	public EntityAirGust(World world) {
		super(world);
		setSize(1f, 1f);
		putsOutFires = true;
		this.noClip = true;
		this.pushStoneButton = pushStone;
		this.pushDoor = pushIronDoor;
		this.pushTrapDoor = pushIronTrapDoor;
	}

	public float getSize() {
		return dataManager.get(SYNC_SIZE);
	}

	public void setSize(float size) {
		dataManager.set(SYNC_SIZE, size);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_SIZE, 1F);
	}



	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		airGrab = nbt.getBoolean("AirGrab");
		destroyProjectiles = nbt.getBoolean("DestroyProjectiles");
		pushStone = nbt.getBoolean("PushStone");
		pushIronDoor = nbt.getBoolean("PushStone");
		pushIronTrapDoor = nbt.getBoolean("PushStone");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setBoolean("AirGrab", airGrab);
		nbt.setBoolean("DestroyProjectiles", destroyProjectiles);
		nbt.setBoolean("PushStone", pushStone);
		nbt.setBoolean("PushIronDoor", pushIronDoor);
		nbt.setBoolean("PushIronTrap", pushIronTrapDoor);

	}

	@Override
	public BendingStyle getElement() {
		return new Airbending();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		ControlPoint first = getControlPoint(0);
		ControlPoint second = getControlPoint(1);
		if (first.position().sqrDist(second.position()) >= 400
				|| ticksExisted > 120) {
			setDead();
		}
		List<Entity> hit = world.getEntitiesWithinAABB(Entity.class, getEntityBoundingBox().grow(1.5));
		if (!hit.isEmpty()) {
			for (Entity e : hit) {
				if (canCollideWith(e)) {
					onCollideWithEntity(e);
				}
			}
		}
		float expansionRate = 1f / 60;
		setSize(getSize() + expansionRate);
		setSize(getSize(), getSize());
	}

	@Override
	public void onCollideWithEntity(Entity entity) {
		EntityLivingBase owner = getOwner();
		if (!entity.world.isRemote && entity != owner && canCollideWith(entity) && owner != null) {

			if (entity instanceof AvatarEntity) {
				AvatarEntity avatarEntity = (AvatarEntity) entity;
				if (avatarEntity.onAirContact()) return;
				if (!avatarEntity.canPush()) return;
			}

			BendingData data = Objects.requireNonNull(Bender.get(owner)).getData();
			float xp = 0;
			if (data != null) {
				AbilityData abilityData = data.getAbilityData("air_gust");
				xp = abilityData.getTotalXp();
				abilityData.addXp(SKILLS_CONFIG.airGustHit);
			}

			Vector velocity = velocity().times(0.15).times(1 + xp / 200.0);
			velocity = velocity.withY(airGrab ? -1 : 1).times(airGrab ? -0.2 : 1);

			entity.addVelocity(velocity.x(), velocity.y(), velocity.z());
			afterVelocityAdded(entity);

			if (entity instanceof AvatarEntity) {
				((AvatarEntity) entity).onAirContact();
			}
			setDead();

		}
	}


	@Override
	public boolean onCollideWithSolid() {
		setDead();
		return true;
	}

	@Override
	protected AirGustControlPoint createControlPoint(float size, int index) {
		return new AirGustControlPoint(this, 0.2f, 0, 0, 0);
	}

	@Override
	public int getAmountOfControlPoints() {
		return 2;
	}

	@Override
	protected double getControlPointMaxDistanceSq() {
		return 12; // 20
	}

	@Override
	protected double getControlPointTeleportDistanceSq() {
		// Note: Is not actually called.
		// Set dead as soon as reached sq-distance
		return 200;
	}

	public boolean doesAirGrab() {
		return airGrab;
	}

	public void setAirGrab(boolean airGrab) {
		this.airGrab = airGrab;
	}

	public boolean doesDestroyProjectiles() {
		return destroyProjectiles;
	}

	public void setDestroyProjectiles(boolean destroyProjectiles) {
		this.destroyProjectiles = destroyProjectiles;
	}

	public void setPushStone(boolean pushStone) {
		this.pushStone = pushStone;
	}

	public void setPushIronDoor(boolean pushIronDoor) {
		this.pushIronDoor = pushIronDoor;
	}

	public void setPushIronTrapDoor(boolean pushIronTrapDoor) {
		this.pushIronTrapDoor = pushIronTrapDoor;
	}

	@Override
	public boolean pushButton(boolean pushStone) {
		return true;
	}

	@Override
	public boolean pushLever() {
		return true;
	}

	@Override
	public boolean pushDoor(boolean pushIron) {
		return true;
	}

	@Override
	public boolean pushTrapdoor(boolean pushIron) {
		return true;
	}

	@Override
	protected double getVelocityMultiplier() {
		return 5;
	}

	public static class AirGustControlPoint extends ControlPoint {

		public AirGustControlPoint(EntityArc arc, float size, double x, double y, double z) {
			super(arc, size, x, y, z);
		}

		@Override
		public void onUpdate() {
			super.onUpdate();
			if (arc.getControlPoint(0) == this) {
				float expansionRate = 1f / 80;
				size += expansionRate;
			}
		}

	}
}
