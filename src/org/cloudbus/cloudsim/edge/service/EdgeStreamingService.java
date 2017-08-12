package org.cloudbus.cloudsim.edge.service;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.ServiceTyp;
import org.cloudbus.cloudsim.edge.util.TextUtil;
import org.cloudbus.cloudsim.edge.vm.T2Large;
import org.cloudbus.cloudsim.edge.vm.T2Nano;
import org.cloudbus.cloudsim.edge.vm.T2Small;
import org.cloudbus.cloudsim.edge.vm.VmType;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.TaskStage;

public class EdgeStreamingService extends EdgeService {

	public EdgeStreamingService(String name, double lifeLength) {
		super("EdgeWebService-" + name, lifeLength);
		 setServiceTyp(ServiceTyp.STREAMING);
	}

	public EdgeStreamingService(String name) {
		super("EdgeWebService-" + name);
		 setServiceTyp(ServiceTyp.STREAMING);
	}

	@Override

	protected void generateCloudlets() {
		if (!isCloudletGenerated()) {
			Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Service #" + getId()
					+ ": called generateCloudlets ");
			List<Cloudlet> cList = new ArrayList<Cloudlet>();
			UtilizationModel utilizationModel = new UtilizationModelFull();

			NetworkCloudlet ncl = new NetworkCloudlet(40000, 2, 1000, 1000, 256, utilizationModel, utilizationModel,
					utilizationModel, getId());
			ncl.setVmType(VmType.T2SMALL);
			cList.add(ncl);
			setFirstCloudlet(ncl);
			addCloudletIdServiceMapping(ncl.getCloudletId(), this);

			ncl = new NetworkCloudlet(40000, 1, 1000, 1000, 256, utilizationModel, utilizationModel, utilizationModel,
					getId());
			ncl.setVmType(VmType.T2NANO);
			cList.add(ncl);
			setSecondCloudlet(ncl);
			addCloudletIdServiceMapping(ncl.getCloudletId(), this);

			ncl = new NetworkCloudlet(40000, 4, 1000, 1000, 256, utilizationModel, utilizationModel, utilizationModel,
					getId());
			ncl.setVmType(VmType.T2Large);
			cList.add(ncl);
			setThirdCloudlet(ncl);
			addCloudletIdServiceMapping(ncl.getCloudletId(), this);

			setCloudletList(cList);
			createStages();
			setCloudletGenerated(true);
			System.out.println("Number of cloudlets: " + cList.size());
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
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": [INFO]: Service #" + getId()
				+ ": called createStages with " + " getCloudletReceivedList #" + getCloudletReceivedList().size()
				+ " and getCloudletSubmittedList(): " + getCloudletSubmittedList().size());
		assignVmToCloudlets();

		createStreamingStages(30, 2, 550);
	}

	/**
	 * @param videoSize
	 *            duration of the video in seconds
	 * @param chunkSize
	 *            duration of the chunks in seconds
	 * @param bitRate
	 *            bandwidth between the VMs in kBps
	 */
	public void createStreamingStages(double videoSize, double chunkSize, double bitRate) {

		double numOfChunks = videoSize / chunkSize;
		double packetsize = chunkSize * bitRate;

		int firstCloudletStageId = 0;
		int secondCloudletStageId = 0;
		int thirdCloudletStageId = 0;

		NetworkCloudlet firstCloudlet = getFirstCloudlet();
		NetworkCloudlet secondCloudlet = getSecondCloudlet();
		NetworkCloudlet thirdCloudlet = getThirdCloudlet();

		// set the number of stages based on the number of chunks
		initCloudletStage(firstCloudlet, (1 + (numOfChunks * 2) + 1));
		initCloudletStage(secondCloudlet, (numOfChunks * 6));
		initCloudletStage(thirdCloudlet, (numOfChunks * 3));

		firstCloudlet.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, packetsize, 0, firstCloudletStageId++,
				firstCloudlet.getMemory(), getBrokerVmId(), getBrokerCloudletId()));
		for (int j = 0; j < numOfChunks; j++) {
			firstCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.EXECUTION, 0, 10240 * 0.8, firstCloudletStageId++,
							firstCloudlet.getMemory(), firstCloudlet.getVmId(), firstCloudlet.getCloudletId()));
			firstCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.WAIT_SEND, packetsize, 0, firstCloudletStageId++,
							firstCloudlet.getMemory(), secondCloudlet.getVmId(), secondCloudlet.getCloudletId()));

			secondCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.WAIT_RECV, packetsize, 0, secondCloudletStageId++,
							secondCloudlet.getMemory(), firstCloudlet.getVmId(), firstCloudlet.getCloudletId()));
			secondCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.EXECUTION, 0, 10240 * 0.8, secondCloudletStageId++,
							secondCloudlet.getMemory(), secondCloudlet.getVmId(), secondCloudlet.getCloudletId()));
			secondCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.WAIT_SEND, packetsize, 0, secondCloudletStageId++,
							secondCloudlet.getMemory(), thirdCloudlet.getVmId(), thirdCloudlet.getCloudletId()));

			thirdCloudlet.getStages().add(new TaskStage(NetworkConstants.WAIT_RECV, packetsize, 0, thirdCloudletStageId,
					thirdCloudlet.getMemory(), secondCloudlet.getVmId(), getSecondCloudlet().getCloudletId()));
			thirdCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.EXECUTION, 0, 10240 * 0.8, thirdCloudletStageId,
							thirdCloudlet.getMemory(), thirdCloudlet.getVmId(), thirdCloudlet.getCloudletId()));
			thirdCloudlet.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, packetsize, 0, thirdCloudletStageId,
					thirdCloudlet.getMemory(), secondCloudlet.getVmId(), secondCloudlet.getCloudletId()));

			secondCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.WAIT_RECV, packetsize, 0, secondCloudletStageId++,
							secondCloudlet.getMemory(), thirdCloudlet.getVmId(), thirdCloudlet.getCloudletId()));
			secondCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.EXECUTION, 0, 10240 * 0.8, secondCloudletStageId++,
							secondCloudlet.getMemory(), secondCloudlet.getVmId(), secondCloudlet.getCloudletId()));
			secondCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.WAIT_SEND, packetsize, 0, secondCloudletStageId++,
							secondCloudlet.getMemory(), firstCloudlet.getVmId(), firstCloudlet.getCloudletId()));

			firstCloudlet.getStages()
					.add(new TaskStage(NetworkConstants.WAIT_RECV, packetsize, 0, firstCloudletStageId++,
							firstCloudlet.getMemory(), secondCloudlet.getVmId(), secondCloudlet.getCloudletId()));
		}

		firstCloudlet.getStages().add(new TaskStage(NetworkConstants.WAIT_SEND, packetsize, 0, firstCloudletStageId++,
				firstCloudlet.getMemory(), getBrokerVmId(), getBrokerCloudletId()));

	}

	/**
	 * Stage list, number of stages, die submit time und die start stage id will
	 * be initialized.
	 * 
	 * @param cl
	 *            Cloudlet whose stages will be initialize.
	 * @param stageNum
	 *            number of stages of this Cloudlet
	 */
	public void initCloudletStage(NetworkCloudlet cl, double stageNum) {
		cl.setNumStage(stageNum);
		cl.setSubmittime(CloudSim.clock());
		cl.setStages(new ArrayList<TaskStage>());
		cl.setCurrStagenum(-1);
	}

	public void startEntity() {
		Log.printLine(TextUtil.toString(CloudSim.clock()) + ": Service STREAMING	 #" + getId() + " is starting...");
		super.startEntity();
	}

}
