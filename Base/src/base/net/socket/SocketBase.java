package base.net.socket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.net.socket.tcp.SocketTcpServer;
import base.net.socket.thread.SocketReadThread;
import base.types.SocketType;

/**
 * Socket服务基类
 * @author Devin
 *
 */
public abstract class SocketBase
{
	/**
	 * 服务是否可用
	 */
	protected boolean isAvailable = false;

	/**
	 * 服务是否可用
	 */
	public void setAvailable(boolean isAvailable)
	{
		this.isAvailable = isAvailable;
	}

	/**
	 * 服务是否可用
	 */
	public boolean isAvailable()
	{
		return isAvailable;
	}

	/**
	 * IP地址
	 */
	protected String ipAddress;
	/**
	 * 端口
	 */
	protected int port;

	/**
	 * Selector
	 */
	protected Selector selector;

	/**
	 * 读取数据处理线程池
	 */
	protected ExecutorService connectionReadPools;
	/**
	 * 读取数据处理线程池
	 */
	public void setConnectionReadPools(ExecutorService connectionReadPools)
	{
		this.connectionReadPools=connectionReadPools;
	}

	/**
	 * 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 */
	protected boolean dataTransfer;
	/**
	 * 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	protected boolean isCrypto;

	private SocketType socketType;

	/**
	 * ByteBuffer池
	 */
	protected SocketByteBufferPool socketByteBufferPool;
	/**
	 * ByteBuffer池
	 */
	public void setSocketByteBufferPool(SocketByteBufferPool socketByteBufferPool)
	{
		this.socketByteBufferPool=socketByteBufferPool;
	}
	
	/**
	 * 连接握手处理器
	 */
	protected SocketServerHandshake socketServerHandshake;
	/**
	 * 连接握手处理器
	 */
	public void setSocketServerHandshake(SocketServerHandshake socketServerHandshake)
	{
		this.socketServerHandshake=socketServerHandshake;
	}

	/**
	 * Socket服务基类
	 * @param serverName Socket服务名称
	 * @param ipAddress 连接IP地址
	 * @param port 端口
	 * @param clientPoolSize 连接处理线程最大数
	 * @param socketBaseQueue 待分配标识的SocketBase队列
	 * @param socketConnectionBases 已标识的SocketBase列表容器
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @throws Exception
	 */
	public SocketBase(SocketType socketType, String ipAddress, int port, boolean dataTransfer, boolean isCrypto) throws Exception
	{
		this.socketType = socketType;
		this.ipAddress = ipAddress;
		this.port = port;
		this.dataTransfer = dataTransfer;
		this.isCrypto = isCrypto;
	}

	/**
	 * 开始服务
	 */
	protected void startServer()
	{
	}

	/**
	 * 处理 SelectionKey
	 * @param selectionKey SelectionKey
	 * @param socketConnection SocketConnection
	 * @throws Exception
	 */
	public void handleSelectionKey(SelectionKey selectionKey, ISocketConnection socketConnection)
	{
		try
		{
			if (isAvailable && selectionKey.isValid() && selectionKey.isReadable())
			{// 有可读信息
				// 处理读信息
				if (socketConnection != null)
				{
					ByteBuffer receiveByteBuffer;
					if (socketType == SocketType.TCP)
					{
						receiveByteBuffer = socketByteBufferPool.getByteBuffer();
						receiveByteBuffer.clear();
						SocketChannel channel = (SocketChannel) selectionKey.channel();
						int len = channel.read(receiveByteBuffer);
						receiveByteBuffer.flip();
						if (len > 0)
						{
							//							if(len>30)
							//							{
							//								System.out.println(receiveByteBuffer.getInt());
							//								System.out.println(receiveByteBuffer.get());
							//								System.out.println(receiveByteBuffer.get());
							//								System.out.println(receiveByteBuffer.getShort());
							//								System.out.println(receiveByteBuffer.getInt());
							//								System.out.println(receiveByteBuffer.getLong());
							//								System.out.println(receiveByteBuffer.getFloat());
							//								System.out.println(receiveByteBuffer.getDouble());
							//							}
							// if(!socketConnection.isHandshakeCompleteTcp())
							// {//如果当前连接没有握手成功过，当客户端为websocket时，需要进行websocket的握手操作
							// byte[] bytes=new byte[len];
							// receiveByteBuffer.get(bytes, 0, len);
							// String receiveText = new String(bytes, 0, len, "UTF-8");
							// bytes=null;
							// boolean isWebSocket=WebSocketUtil.isWebSocket(receiveText);//是否为WebSocket连接
							// if(isWebSocket)
							// {//如果为WebSocket连接，则执行连接握手
							// socketConnection.setIsWebSocket(true);
							// socketConnection.webSocketToWebSocketHandshake(receiveText);
							// receiveByteBuffer.clear();
							// receiveByteBuffer=null;
							// return;
							// }
							// else
							// {
							// receiveByteBuffer.flip();//如果不是websocket连接，需要把读取过的数据重置一次为未读，交于后面的数据处理
							// }
							// }
							socketConnection.receiveData(SocketType.TCP, receiveByteBuffer);
							SocketReadThread socketReadThread = new SocketReadThread(socketType, socketConnection);
							connectionReadPools.execute(socketReadThread);
						}
						else if (len < 0)
						{
							receiveByteBuffer = null;
							socketConnection.close((byte)2);// 小于0，通道已无效，关闭连接
						}
						else
						{
							receiveByteBuffer = null;
						}
					}
					else if (socketType == SocketType.UDP)
					{
						receiveByteBuffer = socketByteBufferPool.getByteBuffer();
						receiveByteBuffer.clear();
						DatagramChannel channel = (DatagramChannel) selectionKey.channel();
						SocketAddress socketAddress = channel.receive(receiveByteBuffer);
						if (socketAddress != null)
						{
							receiveByteBuffer.flip();
							if (receiveByteBuffer.hasRemaining())
							{
								if (socketConnection != null && socketConnection.getHandshakeCompleteUdp())
								{
									socketConnection.receiveData(SocketType.UDP, receiveByteBuffer);
									SocketReadThread socketReadThread = new SocketReadThread(socketType, socketConnection);
									connectionReadPools.execute(socketReadThread);
								}
							}
						}
					}
				}
			}
			else if (isAvailable && selectionKey.isValid() && selectionKey.isWritable())
			{
				System.out.println("selectionKey.isValid() && selectionKey.isWritable()");
			}
		}
		catch (NotYetConnectedException e)
		{
			removeSocketConnection(socketConnection);
			socketConnection.close();
			selectionKey.attach(null);
			selectionKey.cancel();
			Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "尚未连接此通道 。", e);
		}
		catch (ClosedByInterruptException e)
		{
			removeSocketConnection(socketConnection);
			socketConnection.close();
			selectionKey.attach(null);
			selectionKey.cancel();
			Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "正在进行读取操作时另一个线程中断了当前线程，因此关闭了该通道并将当前线程设置为中断状态 。", e);
		}
		catch (AsynchronousCloseException e)
		{
			removeSocketConnection(socketConnection);
			socketConnection.close();
			selectionKey.attach(null);
			selectionKey.cancel();
			Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "正在进行读取操作时另一个线程关闭了此通道 。", e);
		}
		catch (ClosedChannelException e)
		{
			removeSocketConnection(socketConnection);
			socketConnection.close();
			selectionKey.attach(null);
			selectionKey.cancel();
			Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "此通道已关闭 。", e);
		}
		catch (IOException e)
		{
			removeSocketConnection(socketConnection);
			socketConnection.close();
			selectionKey.attach(null);
			selectionKey.cancel();
			if(!e.getMessage().contains("远程主机强迫关闭了一个现有的连接") && !e.getMessage().contains("Connection reset by peer"))
			{
				Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "发生其他 I/O 错误 。", e);
			}
		}
		catch (Exception e)
		{
			removeSocketConnection(socketConnection);
			socketConnection.close();
			selectionKey.attach(null);
			selectionKey.cancel();
			Logger.getLogger(SocketTcpServer.class.getName()).log(Level.SEVERE, "SocketBase读运行错误。", e);
		}
	}

	/**
	 * 移除一个SocketConnection，方法必须重写才有效
	 * @param socketConnection SocketConnection对象
	 */
	protected void removeSocketConnection(ISocketConnection socketConnection)
	{
	}

	/**
	 *关闭服务 
	 */
	public synchronized void close()
	{
		try
		{
			isAvailable = false;
			socketByteBufferPool=null;
			selector = null;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketBase.class.getName()).log(Level.SEVERE, null, e);
		}
	}
}
