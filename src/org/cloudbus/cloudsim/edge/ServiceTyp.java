/**
 * 
 */
package org.cloudbus.cloudsim.edge;

/**
 * Enum that represent a service typ.
 * 
 * @author Brice Kamneng Kwam
 *
 */
public enum ServiceTyp {

	/**
	 * DbService
	 */
	DB(0, "DB"),

	/**
	 * WebService
	 */
	WEB(1, "WEB"),

	/**
	 * StreamingService
	 */
	STREAMING(2, "STR");

	private int typ;
	private String name;

	private ServiceTyp(int typ, String name) {
		this.typ = typ;
		this.name = name;
	}

	public int getTp() {
		return typ;
	}
	
	public String getName(){
		return name;
	}

}
