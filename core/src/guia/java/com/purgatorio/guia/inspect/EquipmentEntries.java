package com.purgatorio.guia.inspect;

import com.purgatorio.guia.gui.Ui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

/**
 * Armaduras, armas y herramientas. Las cifras salen del propio juego: los atributos que lleva ESTE objeto (sus
 * componentes, asi que valen tambien los que anaden mods como RPG Loot), el valor normal del jugador, la durabilidad,
 * las reglas de la herramienta... A mano solo se escribe que significa cada atributo (atributos.json).
 */
public final class EquipmentEntries {
	private static final int WRAP = 40;
	private static final int MAX_NAMES = 4;

	/** Icono de cada atributo; el resto usa un papel. */
	private static final Map<String, Item> ICONS = Map.ofEntries(
		Map.entry("minecraft:armor", Items.IRON_CHESTPLATE),
		Map.entry("minecraft:armor_toughness", Items.DIAMOND_CHESTPLATE),
		Map.entry("minecraft:attack_damage", Items.IRON_SWORD),
		Map.entry("minecraft:attack_speed", Items.CLOCK),
		Map.entry("minecraft:attack_knockback", Items.PISTON),
		Map.entry("minecraft:knockback_resistance", Items.ANVIL),
		Map.entry("minecraft:explosion_knockback_resistance", Items.TNT),
		Map.entry("minecraft:max_health", Items.GOLDEN_APPLE),
		Map.entry("minecraft:max_absorption", Items.ENCHANTED_GOLDEN_APPLE),
		Map.entry("minecraft:movement_speed", Items.SUGAR),
		Map.entry("minecraft:luck", Items.RABBIT_FOOT),
		Map.entry("minecraft:mining_efficiency", Items.IRON_PICKAXE),
		Map.entry("minecraft:block_break_speed", Items.IRON_PICKAXE),
		Map.entry("minecraft:jump_strength", Items.SLIME_BALL),
		Map.entry("minecraft:safe_fall_distance", Items.FEATHER),
		Map.entry("minecraft:fall_damage_multiplier", Items.FEATHER));

	/** Nombres legibles de las etiquetas de bloques que usan las herramientas. */
	private static final Map<String, String> BLOCK_TAGS = Map.of(
		"minecraft:mineable/pickaxe", "piedra, minerales y metales",
		"minecraft:mineable/axe", "madera",
		"minecraft:mineable/shovel", "tierra, arena y grava",
		"minecraft:mineable/hoe", "hojas, paja, musgo y sculk",
		"minecraft:sword_efficient", "hojas y plantas blandas",
		"minecraft:sword_instantly_mines", "bambú");

	/** Que significa que un objeto suelto resista un tipo de daño. */
	private static final Map<String, String> DAMAGE_TAGS = Map.of(
		"minecraft:is_fire", "fuego y lava",
		"minecraft:is_explosion", "explosiones",
		"minecraft:is_projectile", "proyectiles",
		"minecraft:is_lightning", "rayos");

	private EquipmentEntries() {
	}

	public static List<GuiElementBuilder> of(ServerPlayer player, ItemStack stack) {
		List<GuiElementBuilder> out = new ArrayList<>();

		ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
		if (mods != null) {
			Map<Holder<Attribute>, List<ItemAttributeModifiers.Entry>> byAttribute = new LinkedHashMap<>();
			for (ItemAttributeModifiers.Entry e : mods.modifiers()) {
				byAttribute.computeIfAbsent(e.attribute(), k -> new ArrayList<>()).add(e);
			}
			byAttribute.forEach((attribute, entries) -> out.add(attributeEntry(player, attribute, entries)));
		}
		GuiElementBuilder durability = durabilityEntry(stack);
		if (durability != null) {
			out.add(durability);
		}
		Tool tool = stack.get(DataComponents.TOOL);
		if (tool != null) {
			out.add(toolEntry(tool));
		}
		Weapon weapon = stack.get(DataComponents.WEAPON);
		if (weapon != null) {
			out.add(weaponEntry(weapon));
		}
		GuiElementBuilder equipment = equipmentEntry(stack);
		if (equipment != null) {
			out.add(equipment);
		}
		return out;
	}

	// ---- atributos ----
	public static String idOf(Holder<Attribute> holder) {
		return holder.unwrapKey().map(k -> k.identifier().toString()).orElse("?");
	}

	static GuiElementBuilder attributeEntry(ServerPlayer player, Holder<Attribute> attribute, List<ItemAttributeModifiers.Entry> entries) {
		String id = idOf(attribute);
		MutableComponent name = Component.translatable(attribute.value().getDescriptionId())
			.withStyle(ChatFormatting.AQUA).withStyle(s -> s.withBold(true).withItalic(false));
		GuiElementBuilder b = new GuiElementBuilder(ICONS.getOrDefault(id, Items.PAPER)).hideDefaultTooltip().setName(name);

		Descriptions.Entry d = Descriptions.attribute(id);
		if (d == null) {
			b.addLoreLine(Ui.text("Sin descripción todavía.", ChatFormatting.DARK_GRAY));
			b.addLoreLine(Ui.text(id, ChatFormatting.DARK_GRAY));
		} else {
			Text.wrap(d.desc(), WRAP).forEach(l -> b.addLoreLine(Ui.text(l, ChatFormatting.GRAY)));
		}
		b.addLoreLine(Ui.blank());

		AttributeInstance instance = player.getAttribute(attribute);
		double base = instance != null ? instance.getBaseValue() : attribute.value().getDefaultValue();
		b.addLoreLine(Ui.text("Valor normal: " + Text.num(base), ChatFormatting.DARK_AQUA));
		for (ItemAttributeModifiers.Entry e : entries) {
			AttributeModifier m = e.modifier();
			boolean percent = m.operation() != AttributeModifier.Operation.ADD_VALUE;
			String line = capitalize(slotLabel(e.slot())) + ": " + Text.signed(m.amount(), percent);
			// Cuando el valor normal no es 0 el total es lo que de verdad importa (daño del golpe, golpes por segundo...).
			if (!percent && base != 0) {
				line += "  (total: " + Text.num(base + m.amount()) + ")";
			}
			b.addLoreLine(Ui.text(line, ChatFormatting.YELLOW));
		}
		return b;
	}

	// ---- durabilidad, reparacion, encantabilidad ----
	static GuiElementBuilder durabilityEntry(ItemStack stack) {
		boolean unbreakable = stack.has(DataComponents.UNBREAKABLE);
		int max = stack.getMaxDamage();
		var enchantable = stack.get(DataComponents.ENCHANTABLE);
		var repairable = stack.get(DataComponents.REPAIRABLE);
		var resistant = stack.get(DataComponents.DAMAGE_RESISTANT);
		if (max <= 0 && !unbreakable && enchantable == null && resistant == null) {
			return null;
		}
		GuiElementBuilder b = new GuiElementBuilder(Items.ANVIL).hideDefaultTooltip()
			.setName(Ui.text("Durabilidad y reparación", ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
		if (unbreakable) {
			b.addLoreLine(Ui.text("Irrompible: no se desgasta.", ChatFormatting.YELLOW));
		} else if (max > 0) {
			int left = max - stack.getDamageValue();
			b.addLoreLine(Ui.text("Durabilidad: " + left + " de " + max + " (" + Text.num(100.0 * left / max) + " %)", ChatFormatting.YELLOW));
		}
		if (repairable != null && max > 0) {
			b.addLoreLine(Ui.text("Se repara en el yunque con:", ChatFormatting.GRAY));
			b.addLoreLine(namesOf(repairable.items()));
		}
		if (enchantable != null) {
			b.addLoreLine(Ui.blank());
			b.addLoreLine(Ui.text("Encantabilidad: " + enchantable.value(), ChatFormatting.AQUA));
			b.addLoreLine(Ui.text("Cuanto más alta, mejores encantamientos", ChatFormatting.GRAY));
			b.addLoreLine(Ui.text("salen en la mesa de encantamientos.", ChatFormatting.GRAY));
		}
		if (resistant != null) {
			b.addLoreLine(Ui.blank());
			b.addLoreLine(Ui.text("Como objeto suelto no se destruye por:", ChatFormatting.GRAY));
			b.addLoreLine(Ui.text(damageNames(resistant.types()), ChatFormatting.YELLOW));
		}
		return b;
	}

	/** "Lingote de hierro, Lingote de oro y 2 más": los objetos de un conjunto, con nombre que traduce el cliente. */
	private static Component namesOf(HolderSet<Item> items) {
		MutableComponent out = Component.empty().withStyle(s -> s.withItalic(false));
		List<Holder<Item>> all = items.stream().toList();
		int shown = 0;
		for (Holder<Item> item : all) {
			if (shown == MAX_NAMES) {
				break;
			}
			if (shown++ > 0) {
				out.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
			}
			out.append(new ItemStack(item).getHoverName().copy().withStyle(ChatFormatting.YELLOW));
		}
		if (all.size() > shown) {
			out.append(Component.literal(" y " + (all.size() - shown) + " más").withStyle(ChatFormatting.DARK_GRAY));
		}
		return out;
	}

	private static String damageNames(HolderSet<net.minecraft.world.damagesource.DamageType> types) {
		return types.unwrapKey().map(tag -> {
			String id = tag.location().toString();
			return DAMAGE_TAGS.getOrDefault(id, "#" + id);
		}).orElse("ciertos tipos de daño");
	}

	// ---- herramienta ----
	/** A partir de aqui el juego usa la velocidad como "rompe al instante" (Float.MAX_VALUE en el bambu de la espada). */
	private static final float INSTANT_SPEED = 1.0E6F;

	static GuiElementBuilder toolEntry(Tool tool) {
		GuiElementBuilder b = new GuiElementBuilder(Items.IRON_PICKAXE).hideDefaultTooltip()
			.setName(Ui.text("Herramienta", ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
		b.addLoreLine(Ui.text("Velocidad base de minado: ×" + Text.num(tool.defaultMiningSpeed()), ChatFormatting.YELLOW));
		for (Tool.Rule rule : tool.rules()) {
			if (rule.speed().isPresent()) {
				float speed = rule.speed().get();
				String head = speed >= INSTANT_SPEED ? "Rompe al instante: " : "×" + Text.num(speed) + " en: ";
				b.addLoreLine(Component.empty().append(Ui.text(head, ChatFormatting.YELLOW)).append(blockNames(rule.blocks()))
					.withStyle(s -> s.withItalic(false)));
			}
		}
		b.addLoreLine(Ui.text("Desgaste: " + tool.damagePerBlock() + " por bloque roto", ChatFormatting.GRAY));
		return b;
	}

	/** Etiqueta conocida -> su nombre; lista corta de bloques -> sus nombres (los traduce el cliente); si no, cuantos son. */
	private static Component blockNames(HolderSet<net.minecraft.world.level.block.Block> blocks) {
		var tag = blocks.unwrapKey();
		if (tag.isPresent()) {
			String id = tag.get().location().toString();
			return Ui.text(BLOCK_TAGS.getOrDefault(id, "#" + id), ChatFormatting.YELLOW);
		}
		List<Holder<net.minecraft.world.level.block.Block>> list = blocks.stream().toList();
		MutableComponent out = Component.empty().withStyle(s -> s.withItalic(false));
		for (int i = 0; i < Math.min(list.size(), MAX_NAMES); i++) {
			if (i > 0) {
				out.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
			}
			out.append(list.get(i).value().getName().copy().withStyle(ChatFormatting.YELLOW));
		}
		if (list.size() > MAX_NAMES) {
			out.append(Component.literal(" y " + (list.size() - MAX_NAMES) + " más").withStyle(ChatFormatting.DARK_GRAY));
		}
		return out;
	}

	// ---- arma ----
	static GuiElementBuilder weaponEntry(Weapon weapon) {
		GuiElementBuilder b = new GuiElementBuilder(Items.IRON_SWORD).hideDefaultTooltip()
			.setName(Ui.text("Al golpear", ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
		b.addLoreLine(Ui.text("Desgaste: " + weapon.itemDamagePerAttack() + " por golpe", ChatFormatting.GRAY));
		if (weapon.disableBlockingForSeconds() > 0) {
			b.addLoreLine(Ui.text("Desactiva el escudo del rival " + Text.num(weapon.disableBlockingForSeconds()) + " s", ChatFormatting.YELLOW));
		}
		return b;
	}

	// ---- equipamiento: hueco, planeador, escudo, adorno ----
	static GuiElementBuilder equipmentEntry(ItemStack stack) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		boolean glider = stack.has(DataComponents.GLIDER);
		var blocks = stack.get(DataComponents.BLOCKS_ATTACKS);
		ArmorTrim trim = stack.get(DataComponents.TRIM);
		if (equippable == null && !glider && blocks == null && trim == null) {
			return null;
		}
		GuiElementBuilder b = new GuiElementBuilder(equippable == null ? Items.SHIELD : slotIcon(equippable.slot())).hideDefaultTooltip()
			.setName(Ui.text("Equipamiento", ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
		if (equippable != null) {
			b.addLoreLine(Ui.text("Se equipa en: " + slotName(equippable.slot()), ChatFormatting.YELLOW));
		}
		if (glider) {
			b.addLoreLine(Ui.text("Permite planear: pulsa saltar en el aire.", ChatFormatting.YELLOW));
		}
		if (blocks != null) {
			b.addLoreLine(Ui.text("Bloquea ataques de frente al mantener clic derecho", ChatFormatting.YELLOW));
			b.addLoreLine(Ui.text("(tarda " + Text.num(blocks.blockDelaySeconds()) + " s en activarse).", ChatFormatting.YELLOW));
		}
		if (trim != null) {
			b.addLoreLine(Ui.blank());
			b.addLoreLine(Component.empty().append(Ui.text("Adorno: ", ChatFormatting.AQUA))
				.append(trim.pattern().value().description().copy().withStyle(ChatFormatting.YELLOW))
				.append(Ui.text(" · ", ChatFormatting.DARK_GRAY))
				.append(trim.material().value().description().copy().withStyle(ChatFormatting.YELLOW))
				.withStyle(s -> s.withItalic(false)));
			if (FabricLoader.getInstance().isModLoaded("trims_overhaul")) {
				b.addLoreLine(Ui.text("Con Trims Overhaul, el adorno puede dar", ChatFormatting.DARK_GRAY));
				b.addLoreLine(Ui.text("habilidades propias del mod (aún no", ChatFormatting.DARK_GRAY));
				b.addLoreLine(Ui.text("se explican aquí).", ChatFormatting.DARK_GRAY));
			}
		}
		return b;
	}

	private static Item slotIcon(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> Items.IRON_HELMET;
			case CHEST -> Items.IRON_CHESTPLATE;
			case LEGS -> Items.IRON_LEGGINGS;
			case FEET -> Items.IRON_BOOTS;
			default -> Items.SHIELD;
		};
	}

	static String slotName(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> "la cabeza";
			case CHEST -> "el pecho";
			case LEGS -> "las piernas";
			case FEET -> "los pies";
			case MAINHAND -> "la mano principal";
			case OFFHAND -> "la mano secundaria";
			case BODY -> "el cuerpo (de una montura)";
			case SADDLE -> "la silla de una montura";
			default -> slot.getName();
		};
	}

	/** "En la mano principal", "En el pecho"... el hueco o grupo de huecos donde el modificador esta activo. */
	static String slotLabel(EquipmentSlotGroup group) {
		return switch (group) {
			case ANY -> "en cualquier hueco";
			case MAINHAND -> "en la mano principal";
			case OFFHAND -> "en la mano secundaria";
			case HAND -> "en cualquier mano";
			case FEET -> "en los pies";
			case LEGS -> "en las piernas";
			case CHEST -> "en el pecho";
			case HEAD -> "en la cabeza";
			case ARMOR -> "con la armadura puesta";
			case BODY -> "en el cuerpo (de una montura)";
			default -> "en " + group.getSerializedName();
		};
	}

	private static String capitalize(String s) {
		return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}
}
