package base.net.socket.udp;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.event.Event;
import base.event.EventDispatcher;
import base.event.IEventListener;
import base.net.NetFactory;
import base.net.socket.SocketConfig;
import base.net.socket.SocketUtil;
import base.net.socket.events.SocketConnectionEvent;
import base.net.socket.packet.ISocketSendablePacketBase;
import base.timer.Timer;
import base.types.SocketDataStructureType;
import base.types.SocketType;
import base.util.CryptoUtil;

/**
 * Socket Udp 发送窗口
 * @author Devin
 *
 */
public class SocketUdpSendable extends EventDispatcher implements IEventListener
{
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
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
	 * 窗口ACK计时器
	 */
	private Timer timer;

	/**
	 * 窗口初始最大大小
	 */
	private int windowSize;

	/**
	 * 窗口当前发送数据包序号
	 */
	private int sequencePacketNumber = 0;

	/**
	 * 已发送的窗口序列集
	 */
	private ConcurrentHashMap<Integer, SocketUdpSendableCell> sentWindowSequence;

	/**
	 * 已发送的窗口序列列表
	 */
	private Vector<SocketUdpSendableCell> sentWindowSequenceList = new Vector<SocketUdpSendableCell>();

	/**
	 * 字节缓冲大小
	 */
	private int byteBufferCapacity;

	/**
	 * 待发送的序列
	 */
	private BlockingQueue<SocketUdpSendableCell> windowSequence = new LinkedBlockingQueue<SocketUdpSendableCell>();

	/**
	 * Udp通道的SocketAddress
	 */
	private SocketAddress socketAddress;

	/**
	 * Udp通道的SocketAddress
	 * @return socketAddress
	 */
	public SocketAddress getSocketAddress()
	{
		lock.readLock().lock();
		try
		{
			return socketAddress;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
			return null;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * Udp通道的SocketAddress
	 * @param socketAddress 要设置的 socketAddress
	 */
	public void setSocketAddress(SocketAddress socketAddress)
	{
		lock.writeLock().lock();
		try
		{
			this.socketAddress = socketAddress;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * Udp通道
	 */
	private DatagramChannel datagramChannel;

	/**
	 * @param datagramChannel 要设置的 datagramChannel
	 */
	public void setDatagramChannel(DatagramChannel datagramChannel)
	{
		lock.writeLock().lock();
		try
		{
			this.datagramChannel = datagramChannel;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

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
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * Socket Udp 发送窗口
	 * @param datagramChannel DatagramChannel
	 * @param socketAddress 目标地址
	 * @param initWindowSize 窗口初始最大大小
	 * @param byteBufferCapacity 字节缓冲大小
	 * @param cryptoKeys 通讯数据包加密密码
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	public SocketUdpSendable(DatagramChannel datagramChannel, SocketAddress socketAddress, int initWindowSize, int byteBufferCapacity, int[] cryptoKeys, boolean isCrypto)
	{
		this.datagramChannel = datagramChannel;
		this.socketAddress = socketAddress;
		this.windowSize = initWindowSize;
		this.byteBufferCapacity = byteBufferCapacity;
		this.cryptoKeys = cryptoKeys;
		this.isCrypto = isCrypto;
		sentWindowSequence = new ConcurrentHashMap<Integer, SocketUdpSendableCell>(this.windowSize);
		timer = new Timer();
		isAvailable=true;
	}

	/**
	 * 增加待处理的字节缓冲，只把字节缓冲当前位置与限制之间的元素数进行加入
	 * @param socketDataStructureType 数据包种类标识
	 * @param byteBuf 加入要发送的数据源
	 */
	public void addByteBuffer(byte socketDataStructureType, ByteBuffer byteBuf)
	{
		lock.writeLock().lock();
		try
		{
			if(!isAvailable)return;
			switch (socketDataStructureType)
			{
				case SocketDataStructureType.HANDSHAKE_UDP:// 控制包-UDP协议握手
					byteBuf.putInt(1, id);// 加入连接ID
					byteBuf.putInt(5, connectionCheckCode);// 加入连接校验码
					if (isCrypto)
					{// 如果需要对数据包进行加密及校验，则进行相应处理
						byte[] bodyBytes = new byte[byteBuf.remaining() - SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH - SocketConfig.CONNECTION_ID_LENGTH - SocketConfig.CONNECTION_CHECK_CODE_LENGTH];// 用来加密数据部分，其长度需要减去包头长度，即为包体部分数据
						byteBuf.limit(byteBuf.capacity());
						byteBuf.position(SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH + SocketConfig.CONNECTION_ID_LENGTH + SocketConfig.CONNECTION_CHECK_CODE_LENGTH);
						byteBuf.get(bodyBytes);
						byte[] encryptEndCode = CryptoUtil.encryptPacket(socketDataStructureType, id, connectionCheckCode, 0, bodyBytes, cryptoKeys);
						byteBuf.position(SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH + SocketConfig.CONNECTION_ID_LENGTH + SocketConfig.CONNECTION_CHECK_CODE_LENGTH);
						byteBuf.put(bodyBytes);// 重置加入包体加密后数据
						byteBuf.put(encryptEndCode);// 加入包尾校验
						byteBuf.flip();
					}
					sendCellData(byteBuf);// 直接发送，不经过待发送列表
					byteBuf.clear();
					byteBuf=null;
					break;
				case SocketDataStructureType.DATA:// 数据包
					while (byteBuf != null && byteBuf.hasRemaining())
					{// 循环装载，直到装载源数据无剩余数据
						int packCapacity = SocketConfig.PACKET_HEAD_LENGTH_UDP + byteBuf.remaining();//当前包的大小计算=UDP包头长+数据内容长度
						if(isCrypto)
						{
							packCapacity+=SocketConfig.PACKET_END_LENGTH;//在加密下，当前包的大小计算=已计算长度+包尾长度
						}
						if(packCapacity>byteBufferCapacity)
						{
							packCapacity=byteBufferCapacity;//不能超过设定的最大包容量
						}

						ByteBuffer byteBuffer = NetFactory.socketController().getSendablePacketClientByteBuffer(SocketType.UDP);
						byteBuffer.order(SocketConfig.byteOrder);
						byteBuffer.put(socketDataStructureType);// 包种类标识
						int bodyLen = byteBuf.remaining() > byteBufferCapacity - SocketConfig.PACKET_HEAD_LENGTH_UDP - SocketConfig.PACKET_END_LENGTH ? byteBufferCapacity - SocketConfig.PACKET_HEAD_LENGTH_UDP - SocketConfig.PACKET_END_LENGTH : byteBuf.remaining();
						byte[] bodyBytes = new byte[bodyLen];
						byteBuf.get(bodyBytes);
						SocketUdpSendableCell socketUdpSendableCell = new SocketUdpSendableCell(socketDataStructureType, byteBuffer, bodyBytes);
						addWindowSequence(socketUdpSendableCell);
					}
					byteBuf.clear();
					byteBuf = null;
					break;
				case SocketDataStructureType.KEEP_ALIVE:// 协议心跳保活包
					sendCellData(byteBuf);// 直接发送，不经过待发送列表
					byteBuf.clear();
					byteBuf=null;
					break;
				case SocketDataStructureType.KEEP_ALIVE_ACK:// 协议心跳保活包应答
					sendCellData(byteBuf);// 直接发送，不经过待发送列表
					byteBuf.clear();
					byteBuf=null;
					break;
				case SocketDataStructureType.ACK:// 控制包-UDP数据包ACK
					if (isCrypto)
					{// 如果需要对数据包进行加密及校验，则进行相应处理
						byte[] bodyBytes = new byte[byteBuf.remaining() - SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH];// 用来加密数据部分，其长度需要减去包头长度，即为包体部分数据
						byteBuf.limit(byteBuf.capacity());
						byteBuf.position(SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH);
						byteBuf.get(bodyBytes);
						byte[] encryptEndCode = CryptoUtil.encryptPacket(socketDataStructureType, id, connectionCheckCode, 0, bodyBytes, cryptoKeys);
						byteBuf.position(SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH);
						byteBuf.put(bodyBytes);// 重置加入包体加密后数据
						byteBuf.put(encryptEndCode);// 加入包尾校验
						byteBuf.flip();
					}
					sendCellData(byteBuf);// 直接发送，不经过待发送列表
					break;
			}
			sendWindowData();
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	private void addWindowSequence(SocketUdpSendableCell socketUdpSendableCell)
	{
		try
		{
			windowSequence.put(socketUdpSendableCell);
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 发送窗体数据
	 */
	public void send()
	{
		sendWindowData();
	}

	/**
	 * 发送窗口数据
	 */
	private void sendWindowData()
	{
		lock.writeLock().lock();
		try
		{
			if(!isAvailable)return;
			SocketUdpSendableCell socketUdpSendableCell;
			while (isAvailable && sentWindowSequenceList.size() < windowSize && (socketUdpSendableCell = windowSequence.poll()) != null)
			{
				if (socketUdpSendableCell.getSocketDataStructureType() == SocketDataStructureType.DATA)
				{// 只为“数据包”包种类标识时做ACK处理
					//					if(sequencePacketNumber>=windowSize)
					//					{
					//						sequencePacketNumber=0;
					//					}
					socketUdpSendableCell.end(id, connectionCheckCode, sequencePacketNumber, isCrypto, cryptoKeys);
					socketUdpSendableCell.setSendNumber((short) 1);
					sendCellData(socketUdpSendableCell.getData());
					sentWindowSequence.put(socketUdpSendableCell.getSequenceNumber(), socketUdpSendableCell);
					sentWindowSequenceList.add(socketUdpSendableCell);
					socketUdpSendableCell.addEventListener(SocketConnectionEvent.SENDABLE_PACKET_ACK_TIME_OUT, this);
					timer.schedule(socketUdpSendableCell.ackTimerStart(), 750);
					sequencePacketNumber++;
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 发送数据包
	 */
	private void sendCellData(ByteBuffer byteBuffer)
	{
		try
		{
			while (byteBuffer.hasRemaining())
			{
				if(datagramChannel!=null)
				{
					datagramChannel.send(byteBuffer, socketAddress);
				}
			}
		}
		catch (IOException e)
		{
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 发出应答ACK
	 * @param packetNum 包序号
	 */
	public void ackSend(int packetNum)
	{
		if(!isAvailable)return;
		ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.ACK);
		socketSendablePacketClient.putInt(packetNum);
		socketSendablePacketClient.end(isCrypto);
		addByteBuffer(socketSendablePacketClient.getSocketDataStructureType(), socketSendablePacketClient.getData());
	}

	/**
	 * 发送协议心跳保活应答
	 */
	public void ackKeepAliveSend()
	{
		if(!isAvailable)return;
		ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.KEEP_ALIVE_ACK);
		socketSendablePacketClient.end(false);
		addByteBuffer(socketSendablePacketClient.getSocketDataStructureType(), socketSendablePacketClient.getData());
	}

	private Vector<SocketUdpSendableCell> removeSendableCells=new Vector<SocketUdpSendableCell>();
	/**
	 * 收到应答ACK
	 * @param packetNum 包序号
	 */
	public void ackReceive(int packetNum)
	{
		lock.writeLock().lock();
		try
		{
			if(!isAvailable)return;
			removeSendableCells.clear();
			SocketUdpSendableCell socketUdpSendableCell = sentWindowSequence.get(packetNum);
			if (socketUdpSendableCell != null)
			{
				socketUdpSendableCell.setIsAck(true);
				for(byte i=0;i<sentWindowSequenceList.size();i++)
				{
					SocketUdpSendableCell cell =sentWindowSequenceList.get(i);
					if(cell.getIsAck())
					{//如果已经应答
						removeSendableCells.add(cell);
					}
					else
					{
						break;//只要遇到未应答的，则不能向下执行
					}
				}
				while(removeSendableCells.size()>0)
				{
					SocketUdpSendableCell cell =removeSendableCells.remove(0);
					sentWindowSequence.remove(cell.getSequenceNumber());
					sentWindowSequenceList.remove(cell);
					cell.removeEventListener(SocketConnectionEvent.SENDABLE_PACKET_ACK_TIME_OUT, this);
					cell.dispose();
					cell = null;
				}
			}
			if (timer != null)
				timer.purge();
			sendWindowData();
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	public void handleEvent(Event event)
	{
		if(!isAvailable)return;
		switch (event.getType())
		{
			case SocketConnectionEvent.SENDABLE_PACKET_ACK_TIME_OUT:// 发送包接收ACK应答超时，需要重发
				SocketUdpSendableCell resendSocketUdpSendableCell = sentWindowSequence.get((int) event.getData());
				if (resendSocketUdpSendableCell != null)
				{
					if (resendSocketUdpSendableCell.getSendNumber() > 10)
					{//如果超时重发超过10次，强制当前udp连接已与服务器断开
						dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.CLOSE_CONNECTION, (byte)2));
						return;
					}

					System.out.println("超时重发:" + resendSocketUdpSendableCell.getSequenceNumber());
					resendSocketUdpSendableCell.setSendNumber((short) (resendSocketUdpSendableCell.getSendNumber()+1));
					resendSocketUdpSendableCell.getData().position(resendSocketUdpSendableCell.getDataPosition());
					resendSocketUdpSendableCell.getData().limit(resendSocketUdpSendableCell.getDataLimit());
					sendCellData(resendSocketUdpSendableCell.getData());
					timer.schedule(resendSocketUdpSendableCell.ackTimerStart(), 300);
				}
				break;
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
			Logger.getLogger(SocketUdpSendable.class.getName()).log(Level.SEVERE, null, e);
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
			cryptoKeys=null;
			if (windowSequence != null)
			{
				while(windowSequence.size()>0)
				{
					SocketUdpSendableCell socketUdpSendableCell = windowSequence.remove();
					socketUdpSendableCell.removeEventListener(SocketConnectionEvent.SENDABLE_PACKET_ACK_TIME_OUT, this);
					socketUdpSendableCell.dispose();
				}
				windowSequence.clear();
				windowSequence = null;
			}
			if (sentWindowSequence != null)
			{
				sentWindowSequence.clear();
				sentWindowSequence = null;
			}
			while(sentWindowSequenceList != null && sentWindowSequenceList.size()>0)
			{
				SocketUdpSendableCell socketUdpSendableCell = sentWindowSequenceList.remove(0);
				socketUdpSendableCell.removeEventListener(SocketConnectionEvent.SENDABLE_PACKET_ACK_TIME_OUT, this);
				socketUdpSendableCell.dispose();
			}
			sentWindowSequenceList.clear();
			sentWindowSequenceList = null;
			datagramChannel=null;
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
