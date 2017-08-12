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

		String exampleFolderPath = System.getProperty("user.dir") + System.getProperty("file.separator") + "src"
				+ System.getProperty("file.separator") + "org" + System.getProperty("file.separator") + "cloudbus"
				+ System.getProperty("file.separator") + "cloudsim" + System.getProperty("file.separator") + "edge"
				+ System.getProperty("file.separator") + "examples";
		String briteFolderPath = exampleFolderPath + System.getProperty("file.separator") + "brites";
		String resultFolderPath = exampleFolderPath + System.getProperty("file.separator") + "results";

		try {

			Properties props = new Properties();
			props.setProperty("FilePath",
					resultFolderPath + System.getProperty("file.separator") + "results_baseline.txt");
			props.setProperty("ServerFilePath",
					resultFolderPath + System.getProperty("file.separator") + "server_utilization.txt");
			props.setProperty("ResponseFilePath",
					resultFolderPath + System.getProperty("file.separator") + "response_time.txt");
			props.setProperty("VmRequestFilePath",
					resultFolderPath + System.getProperty("file.separator") + "vm_request.txt");
			props.setProperty("serviceChainFilePath",
					resultFolderPath + System.getProperty("file.separator") + "service_chain.txt");
			props.setProperty("LogRealTimeClock", "true");
			props.setProperty("LogFormat", "getMessage");

			CustomLog.configLogger(props);

			CustomLog.printf("\t%s\t\t%s\t\t\t%s\t\t\t\t\t%s\t\t%s", "Time", "Entity", "Transmission Time", "Real Time",
					"Data");
			CustomLog.printResponse("\t\t%s\t%s\t\t%s\t\t\t%s\t\t\t%s", "Time", "Service ID", "Service Typ",
					"Transmission Time", "Data");
			CustomLog.printVmRequest("\t%s\t\t%s\t\t\t%s\t\t%s\t\t%s\t\t%s", "Time", "Host ID", "VM ID", "DC ID",
					"Owner ID", "Reason");
			CustomLog.printServer("\t\t%s\t\t%s\t\t%s\t\t%s\t\t\t%s\t\t%s\t\t\t%s\t\t\t%s\t\t%s", "Time", "Host ID",
					"DC ID", "RAM", "CPU", "MIPS", "BW", "Storage", "Num Of VMs");
			CustomLog.printServiceChain("\t\t%s\t\t%s\t\t%s\t\t\t%s\t\t\t\t%s", "Time", "Service ID", "first", "second",
					"third");

			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// load the network topology file
			NetworkTopology
					// .buildNetworkTopology(briteFolderPath +
					// System.getProperty("file.separator") +
					// "topology_double_delay.brite");
					// .buildNetworkTopology(briteFolderPath +
					// System.getProperty("file.separator") +
					// "topology_double_bw.brite");
					.buildNetworkTopology(
							briteFolderPath + System.getProperty("file.separator") + "topology_baseline.brite");

			BaseDatacenter
					// .createNetworkWorkingFirst();
					.createNetworkWorkingSecond();

			// Ends after 30 min
			CloudSim.terminateSimulation(1800000);
			CloudSim.startSimulation();

			Log.printLine("EdgeExample finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}
}
