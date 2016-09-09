package base.net.socket;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Socket用ByteBuffer池
 * @author Devin
 *
 */
public class SocketByteBufferPool
{
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * 池是否可用
	 */
	private volatile boolean isAvailable;

	public boolean isAvailable()
	{
		return isAvailable;
	}

	/**
	 * 空闲ByteBuffer列表
	 */
	private BlockingQueue<ByteBuffer> freeByteBuffers = null;

	/**
	 * 活动ByteBuffer列表
	 */
	private Vector<ByteBuffer> activeByteBuffers = null;

	/**
	 * ByteBuffer容量大小
	 */
	private int byteBuffercapacity;
	
	/**
	 * 空闲池大小
	 */
	private int freeSize;

	/**
	 * 实例池
	 * @param freeSize 空闲池大小
	 * @param byteBuffercapacity ByteBuffer容量大小
	 */
	public SocketByteBufferPool(int freeSize, int byteBuffercapacity)
	{
		this.freeSize=freeSize;
		freeByteBuffers = new ArrayBlockingQueue<ByteBuffer>(freeSize);
		activeByteBuffers = new Vector<ByteBuffer>();
		this.byteBuffercapacity=byteBuffercapacity;
		init();
	}

	/**
	 * 初始化
	 */
	private void init()
	{
		try
		{
			for (int i = 0; i < freeSize; i++)
			{
				ByteBuffer byteBuffer = ByteBuffer.allocate(byteBuffercapacity);
				byteBuffer.order(SocketConfig.byteOrder);
				if (!freeByteBuffers.offer(byteBuffer))
				{
					byteBuffer.clear();
					byteBuffer=null;
				}
			}
			isAvailable = true;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketByteBufferPool.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 取得ByteBuffer
	 * @return
	 */
	public ByteBuffer getByteBuffer()
	{
		lock.writeLock().lock();
		ByteBuffer byteBuffer = null;
		try
		{
			if(freeByteBuffers.size()>0)
			{
				byteBuffer = freeByteBuffers.poll();// 从空闲连接集里取到一个ByteBuffer
			}
			else
			{
				byteBuffer = ByteBuffer.allocate(byteBuffercapacity);
				byteBuffer.order(SocketConfig.byteOrder);
			}
			activeByteBuffers.add(byteBuffer);
			return byteBuffer;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketByteBufferPool.class.getName()).log(Level.SEVERE, null, e);
			return  null;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 回收
	 * @param byteBuffer 要回收的ByteBuffer
	 */
	public void releaseByteBuffer(ByteBuffer byteBuffer)
	{
		lock.writeLock().lock();
		try
		{
			activeByteBuffers.remove(byteBuffer);
			// 回收至空闲集
			if (!freeByteBuffers.offer(byteBuffer))
			{
				byteBuffer.clear();
				byteBuffer=null;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketByteBufferPool.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 关闭当前池
	 */
	public void closePool()
	{
		lock.writeLock().lock();
		try
		{
			if (freeByteBuffers != null)
			{
				for (ByteBuffer byteBuffer : freeByteBuffers)
				{
					byteBuffer.clear();
					byteBuffer=null;
				}
				freeByteBuffers.clear();
			}
			if (activeByteBuffers != null)
			{
				for (ByteBuffer byteBuffer : activeByteBuffers)
				{
					byteBuffer.clear();
					byteBuffer=null;
				}
				activeByteBuffers.clear();
			}
		}
		finally
		{
			lock.writeLock().unlock();
		}
		isAvailable = false;
	}
	
	public synchronized void dispose()
	{
		closePool();
		freeByteBuffers=null;
		activeByteBuffers=null;
	}
}