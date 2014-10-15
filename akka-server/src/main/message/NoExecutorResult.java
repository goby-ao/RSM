package main.message;

import java.io.Serializable;

import main.message.ExecutorState.State;

public class NoExecutorResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String message;
	private String host;
	public State executorState;

	public NoExecutorResult(String host,State executorState, String message) {
		super();
		this.host = host;
		this.message = message;
		this.executorState = executorState;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
