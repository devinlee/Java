package base.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil
{
	/**
	 * ����Ƿ�֧��Cookie
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 */
	public static boolean isCookie(HttpServletRequest request, HttpServletResponse response)
	{
		return true;
//		if(request!=null && response!=null)
//		{
//			Cookie cookie = new Cookie("isCookieCheck", "test");
//			addCookie(response, cookie);
//			String cookieValue = getCookieValue(request, "isCookieCheck");
//			if(cookieValue!=null)
//			{
//				removeCookie(request, response, "isCookieCheck");
//				return true;
//			}
//		}
//		return false;
	}

	/**
	 * ����һ��Cookie
	 * @param response HttpServletResponse
	 * @param cookieName Cookie����
	 * @param cookieValue Cookieֵ
	 * @param maxAge Cookie��Чʱ��
	 */
	public static boolean addCookie(HttpServletResponse response, String cookieName, String cookieValue, int maxAge)
	{
		if(response!=null)
		{
			Cookie cookie = new Cookie(cookieName, cookieValue);
			cookie.setMaxAge(maxAge);
			addCookie(response, cookie);
			return true;
		}
		return false;
	}

	/**
	 * ����һ��Cookie
	 * @param response HttpServletResponse
	 * @param cookie Cookie����
	 */
	public static boolean addCookie(HttpServletResponse response, Cookie cookie)
	{
		if(response!=null && cookie!=null)
		{
			response.addCookie(cookie);
			return true;
		}
		return false;
	}

	/**
	 * ȡ��Cookieֵ
	 * @param request HttpServletRequest
	 * @param cookieName Cookie����
	 */
	public static String getCookieValue(HttpServletRequest request, String cookieName)
	{
		if(request!=null)
		{
			Cookie[] cookies = request.getCookies();
			if(cookies!=null && cookies.length>0)
			{
				for(Cookie cookie : cookies)
				{
					if(cookie.getName().equals(cookieName))
					{
						return cookie.getValue();
					}
				}
			}

		}
		return null;
	}

	/**
	 * ȡ��Cookie����
	 * @param request HttpServletRequest
	 * @param cookieName Cookie����
	 */
	public static Cookie getCookie(HttpServletRequest request, String cookieName)
	{
		if(request!=null)
		{
			Cookie[] cookies = request.getCookies();
			if(cookies!=null && cookies.length>0)
			{
				for(Cookie cookie : cookies)
				{
					if(cookie.getName().equals(cookieName))
					{
						return cookie;
					}
				}
			}

		}
		return null;
	}

	/**
	 * ��������Cookie��ֵ
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param cookieName Cookie����
	 * @param cookieValue Cookieֵ
	 */
	public static boolean setCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue)
	{
		return setCookieValue(request, response, cookieName, cookieValue, -1);
	}

	/**
	 * ��������Cookie��ֵ
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param cookieName Cookie����
	 * @param cookieValue Cookieֵ
	 * @param maxAge Cookie��Чʱ��
	 */
	public static boolean setCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue, int maxAge)
	{
		if(request!=null && response!=null)
		{
			Cookie[] cookies = request.getCookies();
			if(cookies!=null && cookies.length>0)
			{
				for(Cookie cookie : cookies)
				{
					if(cookie.getName().equals(cookieName))
					{
						cookie.setValue(cookieValue);
						if(maxAge!=-1)
						{
							cookie.setMaxAge(maxAge);
						}
						response.addCookie(cookie);
						return true;
					}
				}
			}

		}
		return false;
	}

	/**
	 * �Ƴ��Ѵ���Cookie
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @param cookieName Cookie����
	 */
	public static boolean removeCookie(HttpServletRequest request, HttpServletResponse response, String cookieName)
	{
		if(request!=null && response!=null)
		{
			Cookie[] cookies = request.getCookies();
			if(cookies!=null && cookies.length>0)
			{
				for(Cookie cookie : cookies)
				{
					if(cookie.getName().equals(cookieName))
					{
						cookie.setValue(null);
						cookie.setMaxAge(0);
						response.addCookie(cookie);
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * �Ƴ��Ѵ���Cookie
	 * @param response HttpServletResponse
	 * @param cookie Cookie����
	 */
	public static boolean removeCookie(HttpServletResponse response, Cookie cookie)
	{
		if(response!=null)
		{
			cookie.setValue(null);
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			return true;
		}
		return false;
	}
}