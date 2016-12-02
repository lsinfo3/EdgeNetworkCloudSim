package org.cloudbus.cloudsim.edge;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.edge.service.Service;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM
 * management, as vm creation, sumbission of cloudlets to this VMs and
 * destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author nikolay.grozev
 * @author Brice Kamneng Kwam
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBrokerEdge extends SimEntity {

	/** The service list. */
	protected List<? extends Service> serviceList;

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

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name
	 *            name to be associated with this entity (as required by
	 *            Sim_entity class from simjava package)
	 * @throws Exception
	 *             the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBrokerEdge(String name, final double lifeLength) throws Exception {
		super(name);

		setServiceList(new ArrayList<Service>());
		this.lifeLength = lifeLength;
	}

	public DatacenterBrokerEdge(String name) throws Exception {
		this(name, -1);
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != null
	 * @post $none
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
		// Service send results back after processing Broker Message.
		case CloudSimTagsExt.BROKER_MESSAGE_RETURN:
			processBrokerMessageReturn(ev);
			break;
		case CloudSimTagsExt.BROKER_DESTROY_ITSELF_NOW:
			// do nothing
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
	 * Service send results back after processing Broker Message.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processBrokerMessageReturn(SimEvent ev) {
		// to continu...
		Log.printLine(getName() + " Message was successfully executed by Service #" + ev.getSource());
	}

	/**
	 * 
	 */
	public void startServices() {
		for (Service service : getServiceList()) {
			sendNow(service.getId(), CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT);
		}

	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list
	 *            the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitServiceList(List<? extends Service> list) {
		getServiceList().addAll(list);
	}

	/**
	 * Overrides this method when making a new and different type of Broker.
	 * This method is called by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		switch (ev.getTag()) {
		case CloudSimTagsExt.SERVICE_CLOUDLET_DONE:
			System.out.println(CloudSim.clock() + ": " + getName() + ": Service #" + ev.getSource()
					+ ": all Cloudlets processed!");
			// d();
			// finishExecution();
			break;

		// case CloudSimTagsExt.BROKER_DESTROY_ITSELF_NOW:
		// closeDownBroker();
		// break;
		default:
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker: "
					+ ev.getTag());
			break;
		}
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(CloudSim.clock() + ": " + getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(CloudSim.clock() + ": " + getName() + " is starting...");
//		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	// ==============================EXT========================================

	/**
	 * @return the serviceList
	 */
	@SuppressWarnings("unchecked")
	public <T extends Service> List<T> getServiceList() {
		return (List<T>) serviceList;
	}

	/**
	 * @param serviceList
	 *            the serviceList to set
	 */
	public void setServiceList(List<? extends Service> serviceList) {
		this.serviceList = serviceList;
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
	 *            target entity
	 * @param tag
	 *            Tag
	 * @param data
	 *            the data
	 * @param delay
	 *            the delay before processing this event
	 */
	public void presetEvent(final int id, final int tag, final Object data, final double delay) {
		presetEvents.add(new PresetEvent(id, tag, data, delay));
	}

	/**
	 * send a message to a Service.
	 * 
	 * @param serviceId
	 *            the service id
	 * @param msg
	 *            the message
	 */
	public void sendMessage(int serviceId, Message msg) {
		sendNow(serviceId, CloudSimTagsExt.BROKER_MESSAGE, msg);
	}

	/**
	 * Add a Service to this user Service list.
	 * 
	 * @param service
	 *            Service to add
	 */
	public void addService(Service service) {
		service.setUserId(this.getId());
		this.getServiceList().add(service);
	}
}
