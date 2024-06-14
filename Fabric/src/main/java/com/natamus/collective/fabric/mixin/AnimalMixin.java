package com.natamus.collective.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.natamus.collective.fabric.callbacks.CollectiveAnimalEvents;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

@Mixin(value = Animal.class, priority = 1001)
public abstract class AnimalMixin extends AgeableMob {
	@Shadow private int inLove;
	
	protected AnimalMixin(EntityType<? extends AgeableMob> entityType, Level level) {
		super(entityType, level);
	}

	@ModifyVariable(method = "spawnChildFromBreeding", at = @At(value= "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/animal/Animal;getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/AgeableMob;", ordinal = 0), ordinal = 0)
	private AgeableMob Animal_spawnChildFromBreeding(AgeableMob ageablemob, ServerLevel serverLevel, Animal animal) {
		Animal parentA = (Animal)(Object)this;
		boolean shouldBreed = CollectiveAnimalEvents.PRE_BABY_SPAWN.invoker().onBabySpawn(serverLevel, parentA, animal, ageablemob);
		
		if (!shouldBreed) {
			ageablemob = null;	
			
			this.setAge(6000);
			animal.setAge(6000);
			this.inLove = 0;
			animal.setInLoveTime(0);
		}

		return ageablemob;
	}
}
