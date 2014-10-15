package main.server;

import com.tsinghuabigdata.thm.server.model.host.Host;
import com.tsinghuabigdata.thm.server.service.host.IHostService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author flyaos 2014-3-26
 * 扫描Host表里面的所有主机，更新Agent状态
 * 当Agent连不上时，Ping测试主机是否可连
 */
public class CheckAgentStatus implements Runnable {

	public IHostService hostService = (IHostService) SpringCTXBeans.getBean("hostService");
	static int AGENT_TIMEOUT = 15 * 1000; // Timeout 设为15秒
	
	private String threadName;

	public CheckAgentStatus(String threadName) {
		super();
		this.threadName = threadName;
	}

	@Override
	public void run() {
        do {
            List<Host> hostList = hostService.findAll();
            if (!hostList.isEmpty()) {
                for (Host host : hostList) {
                    String ip = host.getIp();
                    Long currentTime = System.currentTimeMillis();
                    Long lastHeartbeat = hostService.getLastHeartTime(ip);
                    if ((currentTime - lastHeartbeat) > AGENT_TIMEOUT) {
                        hostService.updateAgentStatus(ip, "Dead");
                    } else {
                        hostService.updateAgentStatus(ip, "Alive");
                        System.out.printf("[Host: %s ]: Agent Alive ", ip);
                    }
                    checkHostStatus(ip); // 监测主机是否能 ping 通
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);
	}

    /**
     * Ping 主机看是否 Ping 通,并修改数据库状态,Timeout 8s
     * @param hostIp 主机IP
     */
    public void checkHostStatus(String hostIp) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(hostIp);
        } catch (UnknownHostException e) {
            System.out.println("Unknow Host:" + hostIp);
            e.printStackTrace();
        }
        if (address != null) {
            try {
                boolean reachable = address.isReachable(8000);
                if (!reachable) {
                    hostService.updateHostStatus(hostIp,"Stopped");
                }else {
                	hostService.updateHostStatus(hostIp,"Started");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getThreadName() {
		return threadName;
	}

}
