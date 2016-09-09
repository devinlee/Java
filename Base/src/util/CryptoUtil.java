package base.util;

import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CryptoUtil
{
	/**
	 * 异或加解密
	 * @param bytes 要加解密的byte[]
	 * @param keys 加解密的key
	 * @return 加解密后的byte[]
	 */
	public static byte[] cryptoXOR(byte[] bytes, byte[] keys)
	{
		int keyIndex = 0;
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = (byte) (bytes[i] ^ keys[keyIndex]);
			keyIndex++;
			if (keyIndex >= keys.length)
				keyIndex = 0;
		}
		return bytes;
	}

	/**
	 * 加密指定的加密源bytes，并生成16byte校验加密bytes
	 * @param socketDataStructureType 数据包种类标识
	 * @param connectionID 连接ID
	 * @param connectionCheckCode 连接校验码
	 * @param packetNumber 包序号
	 * @param packetBytes 包体bytes
	 * @param keys 加解的key
	 * @return 成功生成的校验码
	 */
	public static byte[] encryptPacket(byte socketDataStructureType, int connectionID, int connectionCheckCode, int packetNumber, byte[] packetBytes, int[] key)
	{
		try
		{
			int sumValue = socketDataStructureType + connectionID + connectionCheckCode + packetNumber + packetBytes.length + key.length;
			// System.out.println("encryptPacket sumValue1:"+sumValue);
			int t, o, i = 0, j = 0, l = packetBytes.length;
			int[] keys = new int[256];
			System.arraycopy(key, 0, keys, 0, 256);
			for (int c = 0; c < l; c++)
			{
				i = (i + 1) % 256;
				j = (j + keys[i]) % 256;

				t = keys[j];
				keys[j] = keys[i];
				keys[i] = t;

				o = keys[(keys[i] + keys[j]) % 256];
				sumValue += (int) (packetBytes[c] & 0xFF);
				packetBytes[c] = (byte) (packetBytes[c] ^ o);
			}
			// System.out.println("encryptPacket sumValue2:"+sumValue);
			byte[] sumValueBytes = ByteUtil.getBytes(sumValue);
			// System.out.println("encryptPacket 加密前:"+sumValueBytes);
			sumValueBytes = RC4Util.crypto(sumValueBytes, key);
			if (sumValueBytes != null)
			{
				// System.out.println("encryptPacket 加密后:"+sumValueBytes);
				return sumValueBytes;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CryptoUtil.class.getName()).log(Level.SEVERE, "数据包加密错误", e);
		}
		return null;
	}

	/**
	 * 解密指定的加密源bytes，并生成16byte校验加密bytes
	 * @param socketDataStructureType 数据包种类标识
	 * @param connectionID 连接ID
	 * @param connectionCheckCode 连接校验码
	 * @param packetNumber 包序号
	 * @param packetBytes 加密源bytes
	 * @param keys 加解的key
	 * @return 成功生成的4byte校验加密bytes
	 */
	public static byte[] decryptionPacket(byte socketDataStructureType, int connectionID, int connectionCheckCode, int packetNumber, byte[] packetBytes, int[] key)
	{
		try
		{
			int sumValue = socketDataStructureType + connectionID + connectionCheckCode + packetNumber + packetBytes.length + key.length;
			int t, o, i = 0, j = 0, l = packetBytes.length;
			int[] keys = new int[256];
			System.arraycopy(key, 0, keys, 0, 256);
			for (int c = 0; c < l; c++)
			{
				i = (i + 1) % 256;
				j = (j + keys[i]) % 256;

				t = keys[j];
				keys[j] = keys[i];
				keys[i] = t;
				o = keys[(keys[i] + keys[j]) % 256];
				packetBytes[c] = (byte) (packetBytes[c] ^ o);
				sumValue += (int) (packetBytes[c] & 0xFF);
			}
			byte[] sumValueBytes = ByteUtil.getBytes(sumValue);
			sumValueBytes = RC4Util.crypto(sumValueBytes, key);
			if (sumValueBytes != null)
			{
				return sumValueBytes;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CryptoUtil.class.getName()).log(Level.SEVERE, "数据包加密错误", e);
		}
		return null;
	}

	/**
	 * 生成随机字符串
	 * @param length 指定的随机产生的长度
	 * @return  随机产生的字符串
	 */
	public static String genRandomString(int length)
	{
		final int maxNum = 36;// 26个字母+10个数字
		int i;
		int count = 0;
		char[] str = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		StringBuffer pwd = new StringBuffer("");
		Random r = new Random();
		while (count < length)
		{
			i = Math.abs(r.nextInt(maxNum));
			if (i >= 0 && i < str.length)
			{
				pwd.append(str[i]);
				count++;
			}
		}
		return pwd.toString().toLowerCase();
	}

	/**
	 * 生成指定数量的不重复随机整数(从0开始)
	 * @param count 生成的随机数数量
	 * @return  随机产生的整数组
	 */
	public static Vector<Integer> genRandoms(int count)
	{
		Vector<Integer> rands = new Vector<Integer>();
		if (count <= 0)
		{
			return rands;
		}
		int values[] = new int[count];
		for (int i = 0; i < count; i++)
		{
			values[i] = i;
		}
		Random r = new Random();
		for (int i = 0; i < count * 3; i++)
		{
			int randBeginIndex = r.nextInt(count);
			int randEndIndex = r.nextInt(count);
			int temp = values[randBeginIndex];
			values[randBeginIndex] = values[randEndIndex];
			values[randEndIndex] = temp;
		}
		for (int i = 0; i < count; i++)
		{
			rands.add(values[i]);
		}
		return rands;
	}
}
