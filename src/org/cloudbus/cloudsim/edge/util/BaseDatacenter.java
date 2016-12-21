package org.cloudbus.cloudsim.edge.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.edge.CloudSimTagsExt;
import org.cloudbus.cloudsim.edge.DatacenterBrokerEdge;
import org.cloudbus.cloudsim.edge.EdgeHost;
import org.cloudbus.cloudsim.edge.Message;
import org.cloudbus.cloudsim.edge.PresetEvent;
import org.cloudbus.cloudsim.edge.service.EdgeDbService;
import org.cloudbus.cloudsim.edge.service.EdgeWebService;
import org.cloudbus.cloudsim.edge.service.Service;
import org.cloudbus.cloudsim.edge.vm.T2Nano;
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
	 * Creates a datacenter for an User only, it has only one host and can take
	 * only one T2NANO {@link T2Nano} }.
	 * 
	 * @param name
	 *            the name
	 * @param dcNum
	 *            the number of available data centers in this simulation
	 * 
	 * @return the NetworkDatacenter
	 */
	public static NetworkDatacenter createUserNetworkDatacenter(String name) {

		List<EdgeHost> hostList = new ArrayList<EdgeHost>();

		int mips = 1300;

		int ram = 600; // host memory (MB)
		long storage = 1100; // host storage
		int bw = 1100;

		hostList.add(createHost(mips, ram, storage, bw, 1));

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
		datacenter.setUserDC(true);
		// Create Internal Datacenter network
		createInternalDcNetwork(datacenter);
		return datacenter;
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
	public static NetworkDatacenter createNetworkDatacenter(String name, int numHost) {

		List<EdgeHost> hostList = new ArrayList<EdgeHost>();

		int mips = 18870;

		int ram = 8048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;
		for (int i = 0; i < numHost; i++) {
			hostList.add(createHost(mips, ram, storage, bw, 4)); // This is our
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
		createInternalDcNetwork(datacenter);
		return datacenter;
	}

	/**
	 * Create an EdgeHost.
	 * 
	 * @param mips
	 *            the mips
	 * @param ram
	 *            the amount of RAM
	 * @param storage
	 *            the amount of Storage
	 * @param bw
	 *            the bandwidth
	 * @param numOfPes
	 *            the number of processing units (CPUs)
	 * @return
	 */
	public static EdgeHost createHost(int mips, int ram, long storage, int bw, int numOfPes) {
		List<Pe> peList = new ArrayList<Pe>();
		for (int i = 0; i < numOfPes; i++) {
			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
		}
		return new EdgeHost(new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList,
				new VmSchedulerSpaceShared(peList));
	}

	/**
	 * inter-connect data centers.
	 * 
	 * @param dcs
	 *            list of data centers
	 * @throws Exception
	 */
	public static void createNetwork() throws Exception {
		ArrayList<NetworkDatacenter> dcs = new ArrayList<>();
		ArrayList<AggregateSwitch> aggSwitch = new ArrayList<>();
		for (int i = 1; i <= 4; i++) {
			dcs.add(createNetworkDatacenter("DC" + i, 1));
		}
		for (int i = 1; i <= 3; i++) {
			aggSwitch.add(new AggregateSwitch("Agg" + i, NetworkConstants.Agg_LEVEL, dcs.get(0)));
		}
		NetworkDatacenter udc = createNetworkDatacenter("UDC", 1);
		udc.setUserDC(true);

		DatacenterBrokerEdge broker = new DatacenterBrokerEdge("Broker_1");
		broker.setUserDC(udc);

		connectAggSwitchToDc(aggSwitch.get(0), udc);
		connectAggSwitchToDc(aggSwitch.get(0), dcs.get(0));
		connectAggSwitchToDc(aggSwitch.get(1), dcs.get(0));
		connectAggSwitchToDc(aggSwitch.get(1), dcs.get(1));
		connectAggSwitchToDc(aggSwitch.get(1), dcs.get(2));
		connectAggSwitchToDc(aggSwitch.get(2), dcs.get(1));
		connectAggSwitchToDc(aggSwitch.get(2), dcs.get(2));
		connectAggSwitchToDc(aggSwitch.get(2), dcs.get(3));

		aggSwitch.get(0).uplinkswitches.add(aggSwitch.get(1));

		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(0));
		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(2));

		aggSwitch.get(2).uplinkswitches.add(aggSwitch.get(1));

		// Create Brokers

		// Add Services
		broker.addService(new EdgeDbService("EDS_broker1"));
		broker.addService(new EdgeDbService("EDS_broker2"));
		broker.addService(new EdgeDbService("EDS_broker3"));

		// Simulate the Broker sending deferred messages to the Services
		// (e.g. new requests)
		Object[] data = new Object[2];
		for (Service service : broker.getServiceList()) {
			data[0] = service.getId();
			data[1] = Message.ZERO;
			broker.presetEvent(broker.getId(), CloudSimTagsExt.BROKER_MESSAGE, data, 6000);
		}

		// maps CloudSim entities to BRITE entities
		NetworkTopology.mapNode(udc.getId(), 0);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udc), 1);
		NetworkTopology.mapNode(aggSwitch.get(0).getId(), 2);
		NetworkTopology.mapNode(dcs.get(0).getId(), 3);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(0)), 4);
		NetworkTopology.mapNode(aggSwitch.get(1).getId(), 5);
		NetworkTopology.mapNode(dcs.get(1).getId(), 6);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(1)), 7);
		NetworkTopology.mapNode(dcs.get(2).getId(), 8);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(2)), 9);
		NetworkTopology.mapNode(aggSwitch.get(2).getId(), 10);
		NetworkTopology.mapNode(dcs.get(3).getId(), 11);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(3)), 12);
		NetworkTopology.mapNode(broker.getId(), 13);

	}

	/**
	 * inter-connect data centers.
	 * 
	 * @param dcs
	 *            list of data centers
	 * @throws Exception
	 */
	public static void createNetworkWorking() throws Exception {
		ArrayList<NetworkDatacenter> dcs = new ArrayList<>();
		for (int i = 1; i <= 1; i++) {
			dcs.add(createNetworkDatacenter("DC" + i, 3));
		}

		// Create Brokers
		DatacenterBrokerEdge broker = new DatacenterBrokerEdge("Broker_1");
		broker.setUserDC(dcs.get(0));

		// Add Services
		broker.addService(new EdgeDbService("EDS_broker2"));
		broker.addService(new EdgeDbService("EDS_broker3"));

		// Simulate the Broker sending deferred messages to the Services
		// (e.g. new requests)
		Object[] data = new Object[2];
		for (Service service : broker.getServiceList()) {
			data[0] = service.getId();
			data[1] = Message.ZERO;
			broker.presetEvent(broker.getId(), CloudSimTagsExt.BROKER_MESSAGE, data, 600.0);
		}

		NetworkTopology.mapNode(dcs.get(0).getId(), 0);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(0)), 1);
		NetworkTopology.mapNode(broker.getId(), 2);

	}

	/**
	 * inter-connect data centers.
	 * 
	 * @param dcs
	 *            list of data centers
	 * @throws Exception
	 */
	public static void createNetwork3() throws Exception {
		ArrayList<NetworkDatacenter> dcs = new ArrayList<>();
		for (int i = 0; i <= 2; i++) {
			dcs.add(createNetworkDatacenter("DC" + i, 1));
		}

		AggregateSwitch aggSwitch = new AggregateSwitch("Agg1", NetworkConstants.Agg_LEVEL, dcs.get(0));

		// Create Brokers
		DatacenterBrokerEdge broker = new DatacenterBrokerEdge("Broker_1");
		broker.setUserDC(dcs.get(0));

		connectAggSwitchToDc(aggSwitch, dcs.get(0));
		connectAggSwitchToDc(aggSwitch, dcs.get(1));
		connectAggSwitchToDc(aggSwitch, dcs.get(2));

		// Add Services
		broker.addService(new EdgeDbService("EDS_broker1"));
		broker.addService(new EdgeDbService("EDS_broker2"));

		// Simulate the Broker sending deferred messages to the Services
		// (e.g. new requests)
		Object[] data = new Object[2];
		for (Service service : broker.getServiceList()) {
			data[0] = service.getId();
			data[1] = Message.ZERO;
			broker.presetEvent(broker.getId(), CloudSimTagsExt.BROKER_MESSAGE, data, 600.0);
		}


		NetworkTopology.mapNode(dcs.get(0).getId(), 3);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(0)), 4);
		NetworkTopology.mapNode(aggSwitch.getId(), 5);
		NetworkTopology.mapNode(dcs.get(1).getId(), 6);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(1)), 7);
		NetworkTopology.mapNode(dcs.get(2).getId(), 8);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(2)), 9);

	}

	public static int getDcFirstEdgeSwitch(NetworkDatacenter dc) {
		int key = -1;
		for (int k : dc.Switchlist.keySet()) {
			key = k;
			break;
		}
		return key;
	}

	/**
	 * connects an {@link AggregateSwitch} to a {@link NetworkDatacenter}.
	 * 
	 * @param aggSwitch
	 *            the Aggregate Switch
	 * @param netDc
	 *            the Network Data center
	 */
	public static void connectAggSwitchToDc(AggregateSwitch aggSwitch, NetworkDatacenter netDc) {
		aggSwitch.connectDatacenter(netDc);
		for (Switch sw : netDc.Switchlist.values()) {
			aggSwitch.downlinkswitches.add(sw);
			sw.uplinkswitches.add(aggSwitch);
		}
	}

	/**
	 * define the internal network of a data center.
	 * 
	 * @param dc
	 *            the data center
	 */
	@SuppressWarnings("unchecked")
	public static void createInternalDcNetwork(NetworkDatacenter dc) {

		// Edge Switch
		EdgeSwitch edgeswitch = new EdgeSwitch("Edge" + dc.getId(), NetworkConstants.EDGE_LEVEL, dc);

		System.out.println("EdgeSwitch Id: " + edgeswitch.getId() + " created");
		dc.Switchlist.put(edgeswitch.getId(), edgeswitch);

		for (Host hs : dc.getHostList()) {
			EdgeHost hs1 = (EdgeHost) hs;
			hs1.bandwidth = NetworkConstants.BandWidthEdgeHost;
			edgeswitch.hostlist.put(hs.getId(), hs1);
			dc.HostToSwitchid.put(hs.getId(), edgeswitch.getId());
			hs1.sw = edgeswitch;
			List<EdgeHost> hslist = (List<EdgeHost>) (List<?>) hs1.sw.fintimelistHost.get(0D);
			if (hslist == null) {
				hslist = new ArrayList<EdgeHost>();
				hs1.sw.fintimelistHost.put(0D, (List<NetworkHost>) (List<?>) hslist);
			}
			hslist.add(hs1);
		}

	}

	/**
	 * define the internal network of a data center.
	 * 
	 * @param dc
	 *            the data center
	 * @param dcNum
	 *            the number of this data center (1st, 2nd, ...)
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
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + indent
				+ "Time" + indent + "Start Time" + indent + "Finish Time");

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
