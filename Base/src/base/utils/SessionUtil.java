package base.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtil {
	/**
	 * 加入一个Session
	 * @param request HttpServletRequest
	 * @param attributeName Session属性名称
	 * @param attributeValue Session属性值
	 */
	public static boolean setSession(HttpServletRequest request, String attributeName, Object attributeValue)
	{
		if(request!=null)
		{
			HttpSession session = request.getSession();
			if(session!=null)
			{
				session.setAttribute(attributeName, attributeValue);
				return true;
			}
		}
		return false;
	}

	/**
	 * 取得Session属性值
	 * @param request HttpServletRequest
	 * @param attributeName Session属性名称
	 */
	public static Object getSessionAttribute(HttpServletRequest request, String attributeName)
	{
		if(request!=null)
		{
			HttpSession session = request.getSession();
			if(session!=null)
			{
				return session.getAttribute(attributeName);
			}
		}
		return null;
	}

	/**
	 * 移除已存在Session
	 * @param request HttpServletRequest
	 * @param sessionName Session名称
	 */
	public static boolean removeSession(HttpServletRequest request, String sessionName)
	{
		if(request!=null)
		{
			HttpSession session = request.getSession();
			if(session!=null)
			{
				session.removeAttribute(sessionName);
				return true;
			}
		}
		return false;
	}

	/**
	 * 移除所有Session
	 * @param request HttpServletRequest
	 */
	public static boolean removeSession(HttpServletRequest request)
	{
		if(request!=null)
		{
			HttpSession session = request.getSession();
			if(session!=null)
			{
				session.invalidate();
				return true;
			}
		}
		return false;
	}
}