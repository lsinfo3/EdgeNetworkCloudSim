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
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.CloudSimTagsExt;
import org.cloudbus.cloudsim.edge.EdgeDatacenterBroker;
import org.cloudbus.cloudsim.edge.EdgeHost;
import org.cloudbus.cloudsim.edge.Message;
import org.cloudbus.cloudsim.edge.ServiceTyp;
import org.cloudbus.cloudsim.edge.lists.ServiceList;
import org.cloudbus.cloudsim.edge.random.ExponentialRNS;
import org.cloudbus.cloudsim.edge.service.EdgeDbService;
import org.cloudbus.cloudsim.edge.service.EdgeStreamingService;
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

	/**
	 * simulates 30 min = 30 * 60 * 1000 msec = 1800000 msec
	 */
	private static double simulationTime = 1800000;

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
	 *            the DC name
	 * @param numHost
	 *            number of host in this DC
	 * @param ram
	 *            Amount of RAM per host
	 * @param hostCpuNum
	 *            Amount of CPU per host
	 * @return NetworkDatacenter the NetworkDatacenter
	 */
	public static NetworkDatacenter createNetworkDatacenter(String name, int numHost, int ram, int hostCpuNum) {

		List<EdgeHost> hostList = new ArrayList<EdgeHost>();

		// int mips = 18870;
		int mips = 18870 * 2;
		// int ram = 8048;
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
			hostList.add(createHost(mips, ram, storage, bw, hostCpuNum));
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
	public static void createNetworkWorkingFirst() throws Exception {
		ArrayList<NetworkDatacenter> dcs = new ArrayList<>();
		ArrayList<AggregateSwitch> aggSwitch = new ArrayList<>();
		for (int i = 0; i < 4; i++) {
			dcs.add(createNetworkDatacenter("DC" + i, 1, 8048, 4));
		}
		for (int i = 0; i < 3; i++) {
			aggSwitch.add(new AggregateSwitch("Agg" + i, NetworkConstants.Agg_LEVEL, dcs.get(0)));
		}
		NetworkDatacenter udc = createNetworkDatacenter("UDC", 2, 8048, 4);
		udc.setUserDC(true);

		EdgeDatacenterBroker broker = new EdgeDatacenterBroker("Broker_1");
		broker.setUserDC(udc);

		aggSwitch.get(0).uplinkswitches.add(aggSwitch.get(1));

		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(0));
		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(2));

		aggSwitch.get(2).uplinkswitches.add(aggSwitch.get(1));

		// Generate Web Service start times
		List<Double> webServiceStarts = getServiceStartTime(0.000001);
		// Generate DB Service start times
		List<Double> dbServiceStarts = getServiceStartTime(0.000001);

		Log.printLine(TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter # OF Web SERVICES: "
				+ webServiceStarts.size());
		Log.printLine(TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter # OF DB SERVICES: "
				+ dbServiceStarts.size());

		System.out.println();
		System.out.println();

		// Add Web Services
		addServices(broker, dbServiceStarts, ServiceTyp.WEB, 0.000001);
		// Add DB Services
		addServices(broker, webServiceStarts, ServiceTyp.DB, 0.000001);

		// add requests for DB Services
		addRequests(broker, dbServiceStarts, ServiceTyp.WEB, 0.00001);
		// add requests for Web Services
		addRequests(broker, webServiceStarts, ServiceTyp.DB, 0.00001);

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
	public static void createNetworkWorkingSecond() throws Exception {
		ArrayList<NetworkDatacenter> udcs = new ArrayList<>();
		ArrayList<NetworkDatacenter> dcs = new ArrayList<>();
		ArrayList<AggregateSwitch> aggSwitch = new ArrayList<>();
		ArrayList<EdgeDatacenterBroker> brokers = new ArrayList<>();

		// DC with 1 host (16 GB, 16 CPUs)
		dcs.add(createNetworkDatacenter("DC_Savona", 1, 16 * 1024, 16));
		dcs.add(createNetworkDatacenter("DC_Genova", 1, 20 * 1024, 24));
		dcs.add(createNetworkDatacenter("DC_Torino", 1, 48 * 1024, 24));
		dcs.add(createNetworkDatacenter("DC_Milano", 2, 64 * 1024, 24));

		for (int i = 0; i < 6; i++) {
			aggSwitch.add(new AggregateSwitch("Agg" + i, NetworkConstants.Agg_LEVEL, dcs.get(0)));
		}

		NetworkDatacenter udc;
		for (int i = 0; i < 4; i++) {
			udc = createNetworkDatacenter("UDC" + i, 1, 8048, 4);
			udc.setUserDC(true);
			udcs.add(udc);
		}

		aggSwitch.get(0).uplinkswitches.add(aggSwitch.get(1));

		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(0));
		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(2));
		aggSwitch.get(1).uplinkswitches.add(aggSwitch.get(3));

		aggSwitch.get(2).uplinkswitches.add(aggSwitch.get(1));
		aggSwitch.get(2).uplinkswitches.add(aggSwitch.get(4));
		aggSwitch.get(2).uplinkswitches.add(aggSwitch.get(5));

		aggSwitch.get(3).uplinkswitches.add(aggSwitch.get(1));

		aggSwitch.get(4).uplinkswitches.add(aggSwitch.get(2));

		aggSwitch.get(5).uplinkswitches.add(aggSwitch.get(2));

		// Generate Web Service start times
		List<Double> webServiceStarts = getServiceStartTime(0.000001);
		// Generate DB Service start times
		List<Double> dbServiceStarts = getServiceStartTime(0.000001);
		// Generate Streaming Service start times
		List<Double> streamingServiceStarts = getServiceStartTime(0.000001);

		// One user per service!
		int numUserWeb = webServiceStarts.size();
		int numUserDb = dbServiceStarts.size();
		int numUserStreaming = streamingServiceStarts.size();
		EdgeDatacenterBroker broker;
		List<Double> serviceStart = new ArrayList<>();

		Log.printLine(TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter # USERS: "
				+ (numUserWeb + numUserDb + numUserStreaming));

		for (int i = 0; i < numUserWeb; i++) {
			broker = new EdgeDatacenterBroker("Broker_WEB" + i);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter # Broker: " + broker.getName());
			System.out.println();
			serviceStart.clear();
			serviceStart.add(webServiceStarts.get(i));
			// Add DB Services
			addServices(broker, webServiceStarts, ServiceTyp.WEB, 0.000001);
			// add requests for Web Services
			addRequests(broker, webServiceStarts, ServiceTyp.WEB, 0.00001);
			brokers.add(broker);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[ERROR]: BaseDatacenter # Broker: " + broker.getName());
			System.out.println();
			System.out.println();
			System.out.println();
		}
		for (int i = 0; i < numUserDb; i++) {
			broker = new EdgeDatacenterBroker("Broker_DB" + i);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter # Broker: " + broker.getName());
			System.out.println();
			serviceStart.clear();
			serviceStart.add(dbServiceStarts.get(i));
			// Add Web Services
			addServices(broker, serviceStart, ServiceTyp.DB, 0.000001);
			// add requests for DB Services
			addRequests(broker, serviceStart, ServiceTyp.DB, 0.00001);
			brokers.add(broker);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[ERROR]: BaseDatacenter # Broker: " + broker.getName());
			System.out.println();
			System.out.println();
			System.out.println();
		}
		for (int i = 0; i < numUserStreaming; i++) {
			broker = new EdgeDatacenterBroker("Broker_STREAMING" + i);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter # Broker: " + broker.getName());
			System.out.println();
			serviceStart.clear();
			serviceStart.add(streamingServiceStarts.get(i));
			// Add Web Services
			addServices(broker, serviceStart, ServiceTyp.STREAMING, 0.000001);
			// add requests for DB Services
			addRequests(broker, serviceStart, ServiceTyp.STREAMING, 0.00001);
			brokers.add(broker);
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[ERROR]: BaseDatacenter # Broker: " + broker.getName());
			System.out.println();
			System.out.println();
			System.out.println();
		}

		// maps CloudSim entities to BRITE entities
		NetworkTopology.mapNode(udcs.get(0).getId(), 0);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udcs.get(0)), 1);

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

		NetworkTopology.mapNode(aggSwitch.get(3).getId(), 13);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udcs.get(1)), 14);
		NetworkTopology.mapNode(udcs.get(1).getId(), 15);

		NetworkTopology.mapNode(aggSwitch.get(4).getId(), 16);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udcs.get(2)), 17);
		NetworkTopology.mapNode(udcs.get(2).getId(), 18);

		NetworkTopology.mapNode(aggSwitch.get(5).getId(), 19);
		NetworkTopology.mapNode(getDcFirstEdgeSwitch(udcs.get(3)), 20);
		NetworkTopology.mapNode(udcs.get(3).getId(), 21);

	}

	/**
	 * generate a list of service start times.
	 * 
	 * @param lambda
	 *            distribution coefficient
	 * 
	 * @return list of service start times
	 */
	public static List<Double> getServiceStartTime(double lambda) {
		List<Double> serviceStarts = new ArrayList<>();
		ExponentialRNS interServiceStarttimeDist = new ExponentialRNS(lambda);
		for (double nextServiceStart = 0; nextServiceStart < simulationTime;) {
			double next = interServiceStarttimeDist.next();
			nextServiceStart += next;
			serviceStarts.add(nextServiceStart);
		}
		// the last one is over the simulation time
		serviceStarts.remove(serviceStarts.size() - 1);
		return serviceStarts;
	}

	/**
	 * @param broker
	 *            the Broker the services will be added to.
	 * @param serviceStarts
	 *            the services start times.
	 * @param serviceType
	 *            the type of the service, 0 => DB, 1 => Web
	 * @param lambda
	 *            distribution coefficient
	 */
	public static void addServices(EdgeDatacenterBroker broker, List<Double> serviceStarts, ServiceTyp serviceType,
			double lambda) {
		ExponentialRNS serviceLifetimeDist = new ExponentialRNS(lambda);
		Service service;
		// Add number of Web Service X based on the service start times.
		for (int i = 0; i < serviceStarts.size(); i++) {
			double next = serviceLifetimeDist.next();
			// This sum can be bigger than the simulation time!!!
			double startPlusLifetime = serviceStarts.get(i) + next;
			// choose Service type.
			switch (serviceType) {
			case DB:
				service = new EdgeDbService("EDS" + i, startPlusLifetime);
				break;
			case WEB:
				service = new EdgeWebService("EWS" + i, startPlusLifetime);
				break;
			case STREAMING:
				service = new EdgeStreamingService("ESS" + i, startPlusLifetime);
				break;

			default:
				service = null;
				break;
			}

			broker.addService(service);

			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter NEXT SERVICE: "
					+ service.getName() + ": #" + service.getId());
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter START OF NEXT SERVICE: "
					+ serviceStarts.get(i));
			Log.printLine(
					TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter LIFETIME OF NEXT SERVICE: " + next);
			Log.printLine(TextUtil.toString(CloudSim.clock())
					+ "[FATAL]: BaseDatacenter REAL LIFETIME OF NEXT SERVICE: " + startPlusLifetime);
			System.out.println();
			System.out.println();
		}
	}

	/**
	 * @param broker
	 *            the Broker the services will be added to.
	 * @param serviceStarts
	 *            the services start times.
	 * @param serviceType
	 *            the type of the service, 0 => DB, 1 => Web
	 * @param lambda
	 *            distribution coefficient
	 */
	public static void addRequests(EdgeDatacenterBroker broker, List<Double> serviceStarts, ServiceTyp serviceType,
			double lambda) {
		// Request Type List
		List<Message> messageList = getMessageList();
		List<Service> serviceList = broker.getServiceList();
		List<Service> serviceTypeList;

		// List of Services of the given type

		switch (serviceType) {
		case DB:
			serviceTypeList = ServiceList.getDbServices(serviceList);
			break;
		case WEB:
			serviceTypeList = ServiceList.getWebServices(serviceList);
			break;
		case STREAMING:
			serviceTypeList = ServiceList.getStreamingServices(serviceList);
			break;

		default:
			serviceTypeList = new ArrayList<>();
			break;
		}

		Object[] data = new Object[3];
		int requestId;
		Service service;
		Random rand = new Random();

		for (int i = 0; i < serviceTypeList.size(); i++) {
			requestId = 0;
			service = serviceTypeList.get(i);
			ExponentialRNS interRequestDist = new ExponentialRNS(lambda);
			double serviceStart = serviceStarts.get(i);

			broker.addServiceFirstRequestTime(service.getId(), serviceStart);
			// simulates 30 min = 30 * 60 * 1000 msec = 1800000 msec
			for (double requestStart = serviceStart; requestStart <= simulationTime;) {
				// randomly choose Request type.
				Message message = messageList.get(rand.nextInt(messageList.size()));
				data[0] = requestId;
				data[1] = service.getId();
				data[2] = message;
				broker.addRequestId(service.getId(), requestId);
				broker.presetEvent(broker.getId(), CloudSimTagsExt.BROKER_MESSAGE, data, requestStart);

				double next = interRequestDist.next();
				requestStart += next;
				requestId++;
			}
			Log.printLine(TextUtil.toString(CloudSim.clock()) + "[FATAL]: BaseDatacenter # of Request for Service "
					+ service.getName() + " = " + requestId);
		}
	}

	/**
	 * @return a list of all request types.
	 */
	public static List<Message> getMessageList() {
		List<Message> messageList = new ArrayList<>();
		messageList.add(Message.ZERO);
		messageList.add(Message.ONE);
		messageList.add(Message.TEN);
		messageList.add(Message.HUNDRED);
		messageList.add(Message.THOUSAND);
		return messageList;
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
