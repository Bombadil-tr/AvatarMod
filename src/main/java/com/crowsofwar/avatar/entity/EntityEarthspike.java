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

package com.crowsofwar.avatar.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author CrowsOfWar
 */
public class EntityEarthspike extends EntityOffensive {

    public EntityEarthspike(World world) {
        super(world);
        this.noClip = true;
    }


    @Override
    public void onEntityUpdate() {
        // Add width and height stuff
        super.onUpdate();

        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;


        // Destroy non-solid blocks in the Earthspike
        IBlockState inBlock = world.getBlockState(getPosition());
        if (inBlock.getBlock() != Blocks.AIR && !inBlock.isFullBlock()) {
            if (inBlock.getBlockHardness(world, getPosition()) == 0) {
                breakBlock(getPosition());
            } else {
                setDead();
            }
        }
    }

}
