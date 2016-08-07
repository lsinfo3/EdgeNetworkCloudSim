package org.cloudbus.cloudsim.edge.lists;

import java.util.List;

import org.cloudbus.cloudsim.edge.service.Service;


/**
 * @author Brice Kamneng Kwam
 *
 */
public class ServiceList {
	
	
	/**
	 * Gets the by id.
	 * 
	 * @param cloudletList the cloudlet list
	 * @param id the id
	 * @return the by id
	 */
	public static <T extends Service> T getById(List<T> serviceList, int id) {
		for (T service : serviceList) {
			if (service.getId() == id) {
				return service;
			}
		}
		return null;
	}

}
