/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.ext;

/**
 * Contains various static command tags that indicate a type of action that needs to be undertaken
 * by CloudSim entities when they receive or send events. <b>NOTE:</b> To avoid conflicts with other
 * tags, CloudSim reserves negative numbers, 0 - 299, and 9600.
 * 
 * @author Manzur Murshed
 * @author Rajkumar Buyya
 * @author Anthony Sulistio
 * @since CloudSim Toolkit 1.0
 */
public class CloudSimTagsExt {

	/** Starting constant value for cloud-related tags **/
	private static final int BASE = 5000;
	
	/**
	 * Denotes events for interactions between User(Broker) and Service
	 */
	public static final int CLOUDLET_SERVICE_SUBMIT = BASE + 1;
	public static final int VM_SERVICE_SUBMIT = BASE + 2;
	public static final int BROKER_DESTROY_ITSELF_NOW = BASE + 3;
	public static final int BROKER_DESTROY_VMS_NOW = BASE + 4;
	public static final int BROKER_SUBMIT_VMS_NOW = BASE + 5;
	public static final int BROKER_CLOUDLETS_NOW = BASE + 6;
	public static final int DATACENTER_BOOT_VM_TAG = BASE + 7;
	
	
	/** Private Constructor */
	private CloudSimTagsExt() {
		throw new UnsupportedOperationException("CloudSim Tags cannot be instantiated");
	}

}
