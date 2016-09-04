import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import base.servlet.FilterBase;

public class GlobalFilter extends FilterBase
{
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		System.out.println("GlobalFilter");
		chain.doFilter(request, response);
	}

	public void init(FilterConfig config) throws ServletException
	{
	}

	public void destroy() {
	}
}
