package main.server.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.tsinghuabigdata.thm.server.service.agentService.IServiceRoleService;

import main.message.Command;
import main.server.SpringCTXBeans;
import main.server.TaskQueue;

public class AkkaServerImpl implements IAkkaService {

	public IServiceRoleService serviceRoleService = (IServiceRoleService) SpringCTXBeans.getBean("serviceRoleService");;

	/**
	 * 提供给前台的接口,发送命令,写入HashMap,TaskQueue中的静态变量 hostQueue.
	 */
	@Override
	public void sendCommand(Command cmd) {
		TaskQueue taskQueue = new TaskQueue();

		// 这里可以改状态为 
		try{
			String id = serviceRoleService.findByHostIdAndServiceRoleId(cmd.getHostId(), cmd.getServiceRoleId());
			serviceRoleService.changeOperateStatus(id, "Waiting for agent");
		}catch(NullPointerException e) {
			e.printStackTrace();
			System.out.println("id is Null");
		}

		// 启动脚本进程需要的args
		List<String> operateParams = new ArrayList<String>();
		operateParams.add("-t");
		operateParams.add(cmd.getServiceRole());
		operateParams.add("-o");
		operateParams.add(cmd.cmdType.toString().toLowerCase());
		cmd.setOperateParams(operateParams);

		taskQueue.enqueue(cmd.getHost(), cmd);
	}

	@Override
	public String checkHostStatus(String host) {
		// TODO Auto-generated method stub
		return null;
	}
}
