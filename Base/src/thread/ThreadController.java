package base.thread;

import base.log.UncaughtException;

public class ThreadController
{
	/**
	 * 线程由于未捕获到异常而突然终止时调用的处理程序
	 */
	private UncaughtException uncaughtException = new UncaughtException();

	/**
	 * 创建线程
	 * @param target Runnable
	 * @return
	 */
	public Thread create(Runnable target)
	{
		Thread thread = new Thread(target);
		thread.setUncaughtExceptionHandler(uncaughtException);
		return thread;
	}

	/**
	 * 创建线程
	 * @param target 其run方法被调用的Runnable对象
	 * @param name 线程名称
	 * @return
	 */
	public Thread create(Runnable target, String name)
	{
		Thread thread = new Thread(target, name);
		thread.setUncaughtExceptionHandler(uncaughtException);
		return thread;
	}

	/**
	 * 创建线程
	 * @param group 线程组
	 * @param target 其run方法被调用的Runnable对象
	 * @return
	 */
	public Thread create(ThreadGroup group, Runnable target)
	{
		Thread thread = new Thread(group, target);
		thread.setUncaughtExceptionHandler(uncaughtException);
		return thread;
	}

	/**
	 * 创建线程
	 * @param group 线程组
	 * @param target 其run方法被调用的Runnable对象
	 * @param name 线程名称
	 * @return
	 */
	public Thread create(ThreadGroup group, Runnable target, String name)
	{
		Thread thread = new Thread(group, target, name);
		thread.setUncaughtExceptionHandler(uncaughtException);
		return thread;
	}

	/**
	 * 创建线程
	 * @param group 线程组
	 * @param target 其run方法被调用的Runnable对象
	 * @param name 线程名称
	 * @param stackSize 新线程的预期堆栈大小，为零时表示忽略该参数。
	 * @return
	 */
	public Thread create(ThreadGroup group, Runnable target, String name, long stackSize)
	{
		Thread thread = new Thread(group, target, name, stackSize);
		thread.setUncaughtExceptionHandler(uncaughtException);
		return thread;
	}
}
