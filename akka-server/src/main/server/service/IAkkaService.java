package main.server.service;
import main.message.Command;

public interface IAkkaService {
	/**
	 * 发送命令给 Server
	 * @param cmd
	 * @return void
	 */
	public void sendCommand(Command cmd);
	
	/**
	 * 检查host状态
	 * @param host
	 * @return
	 */
	public String checkHostStatus(String host);
}
