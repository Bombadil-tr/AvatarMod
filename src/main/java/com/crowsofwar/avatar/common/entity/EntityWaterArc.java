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

import com.crowsofwar.avatar.common.damageutils.AvatarDamageSource;
import com.crowsofwar.avatar.common.bending.BattlePerformanceScore;
import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.bending.water.AbilityWaterArc;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.entity.data.WaterArcBehavior;
import com.crowsofwar.avatar.common.util.AvatarUtils;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.crowsofwar.avatar.common.bending.StatusControl.THROW_WATER;
import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

public class EntityWaterArc extends EntityArc<EntityWaterArc.WaterControlPoint> {

	private static final DataParameter<WaterArcBehavior> SYNC_BEHAVIOR = EntityDataManager
			.createKey(EntityWaterArc.class, WaterArcBehavior.DATA_SERIALIZER);

	private static final DataParameter<Float> SYNC_SIZE = EntityDataManager
			.createKey(EntityWaterArc.class, DataSerializers.FLOAT);

	/**
	 * The amount of ticks since last played splash sound. -1 for splashable.
	 */
	private int lastPlayedSplash;

	private boolean isSpear;

	private float damageMult;

	private float Size;

	private float Gravity;

	private float velocityMultiplier;


	public EntityWaterArc(World world) {
		super(world);
		this.Size = 0.4F;
		setSize(Size, Size);
		this.lastPlayedSplash = -1;
		this.damageMult = 1;
		this.putsOutFires = true;
		this.Gravity = 9.82F;
	}

	public float getDamageMult() {
		return damageMult;
	}

	public void setDamageMult(float mult) {
		this.damageMult = mult;
	}

	public void isSpear(boolean isSpear) {
		this.isSpear = isSpear;
	}

	public float getSize() {
		return dataManager.get(SYNC_SIZE);
	}

	public void setSize(float size) {
		dataManager.set(SYNC_SIZE, size);
	}

	public float getGravity() {
		return this.Gravity;
	}

	public void setGravity(float gravity) {
		this.Gravity = gravity;
	}


	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_BEHAVIOR, new WaterArcBehavior.Idle());
		dataManager.register(SYNC_SIZE, Size);
	}

	@Override
	protected void updateCpBehavior() {
		super.updateCpBehavior();
	//	getControlPoint(0).setPosition(this.position());
	//	getLeader().setPosition(this.position().plusY(getSize() / 8));
	}

	public void damageEntity(Entity entity) {
		if (!world.isRemote) {
			if (canDamageEntity(entity)) {
				DamageSource ds = AvatarDamageSource.causeWaterDamage(entity, getOwner());
				float damage = STATS_CONFIG.waterArcSettings.damage * damageMult;
				entity.attackEntityFrom(ds, damage);
				if (entity.attackEntityFrom(ds, damage)) {
					if (getOwner() != null && !world.isRemote && getAbility() != null) {
						BendingData data1 = BendingData.get(getOwner());
						AbilityData abilityData1 = data1.getAbilityData(getAbility().getName());
						abilityData1.addXp(SKILLS_CONFIG.waterHit);
						BattlePerformanceScore.addMediumScore(getOwner());

					}
				}
			}
		}
	}

	public void Splash() {
		if (world instanceof WorldServer) {

			float speed = 0.025F;
			float hitBox = 0.5F;
			int numberOfParticles = 500;

			if (getAbility() instanceof AbilityWaterArc && !world.isRemote) {
				AbilityData abilityData = BendingData.get(Objects.requireNonNull(getOwner())).getAbilityData("water_arc");
				int lvl = abilityData.getLevel();
				this.damageMult = lvl >= 2 ? 2 : 0.5F;
				//If the player's water arc level is level III or greater the aoe will do 2+ damage.
				hitBox = lvl <= 0 ? 0.5F : 0.5f * (lvl + 1);
				speed = lvl <= 0 ? 0.025F : 0.025F * (lvl + 1);
				numberOfParticles = lvl <= 0 ? 500 : 500 + 100 * lvl;
			} else this.damageMult = 0.5f;


			WorldServer World = (WorldServer) this.world;
			World.spawnParticle(EnumParticleTypes.WATER_WAKE, posX, posY, posZ, numberOfParticles, 0.2, 0.1, 0.2, speed);
			world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

			List<Entity> collided = world.getEntitiesInAABBexcluding(this, getEntityBoundingBox().grow(1, 1, 1),
					entity -> entity != getOwner());
			if (!collided.isEmpty() && !world.isRemote) {
				for (Entity entity : collided) {
					if (entity != getOwner() && entity != null && getOwner() != null) {


						Vector velocity = Vector.getEntityPos(entity).minus(Vector.getEntityPos(this));
						double distance = Vector.getEntityPos(entity).dist(Vector.getEntityPos(this));
						double direction = (hitBox - distance) * (speed * 5) / hitBox;
						velocity = velocity.times(direction).times(-1 + (-1 * hitBox / 2)).withY(speed / 2);

						double x = (velocity.x());
						double y = (velocity.y()) > 0 ? velocity.y() : 0.25F;
						double z = (velocity.z());
						entity.addVelocity(x, y, z);
						if (canDamageEntity(entity)) {
							damageEntity(entity);
						}
						BattlePerformanceScore.addSmallScore(getOwner());

						if (entity instanceof AvatarEntity) {
							AvatarEntity avent = (AvatarEntity) entity;
							avent.addVelocity(x, y, z);
						}
						entity.isAirBorne = true;
						AvatarUtils.afterVelocityAdded(entity);
					}
				}
			}

		}

	}

	@Override
	public boolean onCollideWithSolid() {
		if (isSpear && getBehavior() != null && getBehavior() instanceof WaterArcBehavior.Thrown) {
			breakCollidingBlocks();
			Splash();
			setDead();
			cleanup();

		}

		if (!world.isRemote && getBehavior() instanceof WaterArcBehavior.Thrown && !isSpear) {

			Splash();
			setDead();
			cleanup();


			if (world.isRemote) {
				Random random = new Random();

				double xVel, yVel, zVel;
				double offX, offY, offZ;

				if (collidedVertically) {

					xVel = 5;
					yVel = 3.5;
					zVel = 5;
					offX = 0;
					offY = 0.6;
					offZ = 0;

				} else {

					xVel = 7;
					yVel = 2;
					zVel = 7;
					offX = 0.6;
					offY = 0.2;
					offZ = 0.6;

				}

				xVel *= 0.0;
				yVel *= 0.0;
				zVel *= 0.0;

				int particles = random.nextInt(3) + 4;
				for (int i = 0; i < particles; i++) {

					world.spawnParticle(EnumParticleTypes.WATER_SPLASH, posX + random.nextGaussian() * offX,
							posY + random.nextGaussian() * offY + 0.2, posZ + random.nextGaussian() * offZ,
							random.nextGaussian() * xVel, random.nextGaussian() * yVel,
							random.nextGaussian() * zVel);

				}

			}
		}

		return false;

	}

	@Override
	public void onCollideWithEntity(Entity entity) {
		if (entity instanceof AvatarEntity && getBehavior() instanceof WaterArcBehavior.Thrown && ((AvatarEntity) entity).getOwner() != getOwner()) {
			((AvatarEntity) entity).onMinorWaterContact();
		}

	}

	@Override
	public void setDead() {
		super.setDead();
		cleanup();
	}

	@Override
	public void onUpdate() {

		super.onUpdate();
		if (lastPlayedSplash > -1) {
			lastPlayedSplash++;
			if (lastPlayedSplash > 20) lastPlayedSplash = -1;
		}

		WaterArcBehavior behavior = getBehavior();
		WaterArcBehavior next = (WaterArcBehavior) behavior.onUpdate(this);
		if (next != behavior) {
			setBehavior(next);
		}

		if (getOwner() == null) {
			this.setDead();
		}
		setSize(getSize() / 2, getSize() / 2);

		if (getBehavior() != null && getBehavior() instanceof WaterArcBehavior.PlayerControlled) {
			this.velocityMultiplier = 4;
		}

		if (getAbility() instanceof AbilityWaterArc && !world.isRemote && getOwner() != null) {
			if (getBehavior() != null && getBehavior() instanceof WaterArcBehavior.Thrown) {
				AbilityData aD = AbilityData.get(getOwner(), "water_arc");
				int lvl = aD.getLevel();
				this.velocityMultiplier = lvl >= 1 ? 8 + (2 * lvl) : 8;
			}
		} else if (getBehavior() != null && getBehavior() instanceof WaterArcBehavior.Thrown) {
			this.velocityMultiplier = 8;
		}


		if (getOwner() != null) {
			EntityWaterArc arc = AvatarEntity.lookupControlledEntity(world, EntityWaterArc.class, getOwner());
			BendingData bD = BendingData.get(getOwner());
			if (arc == null && bD.hasStatusControl(StatusControl.THROW_WATER)) {
				bD.removeStatusControl(StatusControl.THROW_WATER);
			}
			if (arc != null && arc.getBehavior() instanceof WaterArcBehavior.PlayerControlled && !(bD.hasStatusControl(StatusControl.THROW_WATER))) {
				bD.addStatusControl(StatusControl.THROW_WATER);
			}

		}
	}

	@Override
	protected WaterControlPoint createControlPoint(float size, int index) {
		return new WaterControlPoint(this, size, 0, 0, 0);
	}

	public boolean canPlaySplash() {
		return lastPlayedSplash == -1;
	}

	public void playSplash() {
		world.playSound(posX, posY, posZ, SoundEvents.ENTITY_GENERIC_SWIM, SoundCategory.PLAYERS, 0.3f,
				1.5f, false);
		lastPlayedSplash = 0;
	}

	public WaterArcBehavior getBehavior() {
		return dataManager.get(SYNC_BEHAVIOR);
	}

	public void setBehavior(WaterArcBehavior behavior) {
		dataManager.set(SYNC_BEHAVIOR, behavior);
	}

	@Override
	public EntityLivingBase getController() {
		return getBehavior() instanceof WaterArcBehavior.PlayerControlled ? getOwner() : null;
	}

	public void cleanup() {
		if (getOwner() != null) {
			BendingData data = Objects.requireNonNull(Bender.get(getOwner())).getData();
			if (data != null) {
				data.removeStatusControl(THROW_WATER);
			}
		}
	}

	private void breakCollidingBlocks() {
		// Hitbox expansion (in each direction) to destroy blocks before the
		// waterarc collides with them
		double expansion = 0.1;
		AxisAlignedBB hitbox = getEntityBoundingBox().grow(expansion, expansion, expansion);

		for (int ix = 0; ix <= 1; ix++) {
			for (int iz = 0; iz <= 1; iz++) {

				double x = ix == 0 ? hitbox.minX : hitbox.maxX;
				double y = hitbox.minY;
				double z = iz == 0 ? hitbox.minZ : hitbox.maxZ;
				BlockPos pos = new BlockPos(x, y, z);

				tryBreakBlock(world.getBlockState(pos), pos);

			}
		}
	}

	/**
	 * Assuming the waterarc can break blocks, tries to break the block.
	 */
	private void tryBreakBlock(IBlockState state, BlockPos pos) {
		if (state.getBlock() == Blocks.AIR || !STATS_CONFIG.waterArcBreakableBlocks.contains(state.getBlock())) {
			return;
		}

		float hardness = state.getBlockHardness(world, pos);
		if (hardness <= 4) {
			breakBlock(pos);
			setVelocity(velocity().times(0.75));
		}
	}

	public static class WaterControlPoint extends ControlPoint {

		private WaterControlPoint(EntityArc arc, float size, double x, double y, double z) {
			super(arc, size, x, y, z);
		}

	}

	@Override
	protected double getVelocityMultiplier() {
		return velocityMultiplier;
	}
}
