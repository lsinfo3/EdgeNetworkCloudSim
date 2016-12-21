/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.edge;

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
	private static final int BASEEXT = 5000;
	
	/**
	 * Denotes events for interactions between User(Broker) and Service
	 */
	public static final int CLOUDLET_SERVICE_SUBMIT = BASEEXT + 1;
	public static final int VM_SERVICE_SUBMIT = BASEEXT + 2;
	public static final int BROKER_DESTROY_ITSELF_NOW = BASEEXT + 3;
	public static final int SERVICE_DESTROY_VMS_NOW = BASEEXT + 4;
	public static final int SERVICE_SUBMIT_VMS_NOW = BASEEXT + 5;
	public static final int BROKER_CLOUDLETS_NOW = BASEEXT + 6;
	public static final int DATACENTER_BOOT_VM_TAG = BASEEXT + 7;
	public static final int SERVICE_DESTROY_ITSELF_NOW = BASEEXT + 8;
	public static final int CLOUDLET_DESTROY_ITSELF_NOW = BASEEXT + 9;
	public static final int BROKER_MESSAGE = BASEEXT + 10;
	public static final int BROKER_MESSAGE_RETURN = BASEEXT + 11;
	public static final int VM_DC_MAPPING = BASEEXT + 12;
	public static final int SERVICE_CLOUDLET_DONE = BASEEXT + 13;
	public static final int SERVICE_CLOUDLET_DONE_VM = BASEEXT + 14;
	public static final int SERVICE_START = BASEEXT + 15;
	public static final int SERVICE_START_ACK = BASEEXT + 16;
	public static final int SERVICE_ALL_CLOUDLETS_SENT = BASEEXT + 17;
	public static final int KEEP_UP = BASEEXT + 18;
	
	
	
	/** Private Constructor */
	private CloudSimTagsExt() {
		throw new UnsupportedOperationException("CloudSim Tags cannot be instantiated");
	}

}
