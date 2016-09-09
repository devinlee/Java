package base.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter
{
	private Date date = new Date();
	private static final char FIELD_SEPARATOR = '|';
	private static final String CRLF = System.getProperty("line.separator");
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public LogFormatter()
	{
		super();
	}

	public String format(LogRecord record)
	{
		return uniformLogFormat(record);
	}

	private String uniformLogFormat(LogRecord record)
	{
		try
		{
			StringBuilder recordBuffer = new StringBuilder();
			date.setTime(record.getMillis());
			recordBuffer.append(dateFormatter.format(date)).append(FIELD_SEPARATOR);
			recordBuffer.append(record.getLevel()).append(FIELD_SEPARATOR);
			recordBuffer.append(record.getLoggerName()).append(FIELD_SEPARATOR);
			recordBuffer.append(record.getMessage());
			Throwable throwable = record.getThrown();
			if (throwable != null)
			{
				recordBuffer.append(CRLF);
				recordBuffer.append(getErrorMessage(throwable));
			}
			recordBuffer.append(CRLF);
			return recordBuffer.toString();
		}
		catch (Exception ex)
		{
			return "Log error occurred on msg:" + record.getMessage() + ":" + ex;
		}
	}

	private String getErrorMessage(Throwable arg1)
	{
		PrintWriter pw = null;
		Writer writer = null;
		try
		{
			writer = new StringWriter();
			pw = new PrintWriter(writer);
			arg1.printStackTrace(pw);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (null != pw)
			{
				pw.close();
			}
		}
		String error = writer.toString();
		return error;
	}
}
