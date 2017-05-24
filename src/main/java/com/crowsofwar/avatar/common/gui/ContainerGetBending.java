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
package com.crowsofwar.avatar.common.gui;

import static com.crowsofwar.avatar.common.item.ItemScroll.getScrollType;
import static net.minecraft.item.ItemStack.field_190927_a;

import com.crowsofwar.avatar.common.item.AvatarItems;
import com.crowsofwar.avatar.common.item.ItemScroll.ScrollType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * 
 * 
 * @author CrowsOfWar
 */
public class ContainerGetBending extends Container {
	
	private final GetBendingInventory inventory;
	
	private int invIndex, hotbarIndex;
	private float incompatibleMsgTicks;
	
	public ContainerGetBending(EntityPlayer player) {
		
		inventory = new GetBendingInventory();
		incompatibleMsgTicks = -1;
		
		addSlotToContainer(new ScrollSlot(inventory, 0, -18, -18));
		addSlotToContainer(new ScrollSlot(inventory, 1, -18, -18));
		addSlotToContainer(new ScrollSlot(inventory, 2, -18, -18));
		
		// Main inventory
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 9; c++) {
				int id = c + r * 9 + 9;
				Slot slot = new Slot(player.inventory, id, 100, 100);
				addSlotToContainer(slot);
				if (r == 0 && c == 0) {
					invIndex = slot.slotNumber;
				}
			}
		}
		
		// Hotbar
		for (int i = 0; i < 9; i++) {
			Slot slot = new Slot(player.inventory, i, 100, 100);
			addSlotToContainer(slot);
			if (i == 0) {
				hotbarIndex = slot.slotNumber;
			}
		}
		
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		
		Slot slot = inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			
			if (index >= 0 && index <= 2) {
				if (!mergeItemStack(stack, 1, 37, true)) {
					return field_190927_a;
				}
			} else {
				if (!mergeItemStack(stack, 0, 3, true)) {
					return field_190927_a;
				}
			}
			
			return stack;
			
		}
		
		return field_190927_a;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
	
	public int getSize() {
		return inventory.getSizeInventory();
	}
	
	public int getInvIndex() {
		return invIndex;
	}
	
	public int getHotbarIndex() {
		return hotbarIndex;
	}
	
	/**
	 * Returns the ticks left to display incompatible scrolls message, or -1 if
	 * no display
	 */
	public float getIncompatibleMsgTicks() {
		return incompatibleMsgTicks;
	}
	
	public void decrementIncompatibleMsgTicks(float amount) {
		incompatibleMsgTicks -= amount;
	}
	
	private class ScrollSlot extends Slot {
		
		public ScrollSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
		
		@Override
		public boolean isItemValid(ItemStack stack) {
			if (stack.getItem() == AvatarItems.itemScroll) {
				
				ScrollType type1 = getScrollType(stack);
				
				for (int i = 0; i <= 2; i++) {
					ItemStack stack2 = getSlot(i).getStack();
					if (!stack2.func_190926_b()) {
						ScrollType type2 = getScrollType(stack2);
						if (!type1.isCompatibleWith(type2)) {
							incompatibleMsgTicks = 100;
							return false;
						}
					}
				}
				
				return true;
				
			}
			
			return false;
		}
		
	}
	
}
