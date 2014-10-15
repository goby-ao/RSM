package main.server;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

// For example, using the akka script an application can be started with the following at the command line:
// bin/akka org.app.ServerApplication

public class ServerApplication {

	public ActorSystem system;

	public ServerApplication() {

		// 读取配置文件，创建名为ThmServer的 ActorSystem
		final ActorSystem system = ActorSystem.create("ThmServer",ConfigFactory.load().getConfig("ServerSys"));

		// 创建Server端actor
		ActorRef ServerActor = system.actorOf(Props.create(ServerSupervisor.class), "ServerActor");
		ServerActor.tell("hello akka",null);
		System.out.println("[Server]: " + ServerActor.path());
		System.out.println("[Server]: server is ready");
		
		// 检查Agent和主机状态
		CheckAgentStatus checkThread = new CheckAgentStatus("CheckHearbeat");
		Thread checkAgentStatus = new Thread(checkThread);
		checkAgentStatus.start();
	}

}
