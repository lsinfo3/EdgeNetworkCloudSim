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
			props.setProperty("FilePath", "C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\results\\results_baseline.txt");
//			props.setProperty("FilePath", "C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\results\\results_double_bw.txt");
//			props.setProperty("FilePath", "C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\results\\results_double_delay.txt");
//			props.setProperty("FilePath", "C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\results\\results_double_data.txt");
			props.setProperty("LogRealTimeClock", "true");
			props.setProperty("LogFormat", "getMessage");
			
			CustomLog.configLogger(props);
			
			CustomLog.printf("\t%s\t\t%s\t\t\t%s\t\t%s", "Time", "Entity", "Transmission Time", "Data");
			
			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// load the network topology file
			NetworkTopology
//			.buildNetworkTopology("C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\brites\\topology_double_delay.brite");
//			.buildNetworkTopology("C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\brites\\topology_double_bw.brite");
			.buildNetworkTopology("C:\\Users\\kwam8\\Dropbox\\Semester\\WS1516\\network\\test_run\\brites\\topology_baseline.brite");
			
			
			BaseDatacenter.createNetworkWorking();

			

			CloudSim.startSimulation();
			
			 CloudSim.terminateSimulation(1800000);
			 CloudSim.stopSimulation();
			Log.printLine("EdgeExample finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}
}
