package org.cloudbus.cloudsim.edge.examples;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

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

			// create Datacenters
			NetworkDatacenter datacenter0 = BaseDatacenter.createNetworkDatacenter("Datacenter_0", 2);

			NetworkDatacenter datacenter1 = BaseDatacenter.createNetworkDatacenter("Datacenter_1", 2);
			
			List<NetworkDatacenter> dcs = new LinkedList<>();
			dcs.add(datacenter0);
			dcs.add(datacenter1);
			BaseDatacenter.createNetwork(dcs);

			// Create Brokers
			DatacenterBrokerEdge broker0 = new DatacenterBrokerEdge("Broker_0");

			DatacenterBrokerEdge broker1 = new DatacenterBrokerEdge("Broker_1");

			// Add Services
			broker0.addService(new EdgeWebService("EWS_broker1"));
			broker1.addService(new EdgeDbService("EDS_broker2"));

			// Simulate the Broker sending deferred messages to the Services
			// (e.g. new requests)
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

//			for (Service service0 : broker0.getServiceList()) {
//				Log.print("=============> User " + service0.getUserId() + "    ");
//				Log.print("=============> Service " + service0.getName() + "    ");
//				BaseDatacenter.printCloudletList(service0.getCloudletReceivedList());
//			}
//
//			for (Service service1 : broker1.getServiceList()) {
//				Log.print("=============> User " + service1.getUserId() + "    ");
//				Log.print("=============> Service " + service1.getName() + "    ");
//				BaseDatacenter.printCloudletList(service1.getCloudletReceivedList());
//			}

			// CloudSim.stopSimulation();
			Log.printLine("CloudSimExampleExt1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}
}
