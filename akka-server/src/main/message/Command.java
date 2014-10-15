package main.message;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * 发送的命令信息
 * @author flyaos
 *
 */
public class Command implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String service;  // 服务
	private String serviceRole; // 服务角色
	private String host; // 所在主机
	private String workDir; // 要启动是 shell 脚本所在目录
	private String shellName; //shell 文件名

	private String hostId ; // 主机id
	private String serviceId; //服务id
	private String serviceRoleId; // 服务角色id
	private List<String> operateParams; // shell脚本的操作参数
	
	public CmdType cmdType; // 命令类型，当前：启动、停止和重启
	public enum CmdType {
		START, STOP, RESTART
	}
	
	public Command(ServiceInfo service, CmdType cmdType, List<String> args) {
		
	}

	public Command(String service, String serviceRole, String host, CmdType cmdType,
			String workDir, String shellName, String hostId, String serviceId, String serviceRoleId) {
		super();
		this.service = service;
		this.serviceRole = serviceRole;
		this.host = host;
		this.cmdType = cmdType;
		this.workDir = workDir;
		this.shellName = shellName;
		this.hostId = hostId;
		this.serviceId = serviceId;
		this.serviceRoleId = serviceRoleId;
	}
	
	public Command() {
	}

	// 组装命令参数 sh myworkdir/myshell.sh [-h xx.xx.xx.xx] [-t master] [-o start]
	public List<String> getArgs() {
		List<String> args = new ArrayList<String>();
//		args.add("/bin/sh");
//		args.add("-c");
		args.add("sh");
		args.add(shellName);
		args.addAll(operateParams);
		return args;
	}

	public List<String> getOperateParams() {
		return operateParams;
	}

	public void setOperateParams(List<String> operateParams) {
		this.operateParams = operateParams;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getServiceRole() {
		return serviceRole;
	}

	public void setServiceRole(String serviceRole) {
		this.serviceRole = serviceRole;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public CmdType getCmdType() {
		return cmdType;
	}

	public void setCmdType(CmdType cmdType) {
		this.cmdType = cmdType;
	}

	public String getWorkDir() {
		return workDir;
	}

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	public String getShellName() {
		return shellName;
	}

	public void setShellName(String shellName) {
		this.shellName = shellName;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getServiceRoleId() {
		return serviceRoleId;
	}

	public void setServiceRoleId(String serviceRoleId) {
		this.serviceRoleId = serviceRoleId;
	}
	
	

	@Override
	public String toString() {
		return "Command [service=" + service + ", serviceRole=" + serviceRole + ", host=" + host
				+ ", cmdType=" + cmdType + ", workDir=" + workDir + ", shellName=" + shellName
				+ ", hostId=" + hostId + ", serviceRoleId=" + serviceRoleId + "]";
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceId() {
		return serviceId;
	}

	

}
