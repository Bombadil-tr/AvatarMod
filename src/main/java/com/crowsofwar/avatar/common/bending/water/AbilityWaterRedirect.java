package com.crowsofwar.avatar.common.bending.water;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.bending.BendingStyle;

import java.util.UUID;

public class AbilityWaterRedirect extends Ability {

	public AbilityWaterRedirect() {
		super(Waterbending.ID, "water_redirect");
	}

	@Override
	public int getBaseTier() {
		return 3;
	}

	@Override
	public boolean isVisibleInRadial() {
		return false;
	}

	@Override
	public boolean isUtility() {
		return true;
	}
}