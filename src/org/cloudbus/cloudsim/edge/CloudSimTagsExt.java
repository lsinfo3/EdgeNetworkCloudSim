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
	private static final int BASEEDGE = 5000;
	
	/**
	 * Denotes events for interactions between User(Broker) and Service
	 */
	public static final int CLOUDLET_SERVICE_SUBMIT = BASEEDGE + 1;
	public static final int VM_SERVICE_SUBMIT = BASEEDGE + 2;
	public static final int BROKER_DESTROY_ITSELF_NOW = BASEEDGE + 3;
	public static final int SERVICE_DESTROY_VMS_NOW = BASEEDGE + 4;
	public static final int SERVICE_SUBMIT_VMS_NOW = BASEEDGE + 5;
	public static final int BROKER_CLOUDLETS_NOW = BASEEDGE + 6;
	public static final int DATACENTER_BOOT_VM_TAG = BASEEDGE + 7;
	public static final int SERVICE_DESTROY_ITSELF_NOW = BASEEDGE + 8;
	public static final int CLOUDLET_DESTROY_ITSELF_NOW = BASEEDGE + 9;
	public static final int BROKER_MESSAGE = BASEEDGE + 10;
	public static final int BROKER_MESSAGE_RETURN = BASEEDGE + 11;
	public static final int VM_DC_MAPPING = BASEEDGE + 12;
	public static final int SERVICE_CLOUDLET_DONE = BASEEDGE + 13;
	public static final int SERVICE_CLOUDLET_DONE_VM = BASEEDGE + 14;
	public static final int SERVICE_START = BASEEDGE + 15;
	public static final int SERVICE_START_ACK = BASEEDGE + 16;
	public static final int SERVICE_ALL_CLOUDLETS_SENT = BASEEDGE + 17;
	public static final int KEEP_UP = BASEEDGE + 18;
	public static final int BROKER_REQUEST = BASEEDGE + 19;
	public static final int SERVICE_DESTROYED_ITSELF = BASEEDGE + 20;
	public static final long DATA_SIZE = 1000000000;
	
	
	
	/** Private Constructor */
	private CloudSimTagsExt() {
		throw new UnsupportedOperationException("CloudSim Tags cannot be instantiated");
	}

}
