package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveItemEvents;
import com.natamus.collective.fabric.callbacks.CollectivePlayerEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemEntity.class, priority = 1001)
public abstract class ItemEntityMixin extends Entity {
	public ItemEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", ordinal = 1))
	public void ItemEntity_tick(CallbackInfo ci) {
		ItemEntity itemEntity = (ItemEntity)(Object)this;
		ItemStack itemStack = itemEntity.getItem();
		CollectiveItemEvents.ON_ITEM_EXPIRE.invoker().onItemExpire(itemEntity, itemStack);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"), method = "playerTouch")
	public boolean playerTouch(Inventory inventory, ItemStack itemStack) {
		Player player = inventory.player;

		CollectivePlayerEvents.ON_ITEM_PICKED_UP.invoker().onItemPickedUp(player.level(), player, itemStack);

		return inventory.add(itemStack);
	}
}
