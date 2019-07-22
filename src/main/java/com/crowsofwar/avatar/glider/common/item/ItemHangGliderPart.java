package com.crowsofwar.avatar.glider.common.item;

import com.crowsofwar.avatar.AvatarInfo;
import com.crowsofwar.avatar.common.item.AvatarItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import static com.crowsofwar.avatar.AvatarInfo.MOD_ID;

public class ItemHangGliderPart extends Item {

    public static String[] names = {"wing_left", "wing_right", "scaffolding"};

    public ItemHangGliderPart() {
        super();
        setCreativeTab(AvatarItems.tabItems);
        setHasSubtypes(true);
        setTranslationKey(MOD_ID +":" + AvatarInfo.ITEM_GLIDER_PART_NAME+ ".");
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
        if (isInCreativeTab(tab)) {
            for (int i = 0; i < names.length; i++)
                subItems.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + names[stack.getItemDamage()];
    }

}
