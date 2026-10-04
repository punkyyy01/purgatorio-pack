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
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;

/** Temas de la Guia del servidor (resources/purgatorio_guia/guia_servidor.json). */
public final class Topics {
	public static final String RESOURCE = "/purgatorio_guia/guia_servidor.json";

	public record Paragraph(String titulo, String texto) {
	}

	public record Topic(String id, String titulo, String icono, String requiere, String resumen, List<Paragraph> parrafos, List<String> objetos) {
		/** El tema solo se muestra si el mod del que habla esta cargado (algun objeto registrado con ese espacio de nombres). */
		public boolean available() {
			return requiere == null || requiere.isEmpty()
				|| BuiltInRegistries.ITEM.keySet().stream().anyMatch(k -> k.getNamespace().equals(requiere));
		}
	}

	private static volatile List<Topic> topics = List.of();

	private Topics() {
	}

	public static void load() {
		try (InputStream in = Topics.class.getResourceAsStream(RESOURCE)) {
			if (in == null) {
				PurgatorioGuia.LOGGER.warn("No se encontro {}: la guia no tendra la Guia del servidor", RESOURCE);
				return;
			}
			topics = parse(JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject());
		} catch (IOException | RuntimeException e) {
			PurgatorioGuia.LOGGER.error("No se pudo leer {}", RESOURCE, e);
		}
	}

	/** Separado de {@link #load()} para poder probarlo con JSON propio. */
	public static List<Topic> parse(JsonObject root) {
		List<Topic> out = new ArrayList<>();
		for (JsonElement e : root.getAsJsonArray("temas")) {
			JsonObject o = e.getAsJsonObject();
			List<Paragraph> paragraphs = new ArrayList<>();
			for (JsonElement p : o.getAsJsonArray("parrafos")) {
				paragraphs.add(new Paragraph(p.getAsJsonObject().get("titulo").getAsString(), p.getAsJsonObject().get("texto").getAsString()));
			}
			List<String> items = new ArrayList<>();
			for (JsonElement i : o.getAsJsonArray("objetos")) {
				items.add(i.getAsString());
			}
			out.add(new Topic(o.get("id").getAsString(), o.get("titulo").getAsString(), o.get("icono").getAsString(),
				o.has("requiere") ? o.get("requiere").getAsString() : "", o.get("resumen").getAsString(), paragraphs, items));
		}
		return out;
	}

	public static List<Topic> all() {
		return topics;
	}

	/** Solo los temas cuyo mod esta cargado. */
	public static List<Topic> available() {
		return topics.stream().filter(Topic::available).toList();
	}
}
