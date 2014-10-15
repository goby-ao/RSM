package main.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

//import org.apache.log4j.Logger;

import main.message.Command;


/**
 * 任务队列 
 * 类型：HashMap<String Queue<Command>>
 * 说明：一个 host 对象一个 Command Queue
 * @author flyaos
 */
 
public class TaskQueue {

//	private static Logger logger = Logger.getLogger(TaskQueue.class);
	
	// 静态变量-新增任务，分发任务,删除任务都要读取
	public static Map<String, Queue<Command>> hostQueues = new HashMap<String, Queue<Command>>();
	
	public void init(){
//		hostQueues = new HashMap<String, Queue<Command>>();
//		logger.debug("init the hostQueue");
	}
	

	// 取队列，同步
	private  Queue<Command> getQueue(String hostname) {
		return hostQueues.get(hostname);
	}

	// 根据 hostname 新增一个队列
	private static void addQueue(String hostname, Queue<Command> q) {
		hostQueues.put(hostname, q);
	}

	public void enqueue(String hostname, Command cmd) {
		Queue<Command> q;
		synchronized (this) {
			// 先判断 hostname 的队列是否存在，若不存在则新建一个队列
			if(getQueue(hostname) == null){
				addQueue(hostname, new LinkedList<Command>());
				q = getQueue(hostname);
			}else {
				
			q = getQueue(hostname);
			if (q == null) {
				addQueue(hostname, new LinkedList<Command>());
				q = getQueue(hostname);
			}	
		}
	}

		synchronized (q) {
			// 若队列存在，则判断 cmd 是否在队列中，不存在时 push 进队列,排除同时对一个 host 进行多个相同的操作
			if (q.contains(cmd)) {
//				logger.warn("cmd already exists in the queue, will not adding again");
				return;
			}
			q.add(cmd);
		}
	}

	public Command dequeue(String hostname) {
		Queue<Command> q = getQueue(hostname);
		if (q == null) {
			return null;
		}
		synchronized (q) {
			if (q.isEmpty()) {
				return null;
			} else {
				return q.remove();
			}
		}
	}

	public int size(String hostname) {
		Queue<Command> q = getQueue(hostname);
		if (q == null) {
			return 0;
		}
		synchronized (q) {
			return q.size();
		}
	}

	public List<Command> dequeueAll(String hostname) {
		Queue<Command> q = getQueue(hostname);
		if (q == null) {
			return null;
		}
		List<Command> l = new ArrayList<Command>();
		synchronized (q) {
			while (true) {
				try {
					Command cmd = q.remove();
					if (cmd != null) {
						l.add(cmd);
					}
				} catch (NoSuchElementException ex) {
					return l;
				}
			}
		}
	}

}
