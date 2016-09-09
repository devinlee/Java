package base.net.socket;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.event.Event;
import base.event.EventDispatcher;
import base.net.NetFactory;
import base.net.socket.events.SocketConnectionEvent;
import base.net.socket.packet.ISocketSendablePacketBase;
import base.net.socket.tcp.SocketTcpClient;
import base.net.socket.tcp.SocketTcpKeepAlive;
import base.net.socket.udp.SocketUdpClient;
import base.net.socket.udp.SocketUdpKeepAlive;
import base.timer.TimerController;
import base.types.SocketType;

/**
 * Socket Client
 * @author Devin
 *
 */
public class SocketClient extends EventDispatcher implements ISocketServer
{
	/**
	 * Socket名称
	 */
	private String socketName;

	public String getSocketName()
	{
		return socketName;
	}

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
	 * Socket通讯模式
	 */
	private SocketType socketType;
	/**
	 * IP地址
	 */
	protected String ipAddress;
	/**
	 * 端口
	 */
	protected int port;

	/**
	 * 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID
	 */
	protected boolean dataTransfer;

	/**
	 * 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	private boolean isCrypto;

	/**
	 * 是否发送心跳包
	 */
	private boolean isSendKeepAlive;

	/**
	 * SocketChannel
	 */
	private SocketChannel socketChannel;
	/**
	 * 待发送包队列
	 */
	private BlockingQueue<ISocketSendablePacketBase> sendablePacketQueue = new LinkedBlockingQueue<ISocketSendablePacketBase>();

	/**
	 * Socket基础连接处理类
	 */
	private ISocketConnection socketConnection;

	/**
	 * Socket基础连接处理类
	 * @return
	 */
	public ISocketConnection getSocketConnectionBase()
	{
		return socketConnection;
	}

	/**
	 * Socket Tcp客户端
	 */
	private SocketTcpClient socketTcpClient;
	/**
	 * Socket Udp客户端
	 */
	private SocketUdpClient socketUdpClient;

	/**
	 * 保持Socket Tcp 连接存活的心跳发送任务
	 */
	private SocketTcpKeepAlive socketTcpKeepAlive;

	/**
	 * 保持Socket Udp 连接存活的心跳发送任务
	 */
	private SocketUdpKeepAlive socketUdpKeepAlive;

	private SocketSelector socketSelector;

	/**
	 * ByteBuffer池
	 */
	private SocketByteBufferPool socketByteBufferPool;

	public SocketSelector getSocketSelector()
	{
		return socketSelector;
	}

	/**
	 * Socket Client
	 * @param socketSelector SocketSelector
	 * @param clientName 客户端名称
	 * @param socketType Socket通讯模式
	 * @param ipAddress 连接的服务端ip地址
	 * @param port 连接的服务端端口
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @param isSendKeepAlive 是否发送心跳包
	 */
	public SocketClient(SocketSelector socketSelector, String clientName, SocketType socketType, String ipAddress, int port, boolean dataTransfer, boolean isCrypto, boolean isSendKeepAlive)
	{
		this.socketSelector = socketSelector;
		this.socketName = clientName;
		this.socketType = socketType;
		this.ipAddress = ipAddress;
		this.port = port;
		this.dataTransfer = dataTransfer;
		this.isCrypto = isCrypto;
		this.isSendKeepAlive = isSendKeepAlive;
		this.socketByteBufferPool = new SocketByteBufferPool(10000, SocketConfig.PACKET_TCP_LENGTN);
	}

	/**
	 * 开始服务
	 */
	public void startServer()
	{
		startServer(true);
	}

	/**
	 * 开始服务
	 */
	public void startServer(Boolean isStartSocketSelector)
	{
		try
		{
			socketConnection = NetFactory.socketController().createSocketConnectionBase(dataTransfer, isCrypto);
			socketConnection.addEventListener(SocketConnectionEvent.HANDSHAKE_COMPLETE, this);
			socketConnection.addEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
			socketConnection.setSocketByteBufferPool(socketByteBufferPool);
			if (socketType == SocketType.ALL || socketType == SocketType.TCP)
			{
				socketTcpClient = new SocketTcpClient(this, ipAddress, port, dataTransfer, isCrypto);
				socketTcpClient.setSocketByteBufferPool(socketByteBufferPool);
				socketTcpClient.startServer();
			}
			if (socketType == SocketType.ALL || socketType == SocketType.UDP)
			{
				socketUdpClient = new SocketUdpClient(this, ipAddress, port, dataTransfer, isCrypto);
				socketUdpClient.setSocketByteBufferPool(socketByteBufferPool);
				socketUdpClient.startServer();
			}
			if (isStartSocketSelector && socketSelector != null && !socketSelector.getIsAvailable() && !socketSelector.isAlive())
			{
				socketSelector.start();
			}
			isAvailable = true;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, "SocketClient " + socketName + " 运行错误。", e);
		}
	}

	private boolean connectableTcp;
	private boolean connectableUdp;
	/**
	 * 连接成功
	 * @param socketType 连接Socket类型
	 */
	public void connectable(SocketType socketType)
	{
		if(socketType == SocketType.TCP)
		{
			connectableTcp=true;
		}
		if(socketType == SocketType.UDP)
		{
			connectableUdp=true;
		}
		boolean isConnectable = false;
		if ((this.socketType == SocketType.ALL && connectableTcp&& connectableUdp)
				|| (this.socketType == SocketType.TCP && connectableTcp)
				|| (this.socketType == SocketType.UDP && connectableUdp))
		{
			isConnectable = true;
		}
		if (isConnectable)
		{
			if (socketType == SocketType.TCP)
			{
				socketConnection.clientToServerTcpConnecyionSuccess();// Tcp连接成功，发送至服务器告知成功
			}
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.CONNECTABLE, socketType));
		}
	}

	/**
	 * 协议握手成功
	 * @param socketType 连接Socket类型
	 */
	public void handshakeComplete(SocketType socketType)
	{
		boolean isHandshakeComplete = false;
		if ((this.socketType == SocketType.ALL && socketConnection.getHandshakeCompleteTcp() && socketConnection.getHandshakeCompleteUdp())
				|| (this.socketType == SocketType.TCP && socketConnection.getHandshakeCompleteTcp())
				|| (this.socketType == SocketType.UDP && socketConnection.getHandshakeCompleteUdp()))
		{
			isHandshakeComplete = true;
		}
		if (isSendKeepAlive)
		{
			setKeepAlive(10, 10);
		}
		if (isHandshakeComplete)
		{
			dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.HANDSHAKE_COMPLETE));
		}
	}

	/**
	 * 保持连接存活的心跳包发送
	 * @param tcpSecondTime tcp包发送时间秒
	 * @param udpSecondTime udp包发送时间秒
	 */
	private void setKeepAlive(int tcpSecondTime, int udpSecondTime)
	{
		if (socketType == SocketType.ALL || socketType == SocketType.TCP)
		{
			socketTcpKeepAlive = new SocketTcpKeepAlive(this);
			TimerController.timer("SocketKeepAlive", socketTcpKeepAlive, 0, tcpSecondTime * 1000);
		}
		if (socketType == SocketType.ALL || socketType == SocketType.UDP)
		{
			socketUdpKeepAlive = new SocketUdpKeepAlive(this);
			TimerController.timer("SocketKeepAlive", socketUdpKeepAlive, 0, udpSecondTime * 1000);
		}
	}

	/**
	 * 发送数据包
	 * @param socketSendablePacketBase 发送数据包
	 */
	public void send(ISocketSendablePacketBase socketSendablePacketBase)
	{
		try
		{
			sendablePacketQueue.put(socketSendablePacketBase);
			sendSendablePacketQueue();
		}
		catch (InterruptedException e)
		{
			Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, "将发送包插入发送队列时错误。", e);
		}
	}

	/**
	 * 执行发送包队列发送
	 */
	public void sendSendablePacketQueue()
	{
		if (isAvailable && socketConnection != null && socketConnection.getIsAvailable())
		{
			ISocketSendablePacketBase sendablePacket = null;
			while ((sendablePacket = sendablePacketQueue.poll()) != null)
			{// 循环取得发收包队列中的包予发送
				socketConnection.sendPacket(sendablePacket);
			}
		}
	}

	@Override
	public void handleEvent(Event event)
	{
		switch (event.getType())
		{
			case SocketConnectionEvent.HANDSHAKE_COMPLETE:
				handshakeComplete((SocketType) event.getData());
				break;
			case SocketConnectionEvent.CLOSE_CONNECTION:
				dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.CLOSE_CONNECTION));
				break;
		}
	}

	/**
	 * 关闭SocketClient
	 */
	public synchronized void close()
	{
		try
		{
			isAvailable = false;
			if (socketTcpKeepAlive != null)
			{
				socketTcpKeepAlive.cancel();
				socketTcpKeepAlive = null;
			}
			if (socketUdpKeepAlive != null)
			{
				socketUdpKeepAlive.cancel();
				socketUdpKeepAlive = null;
			}
			while (sendablePacketQueue != null && sendablePacketQueue.size() > 0)
			{
				ISocketSendablePacketBase sendablePacket = sendablePacketQueue.poll();
				if (sendablePacket != null)
				{
					sendablePacket.dispose();
					sendablePacket = null;
				}
			}
			if (socketConnection != null)
			{
				socketConnection.removeEventListener(SocketConnectionEvent.HANDSHAKE_COMPLETE, this);
				socketConnection.removeEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				socketConnection.close();
				socketConnection = null;
			}
			if (socketChannel != null)
			{
				socketChannel.close();
				socketChannel = null;
			}
			socketSelector = null;
		}
		catch (IOException e)
		{
			Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, "SocketClient关闭时错误。", e);
		}
	}
}
