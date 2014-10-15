package main.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import main.message.Command;
import main.message.ExecutorState;
import main.message.ExecutorState.State;

/**
 * 发起执行 Shell 脚本命令
 * 
 * @author flyaos
 * 
 */
public class ExecutorRunner implements Runnable{

	private static Log LOG = LogFactory.getLog(ExecutorRunner.class);
	public static Command cmd;

	public ExecutorRunner(Command cmd) {
		super();
		ExecutorRunner.cmd = cmd;
	}

	// @Override
	public void run() {

		LOG.info("ExecutorRunner is running");
		LOG.info("[ExecutorRunner]: cmd: " + cmd.toString());

		List<String> args = cmd.getArgs(); 
		//sh ./hbase.sh [-h xx.xx.xx.xx][-t regionserver/master][-o start/stop]

		LOG.info("process start");
		
		//打印命令
		Iterator<String> it = args.iterator();
		StringBuilder cmdBuffer = new StringBuilder();
		cmdBuffer.append("[");
		while (it.hasNext()) {
			cmdBuffer.append(it.next()).append(" ");
		}
		cmdBuffer.append("]");

		LOG.info("The built command: " + cmdBuffer);

		File file = new File(cmd.getWorkDir()); //设置工作目录，也就是要执行的脚本所在的路径
		ProcessBuilder processBuilder = new ProcessBuilder(args).directory(file);
		Process process = null;
		try { 
			process = processBuilder.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		LOG.info("ProcessBuilder process start");
		InputStream is = process.getInputStream();
		InputStream errorIs = null;
		
		int exitValue = 0; // 若为0则进程正确运行，否则发生阻塞或者错误
		try {
			if ((exitValue = process.waitFor()) != 0) {
				errorIs = process.getErrorStream();
				ExecutorResult.errorInfo = convertStreamToStr(errorIs);
				LOG.warn(ExecutorResult.errorInfo);
			}
		} catch (Exception e) {
			// 发生错误异常处理
			ExecutorResult.result = "error";
			AgentActor.executorState = State.ERROR;
			process.destroy();
			LOG.warn("Error occur during shell processing, ", e);
		}
		
		LOG.info("Shell Exit value: " + exitValue);

		// 读取输出结果每一行，根据结果判断。
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.contains("successful")) {
					ExecutorResult.result = "success";
					break;
				} else if (line.contains("failed")) {
					ExecutorResult.result = "failed";
					break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * 将输入流转为字符串
	 */

	public static String convertStreamToStr(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public List<String> buildCommand() {
		return null;
	}

	public void setCmd(Command command) {
		cmd = command;
	}

	public Command getCmd() {
		return cmd;
	}

}