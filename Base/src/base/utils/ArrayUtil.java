package base.utils;

import java.util.Arrays;

public class ArrayUtil
{
	// /**
	// * 比较指定的数组是否包含指定的元素
	// * @param array 数组
	// * @param value 元素
	// * @return 如果包含则返回true,否则返回false
	// */
	// public static<T> boolean contains(T value, T array[])
	// {
	// if(array==null || array.length<=0)return false;
	// for(final T e : array)
	// {
	// if (e == value || (value != null && value.equals(e)))
	// {
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * 比较指定的数组是否包含指定的元素
	 * @param array 数组
	 * @param value 元素
	 * @return 如果包含则返回true,否则返回false
	 */
	public static <T> boolean contains(T value, T array[])
	{
		return Arrays.asList(array).contains(value);
	}
}
