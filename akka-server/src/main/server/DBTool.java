package main.server;

import java.text.SimpleDateFormat;
import java.util.List;

import main.message.ServiceRoleStatus;
import main.message.Command.CmdType;
import main.message.ExecutorState.State;

import com.tsinghuabigdata.commons.server.cluster.model.LogInfo;
import com.tsinghuabigdata.commons.utils.UUID;
import com.tsinghuabigdata.thm.server.model.HostServiceRole;
import com.tsinghuabigdata.thm.server.model.ServiceRole;
import com.tsinghuabigdata.thm.server.model.Services;
import com.tsinghuabigdata.thm.server.model.host.Host;
import com.tsinghuabigdata.thm.server.service.ILogInfoService;
import com.tsinghuabigdata.thm.server.service.agentService.IServiceRoleService;
import com.tsinghuabigdata.thm.server.service.agentService.IServicesService;
import com.tsinghuabigdata.thm.server.service.host.IHostService;

/**
 * 数据库操作类
 * @author flyaos
 *
 */
public class DBTool {
	
    public IServiceRoleService serviceRoleService = (IServiceRoleService) SpringCTXBeans.getBean("serviceRoleService");
    public IServicesService servicesService = (IServicesService) SpringCTXBeans.getBean("servicesService");
    public IHostService hostService = (IHostService) SpringCTXBeans.getBean("hostService");
    public ILogInfoService logInfoService = (ILogInfoService) SpringCTXBeans.getBean("logInfoService");
	
	public static DBTool instance = null;
	
	public synchronized static DBTool getSingleton() {
		if(instance == null) {
			return new DBTool();
		}
		else{
			return instance;
		}
	}
	
    /**
     * 数据库操作  | 表: [services]
     * <p/>
     * 更新服务状态
     * 当服务下的所有角色都启动，则服务状态为启动。
     *
     * @param serviceId service id
     */
    void updateServiceStatus(String serviceId) {
        List<ServiceRole> serviceRoles = serviceRoleService.getRoles(serviceId);
        Boolean serviceStatus = true;

        for (ServiceRole role : serviceRoles) {
        	String roleId = role.getServiceRoleId();
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
     * 数据库操作  | 表:[serviceRole]
     * <p/>
     * 更新角色状态
     * 根据每一台主机上的角色状态来判断，如果有一个主机启动了，则为启动
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
     * 数据库操作  | 表: [host_service_role]
     * <p/>
     * 更新角色状态和操作状态
     *
     * @param id         serviceRoleId
     * @param opType     operate type
     * @param execStatus executor status
     */
    public void updateRoleAndOperateStatus(String id, String opType, String execStatus) {
        String operateInfo = opType.toUpperCase() + "_" + execStatus.toUpperCase();
        LogInfo logInfo = new LogInfo();
        String logInfoStr = "";
        
        logInfo.setLogId(UUID.generateUUID());
        long systemTime = System.currentTimeMillis();
        SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createTime = sim.format(systemTime);
        logInfo.setCreateTime(createTime);
        
        HostServiceRole hostServiceRole = serviceRoleService.findHostServiceRoleById(id);
        String hostId = hostServiceRole.getHostId();
        String ServiceRoleId = hostServiceRole.getServiceRoleId();
        
        Host host = hostService.findByHostId(hostId);
        ServiceRole serviceRole = serviceRoleService.findById(ServiceRoleId);
        
        logInfoStr = createTime+" 操作主机: "+host.getHostName()+" 下的服务角色: "+serviceRole.getName()+" "+operateInfo;
       
        logInfo.setLogInfo(logInfoStr);
        
        try {
			logInfoService.addLogInfo(logInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
        //TODO
        serviceRoleService.changeOperateStatus(id, operateInfo); // 更新操作状态
        
        if (execStatus.equals(State.SUCCESS.toString())) { // 如果成功则更新为Start
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
     * 数据库操作 | 表:[host]
     * <p/>
     * 更新心跳信息
     *
     * @param host host_ip
     */
    public void updateHeatBeat(String host) {
        Long lastHeartBeatTime = System.currentTimeMillis();
        hostService.updateHeartBeat(host, lastHeartBeatTime);
    }

    /**
     * 数据库操作  | 表:[host]
     * <p/>
     * 更新注册信息
     * @param host
     * @param osType
     * @param lastRegisterTime
     */
    public void updateRegistation(String host, String osType, Long lastRegisterTime) {
    	hostService.updateRegistation(host, osType, lastRegisterTime);
    }

    /**
     * 数据库操作  | 表:[host]
     * <p/>
     * 更新agent状态(alive 或者 dead)
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
	 * 数据库操作  | 表:[hostServiceRole]
	 * <p/>
	 * 更新操作状态
	 * @param id
	 * @param operateStatus
	 */
	public void changeOperateStatus(String id, String operateStatus) {
		serviceRoleService.changeOperateStatus(id, operateStatus);
		
	}

}
