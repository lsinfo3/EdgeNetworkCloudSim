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
			props.setProperty("FilePath", "C:\\Users\\kwam8\\Nextcloud\\Semester\\results\\results_baseline.txt");
			props.setProperty("ServerFilePath",
					"C:\\Users\\kwam8\\Nextcloud\\Semester\\results\\baseline\\server_utilization.txt");
			props.setProperty("ResponseFilePath",
					"C:\\Users\\kwam8\\Nextcloud\\Semester\\results\\baseline\\response_time.txt");
			props.setProperty("VmRequestFilePath",
					"C:\\Users\\kwam8\\Nextcloud\\Semester\\results\\baseline\\vm_request.txt");
			props.setProperty("serviceChainFilePath",
					"C:\\Users\\kwam8\\Nextcloud\\Semester\\results\\baseline\\service_chain.txt");
			// props.setProperty("FilePath",
			// "C:\\Users\\kwam8\\Nextcloud\\Semester\\results\\results_double_bw.txt");
			// props.setProperty("FilePath",
			// "C:\\Users\\kwam8\\Nextcloud\\Semester\\results\\results_double_delay.txt");
			// props.setProperty("FilePath",
			// "C:\\Users\\kwam8\\Nextcloud\\Semester\\results\\results_double_data.txt");
			props.setProperty("LogRealTimeClock", "true");
			props.setProperty("LogFormat", "getMessage");

			CustomLog.configLogger(props);

			CustomLog.printf("\t%s\t\t%s\t\t\t%s\t\t\t\t\t%s\t\t%s", "Time", "Entity", "Transmission Time", "Real Time", "Data");
			CustomLog.printResponse("\t\t%s\t%s\t\t%s\t\t\t%s\t\t\t%s", "Time", "Service ID", "Service Typ", "Transmission Time", "Data");
			CustomLog.printVmRequest("\t%s\t\t%s\t\t\t%s\t\t%s\t\t%s\t\t%s", "Time", "Host ID", "VM ID", "DC ID",
					"Owner ID", "Reason");
			CustomLog.printServer("\t\t%s\t\t%s\t\t%s\t\t%s\t\t\t%s\t\t%s\t\t\t%s\t\t\t%s\t\t%s", "Time", "Host ID",
					"DC ID", "RAM", "CPU", "MIPS", "BW", "Storage", "Num Of VMs");
			CustomLog.printServiceChain("\t\t%s\t\t%s\t\t%s\t\t\t%s\t\t\t\t%s", "Time", "Service ID",
					"first", "second", "third");

			// before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// load the network topology file
			NetworkTopology
					// .buildNetworkTopology("C:\\Users\\kwam8\\Nextcloud\\Semester\\brites\\first\\topology_double_delay.brite");
					// .buildNetworkTopology("C:\\Users\\kwam8\\Nextcloud\\Semester\\brites\\first\\topology_double_bw.brite");
					// .buildNetworkTopology("C:\\Users\\kwam8\\Nextcloud\\Semester\\brites\\first\\topology_baseline.brite");

					// .buildNetworkTopology("C:\\Users\\kwam8\\Nextcloud\\Semester\\brites\\second\\topology_double_delay.brite");
					// .buildNetworkTopology("C:\\Users\\kwam8\\Nextcloud\\Semester\\brites\\second\\topology_double_bw.brite");
					.buildNetworkTopology(
							"C:\\Users\\kwam8\\Nextcloud\\Semester\\brites\\second\\topology_baseline.brite");

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
