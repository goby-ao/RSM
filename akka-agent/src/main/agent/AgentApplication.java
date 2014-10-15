package main.agent;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

// 测试 agent 主动注册 server
public class AgentApplication {

	// main 参数列表
	public static void main(String[] args) {
		Config config = ConfigFactory.load();
		ActorSystem system = ActorSystem.create("AgentSystem",ConfigFactory.load().getConfig("AgentSys").withFallback(config));
		system.actorOf(Props.create(AgentActor.class, system));

	}

}
