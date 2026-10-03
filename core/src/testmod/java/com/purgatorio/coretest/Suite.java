package com.purgatorio.coretest;

/** Una prueba de integracion. Se ejecuta de forma sincrona en el hilo del servidor. */
public interface Suite {
	String name();

	void run(Ctx ctx) throws Exception;
}
