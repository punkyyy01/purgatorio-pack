package com.purgatorio.coretest;

import com.purgatorio.guia.book.GuideBook;
import com.purgatorio.guia.gui.InspectorGui;
import com.purgatorio.guia.inspect.Descriptions;
import com.purgatorio.guia.inspect.Text;
import eu.pb4.sgui.api.ClickType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
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
		book(ctx);
		bookClicks(ctx);
		bookLeaks(ctx);
		descriptions(ctx);
		inspector(ctx);
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
		ctx.check(all.contains("Sin descripción todavía."), "lo que no esta descrito lo dice (Irrompibilidad)");
		ctx.check(all.contains("minecraft:unbreaking"), "e incluye el id para poder reportarlo");
		ctx.eq(2, countEntries(gui), "una entrada por encantamiento (2)");

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
		ctx.check(gui.getGuiElement(49).getItemStack().is(Items.OAK_SIGN), "explica que son los que admite");
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
			var e = gui.getGuiElement(i);
			if (e != null && e.getItemStack().is(Items.ENCHANTED_BOOK)) {
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
