package com.purgatorio.guia.inspect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.purgatorio.guia.PurgatorioGuia;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Descripciones escritas a mano: encantamientos (resources/purgatorio_guia/encantamientos.json) y efectos
 * (efectos.json). Lo que no este aqui se muestra como "sin descripcion todavia"; tools/inventario-guia.py lista lo
 * que falta.
 */
public final class Descriptions {
	public static final String ENCHANTMENTS_RESOURCE = "/purgatorio_guia/encantamientos.json";
	public static final String EFFECTS_RESOURCE = "/purgatorio_guia/efectos.json";
	public static final String ATTRIBUTES_RESOURCE = "/purgatorio_guia/atributos.json";
	/** En efectos.json, las pociones base sin efectos van como "potion:<id de la pocion>". */
	public static final String POTION_PREFIX = "potion:";

	/**
	 * Valor que depende del nivel. {@code tipo}: "lineal" (base + paso*(nivel-1), por defecto), "der" (base >> (nivel-1),
	 * minimo 1: intervalos de ticks como la Regeneracion) o "izq" (base << (nivel-1): curas que se duplican). Despues se
	 * aplica {@code tope} (si hay) y se divide entre {@code div} (p. ej. 20 para pasar ticks a segundos).
	 */
	public record Value(double base, double paso, String tipo, double div, Double tope) {
		double at(int level) {
			int shift = Math.min(Math.max(level - 1, 0), 30);
			double v = switch (tipo) {
				case "der" -> Math.max(1L, (long) base >> shift);
				case "izq" -> (long) base << shift;
				default -> base + paso * (level - 1);
			};
			if (tope != null) {
				v = Math.min(v, tope);
			}
			return v / div;
		}
	}

	/**
	 * {@code efecto}+{@code valores} o bien {@code niveles}; los dos son opcionales. {@code atributos} false oculta las
	 * lineas automaticas de atributos de un efecto (cuando un atributo interno no dice nada util al jugador).
	 */
	public record Entry(String desc, String efecto, Map<String, Value> valores, List<String> niveles, boolean atributos) {
		/** Linea con el efecto en ese nivel, o null si la entrada no lo describe. */
		public String effectAt(int level) {
			if (niveles != null && !niveles.isEmpty()) {
				return level >= 1 && level <= niveles.size() ? niveles.get(level - 1) : null;
			}
			if (efecto == null) {
				return null;
			}
			String out = efecto;
			for (Map.Entry<String, Value> v : valores.entrySet()) {
				out = out.replace("{" + v.getKey() + "}", Text.num(v.getValue().at(level)));
			}
			return out;
		}
	}

	private static volatile Map<String, Entry> enchantments = Map.of();
	private static volatile Map<String, Entry> effects = Map.of();
	private static volatile Map<String, Entry> attributes = Map.of();

	private Descriptions() {
	}

	public static void load() {
		enchantments = read(ENCHANTMENTS_RESOURCE);
		effects = read(EFFECTS_RESOURCE);
		attributes = read(ATTRIBUTES_RESOURCE);
		PurgatorioGuia.LOGGER.info("Guia: {} encantamientos, {} efectos y {} atributos descritos",
			enchantments.size(), effects.size(), attributes.size());
	}

	private static Map<String, Entry> read(String resource) {
		try (InputStream in = Descriptions.class.getResourceAsStream(resource)) {
			if (in == null) {
				PurgatorioGuia.LOGGER.warn("No se encontro {}: la guia no tendra esas descripciones", resource);
				return Map.of();
			}
			return parse(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject());
		} catch (IOException | RuntimeException e) {
			PurgatorioGuia.LOGGER.error("No se pudo leer {}", resource, e);
			return Map.of();
		}
	}

	/** Separado de {@link #load()} para poder probarlo con JSON propio. Las claves que empiezan por "_" son notas. */
	public static Map<String, Entry> parse(JsonObject root) {
		Map<String, Entry> out = new HashMap<>();
		for (Map.Entry<String, JsonElement> e : root.entrySet()) {
			if (e.getKey().startsWith("_")) {
				continue;
			}
			JsonObject o = e.getValue().getAsJsonObject();
			Map<String, Value> values = new HashMap<>();
			if (o.has("valores")) {
				for (Map.Entry<String, JsonElement> v : o.getAsJsonObject("valores").entrySet()) {
					JsonObject vo = v.getValue().getAsJsonObject();
					values.put(v.getKey(), new Value(
						vo.get("base").getAsDouble(),
						vo.has("paso") ? vo.get("paso").getAsDouble() : 0.0,
						vo.has("tipo") ? vo.get("tipo").getAsString() : "lineal",
						vo.has("div") ? vo.get("div").getAsDouble() : 1.0,
						vo.has("tope") ? vo.get("tope").getAsDouble() : null));
				}
			}
			List<String> levels = null;
			if (o.has("niveles")) {
				levels = new ArrayList<>();
				for (JsonElement l : o.getAsJsonArray("niveles")) {
					levels.add(l.getAsString());
				}
			}
			out.put(e.getKey(), new Entry(o.get("desc").getAsString(),
				o.has("efecto") ? o.get("efecto").getAsString() : null, values, levels,
				!o.has("atributos") || o.get("atributos").getAsBoolean()));
		}
		return out;
	}

	/** Descripcion de un encantamiento por id, o null. */
	public static Entry get(String id) {
		return enchantments.get(id);
	}

	/** Descripcion de un efecto (o, con {@link #POTION_PREFIX}, de una pocion base) por id, o null. */
	public static Entry effect(String id) {
		return effects.get(id);
	}

	/** Descripcion de un atributo (minecraft:armor...) por id, o null. */
	public static Entry attribute(String id) {
		return attributes.get(id);
	}

	public static java.util.Set<String> attributeIds() {
		return attributes.keySet();
	}

	public static int size() {
		return enchantments.size();
	}

	/** Ids descritos (para validarlos contra el registro: un id mal escrito nunca se mostraria). */
	public static java.util.Set<String> ids() {
		return enchantments.keySet();
	}

	public static java.util.Set<String> effectIds() {
		return effects.keySet();
	}
}
