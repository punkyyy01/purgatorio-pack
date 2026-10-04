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
 * Descripciones escritas a mano de cada encantamiento (resources/purgatorio_guia/encantamientos.json). Lo que no
 * este aqui se muestra como "sin descripcion todavia"; tools/inventario-guia.py lista lo que falta.
 */
public final class Descriptions {
	public static final String RESOURCE = "/purgatorio_guia/encantamientos.json";

	/** valor(nivel) = base + paso * (nivel - 1). */
	public record Value(double base, double paso) {
		double at(int level) {
			return base + paso * (level - 1);
		}
	}

	/** {@code efecto}+{@code valores} o bien {@code niveles}; los dos son opcionales. */
	public record Entry(String desc, String efecto, Map<String, Value> valores, List<String> niveles) {
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

	private static volatile Map<String, Entry> entries = Map.of();

	private Descriptions() {
	}

	public static void load() {
		try (InputStream in = Descriptions.class.getResourceAsStream(RESOURCE)) {
			if (in == null) {
				PurgatorioGuia.LOGGER.warn("No se encontro {}: la guia no tendra descripciones", RESOURCE);
				return;
			}
			entries = parse(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject());
			PurgatorioGuia.LOGGER.info("Guia: {} encantamientos descritos", entries.size());
		} catch (IOException | RuntimeException e) {
			PurgatorioGuia.LOGGER.error("No se pudo leer {}", RESOURCE, e);
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
					values.put(v.getKey(), new Value(vo.get("base").getAsDouble(), vo.get("paso").getAsDouble()));
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
				o.has("efecto") ? o.get("efecto").getAsString() : null, values, levels));
		}
		return out;
	}

	public static Entry get(String id) {
		return entries.get(id);
	}

	public static int size() {
		return entries.size();
	}

	/** Ids descritos (para validarlos contra el registro: un id mal escrito nunca se mostraria). */
	public static java.util.Set<String> ids() {
		return entries.keySet();
	}
}
