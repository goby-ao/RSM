package main.message;

import java.io.Serializable;

import main.message.ExecutorState.State;

/**
 * 命令执行中的状态变化
 * @author flyaos
 *
 */
public class ExecutorStateChanged  extends ServiceInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String serviceState;
	private String serviceRoleState;
	private State executorState;
	private Command cmd;
	
	public ExecutorStateChanged(Command cmd,String serviceState,String serviceRoleState,State executorState) {
		super(cmd.getHost(),cmd.getService(),cmd.getServiceRole());
		this.cmd = cmd;
		this.serviceState = serviceState;
		this.serviceRoleState = serviceRoleState;
		this.executorState = executorState;
	}


	public String getserviceState() {
		return serviceState;
	}

	public void setserviceState(String serviceState) {
		this.serviceState = serviceState;
	}

	public void setExecutorState(State executorState) {
		this.executorState = executorState;
	}

	public State getExecutorState() {
		return executorState;
	}

	public void setServiceRoleState(String serviceRoleState) {
		this.serviceRoleState = serviceRoleState;
	}

	public String getServiceRoleState() {
		return serviceRoleState;
	}


	public void setCmd(Command cmd) {
		this.cmd = cmd;
	}


	public Command getCmd() {
		return cmd;
	}

}
