package main.server.service;

import java.util.logging.Logger;

import main.server.ServerApplication;
import main.server.TaskQueue;

public class AkkaInit extends Thread {

	private static final Logger logger = Logger.getLogger(AkkaInit.class.getName());

	@Override
	public void run() {
		logger.info("akka server starting ....");
		ServerApplication system = new ServerApplication();

		TaskQueue taskQueue = new TaskQueue();
		taskQueue.init();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("akka server is started");
	}

}