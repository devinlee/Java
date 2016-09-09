package base.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RC4Util
{
	/**
	 * 取得RC4 Keys
	 * @param keys 源keys
	 * @return 返回生成完成的keys
	 */
	public static int[] getKey(byte[] keys)
	{
		try
		{
			int[] box = new int[256];
			int i = 0, x = 0, t = 0, l = keys.length;

			for (i = 0; i < 256; i++)
			{
				box[i] = i;
			}

			for (i = 0; i < 256; i++)
			{
				x = (x + box[i] + keys[i % l]) % 256;
				t = box[x];
				box[x] = box[i];
				box[i] = t;
			}
			return box;
		}
		catch (Exception e)
		{
			Logger.getLogger(RC4Util.class.getName()).log(Level.SEVERE, "取得RC4加密Keys错误", e);
		}
		return null;
	}

	/**
	 * RC4加解密
	 * @param data 加解密源数据
	 * @param key RC4 Keys
	 * @return 返回成功加解密后的数据
	 */
	public static byte[] crypto(byte[] data, int[] key)
	{
		try
		{
			int t, o, i = 0, j = 0, l = data.length;
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
				data[c] = (byte) (data[c] ^ o);
			}
			return data;
		}
		catch (Exception e)
		{
			Logger.getLogger(RC4Util.class.getName()).log(Level.SEVERE, "RC4加解密错误", e);
		}
		return null;
	}
}
