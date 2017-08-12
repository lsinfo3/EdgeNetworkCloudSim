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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.Objects;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.edge.service.Service;
import org.cloudbus.cloudsim.edge.util.BaseDatacenter;
import org.cloudbus.cloudsim.edge.util.CustomLog;
import org.cloudbus.cloudsim.edge.util.Request;
import org.cloudbus.cloudsim.edge.util.TextUtil;
import org.cloudbus.cloudsim.edge.vm.T2Small;
import org.cloudbus.cloudsim.edge.vm.VMStatus;
import org.cloudbus.cloudsim.edge.vm.EdgeVm;
import org.cloudbus.cloudsim.edge.vm.VmType;
import org.cloudbus.cloudsim.lists.CloudletList;
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
public class EdgeDatacenterBroker extends SimEntity {

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

	/** Number of VM destructions requested. */
	private int vmDestructsRequested = 0;

	/** Number of VM destructions acknowledged. */
	private int vmDestructsAcks = 0;

	/** The vms to datacenters map. */
	private Map<Integer, Integer> vmsToDatacentersMap;

	private NetworkDatacenter userDC;

	/**
	 * the services to their first Cloudlets [Cloudlet Id, Vm Id] maps.
	 */
	private Map<Integer, int[]> servicesToServiceCloudletsMap;

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
	 * The amount of services which have already sent their first cloudlet
	 */
	private int serviceAllCloudletsSent = 0;
	private Map<Integer, Boolean> serviceAllCloudletsSentMap;

	/**
	 * Which Cloudlet is responsible for which service.
	 */
	private Map<Integer, Integer> servicesToBrokerCloudletsMap;

	private Map<Integer, Boolean> servicesProcessingRequestMap;

	/**
	 * List of IDs of request this broker has to process, important to process
	 * the requests in the right order.
	 */
	private Map<Integer, List<Integer>> servicesTorequestIdMap;

	/**
	 * Mapping of services to the time of their first request
	 */
	private Map<Integer, Double> servicesToFirstrequestTimeMap;

	/** The datacenter ids list. */
	private List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	private List<Integer> datacenterRequestedIdsList;

	/** The datacenter characteristics list. */
	private Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;
	
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
	public EdgeDatacenterBroker(String name, final double lifeLength) throws Exception {
		super(name);

		setServiceList(new ArrayList<Service>());
		setVmList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<>());
		setCloudletReceivedList(new ArrayList<>());
		setCloudletSubmittedList(new ArrayList<>());
		setPresetEvents(new ArrayList<>());
		setServicesToServiceCloudletsMap(new HashMap<>());
		setServicesToBrokerCloudletsMap(new HashMap<>());
		setVmsCreatedList(new ArrayList<>());
		setVmsToDatacentersMap(new HashMap<>());
		this.lifeLength = lifeLength;
		this.setServicesProcessingRequestMap(new HashMap<>());
		this.setServiceAllCloudletsSentMap(new HashMap<>());
		this.setServicesToFirstrequestTimeMap(new HashMap<>());
		this.servicesTorequestIdMap = new HashMap<>();

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	}

	public EdgeDatacenterBroker(String name) throws Exception {
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

		if (this.getLifeLength() > 0 && CloudSim.clock() > this.getLifeLength()) {
			// Drop Event, since it is over this entity lifetime
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[REQUEST]: Broker #" + getId()
					+ " DROPING Event... from Entity #" + ev.getSource() + "... since over this broker lifetime");
			return;
		}

		switch (ev.getTag()) {
		// Resource characteristics request
		case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
			processResourceCharacteristicsRequest(ev);
			break;
		// Resource characteristics answer
		case CloudSimTags.RESOURCE_CHARACTERISTICS:
			processResourceCharacteristics(ev);
			break;
		// Service send results back after processing Broker Message.
		case CloudSimTagsExt.BROKER_MESSAGE_RETURN:
			processBrokerMessageReturn(ev);
			break;
		// process delayed message sending to Service.
		case CloudSimTagsExt.BROKER_MESSAGE:
			processBrokerMessage(ev);
			break;
		case CloudSimTagsExt.BROKER_DESTROY_ITSELF_NOW:
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Broker #" + getId()
					+ " TIME TO LIVE reached: processing SERVICE_DESTROY_ITSELF_NOW.");
			finishExecution();
			break;
		case CloudSimTagsExt.SERVICE_START_ACK:
			processServiceStartAck(ev);
			break;
		case CloudSimTagsExt.SERVICE_ALL_CLOUDLETS_SENT:
			processServiceAllCloudletsSent(ev);
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

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId()
				+ ": Cloud Resource List received with " + getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the return of a request for the characteristics of a
	 * PowerDatacenter.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			submitVmList();
			createVmsInDatacenter(getNextDcIdWithShortestDelay());
		}
	}

	/**
	 * get the next datacenter with the shortest delay to this broker. meaning
	 * the next best Dc after those that have already been requested.
	 * 
	 * @pre Network enable
	 * @post none
	 * @return the next datacenter with the shortest delay
	 */
	public int getNextDcIdWithShortestDelay() {
		int datacenterId = -1;
		double delay = Double.MAX_VALUE;
		int userDcId = -1;

		// get the list of UDCs which have not been querried yet.
		List<Integer> querrableDcIds = new ArrayList<>();
		for (Integer dcId : datacenterIdsList) {
			if (((NetworkDatacenter) CloudSim.getEntity(dcId)).isUserDC()
					&& !getDatacenterRequestedIdsList().contains(dcId)) {
				querrableDcIds.add(dcId);
			}
		}

		if (querrableDcIds.size() == 0) {
			// All DCs have been querried
			return datacenterId;
		}

		try {
			userDcId = getUserDC().getId();
		} catch (Exception e) {
			Random rand = new Random();
			datacenterId = querrableDcIds.get(rand.nextInt(querrableDcIds.size()));
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: EdgeDatacenterBroker " + getName()
					+ " choosed UDC #" + datacenterId);
			return datacenterId;
		}

		// this part is necessary when the user has more than one UVM
		for (Integer dcId : querrableDcIds) {
			if (userDcId == -1)
				return dcId;

			double tmpDelay = NetworkTopology.getDelay(userDcId, dcId);
			if (tmpDelay < delay && !(getDatacenterRequestedIdsList().contains(dcId))) {
				datacenterId = dcId;
				delay = tmpDelay;
			}
		}
		return datacenterId;
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
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Cloudlet "
				+ cloudlet.getCloudletId() + " received");

		cloudletsSubmitted--;
		getCloudletSubmittedList().remove(cloudlet);
		resetCloudlet((NetworkCloudlet) cloudlet);
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) {
			// all Cloudlets executed
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId()
					+ ": All Cloudlets executed. Finishing...");
			// print cloudlets results
			String indent = "    ";
			System.out
					.println(indent + indent + indent + indent + indent + "=============> Broker #" + getId() + indent);
			BaseDatacenter.printCloudletList(getCloudletReceivedList());
			System.out.println(
					"========================================================================================");
			System.out.println(
					"========================================================================================");
			System.out.println(
					"========================================================================================");
			resetCloudlets();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
			}

		}
	}

	public void processServiceAllCloudletsSent(SimEvent ev) {
		getServiceAllCloudletsSentMap().put(ev.getSource(), true);
		System.out.println("Amount of services : " + getServiceList().size());
		Log.printLine(TextUtil.toString(CloudSim.clock()) + "[REQUEST]: Broker : Service #" + ev.getSource()
				+ " sent all its Cloudlets");

		int amount = 0;
		for (Entry<Integer, Boolean> entry : getServiceAllCloudletsSentMap().entrySet()) {
			if (entry.getValue())
				amount++;
		}
		System.out.println("Amount of services whose cloudlets were completely sent: " + amount);
		// Now submitting Cloudlet
		submitCloudlet(CloudletList.getById(getCloudletList(), getServicesToBrokerCloudletsMap().get(ev.getSource())));
	}

	/**
	 * 
	 * @param ev
	 */
	public void processServiceStartAck(SimEvent ev) {
		getServicesToServiceCloudletsMap().put(ev.getSource(), (int[]) ev.getData());
		System.out.println(TextUtil.toString(CloudSim.clock()) + ": [DEBUG]: Broker #" + getId()
				+ " service Fisrt Cloudlet ID received from Service #" + ev.getSource());
		if (getServiceList().size() == getServicesToServiceCloudletsMap().size()) {
			// all service Fisrt Cloudlet ID have been received
			System.out.println(TextUtil.toString(CloudSim.clock()) + ": [DEBUG]: Broker #" + getId()
					+ " all service Fisrt Cloudlet ID have been received");
		}
	}

	/**
	 * @param ev
	 */
	public void processServiceDestroyedItself(SimEvent ev) {
		int serviceId = ev.getSource();
		System.out.println("PROCESSING SERVICE_DESTROYED_ITSELF FROM SERVICE #" + serviceId);

		// set the flag to specify that this broker is no longer processing
		getServicesProcessingRequestMap().put(serviceId, false);

		NetworkCloudlet brokerCloudlet = CloudletList.getById(getCloudletList(),
				getServicesToBrokerCloudletsMap().get(serviceId));

		if (brokerCloudlet == null) {
			// if null, then the cloudlet has already been submitted!
			brokerCloudlet = CloudletList.getById(getCloudletSubmittedList(),
					getServicesToBrokerCloudletsMap().get(serviceId));
		}

		EdgeVm brokerCloudletVm = VmList.getById(getVmList(), brokerCloudlet.getVmId());

		// destroy the vm associated with this broker Cloudlet
		destroyVm(brokerCloudletVm);

	}

	/**
	 * Submits the list of vms after a given delay
	 * 
	 * @param list
	 * @param delay
	 */
	public void createVmAfter(final Vm vm, final double delay) {
		if (started) {
			send(getUserDC().getId(), delay, CloudSimTags.VM_CREATE_ACK, vm);
		} else {
			presetEvent(getUserDC().getId(), CloudSimTags.VM_CREATE_ACK, vm, delay);
		}
	}

	/**
	 * Destroys the VMs after a specified time period. Used mostly for testing
	 * purposes.
	 * 
	 * @param vm
	 *            - the list of vms to terminate.
	 * @param delay
	 *            - the period to wait for.
	 */
	public void destroyVmAfter(final Vm vm, final double delay) {
		if (started) {
			send(getUserDC().getId(), delay, CloudSimTags.VM_DESTROY_ACK, vm);
		} else {
			presetEvent(getUserDC().getId(), CloudSimTags.VM_DESTROY_ACK, vm, delay);
		}
	}

	/**
	 * try to destroy the given Vm in the corresponding datacenter
	 * 
	 * @param vm
	 */
	protected void destroyVm(Vm vm) {

		if (vm.getHost() == null || vm.getHost().getDatacenter() == null) {
			Log.print("VM " + vm.getId() + " has not been assigned in a valid way and can not be terminated.");
			return;
		}

		// Update the cloudlets before we send the kill event
		vm.getHost().updateVmsProcessing(CloudSim.clock());

		Log.printLine(CloudSim.clock() + ": Broker #" + getId() + ": Trying to Destroy VM #" + vm.getId() + " in DC #"
				+ getVmsToDatacentersMap().get(vm.getId()));

		// Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #"
		// + getId() + ": Destroying VM #" + vm.getId());
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY_ACK, vm);
		incrementVmsDetructsRequested();

	}

	/**
	 * process delayed message sending to Service.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processBrokerMessage(SimEvent ev) {
		Object[] dat = (Object[]) ev.getData();
		int requestId = (int) dat[0];
		int serviceId = (int) dat[1];
		Message msg = (Message) dat[2];

		Cloudlet brokerCloudlet = CloudletList.getById(getCloudletList(),
				getServicesToBrokerCloudletsMap().get(serviceId));

		Service service = (Service) CloudSim.getEntity(serviceId);

		if (this.getLifeLength() > 0 && CloudSim.clock() > this.getLifeLength()) {
			// Drop Request, since it is over this entity lifetime
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[REQUEST]: Broker #" + getId()
					+ " DROPING REQUEST... to Service #" + serviceId + "... since over this broker lifetime");

		} else if (service.getLifeLength() > 0 && CloudSim.clock() > service.getLifeLength()) {
			// Drop Request, since it is over the service lifetime
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[REQUEST]: Broker #" + getId()
					+ " DROPING REQUEST... to Service #" + serviceId + "... since over this service lifetime");

		} else {
			boolean isServiceProcessing = getServicesProcessingRequestMap().get(serviceId);
			boolean containsBrokerId = getCloudletList().contains(brokerCloudlet);
			boolean serviceHasCloudletMapping = getServicesToServiceCloudletsMap().containsKey(serviceId);
			boolean RequestIsNext = this.getServicesTorequestIdMap().get(serviceId).get(0) == requestId;
			// if (!getServicesProcessingRequestMap().get(serviceId) &&
			// getCloudletList().contains(brokerCloudlet)
			// && getServicesToServiceCloudletsMap().containsKey(serviceId)
			// && this.getServicesTorequestIdList().get(serviceId).get(0) ==
			// requestId) {
			if (!isServiceProcessing && containsBrokerId && serviceHasCloudletMapping && RequestIsNext) {
				// remove the request from the service request list
				this.getServicesTorequestIdMap().get(serviceId).remove(0);
				// mark this service as busy
				getServicesProcessingRequestMap().put(serviceId, true);
				createStages(serviceId, msg);
				Log.printLine(TextUtil.toString(CloudSim.clock()) + "[REQUEST]: Broker #" + getId()
						+ " process Request... sending to Service #" + serviceId);
				sendNow(serviceId, CloudSimTagsExt.BROKER_MESSAGE, msg);
			} else {
				// System.out.println(TextUtil.toString(CloudSim.clock()) +
				// "[DEBUG]: Broker #" + getId()
				// + " postponing message sending to Service #" + serviceId + "
				// for 100ms because broker not ready yet");
				send(getId(), 100.0, CloudSimTagsExt.BROKER_MESSAGE, ev.getData());
			}
		}

	}

	/**
	 * Service send results back after processing Broker Message.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processBrokerMessageReturn(SimEvent ev) {
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId()
				+ " Message was successfully executed by Service #" + ev.getSource());
	}

	/**
	 * 
	 */
	public void startService(int serviceId) {
		int cloudletId = getServicesToBrokerCloudletsMap().get(serviceId);
		int vmId = CloudletList.getById(getCloudletList(), cloudletId).getVmId();
		int[] data = { cloudletId, vmId };
		sendNow(serviceId, CloudSimTagsExt.SERVICE_START_ACK, data);

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
		EdgeVm eVm;
		NetworkCloudlet ncl;
		for (Service serv : getServiceList()) {
			eVm = new T2Small();
			ncl = new NetworkCloudlet(40000, 1, 1000, 1000, 100, new UtilizationModelFull(), new UtilizationModelFull(),
					new UtilizationModelFull(), getId());
			ncl.setVmType(VmType.T2SMALL);
			ncl.setVmId(eVm.getId());
			getCloudletList().add(ncl);
			getServicesToBrokerCloudletsMap().put(serv.getId(), ncl.getCloudletId());
			getServicesProcessingRequestMap().put(serv.getId(), false);
			getServiceAllCloudletsSentMap().put(serv.getId(), false);
			getVmList().add(eVm);
		}
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
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Broker #" + getId()
					+ ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		switch (ev.getTag()) {
		case CloudSimTagsExt.SERVICE_CLOUDLET_DONE:
			System.out.println(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Service #"
					+ ev.getSource() + ": all Cloudlets processed!");
			break;
		case CloudSimTags.VM_DESTROY_ACK:
			processVMDestroy(ev);
			break;
		case CloudSimTagsExt.SERVICE_DESTROYED_ITSELF:
			processServiceDestroyedItself(ev);
			break;
		default:
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [ERROR]: Broker #" + getId()
					+ ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker: " + ev.getTag());
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

		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);

		// submitVmList();
		// createVmsInDatacenter(getUserDC().getId());

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
		Object[] daten = new Object[3];
		daten[0] = (int) dat[0];
		daten[1] = (int) dat[1];
		daten[2] = (Message) dat[2];

		presetEvents.add(new PresetEvent(id, tag, daten, delay));
	}

	public void presetEvent(final int id, final int tag, final Request data, final double delay) {
		double del = delay;
		for (PresetEvent pe : presetEvents) {
			if (pe.getDelay() == del) {
				del++;
				break;
			}
		}

		presetEvents.add(new PresetEvent(id, tag, data, del));
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

	public int getVmDestructsRequested() {
		return vmDestructsRequested;
	}

	public void setVmDestructsRequested(int vmDestructsRequested) {
		this.vmDestructsRequested = vmDestructsRequested;
	}

	public int getVmDestructsAcks() {
		return vmDestructsAcks;
	}

	public void setVmDestructsAcks(int vmDestructsAcks) {
		this.vmDestructsAcks = vmDestructsAcks;
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
		Log.printLine(TextUtil.toString(CloudSim.clock()) + "[INFO]: Broker #: " + getId() + " calling setUserDC");
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
	 * @return the servicesToServiceCloudletsMap
	 */
	public Map<Integer, int[]> getServicesToServiceCloudletsMap() {
		return servicesToServiceCloudletsMap;
	}

	/**
	 * @param servicesToServiceCloudletsMap
	 *            the servicesToServiceCloudletsMap to set
	 */
	public void setServicesToServiceCloudletsMap(Map<Integer, int[]> servicesToCloudletsMap) {
		this.servicesToServiceCloudletsMap = servicesToCloudletsMap;
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
		int requestedVms = 0;
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				int associatedServiceId = getServiceIdForCloudletId(getCloudletIdForVmId(vm.getId()));
				double associatedServiceFirstRequest = getServicesToFirstrequestTimeMap().get(associatedServiceId);
				double realStart = associatedServiceFirstRequest - CloudSim.clock();

				if (realStart > 0) {
					Log.printLine(
							TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Will try to Create VM #"
									+ vm.getId() + " in Datacenter #" + datacenterId + " in " + realStart + " msec");
					send(datacenterId, realStart, CloudSimTags.VM_CREATE_ACK, vm);
				} else {
					Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId()
							+ ": Will try to Create VM #" + vm.getId() + " in Datacenter #" + datacenterId + " ASAP");
					sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				}
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

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

		incrementVmsAcks();

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": VM #" + vmId
					+ " created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());

			try {
				// was the User DC already set?
				getUserDC().getId();
				// do nothing if it was.
			} catch (Exception e) {
				// if not, set it now.
				setUserDC((NetworkDatacenter) CloudSim.getEntity(datacenterId));
			}

			// get the Broker Cloudlet ID corresponding to this VM Id
			int brokerCloudletId = getCloudletIdForVmId(vmId);
			// get the Service ID corresponding to this Broker Cloudlet ID
			int serviceId = getServiceIdForCloudletId(brokerCloudletId);

			// start the service
			startService(serviceId);
			// notify the service to start creating its VMs
			send(serviceId, 1.0, CloudSimTagsExt.SERVICE_SUBMIT_VMS_NOW);

		} else {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[ERROR]: Broker #" + getId() + ": Creation of VM #"
					+ vmId + " failed in Datacenter #" + datacenterId);
		}

		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			// all the requested VMs have been created
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Broker #" + getId()
					+ ": all the requested VMs have been created");
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				int dcId = getNextDcIdWithShortestDelay();
				if (dcId != -1) {
					createVmsInDatacenter(dcId);
					return;
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					System.out.println(TextUtil.toString(CloudSim.clock()) + ": [DEBUG]: Broker #" + getId()
							+ " some VMs were created... But not all. Continue");
//					finishExecution();
				} else { // no vms created. abort
					Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	public int getCloudletIdForVmId(int vmId) {
		for (NetworkCloudlet ncl : getCloudletList()) {
			if (ncl.getVmId() == vmId)
				return ncl.getCloudletId();
		}
		return -1;
	}

	public int getServiceIdForCloudletId(int cloudletId) {
		for (Map.Entry<Integer, Integer> entry : getServicesToBrokerCloudletsMap().entrySet()) {
			if (entry.getValue() == cloudletId)
				return entry.getKey();
		}
		return -1;
	}

	public void createStages(int serviceId, Message msg) {
		int[] serviceData = this.getServicesToServiceCloudletsMap().get(serviceId);
		NetworkCloudlet cloudlet = CloudletList.getById(getCloudletList(),
				getServicesToBrokerCloudletsMap().get(serviceId));
		int firstCloudletId = serviceData[0];
		int firstVmId = serviceData[1];

		cloudlet.setStages(new ArrayList<TaskStage>());

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Broker #" + getId()
				+ ": called createStages with service # " + serviceId + ", service 1st CL #" + firstCloudletId
				+ " and service 1st CL VM #" + firstVmId + " Cloudlet #" + cloudlet.getCloudletId() + " and Message "
				+ msg + " and # of Cloudlet Submitted: " + getCloudletSubmittedList().size());

		long data = (msg != null) ? msg.getMips() + CloudSimTagsExt.DATA_SIZE : CloudSimTagsExt.DATA_SIZE;

		cloudlet.setCurrStagenum(-1);

		// sending request to the Services.
		cloudlet.setSubmittime(CloudSim.clock());
		cloudlet.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, data, 0, 0, cloudlet.getMemory(), firstVmId,
				firstCloudletId));

		// waiting for responses from the services.
		cloudlet.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, data, 0, 1, cloudlet.getMemory(), firstVmId,
				firstCloudletId));

		cloudlet.setNumStage(2);

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Broker #" + getId() + " Cloudlet #"
				+ cloudlet.getCloudletId() + " has " + cloudlet.getStages().size() + " Stages");

	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		Cloudlet cloudlet;
		int vmId;

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": called submitCloudlets() ");

		ArrayList<Cloudlet> toRemove = new ArrayList<>();
		CloudletList.sortById(getCloudletList());
		for (int i = 0; i < getCloudletList().size(); i++) {
			cloudlet = getCloudletList().get(i);
			vmId = cloudlet.getVmId();
			if (VmList.getById(getVmsCreatedList(), vmId) == null) {
				Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId()
						+ ": Postponing execution of cloudlet " + cloudlet.getCloudletId() + ": bount VM #"
						+ cloudlet.getVmId() + " not available");
				continue;
			}
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Sending cloudlet #"
					+ cloudlet.getCloudletId() + " to VM #" + vmId);

			sendNow(getVmsToDatacentersMap().get(vmId), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
//			sendNow(getUserDC().getId(), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);
			// remove submitted cloudlets from waiting list
			toRemove.add(cloudlet);
		}

		getCloudletList().removeAll(toRemove);
	}

	public void submitCloudlet(Cloudlet cloudlet) {
		int vmId = cloudlet.getVmId();

		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId()
				+ ": called submitCloudlet with Cloudlet #" + cloudlet.getCloudletId());

		if (VmList.getById(getVmsCreatedList(), vmId) == null) {
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM #" + cloudlet.getVmId() + " not available");
		} else {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Sending cloudlet #"
					+ cloudlet.getCloudletId() + " to VM #" + vmId);

			sendNow(getVmsToDatacentersMap().get(vmId), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
//			sendNow(getUserDC().getId(), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);

			// remove submitted cloudlet from waiting list
			getCloudletList().remove(cloudlet);
		}

	}

	protected void incrementVmsDetructsRequested() {
		this.setVmDestructsRequested(this.getVmDestructsRequested() + 1);
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {

		for (Vm vm : getVmsCreatedList()) {
			if (vm.getHost() == null || vm.getHost().getDatacenter() == null) {
				Log.print("VM " + vm.getId() + " has not been assigned in a valid way and can not be terminated.");
				continue;
			}

			// Update the cloudlets before we send the kill event
			vm.getHost().updateVmsProcessing(CloudSim.clock());

			Log.printLine(CloudSim.clock() + ": Broker #" + getId() + ": Trying to Destroy VM #" + vm.getId()
					+ " in DC #" + getVmsToDatacentersMap().get(vm.getId()));

			// Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #"
			// + getId() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY_ACK, vm);
			incrementVmsDetructsRequested();
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Increments the counter of VM destruction acknowledgments.
	 */
	protected void incrementVmDesctructsAcks() {
		setVmDestructsAcks(getVmDestructsAcks() + 1);
	}

	private void processVMDestroy(SimEvent ev) {
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": PROCESSING VM DESTROYED ACK");
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			Vm vm = VmList.getById(getVmsCreatedList(), vmId);

			// One more ack. to consider
			incrementVmDesctructsAcks();

			// Remove the vm from the created list
			getVmsCreatedList().remove(vm);
			finilizeVM(vm);

			// Kill all cloudlets associated with this VM

			for (Cloudlet cloudlet : getCloudletSubmittedList()) {
				if (!cloudlet.isFinished() && vmId == cloudlet.getVmId()) {
					System.out.println(CloudSim.clock() + ": Broker #" + getId() + " TRYING TO TERMINATE CLOUDLET #"
							+ cloudlet.getCloudletId() + " ASSOCIATED WITH VM #" + cloudlet.getVmId());
					try {
						vm.getCloudletScheduler().cloudletCancel(cloudlet.getCloudletId());
						cloudlet.setCloudletStatus(Cloudlet.FAILED_RESOURCE_UNAVAILABLE);
					} catch (Exception e) {
						CustomLog.logError(Level.SEVERE, e.getMessage(), e);
						System.out.println(
								CloudSim.clock() + ": Broker #" + getId() + "CLOUDLET TERMINATION DID NOT WORK!!!");
						System.out.println(CloudSim.clock() + ": Broker #" + getId() + Level.SEVERE + e.getMessage()
								+ e.toString());
					}

					sendNow(cloudlet.getUserId(), CloudSimTags.CLOUDLET_RETURN, cloudlet);
				}
			}

			// Use the standard log for consistency ....
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": VM #" + vmId
					+ " has been destroyed in Datacenter #" + datacenterId);
		} else {
			// Use the standard log for consistency ....
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Broker #" + getId() + ": Desctuction of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

	}

	private void finilizeVM(final Vm vm) {
		if (vm instanceof EdgeVm) {
			EdgeVm vmEX = ((EdgeVm) vm);
			if (vmEX.getStatus() != VMStatus.TERMINATED) {
				vmEX.setStatus(VMStatus.TERMINATED);
			}
		}
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
	 * Reset the Broker Cloudlets, to process the next request.
	 */
	public void resetCloudlets() {
		setCloudletList(getCloudletReceivedList().size() > 0 ? getCloudletReceivedList() : getCloudletList());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());
		setCloudletsSubmitted(0);
		setServiceAllCloudletsSent(0);
		for (Service service : getServiceList()) {
			getServicesProcessingRequestMap().put(service.getId(), false);
		}

		for (NetworkCloudlet networkCloudlet : getCloudletList()) {
			networkCloudlet.reset();
		}
	}

	/**
	 * Reset the given Broker Cloudlet, to process the next request.
	 * 
	 * @param cloudletId
	 */
	public void resetCloudlet(NetworkCloudlet networkCloudlet) {
		getCloudletReceivedList().remove(networkCloudlet);
		getCloudletList().add(networkCloudlet);

		for (Map.Entry<Integer, Integer> entry : getServicesToBrokerCloudletsMap().entrySet()) {
			if (entry.getValue() == networkCloudlet.getCloudletId()) {
				int serviceId = entry.getKey();
				getServicesProcessingRequestMap().put(serviceId, false);
				getServiceAllCloudletsSentMap().put(serviceId, false);
				break;
			}
		}

		networkCloudlet.reset();
	}

	public Map<Integer, Integer> getServicesToBrokerCloudletsMap() {
		return servicesToBrokerCloudletsMap;
	}

	public void setServicesToBrokerCloudletsMap(Map<Integer, Integer> servicesToBrokerCloudletsMap) {
		this.servicesToBrokerCloudletsMap = servicesToBrokerCloudletsMap;
	}

	public Map<Integer, Boolean> getServicesProcessingRequestMap() {
		return servicesProcessingRequestMap;
	}

	public void setServicesProcessingRequestMap(Map<Integer, Boolean> servicesProcessingRequestMap) {
		this.servicesProcessingRequestMap = servicesProcessingRequestMap;
	}

	public Map<Integer, Boolean> getServiceAllCloudletsSentMap() {
		return serviceAllCloudletsSentMap;
	}

	public void setServiceAllCloudletsSentMap(Map<Integer, Boolean> serviceAllCloudletsSentMap) {
		this.serviceAllCloudletsSentMap = serviceAllCloudletsSentMap;
	}

	public void addRequestId(int serviceId, int requestId) {
		List<Integer> ids;
		if (this.getServicesTorequestIdMap().containsKey(serviceId)) {
			ids = this.getServicesTorequestIdMap().get(serviceId);
			ids.add(requestId);
		} else {
			ids = new ArrayList<>();
			ids.add(requestId);
		}
		this.getServicesTorequestIdMap().put(serviceId, ids);
	}

	public Map<Integer, List<Integer>> getServicesTorequestIdMap() {
		return servicesTorequestIdMap;
	}

	public Map<Integer, Double> getServicesToFirstrequestTimeMap() {
		return servicesToFirstrequestTimeMap;
	}

	public void setServicesToFirstrequestTimeMap(Map<Integer, Double> servicesToFirstrequestTimeMap) {
		this.servicesToFirstrequestTimeMap = servicesToFirstrequestTimeMap;
	}

	public void addServiceFirstRequestTime(int serviceId, double firstRequestTime) {
		getServicesToFirstrequestTimeMap().put(serviceId, firstRequestTime);
	}

	public List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	public void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	public List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	public void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

	public Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	public void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}
}
