/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * Taskstage represents various stages a networkCloudlet can have during execution. Four stage types
 * which are possible-> EXECUTION=0; WAIT_SEND=1; WAIT_RECV=2; FINISH=-2; Check NeworkConstants.java
 * file for that.
 * 
 * Please refer to following publication for more details:
 * 
 * Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 */
public class TaskStage {

	public TaskStage(int type, double data, double time, double stageid, long memory, int peer, int vpeer) {
		super();
		this.setType(type);
		this.setData(data);
		this.setTime(time);
		this.stageid = stageid;
		this.memory = memory;
		this.setPeer(peer);
		this.setVpeer(vpeer);
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public int getPeer() {
		return peer;
	}

	public void setPeer(int peer) {
		this.peer = peer;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getVpeer() {
		return vpeer;
	}

	public void setVpeer(int vpeer) {
		this.vpeer = vpeer;
	}

	public double getData() {
		return data;
	}

	public void setData(double data) {
		this.data = data;
	}

	private int vpeer;

	private int type;// execution, recv, send,

	private double data;// data generated or send or recv

	private double time;// execution time for this stage

	double stageid;

	long memory;

	private int peer;// from whom data needed to be recieved or send (VM)

}
