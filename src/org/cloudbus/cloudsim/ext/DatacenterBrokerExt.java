package org.cloudbus.cloudsim.ext;
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
import java.util.Objects;
import java.util.logging.Level;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.ext.util.CustomLog;
import org.cloudbus.cloudsim.ext.vm.VMStatus;
import org.cloudbus.cloudsim.ext.vm.VMex;
import org.cloudbus.cloudsim.ext.Service;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, sumbission of cloudlets to this VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author nikolay.grozev
 * @author Brice Kamneng Kwam
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBrokerExt extends SimEntity {

	/** The vm list. */
	protected List<? extends Vm> vmList;
	
	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

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

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;
	
	//==============================EXT========================================
	/** The service list. */
	protected List<? extends Service> serviceList;
	
	/** Number of VM destructions requested. */
    private int vmDestructsRequested = 0;

    /** Number of VM destructions acknowledged. */
    private int vmDestructsAcks = 0;

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
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBrokerExt(String name, final double lifeLength) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
		this.lifeLength = lifeLength;
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}
	
    


	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
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

	            Vm vm = VmList.getById(getVmList(), vmId);
	            if (vm.isBeingInstantiated()) {
	                vm.setBeingInstantiated(false);
	            }
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
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
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
	 * @param ev a SimEvent object
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
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

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
        case CloudSimTagsExt.BROKER_DESTROY_VMS_NOW:
            destroyVMList((List<Vm>) ev.getData());
            break;
        case CloudSimTagsExt.BROKER_SUBMIT_VMS_NOW:
            submitVmList((List<Vm>) ev.getData());
            // TODO Is the following valid when multiple data centres are
            // handled with a single broker?
            for (int nextDatacenterId : getDatacenterIdsList()) {
                createVmsInDatacenter(nextDatacenterId);
            }
            break;
        case CloudSimTagsExt.BROKER_CLOUDLETS_NOW:
            submitCloudletList((List<Cloudlet>) ev.getData());
            submitCloudlets();
            break;
        case CloudSimTagsExt.BROKER_DESTROY_ITSELF_NOW:
            closeDownBroker();
            break;
        default:
            Log.printLine(getName() + ".processOtherEvent(): "
            		+ "Error - event unknown by this DatacenterBroker.");
            break;
        }
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		int vmIndex = 0;
		for (Cloudlet cloudlet : getCloudletList()) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
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

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
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
	 * @param vmsRequested the new vms requested
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
	 * @param vmsAcks the new vms acks
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
	 * @param vmsDestroyed the new vms destroyed
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
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
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
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
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
	 * @param datacenterCharacteristicsList the datacenter characteristics list
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
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}
	
	
	//==============================EXT========================================
	
	/**
	 * @return the serviceList
	 */
	public List<? extends Service> getServiceList() {
		return serviceList;
	}

	/**
	 * @param serviceList the serviceList to set
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
    
    
    private void finilizeVM(final Vm vm) {
        if (vm instanceof VMex) {
            VMex vmEX = ((VMex) vm);
            if (vmEX.getStatus() != VMStatus.TERMINATED) {
                vmEX.setStatus(VMStatus.TERMINATED);
            }
        }
    }
    
    
    /**
     * Terminates the broker, releases all its resources and state.
     */
    public void closeDownBroker() {
        for (Vm vm : getVmList()) {
            finilizeVM(vm);
        }
        clearDatacenters();
        finishExecution();
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
     * Submits the list of vms after a given delay
     * 
     * @param list
     * @param delay
     */
    public void createVmsAfter(List<? extends Vm> vms, double delay) {
        if (started) {
            send(getId(), delay, CloudSimTagsExt.BROKER_SUBMIT_VMS_NOW, vms);
        } else {
            presetEvent(getId(), CloudSimTagsExt.BROKER_SUBMIT_VMS_NOW, vms, delay);
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
            send(getId(), delay, CloudSimTagsExt.BROKER_DESTROY_VMS_NOW, vms);
        } else {
            presetEvent(getId(), CloudSimTagsExt.BROKER_DESTROY_VMS_NOW, vms, delay);
        }
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

    
    private void processVMDestroy(SimEvent ev) {
        int[] data = (int[]) ev.getData();
//        int datacenterId = data[0];
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
//            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": VM #", vmId,
//                    " has been destroyed in Datacenter #", datacenterId);
        } else {
            // Use the standard log for consistency ....
//            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Desctuction of VM #", vmId,
//                    " failed in Datacenter #", datacenterId);
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
//            String datacenterName = vm.getHost().getDatacenter().getName();

//            Log.printConcatLine(CloudSim.clock(), ": ", getName(), ": Trying to Destroy VM #", vm.getId(), " in ",
//                    datacenterName);

            // Tell the data centre to destroy it
            sendNow(datacenterId, CloudSimTags.VM_DESTROY_ACK, vm);
            requestedVmTerminations++;
        }

        setVmsDestroyed(requestedVmTerminations);
        setVmDestructsAcks(0);
    }
    
    
	
	
	

}
