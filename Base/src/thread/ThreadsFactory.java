package base.thread;

/**
 * 线程工厂
 * @author Devin
 *
 */
public class ThreadsFactory
{
	/**
	 * 线程控制器
	 */
	private final static ThreadController threadController = new ThreadController();
	/**
	 * 线程池控制器
	 */
	private final static ThreadPoolController threadPoolController = new ThreadPoolController();

	private ThreadsFactory()
	{
	}

	/**
	 * 线程控制器
	 * @return threadController 线程控制器
	 */
	public static ThreadController threadController()
	{
		return threadController;
	}

	/**
	 * 线程池控制器
	 * @return threadPoolController 线程池控制器
	 */
	public static ThreadPoolController threadPoolController()
	{
		return threadPoolController;
	}
}
