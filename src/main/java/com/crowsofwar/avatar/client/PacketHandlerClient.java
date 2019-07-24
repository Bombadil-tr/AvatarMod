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

package com.crowsofwar.avatar.client;

import com.crowsofwar.avatar.AvatarLog;
import com.crowsofwar.avatar.AvatarLog.WarningType;
import com.crowsofwar.avatar.client.gui.AvatarUiRenderer;
import com.crowsofwar.avatar.client.gui.skills.SkillsGui;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.network.IPacketHandler;
import com.crowsofwar.avatar.common.network.packets.PacketCErrorMessage;
import com.crowsofwar.avatar.common.network.packets.PacketCOpenSkillCard;
import com.crowsofwar.avatar.common.network.packets.PacketCParticles;
import com.crowsofwar.avatar.common.network.packets.PacketCPowerRating;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

/**
 * Handles packets addressed to the client. Packets like this have a C in their
 * name.
 */
@SideOnly(Side.CLIENT)
public class PacketHandlerClient implements IPacketHandler {

	private final Minecraft mc;

	public PacketHandlerClient() {
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public IMessage onPacketReceived(IMessage packet, MessageContext ctx) {

		if (packet instanceof PacketCParticles)
			return handlePacketParticles((PacketCParticles) packet, ctx);

		if (packet instanceof PacketCErrorMessage)
			return handlePacketNotEnoughChi((PacketCErrorMessage) packet, ctx);

		if (packet instanceof PacketCPowerRating)
			return handlePacketPowerRating((PacketCPowerRating) packet, ctx);

		if (packet instanceof PacketCOpenSkillCard)
			return handlePacketSkillCard((PacketCOpenSkillCard) packet, ctx);

		AvatarLog.warn(WarningType.WEIRD_PACKET, "Client recieved unknown packet from server:" + packet);

		return null;
	}

	@Override
	public Side getSide() {
		return Side.CLIENT;
	}

	private IMessage handlePacketParticles(PacketCParticles packet, MessageContext ctx) {

		EnumParticleTypes particle = packet.getParticle();
		if (particle == null) {
			AvatarLog.warn(WarningType.WEIRD_PACKET, "Unknown particle recieved from server");
			return null;
		}

		Random random = new Random();

		int particles = random.nextInt(packet.getMaximum() - packet.getMinimum() + 1) + packet.getMinimum();

		for (int i = 0; i < particles; i++) {
			mc.world.spawnParticle(particle, packet.getX(), packet.getY(), packet.getZ(),
					packet.getMaxVelocityX() * random.nextGaussian(),
					packet.getMaxVelocityY() * random.nextGaussian(),
					packet.getMaxVelocityZ() * random.nextGaussian());
		}

		return null;
	}

	private IMessage handlePacketPowerRating(PacketCPowerRating packet, MessageContext ctx) {

		Map<UUID, Double> powerRatings = packet.getPowerRatings();
		BendingData data = BendingData.get(mc.player);

		Set<Map.Entry<UUID, Double>> entrySet = powerRatings.entrySet();
		for (Map.Entry<UUID, Double> entry : entrySet) {
			if (data.getPowerRatingManager(entry.getKey()) != null) {
				data.getPowerRatingManager(entry.getKey()).setCachedRatingValue(entry.getValue());
			}
		}

		return null;
	}

	private IMessage handlePacketNotEnoughChi(PacketCErrorMessage packet, MessageContext ctx) {

		AvatarUiRenderer.displayErrorMessage(packet.getMessage());

		return null;
	}

	private IMessage handlePacketSkillCard(PacketCOpenSkillCard packet, MessageContext ctx) {
		if (mc.currentScreen instanceof SkillsGui) {
			((SkillsGui) mc.currentScreen).openWindow(packet.getAbility());
		}
		return null;
	}

}
