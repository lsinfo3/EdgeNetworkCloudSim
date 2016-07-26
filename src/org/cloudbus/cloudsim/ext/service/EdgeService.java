package org.cloudbus.cloudsim.ext.service;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.ext.DatacenterBrokerExt;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;

public abstract class EdgeService extends Service {

	public EdgeService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public EdgeService(String name, double lifeLength) {
		super(name, lifeLength);
		// TODO Auto-generated constructor stub
	}
	
	public abstract void createStages();

	
	public void assignVmToCloudlets() {
		ArrayList<Cloudlet> cList = (ArrayList<Cloudlet>) getCloudletList();
		Vm vm;
		int vmIndex = 0;
		for (int i = 0; i < cList.size(); i++) {
			NetworkCloudlet cl = (NetworkCloudlet) cList.get(i);

			if (cl.getVmId() == -1) {
				vm = ((DatacenterBrokerExt) CloudSim.getEntity(getUserId())).getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(((DatacenterBrokerExt) CloudSim.getEntity(getUserId())).getVmsCreatedList(),
						cl.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cl.getCloudletId() + ": bount VM not available");
					continue;
				}
			}
			cl.setVmId(vm.getId());

			vmIndex = (vmIndex + 1)
					% ((DatacenterBrokerExt) CloudSim.getEntity(getUserId())).getVmsCreatedList().size();

		}
	}

}
