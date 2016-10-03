package org.cloudbus.cloudsim.edge.vm;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.edge.EdgeCloudletSpaceSharedScheduler;

/**
 * Amazon t2.small instance. 1 vCPU, 2 GB RAM.
 * Intel 5570 -> 11.72K/core
 * @author Brice Kamneng Kwam
 *
 */
public class T2Small extends VmEdge {

	private T2Small(String name, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm,
			CloudletScheduler cloudletScheduler) {
		super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
	}

	
	public T2Small() {
		this("t2.small", -1, 11720, 1, 2048, 1024, 1024, "Xen", new EdgeCloudletSpaceSharedScheduler());
		setType(VmType.T2SMALL);
	}

}
