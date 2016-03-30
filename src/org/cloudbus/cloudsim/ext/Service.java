package org.cloudbus.cloudsim.ext;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * @author Brice Kamneng Kwam
 *
 */
public class Service extends SimEntity{
	
	/**
	 * The User or Broker ID. It is advisable that broker set this ID with its own ID, so that
	 * CloudResource returns to it after the execution.
	 **/
	private int userId;
	
	
	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;
	
	
	public Service(String name){
		super(name);
	}
	
	
	/**
	 * Gets the user or owner ID of this Cloudlet.
	 * 
	 * @return the user ID or <tt>-1</tt> if the user ID has not been set before
	 * @pre $none
	 * @post $result >= -1
	 */
	public int getUserId() {
		return userId;
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.core.SimEntity#processEvent(org.cloudbus.cloudsim.core.SimEvent)
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}
	
	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		// TODO Auto-generated method stub
		
	}

}
