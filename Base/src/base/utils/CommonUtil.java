package base.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommonUtil
{
	private static int randomSeed=1;
	private static final AtomicLong seedUniquifier = new AtomicLong(8682522807148012L);

	/**
	 * 取得随机数
	 * @param minValue 最小值
	 * @param maxValue 最大值
	 * @return
	 */
	public static int getRandom(int minValue, int maxValue)
	{
		try
		{
			if(minValue>maxValue)
			{
				return 0;
			}
			if(minValue==maxValue)
			{
				return minValue;
			}
			Random ran = new Random(seedUniquifier() ^ System.nanoTime() + randomSeed++);
			return ran.nextInt(maxValue)%(maxValue-minValue+1) + minValue;
		}
		catch (Exception e)
		{
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, e);
		}
		return 0;
	}
	
	/**
	 * 取得不重复随机数列
	 * @param rateValue 范围值
	 * @param outValue 返回值
	 * @return
	 */
	public static int[] getRandomOnlyList(int rateValue, int outValue)
	{
		int[] sequence = new int[rateValue];
		int[] output = new int[outValue];
		try
		{
			if(outValue > rateValue)
			{
				return output;
			}
			for(int i = 0; i < rateValue; i++)
			{
				sequence[i] = i;
			}
			int end = rateValue - 1;
			for(int i = 0; i < rateValue; i++)
			{
				if(i == outValue)
				{
					break;
				}
				int num = getRandom(0, end + 1);
				output[i] = sequence[num];
				sequence[num] = sequence[end];
				end--;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, e);
		}
		return output;
	}

	/**
	 * 根据概率值，得出是否在概率之内
	 * @param rateValue 概率值，值范围:0f<=值<=1f
	 */
	public static boolean getRandomRate(float rateValue)
	{
		int val = (int)(rateValue * 10000f);
		if (val >= 10000)
		{
			val = 10000;
		}
		return getRandom(1, 10000) <= val;
	}

	private static long seedUniquifier()
	{
		for (;;) 
		{
			long current = seedUniquifier.get();
			long next = current * 181783497276652981L;
			if (seedUniquifier.compareAndSet(current, next))
				return next;
		}
	}
}