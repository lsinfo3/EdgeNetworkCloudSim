package org.cloudbus.cloudsim.edge.service;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.vm.T2Large;
import org.cloudbus.cloudsim.edge.vm.T2Nano;
import org.cloudbus.cloudsim.edge.vm.T2Small;
import org.cloudbus.cloudsim.edge.vm.VmType;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.TaskStage;

public class EdgeWebService extends EdgeService {

	public EdgeWebService(String name, double lifeLength) {
		super("EdgeWebService-" + name, lifeLength);
		// TODO Auto-generated constructor stub
	}

	public EdgeWebService(String name) {
		super("EdgeWebService-" + name);
		// TODO Auto-generated constructor stub
	}

	@Override

	protected void generateCloudlets() {
		if (!isCloudletGenerated()) {
			List<Cloudlet> cList = new ArrayList<Cloudlet>();
			UtilizationModel utilizationModel = new UtilizationModelFull();

			NetworkCloudlet ncl = new NetworkCloudlet(40000, 2, 1000, 1000, 256, utilizationModel, utilizationModel,
					utilizationModel, getUserId(), getId());
			ncl.setVmType(VmType.T2SMALL);
			cList.add(ncl);
			setFirstCloudlet(ncl);

			ncl = new NetworkCloudlet(40000, 1, 1000, 1000, 256, utilizationModel, utilizationModel, utilizationModel,
					getUserId(), getId());
			ncl.setVmType(VmType.T2NANO);
			cList.add(ncl);

			ncl = new NetworkCloudlet(40000, 4, 1000, 1000, 256, utilizationModel, utilizationModel, utilizationModel,
					getUserId(), getId());
			ncl.setVmType(VmType.T2Large);
			cList.add(ncl);

			setCloudletList(cList);
			createStages();
			setCloudletGenerated(true);

		}
	}

	/**
	 * This method is used to send to the broker the list with virtual machines
	 * that must be created.
	 * 
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList() {
		getVmList().add(new T2Small());
		getVmList().add(new T2Nano());
		getVmList().add(new T2Large());
		for (Vm vm : getVmList()) {
			vm.setUserId(this.getId());
		}
	}

	public void createStages() {
		assignVmToCloudlets();
		ArrayList<Cloudlet> cList = (ArrayList<Cloudlet>) getCloudletList();
		for (int i = 0; i < cList.size(); i++) {
			NetworkCloudlet cl = (NetworkCloudlet) cList.get(i);
			if (i == 0) {
				cl.setNumStage(5);
				cl.setSubmittime(CloudSim.clock());
				cl.setCurrStagenum(-1);
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, generateRandomData(), 0, 2, cl.getMemory(), getBrokerVmId(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 10000, 0, 2, cl.getMemory(), getBrokerVmId(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 20000, 0, 2, cl.getMemory(), getBrokerVmId(),
						getBrokerCloudletId()));

				cl.getStages().add(new TaskStage(NetworkConstants.EXECUTION, 0, 10240 * 0.8, 0, cl.getMemory(),
						cl.getVmId(), cl.getCloudletId()));
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, generateRandomData(), 0, 1, cl.getMemory(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 10000, 0, 1, cl.getMemory(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 20000, 0, 1, cl.getMemory(),
						cList.get(1).getVmId(), cList.get(1).getCloudletId()));
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, generateRandomData(), 0, 2, cl.getMemory(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 10000, 0, 2, cl.getMemory(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 20000, 0, 2, cl.getMemory(),
						cList.get(1).getVmId(), cList.get(1).getCloudletId()));

//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, generateRandomData(), 0, 2, cl.getMemory(), getBrokerVmId(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 10000, 0, 2, cl.getMemory(), getBrokerVmId(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 20000, 0, 2, cl.getMemory(), getBrokerVmId(),
						getBrokerCloudletId()));
			}
			if (i == 1) {
				cl.setNumStage(5);
				cl.setSubmittime(CloudSim.clock());
				cl.setCurrStagenum(-1);
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, generateRandomData(), 0, 0, cl.getMemory(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 10000, 0, 0, cl.getMemory(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 20000, 0, 0, cl.getMemory(),
						cList.get(0).getVmId(), cList.get(0).getCloudletId()));
				cl.getStages().add(new TaskStage(NetworkConstants.EXECUTION, 0, 10240 * 0.8, 1, cl.getMemory(),
						cl.getVmId(), cl.getCloudletId()));
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, generateRandomData(), 0, 2, cl.getMemory(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 10000, 0, 2, cl.getMemory(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 20000, 0, 2, cl.getMemory(),
						cList.get(2).getVmId(), cList.get(2).getCloudletId()));
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, generateRandomData(), 0, 3, cl.getMemory(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 10000, 0, 3, cl.getMemory(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 20000, 0, 3, cl.getMemory(),
						cList.get(2).getVmId(), cList.get(2).getCloudletId()));
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, generateRandomData(), 0, 4, cl.getMemory(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 10000, 0, 4, cl.getMemory(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 20000, 0, 4, cl.getMemory(),
						cList.get(0).getVmId(), cList.get(0).getCloudletId()));
			}
			if (i == 2) {
				cl.setNumStage(3);
				cl.setSubmittime(CloudSim.clock());
				cl.setCurrStagenum(-1);
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, generateRandomData(), 0, 0, cl.getMemory(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 10000, 0, 0, cl.getMemory(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, 20000, 0, 0, cl.getMemory(),
						cList.get(1).getVmId(), cList.get(1).getCloudletId()));
				cl.getStages().add(new TaskStage(NetworkConstants.EXECUTION, 0, 10240 * 0.8, 1, cl.getMemory(),
						cl.getVmId(), cl.getCloudletId()));
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, generateRandomData(), 0, 2, cl.getMemory(),
				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 10000, 0, 2, cl.getMemory(),
//				cl.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, 20000, 0, 2, cl.getMemory(),
						cList.get(1).getVmId(), cList.get(1).getCloudletId()));
			}

		}
	}

}
