package main.message;

/**
 * @author flyaos
 * 2014-3-26 
 */
public enum ServiceRoleStatus {
	
	/**
	 * 服务已经启动
	 */
	Started,
	
	/**
	 * 服务已经停止
	 */
	Stopped,
	
	/**
	 * 排队等待执行
	 */
	WAITTING,
	
	/**
	 * 正在执行启动任务
	 */
	START_LAUNCHING,
	
	/**
	 * 正在执行停止任务
	 */
	STOP_LAUNCHING,
	
	/**
	 * 服务启动成功
	 */
	START_SUCCESS,
	
	/**
	 * 服务启动失败
	 */
	START_FAILED,
	
	/**
	 * 服务停止成功
	 */
	STOP_SUCCESS,
	
	/**
	 * 服务停止服败
	 */
	STOP_FAILED
	
}
