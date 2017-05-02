package org.cloudbus.cloudsim.edge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.edge.util.CustomLog;
import org.cloudbus.cloudsim.edge.util.Id;
import org.cloudbus.cloudsim.edge.util.TextUtil;
import org.cloudbus.cloudsim.edge.vm.EdgeVm;
import org.cloudbus.cloudsim.lists.VmList;
import org.cloudbus.cloudsim.network.datacenter.HostPacket;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.NetworkHost;
import org.cloudbus.cloudsim.network.datacenter.NetworkPacket;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

public class EdgeHost extends NetworkHost {

	public EdgeHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage,
			List<? extends Pe> peList, VmScheduler vmScheduler) {
		super(Id.pollId(EdgeHost.class), ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
	}

	public EdgeHost(RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		this(Id.pollId(EdgeHost.class), ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
	}

	/**
	 * Requests updating of processing of cloudlets in the VMs running in this
	 * host.
	 * 
	 * @param currentTime
	 *            the current time
	 * 
	 * @return expected time of completion of the next cloudlet in all VMs in
	 *         this host. Double.MAX_VALUE if there is no future events expected
	 *         in th is host
	 * 
	 * @pre currentTime >= 0.0
	 * @post $none
	 */
	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;
		// insert in each vm packet recieved
		recvpackets();
		for (Vm vm : super.getVmList()) {
			double time = ((EdgeVm) vm).updateVmProcessing(currentTime, getVmScheduler().getAllocatedMipsForVm(vm));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}
		// send the packets to other hosts/VMs
		sendpackets();

		return smallerTime;

	}
	
	/**
	 * Send packet check whether a packet belongs to a local VM or to a VM
	 * hosted on other machine.
	 * 
	 * 
	 */
	@Override
	protected void sendpackets() {
		for (Vm vm : super.getVmList()) {
			for (Entry<Integer, List<HostPacket>> es : ((EdgeCloudletSpaceSharedScheduler) vm
					.getCloudletScheduler()).pkttosend.entrySet()) {
				List<HostPacket> pktlist = es.getValue();
				for (HostPacket pkt : pktlist) {
					NetworkPacket hpkt = new NetworkPacket(getId(), pkt, vm.getId(), pkt.getSender());
					Vm vm2 = VmList.getById(this.getVmList(), hpkt.getRecievervmid());
					if (vm2 != null) {
						packetTosendLocal.add(hpkt);
					} else {
						packetTosendGlobal.add(hpkt);
					}
				}
				pktlist.clear();

			}

		}

		boolean flag = false;

		for (NetworkPacket hs : packetTosendLocal) {
			flag = true;
			hs.setStime(hs.getRtime());
			hs.getPkt().setRecievetime(CloudSim.clock());
			// insert the packet in recievedlist
			Vm vm = VmList.getById(getVmList(), hs.getPkt().getReciever());

			List<HostPacket> pktlist = ((EdgeCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv
					.get(hs.getPkt().getSender());
			if (pktlist == null) {
				pktlist = new ArrayList<HostPacket>();
				((EdgeCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv.put(hs.getPkt().getSender(),
						pktlist);
			}
			pktlist.add(hs.getPkt());
		}
		if (flag) {
			for (Vm vm : super.getVmList()) {
				vm.updateVmProcessing(CloudSim.clock(), getVmScheduler().getAllocatedMipsForVm(vm));
			}
		}

		// Sending packet to other VMs therefore packet is forwarded to a Edge
		// switch
		packetTosendLocal.clear();
		double avband = bandwidth / packetTosendGlobal.size();
		for (NetworkPacket hs : packetTosendGlobal) {
			double delay = (hs.getPkt().getData()) / (avband * 1000);
			NetworkConstants.totaldatatransfer += hs.getPkt().getData();
			
			CustomLog.printf("%s\t\t%s\t\t%s\t\t\t%s\t\t\t%s", TextUtil.toString(CloudSim.clock()),
					"Host#" + getId() + "->#" + sw.getId(),
					TextUtil.toString((hs.getPkt().getData())+ "/(" + avband + "*" + 1000+")"),
					delay,
					TextUtil.toString(hs.getPkt().getData()));
			
			CloudSim.send(getDatacenter().getId(), sw.getId(), delay, CloudSimTags.Network_Event_UP, hs);
			// send to switch with delay
		}
		packetTosendGlobal.clear();
	}

	/**
	 * Receives packet and forward it to the corresponding VM for processing
	 * host.
	 * 
	 * 
	 */
	@Override
	protected void recvpackets() {

		for (NetworkPacket hs : packetrecieved) {
			hs.getPkt().setRecievetime(CloudSim.clock());

			// insert the packet in recievedlist of VM
			Vm vm = VmList.getById(getVmList(), hs.getPkt().getReciever());
			List<HostPacket> pktlist = ((EdgeCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv
					.get(hs.getPkt().getSender());

			if (pktlist == null) {
				System.out.println(String.format("%s\t%s\t%s", TextUtil.toString(CloudSim.clock()), "EdgeHost #" + getId(),
						"RECVD PKT from Host #" + hs.getSenderhostid()));
				pktlist = new ArrayList<HostPacket>();
				((EdgeCloudletSpaceSharedScheduler) vm.getCloudletScheduler()).pktrecv.put(hs.getPkt().getSender(),
						pktlist);

			}
			pktlist.add(hs.getPkt());

		}
		packetrecieved.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String vms = "";
		for (Vm vm : getVmList()) {
			vms += ((EdgeVm) vm).toString() + ",";
		}
		return "EdgeHost [id=" + getId() + ", vms=[" + vms + "]]";
	}

}
