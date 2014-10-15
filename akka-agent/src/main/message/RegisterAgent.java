package main.message;

import java.io.Serializable;

/**
 * Agent 注册信息
 * @author flyaos
 *
 */
public class RegisterAgent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String agentId;
	private String host;
	private int port;
	private String osType;
	
	public RegisterAgent(String agentId, String host, int port ,String osType) {
		super();
		this.agentId = agentId;
		this.host = host;
		this.port = port;
		this.osType = osType;
	}

	
	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getOsType() {
		return osType;
	}
	
	
}
