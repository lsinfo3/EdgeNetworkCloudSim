package org.cloudbus.cloudsim.ext.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.ext.DatacenterBrokerExt;
import org.cloudbus.cloudsim.ext.service.DatabaseService;
import org.cloudbus.cloudsim.ext.service.Service;
import org.cloudbus.cloudsim.ext.service.WebService;
import org.cloudbus.cloudsim.ext.util.Id;
import org.cloudbus.cloudsim.ext.vm.VMex;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Example1 {

	/** The cloudlet lists. */
	private static List<Service> serviceList1;
	private static List<Service> serviceList2;

	/** The vmlists. */
	private static List<VMex> vmexlist1;
	private static List<VMex> vmexlist2;

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
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			@SuppressWarnings("unused")
			Datacenter datacenter1 = createDatacenter("Datacenter_1");

			// Third step: Create Brokers
			DatacenterBrokerExt broker1 = createBroker(1);
			int brokerId1 = broker1.getId();

			DatacenterBrokerExt broker2 = createBroker(2);
			int brokerId2 = broker2.getId();

			// Fourth step: Create one virtual machine for each broker/user
			vmexlist1 = new ArrayList<VMex>();
			vmexlist2 = new ArrayList<VMex>();

			// VM description
			int mips = 250;
			long size = 10000; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 2; // number of cpus
			String vmm = "Xen"; // VMM name

			// create two VMs: the first one belongs to user1
			VMex vmex1 = new VMex("vm_broker1", brokerId1, mips, pesNumber,
					ram, bw, size, vmm, new CloudletSchedulerTimeShared());

			// the second VM: this one belongs to user2
			VMex vmex2 = new VMex("vm_broker2", brokerId2, mips, pesNumber,
					ram, bw, size, vmm, new CloudletSchedulerTimeShared());

			// add the VMs to the vmlists
			vmexlist1.add(vmex1);
			vmexlist2.add(vmex2);

			// submit vm list to the broker
			broker1.submitVmList(vmexlist1);
			broker2.submitVmList(vmexlist2);
			
			serviceList1 = new ArrayList<Service>();
			serviceList2 = new ArrayList<Service>();
			
			Service service1 = new WebService("WService_broker1");
			service1.setUserId(brokerId1);
			Service service2 = new DatabaseService("DService_broker2");
			service2.setUserId(brokerId2);
			serviceList1.add(service1);
			serviceList2.add(service2);
			
			broker1.submitServiceList(serviceList1);
			broker2.submitServiceList(serviceList2);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList1 = service1.getCloudletReceivedList();
			List<Cloudlet> newList2 = service2.getCloudletReceivedList();

			CloudSim.stopSimulation();

			Log.print("=============> User " + brokerId1 + "    ");
			printCloudletList(newList1);

			Log.print("=============> User " + brokerId2 + "    ");
			printCloudletList(newList2);

			Log.printLine("CloudSimExampleExt1 finished!");

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}

	}

	private static Datacenter createDatacenter(String name) {

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store
		// our machine
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 5000;

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips))); // need to store
		peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips))); // need to store
																// Pe id and
																// MIPS Rating

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		// in this example, the VMAllocatonPolicy in use is SpaceShared. It
		// means that only one VM
		// is allowed to run on each Pe. As each Host has only one Pe, only one
		// VM can run on each Host.
		hostList.add(new Host(Id.pollId(Host.class), new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw), storage, peList,
				new VmSchedulerSpaceShared(peList))); // This is our first
		// machine
		hostList.add(new Host(Id.pollId(Host.class), new RamProvisionerSimple(ram),
				new BwProvisionerSimple(bw), storage, peList,
				new VmSchedulerSpaceShared(peList)));

		// 5. Create a DatacenterCharacteristics object that stores the
		// properties of a data center: architecture, OS, list of
		// Machines, allocation policy: time- or space-shared, time zone
		// and its price (G$/Pe time unit).
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
																		// not
																		// adding
																		// SAN
																		// devices
																		// by
																		// now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics,
					new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	// We strongly encourage users to develop their own broker policies, to
	// submit vms and cloudlets according
	// to the specific rules of the simulated scenario
	private static DatacenterBrokerExt createBroker(int id) {

		DatacenterBrokerExt broker = null;
		try {
			broker = new DatacenterBrokerExt("Broker" + id);
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
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time"
				+ indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}

	}

}
