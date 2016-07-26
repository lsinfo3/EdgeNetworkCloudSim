/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * NewtorkPacket represents the packet which travel from one server to another. Each packet contains
 * ids of the sender VM and receiver VM, time at which it is send and received, type and virtual ids
 * of tasks, which are communicating.
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
public class NetworkPacket {

	public NetworkPacket(int id, HostPacket pkt2, int vmid, int cloudletid) {
		pkt = pkt2;
		sendervmid = vmid;
		this.cloudletid = cloudletid;
		senderhostid = id;
		stime = pkt.getSendtime();
		setRecievervmid(pkt2.getReciever());

	}

	public int getRecievervmid() {
		return recievervmid;
	}

	public void setRecievervmid(int recievervmid) {
		this.recievervmid = recievervmid;
	}

	HostPacket pkt;

	int senderhostid;

	int recieverhostid;

	int sendervmid;

	private int recievervmid;

	int cloudletid;

	double stime;// time when sent

	double rtime;// time when received

	/**
	 * @return the pkt
	 */
	public HostPacket getPkt() {
		return pkt;
	}

	/**
	 * @param pkt the pkt to set
	 */
	public void setPkt(HostPacket pkt) {
		this.pkt = pkt;
	}

	/**
	 * @return the senderhostid
	 */
	public int getSenderhostid() {
		return senderhostid;
	}

	/**
	 * @param senderhostid the senderhostid to set
	 */
	public void setSenderhostid(int senderhostid) {
		this.senderhostid = senderhostid;
	}

	/**
	 * @return the recieverhostid
	 */
	public int getRecieverhostid() {
		return recieverhostid;
	}

	/**
	 * @param recieverhostid the recieverhostid to set
	 */
	public void setRecieverhostid(int recieverhostid) {
		this.recieverhostid = recieverhostid;
	}

	/**
	 * @return the sendervmid
	 */
	public int getSendervmid() {
		return sendervmid;
	}

	/**
	 * @param sendervmid the sendervmid to set
	 */
	public void setSendervmid(int sendervmid) {
		this.sendervmid = sendervmid;
	}

	/**
	 * @return the cloudletid
	 */
	public int getCloudletid() {
		return cloudletid;
	}

	/**
	 * @param cloudletid the cloudletid to set
	 */
	public void setCloudletid(int cloudletid) {
		this.cloudletid = cloudletid;
	}

	/**
	 * @return the stime
	 */
	public double getStime() {
		return stime;
	}

	/**
	 * @param stime the stime to set
	 */
	public void setStime(double stime) {
		this.stime = stime;
	}

	/**
	 * @return the rtime
	 */
	public double getRtime() {
		return rtime;
	}

	/**
	 * @param rtime the rtime to set
	 */
	public void setRtime(double rtime) {
		this.rtime = rtime;
	}
	
	
}
