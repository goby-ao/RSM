package main.en;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * IntelliJ IDEA
 * Created by flyaos on 2014/3/27.
 */
public class Worker extends UntypedActor {

    public final LoggingAdapter Log = Logging.getLogger(getContext().system(), this);
    public ActorRef remoteActor;
    private HashMap<String, AgentInfo> hostToAgent;
    private DbTool dbTool; // update DataBase tool object

    @Override
    public void preStart() {
        this.hostToAgent = ServerSupervisor.hostToAgent;
        this.dbTool = DbTool.getSingleton();
    }

    @Override
    public void onReceive(Object o) throws Exception {
        /** get register request from agent */
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

        /** get executor result */
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
            	dbTool.updateRoleStatus(cmd.getServiceRoleId()); // update service role status
            	dbTool.updateServiceStatus(cmd.getServiceId());  // update service status
                pullTaskAndSend(host); // send a new task if any
            }
        }

        /** get empty result (heartbeat) which means the agent is waiting for a task */
        else if (o instanceof NoExecutorResult) {
            NoExecutorResult msg = (NoExecutorResult) o;
            Log.info("[Server]:NoExecutorResult: " + msg.getMessage());
            // Choose a task to send only when the task is finished
            if (isTaskFinished(msg.executorState)) {
            	pullTaskAndSend(msg.getHost());
                System.out.println("[Server]:send new task to host: " + msg.getHost());
            }	
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
     * Send a task according to the host info
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
                // get a task from the queue
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
     * Check whether the task is finished
     * <p/>
     * TODO error handle
     * for now we doing nothing when executor is failed or error
     *
     * @param executorState executor status
     * @return boolean state
     */
    public boolean isTaskFinished(State executorState) {
        return !executorState.equals(State.LAUNCHING);
    }

}
