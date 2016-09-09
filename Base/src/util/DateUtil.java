package base.util;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil
{
	/**
	 * String Date转换为java Date
	 * @param Date String的Date
	 * @return java类型的Date
	 * @throws java.text.ParseException
	 */
	public static Date StringToDate(String stringDate) throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.parse(stringDate);
	}

	/**
	 * java Date转换为MySQL的DateTime类型String
	 * @param Date java的Date
	 * @return DateTime类型的String
	 */
	public static String DateToMySQLDateTime(Date date)
	{
		final String[] MONTH = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", };
		StringBuffer ret = new StringBuffer();
		String dateToString = date.toString();
		ret.append(dateToString.substring(24, 24 + 4));
		String sMonth = dateToString.substring(4, 4 + 3);
		for (int i = 0; i < 12; i++)
		{
			if (sMonth.equalsIgnoreCase(MONTH[i]))
			{
				if (i + 1 < 10)
					ret.append("-0");
				else
					ret.append("-");
				ret.append(i + 1);
				break;
			}
		}
		ret.append("-");
		ret.append(dateToString.substring(8, 8 + 2));
		ret.append(" ");
		ret.append(dateToString.substring(11, 11 + 8));
		return ret.toString();
	}

	/**
	 * 获取当前年
	 */
	public static int GetYear()
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR);
	}

	/**
	 * 获取指定时间的年
	 */
	public static int GetYear(Date time)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		return cal.get(Calendar.YEAR);
	}

	/**
	 * 获取当前月
	 */
	public static int GetMonth()
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.MONTH) + 1;
	}

	/**
	 * 获取指定时间的月
	 */
	public static int GetMonth(Date time)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		return cal.get(Calendar.MONTH) + 1;
	}
	
	/**
	 * 获取当前星期
	 */
	public static int GetWeek()
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_WEEK) - 1;
	}

	/**
	 * 获取当前日
	 */
	public static int GetDay()
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DATE);
	}

	/**
	 * 获取指定时间的日
	 */
	public static int GetDay(Date time)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		return cal.get(Calendar.DATE);
	}
	
	/**
	 * 获取当前小时
	 */
	public static int GetHour()
	{
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 获取指定时间的小时
	 */
	public static int GetHour(Date time)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 获取差距时间(秒)
	 * @param oldTime 老时间
	 * @param currTime 当前时间
	 * @return 差距时间(秒)
	 */
	public static long GetGapTimeToSec(Date oldTime, Date currTime)
	{
		long seconds = (currTime.getTime() - oldTime.getTime()) / 1000;
		return seconds;
	}

	/**
	 * 获取差距时间(秒)
	 * @param oldTime 老时间
	 * @param currTime 当前时间
	 * @return 差距时间(秒)
	 */
	public static long GetGapTimeToSec(Time oldTime, Time currTime)
	{
		long seconds = (currTime.getTime() - oldTime.getTime()) / 1000;
		return seconds;
	}
	
	/**
	 * 获取差距时间(小时)
	 * @param oldTime 老时间
	 * @param currTime 当前时间
	 * @return 差距时间(小时)
	 */
	public static long GetGapTimeToHour(Date oldTime, Date currTime)
	{
		long hours = GetGapTimeToSec(oldTime, currTime) / 60 / 60;
		return hours;
	}

	/**
	 * 获取差距时间(天)
	 * @param oldTime 老时间
	 * @param currTime 当前时间
	 * @return 差距时间(天)
	 */
	public static long GetGapTimeToDay(Date oldTime, Date currTime)
	{
		long days = GetGapTimeToHour(oldTime, currTime) / 24;
		return days;
	}

	/**
	 * 是否为同一天
	 * @param oldTime 老时间
	 * @param currTime 当前时间
	 * @return 是否为同一天
	 */
	public static boolean IsSameDate(Date oldTime, Date currTime)
	{
		if(GetGapTimeToSec(oldTime, currTime) < 86400)
		{
			int oldHour = GetHour(oldTime);
			int oldDay = GetDay(oldTime);
			int currHour = GetHour(currTime);
			int currDay = GetDay(currTime);
			if(oldHour >= 5 && currHour < 5)
			{
				return true;
			}
			if((oldDay == currDay && oldHour < 5 && currHour < 5) || (oldDay == currDay && oldHour >= 5 && currHour >= 5))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取剩余时间(秒)
	 * @param oldTime 老时间
	 * @param currTime 当前时间
	 * @param needTime 需要时间
	 * @return 剩余时间(秒)
	 */
	public static long GetSurplusTime(Date oldTime, Date currTime, int needTime)
	{
		long milliseconds = (currTime.getTime() - oldTime.getTime()) / 1000;
		long time = needTime - milliseconds;
		if(time < 0) time = 0;
		return time;
	}
	
	/**
	 * 获取系统当前时间毫秒
	 */
	public static long getCurrentTimeMillis()
	{
		return 	System.nanoTime() / 1000000L;
	}
	
	/**
	 * 获取系统当前时间秒
	 */
	public static float getCurrentTimeSecond()
	{
		return 	System.nanoTime() / 1000000L / 1000f;
	}
}
