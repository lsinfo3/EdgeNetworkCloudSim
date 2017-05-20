/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.edge.util.Id;
import org.cloudbus.cloudsim.edge.vm.VmType;

/**
 * NetworkCloudlet class extends Cloudlet to support simulation of complex
 * applications. Each such network Cloudlet represents a task of the
 * application. Each task consists of several stages.
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel
 * Applications in Cloud Simulations, Proceedings of the 4th IEEE/ACM
 * International Conference on Utility and Cloud Computing (UCC 2011, IEEE CS
 * Press, USA), Melbourne, Australia, December 5-7, 2011.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 */
public class NetworkCloudlet extends Cloudlet implements Comparable<Object> {

	private long memory;

	private VmType vmType;
	
	/**
	 * helps to compute the service time.
	 */
	private double serviceTime = 0.0;

	public NetworkCloudlet(int cloudletId, long cloudletLength, int pesNumber, long cloudletFileSize,
			long cloudletOutputSize, long memory, UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam, UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw);

		currStagenum = -1;
		this.setMemory(memory);
		stages = new ArrayList<TaskStage>();
	}

	public NetworkCloudlet(long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize,
			long memory, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		this(Id.pollId(NetworkCloudlet.class), cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, memory,
				utilizationModelCpu, utilizationModelRam, utilizationModelBw);
	}

	public NetworkCloudlet(long cloudletLength, int pesNumber, long cloudletFileSize, long cloudletOutputSize,
			long memory, UtilizationModel utilizationModelCpu, UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, int userId) {
		super(cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw, userId);

		currStagenum = -1;
		this.setMemory(memory);
		stages = new ArrayList<TaskStage>();
	}

	private double submittime; // time when cloudlet will be submitted

	private double finishtime; // time when cloudlet finish execution

	private double exetime; // execution time for cloudlet

	private double numStage;// number of stages in cloudlet

	private int currStagenum; // current stage of cloudlet execution

	private double timetostartStage;

	private double timespentInStage; // how much time spent in particular stage

	private Map<Double, HostPacket> timeCommunicate;

	private ArrayList<TaskStage> stages; // all stages which cloudlet execution

	// consists of.

	private double starttime;

	@Override
	public int compareTo(Object arg0) {
		return 0;
	}

	public double getSubmittime() {
		return submittime;
	}

	public long getMemory() {
		return memory;
	}

	public void setMemory(long memory) {
		this.memory = memory;
	}

	/**
	 * @return the finishtime
	 */
	public double getFinishtime() {
		return finishtime;
	}

	/**
	 * @param finishtime
	 *            the finishtime to set
	 */
	public void setFinishtime(double finishtime) {
		this.finishtime = finishtime;
	}

	/**
	 * @return the exetime
	 */
	public double getExetime() {
		return exetime;
	}

	/**
	 * @param exetime
	 *            the exetime to set
	 */
	public void setExetime(double exetime) {
		this.exetime = exetime;
	}

	/**
	 * @return the numStage
	 */
	public double getNumStage() {
		return numStage;
	}

	/**
	 * @param numStage
	 *            the numStage to set
	 */
	public void setNumStage(double numStage) {
		this.numStage = numStage;
	}

	/**
	 * @return the currStagenum
	 */
	public int getCurrStagenum() {
		return currStagenum;
	}

	/**
	 * @param currStagenum
	 *            the currStagenum to set
	 */
	public void setCurrStagenum(int currStagenum) {
		this.currStagenum = currStagenum;
	}

	/**
	 * @return the timetostartStage
	 */
	public double getTimetostartStage() {
		return timetostartStage;
	}

	/**
	 * @param timetostartStage
	 *            the timetostartStage to set
	 */
	public void setTimetostartStage(double timetostartStage) {
		this.timetostartStage = timetostartStage;
	}

	/**
	 * @return the timespentInStage
	 */
	public double getTimespentInStage() {
		return timespentInStage;
	}

	/**
	 * @param timespentInStage
	 *            the timespentInStage to set
	 */
	public void setTimespentInStage(double timespentInStage) {
		this.timespentInStage = timespentInStage;
	}

	/**
	 * @return the timeCommunicate
	 */
	public Map<Double, HostPacket> getTimeCommunicate() {
		return timeCommunicate;
	}

	/**
	 * @param timeCommunicate
	 *            the timeCommunicate to set
	 */
	public void setTimeCommunicate(Map<Double, HostPacket> timeCommunicate) {
		this.timeCommunicate = timeCommunicate;
	}

	/**
	 * @return the stages
	 */
	public ArrayList<TaskStage> getStages() {
		return stages;
	}

	/**
	 * @param stages
	 *            the stages to set
	 */
	public void setStages(ArrayList<TaskStage> stages) {
		this.stages = stages;
	}

	/**
	 * @return the starttime
	 */
	public double getStarttime() {
		return starttime;
	}

	/**
	 * @param starttime
	 *            the starttime to set
	 */
	public void setStarttime(double starttime) {
		this.starttime = starttime;
	}

	/**
	 * @param submittime
	 *            the submittime to set
	 */
	public void setSubmittime(double submittime) {
		this.submittime = submittime;
	}

	/**
	 * @return the vmType
	 */
	public VmType getVmType() {
		return vmType;
	}

	/**
	 * @param vmType
	 *            the vmType to set
	 */
	public void setVmType(VmType vmType) {
		this.vmType = vmType;
	}

	public void setAccumulatedBwCost(double accumulatedBwCost) {
		this.accumulatedBwCost = accumulatedBwCost;
	}

	public void setCostPerBw(double costPerBw) {
		this.costPerBw = costPerBw;
	}

	/**
	 * re-initialize the Cloudlet
	 */
	public void reset() {
		try {
			this.setCloudletStatus(CREATED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setExecStartTime(0.0);
		setFinishtime(-1.0);
		setClassType(0);
		setIndex(-1);
		setAccumulatedBwCost(0.0);
		setCostPerBw(0.0);
		setStages(new ArrayList<TaskStage>());
		setCurrStagenum(-1);
		setNumStage(0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "NetworkCloudlet [id=" + getCloudletId() + ", vmType=" + vmType + ", numStage=" + numStage
				+ ", currStagenum=" + currStagenum + ", vmId=" + vmId + "]";
	}

	/**
	 * @return the serviceTime
	 */
	public double getServiceTime() {
		return serviceTime;
	}

	/**
	 * @param serviceTime the serviceTime to set
	 */
	public void setServiceTime(double serviceTime) {
		this.serviceTime = serviceTime;
	}
	
	

}
