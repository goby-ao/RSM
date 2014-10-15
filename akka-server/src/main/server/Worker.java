package main.server;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import main.message.*;
import main.message.Command.CmdType;
import main.message.ExecutorState.State;
import main.server.ServerSupervisor;
import main.server.TaskQueue;
import main.server.DBTool;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * IntelliJ IDEA
 * Created by flyaos on 2014/3/27.
 * 
 * Worker，与 agent 点对点通信
 */
public class Worker extends UntypedActor {

    public final LoggingAdapter Log = Logging.getLogger(getContext().system(), this);
    public ActorRef remoteActor;
    private DBTool dbTool; // 更新数据库操作的对象

    /**
     * host与 agent映射的Map对象
     * TODO 持久化
     */
    private HashMap<String, AgentInfo> hostToAgent;

    /** 创建worker actor都会先执行preStart()方法  */
    @Override
    public void preStart() {
        this.hostToAgent = ServerSupervisor.hostToAgent;
        this.dbTool = DBTool.getSingleton();
    }

    @Override
    public void onReceive(Object o) throws Exception {
        /** 收到 agent 的注册消息 */
        if (o instanceof AgentInfo) {
            AgentInfo agentInfo = (AgentInfo) o;
            this.remoteActor = agentInfo.getActor();
            remoteActor.tell(new RegisterSuccess("Success"), getSelf());
            
            String host = agentInfo.getHost();
            String osType = agentInfo.getOsType();
            long lastRegisterTime = System.currentTimeMillis();
            dbTool.updateRegistation(host, osType, lastRegisterTime);   
            dbTool.updateAgentStatus(host, "alive");
        }

        /** 收到执行结果消息 */
        else if (o instanceof ExecutorStateChanged) {
            ExecutorStateChanged executeState = (ExecutorStateChanged) o;
            Command cmd = executeState.getCmd();
            String host = executeState.getHost();
            State executorStatus = executeState.getExecutorState();
            CmdType operateType = cmd.cmdType;

            dbTool.updateHeatBeat(host); //update heartbeat

            String serviceRoleServiceId = null;
            try {
                serviceRoleServiceId = dbTool.findByHostIdAndServiceRoleId(
                        cmd.getHostId(), cmd.getServiceRoleId());
            } catch (NullPointerException e) {
                Log.error("id is null");
            }
            Log.info("serviceRoleServiceId is : " + serviceRoleServiceId);

            if (serviceRoleServiceId != null) {
            	dbTool.updateRoleAndOperateStatus(serviceRoleServiceId, operateType.toString(), executorStatus.toString());
            }

            if (isTaskFinished(executorStatus)) {
            	dbTool.updateRoleStatus(cmd.getServiceRoleId()); // 更新服务角色状态
            	dbTool.updateServiceStatus(cmd.getServiceId());  // 更新服务状态
                pullTaskAndSend(host); // 如果任务存在的话，从任务队列取任务发送
            }
        }

        /** 收到空消息，即 agent 空闲，等待任务中 */
        else if (o instanceof NoExecutorResult) {
            NoExecutorResult msg = (NoExecutorResult) o;
            Log.info("[Server]:NoExecutorResult: " + msg.getMessage());
            if (isTaskFinished(msg.executorState)) {
            	pullTaskAndSend(msg.getHost());
                System.out.println("[Server]:send new task to host: " + msg.getHost());
            }
            // 每次心跳更新 host 表里的心跳信息
            dbTool.updateHeatBeat(msg.getHost());
        }
        else if (o instanceof Terminated) {
            Log.warning("----------------------terminated-----------------------------");
            postRestart(new Exception("actor is terminated"));
            throw new Exception("actor is terminated");
        } else {
            unhandled(o);
        }
    } // onRecive() end

    /**
     * 根据 host 从任务队列取任务并发送
     *
     * @param host host_ip
     */
    public void pullTaskAndSend(String host) {
        TaskQueue taskQueue = new TaskQueue();
        Map<String, Queue<Command>> taskMap = TaskQueue.hostQueues;
        if (taskMap.get(host) != null && !taskMap.get(host).isEmpty()) {
            if (hostToAgent != null && hostToAgent.get(host) != null
                    && hostToAgent.get(host).getActor() != null) {
                ActorRef remoteActor = hostToAgent.get(host).getActor();
                // 从队列中出队一个任务
                if (taskMap.get(host).peek() != null) {
                    Log.info("[Server]:the task to send: " + taskMap.get(host).peek().toString());
                    Command cmd = taskMap.get(host).peek();
                    remoteActor.tell(cmd, remoteActor);
                    try{
                    	String id = dbTool.findByHostIdAndServiceRoleId(cmd.getHostId(), cmd.getServiceRoleId());
                    	dbTool.changeOperateStatus(id, "Waiting");
                    }catch(NullPointerException e) {
                    	e.printStackTrace();
                    	Log.error("id is null");
                    }
                    taskQueue.dequeue(host);
                }
            } else {
                Log.warning("[Server]: no actor");
            }

        } else {
            Log.warning("[Server]: agent not exist in server ");
        }
    }

    /**
     * 判断任务是否执行结束
     * <p/>
     * TODO error handle
     * 目前对 failed 和 error 没有进行处理
     *
     * @param executorState executor status
     * @return boolean state
     */
    public boolean isTaskFinished(State executorState) {
        return !executorState.equals(State.LAUNCHING);
    }

}
