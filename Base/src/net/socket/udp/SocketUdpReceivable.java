package base.net.socket.udp;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.event.EventDispatcher;
import base.net.socket.SocketConfig;
import base.net.socket.events.SocketConnectionEvent;
import base.types.SocketDataStructureType;
import base.util.ByteUtil;
import base.util.CryptoUtil;

public class SocketUdpReceivable extends EventDispatcher
{
	/**
	 * 线程读写锁
	 */
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();
	/**
	 * 当前连接对象是否可用
	 */
	private boolean isAvailable = false;
	/**
	 * 标识ID，全局唯一值
	 */
	private int id;
	/**
	 * 连接校验码
	 */
	private int connectionCheckCode;
	/**
	 * 当前UDP已正确接收的最大数据包序号
	 */
	private int udpPacketNumberMax = 0;

	/**
	 * 未处理的已接Udp数据包列表容器
	 */
	private ConcurrentHashMap<Integer, ByteBuffer> byteBufferMap = new ConcurrentHashMap<Integer, ByteBuffer>();

	/**
	 * 通讯数据包加密密码
	 */
	private int[] cryptoKeys;

	/**
	 * 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	private boolean isCrypto;

	/**
	 * 通讯数据包加密密码
	 * @param cryptoKeys 要设置的 cryptoKeys
	 */
	public void setCryptoKeys(int[] cryptoKeys)
	{
		lock.writeLock().lock();
		try
		{
			this.cryptoKeys = cryptoKeys;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpReceivable.class.getName()).log(Level.SEVERE, "读取Socket Udp通道中的数据时发生错误。", e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * UDP接收
	 * @param cryptoKeys 通讯数据包加密密码
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	public SocketUdpReceivable(int[] cryptoKeys, boolean isCrypto)
	{
		this.cryptoKeys = cryptoKeys;
		this.isCrypto = isCrypto;
		isAvailable=true;
	}

	/**
	 * 读取到Socket UDP通道中的数据
	 * @param reByteBuffer 读取到的数据
	 */
	public void receive(ByteBuffer reByteBuffer)
	{
		lock.writeLock().lock();
		try
		{
			if(!isAvailable)return;
			byte socketDataStructureType = reByteBuffer.get(0);// 取得由一个字节组成的标识
			switch (socketDataStructureType)
			{
				case SocketDataStructureType.DATA:// 数据包
					if (reByteBuffer.remaining() > 0)
					{
						int packetNum = reByteBuffer.getInt();// 当前数据包序号
						boolean isPass = false;
						if (packetNum >= udpPacketNumberMax)
						{// 只有收到的包编号>=当前最大包编号，则说明，当前包是后续包，而不是已经处理过一次的包或多次重发的包
							if (isCrypto)
							{// 通讯数据需要安全加密
								byte[] bodyBytes = new byte[reByteBuffer.remaining() - SocketConfig.PACKET_END_LENGTH];// 设置包体长度，包头数据已读，当前剩余数据长度减包尾长度，即为包体长度
								reByteBuffer.get(bodyBytes);// 取得包体字节数组
								if (reByteBuffer.remaining() >= SocketConfig.PACKET_END_LENGTH)
								{// 如果当前包数据剩余长度足够于包尾长度
									// 取得接收包中的包尾校验串
									byte[] endCode = new byte[SocketConfig.PACKET_END_LENGTH];
									reByteBuffer.get(endCode);

									// 进行包数据解密
									byte[] encryptCode = CryptoUtil.decryptionPacket(socketDataStructureType, id, connectionCheckCode, packetNum, bodyBytes, cryptoKeys);
									// 进行包尾校验
									if (ByteUtil.bytesCompare(endCode, encryptCode))
									{// 如果包尾校验匹配，说明该包合法
										reByteBuffer.clear();
										reByteBuffer.put(bodyBytes);
										reByteBuffer.flip();
										isPass = true;
									}
									else
									{
										System.out.println("数据包尾校验未通过");
									}
								}
							}
							else
							{// 通讯数据不需要安全加密
								isPass = true;
							}
						}
						else
						{
							System.out.println("收到重发数据包:"+packetNum);
							dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.SENDABLE_ACK, packetNum));// 发出ACK应答事件
						}

						if (isPass)
						{
							dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.SENDABLE_ACK, packetNum));// 发出ACK应答事件
							if (packetNum == udpPacketNumberMax)
							{// 如果包序号匹配
								dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP, reByteBuffer));
								udpPacketNumberMax++;
								//								if(udpPacketNumberMax>=10)
								//								{
								//									udpPacketNumberMax=0;
								//								}
							}
							else
							{
								byteBufferMap.put(packetNum, reByteBuffer);
							}
							handleUntreatedByteBuffer();
						}
						else
						{
							reByteBuffer.clear();
							reByteBuffer = null;
						}
					}
					break;
				case SocketDataStructureType.KEEP_ALIVE:// 协议心跳
					reByteBuffer.clear();
					reByteBuffer = null;
					dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE));
					break;
				case SocketDataStructureType.ACK:// 应答ACK
					if (reByteBuffer.remaining() > 0)
					{
						if (isCrypto)
						{// 通讯数据需要安全加密
							byte[] bodyBytes = new byte[reByteBuffer.remaining() - SocketConfig.PACKET_END_LENGTH];// 设置包体长度，包头数据已读，当前剩余数据长度减包尾长度，即为包体长度
							reByteBuffer.get(bodyBytes);// 取得包体字节数组
							if (reByteBuffer.remaining() >= SocketConfig.PACKET_END_LENGTH)
							{// 如果当前包数据剩余长度足够于包尾长度
								// 取得接收包中的包尾校验串
								byte[] endCode = new byte[SocketConfig.PACKET_END_LENGTH];
								reByteBuffer.get(endCode);

								// 进行包数据解密
								byte[] encryptCode = CryptoUtil.decryptionPacket(socketDataStructureType, id, connectionCheckCode, 0, bodyBytes, cryptoKeys);
								// 进行包尾校验
								if (ByteUtil.bytesCompare(endCode, encryptCode))
								{// 如果包尾校验匹配，说明该包合法
									reByteBuffer.clear();
									reByteBuffer.put(bodyBytes);
									reByteBuffer.flip();
									dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.RECEIVABLE_ACK, reByteBuffer.getInt()));
								}
								else
								{
									System.out.println("应答ACK包尾校验未通过");
								}
							}
						}
						else
						{// 通讯数据不需要安全加密
							dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.RECEIVABLE_ACK, reByteBuffer.getInt()));
						}
					}
					reByteBuffer.clear();
					reByteBuffer = null;
					break;
				case SocketDataStructureType.CLOSE:// 关闭协议
					reByteBuffer.clear();
					reByteBuffer = null;
					dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.CLOSE_CONNECTION, (byte)2));
					break;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpReceivable.class.getName()).log(Level.SEVERE, "读取Socket Udp通道中的数据时发生错误。", e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 处理未处理的已接Udp数据包列表
	 */
	private void handleUntreatedByteBuffer()
	{
		if (byteBufferMap.containsKey(udpPacketNumberMax))
		{
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP, byteBufferMap.remove(udpPacketNumberMax)));
			udpPacketNumberMax++;
			handleUntreatedByteBuffer();
		}
	}

	/**
	 * 设置连接信息
	 * @param id 标识ID
	 * @param connectionCheckCode 连接校验码
	 */
	public void setConnectionInfo(int id, int connectionCheckCode)
	{
		lock.writeLock().lock();
		try
		{
			this.id = id;
			this.connectionCheckCode = connectionCheckCode;
		}
		catch (Exception e)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	public void dispose()
	{
		lock.writeLock().lock();
		try
		{
			isAvailable=false;
			cryptoKeys = null;
			if (byteBufferMap != null)
				byteBufferMap.clear();
			byteBufferMap = null;
		}
		catch (Exception e)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
}
