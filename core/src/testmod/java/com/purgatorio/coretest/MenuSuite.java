package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.forge.ForgeGui;
import com.purgatorio.core.item.UpgradeData;
import com.purgatorio.core.menu.MainMenu;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Menu minimo server-side (sgui): Alma, forja y Diario. */
public final class MenuSuite implements Suite {
	@Override
	public String name() {
		return "menu";
	}

	@Override
	public void run(Ctx ctx) {
		Ctx.Mock m = ctx.join("menu-test-a");
		ServerPlayer p = m.player();
		var alma = PurgatorioCore.alma();
		alma.set(p, AlmaRules.fromPoints(50));

		SimpleGui gui = MainMenu.open(p);
		ctx.check(p.containerMenu != p.inventoryMenu, "el menu se abre como una pantalla de contenedor (server-side)");
		ItemStack almaItem = gui.getGuiElement(11).getItemStack();
		ctx.check(almaItem.is(Items.EXPERIENCE_BOTTLE), "slot 11: botella de experiencia = Alma");
		ctx.check(almaItem.getHoverName().getString().contains("50"), "muestra el Alma actual (50): '" + almaItem.getHoverName().getString() + "'");
		ctx.check(gui.getGuiElement(13).getItemStack().is(Items.ANVIL), "slot 13: yunque = Forja (instrucciones)");
		ItemStack diary = gui.getGuiElement(15).getItemStack();
		ctx.check(diary.is(Items.WRITABLE_BOOK), "slot 15: Diario");

		// Diario vacio, luego con un descubrimiento.
		ctx.check(!diary.toString().isEmpty(), "el Diario es un item");
		CriteriaTriggers.TICK.trigger(p);
		p.snapTo(40.5, -60, 40.5, 0, 0);
		CriteriaTriggers.LOCATION.trigger(p);
		ctx.eq(AlmaRules.fromPoints(58), alma.getCentis(p), "descubrir suma 8 (50 -> 58)");
		SimpleGui gui2 = MainMenu.open(p);
		ctx.check(gui2.getGuiElement(11).getItemStack().getHoverName().getString().contains("58"), "al reabrir el menu muestra 58");
		ctx.check(MainMenu.discoveries(p).size() == 1, "el Diario lista 1 descubrimiento");

		// Abrir la forja desde el menu (clic) y mejorar con clic.
		FsHelper.holdFreshSword(p);
		p.getInventory().add(new ItemStack(Items.IRON_INGOT, 4));
		gui2.click(13, ClickType.MOUSE_LEFT, ContainerInput.PICKUP);
		ctx.check(p.containerMenu != p.inventoryMenu, "el clic en Forja abre la pantalla de forja");

		SimpleGui forge = ForgeGui.open(p, null);
		ItemStack button = forge.getGuiElement(22).getItemStack();
		ctx.check(button.is(Items.ANVIL), "boton Mejorar disponible (yunque) con requisitos cumplidos");
		ctx.check(forge.getGuiElement(11).getItemStack().getHoverName().getString().contains("10"), "el coste de Alma (10) es visible: '" + forge.getGuiElement(11).getItemStack().getHoverName().getString() + "'");
		forge.click(22, ClickType.MOUSE_LEFT, ContainerInput.PICKUP);
		ctx.eq(1, UpgradeData.get(p.getMainHandItem()), "el clic en Mejorar sube el nivel a 1");
		ctx.eq(AlmaRules.fromPoints(48), alma.getCentis(p), "y gasta 10 de Alma (58 -> 48)");

		// Sin item mejorable: aviso, sin boton.
		p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		SimpleGui empty = ForgeGui.open(p, null);
		ctx.check(empty.getGuiElement(13).getItemStack().is(Items.BARRIER), "sin objeto mejorable la forja explica que no hay nada que mejorar");
		ctx.check(!empty.getGuiElement(22).getItemStack().is(Items.ANVIL), "y no ofrece boton de mejora");
		ctx.check(gui.getGuiElement(0).getItemStack().is(Items.STAINED_GLASS_PANE.black()), "el menu tiene marco de relleno (acabado visual)");
	}
}
