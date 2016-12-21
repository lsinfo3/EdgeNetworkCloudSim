package org.cloudbus.cloudsim.edge.examples;

import java.util.Calendar;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.util.BaseDatacenter;

public class EdgeExample2 {

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting EdgeExample2...");

		try {
			// before creating any entities.
			int num_user = 2; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// load the network topology file
			NetworkTopology
			.buildNetworkTopology("C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\topology.brite");
			
			
			BaseDatacenter.createNetworkWorking();
//			BaseDatacenter.createNetwork3();
//			BaseDatacenter.createNetwork();

			

			CloudSim.startSimulation();

			 CloudSim.stopSimulation();
			Log.printLine("EdgeExample2 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}
}
