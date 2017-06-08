package org.cloudbus.cloudsim.edge.lists;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.edge.service.EdgeDbService;
import org.cloudbus.cloudsim.edge.service.EdgeWebService;
import org.cloudbus.cloudsim.edge.service.Service;

/**
 * @author Brice Kamneng Kwam
 *
 */
public class ServiceList {

	/**
	 * Gets the by id.
	 * 
	 * @param serviceList
	 *            the service list
	 * @param id
	 *            the id
	 * @return the service with id
	 */
	public static <T extends Service> T getById(List<T> serviceList, int id) {
		for (T service : serviceList) {
			if (service.getId() == id) {
				return service;
			}
		}
		return null;
	}

	/**
	 * @param serviceList
	 *            the service list
	 * @return a list of EdgeWebService
	 */
	public static <T extends Service> List<T> getWebServices(List<T> serviceList) {
		List<T> results = new ArrayList<>();
		for (T service : serviceList) {
			if (service instanceof EdgeWebService)
				results.add(service);
		}
		return results;
	}

	/**
	 * @param serviceList
	 *            the service list
	 * @return a list of EdgeWebService
	 */
	public static <T extends Service> List<T> getDbServices(List<T> serviceList) {
		List<T> results = new ArrayList<>();
		for (T service : serviceList) {
			if (service instanceof EdgeDbService)
				results.add(service);
		}
		return results;
	}

}
