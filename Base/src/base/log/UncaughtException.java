package base.log;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UncaughtException implements UncaughtExceptionHandler
{
	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
		Logger.getLogger(UncaughtException.class.getName()).log(Level.SEVERE, "线程" + t.getId() + " " + t.getName() + "UncaughtException错误", e);
	}
}
