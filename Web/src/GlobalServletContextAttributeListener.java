import javax.servlet.ServletContextAttributeEvent;

import base.servlet.ServletContextAttributeListenerBase;

public class GlobalServletContextAttributeListener extends ServletContextAttributeListenerBase
{
	public void attributeAdded(ServletContextAttributeEvent event)
	{
		System.out.println("GlobalServletContextAttributeListener attributeAdded:"+event.getName());
	}

	public void attributeRemoved(ServletContextAttributeEvent event)
	{
		System.out.println("GlobalServletContextAttributeListener attributeRemoved:" + event.getName());
	}

	public void attributeReplaced(ServletContextAttributeEvent event)
	{
		System.out.println("GlobalServletContextAttributeListener attributeReplaced:" + event.getName());
	}
}