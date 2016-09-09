package base.command;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 命令列表
 *
 */
public class CommandList
{
	/**
	 * 线程读写锁
	 */
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 当前最大命令ID
	 */
	private long commandMaxID = 1;

	/**
	 命令列表容器
	 */
	private final ConcurrentHashMap<Long, ICommandBase> commandBases = new ConcurrentHashMap<Long, ICommandBase>();

	/**
	 * 命令列表
	 */
	public CommandList()
	{
	}

	/**
	 * 增加命令
	 * @param commandBase 命令
	 * @return 返回命令ID
	 */
	public long addCommand(ICommandBase commandBase)
	{
		return addCommand(-1, commandBase);
	}

	/**
	 * 增加命令
	 * @param id 命令ID
	 * @param commandBase 命令
	 * @return 返回命令ID
	 */
	public long addCommand(long id, ICommandBase commandBase)
	{
		lock.writeLock().lock();
		try
		{
			if (id == -1)
				id = commandMaxID++;
			commandBase.setId(id);
			commandBases.put(commandBase.getId(), commandBase);
		}
		catch (Exception e)
		{
			Logger.getLogger(CommandList.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return id;
	}

	/**
	 * 取得命令
	 * @param id 命令ID
	 * @return 命令，如果不存在对应的命令则返回null
	 */
	public ICommandBase getCommand(long id)
	{
		return commandBases.get(id);
	}

	/**
	 * 取得命令，且此命令是用与运行的，取得成功后，将会从命令列表中移除对应的命令项对象
	 * @param id 命令ID
	 * @return 命令，如果不存在对应的命令则返回null
	 */
	public ICommandBase getCommandRun(long id)
	{
		return commandBases.remove(id);
	}

	public void dispose()
	{
		if (commandBases != null)
		{
			commandBases.clear();
		}
	}
}
