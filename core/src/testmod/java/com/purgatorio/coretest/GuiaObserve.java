package com.purgatorio.coretest;

import com.purgatorio.guia.inspect.Descriptions;
import com.purgatorio.guia.inspect.Text;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;

/**
 * Mide con el juego lo que hacen de verdad los encantamientos de mods (purgatorio-guia-encantamientos.txt en el servidor
 * de pruebas): el valor real de cada atributo con y sin el encantamiento, y el resultado de cientos de golpes (efectos
 * aplicados, amplificador, duracion, probabilidad). Las descripciones de encantamientos de mods se escriben con lo
 * observado aqui porque algunos usan multiplicadores poco obvios (p. ej. sobre una base 0, que no hacen nada).
 */
final class GuiaObserve {
	private static final int TRIALS = 300;

	private record Attr(String ench, EquipmentSlot slot, String item, String attribute) {
	}

	private record Hit(String ench, String item, EntityType<?> victim) {
	}

	/** Texto que debe aparecer en la descripcion de un nivel, dado el valor base del atributo y el medido con el encantamiento. */
	private interface Expect {
		String of(double base, double value);
	}

	private static String rel(double base, double v) {
		return Text.num(Math.abs((v / base - 1) * 100));
	}

	/** Que numero de la descripcion se comprueba contra la medicion (por encantamiento y objeto). */
	private static final Map<String, Expect> CHECKS = new LinkedHashMap<>();

	static {
		CHECKS.put("dke:dragonhearted", (b, v) -> "+" + Text.num(v - b));
		CHECKS.put("enchantments:attack_speed", GuiaObserve::rel);
		CHECKS.put("enchantments:big_foot", (b, v) -> Text.num(v) + " bloques");
		CHECKS.put("enchantments:block_reach", GuiaObserve::rel);
		CHECKS.put("enchantments:burst", GuiaObserve::rel);
		CHECKS.put("enchantments:entity_reach", GuiaObserve::rel);
		CHECKS.put("enchantments:falling_wings", (b, v) -> Text.num(v) + " bloques");
		CHECKS.put("enchantments:gravity", GuiaObserve::rel);
		CHECKS.put("enchantments:haste", (b, v) -> "×" + Text.num(v));
		CHECKS.put("enchantments:healthy", (b, v) -> Text.num(v - b) + " punto");
		CHECKS.put("enchantments:jump", GuiaObserve::rel);
		CHECKS.put("enchantments:speed", GuiaObserve::rel);
		CHECKS.put("enchantments:strength", GuiaObserve::rel);
		CHECKS.put("enchantments:vision", GuiaObserve::rel);
		CHECKS.put("enchantsplus:crabs_touch", (b, v) -> "+" + Text.num(v - b));
		CHECKS.put("enchantsplus:outreach", (b, v) -> "+" + Text.num(v - b));
		CHECKS.put("enchantsplus:stride", (b, v) -> Text.num(v) + " bloques");
		CHECKS.put("enchantsplus:swift_strike", GuiaObserve::rel);
		CHECKS.put("enchantsplus:vitality", (b, v) -> "+" + Text.num(v - b));
		CHECKS.put("minecraft:blast_protection", (b, v) -> "-" + Text.num((v - b) * 100) + " %");
		CHECKS.put("minecraft:depth_strider", (b, v) -> Text.num((v - b) * 100) + " %");
		CHECKS.put("minecraft:fire_protection", (b, v) -> rel(b, v) + " % menos");
		CHECKS.put("minecraft:respiration", (b, v) -> "Aguantas " + Text.num(15 * (1 + v)) + " s");
		CHECKS.put("minecraft:swift_sneak", (b, v) -> "al " + Text.num(v * 100) + " %");
		CHECKS.put("minecraft:sweeping_edge", (b, v) -> Text.num(Math.round(v * 1000) / 10.0) + " %");
	}

	/**
	 * Comprueba que el numero de cada descripcion coincide con lo que el juego mide de verdad (CHECKS). Si un mod cambia
	 * sus valores, o un texto esta mal calculado, esto falla.
	 */
	static void verify(Ctx ctx) {
		ServerPlayer p = ctx.join("guia-verifica-atributos").player();
		for (Attr s : ATTR_SPECS) {
			Expect expect = CHECKS.get(s.ench());
			Holder<Enchantment> h = enchantment(ctx, s.ench());
			if (expect == null || h == null || (s.ench().equals("enchantments:knockback_protection"))
				|| BuiltInRegistries.ITEM.get(Identifier.parse(s.item())).isEmpty()) {
				continue;
			}
			var entry = Descriptions.get(s.ench());
			if (entry == null) {
				continue;
			}
			Holder<Attribute> attr = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(s.attribute())).orElseThrow();
			double base = put(p, s.slot(), ItemStack.EMPTY, new ItemStack(item(s.item())), attr);
			for (int lvl = 1; lvl <= h.value().getMaxLevel(); lvl++) {
				ItemStack st = new ItemStack(item(s.item()));
				st.enchant(h, lvl);
				double v = put(p, s.slot(), ItemStack.EMPTY, st, attr);
				String line = entry.effectAt(lvl);
				String wanted = expect.of(base, v);
				ctx.check(line != null && line.contains(wanted),
					s.ench() + " nivel " + lvl + ": el juego mide " + Text.num(v) + " (base " + Text.num(base) + "); se esperaba '" + wanted + "' en '" + line + "'");
			}
		}
		// Lifesteal: cuanto cura de verdad la Regeneracion de 1 s (cuantas veces dispara el efecto en 20 ticks).
		var lifesteal = Descriptions.get("enchantments:lifesteal");
		if (lifesteal != null) {
			for (int lvl = 1; lvl <= 3; lvl++) {
				int amp = 2 * (lvl - 1);
				int heals = 0;
				for (int d = 1; d <= 20; d++) {
					if (net.minecraft.world.effect.MobEffects.REGENERATION.value().shouldApplyEffectTickThisTick(d, amp)) {
						heals++;
					}
				}
				String line = lifesteal.effectAt(lvl);
				ctx.check(heals == 0 ? line.contains("no llega a curar") : line.contains("cura " + heals + (heals == 1 ? " vida" : " vidas")),
					"lifesteal nivel " + lvl + ": la regeneracion dispara " + heals + " veces en 1 s; el texto dice '" + line + "'");
			}
		}
	}

	private GuiaObserve() {
	}

	static void run(Ctx ctx) {
		StringBuilder out = new StringBuilder();
		out.append("######## ENTIDADES 'spider' REGISTRADAS Y SU PERTENENCIA A #sensitive_to_bane_of_arthropods\n");
		BuiltInRegistries.ENTITY_TYPE.listElements().forEach(h -> {
			if (h.key().identifier().getPath().contains("spider")) {
				out.append("  ").append(h.key().identifier()).append(" -> ")
					.append(h.is(net.minecraft.tags.TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("minecraft:sensitive_to_bane_of_arthropods")))).append('\n');
			}
		});
		out.append('\n');
		try {
			attributes(ctx, out);
			damages(ctx, out);
			hits(ctx, out);
			voidStep(ctx, out);
			capacity(ctx, out);
		} catch (Throwable t) {
			out.append("\nFALLO DEL ARNES: ").append(t).append('\n');
			for (StackTraceElement e : t.getStackTrace()) {
				out.append("   ").append(e).append('\n');
				if (e.getClassName().startsWith("com.purgatorio")) {
					break;
				}
			}
		}
		try {
			Files.writeString(Path.of("purgatorio-guia-encantamientos.txt"), out.toString(), StandardCharsets.UTF_8);
		} catch (java.io.IOException e) {
			ctx.check(false, "no se pudo escribir las mediciones: " + e);
		}
	}

	private static Holder<Enchantment> enchantment(Ctx ctx, String id) {
		return ctx.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
			.get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(id))).orElse(null);
	}

	private static Item item(String id) {
		return BuiltInRegistries.ITEM.get(Identifier.parse(id)).orElseThrow().value();
	}

	private static final List<Attr> ATTR_SPECS = List.of(
		new Attr("enchantments:attack_speed", EquipmentSlot.MAINHAND, "minecraft:diamond_sword", "minecraft:attack_speed"),
		new Attr("enchantments:big_foot", EquipmentSlot.FEET, "minecraft:diamond_boots", "minecraft:step_height"),
		new Attr("enchantments:block_reach", EquipmentSlot.HEAD, "minecraft:turtle_helmet", "minecraft:block_interaction_range"),
		new Attr("enchantments:burning_protection", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:burning_time"),
		new Attr("enchantments:burst", EquipmentSlot.MAINHAND, "minecraft:iron_spear", "minecraft:attack_speed"),
		new Attr("enchantments:curse_of_giantism", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:scale"),
		new Attr("enchantments:curse_of_smallism", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:scale"),
		new Attr("enchantments:entity_reach", EquipmentSlot.HEAD, "minecraft:turtle_helmet", "minecraft:entity_interaction_range"),
		new Attr("enchantments:falling_wings", EquipmentSlot.FEET, "minecraft:diamond_boots", "minecraft:safe_fall_distance"),
		new Attr("enchantments:gravity", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:gravity"),
		new Attr("enchantments:haste", EquipmentSlot.MAINHAND, "minecraft:diamond_pickaxe", "minecraft:block_break_speed"),
		new Attr("enchantments:healthy", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:max_health"),
		new Attr("enchantments:jump", EquipmentSlot.FEET, "minecraft:diamond_boots", "minecraft:jump_strength"),
		new Attr("enchantments:knockback_protection", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:knockback_resistance"),
		new Attr("enchantments:knockback_protection", EquipmentSlot.CHEST, "minecraft:netherite_chestplate", "minecraft:knockback_resistance"),
		new Attr("enchantments:speed", EquipmentSlot.FEET, "minecraft:diamond_boots", "minecraft:movement_speed"),
		new Attr("enchantments:strength", EquipmentSlot.MAINHAND, "minecraft:diamond_sword", "minecraft:attack_damage"),
		new Attr("enchantments:vision", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:camera_distance"),
		new Attr("dke:dragon_lungs", EquipmentSlot.HEAD, "minecraft:diamond_helmet", "minecraft:submerged_mining_speed"),
		new Attr("dke:dragon_lungs", EquipmentSlot.HEAD, "minecraft:diamond_helmet", "minecraft:oxygen_bonus"),
		new Attr("dke:dragonhearted", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:max_health"),
		new Attr("dke:wingspan", EquipmentSlot.MAINHAND, "minecraft:diamond_sword", "minecraft:sweeping_damage_ratio"),
		new Attr("enchantsplus:crabs_touch", EquipmentSlot.MAINHAND, "minecraft:diamond_pickaxe", "minecraft:block_interaction_range"),
		new Attr("enchantsplus:outreach", EquipmentSlot.MAINHAND, "minecraft:diamond_sword", "minecraft:entity_interaction_range"),
		new Attr("enchantsplus:stride", EquipmentSlot.LEGS, "minecraft:diamond_leggings", "minecraft:step_height"),
		new Attr("enchantsplus:swift_strike", EquipmentSlot.MAINHAND, "minecraft:diamond_sword", "minecraft:attack_speed"),
		new Attr("enchantsplus:vitality", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:max_health"),
		// vanilla, para comprobar la aritmetica de los textos ya escritos
		new Attr("minecraft:aqua_affinity", EquipmentSlot.HEAD, "minecraft:diamond_helmet", "minecraft:submerged_mining_speed"),
		new Attr("minecraft:blast_protection", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:explosion_knockback_resistance"),
		new Attr("minecraft:depth_strider", EquipmentSlot.FEET, "minecraft:diamond_boots", "minecraft:water_movement_efficiency"),
		new Attr("minecraft:fire_protection", EquipmentSlot.CHEST, "minecraft:diamond_chestplate", "minecraft:burning_time"),
		new Attr("minecraft:respiration", EquipmentSlot.HEAD, "minecraft:diamond_helmet", "minecraft:oxygen_bonus"),
		new Attr("minecraft:swift_sneak", EquipmentSlot.LEGS, "minecraft:diamond_leggings", "minecraft:sneaking_speed"),
		new Attr("minecraft:sweeping_edge", EquipmentSlot.MAINHAND, "minecraft:diamond_sword", "minecraft:sweeping_damage_ratio"));

	// ---- atributos: valor real con y sin el encantamiento puesto ----
	private static void attributes(Ctx ctx, StringBuilder out) {
		out.append("######## ATRIBUTOS (valor real del jugador; relativo = cambio frente al mismo objeto sin encantar)\n");
		ServerPlayer p = ctx.join("guia-obs-atributos").player();
		for (Attr s : ATTR_SPECS) {
			Holder<Enchantment> h = enchantment(ctx, s.ench());
			if (h == null || BuiltInRegistries.ITEM.get(Identifier.parse(s.item())).isEmpty()) {
				out.append(s.ench()).append(": (no existe en este servidor)\n");
				continue;
			}
			Holder<Attribute> attr = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(s.attribute())).orElseThrow();
			ItemStack current = ItemStack.EMPTY;
			ItemStack plain = new ItemStack(item(s.item()));
			double base = put(p, s.slot(), current, plain, attr);
			current = plain;
			out.append(String.format("%-38s %-14s sin encantar = %s%n", s.ench(), s.attribute().replace("minecraft:", ""), fmt(base)));
			for (int lvl = 1; lvl <= h.value().getMaxLevel(); lvl++) {
				ItemStack st = new ItemStack(item(s.item()));
				st.enchant(h, lvl);
				double v = put(p, s.slot(), current, st, attr);
				current = st;
				out.append(String.format("      nivel %d: %s  (diferencia %s, relativo %s %%)%n", lvl, fmt(v), fmt(v - base),
					base == 0 ? "n/a" : fmt((v / base - 1) * 100)));
			}
			put(p, s.slot(), current, ItemStack.EMPTY, attr);
		}
	}

	/**
	 * Valor del atributo con ese objeto en el hueco: se reunen los modificadores del objeto y los de sus encantamientos
	 * (EnchantmentHelper.forEachModifier, lo que usa el propio juego), se aplican al atributo real del jugador, se lee el
	 * valor con el calculo del juego (orden de operaciones incluido) y se retiran.
	 */
	private static double put(ServerPlayer p, EquipmentSlot slot, ItemStack previous, ItemStack next, Holder<Attribute> attr) {
		net.minecraft.world.entity.ai.attributes.AttributeInstance inst = p.getAttribute(attr);
		List<net.minecraft.world.entity.ai.attributes.AttributeModifier> applied = new java.util.ArrayList<>();
		java.util.function.BiConsumer<Holder<Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> add = (a, m) -> {
			if (a.equals(attr)) {
				inst.addTransientModifier(m);
				applied.add(m);
			}
		};
		next.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
			net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY).forEach(slot, add);
		EnchantmentHelper.forEachModifier(next, slot, add);
		double value = inst.getValue();
		applied.forEach(m -> inst.removeModifier(m.id()));
		return value;
	}

	// ---- dano: lo que suma de verdad cada encantamiento a un golpe (EnchantmentHelper.modifyDamage, lo que usa el juego) ----
	private record Dmg(String ench, String item, EntityType<?> victim) {
	}

	private static final List<Dmg> DAMAGE_SPECS = List.of(
		new Dmg("minecraft:sharpness", "minecraft:diamond_sword", EntityTypes.COW),
		new Dmg("minecraft:smite", "minecraft:diamond_sword", EntityTypes.ZOMBIE),
		new Dmg("minecraft:bane_of_arthropods", "minecraft:diamond_sword", EntityTypes.CAVE_SPIDER),
		new Dmg("minecraft:impaling", "minecraft:trident", EntityTypes.SQUID),
		new Dmg("enchantments:vanquish", "minecraft:diamond_sword", EntityTypes.WITHER),
		new Dmg("dke:dragonbane", "minecraft:diamond_sword", EntityTypes.ENDER_DRAGON),
		new Dmg("nova_structures:illagers_bane", "minecraft:diamond_sword", EntityTypes.PILLAGER),
		new Dmg("nova_structures:aerials_bane", "minecraft:diamond_sword", EntityTypes.BREEZE));

	private static double damageBonus(Ctx ctx, ServerPlayer attacker, Holder<Enchantment> h, int lvl, Dmg s) {
		ItemStack weapon = new ItemStack(item(s.item()));
		weapon.enchant(h, lvl);
		attacker.setItemInHand(InteractionHand.MAIN_HAND, weapon);
		Entity victim = s.victim().create(ctx.level, EntitySpawnReason.TRIGGERED);
		victim.snapTo(3.5, 80, 0.5, 0, 0);
		ctx.level.addFreshEntity(victim);
		float out = EnchantmentHelper.modifyDamage(ctx.level, weapon, victim, ctx.level.damageSources().playerAttack(attacker), 10.0F);
		victim.discard();
		return out - 10.0;
	}

	private static void damages(Ctx ctx, StringBuilder out) {
		out.append("\n######## DANO EXTRA (golpe base 10; EnchantmentHelper.modifyDamage)\n");
		ServerPlayer attacker = ctx.join("guia-obs-dano").player();
		for (Dmg s : DAMAGE_SPECS) {
			Holder<Enchantment> h = enchantment(ctx, s.ench());
			if (h == null) {
				out.append(s.ench()).append(": (no existe en este servidor)\n");
				continue;
			}
			StringBuilder row = new StringBuilder();
			for (int lvl = 1; lvl <= h.value().getMaxLevel(); lvl++) {
				row.append(String.format(" n%d=+%s", lvl, fmt(damageBonus(ctx, attacker, h, lvl, s))));
			}
			out.append(String.format("%-34s vs %-14s%s%n", s.ench(), s.victim().getDescriptionId().replace("entity.minecraft.", ""), row));
		}
	}

	/** Comprueba que el "+N" de cada descripcion de dano coincide con el que suma el juego. */
	static void verifyDamage(Ctx ctx) {
		ServerPlayer attacker = ctx.join("guia-verifica-dano").player();
		for (Dmg s : DAMAGE_SPECS) {
			Holder<Enchantment> h = enchantment(ctx, s.ench());
			var entry = Descriptions.get(s.ench());
			if (h == null || entry == null) {
				continue;
			}
			for (int lvl = 1; lvl <= h.value().getMaxLevel(); lvl++) {
				double bonus = damageBonus(ctx, attacker, h, lvl, s);
				String line = entry.effectAt(lvl);
				ctx.check(line != null && line.contains("+" + Text.num(bonus)),
					s.ench() + " nivel " + lvl + ": el juego suma +" + Text.num(bonus) + " de daño; el texto dice '" + line + "'");
			}
		}
	}

	// ---- golpes: efectos aplicados, amplificador, duracion y probabilidad ----
	private static void hits(Ctx ctx, StringBuilder out) {
		out.append("\n######## GOLPES (").append(TRIALS).append(" por nivel; EntityType de la victima entre parentesis)\n");
		List<Hit> specs = List.of(
			new Hit("enchantments:frozen", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantments:glow", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantments:invisibility", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantments:lifesteal", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantments:shulker", "minecraft:bow", EntityTypes.COW),
			new Hit("enchantments:wither", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantments:villager_healer", "minecraft:emerald", EntityTypes.COW),
			new Hit("enchantments:spider", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantments:evoking", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantsplus:ice_aspect", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantsplus:displacement_curse", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("enchantsplus:double_edge_curse", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("minecraft:bane_of_arthropods", "minecraft:diamond_sword", EntityTypes.SPIDER),
			new Hit("minecraft:bane_of_arthropods", "minecraft:diamond_sword", EntityTypes.CAVE_SPIDER),
			new Hit("minecraft:bane_of_arthropods", "minecraft:diamond_sword", EntityTypes.SILVERFISH),
			new Hit("minecraft:bane_of_arthropods", "minecraft:diamond_sword", EntityTypes.COW),
			new Hit("minecraft:fire_aspect", "minecraft:diamond_sword", EntityTypes.COW));
		ServerPlayer attacker = ctx.join("guia-obs-golpes").player();
		for (Hit s : specs) {
			Holder<Enchantment> h = enchantment(ctx, s.ench());
			if (h == null) {
				out.append(s.ench()).append(": (no existe en este servidor)\n");
				continue;
			}
			out.append("## ").append(s.ench()).append(" (").append(s.victim().getDescriptionId().replace("entity.minecraft.", "")).append(")\n");
			for (int lvl = 1; lvl <= h.value().getMaxLevel(); lvl++) {
				out.append("  nivel ").append(lvl).append(": ").append(trial(ctx, attacker, h, lvl, s)).append('\n');
			}
		}
	}

	private static final class Stat {
		int count;
		int ampMin = Integer.MAX_VALUE;
		int ampMax = Integer.MIN_VALUE;
		int durMin = Integer.MAX_VALUE;
		int durMax = Integer.MIN_VALUE;

		void add(MobEffectInstance e) {
			count++;
			ampMin = Math.min(ampMin, e.getAmplifier());
			ampMax = Math.max(ampMax, e.getAmplifier());
			durMin = Math.min(durMin, e.getDuration());
			durMax = Math.max(durMax, e.getDuration());
		}
	}

	private static String trial(Ctx ctx, ServerPlayer attacker, Holder<Enchantment> h, int lvl, Hit s) {
		ItemStack weapon = new ItemStack(item(s.item()));
		weapon.enchant(h, lvl);
		attacker.setItemInHand(InteractionHand.MAIN_HAND, weapon);
		attacker.setPos(0.5, 80, 0.5);
		LivingEntity victim = (LivingEntity) s.victim().create(ctx.level, EntitySpawnReason.TRIGGERED);
		victim.snapTo(3.5, 80, 0.5, 0, 0);
		ctx.level.addFreshEntity(victim);
		Map<String, Stat> onVictim = new LinkedHashMap<>();
		Map<String, Stat> onAttacker = new LinkedHashMap<>();
		int cobweb = 0;
		int moved = 0;
		int noAi = 0;
		int fire = 0;
		int fireMax = 0;
		double damageMin = Double.MAX_VALUE;
		double damageMax = 0;
		int damaged = 0;
		BlockPos pos = victim.blockPosition();
		for (int i = 0; i < TRIALS; i++) {
			victim.removeAllEffects();
			victim.clearFire();
			victim.snapTo(3.5, 80, 0.5, 0, 0);
			ctx.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			if (victim instanceof net.minecraft.world.entity.Mob mob) {
				mob.setNoAi(false);
			}
			attacker.removeAllEffects();
			attacker.setHealth(20.0F);
			Map<String, Integer> before = Map.of();
			EnchantmentHelper.doPostAttackEffects(ctx.level, victim, ctx.level.damageSources().playerAttack(attacker));
			for (MobEffectInstance e : victim.getActiveEffects()) {
				onVictim.computeIfAbsent(name(e), k -> new Stat()).add(e);
			}
			for (MobEffectInstance e : attacker.getActiveEffects()) {
				onAttacker.computeIfAbsent(name(e), k -> new Stat()).add(e);
			}
			if (ctx.level.getBlockState(pos).is(Blocks.COBWEB) || ctx.level.getBlockState(victim.blockPosition()).is(Blocks.COBWEB)) {
				cobweb++;
			}
			if (victim.distanceToSqr(3.5, 80, 0.5) > 0.04) {
				moved++;
			}
			if (victim instanceof net.minecraft.world.entity.Mob mob && mob.isNoAi()) {
				noAi++;
			}
			if (victim.getRemainingFireTicks() > 0) {
				fire++;
				fireMax = Math.max(fireMax, victim.getRemainingFireTicks());
			}
			double lost = 20.0 - attacker.getHealth();
			if (lost > 0) {
				damaged++;
				damageMin = Math.min(damageMin, lost);
				damageMax = Math.max(damageMax, lost);
			}
		}
		victim.discard();
		ctx.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		StringBuilder sb = new StringBuilder();
		onVictim.forEach((k, v) -> sb.append(String.format("VICTIMA %s %d/%d amp[%d..%d] dur[%d..%d]t; ", k, v.count, TRIALS, v.ampMin, v.ampMax, v.durMin, v.durMax)));
		onAttacker.forEach((k, v) -> sb.append(String.format("ATACANTE %s %d/%d amp[%d..%d] dur[%d..%d]t; ", k, v.count, TRIALS, v.ampMin, v.ampMax, v.durMin, v.durMax)));
		if (cobweb > 0) {
			sb.append("telarana ").append(cobweb).append('/').append(TRIALS).append("; ");
		}
		if (moved > 0) {
			sb.append("victima movida ").append(moved).append('/').append(TRIALS).append("; ");
		}
		if (noAi > 0) {
			sb.append("victima sin IA ").append(noAi).append('/').append(TRIALS).append("; ");
		}
		if (fire > 0) {
			sb.append("victima ardiendo ").append(fire).append('/').append(TRIALS).append(" max ").append(fireMax).append("t; ");
		}
		if (damaged > 0) {
			sb.append("DANO AL ATACANTE ").append(damaged).append('/').append(TRIALS).append(" [").append(fmt(damageMin)).append("..").append(fmt(damageMax)).append("]; ");
		}
		return sb.length() == 0 ? "(sin efecto observable)" : sb.toString();
	}

	/** Paso del vacio: ejecuta su funcion varias veces con un peto puesto y mide a donde te lleva y cuanta durabilidad cuesta. */
	private static void voidStep(Ctx ctx, StringBuilder out) {
		out.append("\n######## PASO DEL VACIO (funcion enchantments:functions/void_step, 12 ejecuciones)\n");
		if (enchantment(ctx, "enchantments:void_step") == null) {
			out.append("(no existe en este servidor)\n");
			return;
		}
		ServerPlayer p = ctx.join("guia-obs-vacio").player();
		double minD = Double.MAX_VALUE;
		double maxD = 0;
		for (int i = 0; i < 12; i++) {
			ItemStack chest = new ItemStack(item("minecraft:netherite_chestplate"));
			p.setItemSlot(EquipmentSlot.CHEST, chest);
			p.setPos(200.5, -80, 200.5);
			int before = chest.getMaxDamage() - chest.getDamageValue();
			net.minecraft.commands.CommandSourceStack src = ctx.server.createCommandSourceStack().withEntity(p).withPosition(p.position());
			ctx.server.getCommands().performPrefixedCommand(src, "function enchantments:functions/void_step");
			ItemStack after = p.getItemBySlot(EquipmentSlot.CHEST);
			double d = Math.sqrt(p.getX() * p.getX() + p.getZ() * p.getZ());
			minD = Math.min(minD, d);
			maxD = Math.max(maxD, d);
			if (i < 3) {
				out.append(String.format("  ejecucion %d: peto %s, durabilidad %d -> %s ; posicion (%.1f, %.1f, %.1f) a %.1f bloques del centro; resistencia=%s%n", i + 1,
					after.isEmpty() ? "ROTO" : "intacto", before, after.isEmpty() ? "0" : String.valueOf(after.getMaxDamage() - after.getDamageValue()),
					p.getX(), p.getY(), p.getZ(), d, p.hasEffect(net.minecraft.world.effect.MobEffects.RESISTANCE)));
			}
		}
		out.append(String.format("  distancia al centro (0,0): de %.1f a %.1f bloques%n", minD, maxD));
	}

	/** Capacidad de mochilas: cuantos huecos tiene de verdad cada mochila al abrirla con el encantamiento puesto. */
	private static void capacity(Ctx ctx, StringBuilder out) {
		out.append("\n######## CAPACIDAD DE MOCHILAS (huecos al abrir la mochila con serverbackpacks:capacity)\n");
		Holder<Enchantment> h = enchantment(ctx, "serverbackpacks:capacity");
		if (h == null) {
			out.append("(no existe en este servidor)\n");
			return;
		}
		ServerPlayer p = ctx.join("guia-obs-capacidad").player();
		for (String id : List.of("serverbackpacks:small", "serverbackpacks:medium", "serverbackpacks:large")) {
			StringBuilder row = new StringBuilder();
			for (int lvl = 0; lvl <= h.value().getMaxLevel(); lvl++) {
				ItemStack st = new ItemStack(item(id));
				if (lvl > 0) {
					st.enchant(h, lvl);
				}
				p.setItemInHand(InteractionHand.MAIN_HAND, st);
				p.closeContainer();
				st.use(ctx.level, p, InteractionHand.MAIN_HAND);
				row.append(String.format(" n%d=%d huecos", lvl, p.containerMenu.slots.size() - 36));
				p.closeContainer();
			}
			out.append(String.format("%-26s%s%n", id, row));
		}
	}

	private static String name(MobEffectInstance e) {
		return e.getEffect().unwrapKey().map(k -> k.identifier().getPath()).orElse("?");
	}

	private static String fmt(double v) {
		return String.format(java.util.Locale.ROOT, "%.4f", v);
	}
}
