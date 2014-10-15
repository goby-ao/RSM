package main.server.service;

import java.util.TimerTask;

import main.server.TaskQueue;

/**
 * 已过时
 * @author flyaos
 *
 */
public class TaskManager extends TimerTask {

	private TaskQueue taskQueue;

	public TaskManager() {
		this.taskQueue = new TaskQueue();
	}

	/**
	// 遍历 Map,执行任务
	public void run() {
		// 对整个 HashMap 做同步
		synchronized (TaskQueue.hostQueues) {
			System.out.println("[Print]: the map size is :" + TaskQueue.hostQueues.size());
			if (!TaskQueue.hostQueues.isEmpty()) {
				for (String host : TaskQueue.hostQueues.keySet()) {
					if (TaskQueue.hostQueues.get(host) != null
							&& !TaskQueue.hostQueues.get(host).isEmpty()) {
						Queue<Command> commands = TaskQueue.hostQueues.get(host);
						System.out.println("[Print]: the host cmd queue size is: " + commands.size());

						// 根据key值host，从ServerActor 的 hostToAgent 中获取远程的 agent actor
						System.out.println("[Server]:ServerActor.hostToAgent :" + ServerActor.hostToAgent);
						
						if (ServerActor.hostToAgent != null
								&& ServerActor.hostToAgent.get(host) != null
								&& ServerActor.hostToAgent.get(host).getActor() != null) {
							ActorRef remoteActor = ServerActor.hostToAgent.get(host).getActor();
							
							// 从任务队列中出队一个任务
							if (commands.peek() != null) {
								System.out.println("hahahahahaaaaaaaaaaaaaaaaaaaaaa");
								remoteActor.tell(commands.poll(), remoteActor);
								taskQueue.dequeue(host);
							}

						}else {
							System.out.println("[Server]: actor is null in ServerActor, waitting for registration of agent");
						}
					}

				}
			}else {
				System.out.println("[Server]: taskQueue is empty");
			}

		}
	}

	*/
	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
