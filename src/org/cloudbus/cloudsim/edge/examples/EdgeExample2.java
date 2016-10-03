package org.cloudbus.cloudsim.edge.examples;

import java.util.Calendar;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.CloudSimTagsExt;
import org.cloudbus.cloudsim.edge.DatacenterBrokerEdge;
import org.cloudbus.cloudsim.edge.Message;
import org.cloudbus.cloudsim.edge.service.EdgeDbService;
import org.cloudbus.cloudsim.edge.service.EdgeWebService;
import org.cloudbus.cloudsim.edge.service.Service;
import org.cloudbus.cloudsim.edge.util.BaseDatacenter;
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter;

public class EdgeExample2 {

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting CloudSimExampleExt1...");

		try {

			// before creating any entities.
			int num_user = 2; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			NetworkDatacenter datacenter0 = BaseDatacenter.createNetworkDatacenter("Datacenter_0", 2);
			System.out.println("1. Dc Id: " + datacenter0.getId());
			NetworkDatacenter datacenter1 = BaseDatacenter.createNetworkDatacenter("Datacenter_1", 2);
			System.out.println("2. Dc Id: " + datacenter1.getId());

			// Third step: Create Brokers
			DatacenterBrokerEdge broker0 = new DatacenterBrokerEdge("Broker_0");
			int brokerId0 = broker0.getId();
			System.out.println("1. Broker Id: " + brokerId0);

			DatacenterBrokerEdge broker1 = new DatacenterBrokerEdge("Broker_1");
			int brokerId1 = broker1.getId();
			System.out.println("2. Broker Id: " + brokerId1);

			broker0.addService(new EdgeWebService("EWS_broker1"));
			broker1.addService(new EdgeDbService("EDS_broker2"));

			for (Service service : broker0.getServiceList()) {
				broker0.presetEvent(service.getId(), CloudSimTagsExt.BROKER_MESSAGE, Message.ONE, 520);
			}
			for (Service service : broker1.getServiceList()) {
				broker1.presetEvent(service.getId(), CloudSimTagsExt.BROKER_MESSAGE, Message.HUNDRED, 540);
			}

			// load the network topology file
			NetworkTopology
					.buildNetworkTopology("C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\topology.brite");

			// maps CloudSim entities to BRITE entities
			// Datacenter0 will correspond to BRITE node 0
			NetworkTopology.mapNode(datacenter0.getId(), 0);

			NetworkTopology.mapNode(datacenter1.getId(), 1);

			NetworkTopology.mapNode(broker0.getId(), 2);

			NetworkTopology.mapNode(broker1.getId(), 3);

			CloudSim.startSimulation();

			// Final step: Print results when simulation is over

			for (Service service0 : broker0.getServiceList()) {
				Log.print("=============> User " + brokerId0 + "    ");
				Log.print("=============> Service " + service0.getName() + "    ");
				BaseDatacenter.printCloudletList(service0.getCloudletReceivedList());
			}

			for (Service service1 : broker1.getServiceList()) {
				Log.print("=============> User " + brokerId1 + "    ");
				Log.print("=============> Service " + service1.getName() + "    ");
				BaseDatacenter.printCloudletList(service1.getCloudletReceivedList());
			}

			// CloudSim.stopSimulation();

			Log.printLine("CloudSimExampleExt1 finished!");

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}

	}

}
