package main.message;

import java.io.Serializable;
import java.util.Queue;

/**
 * Server传到Agent端的命令队列
 * @author flyaos
 *
 */
public class CommandQueue implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Queue<Command> cmdQueue;


	public CommandQueue(Queue<Command> cmdQueue) {
		super();
		this.cmdQueue = cmdQueue;
	}

	public Queue<Command> getCmdQueue() {
		return cmdQueue;
	}

	public void setCmdQueue(Queue<Command> cmdQueue) {
		this.cmdQueue = cmdQueue;
	}
	
	
	
}
