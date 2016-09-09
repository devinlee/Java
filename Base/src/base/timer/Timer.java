package base.timer;

public class Timer extends java.util.Timer
{
	public Timer()
	{
		super(true);
	}

	public Timer(boolean isDaemon)
	{
		super(isDaemon);
	}

	public Timer(String name)
	{
		super(name);
	}

	public Timer(String name, boolean isDaemon)
	{
		super(name, isDaemon);
	}
	
	public synchronized void dispose()
	{
		this.cancel();
	}
}
