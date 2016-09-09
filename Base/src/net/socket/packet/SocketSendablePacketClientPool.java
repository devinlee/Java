package base.net.socket.packet;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.net.socket.SocketConfig;
import base.types.SocketType;

public class SocketSendablePacketClientPool
{
	/**
	 * 客户端数据包大小
	 */
	private final int CLIENTPACKET_SIZE = 8192;

	/**
	 * 池初始大小
	 */
	private final int INIT_SIZE = 10;

	/**
	 * 最大空闲大小
	 */
	private final int MAX_FREE_SIZE = 2000;

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
	 * 空闲ByteBuffer
	 */
	private BlockingQueue<ByteBuffer> freeByteBuffers = null;
	
	/**
	 * Udp空闲ByteBuffer
	 */
	private BlockingQueue<ByteBuffer> udpFreeByteBuffers = null;

	public SocketSendablePacketClientPool()
	{
		freeByteBuffers = new ArrayBlockingQueue<ByteBuffer>(MAX_FREE_SIZE);
		udpFreeByteBuffers= new ArrayBlockingQueue<ByteBuffer>(MAX_FREE_SIZE);
		init();
	}

	/**
	 * 初始化
	 */
	private void init()
	{
		try
		{
			for (int i = 0; i < INIT_SIZE; i++)
			{
				ByteBuffer byteBuffer = createByteBuffer();
				if (byteBuffer != null)
				{
					if (!freeByteBuffers.offer(byteBuffer))
					{
						closeByteBuffer(byteBuffer);
					}
				}
				
				byteBuffer = createUdpByteBuffer();
				if (byteBuffer != null)
				{
					if (!udpFreeByteBuffers.offer(byteBuffer))
					{
						closeByteBuffer(byteBuffer);
					}
				}
			}
			isAvailable = true;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketSendablePacketClientPool.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 创建ByteBuffer
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private ByteBuffer createByteBuffer()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(CLIENTPACKET_SIZE);
		byteBuffer.order(SocketConfig.byteOrder);
		return byteBuffer;
	}
	
	/**
	 * 创建Udp ByteBuffer
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private ByteBuffer createUdpByteBuffer()
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(SocketConfig.PACKET_UDP_LENGTN);
		byteBuffer.order(SocketConfig.byteOrder);
		return byteBuffer;
	}
	
	/**
	 * 取得ByteBuffer
	 * @return
	 */
	public ByteBuffer getByteBuffer()
	{
		return getByteBuffer(SocketType.TCP);
	}
	
	/**
	 * 取得ByteBuffer
	 * @return
	 */
	public ByteBuffer getByteBuffer(SocketType socketType)
	{
		ByteBuffer byteBuffer = null;
		lock.writeLock().lock();
		try
		{
			if(socketType==SocketType.TCP)
			{
				if (freeByteBuffers.size() > 0)
				{// 当前空闲ByteBuffer数还有可用数时
					byteBuffer = freeByteBuffers.poll();// 从空闲ByteBuffer集里取到一个ByteBuffer
					if (!isValid(byteBuffer))
					{
						byteBuffer = createByteBuffer();
					}
				}
				else
				{// 创建
					byteBuffer = createByteBuffer();
				}
			}
			else
			{
				if (udpFreeByteBuffers.size() > 0)
				{// 当前空闲ByteBuffer数还有可用数时
					byteBuffer = udpFreeByteBuffers.poll();// 从空闲ByteBuffer集里取到一个ByteBuffer
					if (!isValid(byteBuffer))
					{
						byteBuffer = createUdpByteBuffer();
					}
				}
				else
				{// 创建
					byteBuffer = createUdpByteBuffer();
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketSendablePacketClientPool.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
		return byteBuffer;
	}

	/**
	 * 回收ByteBuffer
	 * @param byteBuffer 要回收的ByteBuffer
	 */
	public void releaseByteBuffer(ByteBuffer byteBuffer)
	{
		releaseByteBuffer(byteBuffer, SocketType.TCP); 
	}
	
	/**
	 * 回收ByteBuffer
	 * @param byteBuffer 要回收的ByteBuffer
	 * @param socketType SocketType
	 */
	public void releaseByteBuffer(ByteBuffer byteBuffer, SocketType socketType)
	{
		lock.writeLock().lock();
		try
		{
			if (isValid(byteBuffer))
			{
				byteBuffer.clear();
				if(socketType==SocketType.TCP)
				{
					// 回收至空闲ByteBuffer集
					if (!freeByteBuffers.offer(byteBuffer))
					{
						closeByteBuffer(byteBuffer);
					}
				}
				else if(socketType==SocketType.UDP)
				{
					// 回收至空闲ByteBuffer集
					if (!udpFreeByteBuffers.offer(byteBuffer))
					{
						closeByteBuffer(byteBuffer);
					}
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketSendablePacketClientPool.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 关闭ByteBuffer
	 * @param conn
	 */
	private void closeByteBuffer(ByteBuffer byteBuffer)
	{
		if (isValid(byteBuffer))
		{
			byteBuffer.clear();
			byteBuffer = null;
		}
	}

	/**
	 * 检查ByteBuffer是否可用
	 * @param byteBuffer 数据ByteBuffer
	 * @return
	 */
	private boolean isValid(ByteBuffer byteBuffer)
	{
		if (byteBuffer == null)
		{
			return false;
		}
		return true;
	}

	/**
	 * 关闭当前ByteBuffer池(会关闭池中所有的ByteBuffer)
	 * @throws SQLException
	 */
	public void closeByteBufferPool()
	{
		lock.writeLock().lock();
		try
		{
			if (freeByteBuffers != null)
			{
				for (ByteBuffer byteBuffer : freeByteBuffers)
				{
					if (isValid(byteBuffer))
						closeByteBuffer(byteBuffer);
				}
				freeByteBuffers.clear();
			}
		}
		finally
		{
			lock.writeLock().unlock();
		}
		isAvailable = false;
	}
}
