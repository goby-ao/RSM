package main.message;

import akka.actor.ActorRef;

/**
 * Agent 详细信息
 * @author flyaos
 *
 */
public class AgentInfo {
	private String id; // 注册自动生成id 唯一标示 agent
	private String host; // Hostname 或者 ip
	private int port;
	private ActorRef actor; // 可以与该 agent 通信的 actor
	private String osType;

	// private String publicAddress;

	public AgentInfo() {
	}
	
	public AgentInfo(String id, String host, int port, ActorRef actor, String os) {
		super();
		this.id = id;
		this.host = host;
		this.port = port;
		this.actor = actor;
		this.osType = os;
	}

	public void setId(String id) {
		this.id = id;
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

	public ActorRef getActor() {
		return actor;
	}

	public void setActor(ActorRef actor) {
		this.actor = actor;
	}
	
	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "AgentInfo [id=" + id + ", host=" + host + ", port=" + port
				+ ", actor=" + actor + ", osType=" + osType + "]";
	}

}
