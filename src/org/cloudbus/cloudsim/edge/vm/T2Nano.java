package org.cloudbus.cloudsim.edge.vm;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.edge.EdgeCloudletSpaceSharedScheduler;

/**
 * Amazon t2.nano instance. 1 vCPU, 0.5 GB RAM
 * Intel 5570 -> 11.72K/core 
 * @author Brice Kamneng Kwam
 *
 */
public class T2Nano extends VmEdge {

	private T2Nano(String name, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm,
			CloudletScheduler cloudletScheduler) {
		super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
	}

	public T2Nano() {
		this("t2.nano", -1, 1270, 1, 512, 1024, 1024, "Xen", new EdgeCloudletSpaceSharedScheduler());
		setType(VmType.T2NANO);
	}

}
