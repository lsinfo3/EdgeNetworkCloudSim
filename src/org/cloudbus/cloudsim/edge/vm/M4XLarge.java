package org.cloudbus.cloudsim.edge.vm;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.edge.EdgeCloudletSpaceSharedScheduler;

/**
 * Amazon m4.large instance. 4 vCPU, 16 GB RAM
 * Intel 7675 -> 18.87K/core
 * @author Brice Kamneng Kwam
 *
 */
public class M4XLarge extends VmEdge {

	private M4XLarge(String name, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm,
			CloudletScheduler cloudletScheduler) {
		super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
	}

	public M4XLarge() {
		this("m4.xlarge", -1, 1000, 4, 16384, 1024, 1024, "Xen", new EdgeCloudletSpaceSharedScheduler());
		setType(VmType.M4XLARGE);
	}
}
