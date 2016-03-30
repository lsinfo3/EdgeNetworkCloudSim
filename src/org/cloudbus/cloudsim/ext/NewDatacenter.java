package org.cloudbus.cloudsim.ext;

import java.util.List;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;

public class NewDatacenter extends Datacenter {
	
	public static final int PERIODIC_EVENT = 67567; // choose any unused value
													// you want to represent the
													// tag.

	public NewDatacenter(String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList,
				schedulingInterval);
		// TODO Auto-generated constructor stub
	}


	@Override
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine("Warning: " + CloudSim.clock() + ": "
					+ this.getName() + ": Null event ignored.");
		} else {
			int tag = ev.getTag();
			switch (tag) {
			case PERIODIC_EVENT:
				processPeriodicEvent(ev);
				break;
			default:
				Log.printLine("Warning: " + CloudSim.clock() + ":"
						+ this.getName() + ": Unknown event ignored. Tag:"
						+ tag);
			}
		}
	}

	private void processPeriodicEvent(SimEvent ev) {
		// your code here :
		float delay = 0; // contains the delay to the next periodic event
		boolean generatePeriodicEvent = false; // true if new internal events have to be
										// generated
		if (generatePeriodicEvent)
			send(getId(), delay, PERIODIC_EVENT, ev.getData());
	}

}
