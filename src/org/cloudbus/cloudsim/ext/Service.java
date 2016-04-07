package org.cloudbus.cloudsim.ext;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

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
	
	/**
     * Events that will be executed after the broker has started. The are
     * usually set before the simulation start.
     */
    private List<PresetEvent> presetEvents = new ArrayList<>();

    /** If this broker has started receiving and responding to events. */
    private boolean started = false;

    /**
     * How lond we should keep this broker alive. If negative - the broker is
     * killed when no more cloudlets are left.
     */
    private final double lifeLength;
	
	
	public Service(String name, final double lifeLength){
		super(name);
		this.lifeLength = lifeLength;
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
		if (!started) {
            started = true;

            for (ListIterator<PresetEvent> iter = presetEvents.listIterator(); iter.hasNext();) {
                PresetEvent event = iter.next();
                send(event.id, event.delay, event.tag, event.data);
                iter.remove();
            }

            // Tell the broker to destroy itself after its lifeline.
            if (getLifeLength() > 0) {
                send(getId(), getLifeLength(), CloudSimTagsExt.BROKER_DESTROY_ITSELF_NOW, null);
            }
        }
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
//				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
//				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
//				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
//				processCloudletReturn(ev);
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
		Log.printLine(getName() + " is shutting down...");
		
	}
	
	@Override
    public String toString() {
        return String.valueOf(String.format("Broker(%s, %d)", Objects.toString(getName(), "N/A"), getId()));
    }
	
	/**
     * Returns if this broker has started to respond to events.
     * 
     * @return if this broker has started to respond to events.
     */
    protected boolean isStarted() {
        return started;
    }
    
    public double getLifeLength() {
        return lifeLength;
    }
    
    /**
     * Returns the list of preset events.
     * 
     * @return the list of preset events.
     */
    protected List<PresetEvent> getPresetEvents() {
        return presetEvents;
    }
    
    /**
     * Schedule an event that will be run with a given delay after the
     * simulation has started.
     * 
     * @param id
     * @param tag
     * @param data
     * @param delay
     */
    public void presetEvent(final int id, final int tag, final Object data, final double delay) {
        presetEvents.add(new PresetEvent(id, tag, data, delay));
    }
    
    /**
     * Submits the cloudlets after a specified time period. Used mostly for
     * testing purposes.
     * 
     * @param cloudlets
     *            - the cloudlets to submit.
     * @param delay
     *            - the delay.
     */
    public void submitCloudletList(List<Cloudlet> cloudlets, double delay) {
        if (started) {
            send(getId(), delay, CloudSimTagsExt.BROKER_CLOUDLETS_NOW, cloudlets);
        } else {
            presetEvent(getId(), CloudSimTagsExt.BROKER_CLOUDLETS_NOW, cloudlets, delay);
        }
    }

}
