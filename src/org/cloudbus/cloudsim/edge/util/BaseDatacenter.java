package org.cloudbus.cloudsim.edge.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.edge.EdgeHost;
import org.cloudbus.cloudsim.edge.vmallocationpolicy.VmAllocationPolicyCpu;
import org.cloudbus.cloudsim.network.datacenter.AggregateSwitch;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.NetworkHost;
import org.cloudbus.cloudsim.network.datacenter.Switch;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class BaseDatacenter {

	private BaseDatacenter() {

	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the name
	 * @param dcNum
	 *            the number of available data centers in this simulation
	 * 
	 * @return the NetworkDatacenter
	 */
	public static NetworkDatacenter createNetworkDatacenter(String name, int dcNum) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine

		List<EdgeHost> hostList = new ArrayList<EdgeHost>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		// List<Pe> peList = new ArrayList<Pe>();

		int mips = 18870;

		// 3. Create PEs and add these into a list.
		// peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to
		// store Pe id and MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int ram = 8048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;
		for (int i = 0; i < NetworkConstants.EdgeSwitchPort * NetworkConstants.AggSwitchPort
				* NetworkConstants.RootSwitchPort; i++) {

			List<Pe> peList = new ArrayList<Pe>();

			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));

			// 4. Create PowerHost with its id and list of PEs and add them to
			// the list of machines
			hostList.add(new EdgeHost(i, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
					new VmSchedulerSpaceShared(peList))); // This is our machine
		}

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource

		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a NetworkDatacenter object.
		NetworkDatacenter datacenter = null;
		try {
			datacenter = new NetworkDatacenter(name, characteristics, new VmAllocationPolicyCpu(hostList), storageList,
					0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		// Create Internal Datacenter network
		createInternalDcNetwork(datacenter, dcNum);
		return datacenter;
	}

	/**
	 * inter-connect data centers.
	 * 
	 * @param dcs
	 *            list of data centers
	 */
	public static void createNetwork(List<NetworkDatacenter> dcs) {
		if (dcs.size() > 1) {
			AggregateSwitch aggSwitch = new AggregateSwitch("Agg", NetworkConstants.Agg_LEVEL, dcs.get(0));
			for (NetworkDatacenter netDc : dcs) {
				for (Switch sw : netDc.Switchlist.values()) {
					aggSwitch.downlinkswitches.add(sw);
					sw.uplinkswitches.add(aggSwitch);
				}
			}
		}
	}

	/**
	 * define the internal network of a data center.
	 * 
	 * @param dc
	 *            the data center
	 * @param dcNum
	 *            the number of available data centers in this simulation
	 */
	@SuppressWarnings("unchecked")
	public static void createInternalDcNetwork(NetworkDatacenter dc, int dcNum) {

		// Edge Switch
		EdgeSwitch edgeswitch[] = new EdgeSwitch[1];

		for (int i = 0; i < edgeswitch.length; i++) {
			edgeswitch[i] = new EdgeSwitch("Edge" + i, NetworkConstants.EDGE_LEVEL, dc);
			System.out.println("1. EdgeSwitch Id: " + edgeswitch[i].getId());
			// edgeswitch[i].uplinkswitches.add(null);
			dc.Switchlist.put(edgeswitch[i].getId(), edgeswitch[i]);
			// aggswitch[(int)
			// (i/Constants.AggSwitchPort)].downlinkswitches.add(edgeswitch[i]);
		}

		for (Host hs : dc.getHostList()) {
			EdgeHost hs1 = (EdgeHost) hs;
			hs1.bandwidth = NetworkConstants.BandWidthEdgeHost;
			int switchnum = (int) (hs.getId() / (NetworkConstants.EdgeSwitchPort * dcNum));
			edgeswitch[switchnum].hostlist.put(hs.getId(), hs1);
			dc.HostToSwitchid.put(hs.getId(), edgeswitch[switchnum].getId());
			hs1.sw = edgeswitch[switchnum];
			List<EdgeHost> hslist = (List<EdgeHost>) (List<?>) hs1.sw.fintimelistHost.get(0D);
			if (hslist == null) {
				hslist = new ArrayList<EdgeHost>();
				hs1.sw.fintimelistHost.put(0D, (List<NetworkHost>) (List<?>) hslist);
			}
			hslist.add(hs1);

		}

	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 */
	public static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine(indent + indent + indent + indent + indent + "========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}

	}
}
