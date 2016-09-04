import javax.servlet.ServletContextEvent;

import base.servlet.ServletContextListenerBase;

public class GlobalServletContextListener extends ServletContextListenerBase
{
	public void contextInitialized(ServletContextEvent event)
	{
		System.out.println("GlobalServletContextListener contextInitialized");
	}
	
	public void contextDestroyed(ServletContextEvent event)
	{
		System.out.println("GlobalServletContextListener contextDestroyed");
	}
}
