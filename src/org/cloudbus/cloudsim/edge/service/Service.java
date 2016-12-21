package org.cloudbus.cloudsim.edge.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.edge.CloudSimTagsExt;
import org.cloudbus.cloudsim.edge.Message;
import org.cloudbus.cloudsim.edge.PresetEvent;
import org.cloudbus.cloudsim.edge.util.BaseDatacenter;
import org.cloudbus.cloudsim.edge.util.CustomLog;
import org.cloudbus.cloudsim.edge.vm.VMStatus;
import org.cloudbus.cloudsim.edge.vm.VmEdge;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;

/**
 * @author Brice Kamneng Kwam
 * 
 */
public abstract class Service extends SimEntity {

	/**
	 * The User or Broker ID. It is advisable that broker set this ID with its
	 * own ID, so that CloudResource returns to it after the execution.
	 **/
	private int userId;

	/**
	 * Events that will be executed after the broker has started. The are
	 * usually set before the simulation start.
	 */
	private List<PresetEvent> presetEvents = new ArrayList<>();

	/** If this broker has started receiving and responding to events. */
	private boolean started = false;

	private Map<Integer, Message> cloudletIdToMessage = new HashMap<Integer, Message>();

	/**
	 * How long we should keep this broker alive. If negative - the broker is
	 * killed when no more cloudlets are left.
	 */
	private final double lifeLength;

	private boolean cloudletGenerated;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The vm list. */
	protected List<? extends Vm> vmList;

	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	/** Number of VM destructions requested. */
	private int vmDestructsRequested = 0;

	/** Number of VM destructions acknowledged. */
	private int vmDestructsAcks = 0;

	/**
	 * The first Cloudlet of this Service. The one that communicates with the
	 * Broker
	 */
	private NetworkCloudlet firstCloudlet = null;
	/**
	 * The Vm assigned to the first Cloudlet of this Service.
	 */
	private int firstVmId = -1;
	/**
	 * The cloudlet of the Broker.
	 */
	private int brokerCloudletId = -1;
	/**
	 * The Vm of the Broker.
	 */
	private int brokerVmId = -1;

	/**
	 * Constr.
	 * 
	 * @param name
	 *            - the name of the service.
	 * @param lifeLength
	 *            - for how long we need to keep this broker alive. If -1, then
	 *            the broker is kept alive/running untill all cloudlets
	 *            complete.
	 * @throws Exception
	 *             - from the superclass.
	 */
	public Service(String name, final double lifeLength) {
		super(name);
		this.lifeLength = lifeLength;
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		cloudletsSubmitted = 0;
		cloudletGenerated = false;

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());

		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

	}

	/**
	 * This method is used to send to the broker the list with virtual machines
	 * that must be created.
	 * 
	 * @param list
	 *            the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
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
			System.out.println(CloudSim.clock() + ": [DEBUG] :processResourceCharacteristics: " + getName()
					+ " Vms have been submitted/created");
			System.out.println(Double.MAX_VALUE - 102084703991558655000000000000000000000000000000000000000000.0);
			// createVmsInDatacenter(getNextDcIdWithShortestDelay());
		}
	}

	public abstract void submitVmList();

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

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
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
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId + " has been created in Datacenter #"
					+ datacenterId + ", Host #" + VmList.getById(getVmsCreatedList(), vmId).getHost().getId()
					+ " owned by user #" + getId());
		} else {
			Log.printLine(CloudSim.clock() + "[ERROR]: " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			System.out.println(
					CloudSim.clock() + ": [DEBUG]: " + getName() + " all the requested VMs have been created ");
			// submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				if (getNextDcIdWithShortestDelay() != -1) {
					createVmsInDatacenter(getNextDcIdWithShortestDelay());
					return;
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					System.out.println(CloudSim.clock() + ": [DEBUG]: " + getName() + " some vm were created");
					// submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
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
		// String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in Datacenter #" + datacenterId);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
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

	private void finilizeVM(final Vm vm) {
		if (vm instanceof VmEdge) {
			VmEdge vmEX = ((VmEdge) vm);
			if (vmEX.getStatus() != VMStatus.TERMINATED) {
				vmEX.setStatus(VMStatus.TERMINATED);
			}
		}
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param vmList
	 *            the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param vmsCreatedList
	 *            the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested
	 *            the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks
	 *            the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed
	 *            the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList
	 *            the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList
	 *            the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList
	 *            the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

	/**
	 * Sets the number of requested VM destructions.
	 * 
	 * @param vmDestructsRequested
	 *            - the number of requested VM destructions. A valid positive
	 *            integer or 0.
	 */
	public void setVmDestructsRequested(int vmDestructsRequested) {
		this.vmDestructsRequested = vmDestructsRequested;
	}

	/**
	 * Returns the number of requested VM destructions.
	 * 
	 * @return the number of requested VM destructions.
	 */
	public int getVmDestructsRequested() {
		return vmDestructsRequested;
	}

	/**
	 * Returns the number of acknowledged VM destructions.
	 * 
	 * @return the number of acknowledged VM destructions.
	 */
	public int getVmDestructsAcks() {
		return vmDestructsAcks;
	}

	/**
	 * Sets the number of acknowledged VM destructions.
	 * 
	 * @param vmDestructsAcks
	 *            - acknowledged VM destructions. A valid positive integer or 0.
	 */
	public void setVmDestructsAcks(int vmDestructsAcks) {
		this.vmDestructsAcks = vmDestructsAcks;
	}

	/**
	 * Increments the counter of VM destruction acknowledgments.
	 */
	protected void incrementVmDesctructsAcks() {
		vmDestructsAcks++;
	}

	/**
	 * Submits the list of vms after a given delay
	 * 
	 * @param list
	 * @param delay
	 */
	public void createVmsAfter(List<? extends Vm> vms, double delay) {
		if (started) {
			send(getId(), delay, CloudSimTagsExt.SERVICE_SUBMIT_VMS_NOW, vms);
		} else {
			presetEvent(getId(), CloudSimTagsExt.SERVICE_SUBMIT_VMS_NOW, vms, delay);
		}
	}

	/**
	 * Destroys the VMs after a specified time period. Used mostly for testing
	 * purposes.
	 * 
	 * @param vms
	 *            - the list of vms to terminate.
	 * @param delay
	 *            - the period to wait for.
	 */
	public void destroyVMsAfter(final List<? extends Vm> vms, double delay) {
		if (started) {
			send(getId(), delay, CloudSimTagsExt.SERVICE_DESTROY_VMS_NOW, vms);
		} else {
			presetEvent(getId(), CloudSimTagsExt.SERVICE_DESTROY_VMS_NOW, vms, delay);
		}
	}

	private void processVMDestroy(SimEvent ev) {
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
					try {
						vm.getCloudletScheduler().cloudletCancel(cloudlet.getCloudletId());
						cloudlet.setCloudletStatus(Cloudlet.FAILED_RESOURCE_UNAVAILABLE);
					} catch (Exception e) {
						CustomLog.logError(Level.SEVERE, e.getMessage(), e);
					}

					sendNow(cloudlet.getUserId(), CloudSimTags.CLOUDLET_RETURN, cloudlet);
				}
			}

			// Use the standard log for consistency ....
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId + " has been destroyed in Datacenter #"
					+ datacenterId);
		} else {
			// Use the standard log for consistency ....
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Desctuction of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

	}

	/**
	 * Destroys/terminates the vms.
	 * 
	 * @param vms
	 *            - the vms to terminate. Must not be null.
	 */
	public void destroyVMList(final List<? extends Vm> vms) {
		if (getVmDestructsAcks() != getVmsDestroyed()) {
			throw new IllegalStateException("#" + getVmsDestroyed() + " have been marked for termination, but only #"
					+ getVmDestructsAcks() + " acknowlegdements have been received.");
		}

		int requestedVmTerminations = 0;
		for (final Vm vm : vms) {
			if (vm.getHost() == null || vm.getHost().getDatacenter() == null) {
				Log.print("VM " + vm.getId() + " has not been assigned in a valid way and can not be terminated.");
				continue;
			}

			// Update the cloudlets before we send the kill event
			vm.getHost().updateVmsProcessing(CloudSim.clock());

			int datacenterId = vm.getHost().getDatacenter().getId();
			// String datacenterName = vm.getHost().getDatacenter().getName();

			// Log.printConcatLine(CloudSim.clock(), ": ", getName(),
			// ": Trying to Destroy VM #", vm.getId(), " in ",
			// datacenterName);

			// Tell the data centre to destroy it
			sendNow(datacenterId, CloudSimTags.VM_DESTROY_ACK, vm);
			requestedVmTerminations++;
		}

		setVmsDestroyed(requestedVmTerminations);
		setVmDestructsAcks(0);
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
		for (Integer dcId : datacenterIdsList) {
			if (!((NetworkDatacenter) CloudSim.getEntity(dcId)).isUserDC()) {
				if (NetworkTopology.getDelay(getId(), dcId) < delay
						&& !(getDatacenterRequestedIdsList().contains(dcId))) {
					datacenterId = dcId;
//					delay = NetworkTopology.getDelay(getId(), dcId);
				}
			}
		}
		return datacenterId;
	}

	// ==============================BROKER========================================

	/**
	 * Constr.
	 * 
	 * @param name
	 *            - the name of the broker.
	 * @throws Exception
	 *             - from the superclass.
	 */
	public Service(String name) {
		this(name, -1);
	}

	/**
	 * @return the cloudletGenerated
	 */
	public boolean isCloudletGenerated() {
		return cloudletGenerated;
	}

	/**
	 * @param cloudletGenerated
	 *            the cloudletGenerated to set
	 */
	public void setCloudletGenerated(boolean cloudletGenerated) {
		this.cloudletGenerated = cloudletGenerated;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(CloudSim.clock() + ": " + getName() + " #" + getId() + " is starting...");

		if (!started) {
			started = true;

			for (ListIterator<PresetEvent> iter = presetEvents.listIterator(); iter.hasNext();) {
				PresetEvent event = iter.next();
				send(event.getId(), event.getDelay(), event.getTag(), event.getData());
				iter.remove();
			}

			// Tell the Service to destroy itself after its lifeline.
			// this event does not have to be processed, but as long
			// as there an event for a given entity in the future queue
			// of CloudSim, CloudSim will not shut down the entity.
			if (getLifeLength() > 0) {
				send(getId(), getLifeLength(), CloudSimTagsExt.SERVICE_DESTROY_ITSELF_NOW, null);
			}
		}

		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cloudbus.cloudsim.core.SimEntity#processEvent(org.cloudbus.cloudsim
	 * .core.SimEvent)
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
			int[] data = (int[]) ev.getData();
			int vmId = data[1];
			int result = data[2];

			Vm vm = VmList.getById(getVmList(), vmId);
			if (vm.isBeingInstantiated() && result == CloudSimTags.TRUE) {
//			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}
			processVmCreate(ev);
			break;
		// start submitting Cloudlets
		case CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT:
			System.out.println(CloudSim.clock() + ": [DEBUG]: " + getName()
					+ " received CLOUDLET_SERVICE_SUBMIT flag from #" + ev.getSource());
			submitCloudlets();
			break;
		// A finished cloudlet returned
		case CloudSimTagsExt.SERVICE_START_ACK:
			// TODO start the Service without submitting Cloudlet, create
			// Cloudlet and return the id of the first Cloudlet
			processServiceStart(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTags.CLOUDLET_RETURN:
			processCloudletReturn(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTags.CLOUDLET_PAUSE_ACK:
			processCloudletPausedAck(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTags.CLOUDLET_RESUME_ACK:
			processCloudletPausedAck(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTagsExt.BROKER_MESSAGE:
			processBrokerMessage(ev);
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

	public void processServiceStart(SimEvent ev) {
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			int[] data = (int[]) ev.getData();
			setBrokerCloudletId(data[0]);
			setBrokerVmId(data[1]);

			if (getFirstCloudlet() != null && getFirstVmId() != -1) {
				int[] dat = { getFirstCloudlet().getCloudletId(), getFirstVmId() };
				sendNow(getUserId(), CloudSimTagsExt.SERVICE_START_ACK, dat);
			} else {
				generateCloudlets();
				assignVmToCloudlets();
				int[] dat = { getFirstCloudlet().getCloudletId(), getFirstVmId() };
				sendNow(getUserId(), CloudSimTagsExt.SERVICE_START_ACK, dat);
			}

		} else {
			System.out.println(CloudSim.clock() + ": [DEBUG]: " + getName()
					+ " all Vms not created yet, postponning Service start to 1.0 ");
			send(getId(), 1.0, CloudSimTagsExt.SERVICE_START_ACK, ev.getData());
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
	@SuppressWarnings("unchecked")
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}
		switch (ev.getTag()) {
		case CloudSimTags.VM_DESTROY_ACK:
			processVMDestroy(ev);
			break;
		case CloudSimTagsExt.SERVICE_DESTROY_VMS_NOW:
			destroyVMList((List<Vm>) ev.getData());
			break;
		case CloudSimTagsExt.SERVICE_SUBMIT_VMS_NOW:
			createVmsInDatacenter(getNextDcIdWithShortestDelay());
			break;
		case CloudSimTagsExt.SERVICE_CLOUDLET_DONE_VM:
			System.out.println(CloudSim.clock() + ": " + getName() + ": Service #" + ev.getSource()
					+ ": almost all Cloudlets processed, but some are still waiting for their VMs to be created!");

			// clearDatacenters();
			if (getNextDcIdWithShortestDelay() != -1) {
				createVmsInDatacenter(getNextDcIdWithShortestDelay());
			}
			break;
		default:
			Log.printLine(
					getName() + ".processOtherEvent(): " + "Error - event unknown by this Service: " + ev.getTag());
			break;
		}

	}

	/**
	 * process a Message sent by the broker/user.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processBrokerMessage(SimEvent ev) {
		if (!cloudletGenerated) {
			generateCloudlets();
		} else {
			setCloudletList(getCloudletReceivedList().size() > 0 ? getCloudletReceivedList() : getCloudletList());
			setCloudletSubmittedList(new ArrayList<Cloudlet>());
			setCloudletReceivedList(new ArrayList<Cloudlet>());
		}
		System.out.println(
				CloudSim.clock() + "[DEBUG]: Service #" + getId() + ": Message received from Broker #" + getUserId());
		// generateCloudlets();
		for (int i = 0; i < getCloudletList().size(); i++) {
			((NetworkCloudlet) getCloudletList().get(i)).reset();
			getCloudletList().get(i).setCloudletLength(
					getCloudletList().get(i).getCloudletLength() + ((Message) ev.getData()).getMips());
		}
		createStages();
		setCloudletGenerated(true);
		submitCloudlets();
	}

	protected void processCloudletPausedAck(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int cloudletId = data[1];
		if (cloudletIdToMessage.containsKey(cloudletId)) {
			Cloudlet cloudlet = CloudletList.getById(getCloudletList(), cloudletId);
			Message msg = cloudletIdToMessage.get(cloudletId);
			// get the previous length
			// cut the size that has already been processed
			// add the one from the Message
			cloudlet.setCloudletLength(
					(cloudlet.getCloudletLength() - cloudlet.getCloudletFinishedSoFar()) + msg.getMips()); // not
																											// sure
			// how to
			// calculate
			// this yet.
			// resume the Cloudlet
			sendNow(getVmsToDatacentersMap().get(cloudlet.getVmId()), CloudSimTags.CLOUDLET_RESUME, cloudlet);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		// if ((getCloudletList().size() > 0 && cloudletsSubmitted > 0)) {
		// send(getId(), 100.0, CloudSimTags.END_OF_SIMULATION);

		// }

		for (Vm vm : getVmList()) {
			finilizeVM(vm);
		}
		clearDatacenters();

		Log.printLine(CloudSim.clock() + ": " + getName() + " is shutting down...");

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
			send(getId(), delay, CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT, cloudlets);
		} else {
			presetEvent(getId(), CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT, cloudlets, delay);
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
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletList
	 *            the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId
	 *            ID of the cloudlet being bount to a vm
	 * @param vmId
	 *            the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletSubmittedList
	 *            the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletReceivedList
	 *            the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
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
		// int clId = cloudlet.getCloudletId();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all
																		// cloudlets
																		// executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			// print cloudlets results
			System.out.println("=============> User " + getUserId() + "    ");
			System.out.println("=============> Service " + getName() + "    ");
			BaseDatacenter.printCloudletList(getCloudletReceivedList());
			// Notify Broker that our Cloudlet are done!
			sendNow(getUserId(), CloudSimTagsExt.SERVICE_CLOUDLET_DONE);
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created

				System.out.println("Cloudlets waiting for VM creation!");
				// Notify Broker that our Cloudlet are done! but some bount
				// cloudlet is waiting its VM be created
				sendNow(getId(), CloudSimTagsExt.SERVICE_CLOUDLET_DONE_VM);
			}

		}
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		if (!cloudletGenerated) {
			generateCloudlets();
		}
		ArrayList<Cloudlet> toRemove = new ArrayList<>();
		for (int i = 0; i < getCloudletList().size(); i++) {
			Cloudlet cloudlet = getCloudletList().get(i);
			int vmId = cloudlet.getVmId();

			if (VmList.getById(getVmsCreatedList(), cloudlet.getVmId()) == null) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
						+ cloudlet.getCloudletId() + ": bount VM #" + cloudlet.getVmId() + " not available");
				continue;
			}
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet #" + cloudlet.getCloudletId()
					+ " to VM #" + vmId);
			Log.printLine(CloudSim.clock() + ": " + getName() + "[DEBUG]: cloudlet #" + cloudlet.getCloudletId()
					+ " has " + ((NetworkCloudlet) cloudlet).getNumStage() + " stages");
			sendNow(getVmsToDatacentersMap().get(vmId), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			getCloudletSubmittedList().add(cloudlet);
			// remove submitted cloudlets from waiting list
			toRemove.add(cloudlet);
		}
		getCloudletList().removeAll(toRemove);
		if (getCloudletList().size() == 0) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": sending  SERVICE_ALL_CLOUDLETS_SENT to broker");
			sendNow(getUserId(), CloudSimTagsExt.SERVICE_ALL_CLOUDLETS_SENT);
		}

		// remove submitted cloudlets from waiting list
		// moved up in the loop to make sure that only the submitted cloudlets
		// are removed from the list
		// for (Cloudlet cloudlet : getCloudletSubmittedList()) {
		// getCloudletList().remove(cloudlet);
		// }
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlet(Cloudlet cloudlet) {
		int vmIndex = 0;
		Vm vm;
		// if user didn't bind this cloudlet and it has not been executed
		// yet
		if (cloudlet.getVmId() == -1) {
			vm = getVmsCreatedList().get(vmIndex);
		} else { // submit to the specific vm
			vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
			if (vm == null) { // vm was not created
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
						+ cloudlet.getCloudletId() + ": bount VM #" + cloudlet.getVmId() + " not available");
			}
		}

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet #" + cloudlet.getCloudletId()
				+ " to VM #" + vm.getId());
		cloudlet.setVmId(vm.getId());
		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
		cloudletsSubmitted++;
		vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
		getCloudletSubmittedList().add(cloudlet);

		// remove submitted cloudlets from waiting list
		getCloudletList().remove(cloudlet);
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap
	 *            the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	protected void addCloudlet(Cloudlet cl) {
		if (CloudletList.getById(getCloudletList(), cl.getCloudletId()) == null) {
			getCloudletList().add(cl);
		}
	}

	/**
	 * @return the firstCloudlet
	 */
	public NetworkCloudlet getFirstCloudlet() {
		return firstCloudlet;
	}

	/**
	 * @param firstCloudlet
	 *            the firstCloudlet to set
	 */
	public void setFirstCloudlet(NetworkCloudlet firstCloudlet) {
		this.firstCloudlet = firstCloudlet;
	}

	/**
	 * @return the brokerCloudletId
	 */
	public int getBrokerCloudletId() {
		return brokerCloudletId;
	}

	/**
	 * @param brokerCloudletId
	 *            the brokerCloudletId to set
	 */
	public void setBrokerCloudletId(int brokerCloudletId) {
		this.brokerCloudletId = brokerCloudletId;
	}

	/**
	 * @return the firstVmId
	 */
	public int getFirstVmId() {
		return firstVmId;
	}

	/**
	 * @param firstVmId
	 *            the firstVmId to set
	 */
	public void setFirstVmId(int firstVmId) {
		this.firstVmId = firstVmId;
	}

	/**
	 * @return the brokerVmId
	 */
	public int getBrokerVmId() {
		return brokerVmId;
	}

	/**
	 * @param brokerVmId
	 *            the brokerVmId to set
	 */
	public void setBrokerVmId(int brokerVmId) {
		this.brokerVmId = brokerVmId;
	}

	protected abstract void generateCloudlets();

	protected abstract void createStages();

	public abstract void assignVmToCloudlets();

}
