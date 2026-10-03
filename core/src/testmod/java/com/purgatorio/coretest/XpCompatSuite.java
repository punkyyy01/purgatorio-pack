package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.alma.AlmaRules;
import com.purgatorio.core.alma.MenuLevelBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** P0: Mending, encantar (solo lapis) y yunque (sin niveles) siguen funcionando sin XP vanilla y sin dar Alma. */
public final class XpCompatSuite implements Suite {
	@Override
	public String name() {
		return "xp-compat";
	}

	private static Holder<Enchantment> ench(Ctx ctx, net.minecraft.resources.ResourceKey<Enchantment> key) {
		return ctx.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
	}

	private static int orbsNear(Ctx ctx, Vec3 pos) {
		return ctx.level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos, pos).inflate(6)).size();
	}

	/** Los costes se recalculan cada vez que cambian los huecos; se fijan los de una mesa con 15 estanterias. */
	private static void setCosts(EnchantmentMenu table) {
		table.costs[0] = 10;
		table.costs[1] = 20;
		table.costs[2] = 30;
	}

	@Override
	public void run(Ctx ctx) {
		var alma = PurgatorioCore.alma();
		Ctx.Mock m = ctx.join("xpc-test-a");
		ServerPlayer p = m.player();
		p.snapTo(1.5, -60, 1.5, 0, 0);
		alma.set(p, AlmaRules.fromPoints(10));
		Vec3 here = new Vec3(1.5, -60, 1.5);

		// ================= MENDING =================
		// Sin objeto reparable cerca: no se crean orbes.
		ExperienceOrb.award(ctx.level, here, 50);
		ctx.eq(0, orbsNear(ctx, here), "sin objeto reparable cerca no se crean orbes");
		ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
		pick.enchant(ench(ctx, Enchantments.MENDING), 1);
		p.getInventory().add(pick);
		ExperienceOrb.award(ctx.level, here, 50);
		ctx.eq(0, orbsNear(ctx, here), "un objeto con Mending SIN danar no atrae orbes");
		p.getInventory().clearContent();
		ItemStack plain = new ItemStack(Items.DIAMOND_PICKAXE);
		plain.setDamageValue(500);
		p.getInventory().add(plain);
		ExperienceOrb.award(ctx.level, here, 50);
		ctx.eq(0, orbsNear(ctx, here), "un objeto danado SIN Mending no atrae orbes");

		// Con un objeto danado con Mending cerca: la orbe existe, repara y NO da XP ni Alma.
		p.getInventory().clearContent();
		ItemStack mend = new ItemStack(Items.DIAMOND_PICKAXE);
		mend.enchant(ench(ctx, Enchantments.MENDING), 1);
		mend.setDamageValue(500);
		p.getInventory().add(mend);
		ExperienceOrb.award(ctx.level, here, 10);
		var orbs = ctx.level.getEntitiesOfClass(ExperienceOrb.class, new AABB(here, here).inflate(6));
		ctx.check(!orbs.isEmpty(), "con un pico danado con Mending cerca SI se crea la orbe");
		int before = ItemStackHelper.damage(p);
		p.takeXpDelay = 0;
		for (ExperienceOrb orb : orbs) {
			orb.playerTouch(p);
			p.takeXpDelay = 0;
		}
		ctx.check(ItemStackHelper.damage(p) < before, "recoger la orbe repara el objeto con Mending (dano " + before + " -> " + ItemStackHelper.damage(p) + ")");
		ctx.eq(AlmaRules.fromPoints(10), alma.getCentis(p), "la orbe NO da Alma");
		ctx.eq(0, p.experienceLevel, "ni XP vanilla (nivel real)");
		ctx.eq(0, p.totalExperience, "ni XP vanilla (total)");
		orbs.forEach(ExperienceOrb::discard);

		// Jugador lejos: no cuenta aunque otro cercano no tenga nada.
		Vec3 far = new Vec3(60.5, -60, 60.5);
		ExperienceOrb.award(ctx.level, far, 50);
		ctx.eq(0, orbsNear(ctx, far), "las orbes lejos del jugador con Mending no se crean");
		p.getInventory().clearContent();

		// ================= MESA DE ENCANTAMIENTOS =================
		BlockPos tablePos = new BlockPos(10, -60, 10);
		BlockPos anvilPos = new BlockPos(12, -60, 10);
		// El servidor cierra el menu si el bloque no existe (stillValid): se colocan de verdad.
		ctx.level.setBlockAndUpdate(tablePos, net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE.defaultBlockState());
		ctx.level.setBlockAndUpdate(anvilPos, net.minecraft.world.level.block.Blocks.ANVIL.defaultBlockState());
		p.snapTo(10.5, -60, 12.5, 0, 0);
		p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new EnchantmentMenu(id, inv, ContainerLevelAccess.create(ctx.level, tablePos)), Component.literal("Encantar")));
		ctx.check(p.containerMenu instanceof EnchantmentMenu, "se abrio la mesa de encantamientos");
		EnchantmentMenu table = (EnchantmentMenu) p.containerMenu;
		ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
		table.getSlot(0).set(sword);
		table.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
		setCosts(table);                          // lo que ofreceria con 15 estanterias
		ctx.check(!table.clickMenuButton(p, 2), "CONTROL: sin el puente de nivel el servidor rechaza el encantamiento (nivel real 0)");
		p.doTick();                               // el puente da el nivel temporal al abrir el menu
		ctx.eq(MenuLevelBridge.MENU_LEVEL, p.experienceLevel, "con la mesa abierta el jugador tiene el nivel temporal");
		ClientboundSetExperiencePacket shown = ctx.lastXp(m);
		ctx.check(shown != null && shown.getExperienceLevel() == MenuLevelBridge.MENU_LEVEL, "y el cliente VE ese nivel (si no, el cliente no envia el clic)");
		table.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 1));
		setCosts(table);
		ctx.check(!table.clickMenuButton(p, 2), "el coste en LAPIS se mantiene: con 1 lapis no se puede la opcion de 3");
		table.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 3));
		setCosts(table);
		boolean enchanted = table.clickMenuButton(p, 2);
		ctx.check(enchanted, "con 3 lapis y la mesa abierta SI se encanta");
		ctx.check(table.getSlot(0).getItem().isEnchanted(), "el objeto queda encantado");
		ctx.check(table.getSlot(1).getItem().isEmpty(), "se consumieron los 3 lapis (el coste real)");
		ctx.eq(AlmaRules.fromPoints(10), alma.getCentis(p), "encantar NO toca el Alma");
		p.closeContainer();
		p.doTick();
		ctx.eq(0, p.experienceLevel, "al cerrar la mesa el nivel temporal desaparece (nivel real 0)");
		ctx.eq(0, p.totalExperience, "y no queda XP real");
		shown = ctx.lastXp(m);
		ctx.check(shown != null && shown.getExperienceLevel() == 10, "la barra vuelve a mostrar el Alma (10), real=" + (shown == null ? "sin paquete" : shown.getExperienceLevel()));
		ctx.eq(AlmaRules.fromPoints(10), alma.getCentis(p), "el Alma sigue intacta tras cerrar");

		// ================= YUNQUE =================
		p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new AnvilMenu(id, inv, ContainerLevelAccess.create(ctx.level, anvilPos)), Component.literal("Yunque")));
		ctx.check(p.containerMenu instanceof AnvilMenu, "se abrio el yunque");
		AnvilMenu anvil = (AnvilMenu) p.containerMenu;
		ItemStack axe = new ItemStack(Items.DIAMOND_SWORD);
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		book.enchant(ench(ctx, Enchantments.SHARPNESS), 3);
		anvil.getSlot(0).set(axe);
		anvil.getSlot(1).set(book);
		ctx.check(anvil.getCost() > 0, "el yunque calcula un coste (" + anvil.getCost() + ")");
		ctx.check(!anvil.getSlot(2).mayPickup(p), "CONTROL: sin el puente el resultado no se puede coger (nivel real 0)");
		p.doTick();
		ctx.check(anvil.getSlot(2).mayPickup(p), "con el yunque abierto el resultado SI se puede coger (sin gastar niveles reales)");
		ItemStack result = anvil.getSlot(2).getItem().copy();
		ctx.check(result.isEnchanted(), "el resultado lleva el encantamiento combinado");
		anvil.getSlot(2).onTake(p, result);
		ctx.eq(MenuLevelBridge.MENU_LEVEL, p.experienceLevel, "tomar el resultado no cobra niveles");
		ctx.eq(AlmaRules.fromPoints(10), alma.getCentis(p), "y no cobra Alma");
		p.closeContainer();
		p.doTick();
		ctx.eq(0, p.experienceLevel, "al cerrar el yunque el nivel temporal desaparece");

		// ================= NO-MENU: el nivel real nunca se acumula =================
		for (int i = 0; i < 5; i++) {
			p.doTick();
		}
		ctx.eq(0, p.experienceLevel, "fuera de los menus el nivel real es 0 siempre");
		ctx.eq(AlmaRules.fromPoints(10), alma.getCentis(p), "y el Alma no cambia");

		// Salir con el menu abierto no deja XP guardado.
		p.snapTo(10.5, -60, 12.5, 0, 0);
		p.openMenu(new SimpleMenuProvider((id, inv, pl) -> new EnchantmentMenu(id, inv, ContainerLevelAccess.create(ctx.level, tablePos)), Component.literal("Encantar")));
		p.doTick();
		ctx.eq(MenuLevelBridge.MENU_LEVEL, p.experienceLevel, "(preparacion) nivel temporal con la mesa abierta");
		ctx.leave(m);
		Ctx.Mock again = ctx.join("xpc-test-a");
		again.player().doTick();
		ctx.eq(0, again.player().experienceLevel, "tras salir con la mesa abierta, al volver el nivel real es 0 (no se guarda XP)");
		ctx.eq(AlmaRules.fromPoints(10), alma.getCentis(again.player()), "y el Alma se conserva");
	}
}
