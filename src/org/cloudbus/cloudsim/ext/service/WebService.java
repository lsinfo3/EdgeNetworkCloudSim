package org.cloudbus.cloudsim.ext.service;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;

public class WebService extends Service {

	public WebService(String name, double lifeLength) {
		super("WebService-" + name, lifeLength);
		// TODO Auto-generated constructor stub
	}
	public WebService(String name) {
		super("WebService-" + name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Cloudlet createCloudlet() {
		// TODO Auto-generated method stub
		// Cloudlet properties
		long length = 400000;
		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		int pesNumber = 1; // number of cpus
		return new Cloudlet(length, pesNumber, fileSize, outputSize,
				utilizationModel, utilizationModel, utilizationModel, getUserId(), getId());
	}

}
