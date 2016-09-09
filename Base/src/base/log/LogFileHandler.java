package base.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * @author Devin
 *日志文件处理
 */
public class LogFileHandler extends StreamHandler
{
	public static LogFileHandler _instance;
	private WrapperStream _wrappedStream;
	private String absoluteFileName = null;
	static final String LOG_FILENAME_PREFIX = "systemLog";
	static final String LOG_FILENAME_SUFFIX = ".log";
	private String logFileName;
	public void setLogFileName(String fileName)
	{
		logFileName=fileName;
	}
	
	public LogFileHandler()
	{
		try
		{
			setFormatter(new LogFormatter());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static synchronized LogFileHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new LogFileHandler();
		}
		return _instance;
	}

	public synchronized void publish(LogRecord record)
	{
		if (_wrappedStream == null)
		{
			try
			{
				absoluteFileName = createFileName();
				openFile(absoluteFileName);
			}
			catch (Exception e)
			{
				throw new RuntimeException("Serious Error Couldn't open Log File" + e);
			}
		}
		super.publish(record);
		flush();
	}

	public String createFileName()
	{
		String instDir = ".";
		// instDir = System.getProperty("com.bes.instanceRoot");
		// if(instDir == null || "".equals(instDir)){
		// instDir = ".";
		// }
		return instDir + "/" + getLogFileName();
	}

	/** 
	 * Creates the file and initialized WrapperStream and passes it on to 
	 * Superclass (java.util.logging.StreamHandler). 
	 */
	private void openFile(String fileName) throws IOException
	{
		File file = new File(fileName);
		if (!file.exists())
		{
			if (file.getParentFile() != null && !file.getParentFile().exists())
			{
				file.getParentFile().mkdir();
			}
			file.createNewFile();
		}
		FileOutputStream fout = new FileOutputStream(fileName, true);
		BufferedOutputStream bout = new BufferedOutputStream(fout);
		_wrappedStream = new WrapperStream(bout, file.length());
		setOutputStream(_wrappedStream);
	}

	private class WrapperStream extends OutputStream
	{
		OutputStream out;

		WrapperStream(OutputStream out, long writ)
		{
			this.out = out;
		}

		public void write(int b) throws IOException
		{
			out.write(b);
		}

		public void write(byte buff[]) throws IOException
		{
			out.write(buff);
		}

		public void write(byte buff[], int off, int len) throws IOException
		{
			out.write(buff, off, len);
		}

		public void flush() throws IOException
		{
			out.flush();
		}

		public void close() throws IOException
		{
			out.close();
		}
	}

	protected String getLogFileName()
	{
		return logFileName;
	}
}
