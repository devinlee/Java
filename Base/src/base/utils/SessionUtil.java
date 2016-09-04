package base.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtil {
	/**
	 * ����һ��Session
	 * @param request HttpServletRequest
	 * @param attributeName Session��������
	 * @param attributeValue Session����ֵ
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
	 * ȡ��Session����ֵ
	 * @param request HttpServletRequest
	 * @param attributeName Session��������
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
	 * �Ƴ��Ѵ���Session
	 * @param request HttpServletRequest
	 * @param sessionName Session����
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
	 * �Ƴ�����Session
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