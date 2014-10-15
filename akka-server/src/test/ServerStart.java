package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import akka.actor.ActorSystem;
import main.message.Command;
import main.message.Command.CmdType;
import main.server.ServerApplication;
import main.server.TaskQueue;

public class ServerStart {

	public static ActorSystem system;
	
	public static void main(String[] args) {
		
		// 启动服务，启动监听 
		ServerApplication system = new ServerApplication();

		// 启服务，启动监听，创建系统 actor
		// system = ActorSystem.create("ThmServer",ConfigFactory.load().getConfig("ServerSys"));
		// ActorRef ServerActor = system.actorOf(Props.create(ServerActor.class), "ServerActor");
		// ServerActor.tell("hello server",null);
		// System.out.println("[Server]: " + ServerActor.path());
		// System.out.println("[Server]: server is ready");

		// 初始化任务队列
		// 将 TaskQueue 开放给service，service可以新增任务，此时TaskManager会定时取 任务队列，分发任务
		TaskQueue taskQueue = new TaskQueue();
		taskQueue.init();
		

		
		// 定时去分发任务到 remote actor
		// 2s 后开始执行，每个3s重复执行一次
		// Timer timer = new Timer();
		// timer.schedule(taskManager, 2000, 3000);
		
		// 外部测试新增一个任务 
		System.out.println("[Server]: add 4 data to taskQueue\n");
		
		Command cmd = new Command("hbase","master","172.16.3.52",CmdType.START,"/home","operateHbaseProcess.sh","6","4","3");
		List<String> operateParams = new ArrayList<String>();
		
		operateParams.add("-h");
		operateParams.add("172.16.3.52");
		operateParams.add("-t");
		operateParams.add("master");
		operateParams.add("-o");
		operateParams.add("start");
		cmd.setOperateParams(operateParams);
		
		Command cmd2 = new Command("hbase","regionserver","172.16.3.52",CmdType.START,"/home","operateHbaseProcess.sh","6","4","4");
		List<String> operateParams2 = new ArrayList<String>();
		
		operateParams2.add("-h");
		operateParams2.add("172.16.3.52");	
		operateParams2.add("-t");
		operateParams2.add("regionserver");
		operateParams2.add("-o");
		operateParams2.add("start");
		cmd2.setOperateParams(operateParams2);
		
		Command cmd3 = new Command("hbase","regionserver","172.16.3.54",CmdType.START,"/home","operateHbaseProcess.sh","7","4","4");
		List<String> operateParams3 = new ArrayList<String>();
		
		operateParams3.add("-h");
		operateParams3.add("172.16.3.54");	
		operateParams3.add("-t");
		operateParams3.add("regionserver");
		operateParams3.add("-o");
		operateParams3.add("start");
		cmd3.setOperateParams(operateParams3);
		
		Command cmd4 = new Command("hbase","master","172.16.3.56",CmdType.START,"/home","operateHbaseProcess.sh","8","4","3");
		List<String> operateParams4 = new ArrayList<String>();
		
		operateParams4.add("-h");
		operateParams4.add("172.16.3.56");	
		operateParams4.add("-t");
		operateParams4.add("master"); 
		operateParams4.add("-o");
		operateParams4.add("start");
		cmd4.setOperateParams(operateParams4);
		
		Command cmd5= new Command("hbase","regionserver","172.16.3.56",CmdType.START,"/home","operateHbaseProcess.sh","8","4","4");
		List<String> operateParams5 = new ArrayList<String>();
		
		operateParams5.add("-h");
		operateParams5.add("172.16.3.56");	
		operateParams5.add("-t");
		operateParams5.add("regionserver");
		operateParams5.add("-o");
		operateParams5.add("start");
		cmd5.setOperateParams(operateParams5);
		
		taskQueue.enqueue("172.16.3.52", cmd);
		taskQueue.enqueue("172.16.3.52", cmd2);
		taskQueue.enqueue("172.16.3.54", cmd3);
		taskQueue.enqueue("172.16.3.56", cmd4);
		taskQueue.enqueue("172.16.3.56", cmd5);

		System.out.println(" size: "+TaskQueue.hostQueues.size());
		
	}
}
