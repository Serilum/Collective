package com.natamus.collective.functions;

import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

public class MenuFunctions {
	public static Merchant getTraderFromMerchantMenu(MerchantMenu merchantMenu) {
		return merchantMenu.trader;
	}
}
