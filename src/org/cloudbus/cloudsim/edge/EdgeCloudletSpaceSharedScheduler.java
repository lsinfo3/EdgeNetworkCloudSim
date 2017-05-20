/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.edge.service.Service;
import org.cloudbus.cloudsim.edge.util.CustomLog;
import org.cloudbus.cloudsim.edge.util.TextUtil;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.network.datacenter.HostPacket;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.TaskStage;

/**
 * CloudletSchedulerSpaceShared implements a policy of scheduling performed by a
 * virtual machine. It consider that there will be only one cloudlet per VM.
 * Other cloudlets will be in a waiting list. We consider that file transfer
 * from cloudlets waiting happens before cloudlet execution. I.e., even though
 * cloudlets must wait for CPU, data transfer happens as soon as cloudlets are
 * submitted.
 * 
 * @author Saurabh Kumar Garg
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class EdgeCloudletSpaceSharedScheduler extends CloudletScheduler {

	/** The cloudlet waiting list. */
	private List<? extends ResCloudlet> cloudletWaitingList;

	/** The cloudlet exec list. */
	private List<? extends ResCloudlet> cloudletExecList;

	/** The cloudlet paused list. */
	private List<? extends ResCloudlet> cloudletPausedList;

	/** The cloudlet finished list. */
	private List<? extends ResCloudlet> cloudletFinishedList;

	/** The current CPUs. */
	protected int currentCpus;

	/** The used PEs. */
	protected int usedPes;

	// for network

	public Map<Integer, List<HostPacket>> pkttosend;

	public Map<Integer, List<HostPacket>> pktrecv;

	/**
	 * Creates a new CloudletSchedulerSpaceShared object. This method must be
	 * invoked before starting the actual simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public EdgeCloudletSpaceSharedScheduler() {
		super();
		cloudletWaitingList = new ArrayList<ResCloudlet>();
		cloudletExecList = new ArrayList<ResCloudlet>();
		cloudletPausedList = new ArrayList<ResCloudlet>();
		cloudletFinishedList = new ArrayList<ResCloudlet>();
		usedPes = 0;
		currentCpus = 0;
		pkttosend = new HashMap<Integer, List<HostPacket>>();
		pktrecv = new HashMap<Integer, List<HostPacket>>();
	}

	/**
	 * Updates the processing of cloudlets running under management of this
	 * scheduler.
	 * 
	 * @param currentTime
	 *            current simulation time
	 * @param mipsShare
	 *            array with MIPS share of each processor available to the
	 *            scheduler
	 * @return time predicted completion time of the earliest finishing
	 *         cloudlet, or 0 if there is no next events
	 * @pre currentTime >= 0
	 * @post $none
	 */
	@Override
	public double updateVmProcessing(double currentTime, List<Double> mipsShare) {

		setCurrentMipsShare(mipsShare);
		// update
		double capacity = 0.0;
		int cpus = 0;

		for (Double mips : mipsShare) { // count the CPUs available to the VMM
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCpus = cpus;
		capacity /= cpus; // average capacity of each cpu

		for (ResCloudlet rcl : getCloudletExecList()) {
			// each machine in the exec list has the same amount of cpu

			NetworkCloudlet cl = (NetworkCloudlet) rcl.getCloudlet();

			// check status if execution stage update the cloudlet finishtime
			// CHECK WHETHER IT IS WAITING FOR THE PACKET
			// if packet received change the status of job and update the time.

			if ((cl.getCurrStagenum() != -1)) {
				TaskStage st = cl.getStages().get(cl.getCurrStagenum());
				if (st.getType() != NetworkConstants.EXECUTION) {
					// System.out.println(TextUtil.toString(CloudSim.clock())
					// + " [DEBUG]: CL #" + cl.getCloudletId()
					// + " Current stageNum " + cl.getCurrStagenum());
				}

				if (cl.getCurrStagenum() == NetworkConstants.FINISH) {
					break;
				}
				if (st.getType() == NetworkConstants.EXECUTION) {

					// update the time
					cl.setTimespentInStage(Math.round(CloudSim.clock() - cl.getTimetostartStage()));
					if (cl.getTimespentInStage() >= st.getTime()) {
						// change the stage
						changetonextstage(cl, st);
					}
				}
				if (st.getType() == NetworkConstants.WAIT_RECV) {
					List<HostPacket> pktlist = pktrecv.get(st.getPeer());
					List<HostPacket> pkttoremove = new ArrayList<HostPacket>();
					if (pktlist != null) {
						if (pktlist.isEmpty()) {
							// changetonextstage(cl, st);
							continue;
						}
						Iterator<HostPacket> it = pktlist.iterator();
						HostPacket pkt = null;
						if (it.hasNext()) {
							pkt = it.next();
							// Asumption packet will not arrive in the same
							// cycle
							if (pkt.getReciever() == cl.getVmId()) {
								if (pkt.getVirtualrecvid() == cl.getCloudletId()) {

									if (isCloudletOwnerBroker(cl)) {

										CustomLog.printf("%s\t\t%s\t\t%s\t\t\t%s", TextUtil.toString(CloudSim.clock()),
												"Service #", TextUtil.toString(CloudSim.clock() - cl.getServiceTime()),
												TextUtil.toString(st.getData()));

										cl.setServiceTime(CloudSim.clock());
									}

									System.out.println(TextUtil.toString(CloudSim.clock()) + " [RECV]: CL #"
											+ cl.getCloudletId() + " received a packet from CL #"
											+ pkt.getVirtualsendid() + " with the data: " + pkt.getData());

									pkt.setRecievetime(CloudSim.clock());
									st.setTime(CloudSim.clock() - pkt.getSendtime());
									changetonextstage(cl, st);
									pkttoremove.add(pkt);
								}
							}
						}
						pktlist.removeAll(pkttoremove);
						// else wait for recieving the packet
					} else {
						// System.out.println(TextUtil.toString(CloudSim.clock())
						// + " [RECV_WAIT]: CL #"
						// + cl.getCloudletId() + " waiting packet from CL #" +
						// st.getVpeer());
					}
				}
				if (st.getType() == NetworkConstants.WAIT_SEND) {

					if (isCloudletOwnerBroker(cl)) {
						cl.setServiceTime(CloudSim.clock());
					}

					System.out.println(TextUtil.toString(CloudSim.clock()) + " [SEND]: CL #" + cl.getCloudletId()
							+ " in VM #" + cl.getVmId() + " sends packet to CL #" + st.getVpeer() + " in VM #"
							+ st.getPeer() + " with data " + st.getData());

					HostPacket pkt = new HostPacket(cl.getVmId(), st.getPeer(), st.getData(), CloudSim.clock(), -1,
							cl.getCloudletId(), st.getVpeer());

					List<HostPacket> pktlist = pkttosend.get(cl.getVmId());
					if (pktlist == null) {
						pktlist = new ArrayList<HostPacket>();
					}
					pktlist.add(pkt);
					pkttosend.put(cl.getVmId(), pktlist);

					// change the stage
					changetonextstage(cl, st);
				}

			} else {
				cl.setCurrStagenum(0);
				cl.setTimetostartStage(CloudSim.clock());

				Datacenter dc = getCloudletDC(cl);
				if (cl.getStages().get(0).getType() == NetworkConstants.EXECUTION) {
					dc.schedule(dc.getId(), cl.getStages().get(0).getTime(), CloudSimTags.VM_DATACENTER_EVENT);
				} else {
					dc.schedule(dc.getId(), 0.0001, CloudSimTags.VM_DATACENTER_EVENT);
					// sendstage
				}
			}

		}

		if (getCloudletExecList().size() == 0 && getCloudletWaitingList().size() == 0) { // no
			// more cloudlets in this scheduler
			setPreviousTime(currentTime);
			return 0.0;
		}

		// update each cloudlet
		int finished = 0;
		List<ResCloudlet> toRemove = new ArrayList<ResCloudlet>();
		for (ResCloudlet rcl : getCloudletExecList()) {
			// rounding issue...
			if (((NetworkCloudlet) (rcl.getCloudlet())).getCurrStagenum() == NetworkConstants.FINISH) {
				// stage is changed and packet to send
				((NetworkCloudlet) (rcl.getCloudlet())).setFinishtime(CloudSim.clock());
				toRemove.add(rcl);
				cloudletFinish(rcl);
				finished++;
			}
		}
		getCloudletExecList().removeAll(toRemove);
		// add all the CloudletExecList in waitingList.
		// sort the waitinglist

		// for each finished cloudlet, add a new one from the waiting list
		if (!getCloudletWaitingList().isEmpty()) {
			for (int i = 0; i < finished; i++) {
				toRemove.clear();
				for (ResCloudlet rcl : getCloudletWaitingList()) {
					if ((currentCpus - usedPes) >= rcl.getNumberOfPes()) {
						rcl.setCloudletStatus(Cloudlet.INEXEC);
						for (int k = 0; k < rcl.getNumberOfPes(); k++) {
							rcl.setMachineAndPeId(0, i);
						}
						getCloudletExecList().add(rcl);
						usedPes += rcl.getNumberOfPes();
						toRemove.add(rcl);
						break;
					}
				}
				getCloudletWaitingList().removeAll(toRemove);
			} // for(cont)
		}

		// estimate finish time of cloudlets in the execution queue
		double nextEvent = Double.MAX_VALUE;
		for (ResCloudlet rcl : getCloudletExecList()) {
			double remainingLength = rcl.getRemainingCloudletLength();
			double estimatedFinishTime = currentTime + (remainingLength / (capacity * rcl.getNumberOfPes()));
			if (estimatedFinishTime - currentTime < CloudSim.getMinTimeBetweenEvents()) {
				estimatedFinishTime = currentTime + CloudSim.getMinTimeBetweenEvents();
			}
			if (estimatedFinishTime < nextEvent) {
				nextEvent = estimatedFinishTime;
			}
		}
		setPreviousTime(currentTime);
		return nextEvent;
	}
	
	private boolean isCloudletOwnerBroker(Cloudlet cloudlet){
		int brID = cloudlet.getUserId();
		SimEntity entity = CloudSim.getEntity(brID);
		if (entity instanceof EdgeDatacenterBroker) {
			return true;

		}
		return false;
	}

	private void changetonextstage(NetworkCloudlet cl, TaskStage st) {
		// for the new/next stage
		cl.setTimespentInStage(0);
		cl.setTimetostartStage(CloudSim.clock());
		int currstage = cl.getCurrStagenum();
		if (currstage > (cl.getStages().size() - 1)) {
			cl.setCurrStagenum(NetworkConstants.FINISH);
		} else {
			cl.setCurrStagenum(currstage + 1);
			int i = 0;
			for (i = cl.getCurrStagenum(); i < cl.getStages().size(); i++) {
				if (cl.getStages().get(i).getType() == NetworkConstants.WAIT_SEND) {

					if (isCloudletOwnerBroker(cl)) {
						cl.setServiceTime(CloudSim.clock());
					}

					HostPacket pkt = new HostPacket(cl.getVmId(), cl.getStages().get(i).getPeer(),
							cl.getStages().get(i).getData(), CloudSim.clock(), -1, cl.getCloudletId(),
							cl.getStages().get(i).getVpeer());
					List<HostPacket> pktlist = pkttosend.get(cl.getVmId());

					System.out.println(TextUtil.toString(CloudSim.clock()) + " [SEND]: CL #" + cl.getCloudletId()
							+ " in VM #" + cl.getVmId() + " sends packet to CL #" + cl.getStages().get(i).getVpeer()
							+ " in VM #" + cl.getStages().get(i).getPeer() + " with data "
							+ cl.getStages().get(i).getData());

					if (pktlist == null) {
						pktlist = new ArrayList<HostPacket>();
					}
					pktlist.add(pkt);
					pkttosend.put(cl.getVmId(), pktlist);

				} else {
					if (cl.getStages().get(i).getType() == NetworkConstants.EXECUTION) {
						// Starts EXEC phase
					}
					break;
				}

			}
			Datacenter dc = getCloudletDC(cl);
			dc.schedule(dc.getId(), 0.0001, CloudSimTags.VM_DATACENTER_EVENT);
			if (i == cl.getStages().size()) {
				cl.setCurrStagenum(NetworkConstants.FINISH);
			} else {
				cl.setCurrStagenum(i);
				if (cl.getStages().get(i).getType() == NetworkConstants.EXECUTION) {
					dc.schedule(dc.getId(), cl.getStages().get(i).getTime() + 1.0, CloudSimTags.VM_DATACENTER_EVENT);
				}

			}
		}

	}

	/**
	 * Cancels execution of a cloudlet.
	 * 
	 * @param cloudletId
	 *            ID of the cloudlet being cancealed
	 * @return the canceled cloudlet, $null if not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Cloudlet cloudletCancel(int cloudletId) {
		// First, looks in the finished queue
		for (ResCloudlet rcl : getCloudletFinishedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				getCloudletFinishedList().remove(rcl);
				return rcl.getCloudlet();
			}
		}

		// Then searches in the exec list
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getCloudletId() == cloudletId) {
				getCloudletExecList().remove(rcl);
				if (rcl.getRemainingCloudletLength() == 0.0) {
					cloudletFinish(rcl);
				} else {
					rcl.setCloudletStatus(Cloudlet.CANCELED);
				}
				return rcl.getCloudlet();
			}
		}

		// Now, looks in the paused queue
		for (ResCloudlet rcl : getCloudletPausedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				getCloudletPausedList().remove(rcl);
				return rcl.getCloudlet();
			}
		}

		// Finally, looks in the waiting list
		for (ResCloudlet rcl : getCloudletWaitingList()) {
			if (rcl.getCloudletId() == cloudletId) {
				rcl.setCloudletStatus(Cloudlet.CANCELED);
				getCloudletWaitingList().remove(rcl);
				return rcl.getCloudlet();
			}
		}

		return null;

	}

	/**
	 * Pauses execution of a cloudlet.
	 * 
	 * @param cloudletId
	 *            ID of the cloudlet being paused
	 * @return $true if cloudlet paused, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean cloudletPause(int cloudletId) {
		boolean found = false;
		int position = 0;

		// first, looks for the cloudlet in the exec list
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			// moves to the paused list
			ResCloudlet rgl = getCloudletExecList().remove(position);
			if (rgl.getRemainingCloudletLength() == 0.0) {
				cloudletFinish(rgl);
			} else {
				rgl.setCloudletStatus(Cloudlet.PAUSED);
				getCloudletPausedList().add(rgl);
			}
			return true;

		}

		// now, look for the cloudlet in the waiting list
		position = 0;
		found = false;
		for (ResCloudlet rcl : getCloudletWaitingList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			// moves to the paused list
			ResCloudlet rgl = getCloudletWaitingList().remove(position);
			if (rgl.getRemainingCloudletLength() == 0.0) {
				cloudletFinish(rgl);
			} else {
				rgl.setCloudletStatus(Cloudlet.PAUSED);
				getCloudletPausedList().add(rgl);
			}
			return true;

		}

		return false;
	}

	/**
	 * Processes a finished cloudlet.
	 * 
	 * @param rcl
	 *            finished cloudlet
	 * @pre rgl != $null
	 * @post $none
	 */
	@Override
	public void cloudletFinish(ResCloudlet rcl) {
		rcl.setCloudletStatus(Cloudlet.SUCCESS);
		rcl.finalizeCloudlet();
		getCloudletFinishedList().add(rcl);
		usedPes -= rcl.getNumberOfPes();
	}

	/**
	 * Resumes execution of a paused cloudlet.
	 * 
	 * @param cloudletId
	 *            ID of the cloudlet being resumed
	 * @return $true if the cloudlet was resumed, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public double cloudletResume(int cloudletId) {
		boolean found = false;
		int position = 0;

		// look for the cloudlet in the paused list
		for (ResCloudlet rcl : getCloudletPausedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				found = true;
				break;
			}
			position++;
		}

		if (found) {
			ResCloudlet rcl = getCloudletPausedList().remove(position);

			// it can go to the exec list
			if ((currentCpus - usedPes) >= rcl.getNumberOfPes()) {
				rcl.setCloudletStatus(Cloudlet.INEXEC);
				for (int i = 0; i < rcl.getNumberOfPes(); i++) {
					rcl.setMachineAndPeId(0, i);
				}

				long size = rcl.getRemainingCloudletLength();
				size *= rcl.getNumberOfPes();
				rcl.getCloudlet().setCloudletLength(size);

				getCloudletExecList().add(rcl);
				usedPes += rcl.getNumberOfPes();

				// calculate the expected time for cloudlet completion
				double capacity = 0.0;
				int cpus = 0;
				for (Double mips : getCurrentMipsShare()) {
					capacity += mips;
					if (mips > 0) {
						cpus++;
					}
				}
				currentCpus = cpus;
				capacity /= cpus;

				long remainingLength = rcl.getRemainingCloudletLength();
				double estimatedFinishTime = CloudSim.clock() + (remainingLength / (capacity * rcl.getNumberOfPes()));

				return estimatedFinishTime;
			} else {// no enough free PEs: go to the waiting queue
				rcl.setCloudletStatus(Cloudlet.QUEUED);

				long size = rcl.getRemainingCloudletLength();
				size *= rcl.getNumberOfPes();
				rcl.getCloudlet().setCloudletLength(size);

				getCloudletWaitingList().add(rcl);
				return 0.0;
			}

		}

		// not found in the paused list: either it is in in the queue, executing
		// or not exist
		return 0.0;

	}

	/**
	 * Receives an cloudlet to be executed in the VM managed by this scheduler.
	 * 
	 * @param cloudlet
	 *            the submited cloudlet
	 * @param fileTransferTime
	 *            time required to move the required files from the SAN to the
	 *            VM
	 * @return expected finish time of this cloudlet, or 0 if it is in the
	 *         waiting queue
	 * @pre gl != null
	 * @post $none
	 */
	@Override
	public double cloudletSubmit(Cloudlet cloudlet, double fileTransferTime) {
		// it can go to the exec list
		if ((currentCpus - usedPes) >= cloudlet.getNumberOfPes()) {
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			rcl.setCloudletStatus(Cloudlet.INEXEC);
			for (int i = 0; i < cloudlet.getNumberOfPes(); i++) {
				rcl.setMachineAndPeId(0, i);
			}

			getCloudletExecList().add(rcl);
			usedPes += cloudlet.getNumberOfPes();
		} else {// no enough free PEs: go to the waiting queue
			System.out.println(TextUtil.toString(CloudSim.clock())
					+ ": [ERROR]: EdgeCloudletSpaceSharedScheduler no enough free PEs: go to the waiting queue : CL #"
					+ cloudlet.getCloudletId() + " on VM #" + cloudlet.getVmId() + " current: " + currentCpus
					+ ", used: " + usedPes + " and requested: " + cloudlet.getNumberOfPes());
			ResCloudlet rcl = new ResCloudlet(cloudlet);
			rcl.setCloudletStatus(Cloudlet.QUEUED);
			getCloudletWaitingList().add(rcl);
			return 0.0;
		}

		// calculate the expected time for cloudlet completion
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : getCurrentMipsShare()) {
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}

		currentCpus = cpus;
		capacity /= cpus;

		// use the current capacity to estimate the extra amount of
		// time to file transferring. It must be added to the cloudlet length
		double extraSize = capacity * fileTransferTime;
		long length = cloudlet.getCloudletLength();
		length += extraSize;
		cloudlet.setCloudletLength(length);
		return cloudlet.getCloudletLength() / capacity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cloudsim.CloudletScheduler#cloudletSubmit(cloudsim.Cloudlet)
	 */
	@Override
	public double cloudletSubmit(Cloudlet cloudlet) {
		cloudletSubmit(cloudlet, 0);
		return 0;
	}

	/**
	 * Gets the status of a cloudlet.
	 * 
	 * @param cloudletId
	 *            ID of the cloudlet
	 * @return status of the cloudlet, -1 if cloudlet not found
	 * @pre $none
	 * @post $none
	 */
	@Override
	public int getCloudletStatus(int cloudletId) {
		for (ResCloudlet rcl : getCloudletExecList()) {
			if (rcl.getCloudletId() == cloudletId) {
				return rcl.getCloudletStatus();
			}
		}

		for (ResCloudlet rcl : getCloudletPausedList()) {
			if (rcl.getCloudletId() == cloudletId) {
				return rcl.getCloudletStatus();
			}
		}

		for (ResCloudlet rcl : getCloudletWaitingList()) {
			if (rcl.getCloudletId() == cloudletId) {
				return rcl.getCloudletStatus();
			}
		}

		return -1;
	}

	/**
	 * Get utilization created by all cloudlets.
	 * 
	 * @param time
	 *            the time
	 * @return total utilization
	 */
	@Override
	public double getTotalUtilizationOfCpu(double time) {
		double totalUtilization = 0;
		for (ResCloudlet gl : getCloudletExecList()) {
			totalUtilization += gl.getCloudlet().getUtilizationOfCpu(time);
		}
		return totalUtilization;
	}

	/**
	 * Informs about completion of some cloudlet in the VM managed by this
	 * scheduler.
	 * 
	 * @return $true if there is at least one finished cloudlet; $false
	 *         otherwise
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean isFinishedCloudlets() {
		return getCloudletFinishedList().size() > 0;
	}

	/**
	 * Returns the next cloudlet in the finished list, $null if this list is
	 * empty.
	 * 
	 * @return a finished cloudlet
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Cloudlet getNextFinishedCloudlet() {
		if (getCloudletFinishedList().size() > 0) {
			return getCloudletFinishedList().remove(0).getCloudlet();
		}
		return null;
	}

	/**
	 * Returns the number of cloudlets runnning in the virtual machine.
	 * 
	 * @return number of cloudlets runnning
	 * @pre $none
	 * @post $none
	 */
	@Override
	public int runningCloudlets() {
		return getCloudletExecList().size();
	}

	/**
	 * Returns one cloudlet to migrate to another vm.
	 * 
	 * @return one running cloudlet
	 * @pre $none
	 * @post $none
	 */
	@Override
	public Cloudlet migrateCloudlet() {
		ResCloudlet rcl = getCloudletExecList().remove(0);
		rcl.finalizeCloudlet();
		Cloudlet cl = rcl.getCloudlet();
		usedPes -= cl.getNumberOfPes();
		return cl;
	}

	/**
	 * Gets the cloudlet waiting list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet waiting list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResCloudlet> List<T> getCloudletWaitingList() {
		return (List<T>) cloudletWaitingList;
	}

	/**
	 * Cloudlet waiting list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletWaitingList
	 *            the cloudlet waiting list
	 */
	protected <T extends ResCloudlet> void cloudletWaitingList(List<T> cloudletWaitingList) {
		this.cloudletWaitingList = cloudletWaitingList;
	}

	/**
	 * Gets the cloudlet exec list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet exec list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResCloudlet> List<T> getCloudletExecList() {
		return (List<T>) cloudletExecList;
	}

	/**
	 * Sets the cloudlet exec list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletExecList
	 *            the new cloudlet exec list
	 */
	protected <T extends ResCloudlet> void setCloudletExecList(List<T> cloudletExecList) {
		this.cloudletExecList = cloudletExecList;
	}

	/**
	 * Gets the cloudlet paused list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet paused list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResCloudlet> List<T> getCloudletPausedList() {
		return (List<T>) cloudletPausedList;
	}

	/**
	 * Sets the cloudlet paused list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletPausedList
	 *            the new cloudlet paused list
	 */
	protected <T extends ResCloudlet> void setCloudletPausedList(List<T> cloudletPausedList) {
		this.cloudletPausedList = cloudletPausedList;
	}

	/**
	 * Gets the cloudlet finished list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @return the cloudlet finished list
	 */
	@SuppressWarnings("unchecked")
	protected <T extends ResCloudlet> List<T> getCloudletFinishedList() {
		return (List<T>) cloudletFinishedList;
	}

	/**
	 * Sets the cloudlet finished list.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param cloudletFinishedList
	 *            the new cloudlet finished list
	 */
	protected <T extends ResCloudlet> void setCloudletFinishedList(List<T> cloudletFinishedList) {
		this.cloudletFinishedList = cloudletFinishedList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.CloudletScheduler#getCurrentRequestedMips()
	 */
	@Override
	public List<Double> getCurrentRequestedMips() {
		List<Double> mipsShare = new ArrayList<Double>();
		if (getCurrentMipsShare() != null) {
			for (Double mips : getCurrentMipsShare()) {
				mipsShare.add(mips);
			}
		}
		return mipsShare;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.CloudletScheduler#
	 * getTotalCurrentAvailableMipsForCloudlet
	 * (org.cloudbus.cloudsim.ResCloudlet, java.util.List)
	 */
	@Override
	public double getTotalCurrentAvailableMipsForCloudlet(ResCloudlet rcl, List<Double> mipsShare) {
		double capacity = 0.0;
		int cpus = 0;
		for (Double mips : mipsShare) { // count the cpus available to the vmm
			capacity += mips;
			if (mips > 0) {
				cpus++;
			}
		}
		currentCpus = cpus;
		capacity /= cpus; // average capacity of each cpu
		return capacity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.CloudletScheduler#
	 * getTotalCurrentAllocatedMipsForCloudlet
	 * (org.cloudbus.cloudsim.ResCloudlet, double)
	 */
	@Override
	public double getTotalCurrentAllocatedMipsForCloudlet(ResCloudlet rcl, double time) {
		return 0.0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.cloudbus.cloudsim.CloudletScheduler#
	 * getTotalCurrentRequestedMipsForCloudlet
	 * (org.cloudbus.cloudsim.ResCloudlet, double)
	 */
	@Override
	public double getTotalCurrentRequestedMipsForCloudlet(ResCloudlet rcl, double time) {
		return 0.0;
	}

	@Override
	public double getCurrentRequestedUtilizationOfBw() {
		return 0;
	}

	@Override
	public double getCurrentRequestedUtilizationOfRam() {
		return 0;
	}

	public Datacenter getCloudletDC(Cloudlet cl) {
		SimEntity entity = CloudSim.getEntity(cl.getUserId());
		;
		Vm vm;
		if (cl.getVmId() != -1) {
			try {
				vm = VmList.getById(((Service) entity).getVmList(), cl.getVmId());
			} catch (Exception e) {
				vm = VmList.getById(((EdgeDatacenterBroker) entity).getVmList(), cl.getVmId());
			}
			return vm.getHost().getDatacenter();
		}
		return null;
	}

}
