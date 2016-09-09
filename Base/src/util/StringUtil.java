package base.util;

public class StringUtil
{
	/**
	 * 字符是否为空
	 * @param value 字符值
	 * @return 如果为空返回true,否则返回false
	 */
	public static boolean isEmptyOrNull(String value)
	{
		if (value == null || value == "" || value.isEmpty())
		{
			return true;
		}
		return false;
	}
}
