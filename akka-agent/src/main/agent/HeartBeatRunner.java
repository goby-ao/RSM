package main.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import akka.actor.ActorRef;

import main.agent.ExecutorRunner;
import main.message.Command;
import main.message.ExecutorState;
import main.message.ExecutorStateChanged;
import main.message.NoExecutorResult;
import main.message.ExecutorState.State;

/**
 * 心跳进程 定时读取 ExecutorRunner 类里的 executorResultBuffer
 * 
 * @author flyaos
 * 
 */
public class HeartBeatRunner implements Runnable {

	private static Log LOG = LogFactory.getLog(HeartBeatRunner.class);
	private ActorRef serverActor; // Server端actor

	public HeartBeatRunner(ActorRef serverActor) {
		this.serverActor = serverActor;
	}
	private volatile boolean isRunning = true;
	
	@Override
	public void run() {
		while (isRunning) {
			if (ExecutorResult.result != "") {
				LOG.info("The executor result read is " + ExecutorResult.result);
				System.out.println("[HeartBeat runner]: the result is  " + ExecutorResult.result);
				
				Command command = ExecutorRunner.cmd;
				State executorState;

				if (ExecutorResult.result.contains("error")) {
					// Shell 发生错误
					LOG.info("Task: " + command.getCmdType().toString() + " " +command.getServiceRole()
							+ " error occur");
					executorState = State.ERROR;
				} else if (ExecutorResult.result.contains("success")) {
					// 任务执行成功
					LOG.info("Task: " + command.getCmdType().toString() + " " + command.getServiceRole()
							+ " success");
					executorState = State.SUCCESS;
				} else {
					// 任务执行失败
					LOG.info("Task: " + command.getCmdType().toString() + " " + command.getServiceRole() 
							+ " failed");
					executorState = State.FAILED;
				}
				// 给Server发送执行情况
				ExecutorStateChanged msg = new ExecutorStateChanged(command, "serviceState",
						"serviceRoleState", executorState);
				serverActor.tell(msg, AgentActor.selfActor);
				AgentActor.executorState = executorState;
				ExecutorResult.result = "";

			} else {
				AgentActor.serverActor.tell(new NoExecutorResult(AgentActor.localIp,
						AgentActor.executorState, "new task"), AgentActor.selfActor);
				LOG.info("The executor result is null now, and the executorstate is "
						+ AgentActor.executorState);
				LOG.info("Send NoExecutorResult message to Server");
				LOG.info("[Agent]: [no task], send NoExecutorResult message,executorState: "
						+ AgentActor.executorState.toString());
			}

			// 每隔3秒读一次
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void kill() {
		isRunning = false;
		System.out.println("stop heartbeat thread");
	}
	public void setServerActor(ActorRef serverActor) {
		this.serverActor = serverActor;
	}

	public ActorRef getServerActor() {
		return serverActor;
	}

}