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
package com.crowsofwar.avatar.common.entity.mob;

import com.crowsofwar.avatar.common.bending.Abilities;
import com.crowsofwar.avatar.common.bending.air.Airbending;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.item.ItemScroll.ScrollType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.Objects;
import java.util.Random;

import static com.crowsofwar.avatar.common.config.ConfigMobs.MOBS_CONFIG;

/**
 * @author CrowsOfWar
 */
public class EntityAirbender extends EntityHumanBender {

	public static final ResourceLocation LOOT_TABLE = LootTableList
			.register(new ResourceLocation("avatarmod", "airbender"));

	/**
	 * @param world
	 */
	public EntityAirbender(World world) {
		super(world);

		BendingData data = BendingData.get(this);
		data.addBendingId(Airbending.ID);
		Random rand = new Random();
		int level = rand.nextInt(3) + 1;
		getData().getAbilityData("air_bubble").setLevel(level);
		getData().getAbilityData("air_gust").setLevel(level);
		getData().getAbilityData("airblade").setLevel(level);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);

	}

	@Override
	protected void addBendingTasks() {
		this.tasks.addTask(1, Objects.requireNonNull(Abilities.getAi("air_bubble", this, Bender.get(this))));
		this.tasks.addTask(1, Objects.requireNonNull(Abilities.getAi("air_gust", this, Bender.get(this))));
		this.tasks.addTask(1, Objects.requireNonNull(Abilities.getAi("airblade", this, Bender.get(this))));
		this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.5, true));
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}

	@Override
	protected ScrollType getScrollType() {
		return ScrollType.AIR;
	}

	@Override
	protected int getNumSkins() {
		return 7;
	}

	@Override
	protected boolean isTradeItem(Item item) {
		return super.isTradeItem(item) || MOBS_CONFIG.isAirTradeItem(item);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.ticksExisted % 20 == 0) {
			BendingData data = BendingData.get(this);
			data.addBendingId(Airbending.ID);
		}
	}
}
