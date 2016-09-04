import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import base.servlet.HttpServletBase;

public class GlobalHttpServlet extends HttpServletBase
{
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		super.doGet(request, response);
		System.out.println("GlobalHttpServlet doGet");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
		System.out.println("GlobalHttpServlet doPost");
	}
	
	public void destroy()
	{
	}
}
