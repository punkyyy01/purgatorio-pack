package com.purgatorio.core.mixin;

import com.purgatorio.core.PurgatorioCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Bonus de dano del Alma. {@code hurtServer} es el punto unico de dano; {@code getEntity()} es el
 * causante (el jugador aunque el dano lo haga una flecha), asi que cubre cuerpo a cuerpo y distancia.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float purgatorio$almaDamageBonus(float damage, ServerLevel level, DamageSource source) {
		Entity attacker = source.getEntity();
		if (attacker instanceof ServerPlayer player && attacker != (Object) this && damage > 0.0F) {
			return (float) (damage * PurgatorioCore.alma().damageMultiplier(player));
		}
		return damage;
	}
}
