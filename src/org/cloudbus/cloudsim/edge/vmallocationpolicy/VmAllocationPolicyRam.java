/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.edge.vmallocationpolicy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * 
 * VmAllocationPolicyRam is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less RAM in use.
 * 
 * @author Brice Kamneng Kwam
 * based on CloudSim 3.0.3
 * 
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less RAM in use.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmAllocationPolicyRam extends VmAllocationPolicy {

	/** The vm table. */
	private Map<String, Host> vmTable;

	/** The used ram. */
	private Map<String, Integer> usedRam;

	/** The free ram. */
	private List<Integer> freeRam;

	/**
	 * Creates the new VmAllocationPolicySimple object.
	 * 
	 * @param list the list
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicyRam(List<? extends Host> list) {
		super(list);

		setFreeRam(new ArrayList<Integer>());
		for (Host host : getHostList()) {
			getFreeRam().add(host.getRam());

		}

		setVmTable(new HashMap<String, Host>());
		setUsedRam(new HashMap<String, Integer>());
	}

	/**
	 * Allocates a host for a given VM.
	 * 
	 * @param vm VM specification
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean allocateHostForVm(Vm vm) {
		int requiredRam = vm.getRam();
		boolean result = false;
		int tries = 0;
		List<Integer> freeRamTmp = new ArrayList<Integer>();
		for (Integer freeRam : getFreeRam()) {
			freeRamTmp.add(freeRam);
		}

		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				// we want the host with less ram in use
				for (int i = 0; i < freeRamTmp.size(); i++) {
					if (freeRamTmp.get(i) > moreFree) {
						moreFree = freeRamTmp.get(i);
						idx = i;
					}
				}

				Host host = getHostList().get(idx);
				result = host.vmCreate(vm);

				if (result) { // if vm were succesfully created in the host
					getVmTable().put(vm.getUid(), host);
					getUsedRam().put(vm.getUid(), requiredRam);
					getFreeRam().set(idx, getFreeRam().get(idx) - requiredRam);
					result = true;
					break;
				} else {
					freeRamTmp.set(idx, Integer.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreeRam().size());

		}

		return result;
	}

	/**
	 * Releases the host used by a VM.
	 * 
	 * @param vm the vm
	 * @pre $none
	 * @post none
	 */
	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
		int idx = getHostList().indexOf(host);
		int ram = getUsedRam().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			getFreeRam().set(idx, getFreeRam().get(idx) + ram);
		}
	}

	/**
	 * Gets the host that is executing the given VM belonging to the given user.
	 * 
	 * @param vm the vm
	 * @return the Host with the given vmID and userID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	/**
	 * Gets the host that is executing the given VM belonging to the given user.
	 * 
	 * @param vmId the vm id
	 * @param userId the user id
	 * @return the Host with the given vmID and userID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Gets the vm table.
	 * 
	 * @return the vm table
	 */
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 * 
	 * @param vmTable the vm table
	 */
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

	/**
	 * Gets the used ram.
	 * 
	 * @return the used ram
	 */
	protected Map<String, Integer> getUsedRam() {
		return usedRam;
	}

	/**
	 * Sets the used ram.
	 * 
	 * @param usedRam the used ram
	 */
	protected void setUsedRam(Map<String, Integer> usedRam) {
		this.usedRam = usedRam;
	}

	/**
	 * Gets the free ram.
	 * 
	 * @return the free ram
	 */
	protected List<Integer> getFreeRam() {
		return freeRam;
	}

	/**
	 * Sets the free ram.
	 * 
	 * @param freeRam the new free ram
	 */
	protected void setFreeRam(List<Integer> freeRam) {
		this.freeRam = freeRam;
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.VmAllocationPolicy#optimizeAllocation(double, cloudsim.VmList, double)
	 */
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm,
	 * org.cloudbus.cloudsim.Host)
	 */
	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);

			int requiredRam = vm.getRam();
			int idx = getHostList().indexOf(host);
			getUsedRam().put(vm.getUid(), requiredRam);
			getFreeRam().set(idx, getFreeRam().get(idx) - requiredRam);

			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}

		return false;
	}
}
