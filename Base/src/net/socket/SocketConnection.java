package base.net.socket;

import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.misc.BASE64Encoder;
import base.Base;
import base.event.Event;
import base.event.EventDispatcher;
import base.event.IEventListener;
import base.net.socket.events.SocketConnectionEvent;
import base.net.socket.packet.ISocketReceivablePacket;
import base.net.socket.packet.ISocketSendablePacketBase;
import base.net.socket.tcp.ISocketTcpConnection;
import base.net.socket.tcp.SocketTcpConnection;
import base.net.socket.udp.SocketUdpConnection;
import base.types.SocketDataStructureType;
import base.types.SocketType;
import base.util.ByteUtil;
import base.util.CryptoUtil;
import base.util.DateUtil;
import base.util.RC4Util;
import base.util.RSAUtil;
import base.util.WebSocketUtil;

/**
 * Socket连接基类
 * @author Devin
 *
 */
public class SocketConnection extends EventDispatcher implements IEventListener, ISocketConnection
{
	/**
	 * 线程读写锁
	 */
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();
	/**
	 * 当前连接对象是否可用
	 */
	protected boolean isAvailable = false;

	/**
	 * 当前连接对象是否可用
	 * @return
	 */
	public boolean getIsAvailable()
	{
		lock.readLock().lock();
		try
		{
			return isAvailable;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 当前连接对象是否为WebSocket
	 */
	protected boolean isWebSocket = false;

	/**
	 * @param isWebSocket 要设置的 isWebSocket
	 */
	public void setIsWebSocket(boolean isWebSocket)
	{
		this.isWebSocket = isWebSocket;
	}

	/**
	 * 当前连接对象是否为WebSocket
	 * @return
	 */
	public boolean getIsWebSocket()
	{
		return isWebSocket;
	}

	/**
	 * 标识ID，全局唯一值
	 */
	private int id;

	/**
	 * 标识ID，全局唯一值
	 * @return id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * SocketConnection连接校验码
	 */
	private int connectionCheckCode;

	/**
	 * SocketConnectionBase连接校验码
	 * @return connectionCheckCode
	 */
	public int getConnectionCheckCode()
	{
		return connectionCheckCode;
	}

	/**
	 * RSA公钥
	 */
	private RSAPublicKey publicKey;
	/**
	 * RSA公钥
	 */
	public RSAPublicKey getPublicKey()
	{
		return publicKey;
	}

	/**
	 * RSA私钥
	 */
	private RSAPrivateKey privateKey;
	/**
	 * RSA私钥
	 */
	public RSAPrivateKey getPrivateKey()
	{
		return privateKey;
	}

	/**
	 * 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	protected boolean isCrypto;

	/**
	 * 通讯数据包加密密码
	 */
	private int[] cryptoKeys;

	/**
	 * 通讯数据包加密密码
	 * @return cryptoKeys
	 */
	public int[] getCryptoKeys()
	{
		return cryptoKeys;
	}

	/**
	 * 通讯数据包加密密码
	 */
	public void setCryptoKeys(int[] cryptoKeys)
	{
		this.cryptoKeys=cryptoKeys;
		socketUdpConnection.setCryptoKeys(cryptoKeys);
	}

	/**
	 * TCP连接是否握手完成
	 */
	private boolean handshakeCompleteTcp = false;

	/**
	 * TCP连接是否握手完成
	 * @return handshakeCompleteTcp
	 */
	public boolean getHandshakeCompleteTcp()
	{
		return handshakeCompleteTcp;
	}

	/**
	 * TCP连接是否握手完成
	 * @return handshakeCompleteTcp
	 */
	public void setHandshakeCompleteTcp(boolean handshakeCompleteTcp)
	{
		if(this.handshakeCompleteTcp==handshakeCompleteTcp)return;
		this.handshakeCompleteTcp=handshakeCompleteTcp;
		if(this.handshakeCompleteTcp)
		{
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.HANDSHAKE_COMPLETE, SocketType.TCP));
		}
	}

	/**
	 * UDP连接是否握手完成
	 */
	private boolean handshakeCompleteUdp = false;

	/**
	 * UDP连接是否握手完成
	 */
	public boolean getHandshakeCompleteUdp()
	{
		return handshakeCompleteUdp;
	}

	/**
	 * UDP连接是否握手完成
	 */
	public void setHandshakeCompleteUdp(boolean handshakeCompleteUdp)
	{
		if(this.handshakeCompleteUdp==handshakeCompleteUdp)return;
		this.handshakeCompleteUdp=handshakeCompleteUdp;
		if(this.handshakeCompleteUdp)
		{
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.HANDSHAKE_COMPLETE, SocketType.UDP));
		}
	}

	/**
	 * Tcp连接
	 */
	protected ISocketTcpConnection socketTcpConnection;

	/**
	 * Tcp连接
	 * @return socketTcpConnection
	 */
	public ISocketTcpConnection getSocketTcpConnection()
	{
		return socketTcpConnection;
	}

	/**
	 * Udp连接
	 */
	private SocketUdpConnection socketUdpConnection;

	/**
	 * Udp连接
	 * @return socketUdpConnection
	 */
	public SocketUdpConnection getSocketUdpConnection()
	{
		return socketUdpConnection;
	}

	/**
	 * tcp连接中当前已接收未处理的字节缓冲
	 */
	private ByteBuffer receiveTcpByteBuffer;
	/**
	 * udp连接中当前已接收未处理的字节缓冲
	 */
	private ByteBuffer receiveUdpByteBuffer;

	/**
	 * 待发送包队列
	 */
	private BlockingQueue<ISocketSendablePacketBase> sendablePacketQueue = new LinkedBlockingQueue<ISocketSendablePacketBase>();

	/**
	 * 当前TCP接收最大数据包序号，此序号与发送方需保持一致
	 */
	private int tcpReceivePacketNumberMax = 0;

	/**
	 * 当前TCP发送最大数据包序号，此序号与发送方需保持一致
	 */
	private int tcpSendPacketNumberMax = 0;

	/**
	 * 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 */
	private boolean dataTransfer;

	/**
	 * 接收到的未处理的ByteBuffer
	 */
	private BlockingQueue<ByteBuffer> receiveTcpQueue = new LinkedBlockingQueue<ByteBuffer>();

	/**
	 * 接收到的未处理的ByteBuffer
	 */
	private BlockingQueue<ByteBuffer> receiveUdpQueue = new LinkedBlockingQueue<ByteBuffer>();

	/**
	 * 是否正在处理接收到的数据
	 */
	private boolean isHandleReceive=false;

	/**
	 * ByteBuffer池
	 */
	private SocketByteBufferPool socketByteBufferPool;
	/**
	 * ByteBuffer池
	 */
	public SocketByteBufferPool getSocketByteBufferPool()
	{
		return this.socketByteBufferPool;
	}
	/**
	 * ByteBuffer池
	 */
	public void setSocketByteBufferPool(SocketByteBufferPool socketByteBufferPool)
	{
		this.socketByteBufferPool=socketByteBufferPool;
	}

	/**
	 * RSA密钥
	 */
	private HashMap<String, Object> rsaKeys;
	/**
	 * RSA密钥
	 */
	public void setRSAKeys(HashMap<String, Object> rsaKeys)
	{
		this.rsaKeys=rsaKeys;
		this.publicKey = (RSAPublicKey) rsaKeys.get("public");
		this.privateKey = (RSAPrivateKey) rsaKeys.get("private");
	}
	/**
	 * RSA密钥
	 */
	public HashMap<String, Object> getRSAKeys()
	{
		return rsaKeys;
	}

	/**
	 * Socket连接基类
	 */
	public SocketConnection()
	{
	}

	/**
	 * 初始化
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @throws IOException
	 */
	public void init(boolean dataTransfer, boolean isCrypto)
	{
		this.dataTransfer = dataTransfer;
		this.isCrypto = isCrypto;
		receiveTcpByteBuffer = ByteBuffer.allocate(SocketConfig.PACKET_TCP_LENGTN);
		receiveTcpByteBuffer.order(SocketConfig.byteOrder);
		receiveTcpByteBuffer.flip();
		receiveUdpByteBuffer = ByteBuffer.allocate(SocketConfig.PACKET_UDP_LENGTN);
		receiveUdpByteBuffer.order(SocketConfig.byteOrder);
		receiveUdpByteBuffer.flip();
		//		receiveWebsocketByteBuffer = ByteBuffer.allocate(SocketConfig.PACKET_UDP_LENGTN);
		//		receiveWebsocketByteBuffer.order(SocketConfig.byteOrder);
		//		receiveWebsocketByteBuffer.flip();
	}

	/**
	 * 取得接受的数据，并创建接收包予队列中
	 * @param socketType Socket通讯模式
	 * @param byteBuffer 从通道中读取到的ByteBuffer
	 */
	public void receiveData(SocketType socketType, ByteBuffer byteBuffer)
	{
		try
		{
			if(socketType==SocketType.TCP)
			{
				receiveTcpQueue.put(byteBuffer);
			}
			else if(socketType==SocketType.UDP)
			{
				receiveUdpQueue.put(byteBuffer);
			}
		}
		catch (InterruptedException e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, "读取Socket数据包错误。", e);
		}

		//		if (isWebSocket)
		//		{// 为websocket时，接受的数据需要处理数据帧头
		//			receiveDataWebsocket(socketType, byteBuffer);
		//		}
		//		else
		//		{
		//			receiveDataTcpUdp(socketType, byteBuffer);
		//		}
	}

	/**
	 * 取得Tcp/Udp接受的数据，并创建接收包予队列中
	 * @param socketType Socket通讯模式
	 * @param byteBuffer 从通道中读取到的ByteBuffer
	 */
	public void receiveDataTcpUdp(SocketType socketType)
	{
		lock.writeLock().lock();
		try
		{
			if(!isAvailable || isHandleReceive || (receiveTcpQueue.size()<=0 && receiveUdpQueue.size()<=0))
			{
				return;
			}
			isHandleReceive=true;
			ByteBuffer byteBuffer;
			ByteBuffer receiveByteBuffer = null;
			if (socketType == SocketType.TCP)
			{
				if(socketTcpConnection.getIsAvailable())
				{
					receiveByteBuffer = receiveTcpByteBuffer;
					while (isAvailable && (byteBuffer = receiveTcpQueue.poll()) != null)
					{
						// System.out.println("收到数据(receiveData)："+socketType);
						if (byteBuffer != null && byteBuffer.remaining() > 0)
						{
							receiveByteBuffer.compact();
							if (receiveByteBuffer.remaining() < byteBuffer.remaining())
							{// 如果用来装载当次接收的字节缓冲容量不够时，就需要扩充装载缓冲的容量
								ByteBuffer bb = ByteBuffer.allocate(receiveByteBuffer.capacity() + byteBuffer.remaining());
								bb.order(SocketConfig.byteOrder);
								receiveByteBuffer.flip();
								bb.put(receiveByteBuffer);
								receiveByteBuffer.clear();
								receiveByteBuffer = null;
								receiveTcpByteBuffer = bb;
								receiveByteBuffer = bb;
							}
							// System.out.println("receiveDataTcpUdp：byteBuffer.remaining():"+byteBuffer.remaining() + " receiveByteBuffer.remaining():"+receiveByteBuffer.remaining());
							if (byteBuffer.remaining() > 0)
								receiveByteBuffer.put(byteBuffer);// 如果通道缓冲区有数据可用，装载缓冲装载通道缓冲区的数据
							receiveByteBuffer.flip();
							socketByteBufferPool.releaseByteBuffer(byteBuffer);
						}
					}
				}
			}
			else if(socketType == SocketType.UDP)
			{
				receiveByteBuffer = receiveUdpByteBuffer;
				if (socketUdpConnection != null && socketUdpConnection.getIsAvailable())
				{
					while (isAvailable && (byteBuffer = receiveUdpQueue.poll()) != null)
					{
						// System.out.println("收到数据(receiveData)："+socketType);
						if (byteBuffer != null && byteBuffer.remaining() > 0)
						{
							receiveByteBuffer.compact();
							if (receiveByteBuffer.remaining() < byteBuffer.remaining())
							{// 如果用来装载当次接收的字节缓冲容量不够时，就需要扩充装载缓冲的容量
								ByteBuffer bb = ByteBuffer.allocate(receiveByteBuffer.capacity() + byteBuffer.remaining());
								bb.order(SocketConfig.byteOrder);
								receiveByteBuffer.flip();
								bb.put(receiveByteBuffer);
								receiveByteBuffer.clear();
								receiveByteBuffer = null;
								receiveUdpByteBuffer = bb;
								receiveByteBuffer = bb;
							}
							// System.out.println("receiveDataTcpUdp：byteBuffer.remaining():"+byteBuffer.remaining() + " receiveByteBuffer.remaining():"+receiveByteBuffer.remaining());
							if (byteBuffer.remaining() > 0)
								receiveByteBuffer.put(byteBuffer);// 如果通道缓冲区有数据可用，装载缓冲装载通道缓冲区的数据
							receiveByteBuffer.flip();
							socketByteBufferPool.releaseByteBuffer(byteBuffer);
						}
					}
				}
			}

			handleReceiveByteBuffer(socketType);
			isHandleReceive=false;
		}
		catch (Exception e)
		{
			isHandleReceive=false;
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, "读取Socket数据包错误。", e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 是否已读取过包种类标识
	 */
	private boolean isReadPacketType = false;
	/**
	 * 是否已读取过包头
	 */
	private boolean isReadHead = false;
	/**
	 * 包头长度
	 */
	private byte packetHeadLength = 0;
	/**
	 * 包体长度
	 */
	private int packetBodyLength = 0;
	/**
	 * 包尾长度
	 */
	private byte packetEndLength = 0;
	/**
	 * 当前数据包序号
	 */
	private int receivePacketNumber = 0;
	/**
	 * 包种类标识
	 */
	private byte socketDataStructureType;

	/**
	 * 连接上次收到心跳包的时间
	 */
	private long keepAliveTime;

	/**
	 * 处理Tcp/Udp待处理数据
	 * @param socketType SocketType
	 */
	@SuppressWarnings("unused")
	private void handleReceiveByteBuffer(SocketType socketType)
	{
		lock.writeLock().lock();
		try
		{
			if(!isAvailable)
			{
				return;
			}
			ByteBuffer receiveByteBuffer = null;
			if (socketType == SocketType.UDP)
			{
				receiveByteBuffer = receiveUdpByteBuffer;
			}
			else
			{
				receiveByteBuffer = receiveTcpByteBuffer;
			}

			if (!isReadPacketType && receiveByteBuffer.remaining() >= SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH)
			{// 取得包种类标识
				socketDataStructureType = receiveByteBuffer.get();// 取得包种类标识
				switch (socketDataStructureType)
				{
					case SocketDataStructureType.KEEP_ALIVE:// 控制包-协议心跳保活
						isReadPacketType = false;// 该类包直接收取一个字节，无后续数据，则直接重新标识读取下一个包
						keepAlive(socketType);
						handleReceiveByteBuffer(socketType);// 直接读取下一个包
						break;
					case SocketDataStructureType.CONNECTION_TCP:// 控制包-TCP协议连接成功(发送端(客户端)TCP向接收端(服务端)TCP发送)
						//						System.out.println("控制包-TCP协议连接成功(发送端(客户端)TCP向接收端(服务端)TCP发送)："+this.socketTcpConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+this.socketTcpConnection.getSocketChannel().socket().getPort());
						tcpToTcpHandshake();
						break;
					case SocketDataStructureType.HANDSHAKE_TCP:// 控制包-TCP协议握手
					case SocketDataStructureType.HANDSHAKE_TCP_ACK:// 控制包-TCP协议握手应答
						//						System.out.println("控制包-TCP协议握手/控制包-TCP协议握手应答："+this.socketTcpConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+this.socketTcpConnection.getSocketChannel().socket().getPort());
						isReadPacketType = true;
						packetHeadLength = SocketConfig.PACKET_BODY_LENGTH;
						packetEndLength = 0;
						break;
					case SocketDataStructureType.HANDSHAKE_TCP_ACK_ACK:// 控制包-TCP协议握手应答的应答
						//						System.out.println("SocketConnection handleReceiveByteBuffer 控制包-TCP协议握手应答的应答/控制包-TCP协议握手应答的应答："+this.socketTcpConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+this.socketTcpConnection.getSocketChannel().socket().getPort());
						isReadPacketType = false;
						setHandshakeCompleteTcp(true);
						if (socketUdpConnection != null)
						{
							udpToUdpHandshake();// 当前SocketConnectionBase得到由服务器发送过来的握手数据后，执行UDP握手数据发送(UDP握手数据发送由发送端(客户端)主动发起给接收端(服务器端))
						}
						handleReceiveByteBuffer(socketType);// 直接读取下一个包
						break;
					case SocketDataStructureType.HANDSHAKE_UDP_ACK:// 控制包-UDP协议握手成功应答
						//						System.out.println("UDP协议握手成功应答：" + socketType);
						setHandshakeCompleteUdp(true);
						isReadPacketType = false;
						handleReceiveByteBuffer(socketType);// 直接读取下一个包
						break;
					case SocketDataStructureType.DATA:// 数据包
						if ((socketType == SocketType.UDP && !handshakeCompleteUdp) || (socketType==SocketType.TCP && !handshakeCompleteTcp))
						{//如果连接还没有握手完成，不允许数据包通讯，一般为非法连接
							Logger.getLogger(SocketConnection.class.getName()).log(Level.INFO, "出现非法连接，在连接协议未握手的状态下进行数据包通讯，连接将会被强制关闭。");
							close();//直接关闭连接
							return;
						}
						isReadPacketType = true;
						if (socketType == SocketType.UDP)
						{
							packetHeadLength = SocketConfig.PACKET_BODY_LENGTH;
							packetEndLength = 0;
						}
						else
						{
							packetHeadLength = SocketConfig.PACKET_NUMBER_LENGTH + SocketConfig.PACKET_BODY_LENGTH;
							if (isCrypto)
							{
								packetEndLength = SocketConfig.PACKET_END_LENGTH;
							}
							else
							{
								packetEndLength = 0;
							}
						}
						break;
				}
			}

			if (isReadPacketType && !isReadHead && receiveByteBuffer.remaining() >= packetHeadLength)
			{// 取得包头
				receivePacketNumber = 0;
				if (socketType == SocketType.TCP)
				{
					switch (socketDataStructureType)
					{
						case SocketDataStructureType.DATA:
							receivePacketNumber = receiveByteBuffer.getInt();// 当前数据包序号
							break;
					}
				}
				packetBodyLength = receiveByteBuffer.getInt();// 取得包体数据长度(不包括包头，包尾)
				isReadHead = true;
			}
			if (isReadHead && receiveByteBuffer.remaining() >= packetBodyLength + packetEndLength)
			{// 已经取得了包头，并且当前装载缓冲区的数据满足包体加包尾的长度，则进行完整包数据填充
				byte[] bodyBytes = new byte[packetBodyLength];
				receiveByteBuffer.get(bodyBytes);// 取得包体数据

				if (socketType == SocketType.TCP)
				{
					byte[] endCode = null;
					if (isCrypto && packetEndLength > 0 && receiveByteBuffer.remaining() >= packetEndLength)
					{
						endCode = new byte[packetEndLength];
						receiveByteBuffer.get(endCode);
					}
					try
					{
						switch (socketDataStructureType)
						{
							case SocketDataStructureType.DATA:
								if (receivePacketNumber != tcpReceivePacketNumberMax)
								{// TCP包执行包序号校验，如果包序号校验不匹配，则丢弃包
									bodyBytes = null;
									System.out.println("TCP包执行包序号校验未通过 receivePacketNumber:" + receivePacketNumber + " tcpReceivePacketNumberMax:" + tcpReceivePacketNumberMax+" packetBodyLength:"+packetBodyLength+" packetEndLength:"+packetEndLength +" receiveByteBuffer.remaining():"+receiveByteBuffer.remaining()+" isHandleReceive:"+isHandleReceive);
								}
								else
								{
									boolean isPass = true;
									tcpReceivePacketNumberMax++;
									if (isCrypto)
									{// 通讯数据需要安全加密
										byte[] encryptCode = CryptoUtil.decryptionPacket(socketDataStructureType, id, connectionCheckCode, receivePacketNumber, bodyBytes, cryptoKeys);
										if (encryptCode != null)
										{
											if (!ByteUtil.bytesCompare(endCode, encryptCode))
											{
												isPass = false;
												System.out.println("包尾校验码未匹配:" + tcpReceivePacketNumberMax);
											}
										}
										else
										{
											close();
										}
									}
									if (isPass)
									{
										ByteBuffer packByteBuffer = ByteBuffer.wrap(bodyBytes);
										packByteBuffer.order(SocketConfig.byteOrder);
										receiveByteBufferComplete(socketType, socketDataStructureType, packByteBuffer);
										// System.out.println("完成包_tcpReceivePacketNumberMax:"+tcpReceivePacketNumberMax);
									}
								}
								break;
							case SocketDataStructureType.HANDSHAKE_TCP:// TCP协议握手(由TCP接收端(服务端)发送握手数据给TCP发送端(客户端))，说明当前的SocketConnectionBase为客户端连接，需要设置由服务器发过来的握手数据
								ByteBuffer packByteBuffer = ByteBuffer.wrap(bodyBytes);
								packByteBuffer.order(SocketConfig.byteOrder);
								id = packByteBuffer.getInt();// 取得并设置当前SocketConnectionBase标识ID
								connectionCheckCode = packByteBuffer.getInt();// 取得并设置当前SocketConnectionBase连接校验码
								int modulusLen = packByteBuffer.getInt();
								byte[] modulus = new byte[modulusLen];
								packByteBuffer.get(modulus);
								int publicExponentLen = packByteBuffer.getInt();
								byte[] publicExponent = new byte[publicExponentLen];
								packByteBuffer.get(publicExponent);
								PublicKey publicKey = RSAUtil.getPublicKey(modulus, publicExponent);// 生成RSA公钥
								String cryptoStr = CryptoUtil.genRandomString(32);// 生成一个32字节长度的随机字符串，用做密码
								byte[] cryptoKeysBytes = cryptoStr.getBytes();
								cryptoKeys = RC4Util.getKey(cryptoKeysBytes);// 生成通讯数据加密密码bytes，用与发给服务器，以及客户端自已加密用;
								byte[] rsaCryptoKeys = RSAUtil.encrypt(publicKey, cryptoKeysBytes);// 将通讯密码使用公钥加密
								rsaCryptoKeys = cryptoKeysBytes;// 暂时用rc4明文做为密码，以后再改成rsa加密的
								if (rsaCryptoKeys != null)
								{
									if (socketUdpConnection != null)
									{
										socketUdpConnection.setConnectionInfo(id, connectionCheckCode);
										socketUdpConnection.setCryptoKeys(cryptoKeys);
									}
									tcpToTcpHandshakeAck(rsaCryptoKeys);
								}
								else
								{
									close();
								}
								break;
							case SocketDataStructureType.HANDSHAKE_TCP_ACK:
								if (isWebSocket)
								{
									String strBodyBytes = ByteUtil.getString(bodyBytes);
									System.out.println(strBodyBytes);
									System.out.println("bodyBytes RSA原字节长度:" + bodyBytes.length);
									bodyBytes = new BigInteger(strBodyBytes, 16).toByteArray();
									System.out.println("bodyBytes RSA BigInteger后字节长度:" + bodyBytes.length);
								}
								// byte[] decryptBodyBytes = RSAUtil.decryption(privateKey, bodyBytes);
								byte[] decryptBodyBytes = bodyBytes;// 暂时用收到的数据做为密码源，以后再改
								if (decryptBodyBytes != null)
								{
									if (isWebSocket)
									{
										String decryptBodyStr = ByteUtil.getString(decryptBodyBytes);
										StringBuilder sb = new StringBuilder();
										sb.append(decryptBodyStr);
										sb.reverse();
										decryptBodyStr = sb.toString().trim();
										System.out.println("decryptBodyStr:" + decryptBodyStr);
										decryptBodyBytes = ByteUtil.getBytes(decryptBodyStr);
									}
									cryptoKeys = RC4Util.getKey(decryptBodyBytes);// 取得由rsa私钥解密出来的通讯密码
									if (cryptoKeys != null)
									{
										setHandshakeCompleteTcp(true);// 设置当前连接TCP握手成功，此连接为服务端内的客户端连接
										tcpToTcpHandshakeAckAck();
									}
									else
									{// 如果出现取得RC4加密Keys错误，则关闭错误
										close();
									}
								}
								else
								{
									close();
								}
								break;
						}
					}
					catch (Exception e)
					{
						tcpReceivePacketNumberMax++;
						Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, "读取Socket数据包错误。", e);
					}
				}
				else
				{
					ByteBuffer packByteBuffer = ByteBuffer.wrap(bodyBytes);
					packByteBuffer.order(SocketConfig.byteOrder);
					receiveByteBufferComplete(socketType, socketDataStructureType, packByteBuffer);
				}

				isReadPacketType = false;
				isReadHead = false;
				handleReceiveByteBuffer(socketType);
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, "读取Socket数据包错误。", e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	// -------------------------------------------websocket start-----------------------------------------------
	//	/**
	//	 * Websocket连接中当前已接收未处理的字节缓冲
	//	 */
	//	protected ByteBuffer receiveWebsocketByteBuffer;
	//
	//	/**
	//	 * 取得Websocket接受的数据，并创建接收包予队列中
	//	 * @param socketType Socket通讯模式
	//	 * @param byteBuffer 从通道中读取到的ByteBuffer
	//	 */
	//	private void receiveDataWebsocket(SocketType socketType, ByteBuffer byteBuffer)
	//	{
	//		lock.writeLock().lock();
	//		try
	//		{
	//			if (isAvailable)
	//			{
	//				receiveWebsocketByteBuffer.compact();
	//				if (receiveWebsocketByteBuffer.remaining() < byteBuffer.remaining())
	//				{// 如果用来装载当次接收的字节缓冲容量不够时，就需要扩充装载缓冲的容量
	//					ByteBuffer bb = ByteBuffer.allocate(receiveWebsocketByteBuffer.capacity() + byteBuffer.remaining());
	//					bb.order(SocketConfig.byteOrder);
	//					receiveWebsocketByteBuffer.flip();
	//					bb.put(receiveWebsocketByteBuffer);
	//					receiveWebsocketByteBuffer.clear();
	//					receiveWebsocketByteBuffer = null;
	//					receiveWebsocketByteBuffer = bb;
	//				}
	//				if (byteBuffer.remaining() > 0)
	//					receiveWebsocketByteBuffer.put(byteBuffer);
	//				receiveWebsocketByteBuffer.flip();
	//				handleReceiveDataWebsocket(socketType);
	//			}
	//		}
	//		catch (Exception e)
	//		{
	//			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, "读取Socket数据包错误。", e);
	//		}
	//		finally
	//		{
	//			lock.writeLock().unlock();
	//		}
	//	}
	//
	//	/*
	//	 * Websocket版本数据帧结构图 0 1 2 3 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 +-+-+-+-+-------+-+-------------+-------------------------------+ |F|R|R|R| opcode|M| Payload len | Extended payload length | |I|S|S|S| (4) |A| (7) | (16/64) | |N|V|V|V| |S| | (if payload len==126/127) |
	//	 * | |1|2|3| |K| | | +-+-+-+-+-------+-+-------------+ - - - - - - - - - - - - - - - + | Extended payload length continued, if payload len == 127 | + - - - - - - - - - - - - - - - +-------------------------------+ | |Masking-key, if MASK set to 1 |
	//	 * +-------------------------------+-------------------------------+ | Masking-key (continued) | Payload Data | +-------------------------------- - - - - - - - - - - - - - - - + : Payload Data continued ... : + - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - + | Payload Data
	//	 * continued ... | +---------------------------------------------------------------+
	//	 */
	//	/**
	//	 * 数据帧头
	//	 */
	//	private byte[] frameHeadBytes;
	//	/**
	//	 * 数据帧负载数据总长度
	//	 */
	//	private long payloadDataLen = 0;
	//	/**
	//	 * 掩码值
	//	 */
	//	private byte[] maskCode = new byte[4];
	//	/**
	//	 * 是否已经读取了websocket数据帧头部
	//	 */
	//	private boolean isReadWebsocketFrameHead = false;
	//	/**
	//	 * 是否已经读取了websocket数据帧全部数据
	//	 */
	//	private boolean isReadWebsocketFrame = false;
	//
	//	/**
	//	 * 取得Websocket接受的数据，并创建接收包予队列中
	//	 * @param socketType Socket通讯模式
	//	 */
	//	private void handleReceiveDataWebsocket(SocketType socketType)
	//	{
	//		if (receiveWebsocketByteBuffer.remaining() < WebSocketType.FRAME_MIN_LENGTH)
	//		{// 当前缓冲字大小如果小于websocket帧头最小尺寸，则表示数据不够，直接返回
	//			return;
	//		}
	//		if (!isReadWebsocketFrameHead && !isReadWebsocketFrame)
	//		{
	//			frameHeadBytes = new byte[WebSocketType.FRAME_MIN_LENGTH];
	//			receiveWebsocketByteBuffer.get(frameHeadBytes, 0, frameHeadBytes.length);
	//			isReadWebsocketFrameHead = true;
	//		}
	//
	//		byte[] frameBits = ByteUtil.byteToBit(frameHeadBytes[0]);
	//
	//		// 取得RSV1、RSV2、RSV3
	//		String strRsv = ByteUtil.bytesToString(frameBits, 1, 3);// 取得Opcode的bit值
	//		byte rsv = ByteUtil.bitToByte(strRsv);
	//		if (rsv != 0x0)
	//		{// 如果rsv值不为0，则直接关闭连接
	//			close();
	//			return;
	//		}
	//
	//		String strOpcode = ByteUtil.bytesToString(frameBits, 4, 4);// 取得Opcode的bit值
	//		byte opcode = ByteUtil.bitToByte(strOpcode);// 取得Opcode值
	//
	//		// 0x0 代表一个继续帧
	//		// 0x1 代表一个文本帧
	//		// 0x2 代表一个二进制帧
	//		// 0x3-7 保留用于未来的非控制帧
	//		// 0x8 代表连接关闭
	//		// 0x9 代表ping
	//		// 0xA 代表pong
	//		// 0xB-F 保留用于未来的控制帧
	//		switch (opcode)
	//		{
	//			case 0x0:// 代表一个继续帧
	//				break;
	//			case 0x1:// 代表一个文本帧
	//				close();// 不接受文本帧，如果为文本帧则直接关闭连接
	//				return;
	//			case 0x2:// 代表一个二进制帧
	//				break;
	//			case 0x8:// 代表连接关闭
	//				close();// 关闭连接
	//				return;
	//			case 0x9:// 代表ping
	//				System.out.println("收到Websocket Ping帧");
	//				// sendPongFrame();//回复一个pong帧
	//				break;
	//			case 0xA:// 代表pong
	//				System.out.println("收到Websocket Pong帧");
	//				// 不响应或不处理
	//				break;
	//		}
	//
	//		frameBits = ByteUtil.byteToBit(frameHeadBytes[1]);
	//		String strMask = ByteUtil.bytesToString(frameBits, 0, 1);// 取得Mask的bit值
	//		byte mask = ByteUtil.bitToByte(strMask);// 取得Mask值
	//		boolean isMask = mask == 0x1;// 是否有掩码Mask
	//
	//		String strPayloadLen = ByteUtil.bytesToString(frameBits, 1, 7);// 取得Payload Len的bit值
	//		byte payloadLen = ByteUtil.bitToByte(strPayloadLen);// 取得Payload Len值
	//
	//		if (!isReadWebsocketFrame)
	//		{
	//			if (payloadLen == 126)
	//			{
	//				if (isMask)
	//				{// 如果有掩码
	//					if (receiveWebsocketByteBuffer.remaining() >= 6)
	//					{// 当前缓冲区数据进行长度>=2(负载数据长度)+4(掩码长度)=6字节，则说明当前数据包数据当前处理是足够的
	//						payloadDataLen = receiveWebsocketByteBuffer.getShort();// 取得负载数据长度
	//						receiveWebsocketByteBuffer.get(maskCode, 0, maskCode.length);// 取得掩码
	//						isReadWebsocketFrameHead = false;
	//						isReadWebsocketFrame = true;
	//					}
	//				}
	//				else
	//				{// 没有掩码
	//					if (receiveWebsocketByteBuffer.remaining() >= 2)
	//					{// 当前缓冲区数据进行长度小于2(负载数据长度)字节，则说明当前数据包数据当前处理是足够的
	//						payloadDataLen = receiveWebsocketByteBuffer.getShort();// 取得负载数据长度
	//						isReadWebsocketFrameHead = false;
	//						isReadWebsocketFrame = true;
	//					}
	//				}
	//			}
	//			else if (payloadLen == 127)
	//			{
	//				if (isMask)
	//				{// 如果有掩码
	//					if (receiveWebsocketByteBuffer.remaining() >= 12)
	//					{// 当前缓冲区数据进行长度>=8(负载数据长度)+4(掩码长度)=12字节，则说明当前数据包数据当前处理是足够的
	//						payloadDataLen = receiveWebsocketByteBuffer.getLong();// 取得负载数据长度
	//						receiveWebsocketByteBuffer.get(maskCode, 0, maskCode.length);// 取得掩码
	//						isReadWebsocketFrameHead = false;
	//						isReadWebsocketFrame = true;
	//					}
	//				}
	//				else
	//				{// 没有掩码
	//					if (receiveWebsocketByteBuffer.remaining() >= 8)
	//					{// 当前缓冲区数据进行长度>=8(负载数据长度)字节，则说明当前数据包数据当前处理是足够的
	//						payloadDataLen = receiveWebsocketByteBuffer.getLong();// 取得负载数据长度
	//						isReadWebsocketFrameHead = false;
	//						isReadWebsocketFrame = true;
	//					}
	//				}
	//			}
	//			else
	//			{
	//				payloadDataLen = payloadLen;// 取得负载数据长度
	//				if (isMask)
	//				{// 如果有掩码
	//					if (receiveWebsocketByteBuffer.remaining() >= 4)
	//					{// 当前缓冲区数据进行长度小于4(掩码长度)字节，则说明当前数据包数据当前处理是足够的
	//						receiveWebsocketByteBuffer.get(maskCode, 0, maskCode.length);// 取得掩码
	//						isReadWebsocketFrameHead = false;
	//						isReadWebsocketFrame = true;
	//					}
	//				}
	//				else
	//				{
	//					isReadWebsocketFrameHead = false;
	//					isReadWebsocketFrame = true;
	//				}
	//			}
	//		}
	//
	//		if (isReadWebsocketFrame && receiveWebsocketByteBuffer.remaining() >= payloadDataLen)
	//		{
	//			ByteBuffer byteBufferData = ByteBuffer.allocate((int) payloadDataLen);
	//			byteBufferData.order(SocketConfig.byteOrder);
	//			byte[] dataBytes = new byte[(int) payloadDataLen];
	//			receiveWebsocketByteBuffer.get(dataBytes, 0, dataBytes.length);
	//			WebSocketUtil.formatWebSocketBytes(dataBytes, maskCode);
	//			byteBufferData.put(dataBytes);
	//			byteBufferData.flip();
	////			receiveDataTcpUdp(socketType, byteBufferData);
	//			isReadWebsocketFrame = false;
	//			handleReceiveDataWebsocket(socketType);
	//		}
	//	}

	// -------------------------------------------websocket end-----------------------------------------------

	/**
	 * 接受数据完成，最后操作
	 * @param socketType SocketType
	 * @param packByteBuffer 数据体
	 */
	private void receiveByteBufferComplete(SocketType socketType, byte socketDataStructureType, ByteBuffer packByteBuffer)
	{
		try
		{
			ISocketReceivablePacket receivablePacket = null;
			if (dataTransfer)
			{
				receivablePacket = SocketUtil.socketReceivablePacket(socketType, packByteBuffer, packByteBuffer.getShort(), packByteBuffer.getLong());// 创建接收到的数据包
			}
			else
			{
				receivablePacket = SocketUtil.socketReceivablePacket(socketType, packByteBuffer, packByteBuffer.getShort());// 创建接收到的数据包
			}
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.RECEIVABLE_PACKET, receivablePacket));
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 向发送队列增加发送数据包
	 * @param socketSendablePacketBase 数据包
	 * @throws IOException 
	 */
	public void addQueueSendPacket(ISocketSendablePacketBase socketSendablePacketBase)
	{
		try
		{
			if (isAvailable)
			{
				sendablePacketQueue.put(socketSendablePacketBase);
			}
		}
		catch (InterruptedException e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	/**
	 * 执行包发送
	 */
	public void sendPacket()
	{
		if (isAvailable)
		{
			sendSendablePacketQueue();// 执行发送包队列发送
		}
	}

	/**
	 * 向发送队列增加发送数据包并执行发送
	 * @param socketSendablePacketBase 数据包
	 * @throws IOException 
	 */
	public void sendPacket(ISocketSendablePacketBase socketSendablePacketBase)
	{
		try
		{
			if (isAvailable)
			{
				sendablePacketQueue.put(socketSendablePacketBase);
				sendSendablePacketQueue();
			}
		}
		catch (InterruptedException e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	//	/**
	//	 * 向发送队列增加发送数据包并执行发送
	//	 * @param socketSendablePacketBase 数据包
	//	 * @throws IOException 
	//	 */
	//	public void sendPacket(ISocketSendablePacketBase socketSendablePacketBase)
	//	{
	//		try
	//		{
	//			if (isAvailable)
	//			{
	//				if (socketSendablePacketBase.getSocketType() == SocketType.UDP)
	//				{
	//					udpSendablePacketQueue.put(socketSendablePacketBase);// 将发送包装载至udp发送队列
	//					sendUdpSendablePacketQueue();// 执行udp发送包队列发送
	//				}
	//				else
	//				{
	//					tcpSendablePacketQueue.put(socketSendablePacketBase);// 将发送包装载至tcp发送队列
	//					sendTcpSendablePacketQueue();// 执行tcp发送包队列发送
	//				}
	//			}
	//		}
	//		catch (InterruptedException e)
	//		{
	//			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
	//		}
	//	}

	/**
	 * 执行发送包队列发送
	 */
	public void sendSendablePacketQueue()
	{
		lock.writeLock().lock();
		try
		{
			if (isAvailable)
			{
				boolean isUdp=false;
				ISocketSendablePacketBase sendablePacket = null;
				while (isAvailable && sendablePacketQueue!=null && (sendablePacket = sendablePacketQueue.poll()) != null)
				{// 循环取得发送包队列中的包予发送
					if(sendablePacket.getSocketType()==SocketType.ALL)
					{
						if(this.handshakeCompleteUdp)
						{//如果udp已握手，优先udp连接
							sendablePacket.setSocketType(SocketType.UDP);
						}
						else
						{
							sendablePacket.setSocketType(SocketType.TCP);
						}
					}
					if(sendablePacket.getSocketType()==SocketType.TCP)
					{
						if(socketTcpConnection!=null)
						{
							switch (sendablePacket.getSocketDataStructureType())
							{
								case SocketDataStructureType.DATA:
									sendablePacket.end(isCrypto, tcpSendPacketNumberMax);
									if (isCrypto)
									{
										int headLen = SocketConfig.PACKET_STRUCTURE_TYPE_LENGTH + SocketConfig.PACKET_NUMBER_LENGTH + SocketConfig.PACKET_BODY_LENGTH;// 包头长度
										encryptPacket(headLen, tcpSendPacketNumberMax, sendablePacket);
									}
									tcpSendPacketNumberMax++;
									break;
								default:
									sendablePacket.end(isCrypto);
									break;
							}
							sendTcpPacket(sendablePacket);
						}

					}
					else if(sendablePacket.getSocketType()==SocketType.UDP)
					{
						if(socketUdpConnection!=null)
						{
							isUdp=true;
							sendablePacket.end(isCrypto);
							socketUdpConnection.addSendData(sendablePacket.getSocketDataStructureType(), sendablePacket.getData());
							sendablePacket = null;
						}
					}
				}
				if(isUdp)
				{
					socketUdpConnection.send();
				}
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 发送Tcp数据包
	 * @param sendablePacket 数据包
	 */
	protected void sendTcpPacket(ISocketSendablePacketBase sendablePacket)
	{
		if (isWebSocket)
		{// 为websocket时，发送数据需要经过数据帧处理
			ByteBuffer buffPacket = sendablePacket.getData();// 取得数据包数据
			byte[] frameHeadBytes = WebSocketUtil.buildFrameHead(buffPacket.remaining());// 取得Websocket帧头数据
			ByteBuffer webSocketBuffer = ByteBuffer.allocate(frameHeadBytes.length + buffPacket.remaining());// 创建新的缓冲区用来装载符合Websocket的数据
			webSocketBuffer.order(SocketConfig.byteOrder);
			webSocketBuffer.put(frameHeadBytes);
			webSocketBuffer.put(buffPacket);
			webSocketBuffer.flip();
			socketTcpConnection.send(webSocketBuffer);
			frameHeadBytes = null;
			webSocketBuffer = null;
		}
		else
		{
			if(sendablePacket.getMarkPosition()>-1)
			{
				sendablePacket.applyMarkPosition();//如果有标记位置，则发送之前置于之前标记位置
			}
			socketTcpConnection.send(sendablePacket.getData());
		}
		sendablePacket.dispose();
		sendablePacket = null;
	}

	private void encryptPacket(int headLen, int packetNumber, ISocketSendablePacketBase sendablePacket)
	{
		ByteBuffer byteBuffer = sendablePacket.getData();
		byte[] bodyBytes = new byte[byteBuffer.remaining() - headLen];
		int pos = byteBuffer.position();
		byteBuffer.position(pos + headLen);
		byteBuffer.get(bodyBytes);
		byteBuffer.position(pos + headLen);
		byte[] encryptCode = CryptoUtil.encryptPacket(sendablePacket.getSocketDataStructureType(), id, connectionCheckCode, packetNumber, bodyBytes, cryptoKeys);
		if (encryptCode != null)
		{
			byteBuffer.put(bodyBytes);// 加入包体
			byteBuffer.limit(byteBuffer.capacity());
			byteBuffer.put(encryptCode);// 加入包尾校验串
			byteBuffer.flip();
			byteBuffer.position(pos);
		}
		else
		{
			close();
		}
	}

	/**
	 * 设置Tcp通道并创建Tcp连接
	 * @param socketChannel SocketChannel
	 */
	public void setSocketChannel(SocketChannel socketChannel)
	{
		lock.writeLock().lock();
		try
		{
			if (socketTcpConnection == null)
			{
				socketTcpConnection = Base.newClass(SocketTcpConnection.class);
				socketTcpConnection.init(socketChannel, SocketConfig.PACKET_TCP_LENGTN);
				socketTcpConnection.addEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				keepAliveTime = DateUtil.getCurrentTimeMillis();
				isAvailable = true;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 设置Udp通道并创建Udp连接
	 * @param datagramChannel DatagramChannel
	 * @param socketAddress Udp通道的socketAddress
	 */
	public void setSocketChannel(DatagramChannel datagramChannel, SocketAddress socketAddress)
	{
		lock.writeLock().lock();
		try
		{
			if (socketUdpConnection == null)
			{
				socketUdpConnection = new SocketUdpConnection(datagramChannel, socketAddress, SocketConfig.PACKET_UDP_LENGTN, cryptoKeys, isCrypto);
				socketUdpConnection.setConnectionInfo(id, connectionCheckCode);
				socketUdpConnection.addEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				socketUdpConnection.addEventListener(SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP, this);
				socketUdpConnection.addEventListener(SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE, this);
				keepAliveTime = DateUtil.getCurrentTimeMillis();
				isAvailable = true;
				sendSendablePacketQueue();
			}
			else
			{
				socketUdpConnection.setSocketChannel(datagramChannel, socketAddress);
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 设置SocketConnectionBase标识
	 * @param id 标识ID，全局唯一
	 *  @param connectionCheckCode 校验码
	 */
	public void setMark(int id, int connectionCheckCode)
	{
		lock.writeLock().lock();
		try
		{
			this.id = id;
			this.connectionCheckCode = connectionCheckCode;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}

	/**
	 * 发送端(客户端)TCP向接收端(服务端)TCP发送连接成功
	 */
	public void clientToServerTcpConnecyionSuccess()
	{
		//		System.out.println("发送端(客户端)TCP向接收端(服务端)TCP发送连接成功："+this.socketTcpConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+this.socketTcpConnection.getSocketChannel().socket().getPort());
		ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.TCP, SocketDataStructureType.CONNECTION_TCP);
		sendPacket(socketSendablePacketClient);
	}

	/**
	 * 接收端(服务器端)TCP向发送端(客户端)TCP发送协议握手数据
	 */
	public void tcpToTcpHandshake()
	{
		//		System.out.println("接收端(服务器端)TCP向发送端(客户端)TCP发送协议握手数据 1："+this.socketTcpConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+this.socketTcpConnection.getSocketChannel().socket().getPort());
		lock.readLock().lock();
		try
		{
			// 生成RSA公、私钥
			publicKey = (RSAPublicKey) rsaKeys.get("public");
			privateKey = (RSAPrivateKey) rsaKeys.get("private");
			BigInteger bigIntegerModulus = publicKey.getModulus();
			BigInteger bigIntegerPublicExponent = publicKey.getPublicExponent();

			byte[] modulusBytes = null;
			byte[] publicExponentBytes = null;

			if (isWebSocket)
			{
				String strModulus = bigIntegerModulus.toString(16);
				String strPublicExponent = bigIntegerPublicExponent.toString(16);
				modulusBytes = ByteUtil.getBytes(strModulus);
				publicExponentBytes = ByteUtil.getBytes(strPublicExponent);
			}
			else
			{
				modulusBytes = bigIntegerModulus.toByteArray();
				publicExponentBytes = bigIntegerPublicExponent.toByteArray();
			}

			// String strModulus = bigIntegerModulus.toString();
			// String strPublicExponent=bigIntegerPublicExponent.toString(16);
			// modulusBytes=ByteUtil.getBytes(strModulus);
			// publicExponentBytes=ByteUtil.getBytes(strPublicExponent);

			ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.TCP, SocketDataStructureType.HANDSHAKE_TCP);
			socketSendablePacketClient.putInt(id);// 连接ID
			socketSendablePacketClient.putInt(connectionCheckCode);// 连接校验码
			socketSendablePacketClient.putInt(modulusBytes.length);
			socketSendablePacketClient.put(modulusBytes);
			socketSendablePacketClient.putInt(publicExponentBytes.length);
			socketSendablePacketClient.put(publicExponentBytes);

			// System.out.println("id:"+id);
			// System.out.println("connectionCheckCode:"+connectionCheckCode);
			// System.out.println("modulusBytes.length:"+modulusBytes.length);
			// System.out.println("modulusBytes:"+modulusBytes);
			// System.out.println("modulusBytes:"+modulusBytes.toString());
			// System.out.println("bigIntegerModulus:"+bigIntegerModulus.toString(16));
			// System.out.println("publicExponentBytes.length:"+publicExponentBytes.length);
			// System.out.println("publicExponentBytes:"+publicExponentBytes);
			// System.out.println("publicExponentBytes.intValue:"+bigIntegerPublicExponent.intValue());
			// System.out.println("bigIntegerPublicExponent:"+bigIntegerPublicExponent.toString(16));
			//			System.out.println("接收端(服务器端)TCP向发送端(客户端)TCP发送协议握手数据 2："+this.socketTcpConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+this.socketTcpConnection.getSocketChannel().socket().getPort());
			sendPacket(socketSendablePacketClient);
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * 发送端(客户端)TCP向接收端(服务端)TCP发送协议握手成功应答数据
	 * @param cryptoKeys 通讯加密密码
	 */
	public void tcpToTcpHandshakeAck(byte[] cryptoKeys)
	{
		//		System.out.println("发送端(客户端)TCP向接收端(服务端)TCP发送协议握手成功应答数据："+this.socketTcpConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+this.socketTcpConnection.getSocketChannel().socket().getPort());
		ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.TCP, SocketDataStructureType.HANDSHAKE_TCP_ACK);
		socketSendablePacketClient.put(cryptoKeys);// 通讯加密密码
		sendPacket(socketSendablePacketClient);
	}

	/**
	 * 接收端(服务端)TCP向发送端(客户端)TCP发送协议握手成功应答的应答数据
	 */
	public void tcpToTcpHandshakeAckAck()
	{
		//		System.out.println("SocketConnection tcpToTcpHandshakeAckAck 接收端(服务端)TCP向发送端(客户端)TCP发送协议握手成功应答的应答数据："+this.socketTcpConnection.getSocketChannel().socket().getInetAddress().getHostAddress()+":"+this.socketTcpConnection.getSocketChannel().socket().getPort());
		ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.TCP, SocketDataStructureType.HANDSHAKE_TCP_ACK_ACK);
		sendPacket(socketSendablePacketClient);
	}

	/**
	 * 发送端(客户端)UDP向接收端(服务器端)UDP发送UDP协议握手数据，数据内容就为包头中的连接ID与连接校验码
	 */
	public void udpToUdpHandshake()
	{
		ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.HANDSHAKE_UDP);
		sendPacket(socketSendablePacketClient);
	}

	//	/**
	//	 * UDP协议连接握手成功，接收端(服务器端)TCP向发送端(客户端)TCP发送UDP成功握手消息
	//	 * @param type 握手方式:0=udp自行握手;1=通过tcp握手
	//	 */
	//	public void udpToUdpHandshakeAck(byte type)
	//	{
	//		this.handshakeCompleteUdp=true;
	//		if(type==1)
	//		{// 通过TCP通道发送UDP协议握手成功ACK
	//			ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.HANDSHAKE_UDP_ACK);
	//			socketSendablePacketClient.put(type);
	//			sendPacket(socketSendablePacketClient);
	//		}
	//		else
	//		{//udp自行握手
	//			ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.HANDSHAKE_UDP_ACK);
	//			socketSendablePacketClient.put(type);
	//			// 取得RSA公、私钥
	//			publicKey = (RSAPublicKey) rsaKeys.get("public");
	//			privateKey = (RSAPrivateKey) rsaKeys.get("private");
	//			BigInteger bigIntegerModulus = publicKey.getModulus();
	//			BigInteger bigIntegerPublicExponent = publicKey.getPublicExponent();
	//
	//			byte[] modulusBytes = null;
	//			byte[] publicExponentBytes = null;
	//			modulusBytes = bigIntegerModulus.toByteArray();
	//			publicExponentBytes = bigIntegerPublicExponent.toByteArray();
	//
	//			socketSendablePacketClient.putInt(id);// 连接ID
	//			socketSendablePacketClient.putInt(connectionCheckCode);// 连接校验码
	//			socketSendablePacketClient.putInt(modulusBytes.length);
	//			socketSendablePacketClient.put(modulusBytes);
	//			socketSendablePacketClient.putInt(publicExponentBytes.length);
	//			socketSendablePacketClient.put(publicExponentBytes);
	//			
	//			sendPacket(socketSendablePacketClient);
	//		}
	//	}

	//	/**
	//	 * UDP协议连接握手成功，接收端(服务器端)TCP向发送端(客户端)TCP发送UDP成功握手消息应答的应答
	//	 * @param type 握手方式:0=udp自行握手;1=通过tcp握手
	//	 */
	//	public void udpToUdpHandshakeAckAck()
	//	{
	//		ISocketSendablePacketBase socketSendablePacketClient = SocketUtil.socketSendablePacketClient(SocketType.UDP, SocketDataStructureType.HANDSHAKE_UDP_ACK_ACK_ACK);
	//		sendPacket(socketSendablePacketClient);
	//	}

	/**
	 * 接收端(服务器端)WebSocket向发送端(客户端)WebSocket发送协议握手数据
	 */
	public void webSocketToWebSocketHandshake(String receiveText)
	{
		lock.readLock().lock();
		try
		{
			String key = "";
			Pattern pattern = Pattern.compile("Sec-WebSocket-Key:(.*?)" + System.lineSeparator());
			Matcher matcher = pattern.matcher(receiveText);
			if (matcher.find())
			{
				key = matcher.group(0);
				String[] keys = key.split(":");
				if (keys != null && keys.length > 1)
				{
					key = keys[1].replace("\r\n", "").trim();
				}
			}
			BASE64Encoder base64Encoder = new BASE64Encoder();
			MessageDigest md = null;
			md = MessageDigest.getInstance("SHA-1");
			byte[] encryptionBytes = md.digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes());
			key = base64Encoder.encode(encryptionBytes);

			String handshakeText = "HTTP/1.1 101 Switching Protocols\r\n";
			handshakeText += "Upgrade: websocket\r\n";
			handshakeText += "Connection: Upgrade\r\n";
			handshakeText += "Sec-WebSocket-Accept: " + key + "\r\n\r\n";
			// 如果把上一行换成下面两行，才是thewebsocketprotocol-17协议，但居然握手不成功，目前仍没弄明白！
			// responseBuilder.Append("Sec-WebSocket-Accept: " + secKeyAccept + Environment.NewLine);
			// responseBuilder.Append("Sec-WebSocket-Protocol: chat" + Environment.NewLine);
			ByteBuffer byteBuffer = ByteBuffer.wrap(handshakeText.getBytes());
			byteBuffer.order(SocketConfig.byteOrder);
			System.out.println("webSocketToWebSocketHandshake1");
			socketTcpConnection.send(byteBuffer);
			System.out.println("webSocketToWebSocketHandshake2");
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public void handleEvent(Event event)
	{
		switch (event.getType())
		{
			case SocketConnectionEvent.CLOSE_CONNECTION:
				byte closeType = (byte)event.getData();
				close(closeType);
				break;
			case SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP:
				receiveData(SocketType.UDP, (ByteBuffer) event.getData());
				receiveDataTcpUdp(SocketType.UDP);
				break;
			case SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE:
				keepAlive((SocketType) event.getData());
				break;
		}
	}

	/**
	 * 收到心跳
	 */
	private void keepAlive(SocketType socketType)
	{
		//		System.out.println("keepAlive 收到心跳包 socketType:"+socketType);
		keepAliveTime = DateUtil.getCurrentTimeMillis();
	}

	/**
	 * 连接心跳判断
	 * @param keepAliveTimeMax 心跳最大间隔毫秒时间
	 * @return 如果最后一次收到的心跳时间超过了指定的最大间隔时间，返回false,否则返回true
	 */
	public boolean checkKeepAlive(long keepAliveTimeMax)
	{
		if(DateUtil.getCurrentTimeMillis() - keepAliveTime > keepAliveTimeMax)
		{//如果最后一次收到的心跳时间超过了指定的最大间隔时间
			return false;
		}
		return true;
	}

	/**
	 * 关闭连接
	 */
	public void close()
	{
		close((byte)0);
	}

	/**
	 * 关闭连接
	 * @param closeType 关闭类型：0=服务器内部主动断开关闭;1=外部强制关闭(比如手动离开一个玩家时，从玩家类调用关闭);2=客户端主动断开网络关闭
	 */
	public void close(byte closeType)
	{
		lock.writeLock().lock();
		try
		{
			isAvailable = false;
			if (socketTcpConnection != null)
			{
				socketTcpConnection.removeEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				socketTcpConnection.dispose();
				socketTcpConnection = null;
			}
			if (socketUdpConnection != null)
			{
				socketUdpConnection.removeEventListener(SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP, this);
				socketUdpConnection.removeEventListener(SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE, this);
				socketUdpConnection.dispose();
				socketUdpConnection = null;
			}
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.CLOSE_CONNECTION, closeType));
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
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
			isAvailable = false;
			while (sendablePacketQueue != null && sendablePacketQueue.size() > 0)
			{
				ISocketSendablePacketBase sendablePacket = sendablePacketQueue.poll();
				if (sendablePacket != null)
				{
					sendablePacket.dispose();
					sendablePacket = null;
				}
			}
			sendablePacketQueue = null;
			if (socketTcpConnection != null)
			{
				socketTcpConnection.removeEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				socketTcpConnection.dispose();
				socketTcpConnection = null;
			}
			if (socketUdpConnection != null)
			{
				socketUdpConnection.removeEventListener(SocketConnectionEvent.RECEIVABLE_BYTE_BUFFER_UDP, this);
				socketUdpConnection.removeEventListener(SocketConnectionEvent.RECEIVABLE_KEEP_ALIVE, this);
				socketUdpConnection.dispose();
				socketUdpConnection = null;
			}
			if (receiveTcpByteBuffer != null)
			{
				receiveTcpByteBuffer.clear();
				receiveTcpByteBuffer = null;
			}
			if (receiveUdpByteBuffer != null)
			{
				receiveUdpByteBuffer.clear();
				receiveUdpByteBuffer = null;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketConnection.class.getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
}
