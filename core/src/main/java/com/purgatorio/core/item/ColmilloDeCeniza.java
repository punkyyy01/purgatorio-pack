package com.purgatorio.core.item;

import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import com.purgatorio.core.menu.Ui;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Colmillo de Ceniza: espada de prueba que demuestra equipo con identidad propia.
 *
 * <p>Rasgo <b>Brasa</b> (verbo con costo): cada tercer golpe seguido al mismo enemigo lo prende, pero
 * cuesta un punto de hambre. Cada mejora de forja suma dano y alarga el fuego.
 */
public final class ColmilloDeCeniza extends SimplePolymerItem implements Upgradeable {
	/** Dano de ataque base (igual que una espada de hierro: 3 + 2 de material). */
	public static final double BASE_DAMAGE = 5.0;
	/** Dano extra por nivel de mejora. */
	public static final double DAMAGE_PER_LEVEL = 1.5;
	public static final double ATTACK_SPEED = -2.4;
	public static final int MAX_LEVEL = 3;

	public ColmilloDeCeniza(Item.Properties properties) {
		super(properties, Items.IRON_SWORD, true);
	}

	public static double damageForLevel(int level) {
		return BASE_DAMAGE + DAMAGE_PER_LEVEL * Math.max(0, Math.min(level, MAX_LEVEL));
	}

	/** Segundos de fuego que aplica el rasgo Brasa en un nivel dado. */
	public static int burnSeconds(int level) {
		return 3 + Math.max(0, level);
	}

	@Override
	public void applyLevel(ItemStack stack, int level) {
		stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder()
			.add(Attributes.ATTACK_DAMAGE,
				new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damageForLevel(level), AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND)
			.add(Attributes.ATTACK_SPEED,
				new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, ATTACK_SPEED, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND)
			.build());
	}

	@Override
	public List<Component> upgradePreview(int from, int to) {
		return List.of(
			Component.literal("Da\u00f1o de ataque: " + Ui.num(damageForLevel(from) + 1.0) + " \u2192 " + Ui.num(damageForLevel(to) + 1.0)).withStyle(ChatFormatting.GREEN),
			Component.literal("Fuego de Brasa: " + burnSeconds(from) + " s \u2192 " + burnSeconds(to) + " s").withStyle(ChatFormatting.YELLOW)
		);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
		int level = UpgradeData.get(stack);
		out.accept(Component.literal("Forjado con las brasas de una ruina olvidada.").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		out.accept(Ui.blank());
		out.accept(Component.literal("Rasgo \u2014 Brasa").withStyle(ChatFormatting.GOLD));
		out.accept(Component.literal("Cada tercer golpe seguido al mismo enemigo").withStyle(ChatFormatting.GRAY));
		out.accept(Component.literal("lo prende durante " + burnSeconds(level) + " s.").withStyle(ChatFormatting.GRAY));
		out.accept(Component.literal("Costo: pierdes 1 punto de hambre.").withStyle(ChatFormatting.RED));
		out.accept(Ui.blank());
		out.accept(Component.literal("Mejora ").withStyle(ChatFormatting.AQUA)
			.append(Component.literal("\u25b0".repeat(level) + "\u25b1".repeat(MAX_LEVEL - level)).withStyle(ChatFormatting.AQUA))
			.append(Component.literal(" " + level + "/" + MAX_LEVEL).withStyle(ChatFormatting.DARK_AQUA)));
		out.accept(Component.literal("Se mejora en la forja con Alma y materiales.").withStyle(ChatFormatting.DARK_GRAY));
	}
}
