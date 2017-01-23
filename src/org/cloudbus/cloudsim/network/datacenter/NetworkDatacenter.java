/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.edge.util.TextUtil;

/**
 * NetworkDatacenter class is a Datacenter whose hostList are virtualized and
 * networked. It contains all the information about internal network. For
 * example, which VM is connected to Switch etc. It deals with processing of VM
 * queries (i.e., handling of VMs) instead of processing Cloudlet-related
 * queries. So, even though an AllocPolicy will be instantiated (in the init()
 * method of the superclass, it will not be used, as processing of cloudlets are
 * handled by the CloudletScheduler and processing of VirtualMachines are
 * handled by the VmAllocationPolicy.
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel
 * Applications in Cloud Simulations, Proceedings of the 4th IEEE/ACM
 * International Conference on Utility and Cloud Computing (UCC 2011, IEEE CS
 * Press, USA), Melbourne, Australia, December 5-7, 2011.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class NetworkDatacenter extends Datacenter {

	public Map<Integer, Integer> VmToSwitchid;

	public Map<Integer, Integer> HostToSwitchid;

	public Map<Integer, Switch> Switchlist;

	public Map<Integer, Integer> VmtoHostlist;

	/**
	 * flag to reserve this datacenter for the user VM only.
	 */
	private boolean userDC;

	/**
	 * Allocates a new NetworkDatacenter object.
	 * 
	 * @param name
	 *            the name to be associated with this entity (as required by
	 *            Sim_entity class from simjava package)
	 * @param characteristics
	 *            an object of DatacenterCharacteristics
	 * @param storageList
	 *            a LinkedList of storage elements, for data simulation
	 * @param vmAllocationPolicy
	 *            the vmAllocationPolicy
	 * 
	 * @throws Exception
	 *             This happens when one of the following scenarios occur:
	 *             <ul>
	 *             <li>creating this entity before initializing CloudSim package
	 *             <li>this entity name is <tt>null</tt> or empty
	 *             <li>this entity has <tt>zero</tt> number of PEs (Processing
	 *             Elements). <br>
	 *             No PEs mean the Cloudlets can't be processed. A CloudResource
	 *             must contain one or more Machines. A Machine must contain one
	 *             or more PEs.
	 *             </ul>
	 * 
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	public NetworkDatacenter(String name, DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval)
			throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		VmToSwitchid = new HashMap<Integer, Integer>();
		HostToSwitchid = new HashMap<Integer, Integer>();
		VmtoHostlist = new HashMap<Integer, Integer>();
		Switchlist = new HashMap<Integer, Switch>();
		this.setUserDC(false);
	}

	/**
	 * Get list of all EdgeSwitches in the Datacenter network One can design
	 * similar functions for other type of switches.
	 * 
	 */
	public Map<Integer, Switch> getEdgeSwitch() {
		Map<Integer, Switch> edgeswitch = new HashMap<Integer, Switch>();
		for (Entry<Integer, Switch> es : Switchlist.entrySet()) {
			if (es.getValue().level == NetworkConstants.EDGE_LEVEL) {
				edgeswitch.put(es.getKey(), es.getValue());
			}
		}
		return edgeswitch;

	}

	/**
	 * @return the vmtoHostlist
	 */
	public Map<Integer, Integer> getVmtoHostlist() {
		return VmtoHostlist;
	}

	/**
	 * Create the VM within the NetworkDatacenter. It can be directly accessed
	 * by Datacenter Broker which manage allocation of Cloudlets.
	 * 
	 * 
	 */
	public boolean processVmCreateNetwork(Vm vm) {

		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

		if (result) {
			VmToSwitchid.put(vm.getId(), ((NetworkHost) vm.getHost()).sw.getId());
			VmtoHostlist.put(vm.getId(), vm.getHost().getId());
			System.out.println(TextUtil.toString(CloudSim.clock()) + ": Datacenter #" + getId() + " - VM #" + vm.getId() + " is created on Host #"
					+ vm.getHost().getId());

			getVmList().add(vm);

			vm.updateVmProcessing(CloudSim.clock(),
					getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
		}
		return result;
	}

	/**
	 * Process the event for an User/Broker who wants to create a VM in this
	 * PowerDatacenter. This PowerDatacenter will then send the status back to
	 * the User/Broker.
	 * 
	 * @param ev
	 *            a Sim_event object
	 * @param ack
	 *            the ack
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processVmCreate(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		vm.setUid(vm.getUserId() + "-" + vm.getId());

		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, data);
		}

		if (result) {
			VmToSwitchid.put(vm.getId(), ((NetworkHost) vm.getHost()).sw.getId());
			VmtoHostlist.put(vm.getId(), vm.getHost().getId());
			System.out.println(TextUtil.toString(CloudSim.clock()) + ": Datacenter #" + getId() + " - VM #" + vm.getId() + " is created on Host #"
					+ vm.getHost().getId());

			getVmList().add(vm);

			if (vm.isBeingInstantiated()) {
				vm.setBeingInstantiated(false);
			}

			vm.updateVmProcessing(CloudSim.clock(),
					getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
		}
	}

	/**
	 * Processes a Cloudlet submission.
	 * 
	 * @param ev
	 *            a SimEvent object
	 * @param ack
	 *            an acknowledgement
	 * 
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		updateCloudletProcessing();

		try {
			// gets the Cloudlet object
			Cloudlet cl = (Cloudlet) ev.getData();

			// checks whether this Cloudlet has finished or not
			if (cl.isFinished()) {
				String name = CloudSim.getEntityName(cl.getUserId());
				Log.printLine(getName() + ": Warning - Cloudlet #" + cl.getCloudletId() + " owned by " + name
						+ " is already completed/finished.");
				Log.printLine("Therefore, it is not being executed again");
				Log.printLine();

				// NOTE: If a Cloudlet has finished, then it won't be processed.
				// So, if ack is required, this method sends back a result.
				// If ack is not required, this method don't send back a result.
				// Hence, this might cause CloudSim to be hanged since waiting
				// for this Cloudlet back.
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.FALSE;

					// unique tag = operation tag
					int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
					// sendNow(cl.getUserId(), tag, data);
					sendNow(cl.getServiceId(), tag, data);
				}

				// sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
				sendNow(cl.getServiceId(), CloudSimTags.CLOUDLET_RETURN, cl);

				return;
			}

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(),
					getCharacteristics().getCostPerBw());

			// int userId = cl.getUserId();
			int serviceId = cl.getServiceId();
			int vmId = cl.getVmId();

			// time to transfer the files
			double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles());

			Host host = getVmAllocationPolicy().getHost(vmId, serviceId);
			Vm vm = host.getVm(vmId, serviceId);
			CloudletScheduler scheduler = vm.getCloudletScheduler();
			double estimatedFinishTime = scheduler.cloudletSubmit(cl, fileTransferTime);

			if (estimatedFinishTime > 0.0) { // if this cloudlet is in the exec
				// time to process the cloudlet
				estimatedFinishTime += fileTransferTime;
				send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);

				// event to update the stages
				send(getId(), 0.0001, CloudSimTags.VM_DATACENTER_EVENT);
			}

			if (ack) {
				int[] data = new int[3];
				data[0] = getId();
				data[1] = cl.getCloudletId();
				data[2] = CloudSimTags.TRUE;

				// unique tag = operation tag
				int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
				// sendNow(cl.getUserId(), tag, data);
				sendNow(cl.getServiceId(), tag, data);
			}
		} catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
			c.printStackTrace();
		} catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
			e.printStackTrace();
		}

		checkCloudletCompletion();
	}

	/**
	 * Verifies if some cloudlet inside this PowerDatacenter already finished.
	 * If yes, send it to the User/Broker
	 * 
	 * @pre $none
	 * @post $none
	 */
	@Override
	protected void checkCloudletCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						// sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN,
						// cl);
						sendNow(cl.getServiceId(), CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
	}

	/**
	 * Processes a Cloudlet based on the event type.
	 * 
	 * @param ev
	 *            a Sim_event object
	 * @param type
	 *            event type
	 * @pre ev != null
	 * @pre type > 0
	 * @post $none
	 */
	@Override
	protected void processCloudlet(SimEvent ev, int type) {
		int cloudletId = 0;
		int userId = 0;
		int serviceId = 0;
		int vmId = 0;

		try { // if the sender using cloudletXXX() methods
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];
		}

		// if the sender using normal send() methods
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();
				serviceId = cl.getServiceId();
				vmId = cl.getVmId();
			} catch (Exception e) {
				Log.printLine(super.getName() + ": Error in processing Cloudlet");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printLine(super.getName() + ": Error in processing a Cloudlet.");
			Log.printLine(e.getMessage());
			return;
		}

		// begins executing ....
		switch (type) {
		case CloudSimTags.CLOUDLET_CANCEL:
			// processCloudletCancel(cloudletId, userId, vmId);
			processCloudletCancel(cloudletId, userId, serviceId, vmId);
			break;

		case CloudSimTags.CLOUDLET_PAUSE:
			// processCloudletPause(cloudletId, userId, vmId, false);
			processCloudletPause(cloudletId, userId, serviceId, vmId, false);
			break;

		case CloudSimTags.CLOUDLET_PAUSE_ACK:
			// processCloudletPause(cloudletId, userId, vmId, true);
			processCloudletPause(cloudletId, userId, serviceId, vmId, true);
			break;

		case CloudSimTags.CLOUDLET_RESUME:
			// processCloudletResume(cloudletId, userId, vmId, false);
			processCloudletResume(cloudletId, userId, serviceId, vmId, false);
			break;

		case CloudSimTags.CLOUDLET_RESUME_ACK:
			// processCloudletResume(cloudletId, userId, vmId, true);
			processCloudletResume(cloudletId, userId, serviceId, vmId, true);
			break;
		default:
			break;
		}

	}

	/**
	 * Processes a Cloudlet cancel request.
	 * 
	 * @param cloudletId
	 *            resuming cloudlet ID
	 * @param userId
	 *            ID of the cloudlet's owner
	 * @param serviceId
	 *            ID of the cloudlet's service
	 * @param vmId
	 *            the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletCancel(int cloudletId, int userId, int serviceId, int vmId) {
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId, userId).getCloudletScheduler()
				.cloudletCancel(cloudletId);
		sendNow(serviceId, CloudSimTags.CLOUDLET_CANCEL, cl);
	}

	/**
	 * Processes a Cloudlet pause request.
	 * 
	 * @param cloudletId
	 *            resuming cloudlet ID
	 * @param userId
	 *            ID of the cloudlet's Broker
	 * @param serviceId
	 *            ID of the cloudlet's Service
	 * @param ack
	 *            $true if an ack is requested after operation
	 * @param vmId
	 *            the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletPause(int cloudletId, int userId, int serviceId, int vmId, boolean ack) {
		boolean status = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId, userId).getCloudletScheduler()
				.cloudletPause(cloudletId);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(serviceId, CloudSimTags.CLOUDLET_PAUSE_ACK, data);
		}
	}

	/**
	 * Processes a Cloudlet resume request.
	 * 
	 * @param cloudletId
	 *            resuming cloudlet ID
	 * @param userId
	 *            ID of the cloudlet's owner
	 * @param serviceId
	 *            ID of the cloudlet's owner
	 * @param ack
	 *            $true if an ack is requested after operation
	 * @param vmId
	 *            the vm id
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletResume(int cloudletId, int userId, int serviceId, int vmId, boolean ack) {
		double eventTime = getVmAllocationPolicy().getHost(vmId, userId).getVm(vmId, userId).getCloudletScheduler()
				.cloudletResume(cloudletId);

		boolean status = false;
		if (eventTime > 0.0) { // if this cloudlet is in the exec queue
			status = true;
			if (eventTime > CloudSim.clock()) {
				schedule(getId(), eventTime, CloudSimTags.VM_DATACENTER_EVENT);
			}
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(serviceId, CloudSimTags.CLOUDLET_RESUME_ACK, data);
		}
	}

	@Override
	public String toString() {
		return String.format("DC(%s,%d)", Objects.toString(getName(), "N/A"), getId());
	}

	/**
	 * @return the userDC
	 */
	public boolean isUserDC() {
		return userDC;
	}

	/**
	 * @param userDC
	 *            the userDC to set
	 */
	public void setUserDC(boolean userDC) {
		this.userDC = userDC;
	}
}
