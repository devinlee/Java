package test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import base.servlet.HttpServletBase;
import base.utils.CookieUtil;

public class TestHttpServlet extends HttpServletBase {
	private static final long serialVersionUID = 1L;

	public TestHttpServlet() {
	}

	public void destroy() {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
		response.getWriter().append("TestHttpServlet Served at: ").append(request.getContextPath());
		boolean isCookie = CookieUtil.isCookie(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
