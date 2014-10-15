package main.message;

/**
 * Command 执行状态 
 * @author flyaos
 *
 */
public class ExecutorState {
	public enum State {
		WAITING,LAUNCHING, SUCCESS, FAILED, ERROR
	}

	private State executorState;

	public State getExecutorState() {
		return executorState;
	}

	public void setExecutorState(State executorState) {
		this.executorState = executorState;
	}

	public ExecutorState(State executorState) {
		super();
		this.executorState = executorState;
	}


}
