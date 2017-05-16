/**
 * 
 */
package org.cloudbus.cloudsim.edge.util;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.edge.Message;

/**
 * @author Brice Kamneng Kwam
 *
 */
public class Request {

	/**
	 * Maping service Id -> Broker Message
	 */
	private Map<Integer, Message> serviceToMessage;
	
	/**
	 * Request Id
	 */
	private int id;
	
	
	public Request(){
		this(new HashMap<>());
	}
	public Request(Map<Integer, Message> serviceToMessage){
		this.serviceToMessage = new HashMap<>(serviceToMessage);
		this.setId(Id.pollId(Request.class));
		
	}
	
	public Message addServiceMessage(int serviceId, Message msg){
		return this.getServiceToMessage().put(serviceId, msg);
	}
	
	public int getId() {
		return id;
	}
	protected void setId(int id) {
		this.id = id;
	}
	public Map<Integer, Message> getServiceToMessage() {
		return serviceToMessage;
	}
	public void setServiceToMessage(Map<Integer, Message> serviceToMessage) {
		this.serviceToMessage = serviceToMessage;
	}
	
	
	
}
