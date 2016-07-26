package org.cloudbus.cloudsim.ext.service;

import java.util.ArrayList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.TaskStage;

public class EdgeDbService extends EdgeService {

	public EdgeDbService(String name, double lifeLength) {
		super("EdgeDbService-" + name, lifeLength);
		// TODO Auto-generated constructor stub
	}

	public EdgeDbService(String name) {
		super("EdgeDbService-" + name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected NetworkCloudlet createCloudlet() {
		// TODO Auto-generated method stub
		// Cloudlet properties
		long length = 4000000;
		long memory = 100;
		long fileSize = 1000;
		long outputSize = 1000;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		int pesNumber = 2; // number of cpus
		return new NetworkCloudlet(length, pesNumber, fileSize, outputSize, memory, utilizationModel, utilizationModel,
				utilizationModel, getUserId(), getId());
	}

	public void createStages() {
		assignVmToCloudlets();
		ArrayList<Cloudlet> cList = (ArrayList<Cloudlet>) getCloudletList();
		for (int i = 0; i < cList.size(); i++) {
			NetworkCloudlet cl = (NetworkCloudlet) cList.get(i);
			if (i == 0) {
				cl.setNumStage(2);
				cl.setSubmittime(CloudSim.clock());
				cl.setCurrStagenum(-1);
				cl.getStages().add(new TaskStage(NetworkConstants.EXECUTION, 0, 1000 * 0.8, 0, cl.getMemory(),
						cl.getVmId(), cl.getCloudletId()));
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 1000, 0, 1, cl.getMemory(), cList.get(2).getVmId(),
						cList.get(2).getCloudletId()));
			}
			if (i == 1) {
				cl.setNumStage(2);
				cl.setSubmittime(CloudSim.clock());
				cl.setCurrStagenum(-1);
				cl.getStages().add(new TaskStage(NetworkConstants.EXECUTION, 0, 1000 * 0.8, 0, cl.getMemory(),
						cl.getVmId(), cl.getCloudletId()));
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 1000, 0, 1, cl.getMemory(), cList.get(2).getVmId(),
						cList.get(2).getCloudletId()));
			}
			if (i == 2) {
				cl.setNumStage(3);
				cl.setSubmittime(CloudSim.clock());
				cl.setCurrStagenum(-1);
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 1000, 0, 0, cl.getMemory(), cList.get(0).getVmId(),
						cList.get(0).getCloudletId()));
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 1000, 0, 1, cl.getMemory(), cList.get(1).getVmId(),
						cList.get(1).getCloudletId()));
				cl.getStages().add(new TaskStage(NetworkConstants.EXECUTION, 0, 1000 * 0.8, 1, cl.getMemory(),
						cl.getVmId(), cl.getCloudletId()));
			}

		}
	}

}
