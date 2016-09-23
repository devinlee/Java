package base.net.socket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import base.event.Event;
import base.event.EventDispatcher;
import base.net.socket.events.SocketConnectionEvent;
import base.net.socket.tcp.SocketTcpServer;
import base.net.socket.udp.SocketUdpServer;
import base.thread.ThreadsFactory;
import base.timer.TimerController;
import base.types.SocketType;
import base.utils.RSAUtil;

/**
 * Socket Server
 * @author Devin
 *
 */
public class SocketServer extends EventDispatcher implements ISocketServer
{
	private SocketSelector socketSelector;

	public SocketSelector getSocketSelector()
	{
		return socketSelector;
	}

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
	/* 服务是否可用
	 */
	public void setAvailable(boolean isAvailable)
	{
		this.isAvailable = isAvailable;
	}

	/**
	/* 服务是否可用
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
	 * 连接的客户端池大小
	 */
	protected int poolSize;

	public int getPoolSize()
	{
		return poolSize;
	}

	/**
	 * 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 */
	protected boolean dataTransfer;

	/**
	 * 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @return dataTransfer
	 */
	public boolean isDataTransfer()
	{
		return dataTransfer;
	}

	/**
	 * 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	private boolean isCrypto;
	/**
	 * 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 */
	public boolean isCrypto()
	{
		return this.isCrypto;
	}
	
	/**
	 * 是否检测心跳时间
	 */
	private boolean isCheckKeepAlive;

	/**
	 * Socket通讯模式
	 */
	private SocketType socketType;

	/**
	 * Socket TCP服务
	 */
	private SocketTcpServer socketTcpServer;

	/**
	 * Socket UDP服务
	 */
	private SocketUdpServer socketUdpServer;
	
	/**
	 * 连接握手处理器
	 */
	private SocketServerHandshake socketServerHandshake;
	
	/**
	 * 读取数据处理线程池
	 */
	protected ExecutorService connectionReadPools;

	/**
	 * 当前Socket服务最大的全局Socket连接编号，该编号每加入一个SocketConnectionBase会自增长1
	 */
	private int socketConnectionBaseMaxGlobalNumber = 1;

	/**
	 * SocketConnectionBase列表容器
	 */
	private final ConcurrentHashMap<Integer, ISocketConnection> socketConnections = new ConcurrentHashMap<Integer, ISocketConnection>();

	/**
	 * 连接的心跳检测
	 */
	private SocketServerKeepAlive socketServerKeepAlive;
	
	/**
	 * ByteBuffer池
	 */
	private SocketByteBufferPool socketByteBufferPool;
	
	/**
	 * RSA密钥
	 */
	private HashMap<String, Object> rsaKeys;
	
	/**
	 * Socket Server
	 * @param serverName 服务名称
	 * @param socketType Socket通讯模式
	 * @param ipAddress 绑定ip地址
	 * @param port 端口
	 * @param byteBufferCapacity 缓冲区大小
	 * @param poolSize 连接的客户端池大小
	 * @param dataTransfer 是否为数据传递服务。当值为true时，则在收取数据包时，会读取初始来源的Socket命令ID，否则不读取初始来源的Socket命令ID。
	 * @param isCrypto 通讯数据是否安全加密(不加密包体以及不加入包尾校验串)
	 * @param isCheckKeepAlive 是否检测心跳时间
	 */
	public SocketServer(SocketSelector socketSelector, String serverName, SocketType socketType, String ipAddress, int port, int poolSize, boolean dataTransfer, boolean isCrypto, boolean isCheckKeepAlive)
	{
		this.socketSelector = socketSelector;
		this.socketName = serverName;
		this.socketType = socketType;
		this.ipAddress = ipAddress;
		this.port = port;
		this.poolSize = poolSize;
		this.dataTransfer = dataTransfer;
		this.isCrypto = isCrypto;
		this.isCheckKeepAlive=isCheckKeepAlive;
		this.socketByteBufferPool = new SocketByteBufferPool(10000, SocketConfig.PACKET_TCP_LENGTN);
		this.rsaKeys = RSAUtil.getKeys();
		this.connectionReadPools = ThreadsFactory.threadPoolController().create("SocketServerConnectionReadPools", Runtime.getRuntime().availableProcessors() + 10);
		this.socketServerHandshake=new SocketServerHandshake(this, connectionReadPools, socketByteBufferPool);
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
			if (socketType == SocketType.ALL || socketType == SocketType.TCP)
			{
				socketTcpServer = new SocketTcpServer(this, ipAddress, port, poolSize, dataTransfer, isCrypto);
				socketTcpServer.setSocketByteBufferPool(socketByteBufferPool);
				socketTcpServer.setConnectionReadPools(connectionReadPools);
				socketTcpServer.setSocketServerHandshake(socketServerHandshake);
				socketTcpServer.startServer();
			}
			if (socketType == SocketType.ALL || socketType == SocketType.UDP)
			{
				socketUdpServer = new SocketUdpServer(this, ipAddress, port, poolSize, dataTransfer, isCrypto);
				socketUdpServer.setSocketByteBufferPool(socketByteBufferPool);
				socketUdpServer.setConnectionReadPools(connectionReadPools);
				socketUdpServer.setSocketServerHandshake(socketServerHandshake);
				socketUdpServer.startServer();
			}
			if (isStartSocketSelector && socketSelector != null && !socketSelector.getIsAvailable() && !socketSelector.isAlive())
			{
				socketSelector.start();
			}
			isAvailable = true;
			if(isCheckKeepAlive)
			{
				socketKeepAlive();
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, "SocketServer " + socketName + " 运行时错误。", e);
		}
	}
	
	/**
	 * 线程读写锁
	 */
	private final ReadWriteLock connectionIDCheckCodeLock = new ReentrantReadWriteLock();
	
	/**
	 * 取得一组连接ID以及连接校验码
	 * @return
	 */
	public int[] getConnectionIDCheckCode()
	{
		connectionIDCheckCodeLock.writeLock().lock();
		try
		{
			int[] codes=new int[]{socketConnectionBaseMaxGlobalNumber++, new Random().nextInt(Integer.MAX_VALUE)};
			return codes;
		}
		catch (Exception e)
		{
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
		finally
		{
			connectionIDCheckCodeLock.writeLock().unlock();
		}
		return null;
	}

	/**
	 * 增加SocketConnectionBase
	 * @param socketConnection SocketConnectionBase对象
	 * @param connectionID 连接ID
	 * @param connectionCheckCode 连接校验码
	 */
	public boolean addSocketConnectionBase(ISocketConnection socketConnection, int connectionID, int connectionCheckCode)
	{
		boolean result = false;
		try
		{
			if (socketConnection != null)
			{
				socketConnection.addEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				socketConnection.addEventListener(SocketConnectionEvent.HANDSHAKE_COMPLETE, this);
				socketConnection.setMark(connectionID, connectionCheckCode);
				socketConnection.setSocketByteBufferPool(socketByteBufferPool);
				socketConnection.setRSAKeys(rsaKeys);
				socketConnections.put(connectionID, socketConnection);
				dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.NEW_CONNECTION, socketConnection));
				result = true;
			}
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, "SocketServer " + socketName + " addSocketBase 时错误。", e);
		}
		return result;
	}

	/**
	 * 根据ID标识取得一个SocketConnectionBase
	 * @param id ID标识
	 * @return 如果已标识的列表容器中存在相应标识ID的SocketConnectionBase，则返回对应的SocketConnectionBase，否则返回null
	 */
	public ISocketConnection getSocketConnectionBaseById(int id)
	{
		ISocketConnection socketConnection = null;
		try
		{
			socketConnection = socketConnections.get(id);
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, "SocketServer " + socketName + " getSocketConnectionBaseById 时错误。", e);
		}
		return socketConnection;
	}

	/**
	 * 根据ID标识取得一个SocketConnectionBase
	 * @param id ID标识
	 * @param connectionCheckCode 连接校验码
	 * @return 如果已标识的列表容器中存在相应标识ID的SocketConnectionBase，则返回对应的SocketConnectionBase，否则返回null
	 */
	public ISocketConnection getSocketConnectionBaseById(int id, int connectionCheckCode)
	{
		ISocketConnection socketConnection = getSocketConnectionBaseById(id);
		if (socketConnection != null && socketConnection.getConnectionCheckCode() == connectionCheckCode)
		{
			return socketConnection;
		}
		return null;
	}

	/**
	 * 移除SocketConnectionBase
	 * @param socketConnection SocketConnectionBase连接对象
	 */
	public boolean removeSocketConnection(ISocketConnection socketConnection)
	{
		boolean result = false;
		try
		{
			if (socketConnection != null)
			{
				socketConnection.removeEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
				socketConnection.removeEventListener(SocketConnectionEvent.HANDSHAKE_COMPLETE, this);
				socketConnections.remove(socketConnection.getId());
				dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.REMOVE_CONNECTION, socketConnection));
			}
			result = true;
		}
		catch (Exception e)
		{
			result = false;
			Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, "SocketServer " + socketName + " removeSocketBase 时错误。", e);
		}
		return result;
	}

	/**
	 * 取得SocketConnectionBase总数量
	 * @return 当前SocketConnectionBase总数量
	 */
	public int getSocketConnectionTotalNumber()
	{
		return socketConnections.size();
	}

	@Override
	public void handleEvent(Event event)
	{
		switch (event.getType())
		{
			case SocketConnectionEvent.CLOSE_CONNECTION:
				ISocketConnection socketConnection = (ISocketConnection) event.getCurrentTarget();
				if (socketConnection != null)
				{
					removeSocketConnection(socketConnection);
					socketConnection.dispose();
					socketConnection = null;
				}
				break;
			case SocketConnectionEvent.HANDSHAKE_COMPLETE:
				socketConnection = (ISocketConnection) event.getCurrentTarget();
				if (socketConnection != null)
				{
					dispatchEvent(new SocketConnectionEvent(SocketConnectionEvent.HANDSHAKE_COMPLETE, socketConnection));
				}
				break;
		}
	}
	
	/**
	 * 连接的心跳检测处理
	 */
	private void socketKeepAlive()
	{
		socketServerKeepAlive = new SocketServerKeepAlive(socketConnections);
		TimerController.timer("SocketServerKeepAlive", socketServerKeepAlive, 0, 15000);
	}

	public synchronized void close()
	{
		try
		{
			isAvailable = false;
			Collection<ISocketConnection> socketBaseList = socketConnections.values();
			for (ISocketConnection socketConnection : socketBaseList)
			{
				if (socketConnection != null)
				{
					socketConnection.removeEventListener(SocketConnectionEvent.CLOSE_CONNECTION, this);
					socketConnection.removeEventListener(SocketConnectionEvent.HANDSHAKE_COMPLETE, this);
					socketConnection.close();
					socketConnection.dispose();
					socketConnection = null;
				}
			}
			socketConnections.clear();
			if (socketTcpServer != null)
			{
				socketTcpServer.close();
				socketTcpServer = null;
			}
			socketSelector = null;
		}
		catch (Exception e)
		{
			Logger.getLogger(SocketServer.class.getName()).log(Level.SEVERE, "SocketServer " + socketName + " 关闭时错误。", e);
		}
	}
}
