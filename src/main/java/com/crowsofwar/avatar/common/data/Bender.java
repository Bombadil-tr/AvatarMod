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
package com.crowsofwar.avatar.common.data;

import com.crowsofwar.avatar.AvatarMod;
import com.crowsofwar.avatar.common.AvatarChatMessages;
import com.crowsofwar.avatar.common.QueuedAbilityExecutionHandler;
import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.data.ctx.AbilityContext;
import com.crowsofwar.avatar.common.data.ctx.PlayerBender;
import com.crowsofwar.avatar.common.entity.mob.EntityBender;
import com.crowsofwar.avatar.common.network.PacketHandlerServer;
import com.crowsofwar.avatar.common.network.packets.PacketCErrorMessage;
import com.crowsofwar.avatar.common.util.Raytrace;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * A wrapper for any mob/player that can bend to provide greater abstraction
 * over useful methods.
 * 
 * @author CrowsOfWar
 */
public abstract class Bender {
	
	/**
	 * For players, returns the username. For mobs, returns the mob's name (e.g.
	 * Chicken).
	 */
	public String getName() {
		return getEntity().getName();
	}
	
	/**
	 * Gets this bender in entity form
	 */
	public abstract EntityLivingBase getEntity();
	
	/**
	 * Get the world this entity is currently in
	 */
	public World getWorld() {
		return getEntity().world;
	}

	/**
	 * Returns whether this bender is a player
	 *
	 * @deprecated This ruins abstraction; <a href="https://trello.com/c/ph9WP946/210-remove-benderisplayer">to be removed</a>
	 */
	@Deprecated
	public boolean isPlayer() {
		return getEntity() instanceof EntityPlayer;
	}

	/**
	 * Get a BenderInfo object, a way to store the Bender's lookup information on disk so it can be
	 * found again later.
	 */
	public BenderInfo getInfo() {
		return BenderInfo.get(this);
	}
	
	public abstract BendingData getData();

	/**
	 * Returns whether this bender is in creative mode
	 *
	 * @deprecated This ruins abstraction; <a href="https://trello.com/c/ph9WP946/210-remove-benderisplayer">to be removed</a>
	 */
	@Deprecated
	public abstract boolean isCreativeMode();
	
	public abstract boolean isFlying();
	
	/**
	 * If any water pouches are in the inventory, checks if there is enough
	 * water. If there is, consumes the total amount of water in those pouches
	 * and returns true.
	 */
	public abstract boolean consumeWaterLevel(int amount);

	/**
	 * Tries to consume the given amount of chi from the Bender. Returns true if successful (ie
	 * there was enough chi); false on failure
	 */
	public boolean consumeChi(float amount) {
		// TODO Account for entity Chi?
		return true;
	};

	/**
	 * Checks whether the Bender can use that given ability.
	 */
	protected boolean canUseAbility(Ability ability) {
		BendingData data = getData();
		return data.hasBendingId(ability.getBendingId()) && !data.getAbilityData(ability)
				.isLocked();
	}

	/**
	 * Executes the given ability in the correct context. This will eventually lead to calling
	 * Ability{@link Ability#execute(AbilityContext)}.
	 * <p>
	 * In certain conditions, other action might be taken; e.g. on client, send a packet to the
	 * server to execute the ability.
	 *
	 * @param raytrace Raytrace performed client-side and sent to the server
	 */
	public void executeAbility(Ability ability, Raytrace.Result raytrace) {
		if (getWorld().isRemote) {
			// Client-side : Send packet to server

		} else {
			// Server-side : Execute the ability

			BendingData data = getData();
			EntityLivingBase entity = getEntity();
			if (canUseAbility(ability)) {
				if (data.getAbilityCooldown() == 0) {

					if (data.getCanUseAbilities()) {
						AbilityContext abilityCtx = new AbilityContext(data, raytrace, ability,
								entity);
						ability.execute(abilityCtx);
						data.setAbilityCooldown(ability.getCooldown(abilityCtx));
					} else {
						// TODO make bending disabled available for multiple things
						AvatarChatMessages.MSG_SKATING_BENDING_DISABLED.send(getEntity());
					}

				} else {
					QueuedAbilityExecutionHandler.queueAbilityExecution(entity, data, ability,
							raytrace);
				}
			} else {
				AvatarMod.network.sendTo(new PacketCErrorMessage("avatar.abilityLocked"), entity);
			}

		}
	}

	/**
	 * Sends an error message to the Bender. This is really only useful for players, which causes
	 * an error message to be displayed above the hotbar.
	 *
	 * @see com.crowsofwar.avatar.common.network.packets.PacketCErrorMessage
	 */
	public void sendMessage(String message) {}

	/**
	 * Creates an appropriate Bender instance for that entity
	 */
	@Nullable
	public static Bender get(@Nullable EntityLivingBase entity) {
		if (entity == null) {
			return null;
		} else if (entity instanceof EntityBender) {
			return ((EntityBender) entity).getBender();
		} else if (entity instanceof EntityPlayer) {
			return new PlayerBender((EntityPlayer) entity);
		} else {
			throw new IllegalArgumentException("Unsure how to get bender for entity " + entity);
		}
	}

	public static boolean isBenderSupported(EntityLivingBase entity) {
		return entity == null || entity instanceof EntityPlayer || entity instanceof EntityBender;
	}
	
}
