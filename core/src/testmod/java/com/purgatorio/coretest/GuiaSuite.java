package com.purgatorio.coretest;

import com.purgatorio.guia.book.GuideBook;
import com.purgatorio.guia.gui.InspectorGui;
import com.purgatorio.guia.inspect.Descriptions;
import com.purgatorio.guia.inspect.EnchantmentEntries;
import com.purgatorio.guia.inspect.Text;
import eu.pb4.sgui.api.ClickType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;

/** Libro-guia (no se puede perder ni sacar) e inspector de objetos (solo lectura, con descripciones). */
public final class GuiaSuite implements Suite {
	private static final int INSPECTOR_FIRST_ENTRY = 9;
	private static final int INSPECTOR_LAST_ENTRY = 44;

	@Override
	public String name() {
		return "guia";
	}

	@Override
	public void run(Ctx ctx) {
		dumpRegistry(ctx);
		observeBehavior(ctx);
		book(ctx);
		bookClicks(ctx);
		bookLeaks(ctx);
		descriptions(ctx);
		inspector(ctx);
		effectDescriptions(ctx);
		effectFormulas(ctx);
		inspectorEffects(ctx);
		attributeDescriptions(ctx);
		inspectorEquipment(ctx);
		specialItems(ctx);
		consumeEffects(ctx);
	}

	/**
	 * Vuelca los efectos y pociones REALES del servidor a purgatorio-guia-registro.json (en el directorio del servidor de
	 * pruebas). Con FULL_PACK=1 incluye los de todos los mods; tools/inventario-guia.py lo usa para saber que describir.
	 */
	private void dumpRegistry(Ctx ctx) {
		ServerPlayer probe = ctx.join("guia-sonda").player();
		com.google.gson.JsonObject root = new com.google.gson.JsonObject();
		com.google.gson.JsonObject effects = new com.google.gson.JsonObject();
		net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.listElements().forEach(h -> {
			var e = h.value();
			com.google.gson.JsonObject o = new com.google.gson.JsonObject();
			o.addProperty("categoria", e.getCategory().name());
			o.addProperty("instantaneo", e.isInstantaneous());
			o.addProperty("clave", e.getDescriptionId());
			com.google.gson.JsonArray mods = new com.google.gson.JsonArray();
			e.createModifiers(0, (attr, mod) -> {
				com.google.gson.JsonObject m = new com.google.gson.JsonObject();
				m.addProperty("atributo", attr.unwrapKey().map(k -> k.identifier().toString()).orElse("?"));
				m.addProperty("operacion", mod.operation().name());
				m.addProperty("cantidad_nivel1", mod.amount());
				mods.add(m);
			});
			o.add("modificadores", mods);
			effects.add(h.key().identifier().toString(), o);
		});
		root.add("efectos", effects);
		com.google.gson.JsonObject potions = new com.google.gson.JsonObject();
		net.minecraft.core.registries.BuiltInRegistries.POTION.listElements().forEach(h -> {
			com.google.gson.JsonArray list = new com.google.gson.JsonArray();
			for (var inst : h.value().getEffects()) {
				list.add(inst.getEffect().unwrapKey().map(k -> k.identifier().toString()).orElse("?") + " nivel " + (inst.getAmplifier() + 1) + " " + inst.getDuration() + "t");
			}
			potions.add(h.key().identifier().toString(), list);
		});
		root.add("pociones", potions);

		// Atributos y objetos que los usan (los modificadores de los objetos vienen de sus componentes por defecto).
		com.google.gson.JsonObject attrs = new com.google.gson.JsonObject();
		net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.listElements().forEach(h -> {
			com.google.gson.JsonObject o = new com.google.gson.JsonObject();
			o.addProperty("clave", h.value().getDescriptionId());
			o.addProperty("por_defecto", h.value().getDefaultValue());
			attrs.add(h.key().identifier().toString(), o);
		});
		root.add("atributos", attrs);
		com.google.gson.JsonObject items = new com.google.gson.JsonObject();
		com.google.gson.JsonObject counts = new com.google.gson.JsonObject();
		java.util.Map<String, Integer> tally = new java.util.TreeMap<>();
		net.minecraft.core.registries.BuiltInRegistries.ITEM.listElements().forEach(h -> {
			var comps = h.value().components();
			String id = h.key().identifier().toString();
			for (var t : java.util.List.of(
				net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, net.minecraft.core.component.DataComponents.TOOL,
				net.minecraft.core.component.DataComponents.WEAPON, net.minecraft.core.component.DataComponents.EQUIPPABLE,
				net.minecraft.core.component.DataComponents.MAX_DAMAGE, net.minecraft.core.component.DataComponents.ENCHANTABLE,
				net.minecraft.core.component.DataComponents.REPAIRABLE, net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS,
				net.minecraft.core.component.DataComponents.GLIDER, net.minecraft.core.component.DataComponents.PIERCING_WEAPON,
				net.minecraft.core.component.DataComponents.KINETIC_WEAPON, net.minecraft.core.component.DataComponents.DAMAGE_RESISTANT,
				net.minecraft.core.component.DataComponents.UNBREAKABLE)) {
				if (comps.has(t)) {
					tally.merge(net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(t) + (id.startsWith("minecraft:") ? "" : " [mods]"), 1, Integer::sum);
				}
			}
			var mods = comps.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
			if (mods != null && !mods.modifiers().isEmpty()) {
				com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
				for (var e : mods.modifiers()) {
					arr.add(e.attribute().unwrapKey().map(k -> k.identifier().toString()).orElse("?") + " " + e.modifier().operation().name()
						+ " " + e.modifier().amount() + " @" + e.slot().getSerializedName());
				}
				items.add(id, arr);
			}
		});
		tally.forEach(counts::addProperty);
		root.add("objetos_con_atributos", items);

		// Todos los objetos NO vanilla, con una pista de si el inspector ya los explica (entradas de efectos/equipo) y de
		// los componentes que traen: sirve para decidir cuales necesitan una descripcion a mano.
		com.google.gson.JsonObject specials = new com.google.gson.JsonObject();
		net.minecraft.core.registries.BuiltInRegistries.ITEM.listElements().forEach(h -> {
			String id = h.key().identifier().toString();
			if (id.startsWith("minecraft:")) {
				return;
			}
			ItemStack st = new ItemStack(h);
			com.google.gson.JsonObject o = new com.google.gson.JsonObject();
			o.addProperty("nombre", st.getHoverName().getString());
			o.addProperty("explicado_por_inspector",
				!com.purgatorio.guia.inspect.EffectEntries.of(probe, st).isEmpty()
					|| !com.purgatorio.guia.inspect.EquipmentEntries.of(probe, st).isEmpty());
			com.google.gson.JsonArray comps = new com.google.gson.JsonArray();
			h.value().components().keySet().forEach(t -> comps.add(String.valueOf(net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(t))));
			o.add("componentes", comps);
			o.addProperty("clase", h.value().getClass().getName());
			specials.add(id, o);
		});
		root.add("objetos_de_mods", specials);

		// Tipos de efecto de consumo que NO son "aplicar efectos": como se codifican (campos reales) y que objetos los usan.
		com.google.gson.JsonObject consume = new com.google.gson.JsonObject();
		net.minecraft.core.registries.BuiltInRegistries.ITEM.listElements().forEach(h -> {
			var c = h.value().components().get(net.minecraft.core.component.DataComponents.CONSUMABLE);
			if (c == null) {
				return;
			}
			for (var e : c.onConsumeEffects()) {
				var json = net.minecraft.world.item.consume_effects.ConsumeEffect.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, e);
				String text = json.result().map(Object::toString).orElse("?");
				com.google.gson.JsonObject entry = consume.has(text) ? consume.getAsJsonObject(text) : new com.google.gson.JsonObject();
				com.google.gson.JsonArray users = entry.has("objetos") ? entry.getAsJsonArray("objetos") : new com.google.gson.JsonArray();
				users.add(h.key().identifier().toString());
				entry.add("objetos", users);
				consume.add(text, entry);
			}
		});
		root.add("efectos_de_consumo", consume);
		root.add("componentes_en_objetos", counts);
		try {
			java.nio.file.Files.writeString(java.nio.file.Path.of("purgatorio-guia-registro.json"),
				new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root), java.nio.charset.StandardCharsets.UTF_8);
		} catch (java.io.IOException e) {
			ctx.check(false, "no se pudo escribir el volcado del registro: " + e);
		}
	}

	/**
	 * Observa que hacen de verdad algunos objetos de mods al usarlos (purgatorio-guia-comportamiento.txt en el servidor de
	 * pruebas): las descripciones de objetos.json se escriben con lo observado aqui, no de memoria. Solo con el mod cargado.
	 */
	private void observeBehavior(Ctx ctx) {
		StringBuilder out = new StringBuilder();
		ServerPlayer p = ctx.join("guia-comportamiento").player();
		for (String id : java.util.List.of("illagerexp:illusionary_dust", "sswaystones:portable_waystone", "serverbackpacks:small",
			"serverbackpacks:ender", "serverbackpacks:global", "serverbackpacks:lava_backpack", "universal_graves:grave_compass",
			"serverbackpacks:crafting_upgrade", "illagerexp:horn_of_sight")) {
			var holder = BuiltInRegistries.ITEM.get(Identifier.parse(id));
			if (holder.isEmpty()) {
				continue;
			}
			p.removeAllEffects();
			p.closeContainer();
			p.getEnderChestInventory().setItem(0, new ItemStack(Items.DIAMOND));
			ItemStack stack = new ItemStack(holder.get(), 3);
			p.setItemInHand(InteractionHand.MAIN_HAND, stack);
			out.append("=== ").append(id).append('\n');
			try {
				InteractionResult r = stack.use(ctx.level, p, InteractionHand.MAIN_HAND);
				out.append("  use() -> ").append(r).append('\n');
			} catch (Throwable t) {
				out.append("  use() lanzo ").append(t).append('\n');
			}
			out.append("  cantidad en mano tras usar: ").append(p.getItemInHand(InteractionHand.MAIN_HAND).getCount()).append(" (antes 3)\n");
			out.append("  menu abierto: ").append(p.containerMenu != p.inventoryMenu).append(" tipo=").append(p.containerMenu.getClass().getSimpleName());
			if (p.containerMenu != p.inventoryMenu) {
				out.append(" huecos=").append(p.containerMenu.slots.size() - 36)
					.append(" primer hueco=").append(p.containerMenu.getSlot(0).getItem().getItem());
			}
			out.append('\n');
			p.getActiveEffects().forEach(e -> out.append("  efecto: ").append(e.getEffect().unwrapKey().get().identifier())
				.append(" nivel ").append(e.getAmplifier() + 1).append(" ").append(e.getDuration()).append("t\n"));
			out.append("  componentes por defecto: ").append(holder.get().value().components().keySet().stream()
				.map(t -> String.valueOf(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(t))).filter(n -> !n.contains("animation") && !n.contains("use_effects")).toList()).append('\n');
			p.closeContainer();
		}
		// Modulos de mochila: que cambia al usarlos sueltos varias veces (modo del filtro...).
		for (String id : java.util.List.of("void_upgrade", "magnet_upgrade", "jukebox_upgrade", "crafting_upgrade", "stonecutter_upgrade")) {
			var holder = BuiltInRegistries.ITEM.get(Identifier.parse("serverbackpacks:" + id));
			if (holder.isEmpty()) {
				continue;
			}
			ItemStack stack = new ItemStack(holder.get());
			p.setItemInHand(InteractionHand.MAIN_HAND, stack);
			out.append("=== modulo ").append(id).append('\n');
			for (int i = 0; i < 5; i++) {
				InteractionResult r = stack.use(ctx.level, p, InteractionHand.MAIN_HAND);
				ItemStack now = p.getItemInHand(InteractionHand.MAIN_HAND);
				out.append("  uso ").append(i + 1).append(": ").append(r.getClass().getSimpleName()).append(" | datos=")
					.append(now.getComponentsPatch()).append(" | lore=")
					.append(now.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().stream().map(Component::getString).toList()).append('\n');
				stack = now;
			}
		}
		try {
			java.nio.file.Files.writeString(java.nio.file.Path.of("purgatorio-guia-comportamiento.txt"), out.toString(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (java.io.IOException e) {
			ctx.check(false, "no se pudo escribir el comportamiento: " + e);
		}
	}

	// ---- libro: darlo, mantenerlo, abrirlo ----
	private void book(Ctx ctx) {
		Ctx.Mock m = ctx.join("guia-libro");
		ServerPlayer p = m.player();
		ctx.eq(1, GuideBook.count(p), "al entrar el jugador recibe exactamente un libro-guia");
		ctx.check(GuideBook.is(p.getInventory().getItem(GuideBook.PREFERRED_SLOT)), "el libro esta en el ultimo hueco del inventario (35)");
		ctx.check(!GuideBook.is(new ItemStack(Items.KNOWLEDGE_BOOK)), "un libro de conocimiento normal NO cuenta como guia");

		p.getInventory().setItem(GuideBook.PREFERRED_SLOT, ItemStack.EMPTY);
		ctx.check(GuideBook.ensure(p) && GuideBook.is(p.getInventory().getItem(GuideBook.PREFERRED_SLOT)), "si falta se vuelve a dar en su hueco");

		p.getInventory().setItem(0, GuideBook.create());
		ctx.check(GuideBook.ensure(p), "ensure corrige un duplicado");
		ctx.eq(1, GuideBook.count(p), "tras el duplicado sigue habiendo exactamente un libro");
		ctx.check(!GuideBook.ensure(p), "con el libro correcto ensure no hace nada");

		// Hueco preferido ocupado: no se desplaza nada, el libro va al primer hueco libre empezando por el final.
		p.getInventory().setItem(GuideBook.PREFERRED_SLOT, new ItemStack(Items.DIRT));
		p.getInventory().setItem(0, ItemStack.EMPTY);
		for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
			if (GuideBook.is(p.getInventory().getItem(i))) {
				p.getInventory().setItem(i, ItemStack.EMPTY);
			}
		}
		GuideBook.ensure(p);
		ctx.check(p.getInventory().getItem(GuideBook.PREFERRED_SLOT).is(Items.DIRT), "el objeto del hueco 35 no se mueve");
		ctx.check(GuideBook.is(p.getInventory().getItem(GuideBook.PREFERRED_SLOT - 1)), "el libro va al hueco libre 34");

		// Clic derecho abre el inspector; otros objetos no.
		p.setItemInHand(InteractionHand.MAIN_HAND, GuideBook.create());
		InteractionResult result = UseItemCallback.EVENT.invoker().interact(p, ctx.level, InteractionHand.MAIN_HAND);
		ctx.eq(InteractionResult.SUCCESS, result, "usar el libro consume la accion");
		ctx.check(p.containerMenu != p.inventoryMenu, "usar el libro abre la pantalla del inspector");
		p.closeContainer();
		p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		ctx.eq(InteractionResult.PASS, UseItemCallback.EVENT.invoker().interact(p, ctx.level, InteractionHand.MAIN_HAND), "otros objetos no abren la guia");
		p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	// ---- clics de contenedor: el libro no sale del inventario ----
	private void bookClicks(Ctx ctx) {
		Ctx.Mock m = ctx.join("guia-clics");
		ServerPlayer p = m.player();
		SimpleContainer chest = new SimpleContainer(27);
		p.openMenu(new SimpleMenuProvider((id, inv, pl) -> ChestMenu.threeRows(id, inv, chest), Component.literal("cofre de prueba")));
		AbstractContainerMenu menu = p.containerMenu;
		ctx.check(menu instanceof ChestMenu, "se abre un cofre de verdad para probar los clics");
		int bookSlot = 27 + (GuideBook.PREFERRED_SLOT - 9);   // hueco 35 del inventario dentro del menu del cofre
		ctx.check(GuideBook.is(menu.getSlot(bookSlot).getItem()), "el libro esta donde se espera dentro del menu del cofre");

		packet(p, bookSlot, 0, ContainerInput.QUICK_MOVE);
		ctx.check(chest.isEmpty(), "Mayus+clic no manda el libro al cofre");
		ctx.check(GuideBook.is(p.getInventory().getItem(GuideBook.PREFERRED_SLOT)), "y el libro sigue en su hueco");

		packet(p, bookSlot, 0, ContainerInput.PICKUP);
		ctx.check(GuideBook.is(menu.getCarried()), "se puede coger el libro con el cursor (movimiento interno permitido)");
		packet(p, 0, 0, ContainerInput.PICKUP);
		ctx.check(chest.isEmpty() && GuideBook.is(menu.getCarried()), "no se puede soltar sobre un hueco del cofre");
		packet(p, AbstractContainerMenu.SLOT_CLICKED_OUTSIDE, 0, ContainerInput.PICKUP);
		ctx.check(GuideBook.is(menu.getCarried()), "ni soltar fuera de la ventana");
		packet(p, bookSlot, 0, ContainerInput.PICKUP);
		ctx.check(menu.getCarried().isEmpty() && GuideBook.is(p.getInventory().getItem(GuideBook.PREFERRED_SLOT)), "pero si se puede devolver a su hueco");

		packet(p, bookSlot, 0, ContainerInput.THROW);
		ctx.check(GuideBook.is(p.getInventory().getItem(GuideBook.PREFERRED_SLOT)), "la tecla Q sobre el libro no lo tira");
		packet(p, bookSlot, 1, ContainerInput.THROW);
		ctx.check(GuideBook.is(p.getInventory().getItem(GuideBook.PREFERRED_SLOT)), "Ctrl+Q tampoco");

		// Tecla numerica: el libro en la hotbar no se intercambia con un hueco del cofre.
		p.getInventory().setItem(GuideBook.PREFERRED_SLOT, ItemStack.EMPTY);
		p.getInventory().setItem(0, GuideBook.create());
		packet(p, 3, 0, ContainerInput.SWAP);
		ctx.check(chest.isEmpty() && GuideBook.is(p.getInventory().getItem(0)), "intercambio con la tecla de hotbar no saca el libro");

		// Objetos normales: el cofre funciona como siempre.
		p.getInventory().setItem(1, new ItemStack(Items.DIAMOND));
		packet(p, 54 + 1, 0, ContainerInput.QUICK_MOVE);
		ctx.check(chest.hasAnyOf(java.util.Set.of(Items.DIAMOND)), "los demas objetos se pueden guardar en el cofre con normalidad");
		p.closeContainer();

		// Dentro de su inventario el libro se mueve libremente.
		p.getInventory().setItem(0, ItemStack.EMPTY);
		p.getInventory().setItem(GuideBook.PREFERRED_SLOT, GuideBook.create());
		AbstractContainerMenu inv = p.inventoryMenu;
		packet(p, 35, 0, ContainerInput.PICKUP);
		ctx.check(GuideBook.is(inv.getCarried()), "en el inventario normal se puede coger el libro");
		packet(p, 20, 0, ContainerInput.PICKUP);
		ctx.check(inv.getCarried().isEmpty() && GuideBook.is(p.getInventory().getItem(20)), "y colocarlo en otro hueco del inventario");
		packet(p, 20, 0, ContainerInput.PICKUP);
		packet(p, 1, 0, ContainerInput.PICKUP);   // cuadricula de fabricacion: no es inventario
		ctx.check(GuideBook.is(inv.getCarried()) && inv.getSlot(1).getItem().isEmpty(), "pero no meterlo en la cuadricula de fabricacion");
		packet(p, 20, 0, ContainerInput.PICKUP);
		ctx.eq(1, GuideBook.count(p), "tras todas las pruebas el jugador conserva exactamente un libro");
	}

	// ---- fugas: suelo, muerte ----
	private void bookLeaks(Ctx ctx) {
		ItemEntity dropped = new ItemEntity(ctx.level, 0.5, 80, 0.5, GuideBook.create());
		ctx.level.addFreshEntity(dropped);
		ctx.check(dropped.isRemoved(), "un libro-guia convertido en objeto suelto desaparece");
		ItemEntity other = new ItemEntity(ctx.level, 0.5, 80, 0.5, new ItemStack(Items.DIAMOND));
		ctx.level.addFreshEntity(other);
		ctx.check(!other.isRemoved(), "los demas objetos sueltos no se tocan");
		other.discard();

		Ctx.Mock m = ctx.join("guia-muerte");
		ServerPlayer p = m.player();
		p.getInventory().setItem(5, new ItemStack(Items.GOLD_INGOT, 3));
		ServerPlayer reborn = ctx.die(m);
		if (ctx.lastDeathPrevented) {
			return;   // otro mod evito la muerte: nada que comprobar aqui
		}
		AABB around = new AABB(-8, 60, -8, 8, 120, 8);
		boolean bookOnGround = ctx.level.getEntitiesOfClass(ItemEntity.class, around, e -> GuideBook.is(e.getItem())).size() > 0;
		ctx.check(!bookOnGround, "al morir el libro no cae al suelo");
		ctx.eq(1, GuideBook.count(reborn), "al reaparecer el jugador tiene su libro");
	}

	// ---- descripciones ----
	private void descriptions(Ctx ctx) {
		ctx.check(Descriptions.size() >= 4, "se cargaron las descripciones de ejemplo");
		var sharp = Descriptions.get("minecraft:sharpness");
		ctx.eq("Daño extra: +1 (2 puntos = 1 corazón)", sharp.effectAt(1), "Filo I: +1");
		ctx.eq("Daño extra: +3 (2 puntos = 1 corazón)", sharp.effectAt(5), "Filo V: +3");
		ctx.eq("Reduce el daño recibido un 16 %", Descriptions.get("minecraft:protection").effectAt(4), "Proteccion IV: 16 %");
		ctx.eq("Velocidad de minado: +10", Descriptions.get("minecraft:efficiency").effectAt(3), "Eficiencia III: +10 (nivel^2 + 1)");
		ctx.check(Descriptions.get("minecraft:efficiency").effectAt(6) == null, "un nivel fuera de la lista no inventa texto");
		ctx.check(Descriptions.get("minecraft:mending").effectAt(1) == null, "Reparacion no tiene linea de efecto por nivel");
		ctx.check(Descriptions.get("minecraft:no_existe") == null, "un id sin descripcion devuelve null");

		// Cada id descrito existe de verdad y las listas por nivel tienen un texto por nivel.
		var registry = ctx.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		for (String id : Descriptions.ids()) {
			var holder = registry.get(ResourceKey.create(Registries.ENCHANTMENT, Identifier.parse(id)));
			ctx.check(holder.isPresent(), "el id descrito existe en el registro: " + id);
			if (holder.isPresent()) {
				Enchantment e = holder.get().value();
				var entry = Descriptions.get(id);
				ctx.check(entry.niveles() == null || entry.niveles().size() == e.getMaxLevel(),
					id + ": 'niveles' tiene un texto por nivel (" + e.getMaxLevel() + ")");
				ctx.check(!entry.desc().isBlank(), id + ": tiene descripcion");
			}
		}
		ctx.eq("1", Text.num(1.0), "num: entero sin decimales");
		ctx.eq("2,5", Text.num(2.5), "num: coma decimal");
		ctx.eq("0,3", Text.num(0.1 + 0.2), "num: redondeo");
		ctx.check(Text.wrap("uno dos tres cuatro cinco seis siete ocho nueve diez", 12).stream().allMatch(l -> l.length() <= 12), "wrap respeta el ancho");
		ctx.eq("uno dos", Text.wrap("uno dos", 40).get(0), "wrap no parte lo que cabe");
	}

	// ---- inspector ----
	private void inspector(Ctx ctx) {
		Ctx.Mock m = ctx.join("guia-inspector");
		ServerPlayer p = m.player();
		var registry = ctx.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		Holder<Enchantment> sharp = registry.getOrThrow(Enchantments.SHARPNESS);
		Holder<Enchantment> unbreaking = registry.getOrThrow(Enchantments.UNBREAKING);

		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		sword.enchant(sharp, 3);
		sword.enchant(unbreaking, 2);
		p.getInventory().setItem(9, sword);
		p.getInventory().setItem(0, new ItemStack(Items.APPLE, 5));
		p.getInventory().setItem(10, new ItemStack(Items.DIAMOND_PICKAXE));

		InspectorGui insp = InspectorGui.open(p);
		var gui = insp.gui();
		ctx.check(p.containerMenu != p.inventoryMenu, "el inspector abre un cofre doble server-side");
		ctx.check(gui.getGuiElement(4).getItemStack().is(Items.HOPPER), "sin objeto elegido pide elegir uno");
		ctx.check(gui.getGuiElement(54).getItemStack().is(Items.DIAMOND_SWORD), "el inventario se refleja debajo (hueco 9 -> 54)");
		ctx.check(gui.getGuiElement(55).getItemStack().is(Items.DIAMOND_PICKAXE), "(hueco 10 -> 55)");
		ctx.check(gui.getGuiElement(54 + 27).getItemStack().is(Items.APPLE), "la hotbar va despues de las 3 filas (hueco 0 -> 81)");

		// Elegir por el camino real del paquete: el clic llega a SGUI desde la red.
		packet(p, 54, 0, ContainerInput.PICKUP);
		ctx.check(insp.inspected().is(Items.DIAMOND_SWORD), "un clic en el inventario elige el objeto");
		ctx.check(gui.getGuiElement(4).getItemStack().is(Items.DIAMOND_SWORD), "y lo muestra arriba");
		String all = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(all.contains("Daño extra: +2"), "Filo III describe su efecto en ese nivel (+2): " + all);
		ctx.check(all.contains("Nivel 3 de 5"), "muestra nivel actual y maximo del Filo");
		ctx.check(all.contains("Usos gratis: 66,7 % en herramientas y armas"), "Irrompibilidad II describe su efecto por nivel: " + all);
		// La ruta "sin descripcion" se prueba con un encantamiento SIN id de registro (Holder.direct), asi no depende de lo que falte describir.
		var undescribed = EnchantmentEntries.entry(Holder.direct(unbreaking.value()), 2).asStack().get(DataComponents.LORE);
		String undescribedLore = undescribed.lines().stream().map(Component::getString).collect(Collectors.joining("\n"));
		ctx.check(undescribedLore.contains("Sin descripción todavía.") && undescribedLore.contains("?"), "lo que no esta descrito lo dice y muestra su id: " + undescribedLore);
		ctx.eq(2, countOf(gui, Items.ENCHANTED_BOOK), "una entrada por encantamiento (2)");

		ItemStack real = p.getInventory().getItem(9);
		ctx.check(real.is(Items.DIAMOND_SWORD) && real.getCount() == 1 && real.getEnchantments().size() == 2, "el objeto real sigue intacto en el inventario");
		ctx.check(p.containerMenu.getCarried().isEmpty(), "el clic no deja nada en el cursor");

		// Quitar de la vista: clic en el objeto elegido.
		packet(p, 4, 0, ContainerInput.PICKUP);
		ctx.check(insp.inspected().isEmpty() && gui.getGuiElement(4).getItemStack().is(Items.HOPPER), "clic en el objeto elegido lo quita");
		ctx.eq(0, countEntries(gui), "y vacia la lista");

		// Un clic en un panel de relleno no hace nada ni mueve nada.
		packet(p, 0, 0, ContainerInput.PICKUP);
		ctx.check(insp.inspected().isEmpty() && p.containerMenu.getCarried().isEmpty(), "el relleno no hace nada");

		// Objeto sin encantamientos: lista lo que admite.
		packet(p, 55, 0, ContainerInput.PICKUP);
		String catalog = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(countEntries(gui) > 5, "un pico sin encantar lista los que admite: " + countEntries(gui));
		ctx.check(catalog.contains("Velocidad de minado") || catalog.contains("Nivel máximo: 5"), "incluye Eficiencia con su dato");
		ctx.check(catalog.contains("2 de durabilidad"), "incluye Reparacion con su descripcion");
		ctx.check(!catalog.contains("Nivel 1 de"), "en el catalogo no hay nivel actual");
		ctx.check(countOf(gui, Items.OAK_SIGN) >= 1 && catalog.contains("puede recibir"), "un separador explica que son los que admite");
		ctx.check(!catalog.contains("warft:tech") && !catalog.contains("rnt:internal"), "no lista encantamientos internos de los mods");

		// Libro encantado: los encantamientos guardados tambien se leen.
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		book.enchant(registry.getOrThrow(Enchantments.MENDING), 1);
		p.getInventory().setItem(11, book);
		InspectorGui second = InspectorGui.open(p);
		packet(p, 56, 0, ContainerInput.PICKUP);
		ctx.eq(1, countEntries(second.gui()), "un libro encantado muestra el encantamiento guardado");
		ctx.check(loreOf(second.gui(), INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY).contains("2 de durabilidad"), "con su descripcion");

		// Cerrar no altera el inventario real.
		int before = countItems(p);
		second.gui().close();
		ctx.eq(before, countItems(p), "cerrar el inspector no cambia el inventario");
		ctx.check(p.getInventory().getItem(9).is(Items.DIAMOND_SWORD) && p.getInventory().getItem(0).is(Items.APPLE), "los objetos siguen en sus huecos");
		ctx.eq(1, GuideBook.count(p), "y el libro sigue unico");
	}

	// ---- efectos: cobertura y cifras contra el codigo del juego ----
	private void effectDescriptions(Ctx ctx) {
		// Todo efecto que existe en el servidor tiene descripcion (si un mod nuevo anade uno, esto avisa).
		BuiltInRegistries.MOB_EFFECT.keySet().forEach(id ->
			ctx.check(Descriptions.effect(id.toString()) != null, "efecto sin descripcion: " + id));
		BuiltInRegistries.POTION.listElements().forEach(h -> {
			if (h.value().getEffects().isEmpty()) {
				ctx.check(Descriptions.effect(Descriptions.POTION_PREFIX + h.key().identifier()) != null,
					"pocion sin efectos y sin descripcion: " + h.key().identifier());
			}
		});
		// Todo id descrito es real (un id mal escrito nunca se mostraria). Los de mods solo si el mod esta cargado.
		for (String id : Descriptions.effectIds()) {
			boolean potion = id.startsWith(Descriptions.POTION_PREFIX);
			Identifier ident = Identifier.parse(potion ? id.substring(Descriptions.POTION_PREFIX.length()) : id);
			boolean checkable = ident.getNamespace().equals("minecraft") || net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(ident.getNamespace());
			if (checkable) {
				boolean exists = potion ? BuiltInRegistries.POTION.containsKey(ident) : BuiltInRegistries.MOB_EFFECT.containsKey(ident);
				ctx.check(exists, "el id descrito existe: " + id);
			}
			var entry = Descriptions.effect(id);
			ctx.check(!entry.desc().isBlank(), id + ": tiene descripcion");
			ctx.check(entry.niveles() == null || !entry.niveles().isEmpty(), id + ": 'niveles' no esta vacio");
		}
	}

	/** Las cifras de los textos salen del codigo del propio juego, no de la memoria. */
	private void effectFormulas(Ctx ctx) {
		// Intervalos: cuantas veces dispara el efecto en 1200 ticks segun el juego -> segundos entre disparos.
		record Interval(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, String id) {
		}
		for (Interval i : java.util.List.of(
			new Interval(MobEffects.REGENERATION, "minecraft:regeneration"),
			new Interval(MobEffects.POISON, "minecraft:poison"),
			new Interval(MobEffects.WITHER, "minecraft:wither"))) {
			for (int amp = 0; amp < 5; amp++) {
				int fires = 0;
				for (int duration = 1; duration <= 1200; duration++) {
					if (i.effect().value().shouldApplyEffectTickThisTick(duration, amp)) {
						fires++;
					}
				}
				String seconds = Text.num(1200.0 / fires / 20.0);
				String line = Descriptions.effect(i.id()).effectAt(amp + 1);
				ctx.check(line != null && line.contains(" " + seconds + " s"),
					i.id() + " nivel " + (amp + 1) + ": el juego dispara cada " + seconds + " s; el texto dice '" + line + "'");
			}
		}
		// Curas y danos instantaneos: se aplican de verdad a un jugador y se mide la vida.
		ServerPlayer p = ctx.join("guia-formulas").player();
		for (int amp = 0; amp < 3; amp++) {
			p.setHealth(1.0F);
			MobEffects.INSTANT_HEALTH.value().applyInstantaneousEffect(ctx.level, p, p, p, amp, 1.0);
			double healed = p.getHealth() - 1.0F;
			String line = Descriptions.effect("minecraft:instant_health").effectAt(amp + 1);
			ctx.check(line.contains("Cura " + Text.num(healed) + " puntos") && line.contains("(" + Text.num(healed / 2) + " corazones)"),
				"Salud instantanea nivel " + (amp + 1) + ": cura " + healed + " en el juego; el texto dice '" + line + "'");
		}
		for (int amp = 0; amp < 2; amp++) {
			// Un jugador nuevo por medida: tras un golpe hay un enfriamiento de dano que falsearia la segunda.
			ServerPlayer victim = ctx.join("guia-formulas-dano" + amp).player();
			victim.setHealth(20.0F);
			MobEffects.INSTANT_DAMAGE.value().applyInstantaneousEffect(ctx.level, victim, victim, victim, amp, 1.0);
			double dealt = 20.0F - victim.getHealth();
			String line = Descriptions.effect("minecraft:instant_damage").effectAt(amp + 1);
			ctx.check(line.contains("Hace " + Text.num(dealt) + " puntos") && line.contains("(" + Text.num(dealt / 2) + " corazones)"),
				"Dano instantaneo nivel " + (amp + 1) + ": hace " + dealt + " en el juego; el texto dice '" + line + "'");
		}
		p.setHealth(20.0F);
	}

	private void inspectorEffects(Ctx ctx) {
		ServerPlayer p = ctx.join("guia-efectos").player();
		InspectorGui insp = InspectorGui.open(p);
		var gui = insp.gui();

		insp.select(PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS));
		String lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.eq(1, countEntries(gui), "una pocion de Velocidad muestra 1 efecto");
		ctx.check(lore.contains("+20 %"), "Velocidad I: +20 % de velocidad (cifra del juego): " + lore);
		ctx.check(lore.contains("Duración: 3:00") && lore.contains("Nivel 1"), "duracion 3:00 y nivel 1");
		ctx.check(gui.getGuiElement(INSPECTOR_FIRST_ENTRY).getItemStack().is(Items.POTION), "el icono es una pocion");
		ctx.check(!gui.getGuiElement(49).getItemStack().is(Items.OAK_SIGN), "con efectos no se muestra el cartel de catalogo");

		insp.select(PotionContents.createItemStack(Items.POTION, Potions.STRONG_SWIFTNESS));
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(lore.contains("+40 %") && lore.contains("Nivel 2") && lore.contains("Duración: 1:30"), "Velocidad II: +40 %, nivel 2, 1:30: " + lore);

		insp.select(PotionContents.createItemStack(Items.POTION, Potions.HARMING));
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(lore.contains("Efecto instantáneo") && lore.contains("Hace 6 puntos de daño") && !lore.contains("Duración"), "Dano instantaneo: sin duracion: " + lore);

		insp.select(PotionContents.createItemStack(Items.POTION, Potions.TURTLE_MASTER));
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.eq(2, countEntries(gui), "Maestro tortuga mezcla 2 efectos");
		ctx.check(lore.contains("-60 %") && lore.contains("Reduce el daño recibido un 60 %"), "Lentitud IV (-60 %) y Resistencia III (60 %): " + lore);

		insp.select(PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON));
		ctx.check(loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY).contains("Arrojadiza"), "la pocion arrojadiza avisa de como dura");
		insp.select(PotionContents.createItemStack(Items.LINGERING_POTION, Potions.POISON));
		ctx.check(loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY).contains("1/4"), "la persistente avisa del 1/4");
		insp.select(PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.POISON));
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(lore.contains("1/8") && lore.contains("cada 1,25 s"), "la flecha avisa del 1/8 y el veneno dice cada 1,25 s: " + lore);

		insp.select(PotionContents.createItemStack(Items.POTION, Potions.AWKWARD));
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.eq(1, countEntries(gui), "una pocion rara es una entrada (sin efectos)");
		ctx.check(lore.contains("sin efectos") && lore.contains("base"), "explica que es la base de las demas: " + lore);
		insp.select(PotionContents.createItemStack(Items.POTION, Potions.WATER));
		ctx.check(loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY).contains("Verruga"), "el frasco con agua explica como seguir");

		ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW);
		stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(java.util.List.of(
			new SuspiciousStewEffects.Entry(MobEffects.NIGHT_VISION, 100))));
		insp.select(stew);
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(lore.contains("Duración: 0:05") && lore.contains("Ves con claridad"), "el estofado sospechoso muestra su efecto y 0:05: " + lore);

		insp.select(new ItemStack(Items.GOLDEN_APPLE));
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(countEntries(gui) >= 2 && lore.contains("cada 1,25 s"), "la manzana dorada: Regeneracion II (cada 1,25 s) y Absorcion: " + lore);

		insp.select(new ItemStack(Items.ROTTEN_FLESH));
		ctx.check(loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY).contains("Probabilidad: 80 %"), "la carne podrida avisa de su probabilidad");

		ItemStack bottle = new ItemStack(Items.OMINOUS_BOTTLE);
		bottle.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(2));
		insp.select(bottle);
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(lore.contains("Nivel 3") && lore.contains("100:00"), "la botella ominosa da Mal presagio del nivel que lleva: " + lore);

		insp.select(new ItemStack(Items.DIRT));
		// El unico elemento de la zona es el cartel "Nada que explicar" (hueco 22): ninguna entrada inventada.
		ctx.eq(1, countEntries(gui), "un objeto sin nada que explicar solo muestra el cartel");
		ctx.check(gui.getGuiElement(22).getItemStack().is(Items.PAPER), "y ese elemento es el cartel (papel)");

		// Un objeto con encantamientos Y efectos muestra ambos (no se pisan).
		var registry = ctx.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		ItemStack both = new ItemStack(Items.SUSPICIOUS_STEW);
		both.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, new SuspiciousStewEffects(java.util.List.of(
			new SuspiciousStewEffects.Entry(MobEffects.SPEED, 100))));
		both.enchant(registry.getOrThrow(Enchantments.MENDING), 1);
		insp.select(both);
		ctx.eq(2, countEntries(gui), "encantamiento y efecto conviven en la misma lista");
		ctx.eq(1, GuideBook.count(p), "el inventario sigue intacto");
	}

	// ---- atributos y equipo ----
	private void attributeDescriptions(Ctx ctx) {
		// Todo atributo del juego tiene descripcion (un objeto puede llevar cualquiera, p. ej. los de RPG Loot).
		BuiltInRegistries.ATTRIBUTE.keySet().forEach(id ->
			ctx.check(Descriptions.attribute(id.toString()) != null, "atributo sin descripcion: " + id));
		for (String id : Descriptions.attributeIds()) {
			ctx.check(BuiltInRegistries.ATTRIBUTE.containsKey(Identifier.parse(id)), "el atributo descrito existe: " + id);
			ctx.check(!Descriptions.attribute(id).desc().isBlank(), id + ": tiene descripcion");
		}
		ctx.eq("+7", Text.signed(7.0, false), "signed: entero con signo");
		ctx.eq("-3,1", Text.signed(-3.1, false), "signed: negativo con coma");
		ctx.eq("+20 %", Text.signed(0.2, true), "signed: proporcional como porcentaje");
	}

	/** Fichas de ejemplo tal como las lee el jugador (purgatorio-guia-muestras.txt en el servidor de pruebas), para revisarlas. */
	private void writeSamples(Ctx ctx, ServerPlayer p) {
		StringBuilder out = new StringBuilder();
		InspectorGui insp = InspectorGui.open(p);
		var registry = ctx.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		ItemStack enchanted = new ItemStack(Items.DIAMOND_SWORD);
		enchanted.enchant(registry.getOrThrow(Enchantments.SHARPNESS), 3);
		for (var item : java.util.List.of(enchanted, new ItemStack(Items.NETHERITE_CHESTPLATE), new ItemStack(Items.IRON_PICKAXE),
			new ItemStack(Items.IRON_AXE), PotionContents.createItemStack(Items.POTION, Potions.TURTLE_MASTER), new ItemStack(Items.GOLDEN_APPLE))) {
			insp.select(item);
			out.append("=================== ").append(item.getHoverName().getString()).append('\n');
			for (int i = INSPECTOR_FIRST_ENTRY; i <= INSPECTOR_LAST_ENTRY; i++) {
				var e = insp.gui().getGuiElement(i);
				if (e == null) {
					continue;
				}
				out.append("[").append(e.getItemStack().getHoverName().getString()).append("]\n");
				ItemLore lore = e.getItemStack().get(DataComponents.LORE);
				if (lore != null) {
					lore.lines().forEach(l -> out.append("    ").append(l.getString()).append('\n'));
				}
				if (i > INSPECTOR_FIRST_ENTRY + 11) {
					out.append("    ... (").append("recortado a 12 entradas)\n");
					break;
				}
			}
		}
		try {
			java.nio.file.Files.writeString(java.nio.file.Path.of("purgatorio-guia-muestras.txt"), out.toString(), java.nio.charset.StandardCharsets.UTF_8);
		} catch (java.io.IOException e) {
			ctx.check(false, "no se pudieron escribir las muestras: " + e);
		}
	}

	private void inspectorEquipment(Ctx ctx) {
		ServerPlayer p = ctx.join("guia-equipo").player();
		InspectorGui insp = InspectorGui.open(p);
		var gui = insp.gui();
		var all = (java.util.function.Supplier<String>) () -> loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);

		// Espada de hierro: las cifras esperadas se calculan LEYENDO EL JUEGO, no se escriben a mano.
		ItemStack sword = new ItemStack(Items.IRON_SWORD);
		double damageBase = p.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
		double speedBase = p.getAttribute(Attributes.ATTACK_SPEED).getBaseValue();
		double damageMod = modifierSum(sword, Attributes.ATTACK_DAMAGE);
		double speedMod = modifierSum(sword, Attributes.ATTACK_SPEED);
		insp.select(sword);
		String lore = all.get();
		ctx.check(lore.contains("En la mano principal: " + Text.signed(damageMod, false) + "  (total: " + Text.num(damageBase + damageMod) + ")"),
			"el daño de la espada y su total salen del juego (" + damageBase + " + " + damageMod + "): " + lore);
		ctx.check(lore.contains("(total: " + Text.num(speedBase + speedMod) + ")"), "y los golpes por segundo (" + speedBase + " + " + speedMod + ")");
		ctx.check(lore.contains("Valor normal: " + Text.num(damageBase)), "con el valor normal del jugador");
		ctx.check(lore.contains("Daño de tus golpes cuerpo a cuerpo"), "y explica que significa el atributo");
		ctx.check(lore.contains("Durabilidad: " + sword.getMaxDamage() + " de " + sword.getMaxDamage() + " (100 %)"), "durabilidad completa: " + lore);
		ctx.check(lore.contains(new ItemStack(Items.IRON_INGOT).getHoverName().getString()), "dice con que se repara (lingote de hierro)");
		ctx.check(lore.contains("Encantabilidad: " + sword.get(DataComponents.ENCHANTABLE).value()), "y su encantabilidad");
		ctx.check(lore.contains("Desgaste: " + sword.get(DataComponents.WEAPON).itemDamagePerAttack() + " por golpe"), "desgaste por golpe del arma");
		ctx.check(countOf(gui, Items.OAK_SIGN) >= 1, "y tras los datos, lo que admite como encantamiento");

		// La espada rompe la telaraña y el bambu al instante: debe decirlo, no escribir el valor interno del juego.
		ctx.check(lore.contains("Rompe al instante:") && !lore.contains("340282"), "el minado instantaneo no enseña el numero interno: " + lore);
		ctx.check(lore.contains(net.minecraft.world.level.block.Blocks.COBWEB.getName().getString()), "y nombra los bloques concretos (telaraña)");

		ItemStack worn = new ItemStack(Items.IRON_SWORD);
		worn.setDamageValue(worn.getMaxDamage() / 2);
		insp.select(worn);
		ctx.check(all.get().contains("Durabilidad: " + (worn.getMaxDamage() - worn.getDamageValue()) + " de " + worn.getMaxDamage() + " (50 %)"), "durabilidad a medias: " + all.get());

		insp.select(new ItemStack(Items.IRON_AXE));
		ctx.check(all.get().contains("Desactiva el escudo del rival"), "el hacha desactiva escudos");
		insp.select(new ItemStack(Items.IRON_PICKAXE));
		lore = all.get();
		ctx.check(lore.contains("piedra, minerales y metales") && lore.contains("Desgaste: 1 por bloque roto"), "el pico explica sus reglas de minado: " + lore);

		// Armadura: hueco, atributos y resistencia del objeto suelto (netherita).
		ItemStack chest = new ItemStack(Items.NETHERITE_CHESTPLATE);
		insp.select(chest);
		lore = all.get();
		ctx.check(lore.contains("En el pecho: " + Text.signed(modifierSum(chest, Attributes.ARMOR), false)), "la armadura del peto sale del juego: " + lore);
		ctx.check(lore.contains("En el pecho: " + Text.signed(modifierSum(chest, Attributes.KNOCKBACK_RESISTANCE), false)), "y su resistencia al empuje");
		ctx.check(lore.contains("Se equipa en: el pecho"), "dice donde se equipa");
		ctx.check(lore.contains("no se destruye por:") && lore.contains("fuego y lava"), "la netherita no se quema como objeto suelto: " + lore);
		ctx.check(lore.contains("Valor normal: 0"), "el valor normal de la armadura es 0");

		// Adorno de armadura.
		var trimMaterials = ctx.level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
		var trimPatterns = ctx.level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
		ItemStack trimmed = new ItemStack(Items.DIAMOND_CHESTPLATE);
		trimmed.set(DataComponents.TRIM, new net.minecraft.world.item.equipment.trim.ArmorTrim(
			trimMaterials.get(net.minecraft.world.item.equipment.trim.TrimMaterials.AMETHYST).orElseThrow(),
			trimPatterns.get(net.minecraft.world.item.equipment.trim.TrimPatterns.SENTRY).orElseThrow()));
		insp.select(trimmed);
		ctx.check(all.get().contains("Adorno: "), "una armadura con adorno lo muestra: " + all.get());

		insp.select(new ItemStack(Items.ELYTRA));
		ctx.check(all.get().contains("Permite planear"), "los elytros permiten planear");
		insp.select(new ItemStack(Items.SHIELD));
		ctx.check(all.get().contains("Bloquea ataques de frente"), "el escudo bloquea");

		// Atributos AÑADIDOS al objeto (como hace RPG Loot): se leen del objeto concreto, no del tipo.
		ItemStack loot = new ItemStack(Items.IRON_SWORD);
		ItemAttributeModifiers base = loot.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		loot.set(DataComponents.ATTRIBUTE_MODIFIERS, base
			.withModifierAdded(Attributes.MAX_HEALTH, new AttributeModifier(Identifier.parse("test:bonus_vida"), 4.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
			.withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(Identifier.parse("test:bonus_vel"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlotGroup.MAINHAND)
			.withModifierAdded(Attributes.FOLLOW_RANGE, new AttributeModifier(Identifier.parse("test:bonus_follow"), 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND));
		insp.select(loot);
		lore = all.get();
		ctx.check(lore.contains("En la mano principal: +4") && lore.contains("Tu vida máxima"), "un atributo añadido (vida +4) aparece con su explicacion: " + lore);
		ctx.check(lore.contains("En la mano principal: +10 %"), "uno proporcional sale como porcentaje (+10 %)");
		ctx.check(lore.contains("Valor normal: " + Text.num(Attributes.FOLLOW_RANGE.value().getDefaultValue())), "un atributo que el jugador no tiene usa su valor por defecto sin fallar");
		ctx.check(countOf(gui, Items.PAPER) >= 1 || countOf(gui, Items.GOLDEN_APPLE) >= 1, "con su propio icono");

		// Objetos sin nada de esto siguen sin inventar entradas.
		insp.select(new ItemStack(Items.DIRT));
		ctx.eq(1, countEntries(gui), "la tierra sigue mostrando solo el cartel");
		ctx.eq(1, GuideBook.count(p), "el inventario sigue intacto");
		writeSamples(ctx, p);
	}

	/** Suma de los modificadores ADD_VALUE de un atributo en un objeto (lo que el juego dice que aporta). */
	private static double modifierSum(ItemStack stack, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
		double sum = 0;
		for (var e : stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
			if (e.attribute().equals(attribute) && e.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
				sum += e.modifier().amount();
			}
		}
		return sum;
	}

	// ---- objetos especiales: el texto coincide con lo observado ----
	private static boolean present(String id) {
		return BuiltInRegistries.ITEM.containsKey(Identifier.parse(id));
	}

	private void specialItems(Ctx ctx) {
		// Todo id descrito existe; si el mod no esta cargado (no tiene ningun objeto registrado) se salta.
		for (String id : Descriptions.itemIds()) {
			Identifier ident = Identifier.parse(id);
			boolean modLoaded = BuiltInRegistries.ITEM.keySet().stream().anyMatch(k -> k.getNamespace().equals(ident.getNamespace()));
			if (modLoaded) {
				ctx.check(BuiltInRegistries.ITEM.containsKey(ident), "el objeto descrito existe: " + id);
			}
			ctx.check(!Descriptions.item(id).desc().isBlank(), id + ": tiene descripcion");
		}
		ServerPlayer p = ctx.join("guia-especiales").player();
		InspectorGui insp = InspectorGui.open(p);

		if (present("illagerexp:illusionary_dust")) {
			var item = BuiltInRegistries.ITEM.get(Identifier.parse("illagerexp:illusionary_dust")).orElseThrow().value();
			ItemStack stack = new ItemStack(item, 3);
			p.setItemInHand(InteractionHand.MAIN_HAND, stack);
			p.removeAllEffects();
			stack.use(ctx.level, p, InteractionHand.MAIN_HAND);
			int invisibility = p.getEffect(MobEffects.INVISIBILITY).getDuration();
			int speed = p.getEffect(MobEffects.SPEED).getDuration();
			String uso = String.join(" ", Descriptions.item("illagerexp:illusionary_dust").uso());
			ctx.eq(2, p.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "el polvo ilusorio se gasta de 1 en 1");
			ctx.check(uso.contains("se gasta 1") && uso.contains("Invisibilidad durante " + invisibility / 20 + " s") && uso.contains("Velocidad durante " + speed / 20 + " s"),
				"el texto del polvo ilusorio coincide con lo observado (" + invisibility + "t y " + speed + "t): " + uso);
			insp.select(new ItemStack(item));
			String lore = loreOf(insp.gui(), INSPECTOR_FIRST_ENTRY, INSPECTOR_FIRST_ENTRY);
			ctx.check(insp.gui().getGuiElement(INSPECTOR_FIRST_ENTRY).getItemStack().is(Items.BOOK) && lore.contains("Cómo se usa") && lore.contains("Cómo se consigue"),
				"el inspector muestra 'Qué es' el primero, con uso y como se consigue: " + lore);
			p.removeAllEffects();
		}
		if (present("sswaystones:portable_waystone")) {
			var item = BuiltInRegistries.ITEM.get(Identifier.parse("sswaystones:portable_waystone")).orElseThrow().value();
			ItemStack stack = new ItemStack(item, 2);
			p.setItemInHand(InteractionHand.MAIN_HAND, stack);
			stack.use(ctx.level, p, InteractionHand.MAIN_HAND);
			ctx.check(p.containerMenu != p.inventoryMenu && p.containerMenu.slots.size() - 36 == 54, "la piedra de viaje portatil abre un menu de 54 huecos");
			ctx.eq(2, p.getItemInHand(InteractionHand.MAIN_HAND).getCount(), "y no se gasta");
			ctx.check(Descriptions.item("sswaystones:portable_waystone").desc().contains("No se gasta"), "el texto lo dice");
			p.closeContainer();
		}
		// Mochilas: el tamano que dice el texto es el de la mochila abierta de verdad.
		java.util.Map<String, Integer> sizes = java.util.Map.of("serverbackpacks:small", 9, "serverbackpacks:medium", 18, "serverbackpacks:large", 27,
			"serverbackpacks:ender", 27, "serverbackpacks:global", 54, "serverbackpacks:lava_backpack", 18);
		sizes.forEach((id, slots) -> {
			if (!present(id)) {
				return;
			}
			var item = BuiltInRegistries.ITEM.get(Identifier.parse(id)).orElseThrow().value();
			ItemStack stack = new ItemStack(item);
			p.setItemInHand(InteractionHand.MAIN_HAND, stack);
			p.getEnderChestInventory().setItem(0, new ItemStack(Items.DIAMOND));
			stack.use(ctx.level, p, InteractionHand.MAIN_HAND);
			ctx.eq(slots, p.containerMenu.slots.size() - 36, id + ": huecos de la mochila abierta");
			ctx.check(Descriptions.item(id).desc().contains(slots + " huecos"), id + ": el texto dice " + slots + " huecos: " + Descriptions.item(id).desc());
			if (id.endsWith(":ender")) {
				ctx.check(p.containerMenu.getSlot(0).getItem().is(Items.DIAMOND), "la mochila de Ender muestra el cofre de Ender del jugador");
			}
			p.closeContainer();
		});
		// Un color de mochila usa la descripcion de su tamano.
		if (present("serverbackpacks:red_medium")) {
			ctx.check(Descriptions.item("serverbackpacks:red_medium") == Descriptions.item("serverbackpacks:medium"), "las variantes de color comparten descripcion");
		}
		// Modulos: tres no hacen nada sueltos; aspiracion y tocadiscos cambian su modo (observado en GuiaSuite.observeBehavior).
		for (String id : java.util.List.of("void_upgrade", "crafting_upgrade", "stonecutter_upgrade", "magnet_upgrade", "jukebox_upgrade")) {
			String full = "serverbackpacks:" + id;
			if (!present(full)) {
				continue;
			}
			ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(Identifier.parse(full)).orElseThrow().value());
			p.setItemInHand(InteractionHand.MAIN_HAND, stack);
			boolean toggles = id.equals("magnet_upgrade") || id.equals("jukebox_upgrade");
			ctx.eq(toggles ? InteractionResult.SUCCESS.getClass() : InteractionResult.PASS.getClass(),
				stack.use(ctx.level, p, InteractionHand.MAIN_HAND).getClass(), full + ": usarlo suelto " + (toggles ? "responde" : "no hace nada"));
			String uso = String.join(" ", Descriptions.item(full).uso());
			ctx.check(toggles ? uso.contains("cambia su modo") && !uso.contains("No se usa suelto") : uso.contains("No se usa suelto"),
				full + ": el texto de uso coincide con lo observado: " + uso);
		}
		p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		ctx.eq(1, GuideBook.count(p), "el inventario sigue intacto");
	}

	private void consumeEffects(Ctx ctx) {
		ServerPlayer p = ctx.join("guia-consumo").player();
		InspectorGui insp = InspectorGui.open(p);
		var gui = insp.gui();
		String lore;

		insp.select(new ItemStack(Items.MILK_BUCKET));
		lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
		ctx.check(lore.contains("Elimina todos tus efectos activos"), "la leche quita todos los efectos: " + lore);
		insp.select(new ItemStack(Items.HONEY_BOTTLE));
		ctx.check(loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY).contains("Elimina el efecto:"), "la miel quita un efecto concreto (nombrado)");
		insp.select(new ItemStack(Items.CHORUS_FRUIT));
		ctx.check(loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY).contains("Te teletransporta al azar (hasta 8 bloques)"), "la fruta de chorus teletransporta hasta 8 bloques (diametro 16)");
		insp.select(new ItemStack(Items.BREAD));
		ctx.eq(0, countOf(gui, Items.HONEY_BOTTLE), "un alimento normal no muestra 'Al consumirlo'");

		// Farmer's Delight (solo si esta cargado): se leen por JSON, sin depender de sus clases.
		for (var e : java.util.Map.of("farmersdelight:melon_juice", "Cura 2 puntos de vida (1 corazón)", "farmersdelight:melon_popsicle", "Apaga el fuego",
			"farmersdelight:hot_cocoa", "Elimina un efecto negativo al azar", "farmersdelight:milk_bottle", "Elimina un efecto activo al azar").entrySet()) {
			if (present(e.getKey())) {
				insp.select(new ItemStack(BuiltInRegistries.ITEM.get(Identifier.parse(e.getKey())).orElseThrow().value()));
				lore = loreOf(gui, INSPECTOR_FIRST_ENTRY, INSPECTOR_LAST_ENTRY);
				ctx.check(lore.contains(e.getValue()), e.getKey() + " dice '" + e.getValue() + "': " + lore);
			}
		}
	}

	// ---- utilidades ----
	/** Envia un clic de contenedor por el mismo camino que la red (SGUI y mixins incluidos). */
	private static void packet(ServerPlayer p, int slot, int button, ContainerInput input) {
		AbstractContainerMenu menu = p.containerMenu;
		p.connection.handleContainerClick(new ServerboundContainerClickPacket(
			menu.containerId, menu.getStateId(), (short) slot, (byte) button, input, new Int2ObjectOpenHashMap<>(), HashedStack.EMPTY));
	}

	private static String loreOf(eu.pb4.sgui.api.gui.SimpleGui gui, int from, int to) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i <= to; i++) {
			var e = gui.getGuiElement(i);
			if (e == null) {
				continue;
			}
			ItemLore lore = e.getItemStack().get(DataComponents.LORE);
			if (lore != null) {
				sb.append(lore.lines().stream().map(Component::getString).collect(Collectors.joining("\n"))).append('\n');
			}
		}
		return sb.toString();
	}

	private static int countEntries(eu.pb4.sgui.api.gui.SimpleGui gui) {
		int n = 0;
		for (int i = INSPECTOR_FIRST_ENTRY; i <= INSPECTOR_LAST_ENTRY; i++) {
			if (gui.getGuiElement(i) != null) {
				n++;
			}
		}
		return n;
	}

	/** Entradas de la zona cuyo icono es ese objeto. */
	private static int countOf(eu.pb4.sgui.api.gui.SimpleGui gui, net.minecraft.world.item.Item item) {
		int n = 0;
		for (int i = INSPECTOR_FIRST_ENTRY; i <= INSPECTOR_LAST_ENTRY; i++) {
			var e = gui.getGuiElement(i);
			if (e != null && e.getItemStack().is(item)) {
				n++;
			}
		}
		return n;
	}

	private static int countItems(ServerPlayer p) {
		int n = 0;
		for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
			n += p.getInventory().getItem(i).getCount();
		}
		return n;
	}
}
