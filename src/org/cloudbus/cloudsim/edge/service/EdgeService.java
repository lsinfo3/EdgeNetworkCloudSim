package org.cloudbus.cloudsim.edge.service;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.vm.VmEdge;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;

public abstract class EdgeService extends Service {

	/**
	 * List of already assigned (a Cloudlet on it) VMs.
	 */
	List<? extends Vm> assignedVm;
	
	
	public EdgeService(String name) {
		super(name);
		assignedVm = new ArrayList<Vm>();
	}

	public EdgeService(String name, double lifeLength) {
		super(name, lifeLength);
		assignedVm = new ArrayList<Vm>();
	}

	/**
	 * @return the assignedVm
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getAssignedVm() {
		return (List<T>) assignedVm;
	}

	/**
	 * @param assignedVm the assignedVm to set
	 */
	public void setAssignedVm(List<Vm> assignedVm) {
		this.assignedVm = assignedVm;
	}


	public void assignVmToCloudlets() {
		ArrayList<Cloudlet> cList = (ArrayList<Cloudlet>) getCloudletList();
		Vm vm;
		for (int i = 0; i < cList.size(); i++) {
			NetworkCloudlet cl = (NetworkCloudlet) cList.get(i);

			if (cl.getVmId() == -1) {
				for (Vm vm1 : getVmsCreatedList()) {
					if(((VmEdge) vm1).getType() == cl.getVmType() && !getAssignedVm().contains(vm1)){
						getAssignedVm().add(vm1);
						cl.setVmId(vm1.getId());
						break;
					}
				}
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cl.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cl.getCloudletId() + ": bount VM not available");
					continue;
				}
				cl.setVmId(vm.getId());
			}
		}
		
		setFirstVmId(getFirstCloudlet().getVmId());
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

	

}
