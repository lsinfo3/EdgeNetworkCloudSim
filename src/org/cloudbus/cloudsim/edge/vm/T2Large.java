package org.cloudbus.cloudsim.edge.vm;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.edge.EdgeCloudletSpaceSharedScheduler;

/**
 * Amazon t2.large instance. 2 vCPU, 8 GB RAM
 * Intel 5570 -> 11.72K/core
 * @author Brice Kamneng Kwam
 *
 */
public class T2Large extends VmEdge {

	private T2Large(String name, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm,
			CloudletScheduler cloudletScheduler) {
		super(name, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);
	}

	public T2Large() {
		this("t2.large", -1, 11270, 1, 8192, 1024, 1024, "Xen", new EdgeCloudletSpaceSharedScheduler());
		setType(VmType.T2SMALL);
	}

}
