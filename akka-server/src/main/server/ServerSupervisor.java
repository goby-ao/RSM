package main.server;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import main.server.Worker;
import main.message.AgentInfo;
import main.message.RegisterAgent;
import scala.Option;
import scala.concurrent.duration.Duration;

import java.util.HashMap;

import static akka.actor.SupervisorStrategy.*;


/**
 * IntelliJ IDEA
 * Created by flyaos on 2014/3/27.
 */
public class ServerSupervisor extends UntypedActor {

    public static HashMap<String, AgentInfo> hostToAgent = new HashMap<String, AgentInfo>();
//    private static SupervisorStrategy strategy = new OneForOneStrategy(10,
//            Duration.create("1 minute"), new Function<Throwable, SupervisorStrategy.Directive>() {
//        @Override
//        public SupervisorStrategy.Directive apply(Throwable t) {
//            if (t instanceof ArithmeticException) {
//                return resume();
//            } else if (t instanceof NullPointerException) {
//                return restart();
//            } else if (t instanceof IllegalArgumentException) {
//                return stop();
//            } else {
//                return escalate();
//            }
//        }
//    }
//    );
    public final LoggingAdapter Log = Logging.getLogger(getContext().system(), this);

//    @Override
//    public SupervisorStrategy supervisorStrategy() {
//        return strategy;
//    }

    @Override
    public void preRestart(Throwable reason, Option<Object> option){

    }

    /**
     * deal with register information
     *
     * @param o the message received from agent
     * @throws Exception
     */
    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof RegisterAgent) {
            RegisterAgent agent = (RegisterAgent) o;

            ActorRef sender = getSender();
            AgentInfo agentInfo = getRegisterInfo(agent, sender);
            ActorRef worker = getContext().actorOf(Props.create(Worker.class), agent.getAgentId());

            if (hostToAgent.containsKey(agent.getHost())) {
                hostToAgent.remove(agent.getHost());
            }

            saveRemoteActor(agentInfo);
            worker.tell(agentInfo, getSelf());
            getContext().watch(worker);
            
        }
        /** greet message from application for testing the start of akka server*/
        else if (o instanceof String) {
            System.out.println("[Server]:" + o);
        }else if(o instanceof Object){
        	System.out.println(Object.class.getCanonicalName());
        	
        }
    }

    /**
     * AgentInfo Object which is used by worker actor
     *
     * @param agent RegisterAgent agent
     * @param actor remote actor
     * @return
     */
    public AgentInfo getRegisterInfo(RegisterAgent agent, ActorRef actor) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setActor(actor);
        agentInfo.setHost(agent.getHost());
        agentInfo.setId(agent.getAgentId());
        agentInfo.setPort(agent.getPort());
        agentInfo.setOsType(agent.getOsType());
        return agentInfo;
    }

    /**
     * save agent in server
     *
     * @param agentInfo detail agent information
     */
    public void saveRemoteActor(AgentInfo agentInfo) {
        hostToAgent.put(agentInfo.getHost(), agentInfo);
    }

}