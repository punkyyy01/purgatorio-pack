package com.purgatorio.guia.inspect;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Utilidades de texto para los tooltips. */
public final class Text {
	private Text() {
	}

	/** Numero con coma decimal y sin ceros sobrantes ("6,5", "8"). */
	public static String num(double value) {
		String s = String.format(Locale.ROOT, "%.2f", value);
		if (s.contains(".")) {
			s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
		}
		return s.replace('.', ',');
	}

	/** Parte un texto en lineas de como mucho {@code width} caracteres (los tooltips de lore no se ajustan solos). */
	public static List<String> wrap(String text, int width) {
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split("\\s+")) {
			if (!line.isEmpty() && line.length() + 1 + word.length() > width) {
				lines.add(line.toString());
				line.setLength(0);
			}
			if (!line.isEmpty()) {
				line.append(' ');
			}
			line.append(word);
		}
		if (!line.isEmpty()) {
			lines.add(line.toString());
		}
		return lines;
	}
}
