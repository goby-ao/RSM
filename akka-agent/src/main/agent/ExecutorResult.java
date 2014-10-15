package main.agent;

public class ExecutorResult {

	public static String result;
	public static String errorInfo;
	
	public ExecutorResult(){
		ExecutorResult.result = "";
		ExecutorResult.errorInfo = "";
	}

	public static String getResult() {
		return result;
	}

	public static String getErrorInfo() {
		return errorInfo;
	}

	public static void setErrorInfo(String errorInfo) {
		ExecutorResult.errorInfo = errorInfo;
	}

	public static void setResult(String result) {
		ExecutorResult.result = result;
	}
	
	
}
