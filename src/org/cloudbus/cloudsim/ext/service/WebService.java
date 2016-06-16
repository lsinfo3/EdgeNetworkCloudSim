package org.cloudbus.cloudsim.ext.service;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.ext.CloudletExt2;

public class WebService extends Service {

	public WebService(String name, double lifeLength) {
		super(name, lifeLength);
		// TODO Auto-generated constructor stub
	}
	public WebService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Cloudlet createCloudlet() {
		// TODO Auto-generated method stub
		// Cloudlet properties
		long length = 40000;
		long fileSize = 300;
		long outputSize = 300;
		UtilizationModel utilizationModel = new UtilizationModelFull();
		int pesNumber = 1; // number of cpus
		return new CloudletExt2(length, pesNumber, fileSize, outputSize,
				utilizationModel, utilizationModel, utilizationModel, getUserId());
	}

}
