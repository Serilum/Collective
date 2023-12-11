package com.natamus.collective.functions;

import com.natamus.collective.data.GlobalVariables;
import com.natamus.collective.util.CollectiveReference;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;

public class EntityFunctions {
	// START: CHECK functions
	public static Boolean isHorse(Entity entity) {
		return entity instanceof AbstractHorse;
	}

	public static boolean isModdedVillager(String entitystring) {
		String type = entitystring.split("\\[")[0];
		for (String moddedvillager : GlobalVariables.moddedvillagers) {
			if (type.equalsIgnoreCase(moddedvillager)) {
				return true;
			}
		}
		return false;
	}
	public static boolean isModdedVillager(Entity entity) {
		String entitystring = getEntityString(entity);
		return isModdedVillager(entitystring);
	}

	public static boolean isMilkable(Entity entity) {
		if (entity instanceof Sheep || entity instanceof Llama || entity instanceof Pig || entity instanceof Donkey || entity instanceof Horse || entity instanceof Mule) {
			if (!(entity instanceof Animal)) {
				return false;
			}

			Animal animal = (Animal)entity;
			return !animal.isBaby();
		}
		return false;
	}
	// END: CHECK functions


	// START: GET functions
	public static String getEntityString(Entity entity) {
		String entitystring = "";

		ResourceLocation rl = EntityType.getKey(entity.getType());
		if (rl != null) {
			entitystring = rl.toString(); // minecraft:villager, minecraft:wandering_trader
			if (entitystring.contains(":")) {
				entitystring = entitystring.split(":")[1];
			}

			entitystring = StringFunctions.capitalizeEveryWord(entitystring.replace("_", " ")).replace(" ", "").replace("Entity", ""); // Villager, WanderingTrader
		}

		return entitystring;
	}
	// END: GET functions


	// Do functions
	public static void nameEntity(Entity entity, String name) {
		if (!name.equals("")) {
			entity.setCustomName(Component.literal(name));
		}
	}

	public static void addPotionEffect(Entity entity, MobEffect effect, Integer ms) {
		MobEffectInstance freeze = new MobEffectInstance(effect, ms/50);
		LivingEntity le = (LivingEntity)entity;
		le.addEffect(freeze);
	}
	public static void removePotionEffect(Entity entity, MobEffect effect) {
		LivingEntity le = (LivingEntity)entity;
		le.removeEffect(effect);
	}

	public static void chargeEntity(Entity entity) {
		Level world = entity.level();
		if (entity instanceof Creeper) {
			entity.getEntityData().set(Creeper.DATA_IS_POWERED, true);
		}
		else if (entity instanceof MushroomCow) {
			((MushroomCow)entity).setVariant(MushroomCow.MushroomType.BROWN);
		}
	}

	public static void setEntityFlag(Entity entity, int flag, boolean set) {
		entity.setSharedFlag(flag, set);
	}

	public static void resetMerchantOffers(Villager villager) {
		for (MerchantOffer offer : villager.getOffers()) {
			resetMerchantOffer(offer);
		}
	}
	public static void resetMerchantOffers(WanderingTrader wanderingTrader) {
		for (MerchantOffer offer : wanderingTrader.getOffers()) {
			resetMerchantOffer(offer);
		}
	}
	public static void resetMerchantOffer(MerchantOffer offer) {
		offer.uses = 0;
		offer.maxUses = Integer.MAX_VALUE;
		offer.demand = 0;
	}

	public static void forceSetHealth(LivingEntity livingEntity, float health) {
		livingEntity.getEntityData().set(LivingEntity.DATA_HEALTH_ID, health);
	}

	public static boolean fishingHookHasCatch(FishingHook fishingHook) {
		return fishingHook.biting;
	}

	public static void transferItemsBetweenEntities(Entity from, Entity to, boolean ignoremainhand) {
		if (!(from instanceof Mob)) {
			return;
		}
		Mob mobfrom = (Mob)from;

		for(EquipmentSlot equipmentslottype : EquipmentSlot.values()) {
			if (ignoremainhand) {
				if (equipmentslottype.equals(EquipmentSlot.MAINHAND)) {
					continue;
				}
			}

			ItemStack itemstack = mobfrom.getItemBySlot(equipmentslottype);
			if (!itemstack.isEmpty()) {
				to.setItemSlot(equipmentslottype, itemstack.copy());
			}
		}
	}
	public static void transferItemsBetweenEntities(Entity from, Entity to) {
		transferItemsBetweenEntities(from, to, false);
	}

	public static Boolean doesEntitySurviveThisDamage(Player player, int halfheartdamage) {
		return doesEntitySurviveThisDamage((LivingEntity)player, halfheartdamage);
	}
	public static Boolean doesEntitySurviveThisDamage(LivingEntity entity, int halfheartdamage) {
		Level level = entity.level();
		float newhealth = entity.getHealth() - (float)halfheartdamage;
		if (newhealth > 0f) {
			entity.hurt(level.damageSources().magic(), 0.1F);
			entity.setHealth(newhealth);
		}
		else {
			entity.hurt(level.damageSources().magic(), Float.MAX_VALUE);
			return false;
		}
		return true;
	}

	public static Boolean isEntityFromSpawner(Entity entity) {
		if (entity == null) {
			return false;
		}
		return entity.getTags().contains(CollectiveReference.MOD_ID + ".fromspawner");
	}

	public static void setEntitySize(Entity entity, EntityDimensions entityDimensions, float eyeHight) {
		entity.dimensions = entityDimensions;
		entity.eyeHeight = eyeHight;
	}

	public static GoalSelector getGoalSelector(Mob mob) {
		return mob.goalSelector;
	}
	public static GoalSelector getTargetSelector(Mob mob) {
		return mob.targetSelector;
	}
}
