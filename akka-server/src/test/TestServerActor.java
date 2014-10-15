package test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import main.message.Command;
import main.message.Command.CmdType;
import main.server.ServerApplication;
import main.server.TaskQueue;
import main.server.service.AkkaServerImpl;
import static main.server.TaskQueue.hostQueues;


public class TestServerActor {

	private static final Logger logger = Logger.getLogger(TestServerActor.class.getName());
	
	public static void main(String[] args) {
		
		RunServerThread serverThread = new RunServerThread();
		serverThread.start();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println("init akka queues size is : "+hostQueues.size());

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		AkkaServerImpl akkaServerImpl = new AkkaServerImpl();
		
		Command cmd = new Command();
		cmd.setCmdType(CmdType.START);
		cmd.setService("hdfs");
		cmd.setServiceRole("namenode");
		cmd.setHost("172.16.3.54");
		cmd.setHostId("7");
		cmd.setServiceId("3");
		cmd.setServiceRoleId("8");
		cmd.setWorkDir("/home/hadoop/akka-service");
		cmd.setShellName("operateHDFSProcess.sh");
		
		List<String> operateParams = new ArrayList<String>();
		operateParams.add("-t");
		operateParams.add(cmd.getServiceRole());
		operateParams.add("-o");
		operateParams.add(cmd.cmdType.toString().toLowerCase());
		cmd.setOperateParams(operateParams);
		
		akkaServerImpl.sendCommand(cmd);
		
	}
	
	
	
}

class RunServerThread extends Thread{

	private static final Logger logger = Logger.getLogger(RunServerThread.class.getName());
	@Override
	public void run() {
		logger.info("akka server starting ....");
		ServerApplication system = new ServerApplication();

		TaskQueue taskQueue = new TaskQueue();
		taskQueue.init();
		
		logger.info("akka server is started");
		
		
	}
	
}
