package org.cloudbus.cloudsim.ext.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.ext.CloudSimTagsExt;
import org.cloudbus.cloudsim.ext.DatacenterBrokerExt;
import org.cloudbus.cloudsim.ext.Message;
import org.cloudbus.cloudsim.ext.PresetEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

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

	/**
	 * the number of cloudlet this Service genertes and works with.
	 */
	private final int cloudletNum = 5;

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
		generateCloudlets();
	}

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
	 * @see org.cloudbus.cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
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
		if (!started) {
			started = true;

			for (ListIterator<PresetEvent> iter = presetEvents.listIterator(); iter
					.hasNext();) {
				PresetEvent event = iter.next();
				send(event.getId(), event.getDelay(), event.getTag(),
						event.getData());
				iter.remove();
			}

			// Tell the Service to destroy itself after its lifeline.
			if (getLifeLength() > 0) {
				send(getId(), getLifeLength(),
						CloudSimTagsExt.SERVICE_DESTROY_ITSELF_NOW, null);
			}
		}
		switch (ev.getTag()) {
		// start submitting Cloudlets
		case CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT:
			submitCloudlets();
			break;
		// A finished cloudlet returned
		case CloudSimTags.CLOUDLET_RETURN:
			processCloudletReturn(ev);
			break;
		// A finished cloudlet returned
		case CloudSimTags.CLOUDLET_PAUSE_ACK:
			processCloudletPausedAck(ev);
			break;
		// The Broker has send its VM mapping
		case CloudSimTagsExt.VM_DC_MAPPING:
			processVmDcMapping(ev);
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
			Log.printLine(getName() + ".processOtherEvent(): "
					+ "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * process a Message sent by the broker/user.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	@SuppressWarnings("unchecked")
	protected void processVmDcMapping(SimEvent ev) {
		//
		setVmsToDatacentersMap((Map<Integer, Integer>) ev.getData());
	}

	/**
	 * process a Message sent by the broker/user.
	 * 
	 * @param ev
	 *            a SimEvent object
	 */
	protected void processBrokerMessage(SimEvent ev) {

		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) {
			// all cloudlets executed, generate new ones.
			generateCloudlets();
			for (Cloudlet cloudlet : getCloudletList()) {
				cloudlet.setCloudletLength(cloudlet.getCloudletLength()
						+ ((Message) ev.getData()).getMips());
			}
			submitCloudlets();
			return;
		} else if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
			// all the cloudlets sent finished. It means that some bount
			// cloudlet is waiting its VM be created

		}

		for (Cloudlet cloudlet : getCloudletList()) {
			if (!cloudlet.isFinished()) {
				sendNow(getVmsToDatacentersMap().get(cloudlet.getVmId()),
						CloudSimTags.CLOUDLET_PAUSE_ACK, cloudlet);
				cloudletIdToMessage.put(cloudlet.getCloudletId(),
						(Message) ev.getData());
			} else {
				cloudlet = createCloudlet();
				cloudlet.setCloudletLength(cloudlet.getCloudletLength()
						+ ((Message) ev.getData()).getMips());
				submitCloudlet(cloudlet);
			}
		}
	}

	protected void processCloudletPausedAck(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int cloudletId = data[1];
		if (cloudletIdToMessage.containsKey(cloudletId)) {
			Cloudlet cloudlet = CloudletList.getById(getCloudletList(),
					cloudletId);
			Message msg = cloudletIdToMessage.get(cloudletId);
			// get the previous length
			// cut the size that has already been processed
			// add the one from the Message
			cloudlet.setCloudletLength((cloudlet.getCloudletLength() - cloudlet
					.getCloudletFinishedSoFar()) + msg.getMips()); // not sure
																	// how to
																	// calculate
																	// this yet.
			// resume the Cloudlet
			sendNow(getVmsToDatacentersMap().get(cloudlet.getVmId()),
					CloudSimTags.CLOUDLET_RESUME, cloudlet);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");

	}

	@Override
	public String toString() {
		return String.valueOf(String.format("Broker(%s, %d)",
				Objects.toString(getName(), "N/A"), getId()));
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
	public void presetEvent(final int id, final int tag, final Object data,
			final double delay) {
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
			send(getId(), delay, CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT,
					cloudlets);
		} else {
			presetEvent(getId(), CloudSimTagsExt.CLOUDLET_SERVICE_SUBMIT,
					cloudlets, delay);
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
	 * @return the cloudletNum
	 */
	public int getCloudletNum() {
		return cloudletNum;
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
	protected <T extends Cloudlet> void setCloudletSubmittedList(
			List<T> cloudletSubmittedList) {
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
	protected <T extends Cloudlet> void setCloudletReceivedList(
			List<T> cloudletReceivedList) {
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
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet "
				+ cloudlet.getCloudletId() + " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all
																		// cloudlets
																		// executed
			Log.printLine(CloudSim.clock() + ": " + getName()
					+ ": All Cloudlets executed. Finishing...");
			// Notify Broker that our Cloudlet are done!
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created

				// Notify Broker that our Cloudlet are done! but some bount
				// cloudlet is waiting its VM be created
				sendNow(getUserId(), CloudSimTagsExt.SERVICE_CLOUDLET_DONE_VM);
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
		int vmIndex = 0;
		for (int i = 0; i < getCloudletList().size(); i++) {
			Cloudlet cloudlet = getCloudletList().get(i);
//		}
//		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed
			// yet
			if (cloudlet.getVmId() == -1) {
				vm = ((DatacenterBrokerExt) CloudSim.getEntity(getUserId()))
						.getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(((DatacenterBrokerExt) CloudSim
						.getEntity(getUserId())).getVmsCreatedList(), cloudlet
						.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId()
							+ ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName()
					+ ": Sending cloudlet " + cloudlet.getCloudletId()
					+ " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()),
					CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1)
					% ((DatacenterBrokerExt) CloudSim.getEntity(getUserId()))
							.getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
			// remove submitted cloudlets from waiting list
			getCloudletList().remove(cloudlet);
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
			vm = ((DatacenterBrokerExt) CloudSim.getEntity(getUserId()))
					.getVmsCreatedList().get(vmIndex);
		} else { // submit to the specific vm
			vm = VmList.getById(((DatacenterBrokerExt) CloudSim
					.getEntity(getUserId())).getVmsCreatedList(), cloudlet
					.getVmId());
			if (vm == null) { // vm was not created
				Log.printLine(CloudSim.clock() + ": " + getName()
						+ ": Postponing execution of cloudlet "
						+ cloudlet.getCloudletId() + ": bount VM not available");
			}
		}

		Log.printLine(CloudSim.clock() + ": " + getName()
				+ ": Sending cloudlet " + cloudlet.getCloudletId() + " to VM #"
				+ vm.getId());
		cloudlet.setVmId(vm.getId());
		sendNow(getVmsToDatacentersMap().get(vm.getId()),
				CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
		cloudletsSubmitted++;
		vmIndex = (vmIndex + 1)
				% ((DatacenterBrokerExt) CloudSim.getEntity(getUserId()))
						.getVmsCreatedList().size();
		getCloudletSubmittedList().add(cloudlet);

		// remove submitted cloudlets from waiting list
		getCloudletList().remove(cloudlet);
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

	protected void generateCloudlets() {
		// TODO Auto-generated method stub
		List<Cloudlet> cList = new ArrayList<Cloudlet>();
		for (int i = 0; i < getCloudletNum(); i++) {
			cList.add(createCloudlet());
		}
		setCloudletList(cList);

	}

	protected abstract Cloudlet createCloudlet();

}
