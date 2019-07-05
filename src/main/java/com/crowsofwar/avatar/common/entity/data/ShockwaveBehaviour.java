package com.crowsofwar.avatar.common.entity.data;

import com.crowsofwar.avatar.common.entity.EntityShockwave;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;

public abstract class ShockwaveBehaviour extends Behavior<EntityShockwave> {

	public static final DataSerializer<ShockwaveBehaviour> DATA_SERIALIZER = new Behavior.BehaviorSerializer<>();

	public ShockwaveBehaviour() {
	}

	public static void register() {
		DataSerializers.registerSerializer(DATA_SERIALIZER);
		registerBehavior(Idle.class);
	}

	public static class Idle extends ShockwaveBehaviour {

		@Override
		public Behavior onUpdate(EntityShockwave entity) {
			return this;
		}

		@Override
		public void fromBytes(PacketBuffer buf) {

		}

		@Override
		public void toBytes(PacketBuffer buf) {

		}

		@Override
		public void load(NBTTagCompound nbt) {

		}

		@Override
		public void save(NBTTagCompound nbt) {

		}
	}
}
