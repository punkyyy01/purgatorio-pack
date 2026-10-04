package purgatorio.es;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;

/** Mete las traducciones de este mod (assets/&lt;ns&gt;/lang) en el resource pack de Polymer. */
public class EsMod implements ModInitializer {
	@Override
	public void onInitialize() {
		PolymerResourcePackUtils.addModAssets("zz_purgatorio_es");
	}
}
