package org.cloudbus.cloudsim.edge.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.CloudSimTagsExt;
import org.cloudbus.cloudsim.edge.DatacenterBrokerEdge;
import org.cloudbus.cloudsim.edge.EdgeCloudletSpaceSharedScheduler;
import org.cloudbus.cloudsim.edge.util.Id;
import org.cloudbus.cloudsim.edge.vm.VmEdge;
import org.cloudbus.cloudsim.edge.vmallocationpolicy.VmAllocationPolicyCpu;
import org.cloudbus.cloudsim.network.datacenter.EdgeSwitch;
import org.cloudbus.cloudsim.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;
import org.cloudbus.cloudsim.network.datacenter.NetworkHost;
import org.cloudbus.cloudsim.edge.EdgeHost;
import org.cloudbus.cloudsim.edge.Message;
import org.cloudbus.cloudsim.edge.service.EdgeDbService;
import org.cloudbus.cloudsim.edge.service.EdgeWebService;
import org.cloudbus.cloudsim.edge.service.Service;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class EdgeExample1 {

	/** The cloudlet lists. */
	private static List<Service> serviceList0;
	private static List<Service> serviceList1;

	/** The vmlists. */
	private static List<Vm> vmexlist0;
	private static List<Vm> vmexlist1;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExampleExt1...");

		try {

			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 2; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			NetworkDatacenter datacenter0 = createNetworkDatacenter("Datacenter_0");
			System.out.println("1. Dc Id: " + datacenter0.getId());
			NetworkDatacenter datacenter1 = createNetworkDatacenter("Datacenter_1");
			System.out.println("2. Dc Id: " + datacenter1.getId());

			// Third step: Create Brokers
			DatacenterBrokerEdge broker0 = createBroker("Broker_0");
			int brokerId0 = broker0.getId();
			System.out.println("1. Broker Id: " + brokerId0);

			DatacenterBrokerEdge broker1 = createBroker("Broker_1");
			int brokerId1 = broker1.getId();
			System.out.println("2. Broker Id: " + brokerId1);

			// Fourth step: Create one virtual machine for each broker/user
			vmexlist0 = new ArrayList<Vm>();
			vmexlist1 = new ArrayList<Vm>();

			// VM description
			int mips = 250;
			long size = 100; // image size (MB)
			int ram = 1024; // vm memory (MB)
			long bw = 10000;
			int pesNumber = 4; // number of cpus
			String vmm = "Xen"; // VMM name

			// create two VMs: user1
			VmEdge vmex1 = new VmEdge("vm1_broker1", brokerId0, mips, pesNumber, ram, bw, size, vmm,
					new EdgeCloudletSpaceSharedScheduler());
			VmEdge vmex2 = new VmEdge("vm2_broker1", brokerId0, mips, pesNumber, ram, bw, size, vmm,
					new EdgeCloudletSpaceSharedScheduler());

			VmEdge vmex3 = new VmEdge("vm3_broker2", brokerId1, mips, pesNumber, ram, bw, size, vmm,
					new EdgeCloudletSpaceSharedScheduler());
			VmEdge vmex4 = new VmEdge("vm4_broker2", brokerId1, mips, pesNumber, ram, bw, size, vmm,
					new EdgeCloudletSpaceSharedScheduler());

			// add the VMs to the vmlists
			vmexlist0.add(vmex1);
			vmexlist0.add(vmex2);
			vmexlist1.add(vmex3);
			vmexlist1.add(vmex4);

			// submit vm list to the broker
			broker0.submitVmList(vmexlist0);
			broker1.submitVmList(vmexlist1);

			serviceList0 = new ArrayList<Service>();
			serviceList1 = new ArrayList<Service>();

			Service service0 = new EdgeWebService("EWS_broker1");
			service0.setUserId(brokerId0);
			Service service1 = new EdgeDbService("EDS_broker2");
			service1.setUserId(brokerId1);
			serviceList0.add(service0);
			serviceList1.add(service1);
			System.out.println("0. Service Id: " + service0.getId());
			System.out.println("1. Service Id: " + service1.getId());

			broker0.submitServiceList(serviceList0);
			broker1.submitServiceList(serviceList1);

			// broker2.presetEvent(service2.getId(), CloudSimTagsExt.BROKER_MESSAGE, Message.HUNDRED, 50000);
			broker1.presetEvent(service1.getId(), CloudSimTagsExt.BROKER_MESSAGE, Message.HUNDRED, 540);
			// broker1.presetEvent(service1.getId(), CloudSimTagsExt.BROKER_MESSAGE, Message.ONE, 500000);
			broker0.presetEvent(service0.getId(), CloudSimTagsExt.BROKER_MESSAGE, Message.ONE, 520);

			// Fifth step: configure network
			// load the network topology file
			NetworkTopology
					.buildNetworkTopology("C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\topology.brite");

			// maps CloudSim entities to BRITE entities
			// Datacenter0 will correspond to BRITE node 0
			NetworkTopology.mapNode(datacenter0.getId(), 0);

			NetworkTopology.mapNode(datacenter1.getId(), 1);

			NetworkTopology.mapNode(broker0.getId(), 2);

			NetworkTopology.mapNode(broker1.getId(), 3);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList0 = service0.getCloudletReceivedList();
			List<Cloudlet> newList1 = service1.getCloudletReceivedList();

			// CloudSim.stopSimulation();

			Log.print("=============> User " + brokerId0 + "    ");
			Log.print("=============> Service " + service0.getName() + "    ");
			printCloudletList(newList0);

			System.out
					.println("============= User " + brokerId1 + " - Service " + service1.getName() + "==============");
			printCloudletList(newList1);

			Log.printLine("CloudSimExampleExt1 finished!");

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}

	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	private static DatacenterBrokerEdge createBroker(String name) {

		DatacenterBrokerEdge broker = null;
		try {
			broker = new DatacenterBrokerEdge(name, 100000);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine(indent + indent + indent + indent + indent + "========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId()
						+ indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent
						+ dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
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
	static void CreateNetwork(NetworkDatacenter dc, int dcNum) {

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
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return the NetworkDatacenter
	 */
	private static NetworkDatacenter createNetworkDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine

		List<EdgeHost> hostList = new ArrayList<EdgeHost>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		// List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into a list.
		// peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to
		// store Pe id and MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;
		for (int i = 0; i < NetworkConstants.EdgeSwitchPort * NetworkConstants.AggSwitchPort
				* NetworkConstants.RootSwitchPort; i++) {

			List<Pe> peList = new ArrayList<Pe>();

			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
			peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
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
		CreateNetwork(datacenter, 2);
		return datacenter;
	}

}
