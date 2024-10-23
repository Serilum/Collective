package com.natamus.collective.fabric.mixin;

import com.natamus.collective.fabric.callbacks.CollectiveEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 1001)
public abstract class LivingEntityMixin {
	@Shadow protected ItemStack useItem;

	@Shadow public abstract InteractionHand getUsedItemHand();

	@Inject(method = "tick()V", at = @At(value = "HEAD"))
	public void LivingEntity_tick(CallbackInfo ci) {
		Entity entity = (Entity)(Object)this;
		Level world = entity.level();
		
		CollectiveEntityEvents.LIVING_TICK.invoker().onTick(world, entity);
	}
	
	@Inject(method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At(value = "HEAD"), cancellable = true)
	public void LivingEntity_die(DamageSource damageSource, CallbackInfo ci) {
		Entity entity = (Entity)(Object)this;
		if (!CollectiveEntityEvents.LIVING_ENTITY_DEATH.invoker().onDeath(entity.level(), entity, damageSource)) {
			ci.cancel();
		}
	}
	
	@Inject(method = "calculateFallDamage(FF)I", at = @At(value = "RETURN"), cancellable = true)
	protected void LivingEntity_calculateFallDamage(float fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		if (livingEntity.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
			return;
		}

		if (CollectiveEntityEvents.ON_FALL_DAMAGE_CALC.invoker().onFallDamageCalc(livingEntity.level(), livingEntity, fallDistance, damageMultiplier) == 0) {
			cir.setReturnValue(0);
		}
	}
	
	@ModifyVariable(method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V", at = @At(value= "INVOKE_ASSIGN", target = "Ljava/lang/Math;max(FF)F"), argsOnly = true)
	private float LivingEntity_actuallyHurt(float g, ServerLevel serverLevel, DamageSource damageSource, float damage) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		Level world = livingEntity.level();

		float newDamage = CollectiveEntityEvents.ON_LIVING_DAMAGE_CALC.invoker().onLivingDamageCalc(world, livingEntity, damageSource, g);
		if (newDamage != -1 && newDamage != g) {
			return newDamage;
		}
		
		return g;
	}

	@Inject(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "HEAD"), cancellable = true)
	public void LivingEntity_hurt(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		Level world = livingEntity.level();

		if (!CollectiveEntityEvents.ON_LIVING_ATTACK.invoker().onLivingAttack(world, livingEntity, damageSource, f)) {
			cir.setReturnValue(false);
		}
	}
	
	@Inject(method = "dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At(value = "TAIL"))
	protected void dropAllDeathLoot(ServerLevel serverLevel, DamageSource damageSource, CallbackInfo ci) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		Level world = livingEntity.level();
		
		CollectiveEntityEvents.ON_ENTITY_IS_DROPPING_LOOT.invoker().onDroppingLoot(world, livingEntity, damageSource);
	}
	
	@Inject(method = "jumpFromGround", at = @At(value = "TAIL"))
	protected void LivingEntity_jumpFromGround(CallbackInfo ci) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		Level world = livingEntity.level();
		
		CollectiveEntityEvents.ON_ENTITY_IS_JUMPING.invoker().onLivingJump(world, livingEntity);
	}
}
