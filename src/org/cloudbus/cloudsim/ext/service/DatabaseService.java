package org.cloudbus.cloudsim.ext.service;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class DatabaseService extends Service {

	public DatabaseService(String name, double lifeLength) {
		super("DatabaseService-" + name, lifeLength);
		// TODO Auto-generated constructor stub
	}
	
	public DatabaseService(String name) {
		super("DatabaseService-" + name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Cloudlet createCloudlet() {
		// TODO Auto-generated method stub
		// Cloudlet properties
		long length = 4000000;
		long fileSize = 1000;
		long outputSize = 1000;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		int pesNumber = 2; // number of cpus
		return new Cloudlet(length, pesNumber, fileSize, outputSize,
				utilizationModel, utilizationModel, utilizationModel, getUserId(), getId());
	}

}
