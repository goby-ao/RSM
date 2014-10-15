package main.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 数据库操作接口注入
 * @author dell
 *
 */
public class SpringCTXBeans {
	
	private static final String[] contexts = new String[]{"spring/context-common.xml"};
	private static ApplicationContext ctx;
	
	static{
		ctx = new ClassPathXmlApplicationContext(contexts);
	}
	
	/**
	 * 根据名称注入
	 * @param name
	 * @return
	 */
	public static Object getBean(String name){
		return ctx.getBean(name);
	}
}
