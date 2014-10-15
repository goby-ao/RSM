package main.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import main.message.Command;
import main.message.ExecutorState;
import main.message.ExecutorStateChanged;
import main.message.RegisterAgent;
import main.message.RegisterFailed;
import main.message.RegisterSuccess;
import main.message.ExecutorState.State;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.remote.DisassociatedEvent;
import akka.remote.RemotingLifecycleEvent;
import akka.util.Timeout;

public class AgentActor extends UntypedActor {

	private final static Log LOG = LogFactory.getLog(AgentActor.class);// 日志文件在
																		// /home/hadoop/akka-server/log

	private String serverIp; // server ip
	private String serverPort; // server 端绑定端口号

	private String agentId;
	private ActorSystem system; // System Actor

	public static String localIp; // local ip
	public static ActorRef serverActor; // 远程通信的 Server actor
	public static ActorRef selfActor;
	public static State executorState; // 当前执行状态

	public Thread heartBeat = null;
	private HeartBeatRunner runnable = null;

	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
	Timeout timeout = new Timeout(3000);

	public AgentActor(ActorSystem system) throws Exception {
		this.system = system;
		preStart();
	}

	@Override
	public void preStart() throws IOException, Exception {

		/**
		 * 初始化 Server 端 IP 和Local IP 从当前目录下的 basic.properties 中读取 Server 端 IP 地址
		 */
		Properties properties = new Properties();
		File file = new File("basic.properties");
		FileInputStream is = new FileInputStream(file);
		properties.load(is);
		is.close();

		this.serverPort = properties.getProperty("ServerPort");
		this.serverIp = properties.getProperty("ServerIp");
		AgentActor.localIp = properties.getProperty("LocalIp");

		system.eventStream().subscribe(getSelf(), RemotingLifecycleEvent.class);
		LOG.info("Server IP is : " + this.serverIp);

		ExecutorResult result = new ExecutorResult(); // 初始化输出结果，默认为空
		AgentActor.executorState = State.WAITING; // 初始化执行状态为「等待任务」
		selfActor = getSelf();// 初始化selfAcotor
		connectToServer();// 向Server端注册
	}

	@Override
	public void preRestart(Throwable reason, scala.Option<Object> message) throws Exception {
		System.out.println("==============preRestat===============");
		super.preRestart(reason, message);
		System.out.print("agent is restart due to " + message);
	}

	public void connectToServer() {

		agentId = generateAgentId(); // 生成唯一的AgentID
		Properties props = System.getProperties(); // 获得系统属性集
		String osType = props.getProperty("os.name"); // 操作系统名称

		RegisterAgent registerAgent = new RegisterAgent(agentId, localIp, 2552, osType);
		String akkaUrl = toAkkaUrl(serverIp, serverPort);// 根据服务端ip和端口号获取akka
															// actor url

		// 如果Server服务没有开启，自动注册重试,akka 配置文件里有默认的重试次数
		// TODO 这里可以改成 future ask 模式
		ActorSelection server = system.actorSelection(akkaUrl);
		server.tell(registerAgent, getSelf());

		LOG.info("akka url: " + akkaUrl);
		LOG.info("Send resistration info to server, agentId:" + agentId);
		LOG.info("start to connect to server...");

	}

	@Override
	public void onReceive(Object message) throws Exception {

		// 收到注册成功的消息
		if (message instanceof RegisterSuccess) {
			LOG.info("Successfully registered with server");

			AgentActor.serverActor = getSender(); // 保存可以与server通信的 sender

			// 注册成功，启动心跳信息线程
			runnable = new HeartBeatRunner(serverActor);
			heartBeat = new Thread(new HeartBeatRunner(serverActor));
			heartBeat.start();

			// system.scheduler().scheduleOnce(Duration.create(50,
			// TimeUnit.MILLISECONDS),
			// serverActor.anchor(), "agent is alive", system.dispatcher(),
			// null);
		}
		if (message instanceof RegisterFailed) {
			LOG.info("Has already registered");
		}

		// 收到执行命令
		if (message instanceof Command) {

			executorState = State.LAUNCHING;
			Command cmd = (Command) message;

			LOG.info("Get Command from server");
			LOG.info("Get Cmd: " + cmd.toString());

			// 开线程执行命令
			Thread executor = new Thread(new ExecutorRunner(cmd));
			executor.start();

			// 发送 cmd 正在执行消息
			ExecutorStateChanged stateChanged = new ExecutorStateChanged(cmd, null, null, ExecutorState.State.LAUNCHING);
			getSender().tell(stateChanged, getSelf());

		}
		// server 端出现问题
		if (message instanceof DisassociatedEvent) {

			LOG.info("------------Server is shutdown-----------");
			postStop();
			/*if (heartBeat != null) {
				runnable.kill();
				heartBeat.join();
			}*/
			preStart();
			LOG.info("------------restart agent and reconnect to server-----------");
		}
	}

	// 转为akka actor通信的url
	public static String toAkkaUrl(String serverIp, String serverPort) {

		String systemName = "ThmServer";
		String actorName = "ServerActor";
		String akkaUrl = String.format("akka.tcp://%s@%s:%s/user/%s", systemName, serverIp, serverPort, actorName);
		return akkaUrl;

	}

	// 生成唯一的 agent id
	public String generateAgentId() {
		return String.format("agent-%s-%s-%s", DATE_FORMAT.format(new Date()), localIp, serverPort);
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getAgentId() {
		return agentId;
	}

}
