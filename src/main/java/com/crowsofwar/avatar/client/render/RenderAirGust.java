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

package com.crowsofwar.avatar.client.render;

import com.crowsofwar.avatar.common.entity.ControlPoint;
import com.crowsofwar.avatar.common.entity.EntityArc;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.Random;

import static net.minecraft.client.renderer.GlStateManager.disableLight;
import static net.minecraft.client.renderer.GlStateManager.disableLighting;

public class RenderAirGust extends RenderArc {

	public static final ResourceLocation TEXTURE = new ResourceLocation("avatarmod",
			"textures/entity/air-ribbon.png");

	private static final Random random = new Random();

	/**
	 * @param renderManager
	 */
	public RenderAirGust(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double xx, double yy, double zz, float p_76986_8_, float partialTicks) {
		disableLighting();
		GlStateManager.color(1, 1, 1, 1);
		EntityArc arc = (EntityArc) entity;
		renderArc(arc, partialTicks, 1, 1);
	}

	@Override
	protected void onDrawSegment(EntityArc arc, ControlPoint first, ControlPoint second) {

		World world = arc.world;
		AxisAlignedBB boundingBox = first.getBoundingBox();
		double spawnX = boundingBox.minX + random.nextDouble() * (boundingBox.maxX - boundingBox.minX);
		double spawnY = boundingBox.minY + random.nextDouble() * (boundingBox.maxY - boundingBox.minY);
		double spawnZ = boundingBox.minZ + random.nextDouble() * (boundingBox.maxZ - boundingBox.minZ);
		world.spawnParticle(EnumParticleTypes.CLOUD, spawnX, spawnY, spawnZ, 0, 0, 0);
		disableLight(7);

	}

	@Override
	protected ResourceLocation getTexture() {
		return TEXTURE;
	}
}
