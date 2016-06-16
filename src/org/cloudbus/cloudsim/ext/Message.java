/**
 * 
 */
package org.cloudbus.cloudsim.ext;

/** Enum which represent the size of a cloudlet.
 * it gives the amount of mips.
 * @author Brice Kamneng Kwam
 *
 */
public enum Message {

	/**
	 * 1 mips.
	 */
	ONE(1000000),
	/**
	 * 10 mips.
	 */
	TEN(10000000),
	/**
	 * 100 mips.
	 */
	HUNDRED(100000000),
	/**
	 * 1000 mips.
	 */
	THOUSAND(1000000000);
	
	private long mips;
	
	private Message(long mips){
		this.mips = mips;
	}
	
	public long getMips(){
		return mips;
	}
}
