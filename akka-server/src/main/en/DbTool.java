package main.en;

import java.util.List;

import main.message.ServiceRoleStatus;
import main.message.Command.CmdType;
import main.message.ExecutorState.State;
import main.server.SpringCTXBeans;

import com.tsinghuabigdata.thm.server.model.HostServiceRole;
import com.tsinghuabigdata.thm.server.model.ServiceRole;
import com.tsinghuabigdata.thm.server.service.agentService.IServiceRoleService;
import com.tsinghuabigdata.thm.server.service.agentService.IServicesService;
import com.tsinghuabigdata.thm.server.service.host.IHostService;

/**
 * Tool for updating DataBase 
 * @author flyaos
 *
 */
public class DbTool {
	
    public IServiceRoleService serviceRoleService = (IServiceRoleService) SpringCTXBeans.getBean("serviceRoleService");
    public IServicesService servicesService = (IServicesService) SpringCTXBeans.getBean("servicesService");
    public IHostService hostService = (IHostService) SpringCTXBeans.getBean("hostService");
	
	public static DbTool instance = null;
	
	public synchronized static DbTool getSingleton() {
		if(instance == null) {
			return new DbTool();
		}
		else{
			return instance;
		}
	}
	
    /**
     * Database operate | Table: [services]
     * <p/>
     * Update the service status
     * Only all the service roles' status is "started" that the service status is start
     *
     * @param serviceId service id
     */
    void updateServiceStatus(String serviceId) {
        List<ServiceRole> serviceRoles = serviceRoleService.getRoles(serviceId);
        Boolean serviceStatus = true;

        for (ServiceRole role : serviceRoles) {
        	String roleId = role.getServiceRoleId(); // get roleId
            String status = serviceRoleService.findServiceRoleById(roleId).getStatus().toLowerCase();
            if("stopped".equals(status)) {
            	serviceStatus = false;
            	break;
            }
        }

        if (serviceStatus) {
            servicesService.updateServiceStatus(serviceId, ServiceRoleStatus.Started.toString());
        } else {
            servicesService.updateServiceStatus(serviceId, ServiceRoleStatus.Stopped.toString());
        }

    }

    /**
     * Database operate | Table:[serviceRole]
     * <p/>
     * Update the ServiceRole status according to roles' status on every host
     * The whole ServiceRoleStatus can update to "Started" need at least one "Started" role
     *
     * @param serviceRoleId service role id
     */
    public void updateRoleStatus(String serviceRoleId) {
        List<HostServiceRole> list = serviceRoleService.findByServiceRoleId(serviceRoleId);
        ServiceRoleStatus serviceRoleStatus = ServiceRoleStatus.Stopped;
        for (HostServiceRole hostServiceRole : list) {
            String status = hostServiceRole.getStatus().toLowerCase();
            if ("started".equalsIgnoreCase(status)) {
                serviceRoleStatus = ServiceRoleStatus.Started;
                break;
            }
        }
        serviceRoleService.updateServiceRoleStatus(serviceRoleStatus.toString(), serviceRoleId);
    }

    /**
     * Database operate | [Table:host_service_role]
     * <p/>
     * Update the operate status and role status
     *
     * @param id         serviceRoleId
     * @param opType     operate type
     * @param execStatus executor status
     */
    public void updateRoleAndOperateStatus(String id, String opType, String execStatus) {
        String operateInfo = opType.toUpperCase() + "_" + execStatus.toUpperCase();
        String a = execStatus;
        serviceRoleService.changeOperateStatus(id, operateInfo); // update the operate status
        if (execStatus.equals(State.SUCCESS.toString())) { // update the role status if success
            if (opType.equals(CmdType.START.toString()) || opType.equals(CmdType.RESTART.toString())) {
                serviceRoleService.changeRoleStatus(id, ServiceRoleStatus.Started.toString());
            } else if (opType.equals(CmdType.STOP.toString())) {
                serviceRoleService.changeRoleStatus(id, ServiceRoleStatus.Stopped.toString());
            }
        }else {
        	System.out.println("Warnning: Task: " + execStatus);
        }
    }

    /**
     * Database operate | Table:[host]
     * <p/>
     * Update the heartbeat
     *
     * @param host host_ip
     */
    public void updateHeatBeat(String host) {
        Long lastHeartBeatTime = System.currentTimeMillis();
        hostService.updateHeartBeat(host, lastHeartBeatTime);
    }

    /**
     * update registation infomation
     * @param host
     * @param osType
     * @param lastRegisterTime
     */
    public void updateRegistation(String host, String osType, Long lastRegisterTime) {
    	hostService.updateRegistation(host, osType, lastRegisterTime);
    }

    /**
     * update agent status (alive or dead)
     * @param host
     * @param status
     */
	public void updateAgentStatus(String host, String status) {
		hostService.updateAgentStatus(host, status);
		
	}
	
	/**
	 * find hostServiceRoleId
	 * @param hostId
	 * @param serviceRoleId
	 * @return
	 */
	public String findByHostIdAndServiceRoleId(String hostId, String serviceRoleId) {
		return serviceRoleService.findByHostIdAndServiceRoleId(hostId, serviceRoleId);
	}

	/**
	 * update operate status
	 * @param id
	 * @param operateStatus
	 */
	public void changeOperateStatus(String id, String operateStatus) {
		serviceRoleService.changeOperateStatus(id, operateStatus);
		
	}

}
