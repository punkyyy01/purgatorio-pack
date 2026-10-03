package com.purgatorio.coretest;

import com.purgatorio.core.PurgatorioCore;
import com.purgatorio.core.item.ColmilloDeCeniza;
import com.purgatorio.core.item.ColmilloTrait;
import com.purgatorio.core.item.PurgatorioItems;
import com.purgatorio.core.item.UpgradeData;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import java.lang.reflect.Field;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** El Colmillo de Ceniza: equipo propio con rasgo (verbo + costo), visible para clientes vanilla via Polymer. */
public final class ItemSuite implements Suite {
	@Override
	public String name() {
		return "item";
	}

	private Zombie zombie(Ctx ctx) {
		Zombie z = EntityTypes.ZOMBIE.create(ctx.level, EntitySpawnReason.COMMAND);
		z.setPos(0.5, 80, 0.5);
		z.setNoAi(true);
		z.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500.0);
		ctx.level.addFreshEntity(z);
		z.setHealth(500.0F);
		return z;
	}

	private static float exhaustion(ServerPlayer p) throws ReflectiveOperationException {
		Field f = p.getFoodData().getClass().getDeclaredField("exhaustionLevel");
		f.setAccessible(true);
		return f.getFloat(p.getFoodData());
	}

	/** Un golpe cuerpo a cuerpo. Se reintenta si no hizo dano (con el pack completo un mod puede anularlo al azar). */
	private void hit(Ctx ctx, ServerPlayer p, Zombie z) {
		for (int attempt = 0; attempt < 6; attempt++) {
			float before = z.getHealth();
			z.damageCooldownTime = 0;
			z.hurtServer(ctx.level, ctx.level.damageSources().playerAttack(p), 5.0F);
			if (z.getHealth() < before) {
				return;
			}
		}
	}

	@Override
	public void run(Ctx ctx) throws Exception {
		Ctx.Mock m = ctx.join("item-test-a");
		ServerPlayer p = m.player();

		// ---- registrado y visible para un cliente vanilla (Polymer) ----
		ctx.check(BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("purgatorio", "colmillo_de_ceniza")).isPresent(), "el item purgatorio:colmillo_de_ceniza esta registrado");
		ItemStack sword = ForgeSuite.giveSword(p, 0);
		ItemStack client = PacketContext.supplyWithContext(p.connection, () -> PolymerItemUtils.getPolymerItemStack(sword, PacketContext.get(), ctx.server.registryAccess()));
		ctx.check(client.is(Items.IRON_SWORD), "un cliente vanilla recibe una espada de hierro (real=" + client.getItem() + ")");
		Identifier model = client.get(DataComponents.ITEM_MODEL);
		ctx.eq("purgatorio:colmillo_de_ceniza", model == null ? null : model.toString(), "con item_model propio (el modelo del resource pack)");
		// Regresion: el cliente veia la clave "item.purgatorio.colmillo_de_ceniza". El nombre legible vive en el archivo de
		// idioma del resource pack (en_us es el idioma de reserva de TODOS los clientes). Aqui se comprueba que existe.
		String key = PurgatorioItems.COLMILLO_DE_CENIZA.getDescriptionId();
		try (var in = PurgatorioCore.class.getResourceAsStream("/assets/purgatorio/lang/en_us.json")) {
			ctx.check(in != null, "existe assets/purgatorio/lang/en_us.json en el mod (Polymer lo copia al resource pack)");
			if (in != null) {
				var lang = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
				ctx.check(lang.has(key), "el idioma traduce la clave " + key);
				ctx.eq("Colmillo de Ceniza", lang.has(key) ? lang.get(key).getAsString() : null, "con el nombre legible");
			}
		}
		var clientName = client.get(DataComponents.ITEM_NAME);
		ctx.check(clientName != null && clientName.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc && tc.getKey().equals(key),
			"el cliente recibe el nombre como la clave traducible que el pack resuelve");
		var shard = ForgeSuite.shard();
		ctx.eq("Esquirla de Brasa", shard == null ? null : shard.getDefaultInstance().getHoverName().getString(), "la Esquirla (Filament) lleva su nombre literal");
		var lines = new java.util.ArrayList<net.minecraft.network.chat.Component>();
		PurgatorioItems.COLMILLO_DE_CENIZA.appendHoverText(sword, net.minecraft.world.item.Item.TooltipContext.EMPTY,
			net.minecraft.world.item.component.TooltipDisplay.DEFAULT, lines::add, net.minecraft.world.item.TooltipFlag.NORMAL);
		String tooltip = lines.stream().map(net.minecraft.network.chat.Component::getString).reduce("", (x, y) -> x + "\n" + y);
		ctx.check(tooltip.contains("Brasa") && tooltip.contains("Costo") && tooltip.contains("Mejora"), "el tooltip describe rasgo, costo y mejora");
		ctx.check(!tooltip.contains("item.purgatorio") && !tooltip.contains("tooltip."), "el tooltip no contiene claves de traduccion sin resolver");

		// ---- el rasgo Brasa: 3 golpes seguidos al mismo enemigo -> fuego + cuesta hambre ----
		Zombie z = zombie(ctx);
		float exh0 = exhaustion(p);
		hit(ctx, p, z);
		hit(ctx, p, z);
		ctx.check(!z.isOnFire(), "tras 2 golpes todavia no hay fuego");
		hit(ctx, p, z);
		ctx.check(z.isOnFire(), "el tercer golpe seguido prende al enemigo");
		ctx.check(z.getRemainingFireTicks() >= 3 * 20 - 5, "fuego de ~3 s con el arma sin mejorar (ticks=" + z.getRemainingFireTicks() + ")");
		ctx.check(exhaustion(p) - exh0 >= ColmilloTrait.HUNGER_EXHAUSTION - 0.5F, "COSTO: prender cuesta ~1 punto de hambre (agotamiento +" + (exhaustion(p) - exh0) + ")");
		z.clearFire();

		// ---- el combo se reinicia tras activarse ----
		float exh1 = exhaustion(p);
		hit(ctx, p, z);
		hit(ctx, p, z);
		ctx.check(!z.isOnFire(), "tras activarse el contador se reinicia");
		ctx.check(exhaustion(p) - exh1 < 1.0F, "los golpes sin activar no cuestan hambre extra");

		// ---- cambiar de objetivo rompe el combo ----
		Zombie za = zombie(ctx);
		Zombie zb = zombie(ctx);
		hit(ctx, p, za);
		hit(ctx, p, zb);
		hit(ctx, p, za);
		hit(ctx, p, zb);
		ctx.check(!za.isOnFire() && !zb.isOnFire(), "alternar objetivos no activa el rasgo");

		// ---- sin el arma en la mano no hay rasgo ----
		p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
		Zombie z3 = zombie(ctx);
		hit(ctx, p, z3);
		hit(ctx, p, z3);
		hit(ctx, p, z3);
		ctx.check(!z3.isOnFire(), "con otra espada en la mano no se activa Brasa");
		ForgeSuite.giveSword(p, 0);

		// ---- las flechas no cuentan (golpe directo cuerpo a cuerpo solamente) ----
		Zombie z4 = zombie(ctx);
		for (int i = 0; i < 4; i++) {
			Arrow arrow = EntityTypes.ARROW.create(ctx.level, EntitySpawnReason.COMMAND);
			arrow.setOwner(p);
			z4.damageCooldownTime = 0;
			z4.hurtServer(ctx.level, ctx.level.damageSources().arrow(arrow, p), 5.0F);
		}
		ctx.check(!z4.isOnFire(), "los proyectiles no activan el rasgo cuerpo a cuerpo");

		// ---- mejoras alargan el fuego ----
		ctx.eq(3, ColmilloDeCeniza.burnSeconds(0), "nivel 0: 3 s de fuego");
		ctx.eq(6, ColmilloDeCeniza.burnSeconds(3), "nivel 3: 6 s de fuego");
		ForgeSuite.giveSword(p, 3);
		Zombie z5 = zombie(ctx);
		hit(ctx, p, z5);
		hit(ctx, p, z5);
		hit(ctx, p, z5);
		ctx.check(z5.getRemainingFireTicks() >= 6 * 20 - 5, "con la mejora 3 el fuego dura ~6 s (ticks=" + z5.getRemainingFireTicks() + ")");
		ctx.near(5.0 + 1.5 * 3, ForgeSuite.weaponDamage(p.getMainHandItem()), 1e-9, "y el dano base es 9,5");

		// ---- un jugador no afecta el contador de otro ----
		Ctx.Mock m2 = ctx.join("item-test-b");
		ForgeSuite.giveSword(m2.player(), 0);
		ForgeSuite.giveSword(p, 0);
		Zombie z6 = zombie(ctx);
		hit(ctx, p, z6);
		hit(ctx, m2.player(), z6);
		hit(ctx, p, z6);
		ctx.check(!z6.isOnFire(), "los golpes de otro jugador no suman a mi combo");
		hit(ctx, p, z6);
		ctx.check(z6.isOnFire(), "mis propios golpes seguidos (tras el de B) si activan mi rasgo");
		ctx.eq(0, UpgradeData.get(PurgatorioItems.COLMILLO_DE_CENIZA.getDefaultInstance()), "un Colmillo nuevo es nivel 0");
		ctx.check(PurgatorioCore.alma() != null, "servicio de Alma disponible");
	}
}
