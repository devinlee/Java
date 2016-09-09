package base.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author Devin
 *日志管理工厂
 */
public class LogFactory extends LogManager
{
	private static LogFileHandler _logFileHandler;
	private static LogFactory _instance;
	private static String logFileName;
	public void setLogFileName(String fileName)
	{
		logFileName=fileName;
	}
	
	public LogFactory()
	{
		super();
	}

	/**
	 * 日志管理工厂
	 * @return
	 */
	public static synchronized LogFactory getInstance()
	{
		if (_instance == null)
		{
			_instance = new LogFactory();
		}
		return _instance;
	}

	public boolean addLogger(Logger logger)
	{
		boolean result = super.addLogger(logger);
//		if (logger.getResourceBundleName() == null)
//		{
//			try
//			{
//				Logger newLogger = Logger.getLogger(logger.getName(), getLoggerResourceBundleName(logger.getName()));
//				assert (logger == newLogger);
//			}
//			catch (Throwable ex)
//			{
//				ex.printStackTrace();
//			}
//		}
		synchronized (this)
		{
			internalInitializeLogger(logger);
		}
		return result;
	}

	/**
	 * 初始化Logger
	 * @param logger
	 */
	private void internalInitializeLogger(final Logger logger)
	{
		Handler[] h = logger.getHandlers();
		for (int i = 0; i < h.length; i++)
		{
			logger.removeHandler(h[i]);
		}

		logger.addHandler(getLogFileHandler());
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.FINEST);
	}

	private static synchronized Handler getLogFileHandler()
	{
		if (_logFileHandler == null)
		{
			try
			{
				_logFileHandler = LogFileHandler.getInstance();
				_logFileHandler.setLevel(Level.ALL);
				_logFileHandler.setLogFileName(logFileName);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return _logFileHandler;
	}

//	public String getLoggerResourceBundleName(String loggerName)
//	{
//		String result = loggerName + "." + "LogStrings";
//		return result;
//	}
}
