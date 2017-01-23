package org.cloudbus.cloudsim.edge;

/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.edge.service.Service;
import org.cloudbus.cloudsim.edge.util.BaseDatacenter;
import org.cloudbus.cloudsim.edge.util.CustomLog;
import org.cloudbus.cloudsim.edge.util.TextUtil;
import org.cloudbus.cloudsim.edge.vm.T2Small;
import org.cloudbus.cloudsim.edge.vm.VMStatus;
import org.cloudbus.cloudsim.edge.vm.VmEdge;
import org.cloudbus.cloudsim.edge.vm.VmType;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.TaskStage;

/**
 * DatacentreBroker represents a broker acting on behalf of a user.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author nikolay.grozev
 * @author Brice Kamneng Kwam
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBrokerEdge extends SimEntity {

	/** The vm list. */
	private List<? extends Vm> vmList = new ArrayList<>();

	/** The vms created list. */
	private List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	private List<? extends NetworkCloudlet> cloudletList;

	private List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	private List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	private int cloudletsSubmitted;

	/** The vms requested. */
	private int vmsRequested;

	/** The vms acks. */
	private int vmsAcks;

	/** The vms destroyed. */
	private int vmsDestroyed;

	/** The vms to datacenters map. */
	private Map<Integer, Integer> vmsToDatacentersMap;

	private NetworkDatacenter userDC;

	/**
	 * the services to their first Cloudlets maps.
	 */
	private Map<Integer, int[]> servicesToCloudletsMap;

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

	private NetworkCloudlet cloudlet = null;

	private List<SimEvent> messageToSend;

	private int serviceAllCloudletsSent = 0;

	private int stageId = 0;

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
		setVmList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<>());
		setCloudletReceivedList(new ArrayList<>());
		setCloudletSubmittedList(new ArrayList<>());
		setPresetEvents(new ArrayList<>());
		setServicesToCloudletsMap(new HashMap<>());
		setVmsCreatedList(new ArrayList<>());
		setVmsToDatacentersMap(new HashMap<>());
		this.lifeLength = lifeLength;
		this.messageToSend = new ArrayList<>();
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

		switch (ev.getTag()) {
		// Service send results back after processing Broker Message.
		case CloudSimTagsExt.BROKER_MESSAGE_RETURN:
			processBrokerMessageReturn(ev);
			break;
		// process delayed message sending to Service.
		case CloudSimTagsExt.BROKER_MESSAGE:
			processBrokerMessage(ev);
			break;
		case CloudSimTagsExt.BROKER_DESTROY_ITSELF_NOW:
			// do nothing
			break;
		case CloudSimTagsExt.SERVICE_START_ACK:
			processServiceStartAck(ev);
			break;
		case CloudSimTagsExt.SERVICE_ALL_CLOUDLETS_SENT:
			processServiceAllCloudletsSent(ev);
			break;
		case CloudSimTagsExt.KEEP_UP:
			processKeepUp();
			break;
		case CloudSimTags.CLOUDLET_RETURN:
			processCloudletReturn(ev);
			break;
		// VM Creation answer
		case CloudSimTags.VM_CREATE_ACK:
			int[] data = (int[]) ev.getData();
			int vmId = data[1];
			int result = data[2];

			Vm vm = VmList.getById(getVmList(), vmId);
			if (vm.isBeingInstantiated() && result == CloudSimTags.TRUE) {
				vm.setBeingInstantiated(false);
			}
			processVmCreate(ev);
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

	public void processKeepUp() {
		send(getId(), 6.8056469E37, CloudSimTagsExt.KEEP_UP);
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");

		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { 
			// all Cloudlets executed
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": All Cloudlets executed. Finishing...");
			// print cloudlets results
			String indent = "    ";
			System.out.println(indent + indent + indent + indent + indent +"=============> Broker #" + getId() + indent);
			BaseDatacenter.printCloudletList(getCloudletReceivedList());
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created

				System.out.println("Cloudlets waiting for VM creation!");
				// Notify Broker that our Cloudlet are done! but some bount
				// cloudlet is waiting its VM be created
				// sendNow(getId(), CloudSimTagsExt.SERVICE_CLOUDLET_DONE_VM);
			}

		}
	}

	public void processServiceAllCloudletsSent(SimEvent ev) {
		setServiceAllCloudletsSent(getServiceAllCloudletsSent() + 1);
		if (getServiceList().size() == getServiceAllCloudletsSent()) {
			// Now submitting Cloudlets
			submitCloudlets();
		}
	}

	/**
	 * 
	 * @param ev
	 */
	public void processServiceStartAck(SimEvent ev) {
		getServicesToCloudletsMap().put(ev.getSource(), (int[]) ev.getData());
		if (getServiceList().size() == getServicesToCloudletsMap().size()) {
			// all service Fisrt Cloudlet ID have been received
			System.out.println(TextUtil.toString(CloudSim.clock()) + ": [DEBUG]: Broker #" + getId()
					+ " all service Fisrt Cloudlet ID have been received");
		}
	}

	/**
	 * process delayed message sending to Service.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processBrokerMessage(SimEvent ev) {
		Object[] dat = (Object[]) ev.getData();
		int serviceId = (int) dat[0];
		Message msg = (Message) dat[1];

		if (getCloudletsSubmitted() == 0 && getCloudletList().size() > 0
				&& (getServiceList().size() == getServicesToCloudletsMap().size())) {
			createStages(serviceId, msg);
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[DEBUG]: Broker #" + getId()
					+ " process delayed message sending to Service #" + serviceId);
			sendNow(serviceId, CloudSimTagsExt.BROKER_MESSAGE, msg);
		} else {
			System.out.println(TextUtil.toString(CloudSim.clock()) + "[DEBUG]: Broker #" + getId()
					+ " postponing message sending to Service #" + serviceId + " because broker not ready yet");
			send(getId(), 6.8056469E37, CloudSimTagsExt.BROKER_MESSAGE, ev.getData());
		}

	}

	/**
	 * Service send results back after processing Broker Message.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processBrokerMessageReturn(SimEvent ev) {
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + " Message was successfully executed by Service #" + ev.getSource());
	}

	/**
	 * 
	 */
	public void startServices(int cloudletId, int vmId) {
		int[] data = { cloudletId, vmId };
		for (Service service : getServiceList()) {
			sendNow(service.getId(), CloudSimTagsExt.SERVICE_START_ACK, data);
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
	 * This method is used to send to the broker the list with virtual machines
	 * that must be created.
	 * 
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList() {
		getVmList().add(new T2Small());
		for (Vm vm : getVmList()) {
			vm.setUserId(this.getId());
		}
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
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Broker #" + getId() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		switch (ev.getTag()) {
		case CloudSimTagsExt.SERVICE_CLOUDLET_DONE:
			System.out.println(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Service #" + ev.getSource()
					+ ": all Cloudlets processed!");
			break;
		default:
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Broker #" + getId() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker: "
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
		for (Vm vm : getVmList()) {
			finilizeVM(vm);
		}
		clearDatacenters();

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {

		if (!started) {
			started = true;

			for (ListIterator<PresetEvent> iter = presetEvents.listIterator(); iter.hasNext();) {
				PresetEvent event = iter.next();
				send(event.id, event.delay, event.tag, event.data);
				iter.remove();
			}

			// Tell the broker to destroy itself after its lifeline.
			if (getLifeLength() > 0) {
				send(getId(), getLifeLength(), CloudSimTagsExt.BROKER_DESTROY_ITSELF_NOW);
			}
		}

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + " is starting...");
		submitVmList();
		createVmsInDatacenter(getUserDC().getId());

	}

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
	public List<PresetEvent> getPresetEvents() {
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
		Object[] dat = (Object[]) data;
		Object[] daten = new Object[2];
		daten[0] = (int) dat[0];
		daten[1] = (Message) dat[1];
		presetEvents.add(new PresetEvent(id, tag, daten, delay));
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

	/**
	 * @return the vmList
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * @param vmList
	 *            the vmList to set
	 */
	public void setVmList(List<? extends Vm> vmList) {
		this.vmList = vmList;
	}

	/**
	 * @return the vmsCreatedList
	 */
	public List<? extends Vm> getVmsCreatedList() {
		return vmsCreatedList;
	}

	/**
	 * @param vmsCreatedList
	 *            the vmsCreatedList to set
	 */
	public void setVmsCreatedList(List<? extends Vm> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * @return the cloudletList
	 */
	@SuppressWarnings("unchecked")
	public <T extends NetworkCloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * @param cloudletList
	 *            the cloudletList to set
	 */
	public void setCloudletList(List<? extends NetworkCloudlet> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * @return the cloudletSubmittedList
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * @param cloudletSubmittedList
	 *            the cloudletSubmittedList to set
	 */
	public void setCloudletSubmittedList(List<? extends Cloudlet> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * @return the cloudletReceivedList
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * @param cloudletReceivedList
	 *            the cloudletReceivedList to set
	 */
	public void setCloudletReceivedList(List<? extends Cloudlet> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * @return the cloudletsSubmitted
	 */
	public int getCloudletsSubmitted() {
		return cloudletsSubmitted;
	}

	/**
	 * @param cloudletsSubmitted
	 *            the cloudletsSubmitted to set
	 */
	public void setCloudletsSubmitted(int cloudletsSubmitted) {
		this.cloudletsSubmitted = cloudletsSubmitted;
	}

	/**
	 * @return the vmsRequested
	 */
	public int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * @param vmsRequested
	 *            the vmsRequested to set
	 */
	public void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * @return the vmsAcks
	 */
	public int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * @param vmsAcks
	 *            the vmsAcks to set
	 */
	public void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * @return the vmsDestroyed
	 */
	public int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * @param vmsDestroyed
	 *            the vmsDestroyed to set
	 */
	public void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * @return the vmsToDatacentersMap
	 */
	public Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * @param vmsToDatacentersMap
	 *            the vmsToDatacentersMap to set
	 */
	public void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * @return the userDC
	 */
	public NetworkDatacenter getUserDC() {
		return userDC;
	}

	/**
	 * @param userDC
	 *            the userDC to set
	 */
	public void setUserDC(NetworkDatacenter userDC) {
		this.userDC = userDC;
	}

	/**
	 * @param presetEvents
	 *            the presetEvents to set
	 */
	public void setPresetEvents(List<PresetEvent> presetEvents) {
		this.presetEvents = presetEvents;
	}

	/**
	 * @param started
	 *            the started to set
	 */
	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * @return the servicesToCloudletsMap
	 */
	public Map<Integer, int[]> getServicesToCloudletsMap() {
		return servicesToCloudletsMap;
	}

	/**
	 * @param servicesToCloudletsMap
	 *            the servicesToCloudletsMap to set
	 */
	public void setServicesToCloudletsMap(Map<Integer, int[]> servicesToCloudletsMap) {
		this.servicesToCloudletsMap = servicesToCloudletsMap;
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId
	 *            Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the
		// next one
		int requestedVms = 0;
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Trying to Create VM #"
						+ vm.getId() + " in Datacenter #" + datacenterId);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": VM #" + vmId
					+ " created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
//			CustomLog.printf("%s\t\t%s\t\t%s\t\t%s", TextUtil.toString(CloudSim.clock()), "Broker #" + getId(),
//					"VM #" + vmId,
//					"DC #" + datacenterId + " Host #" + VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[ERROR]: Broker #" + getId() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlet(getVmList().get(0).getId());
			for (Service s : getServiceList()) {
				sendNow(s.getId(), CloudSimTagsExt.SERVICE_SUBMIT_VMS_NOW);
			}
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [FATAL]: Broker #" + getId()
						+ ": none of the required VMs could be created. Aborting");
				finishExecution();
			}
		}
	}

	public void createStages(int serviceId, Message msg) {
		int[] serviceData = this.servicesToCloudletsMap.get(serviceId);
		;
		int firstCloudletId = serviceData[0];
		int firstVmId = serviceData[1];
		long data = (msg != null) ? msg.getMips() + 10240 : 10240;

		cloudlet.setCurrStagenum(-1);

		// sending request to the Services.
		cloudlet.setSubmittime(CloudSim.clock());
		cloudlet.setSubmittime(CloudSim.clock());
		cloudlet.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, data, 0, getStageId(), cloudlet.getMemory(),
				firstVmId, firstCloudletId));
		setStageId(getStageId() + 1);

		// waiting for responses from the services.
		cloudlet.setSubmittime(CloudSim.clock());
		cloudlet.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, data, 0, getStageId(), cloudlet.getMemory(),
				firstVmId, firstCloudletId));
		setStageId(getStageId() + 1);

		cloudlet.setNumStage(getStageId());

	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list
	 *            the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudlet(int vmId) {
		NetworkCloudlet ncl = new NetworkCloudlet(40000, 1, 1000, 1000, 100, new UtilizationModelFull(),
				new UtilizationModelFull(), new UtilizationModelFull(), getId(), getId());
		ncl.setVmType(VmType.T2SMALL);
		ncl.setVmId(vmId);
		this.cloudlet = ncl;
		getCloudletList().add(ncl);
		startServices(ncl.getCloudletId(), vmId);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		Cloudlet cloudlet = getCloudletList().get(0);
		int vmId = cloudlet.getVmId();

		if (VmList.getById(getVmsCreatedList(), vmId) == null) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Postponing execution of cloudlet "
					+ cloudlet.getCloudletId() + ": bount VM #" + cloudlet.getVmId() + " not available");
		} else {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Sending cloudlet #"
					+ cloudlet.getCloudletId() + " to VM #" + vmId);

			sendNow(getUserDC().getId(), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);
			// remove submitted cloudlets from waiting list
			getCloudletList().remove(cloudlet);
			for (SimEvent ev : getMessageToSend()) {
				processBrokerMessage(ev);
			}
		}
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	private void finilizeVM(final Vm vm) {
		if (vm instanceof VmEdge) {
			VmEdge vmEX = ((VmEdge) vm);
			if (vmEX.getStatus() != VMStatus.TERMINATED) {
				vmEX.setStatus(VMStatus.TERMINATED);
			}
		}
	}

	/**
	 * @return the messageToSend
	 */
	public List<SimEvent> getMessageToSend() {
		return messageToSend;
	}

	/**
	 * @param messageToSend
	 *            the messageToSend to set
	 */
	public void setMessageToSend(List<SimEvent> messageToSend) {
		this.messageToSend = messageToSend;
	}

	/**
	 * @return the serviceAllCloudletsSent
	 */
	public int getServiceAllCloudletsSent() {
		return serviceAllCloudletsSent;
	}

	/**
	 * @param serviceAllCloudletsSent
	 *            the serviceAllCloudletsSent to set
	 */
	public void setServiceAllCloudletsSent(int serviceAllCloudletsSent) {
		this.serviceAllCloudletsSent = serviceAllCloudletsSent;
	}

	/**
	 * @return the stageId
	 */
	public int getStageId() {
		return stageId;
	}

	/**
	 * @param stageId
	 *            the stageId to set
	 */
	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

}
