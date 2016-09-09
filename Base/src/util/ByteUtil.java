package base.util;

import java.nio.charset.Charset;
import java.util.Date;

public class ByteUtil
{
	/**
	 * 合并byte数组
	 * @param bytes1 数组1
	 * @param bytes2 数组2
	 * @return 合并后的byte数组
	 */
	public static byte[] bytesMerger(byte[] bytes1, byte[] bytes2)
	{
		byte[] byte3 = new byte[bytes1.length + bytes2.length];
		System.arraycopy(bytes1, 0, byte3, 0, bytes1.length);
		System.arraycopy(bytes2, 0, byte3, bytes1.length, bytes2.length);
		return byte3;
	}

	/**
	 * 比较两个字节数组是否相等
	 * @param bytes1 数组1
	 * @param bytes2 数组2
	 * @return 如果相等返回true,否则反回false
	 */
	public static boolean bytesCompare(byte[] bytes1, byte[] bytes2)
	{
		if (bytes1 == null && bytes2 == null)
			return true;
		if (bytes1 != null && bytes2 != null && bytes1.length != bytes2.length)
			return false;
		if (bytes1 == null && bytes2 != null)
			return false;
		if (bytes1 != null && bytes2 == null)
			return false;
		for (int i = 0; i < bytes1.length; i++)
		{
			if (bytes1[i] != bytes2[i])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 根据byte取得对应的byte[]
	 * @param value byte值
	 * @return
	 */
	public static byte[] getBytes(byte value)
	{
		byte[] bytes = new byte[0];
		bytes[0] = value;
		return bytes;
	}

	/**
	 * 根据Boolean取得对应的byte[]
	 * @param value Boolean值
	 * @return 原boolean值，true将转换为(byte)1，false将转换为(byte)0，
	 */
	public static byte[] getBytes(Boolean value)
	{
		byte[] bytes = new byte[1];
		bytes[0] = (byte) (value == true ? 1 : 0);
		return bytes;
	}

	/**
	 * 根据char取得对应的byte[]
	 * @param value char值
	 * @return
	 */
	public static byte[] getBytes(char value)
	{
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (value);
		bytes[1] = (byte) (value >> 8);
		return bytes;
	}

	/**
	 * 根据short取得对应的byte[]
	 * @param value short值
	 * @return
	 */
	public static byte[] getBytes(short value)
	{
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) ((value & 0xff00) >> 8);
		return bytes;
	}

	/**
	 * 根据int取得对应的byte[]
	 * @param value int值
	 * @return
	 */
	public static byte[] getBytes(int value)
	{
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) ((value & 0xff00) >> 8);
		bytes[2] = (byte) ((value & 0xff0000) >> 16);
		bytes[3] = (byte) ((value & 0xff000000) >> 24);
		return bytes;
	}

	/**
	 * 根据long取得对应的byte[]
	 * @param value long值
	 * @return
	 */
	public static byte[] getBytes(long value)
	{
		byte[] bytes = new byte[8];
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) ((value >> 8) & 0xff);
		bytes[2] = (byte) ((value >> 16) & 0xff);
		bytes[3] = (byte) ((value >> 24) & 0xff);
		bytes[4] = (byte) ((value >> 32) & 0xff);
		bytes[5] = (byte) ((value >> 40) & 0xff);
		bytes[6] = (byte) ((value >> 48) & 0xff);
		bytes[7] = (byte) ((value >> 56) & 0xff);
		return bytes;
	}

	/**
	 * 根据float取得对应的byte[]
	 * @param value float值
	 * @return
	 */
	public static byte[] getBytes(float value)
	{
		int intBits = Float.floatToIntBits(value);
		return getBytes(intBits);
	}

	/**
	 * 根据double取得对应的byte[]
	 * @param value double值
	 * @return
	 */
	public static byte[] getBytes(double value)
	{
		long longBits = Double.doubleToLongBits(value);
		return getBytes(longBits);
	}

	/**
	 * 根据Date取得对应的byte[]
	 * @param value Date值
	 * @return 将Date值的以getTime()取出其long表示，再将long转为byte[]返回
	 */
	public static byte[] getBytes(Date value)
	{
		long longBits = value.getTime();
		return getBytes(longBits);
	}

	/**
	 * 根据String以及charsetName取得对应的byte[]
	 * @param value String值
	 * @param charsetName charsetName名称
	 * @return
	 */
	public static byte[] getBytes(String value, String charsetName)
	{
		Charset charset = Charset.forName(charsetName);
		return value.getBytes(charset);
	}

	/**
	 * 根据String以UTF-8格式取得对应的byte[]
	 * @param value String值
	 * @return
	 */
	public static byte[] getBytes(String value)
	{
		Charset charset = Charset.forName("UTF-8");
		return value.getBytes(charset);
	}

	/**
	 * 根据byte[]取得short类型数据
	 * @param bytes byte[]数据
	 * @return
	 */
	public static short getShort(byte[] bytes)
	{
		return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}

	/**
	 * 根据byte[]取得char类型数据
	 * @param bytes byte[]数据
	 * @return
	 */
	public static char getChar(byte[] bytes)
	{
		return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}

	/**
	 * 根据byte[]取得int类型数据
	 * @param bytes byte[]数据
	 * @return
	 */
	public static int getInt(byte[] bytes)
	{
		return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) | (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
	}

	/**
	 * 根据byte[]取得long类型数据
	 * @param bytes byte[]数据
	 * @return
	 */
	public static long getLong(byte[] bytes)
	{
		return ((((long) bytes[0] & 0xff) << 56) | (((long) bytes[1] & 0xff) << 48) | (((long) bytes[2] & 0xff) << 40) | (((long) bytes[3] & 0xff) << 32) | (((long) bytes[4] & 0xff) << 24) | (((long) bytes[5] & 0xff) << 16) | (((long) bytes[6] & 0xff) << 8) | (((long) bytes[7] & 0xff) << 0));
	}

	/**
	 * 根据byte[]取得float类型数据
	 * @param bytes byte[]数据
	 * @return
	 */
	public static float getFloat(byte[] bytes)
	{
		return Float.intBitsToFloat(getInt(bytes));
	}

	/**
	 * 根据byte[]取得double类型数据
	 * @param bytes byte[]数据
	 * @return
	 */
	public static double getDouble(byte[] bytes)
	{
		long l = getLong(bytes);
		return Double.longBitsToDouble(l);
	}

	/**
	 * 根据byte[]取得String类型数据
	 * @param bytes byte[]数据
	 * @return
	 */
	public static String getString(byte[] bytes, String charsetName)
	{
		return new String(bytes, Charset.forName(charsetName));
	}

	/**
	 * 根据byte[]取得String类型UTF-8编码数据
	 * @param bytes byte[]数据
	 * @return
	 */
	public static String getString(byte[] bytes)
	{
		return getString(bytes, "UTF-8");
	}

	/**
	 * 将byte转换为8位的bit数组
	 * @param b 转换目标byte
	 * @return 为byte类型数组的数组，其数组每个值为一个bit
	 */
	public static byte[] byteToBit(byte b)
	{
		byte[] array = new byte[8];
		for (int i = 7; i >= 0; i--)
		{
			array[i] = (byte) (b & 1);
			b = (byte) (b >> 1);
		}
		return array;
	}

	/**
	 * 将bit转换为byte
	 * @param bitStr 转换目标bit字符串拼接
	 * @return 转换成功后的byte
	 */
	public static byte bitToByte(String bitStr)
	{
		int result = Integer.parseInt(bitStr, 2);
		return (byte) result;
	}

	/**
	 * 取得指定byte[]的反转后开始与结束索引之间的各个值转为String后相拼接的String，
	 * 如：
	 * byte[] bytes = [0,0,0,0,0,0,1,1]， bytesToString(bytes, 0, 8)后拼接字符串值为：00000011
	 * @param bytes 指定的byte[]
	 * @return
	 */
	public static String bytesToString(byte[] bytes)
	{
		return bytesToString(bytes, 0, bytes.length);
	}
	
	/**
	 * 取得指定byte[]的反转后开始与结束索引之间的各个值转为String后相拼接的String，
	 * 如：
	 * byte[] bytes = [0,0,0,0,0,0,1,1]， bytesToString(bytes, 0, 8)后拼接字符串值为：00000011
	 * @param bytes 指定的byte[]
	 * @param offset 开始索引偏移量
	 * @param length 长度
	 * @return
	 */
	public static String bytesToString(byte[] bytes, int offset, int length)
	{
		if (offset + length >= bytes.length)
			length = bytes.length - offset;
		String str = "";
		if (bytes != null && bytes.length > 0)
		{
			for (int i = offset; i < offset + length; i++)
			{
				str += String.valueOf(bytes[i]);
			}
		}
		return str;
	}
}
