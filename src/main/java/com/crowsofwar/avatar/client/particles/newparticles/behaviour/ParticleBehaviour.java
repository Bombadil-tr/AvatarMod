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

package com.crowsofwar.avatar.client.particles.newparticles.behaviour;

import com.crowsofwar.avatar.AvatarLog;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes a synced behavior. They follow the state design pattern, in that
 * each behavior should be switchable over an entity, and is responsible for an
 * update tick. Typically, behaviors are static inner classes, where the outer
 * class extends Behavior and is the superclass of the inner classes.
 * <p>
 * All custom behaviors must be registered via {@link #registerBehavior(Class)}.
 * It's unnecessary to sync this server-side as it's only used for particles.
 *
 * @param <E> The particle this behaviour is for
 * @author FavouriteDragon
 */
public abstract class ParticleBehaviour<E extends Particle> {

	private static int nextId = 1;
	private static Map<Integer, Class<? extends ParticleBehaviour>> behaviorIdToClass;
	private static Map<Class<? extends ParticleBehaviour>, Integer> classToBehaviorId;

	public ParticleBehaviour() {
	}

	// Static method called from preInit
	public static void registerBehaviours() {
		ParticleAvatarBehaviour.register();
	}

	protected static int registerBehavior(Class<? extends ParticleBehaviour> behaviorClass) {
		if (behaviorIdToClass == null) {
			behaviorIdToClass = new HashMap<>();
			classToBehaviorId = new HashMap<>();
			nextId = 1;
		}
		int id = nextId++;
		behaviorIdToClass.put(id, behaviorClass);
		classToBehaviorId.put(behaviorClass, id);
		return id;
	}

	/**
	 * Looks up the behavior class by the given Id, then instantiates an instance
	 * with reflection.
	 */
	public static ParticleBehaviour lookup(int id, Entity entity) {
		try {

			ParticleBehaviour behaviour = behaviorIdToClass.get(id).newInstance();
			return behaviour;

		} catch (Exception e) {

			AvatarLog.error("Error constructing particle behaviour...");
			e.printStackTrace();
			return null;

		}
	}

	public int getId() {
		return classToBehaviorId.get(getClass());
	}

	/**
	 * Called every update tick.
	 *
	 * @return Next Behavior. Return <code>this</code> to continue the Behavior.
	 * May never return null.
	 */

	@Nonnull
	public abstract ParticleBehaviour onUpdate(E particle);

	public abstract void fromBytes(PacketBuffer buf);

	public abstract void toBytes(PacketBuffer buf);

	public abstract void load(NBTTagCompound nbt);

	public abstract void save(NBTTagCompound nbt);


	/*public static class BehaviorSerializer<B extends ParticleBehaviour<? extends Particle>> implements DataSerializer<B> {

		// FIXME research- why doesn't read/write get called every time that
		// behavior changes???

		@Override
		public void write(PacketBuffer buf, B value) {
			buf.writeInt(value.getId());
			value.toBytes(buf);
		}

		@Override
		public B read(PacketBuffer buf) throws IOException {
			try {

				ParticleBehaviour behavior = behaviorIdToClass.get(buf.readInt()).newInstance();
				behavior.fromBytes(buf);
				return (B) behavior;

			} catch (Exception e) {

				AvatarLog.error("Error reading Behavior from bytes");
				e.printStackTrace();
				return null;

			}
		}

		@Override
		public DataParameter<B> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public B copyValue(B behavior) {
			return behavior;
		}

	}**/

}
