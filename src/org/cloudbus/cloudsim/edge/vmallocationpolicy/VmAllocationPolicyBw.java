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
 * jdhfdjhfdf
 * 
 * @author Brice Kamneng Kwam
 * based on NetworkCloudSim x.x
 * 
 * VmAllocationPolicySimple is an VmAllocationPolicy that chooses, as the host for a VM, the host
 * with less bw in use.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class VmAllocationPolicyBw extends VmAllocationPolicy {

	/** The vm table. */
	private Map<String, Host> vmTable;

	/** The used bw. */
	private Map<String, Long> usedBw;

	/** The free bw. */
	private List<Long> freeBw;

	/**
	 * Creates the new VmAllocationPolicySimple object.
	 * 
	 * @param list the list
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicyBw(List<? extends Host> list) {
		super(list);

		setFreeBw(new ArrayList<Long>());
		for (Host host : getHostList()) {
			getFreeBw().add(host.getBw());

		}

		setVmTable(new HashMap<String, Host>());
		setUsedBw(new HashMap<String, Long>());
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
		long requiredBw = vm.getSize();
		boolean result = false;
		int tries = 0;
		List<Long> freeBwTmp = new ArrayList<Long>();
		for (long freeBw : getFreeBw()) {
			freeBwTmp.add(freeBw);
		}

		if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
			do {// we still trying until we find a host or until we try all of them
				long moreFree = Long.MIN_VALUE;
				int idx = -1;

				// we want the host with less bw in use
				for (int i = 0; i < freeBwTmp.size(); i++) {
					if (freeBwTmp.get(i) > moreFree) {
						moreFree = freeBwTmp.get(i);
						idx = i;
					}
				}

				Host host = getHostList().get(idx);
				result = host.vmCreate(vm);

				if (result) { // if vm were succesfully created in the host
					getVmTable().put(vm.getUid(), host);
					getUsedBw().put(vm.getUid(), requiredBw);
					getFreeBw().set(idx, getFreeBw().get(idx) - requiredBw);
					result = true;
					break;
				} else {
					freeBwTmp.set(idx, Long.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreeBw().size());

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
		
		
		
		long bw = getUsedBw().remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			getFreeBw().set(idx, getFreeBw().get(idx) + bw);
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
	 * Gets the used bw.
	 * 
	 * @return the used bw
	 */
	protected Map<String, Long> getUsedBw() {
		return usedBw;
	}

	/**
	 * Sets the used bw.
	 * 
	 * @param usedBw the used bw
	 */
	protected void setUsedBw(Map<String, Long> usedBw) {
		this.usedBw = usedBw;
	}

	/**
	 * Gets the free bw.
	 * 
	 * @return the free bw
	 */
	protected List<Long> getFreeBw() {
		return freeBw;
	}

	/**
	 * Sets the free bw.
	 * 
	 * @param freeBw the new free bw
	 */
	protected void setFreeBw(List<Long> freeBw) {
		this.freeBw = freeBw;
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

			long requiredBw = vm.getSize();
			int idx = getHostList().indexOf(host);
			getUsedBw().put(vm.getUid(), requiredBw);
			getFreeBw().set(idx, getFreeBw().get(idx) - requiredBw);

			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}

		return false;
	}
}
