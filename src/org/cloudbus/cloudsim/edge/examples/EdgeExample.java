package org.cloudbus.cloudsim.edge.examples;

import java.util.Calendar;
import java.util.Properties;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.util.BaseDatacenter;
import org.cloudbus.cloudsim.edge.util.CustomLog;

public class EdgeExample {

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

		Log.printLine("Starting EdgeExample...");

		try {
			
			Properties props = new Properties();
			props.setProperty("FilePath", "C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\results.txt");
			props.setProperty("LogRealTimeClock", "true");
//			props.setProperty("LogCloudSimClock", "true");
			
			CustomLog.configLogger(props);
			
			CustomLog.printf("%s\t%s\t%s", "Time", "Entity", "Message");
			
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// load the network topology file
			NetworkTopology
			.buildNetworkTopology("C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\topology.brite");
			
			
			BaseDatacenter.createNetworkWorking();

			

			CloudSim.startSimulation();
			
//			 CloudSim.stopSimulation();
			Log.printLine("EdgeExample finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}
}
