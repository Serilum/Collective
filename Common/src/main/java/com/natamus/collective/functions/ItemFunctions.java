package com.natamus.collective.functions;

import com.natamus.collective.config.CollectiveConfigHandler;
import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.fakeplayer.FakePlayer;
import com.natamus.collective.fakeplayer.FakePlayerFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class ItemFunctions {
	public static void generateEntityDropsFromLootTable(Level level) {
		MinecraftServer server = level.getServer();
		if (server == null) {
			return;
		}
		
		GlobalVariables.entitydrops = new HashMap<EntityType<?>, List<Item>>();
		
		FakePlayer fakeplayer = FakePlayerFactory.getMinecraft((ServerLevel)level);
		Vec3 vec = new Vec3(0, 0, 0);
		
		ItemStack lootingsword = new ItemStack(Items.DIAMOND_SWORD, 1);
		lootingsword.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.LOOTING), 10);
		fakeplayer.setItemSlot(EquipmentSlot.MAINHAND, lootingsword);
		
		Collection<Entry<ResourceKey<EntityType<?>>, EntityType<?>>> entitytypes = level.registryAccess().registryOrThrow(Registries.ENTITY_TYPE).entrySet();
		for (Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : entitytypes) {
			try {
				EntityType<?> type = entry.getValue();
				if (type == null) {
					continue;
				}
				Entity entity = type.create(level);
				if (!(entity instanceof LivingEntity)) {
					continue;
				}

				LivingEntity le = (LivingEntity)entity;
				ResourceKey<LootTable> lootlocation = le.getType().getDefaultLootTable();

				LootTable loottable = server.reloadableRegistries().getLootTable(lootlocation);
				LootParams lootParams = new LootParams.Builder((ServerLevel)level)
						.withLuck(1000000F)
						.withParameter(LootContextParams.THIS_ENTITY, entity)
						.withParameter(LootContextParams.ORIGIN, vec)
						.withParameter(LootContextParams.ATTACKING_ENTITY, fakeplayer)
						.withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().playerAttack(fakeplayer))
						.create(LootContextParamSets.ENTITY);

				List<Item> alldrops = new ArrayList<Item>();
				for (int n = 0; n < CollectiveConfigHandler.loopsAmountUsedToGetAllEntityDrops; n++) {
					List<ItemStack> newdrops = loottable.getRandomItems(lootParams);
					for (ItemStack newdrop : newdrops) {
						Item newitem = newdrop.getItem();
						if (!alldrops.contains(newitem) && !newitem.equals(Items.AIR)) {
							alldrops.add(newitem);
						}
					}
				}

				GlobalVariables.entitydrops.put(type, alldrops);
			}
			catch (Exception ignored) { }
		}
	}
	
	public static void shrinkGiveOrDropItemStack(Player player, InteractionHand hand, ItemStack used, ItemStack give) {
		used.shrink(1);
		
		if (used.isEmpty()) {
			Item giveitem = give.getItem();
			int maxstacksize = give.getMaxStackSize();
			List<ItemStack> inventory = player.getInventory().items;
			
			boolean increased = false;
			for (ItemStack slot : inventory) {
				if (slot.getItem().equals(giveitem)) {
					int slotcount = slot.getCount();
					if (slotcount < maxstacksize) {
						slot.setCount(slotcount + 1);
						increased = true;
						break;
					}
				}
			}
			
			if (!increased) {
				player.setItemInHand(hand, give);
			}
		}
		else if (!player.getInventory().add(give)) {
			player.drop(give, false);
		}
	}
	
	public static void giveOrDropItemStack(Player player, ItemStack give) {
		if (!player.getInventory().add(give)) {
			player.drop(give, false);
		}
	}

	public static void itemHurtBreakAndEvent(ItemStack itemStack, ServerPlayer player, InteractionHand hand, int damage) {
		Level level = player.level();
		if (level.isClientSide) {
			return;
		}

		if (!(player.getAbilities().instabuild)) {
			if (itemStack.isDamageableItem()) {
				itemStack.hurtAndBreak(damage, (ServerLevel)level, player, (Item item) -> {
					//CollectiveItemEvents.ON_ITEM_DESTROYED.invoker().onItemDestroyed(player, itemStack, hand); // TODO

					itemStack.shrink(1);
					itemStack.setDamageValue(0);

					player.awardStat(Stats.ITEM_BROKEN.get(item));
				});
			}
		}
	}
	
	public static boolean isStoneTypeItem(Item item) {
		return GlobalVariables.stoneblockitems.contains(item);
	}
	
	public static String itemToReadableString(Item item, int amount) {
		String itemstring;
		String translationkey = item.getDescriptionId();
		if (translationkey.contains("block.")) {
			return BlockFunctions.blockToReadableString(Block.byItem(item), amount);
		}
		
		String[] itemspl = translationkey.replace("item.", "").split("\\.");
		if (itemspl.length > 1) {
			itemstring = itemspl[1];
		}
		else {
			itemstring = itemspl[0];
		}
		
		itemstring = itemstring.replace("_", " ");
		if (amount > 1) {
			itemstring = itemstring + "s";
		}
		
		return itemstring;
	}
	public static String itemToReadableString(Item item) {
		return itemToReadableString(item, 1);
	}

	public static String getNBTStringFromItemStack(Level level, ItemStack itemStack) {
		return getNBTStringFromItemStack(level, itemStack, true);
	}
	public static String getNBTStringFromItemStack(Level level, ItemStack itemStack, boolean shouldSanitize) {
		Tag nbt = itemStack.save(level.registryAccess());

		String nbtstring = nbt.toString();
		if (shouldSanitize) {
			nbtstring = nbtstring.replace(" : ", ": ");
		}

		return nbtstring;
	}

	public static void setItemCategory(Item item, CreativeModeTab category) { }
}