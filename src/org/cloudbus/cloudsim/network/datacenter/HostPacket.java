/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * HostPacket represents the packet that travels through the virtual network with a Host. It
 * contains information about cloudlets which are communicating
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
public class HostPacket {

	public HostPacket(
			int sender,
			int reciever,
			double data,
			double sendtime,
			double recievetime,
			int vsnd,
			int vrvd) {
		super();
		this.sender = sender;
		this.setReciever(reciever);
		this.data = data;
		this.setSendtime(sendtime);
		this.setRecievetime(recievetime);
		virtualrecvid = vrvd;
		virtualsendid = vsnd;
	}

	public double getSendtime() {
		return sendtime;
	}

	public void setSendtime(double sendtime) {
		this.sendtime = sendtime;
	}

	public double getRecievetime() {
		return recievetime;
	}

	public void setRecievetime(double recievetime) {
		this.recievetime = recievetime;
	}

	public int getReciever() {
		return reciever;
	}

	public void setReciever(int reciever) {
		this.reciever = reciever;
	}

	int sender;

	int virtualrecvid;

	int virtualsendid;

	private int reciever;

	double data;

	private double sendtime;

	private double recievetime;

	/**
	 * @return the sender
	 */
	public int getSender() {
		return sender;
	}

	/**
	 * @param sender the sender to set
	 */
	public void setSender(int sender) {
		this.sender = sender;
	}

	/**
	 * @return the virtualrecvid
	 */
	public int getVirtualrecvid() {
		return virtualrecvid;
	}

	/**
	 * @param virtualrecvid the virtualrecvid to set
	 */
	public void setVirtualrecvid(int virtualrecvid) {
		this.virtualrecvid = virtualrecvid;
	}

	/**
	 * @return the virtualsendid
	 */
	public int getVirtualsendid() {
		return virtualsendid;
	}

	/**
	 * @param virtualsendid the virtualsendid to set
	 */
	public void setVirtualsendid(int virtualsendid) {
		this.virtualsendid = virtualsendid;
	}

	/**
	 * @return the data
	 */
	public double getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(double data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HostPacket [vmsender=" + sender + ", cloudletrecvid=" + virtualrecvid + ", cloudletsendid=" + virtualsendid
				+ ", vmreciever=" + reciever + ", data=" + data + ", sendtime=" + sendtime + ", recievetime="
				+ recievetime + "]";
	}
	
	
	
}
