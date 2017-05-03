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
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * VmAllocationPolicyStorage is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less bw in use.
 * 
 * @author Brice Kamneng Kwam
 * based on CloudSim 3.0.3
 * 
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less Storage in use.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmAllocationPolicyStorage extends VmAllocationPolicy {

	/** The vm table. */
	private Map<String, Host> vmTable;

	/** The used storage. */
	private Map<String, Long> usedStorage;

	/** The free storage. */
	private List<Long> freeStorage;

	/**
	 * Creates the new VmAllocationPolicySimple object.
	 * 
	 * @param list the list
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicyStorage(List<? extends Host> list) {
		super(list);

		setFreeStorage(new ArrayList<Long>());
		for (Host host : getHostList()) {
			getFreeStorage().add(host.getStorage());

		}

		setVmTable(new HashMap<String, Host>());
		setUsedStorage(new HashMap<String, Long>());
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
		long requiredStorage = vm.getSize();
		boolean result = false;
		int tries = 0;
		List<Long> freeStorageTmp = new ArrayList<Long>();
		for (long freeStorage : getFreeStorage()) {
			freeStorageTmp.add(freeStorage);
		}

		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				long moreFree = Long.MIN_VALUE;
				int idx = -1;

				// we want the host with less storage in use
				for (int i = 0; i < freeStorageTmp.size(); i++) {
					if (freeStorageTmp.get(i) > moreFree) {
						moreFree = freeStorageTmp.get(i);
						idx = i;
					}
				}

				Host host = getHostList().get(idx);
				result = host.vmCreate(vm);

				if (result) { // if vm were succesfully created in the host
					getVmTable().put(vm.getUid(), host);
					getUsedStorage().put(vm.getUid(), requiredStorage);
					getFreeStorage().set(idx, getFreeStorage().get(idx) - requiredStorage);
					result = true;
					break;
				} else {
					freeStorageTmp.set(idx, Long.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreeStorage().size());

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
		
		
		
		long storage = getUsedStorage().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			getFreeStorage().set(idx, getFreeStorage().get(idx) + storage);
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
	 * Gets the used storage.
	 * 
	 * @return the used storage
	 */
	protected Map<String, Long> getUsedStorage() {
		return usedStorage;
	}

	/**
	 * Sets the used storage.
	 * 
	 * @param usedStorage the used storage
	 */
	protected void setUsedStorage(Map<String, Long> usedStorage) {
		this.usedStorage = usedStorage;
	}

	/**
	 * Gets the free storage.
	 * 
	 * @return the free storage
	 */
	protected List<Long> getFreeStorage() {
		return freeStorage;
	}

	/**
	 * Sets the free storage.
	 * 
	 * @param freeStorage the new free storage
	 */
	protected void setFreeStorage(List<Long> freeStorage) {
		this.freeStorage = freeStorage;
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

			long requiredStorage = vm.getSize();
			int idx = getHostList().indexOf(host);
			getUsedStorage().put(vm.getUid(), requiredStorage);
			getFreeStorage().set(idx, getFreeStorage().get(idx) - requiredStorage);

			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}

		return false;
	}
}
