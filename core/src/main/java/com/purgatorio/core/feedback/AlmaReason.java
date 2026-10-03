package com.purgatorio.core.feedback;

/** Por que se gana Alma. Decide el tono del mensaje; {@link #SILENT} no genera ningun feedback (pruebas, administracion). */
public enum AlmaReason {
	SILENT,
	DESCUBRIMIENTO,
	DERROTA,
	OTRO;

	public static AlmaReason parse(String word) {
		for (AlmaReason r : values()) {
			if (r.name().equalsIgnoreCase(word)) {
				return r;
			}
		}
		return OTRO;
	}
}
