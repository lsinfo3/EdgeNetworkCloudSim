package org.cloudbus.cloudsim.edge.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.edge.CloudSimTagsExt;
import org.cloudbus.cloudsim.edge.EdgeDatacenterBroker;
import org.cloudbus.cloudsim.edge.EdgeHost;
import org.cloudbus.cloudsim.edge.Message;
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

		// system architecture
		String arch = "x86";
		// operating system
		String os = "Linux";
		String vmm = "Xen";
		// time zone this resource located
		double time_zone = 10.0;
		// the cost of using processing in this resource
		double cost = 3.0;
		// the cost of using memory in this resource
		double costPerMem = 0.05;
		// the cost of using storage in this resource
		double costPerStorage = 0.001;
		// the cost of using bw in this resource
		double costPerBw = 0.0;
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// create a NetworkDatacenter object.
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

		// int mips = 18870;
		int mips = 18870 * 2;
		int ram = 8048;
		long storage = 1000000;
		// bandwidth
		int bw = 10000;
		// system architecture
		String arch = "x86";
		// operating system
		String os = "Linux";
		String vmm = "Xen";
		// time zone this resource located
		double time_zone = 10.0;
		// the cost of using processing in this resource
		double cost = 3.0;
		// the cost of using memory in this resource
		double costPerMem = 0.05;
		// the cost of using storage in this resource
		double costPerStorage = 0.001;
		// the cost of using bw in this resource
		double costPerBw = 0.0;
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		for (int i = 0; i < numHost; i++) {
			// creates an host.
			hostList.add(createHost(mips, ram, storage, bw, 4));
		}

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// create the NetworkDatacenter object.
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
		// List of Processing elements (CPU)
		List<Pe> peList = new ArrayList<Pe>();
		for (int i = 0; i < numOfPes; i++) {
			peList.add(new Pe(new PeProvisionerSimple(mips)));
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
	public static void createNetworkWorking() throws Exception {
		ArrayList<NetworkDatacenter> dcs = new ArrayList<>();
		ArrayList<AggregateSwitch> aggSwitch = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			dcs.add(createNetworkDatacenter("DC" + i, 1));
		}
		for (int i = 0; i < 3; i++) {
			aggSwitch.add(new AggregateSwitch("Agg" + i, NetworkConstants.Agg_LEVEL, dcs.get(0)));
		}
		NetworkDatacenter udc = createNetworkDatacenter("UDC", 2);
		udc.setUserDC(true);

		EdgeDatacenterBroker broker = new EdgeDatacenterBroker("Broker_1");
		broker.setUserDC(udc);

		aggSwitch.get(0).uplinkswitches.add(aggSwitch.get(1));

		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(0));
		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(2));

		aggSwitch.get(2).uplinkswitches.add(aggSwitch.get(1));

		// Create Brokers

		// Add Services
		broker.addService(new EdgeDbService("EDS3"));
		broker.addService(new EdgeWebService("EWS22"));
		broker.addService(new EdgeDbService("EDS24"));

		// Request List
		List<Message> messageList = new ArrayList<>();
		messageList.add(Message.ZERO);
		messageList.add(Message.ONE);
		messageList.add(Message.TEN);
		messageList.add(Message.HUNDRED);
		messageList.add(Message.THOUSAND);

		int requestId = 0;
		Object[] data = new Object[3];

		// simulates 30 min = 30 * 60 * 1000 msec = 1800000 msec
		// new request every 5 min = 5 * 60 * 1000 msec = 300000 msec
		Random rand = new Random();
		for (int requestStart = 6000; requestStart <= 1800000; requestStart += 300000) {
			// randomly choose Request type.
			Message message = messageList.get(rand.nextInt(messageList.size()));
			for (Service service : broker.getServiceList()) {
				data[0] = requestId;
				data[1] = service.getId();
				data[2] = message;
				broker.addRequestId(service.getId(), requestId);
				broker.presetEvent(broker.getId(), CloudSimTagsExt.BROKER_MESSAGE, data, requestStart);
			}
			requestId++;
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

	}

	/**
	 * inter-connect data centers.
	 * 
	 * @param dcs
	 *            list of data centers
	 * @throws Exception
	 */
	public static void createNetworkWorking4() throws Exception {
		ArrayList<NetworkDatacenter> dcs = new ArrayList<>();
		ArrayList<AggregateSwitch> aggSwitch = new ArrayList<>();
		for (int i = 0; i <= 3; i++) {
			dcs.add(createNetworkDatacenter("DC" + i, 1));
		}

		for (int i = 1; i <= 2; i++) {
			aggSwitch.add(new AggregateSwitch("Agg" + i, NetworkConstants.Agg_LEVEL, dcs.get(0)));
		}

		// Create Brokers
		EdgeDatacenterBroker broker = new EdgeDatacenterBroker("Broker_1");
		broker.setUserDC(dcs.get(0));
		dcs.get(0).setUserDC(true);

		aggSwitch.get(0).uplinkswitches.add(aggSwitch.get(1));
		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(0));

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
			broker.presetEvent(broker.getId(), CloudSimTagsExt.BROKER_MESSAGE, data, 600.0);
		}

		NetworkTopology.mapNode(dcs.get(0).getId(), 0);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(0)), 1);
		NetworkTopology.mapNode(aggSwitch.get(0).getId(), 2);
		NetworkTopology.mapNode(dcs.get(1).getId(), 3);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(1)), 4);
		NetworkTopology.mapNode(aggSwitch.get(1).getId(), 5);
		NetworkTopology.mapNode(dcs.get(2).getId(), 6);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(2)), 7);
		NetworkTopology.mapNode(dcs.get(3).getId(), 8);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(dcs.get(3)), 9);

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
	 * define the internal network of a data center.
	 * 
	 * @param dc
	 *            the data center
	 */
	@SuppressWarnings("unchecked")
	public static void createInternalDcNetwork(NetworkDatacenter dc) {

		// Edge Switch
		EdgeSwitch edgeswitch = new EdgeSwitch("Edge" + dc.getId(), NetworkConstants.EDGE_LEVEL, dc);

		dc.Switchlist.put(edgeswitch.getId(), edgeswitch);

		for (Host hs : dc.getHostList()) {
			EdgeHost hs1 = (EdgeHost) hs;
			// hs1.bandwidth = NetworkConstants.BandWidthEdgeHost;
			hs1.bandwidth = edgeswitch.downlinkbandwidth;
			edgeswitch.hostlist.put(hs.getId(), hs1);
			dc.HostToSwitchid.put(hs.getId(), edgeswitch.getId());
			hs1.sw = edgeswitch;
			// list of hosts connected to this switch for processing
			List<EdgeHost> hostList = (List<EdgeHost>) (List<?>) hs1.sw.fintimelistHost.get(0D);
			if (hostList == null) {
				hostList = new ArrayList<EdgeHost>();
				hs1.sw.fintimelistHost.put(0D, (List<NetworkHost>) (List<?>) hostList);
			}
			hostList.add(hs1);
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
