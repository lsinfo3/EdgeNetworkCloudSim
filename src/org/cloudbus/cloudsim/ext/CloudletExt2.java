package org.cloudbus.cloudsim.ext;

import java.util.LinkedList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.ext.util.Id;

public class CloudletExt2 extends Cloudlet {

	public CloudletExt2(int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Allocates a new Cloudlet object. The Cloudlet length, input and output
	 * file sizes should be greater than or equal to 1. By default this
	 * constructor sets the history of this object.
	 * 
	 * @param userId
	 *            the ID of the broker/user
	 * @param cloudletLength
	 *            the length or size (in MI) of this cloudlet to be executed in
	 *            a PowerDatacenter
	 * @param cloudletFileSize
	 *            the file size (in byte) of this cloudlet <tt>BEFORE</tt>
	 *            submitting to a PowerDatacenter
	 * @param cloudletOutputSize
	 *            the file size (in byte) of this cloudlet <tt>AFTER</tt> finish
	 *            executing by a PowerDatacenter
	 * @param pesNumber
	 *            the pes number
	 * @param utilizationModelCpu
	 *            the utilization model cpu
	 * @param utilizationModelRam
	 *            the utilization model ram
	 * @param utilizationModelBw
	 *            the utilization model bw
	 * @pre cloudletID >= 0
	 * @pre cloudletLength >= 0.0
	 * @pre cloudletFileSize >= 1
	 * @pre cloudletOutputSize >= 1
	 * @post $none
	 */
	public CloudletExt2(final long cloudletLength,
			final int pesNumber, final long cloudletFileSize,
			final long cloudletOutputSize,
			final UtilizationModel utilizationModelCpu,
			final UtilizationModel utilizationModelRam,
			final UtilizationModel utilizationModelBw, final int userId) {
		super(Id.pollId(CloudletExt.class), cloudletLength, pesNumber,
				cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
				utilizationModelRam, utilizationModelBw, false);
		vmId = -1;
		setUserId(userId);
		accumulatedBwCost = 0.0;
		costPerBw = 0.0;

		setRequiredFiles(new LinkedList<String>() );
	}

}
