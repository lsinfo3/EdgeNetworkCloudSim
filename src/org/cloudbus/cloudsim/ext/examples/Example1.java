package org.cloudbus.cloudsim.ext.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.util.Id;
import org.cloudbus.cloudsim.edge.vm.VMex;
import org.cloudbus.cloudsim.edge.vmallocationpolicy.VmAllocationPolicyBw;
import org.cloudbus.cloudsim.edge.vmallocationpolicy.VmAllocationPolicyCpu;
import org.cloudbus.cloudsim.edge.vmallocationpolicy.VmAllocationPolicyRam;
import org.cloudbus.cloudsim.edge.vmallocationpolicy.VmAllocationPolicyStorage;
import org.cloudbus.cloudsim.ext.CloudSimTagsExt;
import org.cloudbus.cloudsim.ext.DatacenterBrokerExt;
import org.cloudbus.cloudsim.ext.Message;
import org.cloudbus.cloudsim.ext.service.DatabaseService;
import org.cloudbus.cloudsim.ext.service.Service;
import org.cloudbus.cloudsim.ext.service.WebService;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Example1 {

	/** The cloudlet lists. */
	private static List<Service> serviceList1;
	private static List<Service> serviceList2;

	/** The vmlists. */
	private static List<Vm> vmexlist1;
	private static List<Vm> vmexlist2;

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
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			System.out.println("1. Dc Id: " + datacenter0.getId());
			Datacenter datacenter1 = createDatacenter("Datacenter_1");
			System.out.println("1. Dc Id: " + datacenter1.getId());

			// Third step: Create Brokers
			DatacenterBrokerExt broker1 = createBroker(1);
			int brokerId1 = broker1.getId();
			System.out.println("1. Broker Id: " + brokerId1);

			DatacenterBrokerExt broker2 = createBroker(2);
			int brokerId2 = broker2.getId();
			System.out.println("2. Broker Id: " + brokerId2);

			// Fourth step: Create one virtual machine for each broker/user
			vmexlist1 = new ArrayList<Vm>();
			vmexlist2 = new ArrayList<Vm>();

			// VM description
			int mips = 250;
			long size = 100; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 10000;
			int pesNumber = 2; // number of cpus
			String vmm = "Xen"; // VMM name

			// create two VMs: user1
			VMex vmex1 = new VMex("vm1_broker1", brokerId1, mips, pesNumber, ram, bw, size, vmm,
					new CloudletSchedulerSpaceShared());
			VMex vmex2 = new VMex("vm2_broker1", brokerId1, mips, pesNumber, ram, bw, size, vmm,
					new CloudletSchedulerSpaceShared());

			VMex vmex3 = new VMex("vm3_broker2", brokerId2, mips, pesNumber, ram, bw, size, vmm,
					new CloudletSchedulerSpaceShared());
			VMex vmex4 = new VMex("vm4_broker2", brokerId2, mips, pesNumber, ram, bw, size, vmm,
					new CloudletSchedulerSpaceShared());

			// add the VMs to the vmlists
			vmexlist1.add(vmex1);
			vmexlist1.add(vmex2);
			vmexlist2.add(vmex3);
			vmexlist2.add(vmex4);

			// submit vm list to the broker
			broker2.submitVmList(vmexlist2);
			broker1.submitVmList(vmexlist1);

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

			broker2.presetEvent(service2.getId(), CloudSimTagsExt.BROKER_MESSAGE, Message.HUNDRED, 50000);
			broker1.presetEvent(service1.getId(), CloudSimTagsExt.BROKER_MESSAGE, Message.ONE, 500000);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList1 = service1.getCloudletReceivedList();
			List<Cloudlet> newList2 = service2.getCloudletReceivedList();

			// CloudSim.stopSimulation();

			Log.print("=============> User " + brokerId1 + "    ");
			Log.print("=============> Service " + service1.getName() + "    ");
			printCloudletList(newList1);

			Log.print("=============> User " + brokerId2 + "    ");
			Log.print("=============> Service " + service2.getName() + "    ");
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
		peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
		peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));

		// 4. Create Host with its id and list of PEs and add them to the list
		// of machines
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;

		// in this example, the VMAllocatonPolicy in use is SpaceShared. It
		// means that only one VM
		// is allowed to run on each Pe. As each Host has only one Pe, only one
		// VM can run on each Host.
		peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
		peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
		peList.add(new Pe(Id.pollId(Pe.class), new PeProvisionerSimple(mips)));
		hostList.add(new Host(Id.pollId(Host.class), new RamProvisionerSimple(4096), new BwProvisionerSimple(20000),
				storage, peList, new VmSchedulerSpaceShared(peList)));


		hostList.add(new Host(Id.pollId(Host.class), new RamProvisionerSimple(3072), new BwProvisionerSimple(30000),
				2000000, peList, new VmSchedulerSpaceShared(peList)));


		hostList.add(new Host(Id.pollId(Host.class), new RamProvisionerSimple(ram), new BwProvisionerSimple(50000),
				1500000, peList, new VmSchedulerSpaceShared(peList)));


		hostList.add(new Host(Id.pollId(Host.class), new RamProvisionerSimple(1024), new BwProvisionerSimple(bw),
				1200000, peList, new VmSchedulerSpaceShared(peList)));

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

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone,
				cost, costPerMem, costPerStorage, costPerBw);

		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
//			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyRam(hostList), storageList, 0);
//			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyStorage(hostList), storageList, 0);
//			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyBw(hostList), storageList, 0);
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyCpu(hostList), storageList, 0);
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
			broker = new DatacenterBrokerExt("Broker" + id, 1000000000);
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

}
