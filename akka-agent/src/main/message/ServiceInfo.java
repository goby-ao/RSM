package main.message;

import java.io.Serializable;

public class ServiceInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String host;
	private String service;
	private String serviceRole;

	public ServiceInfo(String host2, String service2, String serviceRole2) {
		this.host = host2;
		this.service = service2;
		this.serviceRole = serviceRole2;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getServiceRole() {
		return serviceRole;
	}

	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}

}
